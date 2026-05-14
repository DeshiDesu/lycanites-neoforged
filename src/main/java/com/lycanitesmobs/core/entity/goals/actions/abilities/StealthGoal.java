package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class StealthGoal extends Goal {
	// Targets:
	private BaseCreatureEntity host;
	
	// Properties:
	private int stealthTimeMax = 20;
	private int stealthTimeMaxPrev = 20;
	private int stealthTime = 0;
	private int unstealthRate = 4;
	private boolean stealthMove = false;
	private boolean stealthAttack = false;
	
	private boolean unstealth = false;
	
    // ==================================================
 	//                    Constructor
 	// ==================================================
	public StealthGoal(BaseCreatureEntity setHost) {
		this.host = setHost;
		this.setFlags(EnumSet.noneOf(Goal.Flag.class));
	}
	
	
    // ==================================================
 	//                    Properties
 	// ==================================================
	public StealthGoal setStealthTime(int time) {
		this.stealthTimeMax = time;
		return this;
	}
	public StealthGoal setUnstealthRate(int rate) {
		this.unstealthRate = rate;
		return this;
	}
	public StealthGoal setStealthMove(boolean flag) {
		this.stealthMove = flag;
		return this;
	}
	public StealthGoal setStealthAttack(boolean flag) {
		this.stealthAttack = flag;
		return this;
	}

	
    // ==================================================
 	//                   Should Execute
 	// ==================================================
	@Override
	public boolean canUse() {
		this.unstealth = false;
		if(this.host.isLeashed()) this.unstealth = true;
		
		if(!this.stealthMove && this.host.isMoving()) {
 			this.unstealth = true;
		}
		
		if(!this.stealthAttack && this.host.getTarget() != null)
			this.unstealth = true;
		if(!this.host.canStealth())
			this.unstealth = true;

		return !this.unstealth;
	}

	
    // ==================================================
 	//                 Continue Executing
 	// ==================================================
	@Override
	public boolean canContinueToUse() {
		if(this.host.isLeashed()) this.unstealth = true;
		
		if(!this.stealthMove) {
			if(!this.host.useDirectNavigator() && !this.host.getNavigation().isDone())
				this.unstealth = true;
			if(this.host.useDirectNavigator() && !this.host.directNavigator.atTargetPosition())
				this.unstealth = true;
		}
		
		if(!this.stealthAttack && this.host.getTarget() != null)
			this.unstealth = true;
		if(!this.host.canStealth())
			this.unstealth = true;
		
		if(this.unstealth && this.host.getStealth() <= 0)
			return false;
		
		if(this.stealthTimeMaxPrev != this.stealthTimeMax)
			return false;
		
		return true;
	}

	
    // ==================================================
 	//                 Start Executing
 	// ==================================================
	@Override
	public void start() {
		this.host.setStealth(0F);
		this.stealthTime = 0;
		this.stealthTimeMaxPrev = this.stealthTimeMax;
	}

	
    // ==================================================
 	//                  Reset Task
 	// ==================================================
	@Override
	public void stop() {
		this.host.setStealth(0F);
		this.stealthTime = 0;
		this.stealthTimeMaxPrev = this.stealthTimeMax;
	}

	
    // ==================================================
 	//                  Update Task
 	// ==================================================
	@Override
	public void tick() {
		float nextStealth = (float)this.stealthTime / (float)this.stealthTimeMax;
		this.host.setStealth(nextStealth);
		
		if(!this.unstealth && this.stealthTime < this.stealthTimeMax)
			this.stealthTime++;
		else if(this.unstealth && this.stealthTime > 0)
			this.stealthTime -= this.unstealthRate;
		this.stealthTime = Math.min(Math.max(this.stealthTime, 0), this.stealthTimeMax);
	}
}
