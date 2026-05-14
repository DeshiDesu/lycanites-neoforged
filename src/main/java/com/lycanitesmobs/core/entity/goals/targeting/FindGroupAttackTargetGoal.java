package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.data.info.creature.CreatureGroup;
import net.minecraft.world.entity.LivingEntity;

public class FindGroupAttackTargetGoal extends FindAttackTargetGoal {

    // ==================================================
  	//                    Constructor
  	// ==================================================
    public FindGroupAttackTargetGoal(BaseCreatureEntity setHost) {
        super(setHost);

		for(CreatureGroup group : setHost.creatureInfo.groups) {
			for(CreatureGroup targetGroup : group.huntGroups) {
				if (targetGroup.humanoids) {
					this.targetPlayers = true;
					break;
				}
			}
			for(CreatureGroup targetGroup : group.packGroups) {
				if (targetGroup.humanoids) {
					this.targetPlayers = true;
					break;
				}
			}
		}
    }


    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FindGroupAttackTargetGoal setCheckSight(boolean bool) {
    	this.checkSight = bool;
    	return this;
    }
    
    public FindGroupAttackTargetGoal setOnlyNearby(boolean setNearby) {
    	this.nearbyOnly = setNearby;
    	return this;
    }

    public FindGroupAttackTargetGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
    	this.cantSeeTimeMax = setCantSeeTimeMax;
    	return this;
    }

    public FindGroupAttackTargetGoal setRange(double range) {
        this.targetingRange = range;
        return this;
    }

    public FindGroupAttackTargetGoal setHelpCall(boolean setHelp) {
        this.callForHelp = setHelp;
        return this;
    }
    
    public FindGroupAttackTargetGoal setTameTargetting(boolean setTargetting) {
    	this.tameTargeting = setTargetting;
    	return this;
    }
    
    
    // ==================================================
 	//                 Valid Target Check
 	// ==================================================
    @Override
    protected boolean isValidTarget(LivingEntity target) {
		// Tamed Targeting Check:
		if(!this.tameTargeting && this.host.isTamed()) {
			return false;
		}

		// Group Check:
		if(!this.host.shouldCreatureGroupHunt(target)) {
			return false;
		}

		// Group Check:
		if(!this.host.shouldCreatureGroupHunt(target)) {
			return false;
		}
    	
    	// Type Check:
    	if(!this.host.canAttackType(target.getType()))
            return false;

        // Entity Check:
		if(!this.host.canAttack(target)) {
			return false;
		}

		// Relationships Check:
		CreatureRelationshipEntry relationshipEntry = this.host.relationships.getEntry(target);
		if (relationshipEntry != null && !relationshipEntry.canHunt()) {
			return false;
		}

		// Random Chance:
		if(!this.host.rollAttackTargetChance(target)) {
			return false;
		}
        
    	return true;
    }


	// ==================================================
	//                  Get New Target
	// ==================================================
	@Override
	public LivingEntity getNewTarget(double rangeX, double rangeY, double rangeZ) {
		return super.getNewTarget(rangeX, rangeY, rangeZ);
	}
}
