package scripts.dax.common;

import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;

import java.util.regex.Pattern;

public class Matchers {
    public static boolean nonNotedItem(RSItem item, int id) {
        RSItemDefinition definition = item.getDefinition();
        if (definition == null) return false;
        if (definition.isNoted()) return false;
        return item.getID() == id;
    }

    public static boolean nonNotedItem(RSItem item, Pattern pattern) {
        RSItemDefinition definition = item.getDefinition();
        if (definition == null) return false;
        if (definition.isNoted()) return false;
        return definition.getName().matches(pattern.pattern());
    }



}

