package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.BaseGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BuildAroundTargetGoal extends BaseGoal {
    // Properties:
    private Block block = null;
    private int tickRate = 40;
    private int currentTick = 0;
    private int range = 2;
    private boolean enclose = false;

	public BuildAroundTargetGoal(BaseCreatureEntity setHost) {
        super(setHost);
    }

    /**
     * Sets the block to build around the target.
     * @param block The block to build.
     * @return This goal for chaining.
     */
    public BuildAroundTargetGoal setBlock(Block block) {
        this.block = block;
        return this;
    }

    /**
     * Sets the tick rate for building.
     * @param tickRate The tick rate (20 ticks = 1 second).
     * @return This goal for chaining.
     */
    public BuildAroundTargetGoal setTickRate(int tickRate) {
        this.tickRate = tickRate;
        return this;
    }

    /**
     * Sets the range from the target to build (where 0 would be on the target directly).
     * @param range The range in blocks.
     * @return This goal for chaining.
     */
    public BuildAroundTargetGoal setRange(int range) {
        this.range = range;
        return this;
    }

    /**
     * If true, the range will be reduced if any of the blocks to build are found within the initial range to build in.
     * @param enclose True if the block building range should decrease and engulf the target.
     * @return This goal for chaining.
     */
    public BuildAroundTargetGoal setEnclose(boolean enclose) {
        this.enclose = enclose;
        return this;
    }
    
    @Override
    public boolean canUse() {
        if(!super.canUse()) {
            return false;
        }
        return this.block == null || this.getTarget() != null;
    }

    @Override
    public void start() {
        this.currentTick = 0;
    }

    @Override
    public void tick() {
        if(this.currentTick++ < this.tickRate) {
            return;
        }
        this.currentTick = 0;

        int range = this.range;
        int enclosement = 0;
        BlockPos targetPos = this.getTarget().blockPosition();

        // Enclose:
        if(this.enclose) {
            int smallestRange = range;
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos blockPos = targetPos.offset(x, 0, z);
                    BlockState blockState = this.host.getCommandSenderWorld().getBlockState(blockPos);
                    if (blockState.getBlock() == this.block) {
                        smallestRange = Math.min(Math.max(Math.abs(x), Math.abs(z)), smallestRange);
                    }
                }
            }
            enclosement = range - ((range + 1) - smallestRange);
        }

        // Place Blocks:
        for(int currentRange = range; currentRange >= enclosement; currentRange--) {
            for (int x = -currentRange; x <= currentRange; x++) {
                for (int z = -currentRange; z <= currentRange; z++) {
                    if (Math.abs(x) != currentRange && Math.abs(z) != currentRange) {
                        continue;
                    }
                    BlockPos blockPos = targetPos.offset(x, 0, z);
                    BlockState blockState = this.host.getCommandSenderWorld().getBlockState(blockPos);
                    if (blockState.getBlock() == Blocks.AIR || blockState.getBlock() == this.block) {
                        this.host.getCommandSenderWorld().setBlockAndUpdate(blockPos, this.block.defaultBlockState());
                    }
                }
            }
        }

        // Play Sound:
        this.host.triggerAttackCooldown();
        this.host.playAttackSound();
    }
}
