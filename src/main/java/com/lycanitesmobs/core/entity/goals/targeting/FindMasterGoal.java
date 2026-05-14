package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;

public class FindMasterGoal extends TargetingGoal {
	// Targets:
    private Class targetClass = LivingEntity.class;
    
    // Properties:
    private boolean tameTargeting = false;
    private int targetChance = 0;
    
    // ==================================================
  	//                    Constructor
  	// ==================================================
    public FindMasterGoal(BaseCreatureEntity setHost) {
        super(setHost);
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FindMasterGoal setTameTargetting(boolean setTargetting) {
    	this.tameTargeting = setTargetting;
    	return this;
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FindMasterGoal setChance(int setChance) {
    	this.targetChance = setChance;
    	return this;
    }
    public FindMasterGoal setTargetClass(Class setTargetClass) {
    	this.targetClass = setTargetClass;
    	return this;
    }
    public FindMasterGoal setSightCheck(boolean setSightCheck) {
    	this.checkSight = setSightCheck;
    	return this;
    }
    public FindMasterGoal setOnlyNearby(boolean setNearby) {
    	this.nearbyOnly = setNearby;
    	return this;
    }
    public FindMasterGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
    	this.cantSeeTimeMax = setCantSeeTimeMax;
    	return this;
    }
    public FindMasterGoal setRange(double setDist) {
    	this.targetingRange = setDist;
    	return this;
    }
    
    
    // ==================================================
 	//                    Host Target
 	// ==================================================
    @Override
    protected LivingEntity getTarget() { return this.host.getMasterTarget(); }
    @Override
    protected void setTarget(LivingEntity newTarget) { this.host.setMasterTarget(newTarget); }
    
    
    // ==================================================
 	//                 Valid Target Check
 	// ==================================================
    @Override
    protected boolean isValidTarget(LivingEntity target) {
        // Target Class Check:
        if(this.targetClass != null && !this.targetClass.isAssignableFrom(target.getClass()))
            return false;

        if(target instanceof Animal && ((Animal)target).getAge() < 0)
            return false;
    	if(target instanceof AgeableCreatureEntity && ((AgeableCreatureEntity)target).getGrowingAge() < 0)
            return false;
        
        // Tamed Checks:
        if(!this.tameTargeting && this.host.isTamed())
        	return false;
    	return true;
    }


    // ==================================================
 	//                 Get Target Distance
 	// ==================================================
    @Override
    protected double getTargetDistance() {
    	if(this.targetingRange > 0)
    		return this.targetingRange;
    	AttributeInstance attributeinstance = this.host.getAttribute(Attributes.FOLLOW_RANGE);
        return attributeinstance.getValue();
    }
    
    
    // ==================================================
  	//                   Should Execute
  	// ==================================================
    @Override
    public boolean canUse() {
		if (this.host.updateTick % 20 != 0) {
			return false;
		}
		if(this.targetChance > 0 && this.host.getRandom().nextInt(this.targetChance) != 0) {
			return false;
		}

		this.target = null;
        
        double distance = this.getTargetDistance();
        double heightDistance = 4.0D;
        if(this.host.useDirectNavigator())
            heightDistance = distance;
        this.target = this.getNewTarget(distance, heightDistance, distance);
        return this.target != null;
    }
}
