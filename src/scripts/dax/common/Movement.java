package scripts.dax.common;

import org.tribot.api2007.Player;

public class Movement {
    private final long initial;

    public Movement() {
        initial = System.currentTimeMillis();
    }

    // Intended to be used after a click command is issued
    // Hence the ~2 tick delay to account for delay of initial move
    public boolean isWalking() {
        return System.currentTimeMillis() - initial < 1300 || Player.isMoving();
    }
}
