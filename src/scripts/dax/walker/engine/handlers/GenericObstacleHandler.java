package scripts.dax.walker.engine.handlers;

import lombok.AllArgsConstructor;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSModel;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSObjectDefinition;
import org.tribot.api2007.types.RSTile;
import scripts.dax.common.Closest;
import scripts.dax.common.Distance;
import scripts.dax.walker.data.WalkCondition;
import scripts.dax.walker.engine.Interact;
import scripts.dax.walker.engine.PathWalker.State;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparingDouble;
import static org.tribot.api2007.Projection.tileToScreen;
import static scripts.dax.common.DaxLogger.debug;
import static scripts.dax.common.DaxLogger.warn;
import static scripts.dax.walker.engine.PathWalker.State.FAILED;
import static scripts.dax.walker.engine.compute.Pathfinder.canWalkTo;
import static scripts.dax.walker.engine.compute.Pathfinder.parentMap;

public class GenericObstacleHandler {

    public static boolean canHandle(RSTile from, RSTile to) {
        Vertical vertical = Vertical.determine(from, to);
        if (vertical != null) return true;
        if (canHandleDoor(from, to)) return true;

        // TODO: Stronghold security
        // TODO: Fences (ones you can jump over)
        return false;
    }

    public static State handle(RSTile from, RSTile to, WalkCondition walkCondition) {
        Vertical vertical = Vertical.determine(from, to);
        if (vertical != null) return handle(vertical, from, to, walkCondition);
        if (canHandleDoor(from, to)) return handleDoor(from, to, walkCondition);

        // TODO: Stronghold security
        // TODO: Fences (ones you can jump over)
        return FAILED;
    }

    private static State handle(Vertical vertical, RSTile from, RSTile to, WalkCondition walkCondition) {
        RSObject verticalObject = Closest.withAction(from, vertical.pattern);
        if (verticalObject == null) {
            warn("Vertical object is null");
            return FAILED;
        }

        BooleanSupplier sameFloor = () -> {
            RSTile pos = Player.getPosition();
            if (pos == null) return false;
            return pos.getPlane() == to.getPlane();
        };

        return Interact.with(verticalObject, vertical.pattern, sameFloor, walkCondition);
    }

    private static State handleDoor(RSTile from, RSTile to, WalkCondition walkCondition) {
        List<RSObject> doors = stream(Objects.getAll(15))
                .filter(doorFilter(from, to))
                .sorted(comparingDouble(Distance::from))
                .collect(Collectors.toList());

        BooleanSupplier finishCondition = () -> canWalkTo(parentMap(), to);
        if (doors.size() == 0) {
            warn("Did not find any doors in area for handling generic case");
            return FAILED;
        }

        if (doors.size() == 1 || doors.get(0).getPosition().equals(doors.get(1).getPosition()))
            return Interact.with(doors.get(0), Pattern.compile("(?i)open"), finishCondition, walkCondition);

        // Use model to determine which object is most likely to be the door we need to open
        debug("Using model to determine best door to use since there are multiple");

        Point fromPoint = tileToScreen(from, 0);
        Point toPoint = tileToScreen(to, 0);

        if (fromPoint == null || fromPoint.getX() == -1 || fromPoint.getY() == -1) {
            warn("Failed to grab from[%s] point: %s", from, fromPoint);
            return FAILED;
        }

        if (toPoint == null || toPoint.getX() == -1 || toPoint.getY() == -1) {
            warn("Failed to grab from[%s] point: %s", to, toPoint);
            return FAILED;
        }

        // Midpoint of tile A and tile B, likely to be door position.
        Point midPoint = new Point((toPoint.x + fromPoint.x) / 2, (toPoint.y + fromPoint.y) / 2);

        Comparator<RSObject> closestModelComparator = (o1, o2) -> {
            RSModel model = o1.getModel();
            RSModel model2 = o2.getModel();
            Point p = model != null ? model.getCentrePoint() : new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            Point p2 = model2 != null ? model2.getCentrePoint() : new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            return Double.compare(p.distance(midPoint), p2.distance(midPoint));
        };

        List<RSObject> closestDoorsByModel = stream(Objects.getAll(15))
                .filter(doorFilter(from, to))
                .sorted(closestModelComparator)
                .collect(Collectors.toList());
        if (closestDoorsByModel.size() == 0) {
            warn("No doors found using closest model from %s to %s", from, to);
            return FAILED;
        }

        return Interact.with(closestDoorsByModel.get(0), Pattern.compile("(?i)open"), finishCondition, walkCondition);
    }

    private static boolean canHandleDoor(RSTile from, RSTile to) {
        List<RSObject> doors = stream(Objects.getAll(15))
                .filter(doorFilter(from, to))
                .sorted(comparingDouble(Distance::from))
                .collect(Collectors.toList());
        return doors.size() > 0;
    }

    /**
     * Generally used for stairs/ladders, Vertical as in moving across the Z plane
     */
    @AllArgsConstructor
    private enum Vertical {
        UP(Pattern.compile("(?i)(climb).(up)")),
        DOWN(Pattern.compile("(?i)(climb).(down)"));

        final Pattern pattern;

        public static Vertical determine(RSTile from, RSTile to) {
            if (Distance.between(from, to) > 10) return null;
            if (from.getPlane() + 1 == to.getPlane()) return UP;
            if (from.getPlane() - 1 == to.getPlane()) return DOWN;
            return null;
        }
    }

    private static Predicate<RSObject> doorFilter(RSTile from, RSTile to) {
        return object -> {
            RSObjectDefinition definition = object.getDefinition();
            if (definition == null) return false;
            if (stream(definition.getActions()).noneMatch(s -> s.matches("(?i)open"))) return false;
            return Distance.between(from, object) <= 3;
        };
    }

}
