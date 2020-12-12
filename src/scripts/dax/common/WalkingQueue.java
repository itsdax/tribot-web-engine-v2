package scripts.dax.common;

import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSCharacter;
import org.tribot.api2007.types.RSTile;

import java.util.ArrayList;

public class WalkingQueue {

    /**
     * Method to check if your character is walking to a destination.
     *
     * @param tile
     * @return true if your character is walking or will walk to that tile in the next game tick.
     */
    public static boolean isWalkingTowards(RSTile tile) {
        RSTile tile1 = getNextWalkingTile();
        return tile1 != null && tile1.equals(tile);
    }

    /**
     * Next tile that your character is moving to in the current/next game tick.
     *
     * @return The next tile that your character is walking to
     */
    public static RSTile getNextWalkingTile() {
        ArrayList<RSTile> tiles = getWalkingHistory();
        return tiles.size() > 0 && !tiles.get(0).equals(Player.getPosition()) ? tiles.get(0) : null;
    }

    private static ArrayList<RSTile> getWalkingHistory() {
        return getWalkingHistory(Player.getRSPlayer());
    }

    private static ArrayList<RSTile> getWalkingHistory(RSCharacter rsCharacter) {
        ArrayList<RSTile> walkingQueue = new ArrayList<>();
        if (rsCharacter == null) {
            return walkingQueue;
        }
        int plane = rsCharacter.getPosition().getPlane();
        int[] xIndex = rsCharacter.getWalkingQueueX(), yIndex = rsCharacter.getWalkingQueueY();
        for (int i = 0; i < xIndex.length && i < yIndex.length; i++) {
            walkingQueue.add(new RSTile(xIndex[i], yIndex[i], plane, RSTile.TYPES.LOCAL).toWorldTile());
        }
        return walkingQueue;
    }

}
