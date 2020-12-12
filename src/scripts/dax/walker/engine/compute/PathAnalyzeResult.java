package scripts.dax.walker.engine.compute;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.tribot.api2007.types.RSTile;

/**
 * This class holds the information needed to perform next movement
 */
@Getter
@AllArgsConstructor
public class PathAnalyzeResult {
    // Tile the player is closest to
    private final RSTile currentTile;

    // Tile the player can walk to without special interaction
    // Generally furthest tile of path visible in minimap or furthest tile until a block (e.g. Door, Gate)
    private final RSTile furthestReachable;

    // Tile after the furthest reachable
    private final RSTile tileAfterFurthestReachable;
}
