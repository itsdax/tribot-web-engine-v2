package scripts.dax.walker.engine;

import lombok.Getter;
import lombok.Setter;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.interfaces.Painting;
import scripts.dax.common.DaxLogger;
import scripts.dax.walker.data.PathResult;
import scripts.dax.walker.data.Point3D;
import scripts.dax.walker.data.WalkCondition;
import scripts.dax.walker.engine.interaction.Teleport;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static scripts.dax.walker.data.PathStatus.SUCCESS;

@Getter
@Setter
public class Navigator implements Painting {

    private WalkCondition walkCondition;
    private PathWalker pathWalker;

    public Navigator() {
        this(() -> false, new PathWalker(7));
    }

    public Navigator(WalkCondition walkCondition, PathWalker pathWalker) {
        this.walkCondition = walkCondition;
        this.pathWalker = pathWalker;
    }

    public boolean walk(List<PathResult> paths, WalkCondition walkCondition) {
        List<PathResult> validPaths = removeInvalidPaths(paths);
        PathResult pathResult = getBestPath(validPaths);
        if (pathResult == null) {
            DaxLogger.warn("No valid path found");
            return false;
        }

        DaxLogger.info("Chose path of cost: %d out of %d options.", pathResult.getCost(), validPaths.size());
        return pathWalker.walk(convert(pathResult.getPath()), this.walkCondition.or(walkCondition));
    }

    private PathResult getBestPath(List<PathResult> list) {
        RSTile position = Player.getPosition();
        if (position == null) {
            DaxLogger.warn("Unable to grab player position for calculating move cost");
            return null;
        }

        return list.stream().min(Comparator.comparingInt(value -> getPathMoveCost(value, position))).orElse(null);
    }

    private int getPathMoveCost(PathResult pathResult, RSTile position) {
        if (position == null) {
            DaxLogger.warn("Unable to grab player position for calculating move cost");
            return -1;
        }

        if (position.equals(toRSTile(pathResult.getPath().get(0)))) return pathResult.getCost();
        Teleport teleport = Teleport.getFor(toRSTile(pathResult.getPath().get(0)));
        if (teleport == null) return pathResult.getCost();
        return teleport.getMoveCost() + pathResult.getCost();
    }

    private static RSTile[] convert(List<Point3D> list) {
        RSTile[] path = new RSTile[list.size()];
        for (int i = 0; i < list.size(); i++) {
            path[i] = toRSTile(list.get(i));
        }
        return path;
    }

    private static List<PathResult> removeInvalidPaths(List<PathResult> list) {
        return list.stream().filter(path -> path.getPathStatus() == SUCCESS).collect(toList());
    }

    private static RSTile toRSTile(Point3D point3D) {
        return new RSTile(point3D.getX(), point3D.getY(), point3D.getZ(), RSTile.TYPES.WORLD);
    }

    @Override
    public void onPaint(Graphics graphics) {
        pathWalker.onPaint(graphics);
    }
}
