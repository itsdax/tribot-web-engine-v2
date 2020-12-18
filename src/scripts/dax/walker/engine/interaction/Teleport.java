package scripts.dax.walker.engine.interaction;

import lombok.Getter;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSTile;
import scripts.dax.common.Action;
import scripts.dax.common.DaxLogger;
import scripts.dax.common.Distance;
import scripts.dax.common.ItemInteract;
import scripts.dax.walker.data.Requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static scripts.dax.common.Matchers.nonNotedItem;
import static scripts.dax.walker.engine.interaction.Constants.*;
import static scripts.dax.walker.engine.interaction.WearableItemTeleport.has;
import static scripts.dax.walker.engine.interaction.WearableItemTeleport.teleport;

@Getter
public enum Teleport {
    VARROCK_TELEPORT_TAB(
            35, new RSTile(3212, 3424, 0),
            () -> Inventory.getCount(8007) > 0,
            v -> {
                if (!ItemInteract.firstInventory(rsItem -> nonNotedItem(rsItem, 8007), rsItem -> rsItem.click("Break"))) {
                    DaxLogger.warn("Failed to click Break on Varrock telly tab");
                    return false;
                }

                return true;
            }
    ),

    RING_OF_WEALTH_GRAND_EXCHANGE(
            35, new RSTile(3161, 3478, 0),
            () -> has(RING_OF_WEALTH_MATCHER),
            v -> teleport(RING_OF_WEALTH_MATCHER, Pattern.compile("(?i)Grand Exchange"))
    ),

    RING_OF_WEALTH_FALADOR(
            35, new RSTile(2994, 3377, 0),
            () -> has(RING_OF_WEALTH_MATCHER),
            v -> teleport(RING_OF_WEALTH_MATCHER, Pattern.compile("(?i)falador.*"))
    ),

    RING_OF_DUELING_DUEL_ARENA(
            35, new RSTile(3313, 3233, 0),
            () -> has(RING_OF_DUELING_MATCHER),
            v -> teleport(RING_OF_DUELING_MATCHER, Pattern.compile("(?i).*duel arena.*"))
    ),

    RING_OF_DUELING_CASTLE_WARS(
            35, new RSTile(2440, 3090, 0),
            () -> has(RING_OF_DUELING_MATCHER),
            v -> teleport(RING_OF_DUELING_MATCHER, Pattern.compile("(?i).*Castle Wars.*"))
    ),

    RING_OF_DUELING_CLAN_WARS(
            35, new RSTile(3388, 3161, 0),
            () -> has(RING_OF_DUELING_MATCHER),
            v -> teleport(RING_OF_DUELING_MATCHER, Pattern.compile("(?i).*Clan Wars.*"))
    ),

    NECKLACE_OF_PASSAGE_WIZARD_TOWER(
            35, new RSTile(3113, 3179, 0),
            () -> has(NECKLACE_OF_PASSAGE_MATCHER),
            v -> teleport(NECKLACE_OF_PASSAGE_MATCHER, Pattern.compile("(?i).*wizard.+tower.*"))
    ),

    NECKLACE_OF_PASSAGE_OUTPOST(
            35, new RSTile(2430, 3347, 0),
            () -> has(NECKLACE_OF_PASSAGE_MATCHER),
            v -> teleport(NECKLACE_OF_PASSAGE_MATCHER, Pattern.compile("(?i).*the.+outpost.*"))
    ),

    NECKLACE_OF_PASSAGE_EYRIE(
            35, new RSTile(3406, 3156, 0),
            () -> has(NECKLACE_OF_PASSAGE_MATCHER),
            v -> teleport(NECKLACE_OF_PASSAGE_MATCHER, Pattern.compile("(?i).*eagl.+eyrie.*"))
    ),

    COMBAT_BRACE_WARRIORS_GUILD(
            35, new RSTile(2882, 3550, 0),
            () -> has(COMBAT_BRACE_MATCHER),
            v -> teleport(COMBAT_BRACE_MATCHER, Pattern.compile("(?i).*warrior.+guild.*"))
    ),

    COMBAT_BRACE_CHAMPIONS_GUILD(
            35, new RSTile(3190, 3366, 0),
            () -> has(COMBAT_BRACE_MATCHER),
            v -> teleport(COMBAT_BRACE_MATCHER, Pattern.compile("(?i).*champion.+guild.*"))
    ),

    COMBAT_BRACE_MONASTRY(
            35, new RSTile(3053, 3486, 0),
            () -> has(COMBAT_BRACE_MATCHER),
            v -> teleport(COMBAT_BRACE_MATCHER, Pattern.compile("(?i).*monastery.*"))
    ),

    COMBAT_BRACE_RANGE_GUILD(
            35, new RSTile(2656, 3442, 0),
            () -> has(COMBAT_BRACE_MATCHER),
            v -> teleport(COMBAT_BRACE_MATCHER, Pattern.compile("(?i).*rang.+guild.*"))
    ),

    GAMES_NECK_BURTHORPE(
            35, new RSTile(2897, 3551, 0),
            () -> has(GAMES_NECKLACE_MATCHER),
            v -> teleport(GAMES_NECKLACE_MATCHER, Pattern.compile("(?i).*burthorpe.*"))
    ),

    GAMES_NECK_BARBARIAN_OUTPOST(
            35, new RSTile(2520, 3570, 0),
            () -> has(GAMES_NECKLACE_MATCHER),
            v -> teleport(GAMES_NECKLACE_MATCHER, Pattern.compile("(?i).*barbarian.*"))
    ),

    GAMES_NECK_CORPREAL(
            35, new RSTile(2965, 4832, 2),
            () -> has(GAMES_NECKLACE_MATCHER),
            v -> teleport(GAMES_NECKLACE_MATCHER, Pattern.compile("(?i).*corpreal.*"))
    ),

    GAMES_NECK_WINTER(
            35, new RSTile(1623, 3937, 0),
            () -> has(GAMES_NECKLACE_MATCHER),
            v -> teleport(GAMES_NECKLACE_MATCHER, Pattern.compile("(?i).*wintertodt.*"))
    ),

    GLORY_EDGE(
            35, new RSTile(3087, 3496, 0),
            () -> has(GLORY_MATCHER),
            v -> teleport(GLORY_MATCHER, Pattern.compile("(?i).*edgeville.*"))
    ),

    GLORY_KARAMJA(
            35, new RSTile(2918, 3176, 0),
            () -> has(GLORY_MATCHER),
            v -> teleport(GLORY_MATCHER, Pattern.compile("(?i).*karamja.*"))
    ),

    GLORY_DRAYNOR(
            35, new RSTile(3105, 3251, 0),
            () -> has(GLORY_MATCHER),
            v -> teleport(GLORY_MATCHER, Pattern.compile("(?i).*draynor.*"))
    ),

    GLORY_AL_KHARID(
            35, new RSTile(3293, 3163, 0),
            () -> has(GLORY_MATCHER),
            v -> teleport(GLORY_MATCHER, Pattern.compile("(?i).*al kharid.*"))
    ),

    ;

    // Estimated cost of a move. For example, moving from a tile to the immediate left tile is 1 move cost unit.
    // Taking a boat to karamja costs 30 coins and ~15 seconds, I personally would estimate that into 35 move cost
    // units.
    private final int moveCost;

    // Where this teleport will bring you
    private final RSTile location;

    // Requirement to activate this teleport
    // i.e: If teleport tab, requires teleport tab to be in inventory.
    // i.e: If magic spell, require Magic level and runes to be in inventory as well as correct Spell Book.
    private final Requirement requirement;

    // Action to take to execute the teleport. (Break teleport tab, click spell, etc)
    private final Action<Void> action;

    Teleport(int moveCost, RSTile location, Requirement requirement, Action<Void> action) {
        this.moveCost = moveCost;
        this.location = location;
        this.requirement = requirement;
        this.action = action;
    }

    public boolean trigger() {
        return this.action.perform(null);
    }

    public boolean isAtTeleportSpot() {
        return Distance.from(location) < 15;
    }

    public static List<RSTile> getValidStartingRSTiles() {
        List<RSTile> RSTiles = new ArrayList<>();
        for (Teleport teleport : values()) {
            if (!teleport.requirement.satisfies()) continue;
            RSTiles.add(teleport.location);
        }
        return RSTiles;
    }

    public static Teleport getFor(RSTile rsTile) {
        for (Teleport teleport : values()) {
            if (Distance.between(teleport.location, rsTile) > 1) continue;
            return teleport;
        }
        return null;
    }

}