package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FindParentGoal extends TargetingGoal {
	// Targets:
	private AgeableCreatureEntity host;
    private Class targetClass;
    
    // Properties:
    private boolean tameTargeting = true;
    private int targetChance = 0;
    private double targetDistance = -1D;
    
    // ==================================================
  	//                    Constructor
  	// ==================================================
    public FindParentGoal(AgeableCreatureEntity setHost) {
        super(setHost);
        this.host = setHost;
        this.targetClass = this.host.getClass();
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FindParentGoal setChance(int setChance) {
    	this.targetChance = setChance;
    	return this;
    }
    public FindParentGoal setTargetClass(Class setTargetClass) {
    	this.targetClass = setTargetClass;
    	return this;
    }
    public FindParentGoal setSightCheck(boolean setSightCheck) {
    	this.checkSight = setSightCheck;
    	return this;
    }
    public FindParentGoal setOnlyNearby(boolean setNearby) {
    	this.nearbyOnly = setNearby;
    	return this;
    }
    public FindParentGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
    	this.cantSeeTimeMax = setCantSeeTimeMax;
    	return this;
    }
    public FindParentGoal setDistance(double setDist) {
    	this.targetDistance = setDist;
    	return this;
    }
    public FindParentGoal setTameTargetting(boolean setTargetting) {
    	this.tameTargeting = setTargetting;
    	return this;
    }
    
    
    // ==================================================
 	//                    Host Target
 	// ==================================================
    @Override
    protected LivingEntity getTarget() { return this.host.getParentTarget(); }
    @Override
    protected void setTarget(LivingEntity newTarget) { this.host.setParentTarget(newTarget); }
    
    
    // ==================================================
 	//                 Valid Target Check
 	// ==================================================
    @Override
    protected boolean isValidTarget(LivingEntity target) {
        // Target Class Check:
        if(this.targetClass != null && !this.targetClass.isAssignableFrom(target.getClass()))
            return false;

		if(target.isBaby()) {
			return false;
		}
        
        // Tamed Checks:
        if(!this.tameTargeting && this.host instanceof TameableCreatureEntity && this.host.isTamed())
        	return false;
    	return true;
    }
    
    
    // ==================================================
 	//                 Get Target Distance
 	// ==================================================
    @Override
    protected double getTargetDistance() {
    	if(this.targetDistance > -1)
    		return this.targetDistance;
        AttributeInstance attributeinstance = this.host.getAttribute(Attributes.FOLLOW_RANGE);
        return attributeinstance.getValue();
    }
    
    
    // ==================================================
  	//                   Should Execute
  	// ==================================================
    @Override
    public boolean canUse() {
		if (this.host.updateTick % 20 != 0 || !this.host.shouldFindParent()) {
			return false;
		}

    	this.target = null;
    	if(!this.host.shouldFollowParent()) {
    		this.host.setParentTarget(null);
    		return false;
    	}

		if(this.host.hasParent()) {
			return false;
		}
    	
        if(this.host.updateTick % 20 != 0 && this.targetChance > 0 && this.host.getRandom().nextInt(this.targetChance) != 0)
            return false;
        
        double distance = this.getTargetDistance();
        double heightDistance = 4.0D;
        if(this.host.useDirectNavigator())
            heightDistance = distance;
        this.target = this.getNewTarget(distance, heightDistance, distance);
        return this.target != null;
    }
    
    
    // ==================================================
 	//                  Continue Executing
 	// ==================================================
    public boolean canContinueToUse() {
    	if(this.host.getGrowingAge() >= 0) {
    		this.host.setParentTarget(null);
    		return false;
    	}
    	
    	return super.canContinueToUse();
    }
}
