package scripts.dax.walker.engine.compute;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;
import scripts.dax.common.Distance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static scripts.dax.common.DaxLogger.warn;
import static scripts.dax.walker.engine.compute.CollisionTile.isWalkable;

public final class Pathfinder {

    private static final int GAME_REGION_SIZE = 104;

    public static double distance(Node[][] parentMap, RSTile tile) {
        if (tile.getType() != RSTile.TYPES.WORLD)
            throw new IllegalStateException("Invalid usage of pathfinding distance check");

        RSTile local = tile.toLocalTile();
        if (!withinBounds(local.getX()) || !withinBounds(local.getY())) return Double.MAX_VALUE;

        Node destination = parentMap[local.getX()][local.getY()];
        return destination != null ? destination.getMoveCost() : Double.MAX_VALUE;
    }

    public static boolean canWalkTo(Node[][] parentMap, RSTile tile) {
        if (tile.getType() != RSTile.TYPES.LOCAL) tile = tile.toLocalTile();
        if (!withinBounds(tile.getX()) || !withinBounds(tile.getY())) return false;
        Node n = parentMap[tile.getX()][tile.getY()];
        return n != null && n.getMoveCost() != Double.MAX_VALUE;
    }

    /**
     * Computes the paths for all immediate walkable tiles in the current region.
     *
     * @return Map containing processed pathfinding information
     */
    public static Node[][] parentMap() {
        int[][] collisionFlags = PathFinding.getCollisionData();

        // Do not use world tiles for grabbing index
        RSTile playerPositionWorldTile = Player.getPosition();
        if (playerPositionWorldTile == null) {
            warn("Unable to get player position to generate parent collision map");
            return null;
        }

        Node start = new Node(playerPositionWorldTile.toLocalTile(), null, 0D);
        if (!withinBounds(start.tile.getX()) || !withinBounds(start.tile.getY())) {
            warn("Unable to generate parentMap from collision from starting tile: %s", playerPositionWorldTile);
            return null;
        }

        Node[][] parents = new Node[GAME_REGION_SIZE][GAME_REGION_SIZE];
        parents[start.tile.getX()][start.tile.getY()] = start;

        Queue<Node> queue = new LinkedList<>();
        Map<String, Double> moveCosts = new HashMap<>();

        // Seed initial value to start search with start tile
        queue.add(start);
        moveCosts.put(start.toString(), 0D);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Direction d : Direction.values()) {
                if (!d.isValidDirection(current.tile.getX(), current.tile.getY(), collisionFlags)) continue;

                RSTile neighbor = d.of(current.tile);
                if (!withinBounds(neighbor.getX()) || !withinBounds(neighbor.getY())) continue;
                if (!isWalkable(collisionFlags[neighbor.getX()][neighbor.getY()])) continue;

                double moveCost = current.getMoveCost() + Distance.between(current.tile, neighbor);

                // If this path is worse, skip it
                if (moveCosts.getOrDefault(neighbor.toString(), Double.MAX_VALUE) <= moveCost) continue;

                Node neighborNode = new Node(neighbor, current.tile, moveCost);
                parents[neighbor.getX()][neighbor.getY()] = neighborNode;
                queue.add(neighborNode);
                moveCosts.put(neighbor.toString(), moveCost);
            }
        }

        return parents;
    }

    @Getter
    @AllArgsConstructor
    public static final class Node {
        // Tile that this node represents
        private final RSTile tile;

        // The tile before this Node/Tile (Predecessor)
        private final RSTile parent;

        // Cost to move to this node
        private final double moveCost;

        @Override
        public String toString() {
            return tile.toString();
        }
    }

    // Returns true if value is within local game region [0, GAME_REGION_SIZE - 1)
    public static boolean withinBounds(int value) {
        return value >= 0 && value < GAME_REGION_SIZE;
    }

}
