package com.lycanitesmobs.core.entity.dispenser;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.Random;

public class BaseProjectileDispenseBehaviour extends AbstractProjectileDispenseBehavior {
    protected ProjectileInfo projectileInfo;

    protected String oldProjectileName;
    protected Class<? extends BaseProjectileEntity> oldProjectileClass;

    // ==================================================
    //                      Constructor
    // ==================================================
    public BaseProjectileDispenseBehaviour(ProjectileInfo projectileInfo) {
        super();
        this.projectileInfo = projectileInfo;
    }

    public BaseProjectileDispenseBehaviour(Class<? extends BaseProjectileEntity> oldProjectileClass, String oldProjectileName) {
        super();
        this.oldProjectileClass = oldProjectileClass;
        this.oldProjectileName = oldProjectileName;
    }

    // ==================================================
    //                      Dispense
    // ==================================================
    @Override
    public ItemStack execute(BlockSource blockSource, ItemStack stack) {
        Level world = blockSource.getLevel();
        Position position = DispenserBlock.getDispensePosition(blockSource);
        Direction facing = blockSource.getBlockState().getValue(DispenserBlock.FACING);

        Projectile projectile = this.getProjectile(world, position, stack);
        if (projectile == null)
            return stack;

        projectile.shoot((double) facing.getStepX(), (double) facing.getStepY(), (double) facing.getStepZ(), this.getPower(), this.getUncertainty());
        world.addFreshEntity(projectile);
        stack.split(1);

        return stack;
    }

    @Override
    protected Projectile getProjectile(Level world, Position pos, ItemStack stack) {
        if (this.projectileInfo != null) {
            return this.projectileInfo.createProjectile(world, pos.x(), pos.y(), pos.z());
        }
        if (this.oldProjectileClass != null) {
            return ProjectileManager.getInstance().createOldProjectile(this.oldProjectileClass, world, pos.x(), pos.y(), pos.z());
        }
        return null;
    }

    @Override
    protected float getUncertainty() {
        return 0F;
    }

    @Override
    protected float getPower() {
        if (this.projectileInfo != null) {
            return (float) this.projectileInfo.velocity;
        }
        return 1.1F;
    }


    // ==================================================
    //                        Sound
    // ==================================================
    @Override
    protected void playSound(BlockSource blockSource) {
        SoundEvent soundEvent = this.getDispenseSound();
        if (soundEvent == null || blockSource == null)
            return;
        blockSource.getLevel().playSound(null, blockSource.getPos(), soundEvent, SoundSource.AMBIENT, 1.0F, 1.0F / (new Random().nextFloat() * 0.4F + 0.8F));
    }

    protected SoundEvent getDispenseSound() {
        if (this.projectileInfo != null) {
            return this.projectileInfo.getLaunchSound();
        }
        return ObjectManager.getSound(this.oldProjectileName);
    }
}