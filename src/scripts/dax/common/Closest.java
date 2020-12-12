package scripts.dax.common;

import org.tribot.api2007.Objects;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSObjectDefinition;
import org.tribot.api2007.types.RSTile;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Comparator.comparingDouble;

public class Closest {

    public static RSObject withAction(RSTile from, Pattern regex) {
        Predicate<RSObject> filterWithRegex = rsObject -> {
            RSObjectDefinition definition = rsObject.getDefinition();
            if (definition == null) return false;
            String[] actions = definition.getActions();
            if (actions == null) return false;
            return Arrays.stream(actions).anyMatch(s -> s.matches(regex.pattern()));
        };

        RSObject[] objects = Objects.find(25, filterWithRegex);
        return Arrays.stream(objects).min(comparingDouble(o -> Distance.between(o, from))).orElse(null);
    }

}
