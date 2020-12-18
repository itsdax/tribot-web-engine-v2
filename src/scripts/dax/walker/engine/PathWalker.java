package scripts.dax.walker.engine;

import lombok.Getter;
import org.tribot.api2007.Player;
import org.tribot.api2007.Projection;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.interfaces.Painting;
import scripts.dax.common.AccurateMouse;
import scripts.dax.common.Distance;
import scripts.dax.common.Movement;
import scripts.dax.walker.data.WalkCondition;
import scripts.dax.walker.debug.DaxWalkerDebugger;
import scripts.dax.walker.engine.compute.Direction;
import scripts.dax.walker.engine.compute.PathAnalyzeResult;
import scripts.dax.walker.engine.compute.PathAnalyzer;
import scripts.dax.walker.engine.compute.Pathfinder;
import scripts.dax.walker.engine.handlers.GenericObstacleHandler;
import scripts.dax.walker.engine.handlers.ScenarioHandler;
import scripts.dax.walker.engine.interaction.Teleport;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.tribot.api.General.*;
import static org.tribot.api.Timing.waitCondition;
import static org.tribot.api2007.Game.getDestination;
import static org.tribot.api2007.PathFinding.getCollisionData;
import static org.tribot.api2007.ext.Ships.crossGangplank;
import static org.tribot.api2007.ext.Ships.isOnShip;
import static scripts.dax.common.Constants.NULL_TILE;
import static scripts.dax.common.DaxLogger.*;
import static scripts.dax.walker.debug.DaxWalkerDebugger.Draw;
import static scripts.dax.walker.engine.PathWalker.State.*;
import static scripts.dax.walker.engine.handlers.ScenarioHandlers.AVAILABLE_HANDLERS;

@Getter
public class PathWalker implements Painting {

    private final int maxRetries;
    private final Lock pathLock;
    private final DaxWalkerDebugger daxWalkerDebugger;

    private RSTile[] currentPath;
    private int[][] collision;
    private Pathfinder.Node[][] parentMap;
    private PathAnalyzeResult pathAnalyzeResult;

    public PathWalker(int maxRetries) {
        this.maxRetries = maxRetries;
        this.pathLock = new ReentrantLock();
        this.daxWalkerDebugger = new DaxWalkerDebugger();
    }

    public enum State {
        HANDLED_SCENARIO_SUCCESSFULLY, COMPLETED_PATH, FAILED, EXIT_OUT_WALKER;
    }

    public boolean walk(RSTile[] path, WalkCondition walkCondition) {
        if (!pathLock.tryLock()) throw new IllegalStateException("Multiple threads calling PathWalker#walk");

        try {
            this.currentPath = path;
            if (!useTeleportForPath(path)) {
                warn("Failed to handle teleport");
                return false;
            }

            return walk(path, walkCondition, maxRetries);
        } finally {
            currentPath = null;
            pathLock.unlock();
        }
    }

    private boolean walk(RSTile[] path, WalkCondition walkCondition, int retryAttemptsLeft) {
        if (retryAttemptsLeft == 0) {
            warn("Exiting walker due to retries");
            return false;
        }

        if (path.length == 0) {
            warn("Cannot walk empty path");
            return false;
        }

        collision = getCollisionData();
        parentMap = Pathfinder.parentMap(collision);
        pathAnalyzeResult = PathAnalyzer.compute(path, parentMap);
        switch (handlePathHandle(path, pathAnalyzeResult, retryAttemptsLeft, walkCondition)) {
            case HANDLED_SCENARIO_SUCCESSFULLY -> {
                return walk(path, walkCondition, maxRetries); // reset max retries
            }
            case COMPLETED_PATH -> {
                info("Finished walking path");
                return true;
            }
            case FAILED -> {
                sleep(1000, 3000);
                return walk(path, walkCondition, retryAttemptsLeft - 1); // consume a retry
            }
            case EXIT_OUT_WALKER -> {
                info("Condition triggered to exit out walker before completing path");
                return false;
            }
        }

        return false;
    }

    private State handlePathHandle(RSTile[] path, PathAnalyzeResult analyzed, int retryAttemptsLeft, WalkCondition walkCondition) {
        if (analyzed.getFurthestReachable() == null)
            throw new IllegalStateException("Invalid furthest reachable analyze result");

        if (isOnShip()) return handleShip();

        RSTile walkingTowards = ofNullable(getDestination()).orElse(Player.getPosition());

        // Most basic case; Just walk to the furthest tile in path
        if (walkingTowards.distanceToDouble(analyzed.getFurthestReachable()) >= 3)
            return simpleWalkToTile(randomize(analyzed.getFurthestReachable()), walkCondition);

        // We've arrived
        if (analyzed.getFurthestReachable().equals(lastTileOfPath(path))) return COMPLETED_PATH;

        // We need to handle special case since something is preventing us from navigating [FROM] => [TO]
        RSTile from = analyzed.getFurthestReachable();
        RSTile to = analyzed.getTileAfterFurthestReachable();

        info("Special case %s => %s", from, to);

        // Generic handler did not detect a general case. Check custom handlers
        List<ScenarioHandler> handlers = Arrays.stream(AVAILABLE_HANDLERS)
                .filter(scenarioHandler -> scenarioHandler.canHandle(from, to))
                .collect(Collectors.toList());

        // Conflicting handlers. Each case should not have multiple custom handlers
        if (handlers.size() > 1) throw new IllegalStateException(format("Clashing handlers for %s => %s", from, to));

        // We have a specific handler made for this scenario
        if (handlers.size() == 1) return handlers.get(0).handle(from, to, walkCondition);

        // Generic handler. Should handle most doors, gates, fences, tunnels, etc
        if (GenericObstacleHandler.canHandle(from, to)) return GenericObstacleHandler.handle(from, to, walkCondition);

        // Looks like we are out of options...
        if (retryAttemptsLeft > 1) { // On every other retry, just fail. On last retry, throw exception.
            warn("Failed to find suitable handler for %s => %s", from, to);
            sleep(3000, 6000);
            return FAILED;
        }

        error("No handler for %s => %s", from, to);
        error("Please write a ScenarioHandler OR add support in GenericObstacleHandler");
        error("ScenarioHandlers can be added in scripts.dax.walker.engine.handlers.custom.YOUR_HANDLER");
        error("GenericObstacleHandler is intended to support frequent scenarios. Don't write one-offs here");
        throw new IllegalStateException(format("No handler for %s => %s", from, to));
    }

    private static State simpleWalkToTile(RSTile tile, WalkCondition walkCondition) {
        if (!AccurateMouse.clickMinimap(tile)) {
            debug("Failed to click %s to walk to", tile);
            return FAILED;
        }

        sleep(600, 1200);

        if (!waitCondition(() -> Optional.ofNullable(getDestination()).orElse(NULL_TILE).equals(tile), 800)) {
            debug("Failed to click %s to walk to", tile);
            return FAILED;
        }

        final int distanceBeforeExit = randomSD(4, 11, 8, 2);
        Movement movement = new Movement();
        AtomicBoolean exitEarly = new AtomicBoolean(false);
        BooleanSupplier condition = () -> {
            sleep(80);
            if (!movement.isWalking()) return true;
            if (walkCondition.getAsBoolean()) {
                exitEarly.set(true);
                return true;
            }

            return Distance.from(tile) <= distanceBeforeExit;
        };

        waitCondition(condition, random(8000, 16000));

        if (exitEarly.get()) return EXIT_OUT_WALKER;

        return HANDLED_SCENARIO_SUCCESSFULLY;
    }

    /**
     *
     * @param tile main tile you are trying to reach
     * @return A random tile accounting for clickability, and reachability (computed via collision)
     */
    private RSTile randomize(RSTile tile) {
        RSTile local = tile.toLocalTile();

        List<RSTile> list = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (!direction.isValidDirection(local.getX(), local.getY(), collision)) continue;

            Point p = Projection.tileToMinimap(direction.of(tile));
            if (p == null || p.x == -1 || p.y == -1 || !Projection.isInMinimap(p)) continue;

            list.add(direction.of(tile));
        }

        return list.get(random(0, list.size() - 1));
    }

    private static State handleShip() {
        if (!crossGangplank()) {
            warn("Failed to cross ship gangplank");
            return FAILED;
        }

        if (!waitCondition(() -> !isOnShip(), random(8000, 12000))) {
            warn("Clicked cross gangplank but still on ship");
            return FAILED;
        }

        return HANDLED_SCENARIO_SUCCESSFULLY;
    }

    private static boolean useTeleportForPath(RSTile[] path) {
        RSTile startPosition = path[0];
        for (Teleport teleport : Teleport.values()) {
            // we cannot use this teleport
            if (!teleport.getRequirement().satisfies()) continue;

            // we are already here...
            if (teleport.isAtTeleportSpot()) continue;

            // this teleport is not suitable for this path.
            // we can only use teleports that are close to starting location
            if (Distance.between(startPosition, teleport.getLocation()) > 15) continue;

            info("Using teleport[%s]", teleport);
            if (!teleport.trigger()) {
                info("Failed to trigger teleport[%s]", teleport);
                return false;
            }
            if (!waitCondition(teleport::isAtTeleportSpot, 8000)) {
                info("Triggered teleport[%s] but never arrived at destination", teleport);
                return false;
            }

            return true;
        }

        // No valid teleport for path. Basically a success
        return true;
    }

    private static RSTile lastTileOfPath(RSTile[] path) {
        return path[path.length - 1];
    }

    @Override
    public void onPaint(Graphics graphics) {
        // Hold currentPath in local variable since currentPath can be null anytime
        RSTile[] path = currentPath;
        if (path == null) return;

        Graphics2D g = (Graphics2D) graphics;

        List<Draw> draws = new ArrayList<>();
        PathAnalyzeResult pathAnalyzeResult = this.pathAnalyzeResult;
        if (pathAnalyzeResult != null) {
            RSTile furthestReachable = pathAnalyzeResult.getFurthestReachable();
            if (furthestReachable != null) draws.add(new Draw(furthestReachable, new Color(255, 66, 66)));

            RSTile current = pathAnalyzeResult.getCurrentTile();
            if (current != null) draws.add(new Draw(current, new Color(30, 93, 255)));

            RSTile next = pathAnalyzeResult.getTileAfterFurthestReachable();
            if (next != null) draws.add(new Draw(next, new Color(255, 206, 30)));
        }

        daxWalkerDebugger.drawDebug(g, path, draws.toArray(Draw[]::new));
    }

}
