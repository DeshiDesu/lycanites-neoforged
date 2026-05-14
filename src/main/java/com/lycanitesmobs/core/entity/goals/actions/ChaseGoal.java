package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;


public class ChaseGoal extends Goal {
	// Targets:
    protected BaseCreatureEntity host;
	protected LivingEntity target;
    
    // Properties:
	protected double speed = 1.0D;
	protected float maxTargetDistance = 8.0F;
	protected float minTargetDistance = 0F;

	protected BlockPos movePos;

    public ChaseGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public ChaseGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
	public ChaseGoal setMaxDistance(float setDist) {
		this.maxTargetDistance = setDist;
		return this;
	}
	public ChaseGoal setMinDistance(float setDist) {
		this.minTargetDistance = setDist;
		return this;
	}

	@Override
    public boolean canUse() {
		this.target = this.host.getTarget();
		if(this.target == null || !this.target.isAlive()) {
			return false;
		}
		float distance = this.host.distanceTo(this.target);
		if(distance > this.maxTargetDistance) {
			return false;
		}
		if(distance < this.minTargetDistance) {
			return false;
		}

		this.movePos = this.target.blockPosition();

        return true;
    }

	@Override
    public boolean canContinueToUse() {
		if (!this.canUse()) {
			return false;
		}
		if (!this.host.useDirectNavigator() && this.host.getNavigation().isDone()) {
			return false;
		}
    	return true;
    }

	@Override
	public void start() {
		if(!this.host.useDirectNavigator())
			this.host.getNavigation().moveTo(this.movePos.getX(), this.movePos.getY(), this.movePos.getZ(), this.speed);
		else
			this.host.directNavigator.setTargetPosition(this.movePos, this.speed);
	}

	@Override
	public void stop() {
		this.target = null;
		if(!this.host.useDirectNavigator())
			this.host.getNavigation().stop();
		else
			this.host.directNavigator.clearTargetPosition(this.speed);
	}
}
