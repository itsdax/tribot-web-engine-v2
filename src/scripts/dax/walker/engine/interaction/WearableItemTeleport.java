package scripts.dax.walker.engine.interaction;

import org.tribot.api.General;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Inventory;
import scripts.dax.common.ItemInteract;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import static org.tribot.api.Timing.waitCondition;
import static org.tribot.api2007.NPCChat.getOptions;
import static org.tribot.api2007.NPCChat.selectOption;
import static scripts.dax.common.DaxLogger.*;
import static scripts.dax.common.Matchers.nonNotedItem;

public class WearableItemTeleport {

    public static boolean has(Pattern itemMatcher) {
        return Inventory.find(rsItem -> nonNotedItem(rsItem, itemMatcher)).length > 0
                || Equipment.find(rsItem -> nonNotedItem(rsItem, itemMatcher)).length > 0;
    }

    public static boolean teleport(Pattern itemMatcher, Pattern option) {
        if (teleportEquipment(itemMatcher, option)) return true;

        return ItemInteract.firstInventory(rsItem -> nonNotedItem(rsItem, itemMatcher), rsItem -> {
            if (!rsItem.click("Rub")) {
                warn("Failed to click rub on inventory teleport item [%s]", rsItem.getID());
                return false;
            }

            if (waitCondition(waitForNpcChatOption(option), General.random(3000, 4600))) {
                warn("No available chat option [%s] after clicking teleport item [%s]", option, rsItem.getID());
                return false;
            }

            if (selectOption(Arrays.stream(getOptions())
                            .filter(s -> s.matches(option.pattern()))
                            .findFirst()
                            .orElse(null),
                    false)) {
                warn("Failed to select chat option [%s] after clicking teleport item [%s]", option, rsItem.getID());
                return false;
            }

            return true;
        });
    }

    private static boolean teleportEquipment(Pattern itemMatcher, Pattern option) {
        return ItemInteract.firstEquipment(rsItem -> nonNotedItem(rsItem, itemMatcher),
                rsItem -> {
                    if (!rsItem.click(rsMenuNode -> rsMenuNode.getAction().matches(option.pattern()))) {
                        warn("Failed to click [%s] on equipment teleport item [%s]", option.pattern(), rsItem.getID());
                        return false;
                    }

                    return true;
                });
    }

    private static BooleanSupplier waitForNpcChatOption(Pattern option) {
        return () -> Arrays.stream(getOptions()).anyMatch(s -> s.matches(option.pattern()));
    }

}
