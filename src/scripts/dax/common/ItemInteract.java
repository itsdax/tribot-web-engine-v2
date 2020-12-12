package scripts.dax.common;

import org.tribot.api2007.Equipment;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;

import java.util.function.Predicate;

public class ItemInteract {

    public static boolean firstEquipment(Predicate<RSItem> predicate, Action<RSItem> itemAction) {
        RSItem[] items = Equipment.find(predicate);
        if (items.length == 0) return false;
        return itemAction.perform(items[0]);
    }

    public static boolean firstInventory(Predicate<RSItem> predicate, Action<RSItem> itemAction) {
        RSItem[] items = Inventory.find(predicate);
        if (items.length == 0) return false;
        return itemAction.perform(items[0]);
    }

}
