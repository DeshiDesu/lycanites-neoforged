package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;


public class LookIdleGoal extends Goal {
    // Targets:
    private BaseCreatureEntity host;

    // Properties:
    private int idleTime;
    private int idleTimeMin = 20;
    private int idleTimeRange = 20;
    private double lookX;
    private double lookZ;
    
    // ==================================================
   	//                    Constructor
   	// ==================================================
    public LookIdleGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public LookIdleGoal setTimeMin(int setTimeMin) {
    	this.idleTimeMin = setTimeMin;
    	return this;
    }
    public LookIdleGoal setTimeRange(int setTimeRange) {
    	this.idleTimeRange = setTimeRange;
    	return this;
    }
    
    
    // ==================================================
   	//                  Should Execute
   	// ==================================================
	@Override
    public boolean canUse() {
        return this.host.rollLookChance();
    }
    
    
    // ==================================================
   	//                Continue Executing
   	// ==================================================
	@Override
    public boolean canContinueToUse() {
        return this.idleTime >= 0;
    }
    
    
    // ==================================================
   	//                     Start
   	// ==================================================
	@Override
    public void start() {
        double d0 = (Math.PI * 2D) * this.host.getRandom().nextDouble();
        this.lookX = Math.cos(d0);
        this.lookZ = Math.sin(d0);
        this.idleTime = idleTimeMin + this.host.getRandom().nextInt(idleTimeRange);
    }
    
    
    // ==================================================
   	//                     Update
   	// ==================================================
	@Override
    public void tick() {
        this.idleTime--;
        this.host.getLookControl().setLookAt(
        		this.host.position().x() + this.lookX,
        		this.host.position().y() + (double)this.host.getEyeHeight(),
        		this.host.position().z() + this.lookZ, 10.0F,
        		(float)this.host.getMaxHeadXRot()
        		);
    }
}
