package scripts.dax.walker.engine.compute;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.tribot.api2007.types.RSTile;

import static scripts.dax.walker.engine.compute.CollisionTile.*;

@AllArgsConstructor
public enum Direction {
    EAST(1, 0),
    NORTH(0, 1),
    WEST(-1, 0),
    SOUTH(0, -1),
    NORTH_EAST(1, 1),
    NORTH_WEST(-1, 1),
    SOUTH_EAST(1, -1),
    SOUTH_WEST(-1, -1),
    ;

    @Getter
    private final int x, y;

    // Direction#of(RSTile) i.e. East.of(RSTile(10,10)) == RSTile(10, 11)
    public RSTile of(RSTile tile) {
        return tile.translate(x, y);
    }

    public boolean isValidDirection(int x, int y, int[][] collisionData) {
        try {
            switch (this) {
                case NORTH:
                    return !blockedNorth(collisionData[x][y]);
                case EAST:
                    return !blockedEast(collisionData[x][y]);
                case SOUTH:
                    return !blockedSouth(collisionData[x][y]);
                case WEST:
                    return !blockedWest(collisionData[x][y]);
                case NORTH_EAST:
                    if (blockedNorth(collisionData[x][y]) || blockedEast(collisionData[x][y])) {
                        return false;
                    }
                    if (!isWalkable(collisionData[x + 1][y])) {
                        return false;
                    }
                    if (!isWalkable(collisionData[x][y + 1])) {
                        return false;
                    }
                    if (blockedNorth(collisionData[x + 1][y])) {
                        return false;
                    }
                    if (blockedEast(collisionData[x][y + 1])) {
                        return false;
                    }
                    return true;
                case NORTH_WEST:
                    if (blockedNorth(collisionData[x][y]) || blockedWest(collisionData[x][y])) {
                        return false;
                    }
                    if (!isWalkable(collisionData[x - 1][y])) {
                        return false;
                    }
                    if (!isWalkable(collisionData[x][y + 1])) {
                        return false;
                    }
                    if (blockedNorth(collisionData[x - 1][y])) {
                        return false;
                    }
                    if (blockedWest(collisionData[x][y + 1])) {
                        return false;
                    }
                    return true;
                case SOUTH_EAST:
                    if (blockedSouth(collisionData[x][y]) || blockedEast(collisionData[x][y])) {
                        return false;
                    }
                    if (!isWalkable(collisionData[x + 1][y])) {
                        return false;
                    }
                    if (!isWalkable(collisionData[x][y - 1])) {
                        return false;
                    }
                    if (blockedSouth(collisionData[x + 1][y])) {
                        return false;
                    }
                    if (blockedEast(collisionData[x][y - 1])) {
                        return false;
                    }
                    return true;
                case SOUTH_WEST:
                    if (blockedSouth(collisionData[x][y]) || blockedWest(collisionData[x][y])) {
                        return false;
                    }
                    if (!isWalkable(collisionData[x - 1][y])) {
                        return false;
                    }
                    if (!isWalkable(collisionData[x][y - 1])) {
                        return false;
                    }
                    if (blockedSouth(collisionData[x - 1][y])) {
                        return false;
                    }
                    if (blockedWest(collisionData[x][y - 1])) {
                        return false;
                    }
                    return true;
                default:
                    return false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Todo(itsdax): Length checks for region boundaries
            return false;
        }
    }
}
