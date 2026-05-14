package com.lycanitesmobs.core.block.liquid;

import com.lycanitesmobs.core.data.info.element.ElementInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class MoglavaLiquidBlock extends BaseLiquidBlock {
    public MoglavaLiquidBlock(Supplier<? extends FlowingFluid> fluidSupplier, Block.Properties properties, String name, ElementInfo element, boolean destroyItems) {
        super(fluidSupplier, properties, name, element, destroyItems);
    }

    @Override
    public boolean shouldSpreadLiquid(Level world, BlockPos neighborBlockPos, BlockState blockState) {
        if (isWaterLikeFluid(world, neighborBlockPos)) {
            world.setBlock(neighborBlockPos, Blocks.STONE.defaultBlockState(), 2);
            return false;
        }

        return super.shouldSpreadLiquid(world, neighborBlockPos, blockState);
    }

    @Override
    public void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity)
            entity.hurt(world.damageSources().lava(), 10F);
        super.entityInside(blockState, world, pos, entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        float f;
        float f1;
        float f2;

        if (random.nextInt(100) == 0) {
            f = (float) pos.getX() + random.nextFloat();
            f1 = (float) pos.getY() + random.nextFloat() * 0.5F;
            f2 = (float) pos.getZ() + random.nextFloat();
            world.addParticle(ParticleTypes.LAVA, (double) f, (double) f1, (double) f2, 0.0D, 0.0D, 0.0D);
        }
        super.animateTick(state, world, pos, random);
    }
}
