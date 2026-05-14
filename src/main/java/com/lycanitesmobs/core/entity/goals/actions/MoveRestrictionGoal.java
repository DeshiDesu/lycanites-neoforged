package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import org.joml.Vector3d;

public class MoveRestrictionGoal extends Goal {
	// Targets:
    private BaseCreatureEntity host;
    
    // Properties:
    private double speed = 1.0D;
    private double movePosX;
    private double movePosY;
    private double movePosZ;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public MoveRestrictionGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public MoveRestrictionGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    
    
    // ==================================================
  	//                  Should Execute
  	// ==================================================
	@Override
    public boolean canUse() {
        if(this.host.hasHome())
            return false;
        BlockPos chunkcoordinates = this.host.getRestrictCenter();
        Vector3d vec3 = RandomPositionGenerator.findRandomTargetTowards(this.host, 16, 7, new Vector3d((double)chunkcoordinates.getX(), (double)chunkcoordinates.getY(), (double)chunkcoordinates.getZ()));
        if(vec3 == null)
            return false;
        
        this.movePosX = vec3.x;
        this.movePosY = vec3.y;
        this.movePosZ = vec3.z;
        return true;
    }
    
    
    // ==================================================
  	//                Continue Executing
  	// ==================================================
	@Override
    public boolean canContinueToUse() {
        return !this.host.getNavigation().isDone();
    }
    
    
    // ==================================================
  	//                     Start
  	// ==================================================
	@Override
    public void start() {
        this.host.getNavigation().moveTo(this.movePosX, this.movePosY, this.movePosZ, this.speed);
    }
}
