package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.world.entity.LivingEntity;

public class FindGroupAvoidTargetGoal extends FindAvoidTargetGoal {

    // ==================================================
  	//                    Constructor
  	// ==================================================
    public FindGroupAvoidTargetGoal(BaseCreatureEntity host) {
        super(host);
    }
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FindGroupAvoidTargetGoal setChance(int setChance) {
    	this.targetChance = setChance;
    	return this;
    }

    public FindGroupAvoidTargetGoal setSightCheck(boolean setSightCheck) {
    	this.checkSight = setSightCheck;
    	return this;
    }

    public FindGroupAvoidTargetGoal setOnlyNearby(boolean setNearby) {
    	this.nearbyOnly = setNearby;
    	return this;
    }

    public FindGroupAvoidTargetGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
    	this.cantSeeTimeMax = setCantSeeTimeMax;
    	return this;
    }

    public FindGroupAvoidTargetGoal setTameTargetting(boolean setTargetting) {
    	this.tameTargeting = setTargetting;
    	return this;
    }

    public FindGroupAvoidTargetGoal setHelpCall(boolean setHelp) {
        this.callForHelp = setHelp;
        return this;
    }
    
    
    // ==================================================
 	//                 Valid Target Check
 	// ==================================================
    @Override
    protected boolean isValidTarget(LivingEntity target) {
		// Tamed Check:
		if(target instanceof TameableCreatureEntity && ((TameableCreatureEntity)target).isTamed())
			return false;

		// Group Check:
		return this.host.shouldCreatureGroupFlee(target);
    }
}
