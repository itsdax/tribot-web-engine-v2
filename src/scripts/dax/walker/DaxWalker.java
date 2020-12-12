package scripts.dax.walker;

import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.interfaces.Painting;
import scripts.dax.common.DaxLogger;
import scripts.dax.walker.data.*;
import scripts.dax.walker.data.exceptions.AuthorizationException;
import scripts.dax.walker.data.exceptions.RateLimitException;
import scripts.dax.walker.data.exceptions.UnknownException;
import scripts.dax.walker.engine.Navigator;
import scripts.dax.walker.engine.interaction.Teleport;
import scripts.dax.walker.server.DaxWalkerServerClient;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DaxWalker implements Painting {

    private final DaxWalkerServerClient server;
    private final Navigator navigator;
    private boolean useTeleports;

    public DaxWalker(DaxWalkerServerClient server, Navigator navigator) {
        this.server = server;
        this.navigator = navigator;
    }

    /**
     * This condition will override the default walk condition that enables run for you.
     *
     * @param walkCondition condition to invoke while walker in middle of sleeps.
     *                      e.g While it is in middle of walking, check this condition periodically
     *
     *                      This can be used to eat food while HP is low, exit walker early if PKer
     *                      is visible, etc.
     */
    public void setGlobalCondition(WalkCondition walkCondition) {
        navigator.setWalkCondition(walkCondition);
    }

    public WalkState walkTo(Positionable positionable) {
        return walkTo(positionable, null);
    }

    public boolean isUseTeleports() {
        return useTeleports;
    }

    public void setUseTeleports(boolean useTeleports) {
        this.useTeleports = useTeleports;
    }

    /**
     * @param positionable  destination to walk to
     * @param walkCondition Will trigger along WITH with the global condition.
     * @return The result of walking the path
     */
    public WalkState walkTo(Positionable positionable, WalkCondition walkCondition) {
        RSTile playerPosition = Player.getPosition();
        if (playerPosition == null) {
            DaxLogger.warn("Unable to grab player position to walk to destination");
            return WalkState.FAILED;
        }

        DaxLogger.info("Walking from %s => %s", playerPosition, positionable);

        List<PathRequestPair> pathRequestPairs = useTeleports ? getPathTeleports(positionable.getPosition()) : new ArrayList<>();
        pathRequestPairs.add(new PathRequestPair(toPoint3D(playerPosition), toPoint3D(positionable.getPosition())));

        DaxLogger.debug("A total of %s api calls consumed for walk to destination", pathRequestPairs.size());

        BulkPathRequest request = new BulkPathRequest(PlayerDetails.generate(), pathRequestPairs);
        try {
            return navigator.walk(server.getPaths(request), walkCondition) ? WalkState.SUCCESS : WalkState.FAILED;
        } catch (RateLimitException e) {
            return WalkState.RATE_LIMIT;
        } catch (AuthorizationException | UnknownException e) {
            return WalkState.ERROR;
        }
    }

    public WalkState walkToBank() {
        return walkToBank(null, null);
    }

    public WalkState walkToBank(WalkCondition walkCondition) {
        return walkToBank(null, walkCondition);
    }

    public WalkState walkToBank(RSBank bank) {
        return walkToBank(bank, null);
    }

    /**
     * @param bank          bank to walk to. leave null to walk to closest bank
     * @param walkCondition Will trigger WITH the global condition.
     * @return The result of walking the path
     */
    public WalkState walkToBank(RSBank bank, WalkCondition walkCondition) {
        if (bank != null) return walkTo(toRSTile(bank.getPoint3D()));

        RSTile playerPosition = Player.getPosition();
        if (playerPosition == null) {
            DaxLogger.warn("Unable to grab player position to walk to closest bank");
            return WalkState.FAILED;
        }

        DaxLogger.info("Walking to closest bank starting from %s", playerPosition);

        List<BankPathRequestPair> pathRequestPairs = useTeleports ? getBankPathTeleports() : new ArrayList<>();
        pathRequestPairs.add(new BankPathRequestPair(toPoint3D(playerPosition), null));

        DaxLogger.debug("A total of %s api calls consumed for closest bank", pathRequestPairs.size());

        BulkBankPathRequest request = new BulkBankPathRequest(PlayerDetails.generate(), pathRequestPairs);
        try {
            return navigator.walk(server.getBankPaths(request), walkCondition) ? WalkState.SUCCESS : WalkState.FAILED;
        } catch (RateLimitException e) {
            return WalkState.RATE_LIMIT;
        } catch (AuthorizationException | UnknownException e) {
            return WalkState.ERROR;
        }
    }

    private List<BankPathRequestPair> getBankPathTeleports() {
        return Teleport.getValidStartingRSTiles().stream()
                .map(position -> new BankPathRequestPair(toPoint3D(position), null))
                .collect(Collectors.toList());

    }

    private List<PathRequestPair> getPathTeleports(RSTile start) {
        return Teleport.getValidStartingRSTiles().stream()
                .map(position -> new PathRequestPair(toPoint3D(position), toPoint3D(start)))
                .collect(Collectors.toList());
    }

    private static Point3D toPoint3D(RSTile tile) {
        return new Point3D(tile.getX(), tile.getY(), tile.getPlane());
    }

    private static RSTile toRSTile(Point3D point3D) {
        return new RSTile(point3D.getX(), point3D.getY(), point3D.getZ(), RSTile.TYPES.WORLD);
    }

    @Override
    public void onPaint(Graphics graphics) {
        navigator.onPaint(graphics);
    }
}
