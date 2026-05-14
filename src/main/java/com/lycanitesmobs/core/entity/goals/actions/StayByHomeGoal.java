package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class StayByHomeGoal extends Goal {
	// Targets:
    private BaseCreatureEntity host;
    
    // Properties:
    private boolean enabled = true;
    private double speed = 1.0D;
    private double farSpeed = 1.5D;

    public StayByHomeGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public StayByHomeGoal setEnabled(boolean flag) {
        this.enabled = flag;
        return this;
    }
    
    public StayByHomeGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    
    public StayByHomeGoal setFarSpeed(double setSpeed) {
    	this.farSpeed = setSpeed;
    	return this;
    }

	@Override
    public boolean canUse() {
    	if(!this.enabled)
    		return false;
    	if(!this.host.hasHome() || this.host.getDistanceFromHome() <= 1.0F)
    		return false;
        if(!this.host.canBreatheUnderwater() && this.host.isInWater())
            return false;
        
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
		BlockPos homePos = this.host.getRestrictCenter();
		double speed = this.speed;
		if(this.host.getDistanceFromHome() > this.host.getHomeDistanceMax())
			speed = this.farSpeed;
		if(!host.useDirectNavigator())
			this.host.getNavigation().moveTo(homePos.getX(), homePos.getY(), homePos.getZ(), this.speed);
		else
			host.directNavigator.setTargetPosition(new BlockPos(homePos.getX(), homePos.getY(), homePos.getZ()), speed);
    }

	@Override
	public void stop() {
		if(!this.host.useDirectNavigator())
			this.host.getNavigation().stop();
		else
			this.host.directNavigator.clearTargetPosition(this.speed);
	}
}
