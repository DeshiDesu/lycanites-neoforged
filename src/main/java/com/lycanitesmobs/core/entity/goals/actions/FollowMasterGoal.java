package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class FollowMasterGoal extends FollowGoal {
	// Targets:
	BaseCreatureEntity host;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public FollowMasterGoal(BaseCreatureEntity setHost) {
    	super(setHost);
        this.host = setHost;
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FollowMasterGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    public FollowMasterGoal setTargetClass(Class setTargetClass) {
    	this.targetClass = setTargetClass;
    	return this;
    }
    public FollowMasterGoal setStrayDistance(double setDist) {
    	this.strayDistance = setDist;
    	return this;
    }
    public FollowMasterGoal setLostDistance(double setDist) {
    	this.lostDistance = setDist;
    	return this;
    }
    
	
	// ==================================================
 	//                    Get Target
 	// ==================================================
    @Override
    public Entity getTarget() {
		if(this.host.getMasterTarget() != null && this.host.getMasterTarget() instanceof BaseCreatureEntity && ((BaseCreatureEntity)this.host.getMasterTarget()).isBossAlways()) {
			return null;
		}
    	return this.host.getMasterTarget();
    }

	@Override
	public void setTarget(Entity entity) {
		if(entity instanceof LivingEntity)
			this.host.setMasterTarget((LivingEntity) entity);
	}
}
