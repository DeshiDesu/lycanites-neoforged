package com.lycanitesmobs.core.entity.goals.util;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;

public class GoalConditions {
    protected boolean rareVariantOnly = false;
    protected int battlePhase = -1;

    /**
     * Sets if this condition show check for Rare Variants.
     * @param rareVariantOnly Set to true for rare variants only, false for rare or standard.
     * @return This Goal Condition for chaining.
     */
    public GoalConditions setRareVariantOnly(boolean rareVariantOnly) {
        this.rareVariantOnly = rareVariantOnly;
        return this;
    }

    /**
     * Sets the battle phase to restrict the goal to.
     * @param battlePhase The Battle Phase to restrict to, set to -1 for all phases (default).
     * @return This Goal Condition for chaining.
     */
    public GoalConditions setBattlePhase(int battlePhase) {
        this.battlePhase = battlePhase;
        return this;
    }

    /**
     * Returns if this Goal Condition is met allowing the AI Goal to execute.
     * @return True if conditions are met.
     */
    public boolean isMet(BaseCreatureEntity creatureEntity) {
        if(this.rareVariantOnly && !creatureEntity.isRareVariant()) {
            return false;
        }

        if(this.battlePhase >= 0 && creatureEntity.getBattlePhase() != this.battlePhase) {
            return false;
        }

        return true;
    }
}
