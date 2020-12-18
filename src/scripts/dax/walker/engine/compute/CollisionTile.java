package scripts.dax.walker.engine.compute;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static scripts.dax.walker.engine.compute.CollisionFlags.*;

@AllArgsConstructor
public class CollisionTile {

    @Getter
    private final int collisionData;

    public boolean blockedNorth() {
        return blockedNorth(this.collisionData);
    }

    public boolean blockedEast() {
        return blockedEast(this.collisionData);
    }

    public boolean blockedSouth() {
        return blockedSouth(this.collisionData);
    }

    public boolean blockedWest() {
        return blockedWest(this.collisionData);
    }

    public boolean isWalkable() {
        return isWalkable(this.collisionData);
    }

    public boolean isNotLoaded() {
        return blockedNorth() && blockedEast() && blockedSouth() && blockedWest() && !isWalkable();
    }

    public static boolean blockedNorth(int collisionData) {
        return checkFlag(collisionData, NORTH)
                || checkFlag(collisionData, BLOCKED_NORTH_WALL);
    }

    public static boolean blockedEast(int collisionData) {
        return checkFlag(collisionData, EAST)
                || checkFlag(collisionData, BLOCKED_EAST_WALL);
    }

    public static boolean blockedSouth(int collisionData) {
        return checkFlag(collisionData, SOUTH)
                || checkFlag(collisionData, BLOCKED_SOUTH_WALL);
    }

    public static boolean blockedWest(int collisionData) {
        return checkFlag(collisionData, WEST)
                || checkFlag(collisionData, BLOCKED_WEST_WALL);
    }

    public static boolean isWalkable(int collisionData) {
        return !(checkFlag(collisionData, OCCUPIED)
                || checkFlag(collisionData, SOLID)
                || checkFlag(collisionData, BLOCKED)
                || checkFlag(collisionData, CLOSED));
    }

    public static boolean isNotLoaded(int collisionData) {
        return checkFlag(collisionData, INITIALIZED);
    }

}