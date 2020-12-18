package scripts.dax.walker.engine.handlers;

import org.tribot.api2007.types.RSTile;
import scripts.dax.walker.data.WalkCondition;
import scripts.dax.walker.engine.PathWalker.State;

public interface ScenarioHandler {
    /**
     * Do NOT assume you can grab objects/npcs near {@param to}
     * Your character is at {@param from} and {@param to} may be 1000+ tiles away.
     *
     * @param from Current location
     * @param to   Destination
     * @return
     */
    boolean canHandle(RSTile from, RSTile to);

    /**
     * @param from          From location
     * @param to            Destination for this handler to try to reach
     * @param walkCondition Condition to be sleeping on whenever there is an idle period or waiting.
     * @return HANDLED_SCENARIO_SUCCESSFULLY - return this whenever success
     *         COMPLETED_PATH - NEVER return this
     *         FAILED - return this whenever handle action fails
     *         EXIT_OUT_WALKER - Only return this if walkCondition returns true.
     *                           You should ALWAYS respect the walkCondition so that daxWalker exits out accordingly.
     *                           Otherwise, user cannot rely on walkConditions for exiting out.
     */
    State handle(RSTile from, RSTile to, WalkCondition walkCondition);
}
