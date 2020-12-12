package scripts.dax.walker.engine;

import org.tribot.api.Timing;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSObjectDefinition;
import scripts.dax.common.DaxMouse;
import scripts.dax.walker.data.WalkCondition;
import scripts.dax.walker.engine.PathWalker.State;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import static org.tribot.api.General.random;
import static org.tribot.api.General.sleep;
import static scripts.dax.common.DaxLogger.warn;
import static scripts.dax.walker.engine.PathWalker.State.*;

public class Interact {

    /**
     * @param object
     * @param regexAction
     * @param walkCondition
     * @param finish        true if we should exit out and succeed
     * @return
     */
    public static State with(RSObject object, Pattern regexAction, BooleanSupplier finish, WalkCondition walkCondition) {
        RSObjectDefinition definition = object.getDefinition();
        if (definition == null) {
            warn("Interact: Object has a null definition");
            return FAILED;
        }
        if (!object.isOnScreen() || !object.isClickable()) {
            warn("Interact: Object[%s] is too far away. Not onscreen or clickable", definition.getName());
            return FAILED;
        }

        if (!DaxMouse.clickPattern(object, regexAction)) {
            warn("Interact: Failed to click Object[%s]", definition.getName());
            return FAILED;
        }

        AtomicBoolean exitCondition = new AtomicBoolean();
        AtomicBoolean success = new AtomicBoolean();
        Timing.waitCondition(() -> {
            if (walkCondition.getAsBoolean()) {
                exitCondition.set(true);
                return true;
            }
            if (finish.getAsBoolean()) {
                success.set(true);
                return true;
            }
            sleep(100, 200);
            return false;
        }, random(7000, 15000));

        if (exitCondition.get()) {
            warn("Interact: Walker condition triggered while interacting with Object[%s]", definition.getName());
            return EXIT_OUT_WALKER;
        }

        if (!success.get()) {
            warn("Interact: Failed to satisfy condition after interacting with object[%s]", definition.getName());
            return FAILED;
        }

        return HANDLED_SCENARIO_SUCCESSFULLY;
    }

}
