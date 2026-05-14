package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.world.entity.LivingEntity;

public class RevengeOwnerGoal extends FindAttackTargetGoal {
	
	// Targets:
	private TameableCreatureEntity host;
	
	// Properties:
    private int revengeTime;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public RevengeOwnerGoal(TameableCreatureEntity setHost) {
        super(setHost);
    	this.host = setHost;
    	this.tameTargeting = true;
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public RevengeOwnerGoal setHelpCall(boolean setHelp) {
    	this.callForHelp = setHelp;
    	return this;
    }
    public RevengeOwnerGoal setSightCheck(boolean setSightCheck) {
    	this.checkSight = setSightCheck;
    	return this;
    }
    public RevengeOwnerGoal setOnlyNearby(boolean setNearby) {
    	this.nearbyOnly = setNearby;
    	return this;
    }
    public RevengeOwnerGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
    	this.cantSeeTimeMax = setCantSeeTimeMax;
    	return this;
    }
	
    
	// ==================================================
 	//                  Should Execute
 	// ==================================================
    public boolean canUse() {
    	if(!this.host.isTamed())
    		return false;
    	if(this.host.getOwner() == null)
    		return false;
        if (!(this.host.getOwner() instanceof LivingEntity))
            return false;
        LivingEntity owner = (LivingEntity)this.host.getOwner();
        int i = owner.getLastHurtByMobTimestamp();
        if(i == this.revengeTime)
        	return false;
        if(!this.isEntityTargetable(owner.getLastHurtByMob(), false))
        	return false;
        return true;
    }
	
    
	// ==================================================
 	//                 Start Executing
 	// ==================================================
    public void start() {
        LivingEntity owner = (LivingEntity)this.host.getOwner();
        this.target = owner.getLastHurtByMob();
        this.revengeTime = owner.getLastHurtByMobTimestamp();
        if(this.callForHelp) {
            this.callNearbyForHelp();
        }
        super.start();
    }
}
