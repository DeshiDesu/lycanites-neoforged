package com.lycanitesmobs.core.entity.projectile.hellfire;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityHellShield extends BaseProjectileEntity {

    // Properties:
    public Entity shootingEntity;

    // ==================================================
    //                   Constructors
    // ==================================================
    public EntityHellShield(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public EntityHellShield(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityLivingBase) {
        super(entityType, world, entityLivingBase);
    }

    public EntityHellShield(EntityType<? extends BaseProjectileEntity> entityType, Level world, double x, double y, double z) {
        super(entityType, world, x, y, z);
    }

    // ========== Setup Projectile ==========
    public void setup() {
        this.entityName = "hellshield";
        this.modInfo = LycanitesMobs.modInfo;
        this.setDamage(0);
        this.setProjectileScale(1F);
        this.knockbackChance = 0D;
        this.noPhysics = true;
    }


    // ==================================================
    //                   Update
    // ==================================================
    @Override
    public void tick() {
        super.tick();
        if (this.position().y() > this.getCommandSenderWorld().getMaxBuildHeight() + 20)
            this.remove(RemovalReason.DISCARDED);
    }


    // ==================================================
    //                   Movement
    // ==================================================
    // ========== Gravity ==========
    @Override
    protected float getGravity() {
        return 0F;
    }


    // ==================================================
    //                     Impact
    // ==================================================
    //========== On Impact Particles/Sounds ==========
    @Override
    public void onImpactVisuals() {
        for (int i = 0; i < 8; ++i)
            this.getCommandSenderWorld().addParticle(DustParticleOptions.REDSTONE, this.position().x(), this.position().y(), this.position().z(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public boolean canDamage(LivingEntity entity) {
        return false;
    }


    // ==================================================
    //                      Sounds
    // ==================================================
    @Override
    public SoundEvent getLaunchSound() {
        return null;
    }


    // ==================================================
    //                   Brightness
    // ==================================================
    public float getBrightness() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }


}
