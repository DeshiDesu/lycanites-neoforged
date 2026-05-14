package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.world.entity.LivingEntity;

public class FindAvoidTargetGoal extends TargetingGoal {
	// Targets:
    private Class targetClass = LivingEntity.class;
    
    // Properties:
	protected int targetChance = 0;
    protected boolean tameTargeting = false;
    
    // ==================================================
  	//                    Constructor
  	// ==================================================
    public FindAvoidTargetGoal(BaseCreatureEntity setHost) {
        super(setHost);
		//this.setMutexFlags(EnumSet.of(Flag.TARGET));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FindAvoidTargetGoal setChance(int setChance) {
    	this.targetChance = setChance;
    	return this;
    }

    public FindAvoidTargetGoal setTargetClass(Class setTargetClass) {
    	this.targetClass = setTargetClass;
    	return this;
    }

    public FindAvoidTargetGoal setSightCheck(boolean setSightCheck) {
    	this.checkSight = setSightCheck;
    	return this;
    }

    public FindAvoidTargetGoal setOnlyNearby(boolean setNearby) {
    	this.nearbyOnly = setNearby;
    	return this;
    }

    public FindAvoidTargetGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
    	this.cantSeeTimeMax = setCantSeeTimeMax;
    	return this;
    }

    public FindAvoidTargetGoal setTameTargetting(boolean setTargetting) {
    	this.tameTargeting = setTargetting;
    	return this;
    }

    public FindAvoidTargetGoal setHelpCall(boolean setHelp) {
        this.callForHelp = setHelp;
        return this;
    }
    
    
    // ==================================================
 	//                    Host Target
 	// ==================================================
    @Override
    protected LivingEntity getTarget() { return this.host.getAvoidTarget(); }
    @Override
    protected void setTarget(LivingEntity newTarget) { this.host.setAvoidTarget(newTarget); }
    
    
    // ==================================================
 	//                 Valid Target Check
 	// ==================================================
    @Override
    protected boolean isValidTarget(LivingEntity target) {
        // Target Class Check:
        if(this.targetClass != null && !this.targetClass.isAssignableFrom(target.getClass()))
            return false;

        // Own Class Check:
    	if(this.targetClass != this.host.getClass() && target.getClass() == this.host.getClass())
            return false;

		// Tamed Check:
		if(target instanceof TameableCreatureEntity && ((TameableCreatureEntity)target).isTamed())
			return false;
        
    	return true;
    }
    
    
    // ==================================================
  	//                   Should Execute
  	// ==================================================
    @Override
    public boolean canUse() {
        if (!this.tameTargeting && this.host.isTamed()) {
            return false;
        }
		if (this.host.updateTick % 60 != 0) {
			return false;
		}
		if(this.targetChance > 0 && this.host.getRandom().nextInt(this.targetChance) != 0) {
			return false;
		}

		// Check for other avoid target AIs:
        LivingEntity avoidTarget = this.getTarget();
        if(avoidTarget != null && !this.isValidTarget(avoidTarget)) {
            return false;
        }

    	this.target = null;
        
        double distance = this.getTargetDistance();
        double heightDistance = 4.0D;
        if(this.host.useDirectNavigator())
            heightDistance = distance;
        this.target = this.getNewTarget(distance, heightDistance, distance);
        if(this.callForHelp)
            this.callNearbyForHelp();
        return this.target != null;
    }
}
