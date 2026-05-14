package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.google.common.base.Predicate;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.util.TargetSorterNearest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;


public class GetItemGoal extends Goal {
	// Targets:
	private BaseCreatureEntity host;
	private ItemEntity target;
	
	// Properties:
    private Predicate<ItemEntity> targetSelector;
    private TargetSorterNearest targetSorter;
    private double distanceMax = 32.0D * 32.0D;
    double speed = 1.0D;
    private boolean checkSight = true;
    private int cantSeeTime = 0;
    protected int cantSeeTimeMax = 60;
    private int updateRate = 0;
    private int recheckTime = 0;
    public boolean tamedLooting = true;
    
    // ==================================================
  	//                    Constructor
  	// ==================================================
    public GetItemGoal(BaseCreatureEntity setHost) {
        super();
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.host = setHost;
        this.targetSelector = input -> true;
        this.targetSorter = new TargetSorterNearest(setHost);
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public GetItemGoal setDistanceMax(double setDouble) {
    	this.distanceMax = setDouble * setDouble;
    	return this;
    }

    public GetItemGoal setSpeed(double setDouble) {
    	this.speed = setDouble;
    	return this;
    }
    
    public GetItemGoal setCheckSight(boolean setBool) {
    	this.checkSight = setBool;
    	return this;
    }
    
    public GetItemGoal setTamedLooting(boolean bool) {
    	this.tamedLooting = bool;
    	return this;
    }
    
    
    // ==================================================
  	//                  Should Execute
  	// ==================================================
	@Override
    public boolean canUse() {
    	if(!this.host.canPickupItems())
    		return false;

    	if(this.recheckTime++ < 40) {
    		return false;
		}
		this.recheckTime = 0;

    	if(!this.tamedLooting) {
    		if(this.host instanceof TameableCreatureEntity)
    			if(this.host.isTamed())
    				return false;
    	}
    	
        double heightDistance = 4.0D;
        if(this.host.useDirectNavigator())
        	heightDistance = this.distanceMax;
        List<ItemEntity> possibleTargets = this.host.getCommandSenderWorld().getEntitiesOfClass(ItemEntity.class, this.host.getBoundingBox().inflate(this.distanceMax, heightDistance, this.distanceMax), this.targetSelector);
        
        if(possibleTargets.isEmpty())
            return false;
        Collections.sort(possibleTargets, this.targetSorter);
        this.target = possibleTargets.get(0);
        
        return this.canContinueToUse();
    }
    
    
    // ==================================================
 	//                  Continue Executing
 	// ==================================================
	@Override
    public boolean canContinueToUse() {
    	if(this.target == null)
            return false;
        if(!this.target.isAlive())
            return false;
        
        double distance = this.host.distanceTo(target);
        if(distance > this.distanceMax)
        	return false;
        
        if(this.checkSight)
            if(this.host.getSensing().hasLineOfSight(this.target))
                this.cantSeeTime = 0;
            else if(++this.cantSeeTime > this.cantSeeTimeMax)
                return false;
        
        return true;
    }
    
    
    // ==================================================
 	//                      Reset
 	// ==================================================
	@Override
    public void stop() {
        this.target = null;
        this.host.clearMovement();
    }
    
    
    // ==================================================
  	//                       Start
  	// ==================================================
	@Override
    public void start() {
        this.updateRate = 0;
    }
    
    
    // ==================================================
  	//                      Update
  	// ==================================================
	@Override
    public void tick() {
        if(this.updateRate-- <= 0) {
            this.updateRate = 20;
        	if(!this.host.useDirectNavigator())
        		this.host.getNavigation().moveTo(this.target, this.speed);
        	else
        		this.host.directNavigator.setTargetPosition(new BlockPos((int)this.target.position().x(), (int)this.target.position().y(), (int)this.target.position().z()), this.speed);
        }
    }
}
