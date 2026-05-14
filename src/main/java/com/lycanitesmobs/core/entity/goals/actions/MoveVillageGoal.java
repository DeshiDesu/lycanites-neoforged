package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;


public class MoveVillageGoal extends Goal {
	// Targets:
    private BaseCreatureEntity host;
    
    // Properties:
    private int frequency = 200;
    private boolean isNocturnal = true;
    private BlockPos blockPos;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public MoveVillageGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public MoveVillageGoal setFrequency(int frequency) {
    	this.frequency = frequency;
    	return this;
    }
    public MoveVillageGoal setNocturnal(boolean flag) {
    	this.isNocturnal = flag;
    	return this;
    }
	
    
	// ==================================================
 	//                  Should Execute
 	// ==================================================
	@Override
    public boolean canUse() {
        if (this.host.isVehicle()) {
            return false;
        } else if (this.isNocturnal && this.host.level().isDay()) {
            return false;
        } else if (this.host.getRandom().nextInt(this.frequency) != 0) {
            return false;
        } else {
            ServerLevel serverWorld = (ServerLevel)this.host.level();
            BlockPos blockPos = new BlockPos(this.host.blockPosition());
            if (!serverWorld.isCloseToVillage(blockPos, 6)) {
                return false;
            } else {
                Vec3 lvt_3_1_ = LandRandomPos.getPos(this.host, 15, 7, (p_220755_1_) -> {
                    return (-serverWorld.getBlockFloorHeight(p_220755_1_));
                });
                if (lvt_3_1_ == null) return false;
                this.blockPos = BlockPos.containing(lvt_3_1_.x, lvt_3_1_.y, lvt_3_1_.z);
                return true;
            }
        }
    }
	
    
	// ==================================================
 	//                Continue Executing
 	// ==================================================
	@Override
    public boolean canContinueToUse() {
        return this.blockPos != null && !this.host.getNavigation().isDone() && this.host.getNavigation().getTargetPos().equals(this.blockPos);
    }

    
    // ==================================================
    //                     Update
    // ==================================================
    @Override
    public void tick() {
        if (this.blockPos != null) {
            PathNavigation navigation = this.host.getNavigation();
            Vec3i hostVec = new Vec3i((int) this.host.position().x(), (int) this.host.position().y(), (int) this.host.position().z());
            if (navigation.isDone() && !this.blockPos.closerThan(hostVec, 10.0D)) {
                Vec3 targetVec = new Vec3(this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ());
                Vec3 hostPos = this.host.position();
                Vec3 direction = targetVec.subtract(hostPos).normalize();
                Vec3 moveTarget = hostPos.add(direction.scale(10.0D));
                BlockPos moveBlockPos = this.host.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(moveTarget.x, moveTarget.y, moveTarget.z));
                if (!navigation.moveTo(moveBlockPos.getX(), moveBlockPos.getY(), moveBlockPos.getZ(), 1.0D)) {
                    this.moveRandomly();
                }
            }
        }
    }

    private void moveRandomly() {
        RandomSource lvt_1_1_ = this.host.getRandom();
        BlockPos lvt_2_1_ = this.host.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.host.blockPosition().offset(-8 + lvt_1_1_.nextInt(16), 0, -8 + lvt_1_1_.nextInt(16)));
        this.host.getNavigation().moveTo((double)lvt_2_1_.getX(), (double)lvt_2_1_.getY(), (double)lvt_2_1_.getZ(), 1.0D);
    }
}
