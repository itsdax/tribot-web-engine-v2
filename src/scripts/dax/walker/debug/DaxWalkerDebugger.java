package scripts.dax.walker.debug;

import lombok.AllArgsConstructor;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;
import scripts.dax.walker.engine.compute.CollisionTile;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.awt.RenderingHints.*;
import static java.awt.image.BufferedImage.*;
import static java.lang.Math.toRadians;
import static java.lang.System.currentTimeMillis;
import static org.tribot.api.Timing.timeFromMark;
import static org.tribot.api2007.PathFinding.getCollisionData;

public class DaxWalkerDebugger {

    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 10);
    private static final Color DEFAULT_BLOCKED_COLOR = new Color(255, 188, 19, 90);
    private static final Color WALL_COLOR = new Color(255, 254, 253, 200);
    private static final Color UNLOADED_COLOR = new Color(255, 3, 3, 50);

    private static final Point MAP_CENTER = new Point(641, 83);
    private static final int REGION_SIZE = 104, TILE_WIDTH = 4, TILE_HEIGHT = 4, WALL_SIZE = TILE_WIDTH / 4;

    private final BufferedImage computeLayerImage, displayLayerImage;
    private final Graphics2D computeLayer, displayLayer;
    private final ExecutorService service;
    private final Lock displayImageLock;

    private int[][] collisionMap;
    private long lastUpdatedCollision;

    public DaxWalkerDebugger() {
        computeLayerImage = new BufferedImage(REGION_SIZE * TILE_WIDTH, REGION_SIZE * TILE_WIDTH, TYPE_INT_ARGB);
        computeLayer = computeLayerImage.createGraphics();
        computeLayer.setRenderingHints(new RenderingHints(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON));
        displayLayerImage = new BufferedImage(REGION_SIZE * TILE_WIDTH, REGION_SIZE * TILE_WIDTH, TYPE_INT_ARGB);
        displayLayer = displayLayerImage.createGraphics();
        service = Executors.newSingleThreadExecutor();
        displayImageLock = new ReentrantLock();
    }

    @AllArgsConstructor
    public static class Draw {
        private RSTile tile;
        private Color color;
    }

    public void drawDebug(Graphics2D g, RSTile[] path, Draw... draws) {
        if (timeFromMark(lastUpdatedCollision) > 600) {
            collisionMap = getCollisionData();
            lastUpdatedCollision = currentTimeMillis();
        }

        service.submit(() -> {
            RSTile playerPosition = Player.getPosition().toLocalTile();
            final int playerX = playerPosition.getX(), playerY = playerPosition.getY();

            computeLayer.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            computeLayer.fillRect(0, 0, REGION_SIZE * TILE_WIDTH, REGION_SIZE * TILE_WIDTH);
            computeLayer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

            int[][] map = collisionMap;
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    int relativeX = x - playerX;
                    int relativeY = playerY - y;

                    // Draw coordinates are relative to player position, since we rotate via player position on minimap
                    int drawX = (relativeX + REGION_SIZE / 2) * TILE_WIDTH;
                    int drawY = (relativeY + REGION_SIZE / 2) * TILE_HEIGHT;

                    // set base color (background light hue)
                    computeLayer.setColor(BACKGROUND_COLOR);
                    computeLayer.fillRect(drawX, drawY, TILE_WIDTH, TILE_HEIGHT);

                    CollisionTile tile = new CollisionTile(map[x][y]);
                    drawTile(computeLayer, drawX, drawY, tile);
                }
            }

            if (path != null) {
                computeLayer.setColor(new Color(13, 255, 0, 125));
                for (RSTile tile : path) {
                    if (!tile.isTileLoaded()) continue;
                    RSTile local = tile.toLocalTile();
                    int relativeX = local.getX() - playerX;
                    int relativeY = playerY - local.getY();
                    int drawX = (relativeX + REGION_SIZE / 2) * TILE_WIDTH;
                    int drawY = (relativeY + REGION_SIZE / 2) * TILE_HEIGHT;
                    computeLayer.fillRect(drawX, drawY, TILE_WIDTH, TILE_HEIGHT);
                }
            }

            for (Draw draw : draws) {
                if (!draw.tile.isTileLoaded()) continue;
                RSTile local = draw.tile.toLocalTile();
                int relativeX = local.getX() - playerX;
                int relativeY = playerY - local.getY();
                int drawX = (relativeX + REGION_SIZE / 2) * TILE_WIDTH;
                int drawY = (relativeY + REGION_SIZE / 2) * TILE_HEIGHT;
                computeLayer.setColor(draw.color);
                computeLayer.fillRect(drawX, drawY, TILE_WIDTH, TILE_HEIGHT);
            }

            displayLayer.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            displayLayer.fillRect(0, 0, REGION_SIZE * TILE_WIDTH, REGION_SIZE * TILE_WIDTH);
            displayLayer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            try {
                displayImageLock.lock();
                displayLayer.drawImage(computeLayerImage, 0, 0, null);
            } finally {
                displayImageLock.unlock();
            }
        });

        AffineTransform af = new AffineTransform();
        af.rotate(toRadians(Camera.getCameraRotation()), MAP_CENTER.x + TILE_WIDTH / 2f, MAP_CENTER.y + TILE_WIDTH / 2f);
        float halfRegion = REGION_SIZE / 2f * TILE_WIDTH;
        af.translate(MAP_CENTER.x - halfRegion, MAP_CENTER.y - halfRegion);
        displayImageLock.lock();
        g.drawImage(displayLayerImage, af, null);
        displayImageLock.unlock();
    }

    private static void drawTile(Graphics2D computeLayer, int x, int y, CollisionTile tile) {
        computeLayer.setColor(BACKGROUND_COLOR);
        computeLayer.fillRect(x, y, TILE_WIDTH, TILE_HEIGHT);

        if (tile.isNotLoaded()) {
            computeLayer.setColor(UNLOADED_COLOR);
            computeLayer.fillRect(x, y, TILE_WIDTH, TILE_HEIGHT);
            return;
        }

        // draw yellow for un-walkable
        if (!tile.isWalkable()) {
            computeLayer.setColor(DEFAULT_BLOCKED_COLOR);
            computeLayer.fillRect(x, y, TILE_WIDTH, TILE_HEIGHT);
        }

        // draw edges/walls/fences/etc
        computeLayer.setColor(WALL_COLOR);
        if (tile.blockedNorth()) computeLayer.fillRect(x, y, TILE_WIDTH, WALL_SIZE);
        if (tile.blockedEast()) computeLayer.fillRect(x + TILE_WIDTH - TILE_WIDTH / 4, y, WALL_SIZE, TILE_HEIGHT);
        if (tile.blockedSouth()) computeLayer.fillRect(x, y + TILE_WIDTH - WALL_SIZE, TILE_WIDTH, WALL_SIZE);
        if (tile.blockedWest()) computeLayer.fillRect(x, y, WALL_SIZE, TILE_HEIGHT);
    }


}

