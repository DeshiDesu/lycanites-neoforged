package com.lycanitesmobs.core.entity.goals;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.util.GoalConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class BaseGoal extends Goal {
	// Targets:
    protected BaseCreatureEntity host;

    // Properties:
    private GoalConditions goalConditions;
    private BaseCreatureEntity.TARGET_BITS targetBit = BaseCreatureEntity.TARGET_BITS.ATTACK;

	public BaseGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
    }

    /**
     * Sets the Goal Conditions to use.
     * @param goalConditions The Goal Conditions to use.
     * @return Base goal for chaining.
     */
    public BaseGoal setConditions(GoalConditions goalConditions) {
        this.goalConditions = goalConditions;
        return this;
    }

    /**
     * Sets the target bit to determine which target should be used.
     * @param targetBit The target to use's bit (defaults to ATTACK).
     * @return Base goal for chaining.
     */
    public BaseGoal setTargetBit(BaseCreatureEntity.TARGET_BITS targetBit) {
        this.targetBit = targetBit;
        return this;
    }

    @Override
    public boolean canUse() {
        return this.host.isAlive() && (this.goalConditions == null || this.goalConditions.isMet(this.host));
    }

    public LivingEntity getTarget() {
        if(this.targetBit == BaseCreatureEntity.TARGET_BITS.ATTACK) {
            return this.host.getTarget();
        }
        if(this.targetBit == BaseCreatureEntity.TARGET_BITS.AVOID) {
            return this.host.getAvoidTarget();
        }
        if(this.targetBit == BaseCreatureEntity.TARGET_BITS.MASTER) {
            return this.host.getMasterTarget();
        }
        if(this.targetBit == BaseCreatureEntity.TARGET_BITS.PARENT) {
            return this.host.getParentTarget();
        }
        if(this.targetBit == BaseCreatureEntity.TARGET_BITS.RIDER) {
            return this.host.getRider();
        }
        if(this.targetBit == BaseCreatureEntity.TARGET_BITS.PERCH) {
            return this.host.getPerchTarget();
        }
        return null;
    }
}
