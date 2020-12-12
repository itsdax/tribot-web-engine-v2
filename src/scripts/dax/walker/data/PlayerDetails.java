package scripts.dax.walker.data;

import com.allatori.annotations.DoNotRename;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSVarBit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DoNotRename
@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerDetails {

    public static PlayerDetails generate() {
        List<IntPair> inventory = Arrays.stream(Inventory.getAll())
                .map(rsItem -> new IntPair(rsItem.getID(), rsItem.getStack())).collect(Collectors.toList());

        List<IntPair> equipment = Arrays.stream(Equipment.getItems())
                .map(rsItem -> new IntPair(rsItem.getID(), rsItem.getStack())).collect(Collectors.toList());

        List<IntPair> settings = Stream.of(10, 11, 17, 32, 63, 68, 71, 101, 111, 116, 131, 144, 145, 150, 165, 176,
                179, 273, 299, 302, 307, 314, 335, 347, 351, 365, 371, 387, 399, 425, 437, 440, 482, 622, 655, 671, 705, 794, 810,
                869, 896, 964, 970, 1630, 1671, 1672)
                .map(value -> new IntPair(value, Game.getSetting(value))).distinct().collect(Collectors.toList());

        List<IntPair> varbit = Arrays.stream(new int[]{
                192,
                199,
                357,
                2310,
                2328,
                3741,
                4538,
                4566,
                4885,
                4895,
                4897,
                5087,
                5088,
                5089,
                5090,
                5810,
                9016
        })
                .mapToObj(value -> new IntPair(value, RSVarBit.get(value).getValue())).distinct().collect(
                        Collectors.toList());

        return new PlayerDetails(
                Skills.getActualLevel(Skills.SKILLS.ATTACK),
                Skills.getActualLevel(Skills.SKILLS.DEFENCE),
                Skills.getActualLevel(Skills.SKILLS.STRENGTH),
                Skills.getActualLevel(Skills.SKILLS.HITPOINTS),
                Skills.getActualLevel(Skills.SKILLS.RANGED),
                Skills.getActualLevel(Skills.SKILLS.PRAYER),
                Skills.getActualLevel(Skills.SKILLS.MAGIC),
                Skills.getActualLevel(Skills.SKILLS.COOKING),
                Skills.getActualLevel(Skills.SKILLS.WOODCUTTING),
                Skills.getActualLevel(Skills.SKILLS.FLETCHING),
                Skills.getActualLevel(Skills.SKILLS.FISHING),
                Skills.getActualLevel(Skills.SKILLS.FIREMAKING),
                Skills.getActualLevel(Skills.SKILLS.CRAFTING),
                Skills.getActualLevel(Skills.SKILLS.SMITHING),
                Skills.getActualLevel(Skills.SKILLS.MINING),
                Skills.getActualLevel(Skills.SKILLS.HERBLORE),
                Skills.getCurrentLevel(Skills.SKILLS.AGILITY),
                Skills.getActualLevel(Skills.SKILLS.THIEVING),
                Skills.getActualLevel(Skills.SKILLS.SLAYER),
                Skills.getActualLevel(Skills.SKILLS.FARMING),
                Skills.getActualLevel(Skills.SKILLS.RUNECRAFTING),
                Skills.getActualLevel(Skills.SKILLS.HUNTER),
                Skills.getActualLevel(Skills.SKILLS.CONSTRUCTION),
                settings,
                varbit,
                WorldHopper.isMembers(WorldHopper.getWorld()),
                equipment,
                inventory
        );
    }


    @DoNotRename
    private int attack;

    @DoNotRename
    private int defence;

    @DoNotRename
    private int strength;

    @DoNotRename
    private int hitpoints;

    @DoNotRename
    private int ranged;

    @DoNotRename
    private int prayer;

    @DoNotRename
    private int magic;

    @DoNotRename
    private int cooking;

    @DoNotRename
    private int woodcutting;

    @DoNotRename
    private int fletching;

    @DoNotRename
    private int fishing;

    @DoNotRename
    private int firemaking;

    @DoNotRename
    private int crafting;

    @DoNotRename
    private int smithing;

    @DoNotRename
    private int mining;

    @DoNotRename
    private int herblore;

    @DoNotRename
    private int agility;

    @DoNotRename
    private int thieving;

    @DoNotRename
    private int slayer;

    @DoNotRename
    private int farming;

    @DoNotRename
    private int runecrafting;

    @DoNotRename
    private int hunter;

    @DoNotRename
    private int construction;

    @DoNotRename
    private List<IntPair> setting;

    @DoNotRename
    private List<IntPair> varbit;

    @DoNotRename
    private boolean member;

    @DoNotRename
    private List<IntPair> equipment;

    @DoNotRename
    private List<IntPair> inventory;

    public JsonElement toJson() {
        return new Gson().toJsonTree(this);
    }

}
