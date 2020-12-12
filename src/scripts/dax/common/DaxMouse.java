package scripts.dax.common;

import org.tribot.api.input.Mouse;
import org.tribot.api2007.ChooseOption;
import org.tribot.api2007.types.RSMenuNode;
import org.tribot.api2007.types.RSModel;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSObjectDefinition;

import java.awt.*;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.tribot.api.input.Mouse.*;
import static org.tribot.api2007.Game.getCrosshairState;
import static org.tribot.api2007.Game.getUptext;
import static scripts.dax.common.WaitFor.*;
import static scripts.dax.common.WaitFor.Return.IGNORE;
import static scripts.dax.common.WaitFor.Return.SUCCESS;

// Simplified version of Accurate mouse
public class DaxMouse {

    public static boolean clickPattern(RSObject rsObject, Pattern p) {
        for (int i = 0; i < random(4, 7); i++) {
            if (!attemptAction(rsObject, p)) continue;
            return true;
        }
        return false;
    }

    private static boolean attemptAction(RSObject rsObject, Pattern pattern) {
        RSObjectDefinition definition = rsObject.getDefinition();

        RSModel model = rsObject.getModel();
        if (model == null) {
            DaxLogger.debug("DaxMouse: RSModel of RSObject[%s] is null", definition.getName());
            return false;
        }

        Point hoverPoint = model.getHumanHoverPoint();
        if (hoverPoint == null || hoverPoint.x == -1 || hoverPoint.y == -1) {
            DaxLogger.debug("DaxMouse: Human hover point of RSObject[%s] is null", definition.getName());
            return false;
        }

        if (hoverPoint.distance(getPos()) < getSpeed() / 20D) {
            hop(hoverPoint);
        } else {
            Mouse.move(hoverPoint);
        }

        if (!model.getEnclosedArea().contains(hoverPoint)) {
            DaxLogger.debug("DaxMouse: Target RSObject[%s] moved", definition.getName());
            return false;
        }

        String[] hoverOptions = ChooseOption.getOptions();
        if (hoverOptions == null || hoverOptions.length == 0) {
            DaxLogger.debug("DaxMouse: No initial hover options");
            return false;
        }

        String regex = pattern.pattern() + ".+" + definition.getName();
        Condition waitForAvailableOptions = () -> stream(hoverOptions).anyMatch(s -> s.matches(regex)) ? SUCCESS : IGNORE;
        if (condition(80, waitForAvailableOptions) != SUCCESS) {
            DaxLogger.debug("DaxMouse: No available option visible in choose options");
            return false;
        }

        // re-grab choose options after we waited to make sure it's up-to-date
        String[] latestHoverOptions = ChooseOption.getOptions();
        if (latestHoverOptions == null || latestHoverOptions.length == 0) return false;

        // This is not the first option... need to right click
        if (!ofNullable(getUptext()).orElse("").matches(regex + " .+")) {
            click(3);
            return handleMenuNode(getValidMenuNode(rsObject, pattern));
        }

        click(1);
        return waitResponse() == State.RED;
    }

    private static RSMenuNode getValidMenuNode(RSObject object, Pattern pattern) {
        RSMenuNode[] menuNodes = ChooseOption.getMenuNodes();
        if (menuNodes == null || menuNodes.length == 0) return null;

        return stream(menuNodes).filter(rsMenuNode -> {
            if (!rsMenuNode.correlatesTo(object)) return false;
            return rsMenuNode.getAction().matches(pattern.pattern());
        }).findFirst().orElse(null);
    }

    private static boolean handleMenuNode(RSMenuNode rsMenuNode) {
        if (rsMenuNode == null) return false;

        Rectangle rectangle = rsMenuNode.getArea();
        if (rectangle == null) {
            DaxLogger.debug("DaxMouse: Closing already open menu node");
            ChooseOption.close();
            return false;
        }

        Point currentMousePosition = getPos();
        if (rectangle.contains(currentMousePosition)) {
            click(1);
        } else {
            clickBox(rectangle, 1);
        }

        return true;
    }

    private static State waitResponse() {
        State response = WaitFor.getValue(250, () -> switch (getState()) {
            case YELLOW -> State.YELLOW;
            case RED -> State.RED;
            default -> null;
        });
        return response != null ? response : State.NONE;
    }

    private static State getState() {
        int crosshairState = getCrosshairState();
        for (State state : State.values()) {
            if (state.id != crosshairState) continue;
            return state;
        }
        return State.NONE;
    }

    private enum State {
        NONE(0),
        YELLOW(1),
        RED(2);
        private final int id;

        State(int id) {
            this.id = id;
        }
    }
}
