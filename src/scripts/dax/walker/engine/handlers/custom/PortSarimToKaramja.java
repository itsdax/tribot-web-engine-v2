package scripts.dax.walker.engine.handlers.custom;

import org.tribot.api2007.types.RSTile;
import scripts.dax.walker.data.WalkCondition;
import scripts.dax.walker.engine.PathWalker;
import scripts.dax.walker.engine.handlers.ScenarioHandler;

public class PortSarimToKaramja implements ScenarioHandler {
    @Override
    public boolean canHandle(RSTile from, RSTile to) {
        return from.equals(new RSTile(2953, 3146, 0)) && to.equals(new RSTile(3029, 3217, 0));
    }

    @Override
    public PathWalker.State handle(RSTile from, RSTile to, WalkCondition walkCondition) {
        // TODO: Pay-fare logic with NPC
        return null;
    }
}
