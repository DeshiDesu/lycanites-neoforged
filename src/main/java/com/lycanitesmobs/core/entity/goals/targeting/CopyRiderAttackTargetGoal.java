package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.world.entity.LivingEntity;

public class CopyRiderAttackTargetGoal extends TargetingGoal {
	// Targets:
	private TameableCreatureEntity host;
	
	// Properties:
	private int lastAttackTime;
    
    // ==================================================
  	//                    Constructor
  	// ==================================================
    public CopyRiderAttackTargetGoal(TameableCreatureEntity setHost) {
    	super(setHost);
        this.host = setHost;
        this.checkSight = false;
    }

    
    // ==================================================
  	//                   Should Execute
  	// ==================================================
    @Override
    public boolean canUse() {
    	if(!this.host.hasRiderTarget())
    		return false;
    	if(this.host.isSitting())
    		return false;
    	if(this.host.getRider() == null)
    		return false;
    	
    	this.target = this.host.getRider().getLastHurtMob();
    	if(this.target == null) {
    		return false;
    	}
    	if(lastAttackTime == this.host.getRider().getLastHurtMobTimestamp())
    		return false;
    	return true;
    }

    
    // ==================================================
  	//                       Start
  	// ==================================================
    @Override
    public void start() {
    	if(isTargetValid(target)) {
			lastAttackTime = this.host.getRider().getLastHurtMobTimestamp();
			super.start();
		}
    }
    
    
    // ==================================================
 	//                  Continue Executing
 	// ==================================================
    @Override
    public boolean canContinueToUse() {
    	if(!this.host.hasRiderTarget())
    		return false;
        if(this.host.isSitting())
            return false;
        return super.canContinueToUse();
    }
    
    
    // ==================================================
  	//                    Valid Target
  	// ==================================================
    private boolean isTargetValid(LivingEntity target) {
    	if(target == null)
    		return false;
    	if(!target.isAlive())
    		return false;
		if(target == this.host)
			return false;
		if(!this.host.canAttackType(target.getType()))
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
