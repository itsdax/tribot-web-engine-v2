package scripts.dax.walker.data;

import java.util.function.BooleanSupplier;

/**
 * At any time this method returns true, Walker will exit out
 * and return false since it has not successfully traversed
 * the path.
 */
public interface WalkCondition extends BooleanSupplier {
    default WalkCondition and(WalkCondition walkCondition) {
        if (walkCondition == null) return this;
        return () -> WalkCondition.this.getAsBoolean() && walkCondition.getAsBoolean();
    }

    default WalkCondition or(WalkCondition walkCondition) {
        if (walkCondition == null) return this;
        return () -> WalkCondition.this.getAsBoolean() || walkCondition.getAsBoolean();
    }
}
