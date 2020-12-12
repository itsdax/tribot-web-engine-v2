package scripts.dax.walker.engine.compute;

import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;
import scripts.dax.common.Distance;
import scripts.dax.walker.engine.compute.Pathfinder.Node;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.tribot.api2007.Projection.isInMinimap;
import static org.tribot.api2007.Projection.tileToMinimap;
import static scripts.dax.common.DaxLogger.warn;
import static scripts.dax.walker.engine.compute.Pathfinder.canWalkTo;
import static scripts.dax.walker.engine.compute.Pathfinder.distance;

public final class PathAnalyzer {

    public static PathAnalyzeResult compute(RSTile[] path, Node[][] parentMap) {
        RSTile current = getCurrentTile(path, parentMap);
        RSTile furthest = getFurthestReachableTile(current, path, parentMap);
        return new PathAnalyzeResult(current,
                furthest,
                getTileAfter(path, furthest));
    }

    private static RSTile getCurrentTile(RSTile[] path, Node[][] parentMap) {
        RSTile player = Player.getPosition();
        if (player == null) {
            warn("Could not analyze path: Could not grab player position");
            return null;
        }

        double shortestDistance = Double.MAX_VALUE;
        Node best = null;
        for (RSTile tile : path) {
            RSTile localTile = tile.toLocalTile();

            if (player.getPlane() != tile.getPlane()) continue;

            double distance = distance(parentMap, tile);
            if (distance >= shortestDistance) continue;

            shortestDistance = distance;
            best = parentMap[localTile.getX()][localTile.getY()];
        }

        return best == null ? null : best.getTile().toWorldTile();
    }

    /*
      Context on how collision from server side works:

      Sometimes, server doesn't know the collision perfectly.
      Object sometimes dont load, and collision isn't captured when collecting data for the server.
      So sometimes, you'll have paths that basically path right through an object since the server thinks there is
      an empty tile there.

      To account for this, we cannot do a basic furthest reachable check.
      After the furthest reachable, we check for further walkable as well.
     */
    private static RSTile getFurthestReachableTile(RSTile current, RSTile[] path, Node[][] parentMap) {
        RSTile player = Player.getPosition();
        if (player == null) {
            warn("Could not analyze path: Could not grab player position");
            return null;
        }

        int currentPos = -1;
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals(current)) continue;
            if (player.getPlane() != path[i].getPlane()) continue;
            currentPos = i;
            break;
        }

        if (currentPos == -1) throw new IllegalStateException("Could not use current tile to find furthest");

        int furthestReachableFromStart = -1;
        for (int i = currentPos; i < path.length; i++) {
            if (i == path.length - 1) {
                furthestReachableFromStart = i;
                break;
            }
            RSTile tile = path[i];
            boolean reachable = player.getPlane() == path[i].getPlane()
                    && Distance.from(tile) < 25
                    && isInMinimap(tileToMinimap(tile))
                    && canWalkTo(parentMap, tile);
            if (reachable) continue;
            furthestReachableFromStart = max(0, i - 1);
            break;
        }

        int furthestReachableFromEnd = -1;
        for (int i = path.length - 1; i >= currentPos; i--) {
            RSTile tile = path[i];
            boolean reachable = player.getPlane() == path[i].getPlane()
                    && Distance.from(tile) < 25
                    && isInMinimap(tileToMinimap(tile))
                    && canWalkTo(parentMap, tile);
            if (!reachable) continue;
            furthestReachableFromEnd = min(path.length - 1, i);
            break;
        }


        if (furthestReachableFromEnd != -1) return path[furthestReachableFromEnd];
        if (furthestReachableFromStart != -1) return path[furthestReachableFromStart];
        throw new IllegalStateException("No tile found for furthest reachable");
    }

    private static RSTile getTileAfter(RSTile[] path, RSTile a) {
        for (int i = 0; i < path.length; i++) {
            RSTile tile = path[i];
            if (!tile.equals(a)) continue;
            return path[min(path.length - 1, i + 1)];
        }
        return null;
    }

}
