package com.lycanitesmobs.core.block.fire;

import com.lycanitesmobs.core.block.base.BlockFireBase;
import com.lycanitesmobs.core.manager.ItemManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockFrostfire extends BlockFireBase {

    public BlockFrostfire(Block.Properties properties) {
        this(properties, "frostfire");
    }

    public BlockFrostfire(Block.Properties properties, String name) {
        super(properties, name);

        // Stats:
        this.tickRate = 30;
        this.dieInRain = false;
        this.triggerTNT = false;
        this.agingRate = 3;
        this.spreadChance = 1;
        this.removeOnTick = false;
        this.removeOnNoFireTick = false;

        ItemManager.getInstance().cutoutBlocks.add(this);

    }

    /*@Override
    public Item getItemDropped(BlockState state, Random random, int zero) {
        return ObjectManager.getItem("icefirecharge");
    }*/

    @Override
    public boolean canCatchFire(BlockGetter world, BlockPos pos, Direction face) {
        Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.ICE || block == Blocks.PACKED_ICE)
            return true;
        return false;
    }


    @Override
    public boolean isBlockFireSource(BlockState state, LevelAccessor world, BlockPos pos, Direction side) {
        if (state.getBlock() == Blocks.PACKED_ICE || state.getBlock() == Blocks.SNOW_BLOCK)
            return true;
        return false;
    }

    @Override
    public int getBlockFlammability(BlockGetter world, BlockPos pos, Direction face) {
        Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.ICE)
            return 20;
        return 0;
    }

    @Override
    protected boolean canDie(Level world, BlockPos pos) {
        return false;
    }

    @Override
    public void burnBlockReplace(Level world, BlockPos pos, int newFireAge) {
        if (world.getBlockState(pos).getBlock() == Blocks.ICE) {
            world.setBlock(pos, Blocks.PACKED_ICE.defaultBlockState(), 3);
            return;
        }
        super.burnBlockReplace(world, pos, newFireAge);
    }

    @Override
    public void burnBlockDestroy(Level world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == Blocks.ICE) {
            world.setBlock(pos, Blocks.PACKED_ICE.defaultBlockState(), 3);
            return;
        }
        super.burnBlockDestroy(world, pos);
    }

    @Override
    public void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity) {
        super.entityInside(blockState, world, pos, entity);

        if (entity instanceof LivingEntity) {
            MobEffectInstance effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3 * 20, 0);
            LivingEntity entityLiving = (LivingEntity) entity;
            if (entityLiving.canBeAffected(effect))
                entityLiving.addEffect(effect);
            else
                return; // Entities immune to slow are immune to frostfire damage.
        }

        if (entity instanceof ItemEntity)
            return;
        entity.hurt(world.damageSources().magic(), 2);
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
            world.addParticle(ParticleTypes.ITEM_SNOWBALL, x, y, z, 0.0D, 0.0D, 0.0D);
        }
        super.animateTick(state, world, pos, random);
    }
}