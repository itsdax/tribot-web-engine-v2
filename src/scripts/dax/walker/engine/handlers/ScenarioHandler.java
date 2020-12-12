package scripts.dax.walker.engine.handlers;

import org.tribot.api2007.types.RSTile;
import scripts.dax.walker.data.WalkCondition;
import scripts.dax.walker.engine.PathWalker;

public interface ScenarioHandler {
    /**
     * Do NOT assume you can grab objects/npcs near {@param to}
     * Your character is at {@param from} and {@param to} may be 1000+ tiles away.
     *
     * @param from Current location
     * @param to Destination
     * @return
     */
    boolean canHandle(RSTile from, RSTile to);
    PathWalker.State handle(RSTile from, RSTile to, WalkCondition walkCondition);
}
