package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;

import java.util.EnumSet;

import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PlaceBlockGoal extends Goal {
	// Targets:
    private BaseCreatureEntity host;
    
    // Properties:
    private double speed = 1.0D;
    private double range = 8.0D;
    private double maxDistance = 128.0D;
    private boolean replaceSolid = false;
    private boolean replaceLiquid = true;
    
    private BlockPos pos;
    public BlockState blockState;
    public int metadata = 0;
    
    private int repathTime = 0;
    
    // ==================================================
   	//                     Constructor
   	// ==================================================
    public PlaceBlockGoal(BaseCreatureEntity setHost) {
    	this.host = setHost;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public PlaceBlockGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    public PlaceBlockGoal setRange(double setRange) {
    	this.range = setRange;
    	return this;
    }
    public PlaceBlockGoal setMaxDistance(double setMax) {
    	this.maxDistance = setMax;
    	return this;
    }
    public PlaceBlockGoal setBlockPlacement(BlockState blockState, BlockPos pos) {
        this.blockState = blockState;
    	this.pos = pos;
    	return this;
    }
    public PlaceBlockGoal setMetadata(int setMetadata) {
    	this.metadata = setMetadata;
    	return this;
    }
    public PlaceBlockGoal setReplaceSolid(boolean bool) {
    	this.replaceSolid = bool;
    	return this;
    }
    public PlaceBlockGoal setReplaceLiquid(boolean bool) {
    	this.replaceLiquid = bool;
    	return this;
    }
    
    
    // ==================================================
   	//                  Should Execute
   	// ==================================================
	@Override
    public boolean canUse() {
        if(this.blockState == null)
            return false;
        
    	if(this.repathTime-- <= 0 && !this.canPlaceBlock(this.pos)) {
            this.blockState = null;
			this.repathTime = 20;
    		return false;
    	}

		this.repathTime = 0;
        return true;
    }
    
    
    // ==================================================
   	//                     Start
   	// ==================================================
	@Override
    public void start() {
    	if(!host.useDirectNavigator())
    		this.host.getNavigation().moveTo(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.speed);
    	else
    		host.directNavigator.setTargetPosition(this.pos, this.speed);
    }
	
    
	// ==================================================
 	//                       Reset
 	// ==================================================
	@Override
    public void stop() {
        this.host.getNavigation().stop();
        this.host.directNavigator.clearTargetPosition(1.0D);
        this.blockState = null;
    }
	
    
	// ==================================================
 	//                       Update
 	// ==================================================
	@Override
    public void tick() {
    	if(this.repathTime-- <= 0) {
    		this.repathTime = 20;
    		if(!host.useDirectNavigator())
        		this.host.getNavigation().moveTo(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.speed);
        	else
        		host.directNavigator.setTargetPosition(this.pos, this.speed);
    	}
    	
        this.host.getLookControl().setLookAt(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 30.0F, 30.0F);
        
        // Place Block:
        if(Math.sqrt(this.host.distanceToSqr(new Vec3(this.pos.getX(), this.pos.getY(), this.pos.getZ()))) <= this.range) {
        	this.host.getCommandSenderWorld().setBlock(this.pos, this.blockState, 3);
			this.host.onBlockPlaced(this.pos, this.blockState);
			this.blockState = null;
			this.host.clearMovement();
        }
        
        // Cancel If Too Far:
        if(this.host.distanceToSqr(new Vec3(this.pos.getX(), this.pos.getY(), this.pos.getZ())) >= this.maxDistance * this.maxDistance) {
            this.blockState = null;
            this.host.clearMovement();
        }
    }
    
    
    // ==================================================
   	//                  Can Place Block
   	// ==================================================
    public boolean canPlaceBlock(BlockPos pos) {
    	BlockState targetState = this.host.getCommandSenderWorld().getBlockState(pos);
		if(Material.WATER.contains(targetState.getBlock()) || Material.LAVA.contains(targetState.getBlock())) {
			if(!this.replaceLiquid)
				return false;
		}
		else if(Material.AIR.contains(targetState.getBlock()) && !this.replaceSolid)
			return false;
		if(!this.host.useDirectNavigator() && this.host.getNavigation() != null) {
			if(this.host.getNavigation().createPath(pos, 0) == null)
				return false;
		}
		return true;
    }
}
