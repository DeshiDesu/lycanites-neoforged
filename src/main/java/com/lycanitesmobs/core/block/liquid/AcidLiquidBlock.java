package com.lycanitesmobs.core.block.liquid;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class AcidLiquidBlock extends BaseLiquidBlock {
    public AcidLiquidBlock(Supplier<? extends FlowingFluid> fluidSupplier, Properties properties, String name, ElementInfo element, boolean destroyItems) {
        super(fluidSupplier, properties, name, element, destroyItems);
    }

    @Override
    public boolean shouldSpreadLiquid(Level world, BlockPos neighborBlockPos, BlockState blockState) {
        if (isWaterLikeFluid(world, neighborBlockPos)) {
            world.setBlock(neighborBlockPos, Blocks.DIORITE.defaultBlockState(), 4);
            return false;
        }

        if (isLavaLikeFluid(world, neighborBlockPos)) {
            world.setBlock(neighborBlockPos, Blocks.GRANITE.defaultBlockState(), 4);
            return false;
        }

        return super.shouldSpreadLiquid(world, neighborBlockPos, blockState);
    }


    @Override
    public void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity) {
        // Damage:
        if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb)) {
            entity.hurt(ObjectManager.getDamageSource(world, "acid"), 1F);
        }

        // Effects:
        if (entity instanceof LivingEntity) {
            MobEffect effect = ObjectManager.getEffect("penetration");
            if (effect != null) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(effect, 5 * 20, 0));
            }
        }

        super.entityInside(blockState, world, pos, entity);
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        float f;
        float f1;
        float f2;

        if (random.nextInt(100) == 0) {
            f = (float) pos.getX() + random.nextFloat();
            f1 = (float) pos.getY() + random.nextFloat() * 0.5F;
            f2 = (float) pos.getZ() + random.nextFloat();
            world.addParticle(ParticleTypes.RAIN, (double) f, (double) f1, (double) f2, 0.0D, 0.0D, 0.0D);
        }
        super.animateTick(state, world, pos, random);
    }
}
