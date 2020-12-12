package scripts.dax.walker.debug;

import org.tribot.api2007.Projection;
import org.tribot.api2007.util.ProjectionUtility;

import java.awt.*;

import static scripts.dax.walker.engine.compute.Pathfinder.Node;

public class PathfindingDebugger {

    public static void drawPaths(Graphics2D graphics2D, Node[][] parentMap) {
        if (parentMap == null) return;

        ProjectionUtility projectionUtility = new ProjectionUtility();
        for (Node[] nodes : parentMap) {
            graphics2D.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Node node : nodes) {
                if (node == null) continue;
                Point point = projectionUtility.tileToScreen(node.getTile(), 0);
                if (point == null || point.x == -1 || point.y == -1) continue;
                if (!Projection.isInViewport(point)) continue;
                graphics2D.setFont(graphics2D.getFont().deriveFont(9f));
                graphics2D.setColor(new Color(255, 213, 0));
                graphics2D.drawString(String.format("%.1f", node.getMoveCost()), point.x, point.y);

                Point parent = projectionUtility.tileToScreen(node.getParent(), 0);
                if (parent == null || parent.x == -1 || parent.y == -1) continue;
                graphics2D.setColor(new Color(63, 170, 0,80));
                graphics2D.drawLine(parent.x, parent.y, point.x, point.y);
            }
        }
    }

    public static void debugAnalyzer() {
//        PathAnalyzer.
    }

}
