package com.lycanitesmobs.core.block.fire;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.block.base.BlockFireBase;
import com.lycanitesmobs.core.manager.ItemManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockSmitefire extends BlockFireBase {

    public BlockSmitefire(Properties properties) {
        this(properties, "smitefire");
    }

    public BlockSmitefire(Properties properties, String name) {
        super(properties, name);

        // Stats:
        this.tickRate = 30;
        this.dieInRain = false;
        this.triggerTNT = false;
        this.agingRate = 3;
        this.spreadChance = 2;
        this.removeOnTick = false;
        this.removeOnNoFireTick = false;

        ItemManager.getInstance().cutoutBlocks.add(this);
    }

    /*@Override
    public Item getItemDropped(BlockState state, Random random, int zero) {
        return ObjectManager.getItem("aetherwavecharge");
    }*/

    @Override
    public void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity) {
        super.entityInside(blockState, world, pos, entity);

        if (entity instanceof LivingEntity) {
            MobEffect effect = ObjectManager.getEffect("smited");
            if (effect != null) {
                MobEffectInstance effectInstance = new MobEffectInstance(effect, 10 * 20, 0);
                LivingEntity entityLiving = (LivingEntity) entity;
                if (entityLiving.canBeAffected(effectInstance))
                    entityLiving.addEffect(effectInstance);
            }
        }

        if (entity instanceof ItemEntity)
            if (((ItemEntity) entity).getItem().getItem() == ObjectManager.getItem("aetherwavecharge"))
                return;

        if (entity.isInvulnerableTo(world.damageSources().inFire()))
            return;

        entity.hurt(world.damageSources().inFire(), 1);
        entity.setSecondsOnFire(3);
    }

    @Override
    public boolean isBlockFireSource(BlockState state, LevelAccessor world, BlockPos pos, Direction side) {
        if (state.getBlock() == Blocks.DIAMOND_BLOCK || state.getBlock() == Blocks.GOLD_BLOCK) {
            return true;
        }
        return super.isBlockFireSource(state, world, pos, side);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (random.nextInt(100) == 0) {
            x = pos.getX() + random.nextFloat();
            z = pos.getZ() + random.nextFloat();
            world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
            world.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.0D, 0.0D);
        }
        super.animateTick(state, world, pos, random);
    }
}