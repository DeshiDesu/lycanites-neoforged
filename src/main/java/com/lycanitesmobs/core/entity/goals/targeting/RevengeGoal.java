package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.data.info.creature.CreatureGroup;
import net.minecraft.world.entity.LivingEntity;

public class RevengeGoal extends FindAttackTargetGoal {
	
	// Properties:
	Class<? extends LivingEntity>[] helpClasses = null;
    private int revengeTime;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public RevengeGoal(BaseCreatureEntity setHost) {
        super(setHost);
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public RevengeGoal setHelpCall(boolean setHelp) {
    	this.callForHelp = setHelp;
    	return this;
    }

    public RevengeGoal setHelpClasses(Class<? extends LivingEntity>... setHelpClasses) {
    	this.helpClasses = setHelpClasses;
    	this.callForHelp = true;
    	return this;
    }

    public RevengeGoal setSightCheck(boolean setSightCheck) {
    	this.checkSight = setSightCheck;
    	return this;
    }

    public RevengeGoal setOnlyNearby(boolean setNearby) {
    	this.nearbyOnly = setNearby;
    	return this;
    }

    public RevengeGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
    	this.cantSeeTimeMax = setCantSeeTimeMax;
    	return this;
    }


	// ==================================================
	//                    Host Target
	// ==================================================
	@Override
	protected LivingEntity getTarget() {
		return super.getTarget();
	}

	@Override
	protected void setTarget(LivingEntity newTarget) {
		super.setTarget(newTarget);
	}
	
    
	// ==================================================
 	//                  Should Execute
 	// ==================================================
	@Override
    public boolean canUse() {
    	if(this.host.getLastHurtByMob() == null || !this.isEntityTargetable(this.host.getLastHurtByMob(), false))
    		return false;

		this.target = this.host.getLastHurtByMob();
		if(this.host.getLastHurtByMobTimestamp() != this.revengeTime) {
			this.revengeTime = this.host.getLastHurtByMobTimestamp();
			this.callNearbyForHelp();
		}

    	// Group Check:
		boolean shouldRevenge = this.host.creatureInfo.getGroups().isEmpty();
		boolean shouldPackHunt = false;
		for(CreatureGroup group : this.host.creatureInfo.getGroups()) {
			if (group.shouldRevenge(this.target)) {
				shouldRevenge = true;
			}
			if (group.shouldPackHunt(this.target)) {
				shouldPackHunt = true;
			}
		}
		if(!shouldRevenge && (!shouldPackHunt || !this.host.isInPack())) {
			return false;
		}

		return true;
    }
	
    
	// ==================================================
 	//                 Start Executing
 	// ==================================================
	@Override
    public void start() {
        super.start();
    }
}
