package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;


public class MateGoal extends Goal {
	// Targets:
    private AgeableCreatureEntity host;
    private AgeableCreatureEntity partner;
    
    // Properties:
    private double speed = 1.0D;
    private Class targetClass;
    private int mateTime;
    private int mateTimeMax = 60;
    private double mateDistance = 9.0D;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public MateGoal(AgeableCreatureEntity setHost) {
        this.host = setHost;
        this.targetClass = this.host.getClass();
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public MateGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    public MateGoal setMateDistance(double setDouble) {
    	this.mateDistance = setDouble * setDouble;
    	return this;
    }
    public MateGoal setTargetClass(Class setTargetClass) {
    	this.targetClass = setTargetClass;
    	return this;
    }
    public MateGoal setMateTime(int setTime) {
    	this.mateTimeMax = setTime;
    	return this;
    }
    
    
    // ==================================================
  	//                  Should Execute
  	// ==================================================
	@Override
    public boolean canUse() {
        if(!this.host.canMate()) {
			return false;
		}
        this.partner = this.getPartner();
        return this.partner != null;
    }
    
    
    // ==================================================
  	//                Continue Executing
  	// ==================================================
	@Override
    public boolean canContinueToUse() {
        return this.partner != null && this.partner.isAlive() && this.partner.isInLove() && this.mateTime < mateTimeMax;
    }
    
    
    // ==================================================
  	//                      Reset
  	// ==================================================
	@Override
    public void stop() {
        this.partner = null;
        this.mateTime = 0;
    }
    
    
    // ==================================================
  	//                      Update
  	// ==================================================
	@Override
    public void tick() {
        this.host.getLookControl().setLookAt(this.partner, 10.0F, (float)this.host.getMaxHeadXRot());
        if(!this.host.useDirectNavigator())
        	this.host.getNavigation().moveTo(this.partner, this.speed);
        else
        	this.host.directNavigator.setTargetPosition(new BlockPos((int)this.partner.position().x(), (int)this.partner.position().y(), (int)this.partner.position().z()), speed);
        if(this.host.distanceToSqr(this.partner) < this.mateDistance + this.host.getPhysicalRange()) {
			++this.mateTime;
			if(this.mateTime >= mateTimeMax) {
				this.host.procreate(this.partner);
			}
		}
    }
    
    
    // ==================================================
  	//                    Get Partner
  	// ==================================================
    private AgeableCreatureEntity getPartner() {
        float distance = 8.0F;
        List possibleMates = this.host.getCommandSenderWorld().getEntitiesOfClass(this.targetClass, this.host.getBoundingBox().inflate((double)distance, (double)distance, (double)distance));
        double closestDistance = Double.MAX_VALUE;
        AgeableCreatureEntity newMate = null;
        Iterator possibleMate = possibleMates.iterator();
        
        while(possibleMate.hasNext())  {
        	LivingEntity nextEntity = (LivingEntity)possibleMate.next();
        	if(nextEntity instanceof AgeableCreatureEntity) {
	        	AgeableCreatureEntity testMate = (AgeableCreatureEntity)nextEntity;
	            if(this.host.canBreedWith(testMate) && this.host.distanceTo(testMate) < closestDistance) {
	            	newMate = testMate;
	            	closestDistance = this.host.distanceTo(testMate);
	            }
        	}
        }
        return newMate;
    }
}
