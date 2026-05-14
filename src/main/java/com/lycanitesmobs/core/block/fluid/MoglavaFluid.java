package com.lycanitesmobs.core.block.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class MoglavaFluid extends ForgeFlowingFluid {
    protected MoglavaFluid(Properties props) {
        super(props);
    }

    @Override
    protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState, Direction dir, FluidState newState) {
        if (dir == Direction.DOWN) {
            FluidState fluidBelow = level.getFluidState(pos);

            if (fluidBelow.is(FluidTags.WATER)) {
                if (blockState.getBlock() instanceof LiquidBlock) {
                    level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
                level.levelEvent(1501, pos, 0);
                return;
            }
        }

        super.spreadTo(level, pos, blockState, dir, newState);
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockGetter getter, BlockPos pos, Fluid fluid, Direction dir) {
        return state.getHeight(getter, pos) >= 0.44444445F && fluid.is(FluidTags.WATER);
    }

    public static class Source extends MoglavaFluid {
        public Source(Properties props) {
            super(props);
        }

        public boolean isSource(FluidState state) {
            return true;
        }

        public int getAmount(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends MoglavaFluid {
        public Flowing(Properties props) {
            super(props);
        }

        public boolean isSource(FluidState state) {
            return false;
        }

        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }
    }

}
