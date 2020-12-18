package scripts.dax.walker;

import com.google.common.annotations.Beta;
import org.tribot.api2007.types.RSTile;
import scripts.dax.walker.data.RSBank;
import scripts.dax.walker.data.WalkCondition;
import scripts.dax.walker.data.WalkState;

@Beta
public class WebWalker {
    private static DaxWalker daxWalker;

    private WebWalker() {
    }

    /**
     * Sets the daxwalker instance to use with this singleton
     * @param daxWalker walker to be used
     */
    public static void setDaxWalker(DaxWalker daxWalker) {
        WebWalker.daxWalker = daxWalker;
    }

    /**
     *
     * @param walkCondition Condition to check whenever there is a sleep. This is checked roughly every 100ms.
     *                      There are absolutely NO guarantees this will be called every 100ms. Please keep this
     *                      in mind for time-sensitive operations.
     */
    public static void setGlobalCondition(WalkCondition walkCondition) {
        daxWalker.setGlobalCondition(walkCondition);
    }

    /**
     * Enables or disables teleports.
     * Teleports allow for much shorter paths in OSRS, but also comes at a cost.
     * Each teleported computed is an ADDITIONAL path operation.
     *
     * Example: If you have a glory in your equipment with charges, DaxWalker needs to check 4 more paths than just
     * from your current tile to destination tile. This is equivalent to 5 total API calls which aggregates to your
     * API key.
     *
     * @param b True to enable teleports
     */
    public static void setUseTeleports(boolean b) {
        daxWalker.setUseTeleports(b);
    }

    /**
     *
     * @param rsTile Desired destination
     * @return True if successfully walks to destination. False for any other reason
     */
    public static boolean walkTo(RSTile rsTile) {
        return daxWalker.walkTo(rsTile) == WalkState.SUCCESS;
    }

    /**
     *
     * @param rsTile Desired destination
     * @param walkCondition Condition to check whenever there is a sleep. This is checked roughly every 100ms.
     *                      There are absolutely NO guarantees this will be called every 100ms. Please keep this
     *                      in mind for time-sensitive operations.
     *                      This is COMBINED along with the GLOBAL Walk Condition. Both will get triggered at
     *                      the same time.
     * @return True if successfully walks to destination. False for any other reason
     */
    public static boolean walkTo(RSTile rsTile, WalkCondition walkCondition) {
        return daxWalker.walkTo(rsTile, walkCondition) == WalkState.SUCCESS;
    }

    /**
     *
     * @return True if successfully walks to destination. False for any other reason
     */
    public static boolean walkToBank() {
        return daxWalker.walkToBank() == WalkState.SUCCESS;
    }

    /**
     *
     * @param walkCondition Condition to check whenever there is a sleep. This is checked roughly every 100ms.
     *                      There are absolutely NO guarantees this will be called every 100ms. Please keep this
     *                      in mind for time-sensitive operations.
     *                      This is COMBINED along with the GLOBAL Walk Condition. Both will get triggered at
     *                      the same time.
     * @return True if successfully walks to destination. False for any other reason
     */
    public static boolean walkToBank(WalkCondition walkCondition) {
        return daxWalker.walkToBank(walkCondition) == WalkState.SUCCESS;
    }

    /**
     *
     * @param bank Desired bank to walk to
     * @return True if successfully walks to destination. False for any other reason
     */
    public static boolean walkToBank(RSBank bank) {
        return daxWalker.walkToBank(bank) == WalkState.SUCCESS;
    }

    /**
     *
     * @param bank Desired bank to walk to
     * @param walkCondition Condition to check whenever there is a sleep. This is checked roughly every 100ms.
     *                      There are absolutely NO guarantees this will be called every 100ms. Please keep this
     *                      in mind for time-sensitive operations.
     *                      This is COMBINED along with the GLOBAL Walk Condition. Both will get triggered at
     *                      the same time.
     * @return True if successfully walks to destination. False for any other reason
     */
    public static boolean walkToBank(RSBank bank, WalkCondition walkCondition) {
        return daxWalker.walkToBank(bank, walkCondition) == WalkState.SUCCESS;
    }
}
