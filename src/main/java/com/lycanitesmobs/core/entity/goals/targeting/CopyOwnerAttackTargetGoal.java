package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.world.entity.LivingEntity;

public class CopyOwnerAttackTargetGoal extends TargetingGoal {
	// Targets:
	private TameableCreatureEntity host;
	
	// Properties:
	private int lastAttackTime;
    
    // ==================================================
  	//                    Constructor
  	// ==================================================
    public CopyOwnerAttackTargetGoal(TameableCreatureEntity setHost) {
    	super(setHost);
        this.host = setHost;
        this.checkSight = false;
    }

    
    // ==================================================
  	//                   Should Execute
  	// ==================================================
    @Override
    public boolean canUse() {
    	if(!this.host.isTamed())
    		return false;
    	if(this.host.isSitting())
    		return false;
		if(!this.host.isAssisting() && !this.host.isAggressive())
			return false;
    	if(this.host.getOwner() == null)
    		return false;
        if (!(this.host.getOwner() instanceof LivingEntity))
            return false;

		if (this.host.isAggressive() && this.isValidTarget(this.target)) {
			this.setTarget(this.target);
			return true;
		}

        LivingEntity owner = (LivingEntity)this.host.getOwner();
		int lastAttackedTime = owner.getLastHurtMobTimestamp();
    	this.target = owner.getLastHurtMob();
		if(this.target == null) {
			ExtendedEntity extendedOwner = ExtendedEntity.getForEntity(owner);
			if(extendedOwner != null) {
				this.target = extendedOwner.lastAttackedEntity;
				lastAttackedTime = extendedOwner.lastAttackedTime;
			}
		}
		if(this.target == null) {
			return false;
		}
    	if(this.lastAttackTime == lastAttackedTime)
    		return false;
    	return true;
    }

    
    // ==================================================
  	//                       Start
  	// ==================================================
    @Override
    public void start() {
    	if(this.isEntityTargetable(this.target, false)) {
			this.lastAttackTime = ((LivingEntity)this.host.getOwner()).getLastHurtMobTimestamp();
			super.start();
		}
    }
    
    
    // ==================================================
 	//                  Continue Executing
 	// ==================================================
    @Override
    public boolean canContinueToUse() {
        if(this.host.isSitting() || !this.isValidTarget(this.getTarget()))
            return false;
        return super.canContinueToUse();
    }
    
    
    // ==================================================
  	//                    Valid Target
  	// ==================================================
	@Override
	protected boolean isValidTarget(LivingEntity target) {
    	if(target == null)
            return false;
    	if(!target.isAlive())
            return false;
		if(target == this.host)
            return false;
		if(!this.host.canAttackType(target.getType()))
            return false;
		if(!this.host.canAttack(target))
            return false;
    	return true;
    }
    
    
    // ==================================================
 	//                    Host Target
 	// ==================================================
    @Override
    protected LivingEntity getTarget() { return this.host.getTarget(); }
    @Override
    protected void setTarget(LivingEntity newTarget) { this.host.setTarget(newTarget); }
}
