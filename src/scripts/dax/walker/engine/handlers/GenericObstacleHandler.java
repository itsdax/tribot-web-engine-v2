package scripts.dax.walker.engine.handlers;

import lombok.AllArgsConstructor;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.dax.common.Closest;
import scripts.dax.common.DaxLogger;
import scripts.dax.common.Distance;
import scripts.dax.walker.data.WalkCondition;
import scripts.dax.walker.engine.Interact;
import scripts.dax.walker.engine.PathWalker.State;

import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

public class GenericObstacleHandler {

    public static boolean canHandle(RSTile from, RSTile to) {
        Vertical vertical = Vertical.determine(from, to);
        if (vertical != null) return true;

        // TODO: Stronghold security
        // TODO: Fences (ones you can jump over)
        return false;
    }

    public static State handle(RSTile from, RSTile to, WalkCondition walkCondition) {
        Vertical vertical = Vertical.determine(from, to);
        if (vertical != null) return handle(vertical, from, to, walkCondition);

        // TODO: Stronghold security
        // TODO: Fences (ones you can jump over)
        return State.FAILED;
    }

    private static State handle(Vertical vertical, RSTile from, RSTile to, WalkCondition walkCondition) {
        RSObject verticalObject = Closest.withAction(from, vertical.pattern);
        if (verticalObject == null) {
            DaxLogger.warn("Vertical object is null");
            return State.FAILED;
        }

        BooleanSupplier sameFloor = () -> {
            RSTile pos = Player.getPosition();
            if (pos == null) return false;
            return pos.getPlane() == to.getPlane();
        };

        return Interact.with(verticalObject, vertical.pattern, sameFloor, walkCondition);
    }


    @AllArgsConstructor
    private enum Vertical {
        UP(Pattern.compile("(?i)(climb).(up)")),
        DOWN(Pattern.compile("(?i)(climb).(down)"));

        final Pattern pattern;

        public static Vertical determine(RSTile from, RSTile to) {
            if (Distance.between(from, to) > 2) return null;
            if (from.getPlane() + 1 == to.getPlane()) return UP;
            if (from.getPlane() - 1 == to.getPlane()) return DOWN;
            return null;
        }
    }

}
