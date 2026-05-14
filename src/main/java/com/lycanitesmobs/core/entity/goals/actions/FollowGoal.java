package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;


public abstract class FollowGoal extends Goal {
	// Targets:
	BaseCreatureEntity host;
    
    // Properties:
    double speed = 1.0D;
    Class targetClass;
    private int updateRate;
    double strayDistance = 4 * 4;
    double lostDistance = 32 * 32;
    double behindDistance = 0;
    
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public FollowGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
        this.targetClass = this.host.getClass();
		this.setFlags(EnumSet.of(Flag.MOVE));
    }
    
	
	// ==================================================
 	//                      Target
 	// ==================================================
    public abstract Entity getTarget();
	public abstract void setTarget(Entity entity);
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FollowGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    public FollowGoal setTargetClass(Class setTargetClass) {
    	this.targetClass = setTargetClass;
    	return this;
    }
    public FollowGoal setStrayDistance(double setDist) {
    	this.strayDistance = setDist;
    	return this;
    }
    public FollowGoal setLostDistance(double setDist) {
    	this.lostDistance = setDist;
    	return this;
    }
    public FollowGoal setFollowBehind(double setDist) {
    	this.behindDistance = setDist;
    	return this;
    }
    
    
    // ==================================================
  	//                  Should Execute
  	// ==================================================
	@Override
    public boolean canUse() {
    	Entity target = this.getTarget();
	    if(target == null || target == this.host)
	        return false;
        if(!target.isAlive())
        	return false;

		double distance = this.host.distanceTo(target);
	    if(distance > this.lostDistance && this.lostDistance != 0) {
			return false;
		}
	    if(distance <= this.strayDistance && this.strayDistance != 0) {
			return false;
		}
	    
        return true;
    }
    
    
    // ==================================================
  	//                Continue Executing
  	// ==================================================
	@Override
    public boolean canContinueToUse() {
    	Entity target = this.getTarget();
    	if(target == null)
    		return false;
        if(!target.isAlive())
        	return false;
        
        double distance = this.host.distanceTo(target);
        if(distance > this.lostDistance && this.lostDistance != 0) {
			this.setTarget(null);
			return false;
		}
        // Start straying when we reach halfway between the stray radius and the target
        if(distance <= this.strayDistance / 2.0 && this.strayDistance != 0) {
			return false;
		}
		this.onTargetDistance(distance, target);
        
        return this.getTarget() != null;
    }
    
    
    // ==================================================
  	//                       Start
  	// ==================================================
	@Override
    public void start() {
        this.updateRate = 0;
    }
    
    
    // ==================================================
  	//                      Update
  	// ==================================================
	@Override
    public void tick() {
        if(this.updateRate-- <= 0) {
            Entity target = this.getTarget();
			if(target instanceof Player) {
				this.updateRate = 10;
			}
			else {
				this.updateRate = 60;
			}
        	if(!this.host.useDirectNavigator()) {
        		if(this.behindDistance == 0 || !(target instanceof BaseCreatureEntity)) {
                    this.host.getNavigation().moveTo(target, this.speed);
                }
        		else {
        			BlockPos pos = ((BaseCreatureEntity)target).getFacingPosition(-this.behindDistance);
        			this.host.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), this.speed);
        		}
        	}
        	else {
        		if(this.behindDistance == 0 || !(target instanceof BaseCreatureEntity))
        			this.host.directNavigator.setTargetPosition(new BlockPos((int)target.position().x(), (int)target.position().y(), (int)target.position().z()), this.speed);
        		else {
                    BlockPos pos = ((BaseCreatureEntity)target).getFacingPosition(-this.behindDistance);
        			this.host.directNavigator.setTargetPosition(pos, this.speed);
        		}
        	}
        }
    }
	
    
	// ==================================================
 	//                       Reset
 	// ==================================================
	@Override
    public void stop() {
        this.host.clearMovement();
    }


	// ==================================================
	//                  Target Distance
	// ==================================================
	public void onTargetDistance(double distance, Entity followTarget) {

	}
}
