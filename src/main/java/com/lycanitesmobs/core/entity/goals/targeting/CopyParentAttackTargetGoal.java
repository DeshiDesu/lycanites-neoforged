package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;


public class CopyParentAttackTargetGoal extends Goal {
	// Targets:
	private BaseCreatureEntity host;
	
	// Properties:
    private boolean tameTargeting = true;
    
    // ==================================================
  	//                    Constructor
  	// ==================================================
    public CopyParentAttackTargetGoal(BaseCreatureEntity setHost) {
        host = setHost;
		this.setFlags(EnumSet.of(Flag.TARGET));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public CopyParentAttackTargetGoal setTameTargetting(boolean setTargetting) {
    	this.tameTargeting = setTargetting;
    	return this;
    }

    
    // ==================================================
  	//                   Should Execute
  	// ==================================================
    @Override
    public boolean canUse() {
    	if(this.host.getTarget() != null) {
    		if(!this.host.getTarget().isAlive())
    			return false;
    	}
    	if(this.host.getParentAttackTarget() == null)
    		return false;
    	return true;
    }

    
    // ==================================================
  	//                       Update
  	// ==================================================
    @Override
    public void tick() {
    	if(this.host.getTarget() == null) {
    		LivingEntity target = this.host.getParentAttackTarget();
    		if(this.isTargetValid(target))
    			this.host.setTarget(target);
    	}
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
		if(!this.host.canAttack(target))
			return false;
    	return true;
    }
    
    
    // ==================================================
 	//                       Reset
 	// ==================================================
    @Override
    public void stop() {
        this.host.setTarget(null);
    }
}
