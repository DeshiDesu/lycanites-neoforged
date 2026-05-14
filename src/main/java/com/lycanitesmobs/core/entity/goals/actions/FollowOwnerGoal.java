package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class FollowOwnerGoal extends FollowGoal {
	// Targets:
	TameableCreatureEntity host;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public FollowOwnerGoal(TameableCreatureEntity setHost) {
    	super(setHost);
        this.host = setHost;
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FollowOwnerGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    public FollowOwnerGoal setTargetClass(Class setTargetClass) {
    	this.targetClass = setTargetClass;
    	return this;
    }
    public FollowOwnerGoal setStrayDistance(double setDist) {
    	this.strayDistance = setDist;
    	return this;
    }
    public FollowOwnerGoal setLostDistance(double setDist) {
    	this.lostDistance = setDist;
    	return this;
    }
    
	
	// ==================================================
 	//                    Get Target
 	// ==================================================
    @Override
    public Entity getTarget() {
    	return this.host.getOwner();
    }

	@Override
	public void setTarget(Entity entity) {
    	// Do nothing here.
	}


	// ==================================================
	//                  Should Execute
	// ==================================================
	@Override
	public boolean canUse() {
		Entity target = this.getTarget();
		if(target == null)
			return false;
		if(!target.isAlive())
			return false;
		if(this.host.isSitting() || !this.host.isFollowing())
			return false;

		// Start straying when within the stray radius and the target.
		double distance = this.host.distanceTo(target);
		if(distance <= this.strayDistance && this.strayDistance != 0) {
			this.host.clearMovement();
			return false;
		}

		return true;
	}


	// ==================================================
	//                Continue Executing
	// ==================================================
	@Override
	public boolean canContinueToUse() {
    	return this.canUse();
	}
    
	
	// ==================================================
 	//                      Update
 	// ==================================================
	@Override
    public void tick() {
		if(this.host.distanceTo(this.getTarget()) >= this.lostDistance) {
			this.teleportToOwner();
		}
    	super.tick();
    }
    
    // ========== Teleport to Owner ==========
    public void teleportToOwner() {
    	if(this.getTarget() != null) {
			if(!this.host.canBreatheAir() && ((!this.host.isLavaCreature && !this.getTarget().isInWater()) || (this.host.isLavaCreature && !this.getTarget().isInLava()))) {
				return;
			}
			if(!this.host.canBreatheUnderwater() && this.getTarget().isInWater()) {
				return;
			}

	    	int i = Mth.floor(this.getTarget().position().x()) - 2;
	        int j = Mth.floor(this.getTarget().getBoundingBox().minY);
	        int k = Mth.floor(this.getTarget().position().z()) - 2;

            if(this.host.isFlying() || this.getTarget().isInWater()) {
                this.host.moveTo(i, j + 1, k, this.host.yRotO, this.host.xRotO);
                this.host.clearMovement();
                return;
            }
	
	        for(int x = -2; x <= 2; ++x) {
	            for(int z = -2; z <= 2; ++z) {
	                if(this.canTeleportTo(this.getTarget().blockPosition().offset(x, 0, z))) {
                        this.host.moveTo((float)(i + x) + 0.5F, j, (float)(k + z) + 0.5F, this.host.yRotO, this.host.xRotO);
	                    this.host.clearMovement();
	                    return;
	                }
	            }
	        }
    	}
    }

	protected boolean canTeleportTo(BlockPos blockPos) {
		BlockState blockState = this.host.getCommandSenderWorld().getBlockState(blockPos.below());
		return blockState.isValidSpawn(this.host.getCommandSenderWorld(), blockPos.below(), this.host.getType()) && this.host.getCommandSenderWorld().isEmptyBlock(blockPos) && this.host.getCommandSenderWorld().isEmptyBlock(blockPos.above());
	}
    
    //TODO Wait on the ChunkUnload Chunk event, if this mob is not sitting and the unloading chunk is what it's in, then teleport this mob to it's owner away from the unloaded chunk, unless it's player has disconnected.
}
