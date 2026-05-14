package com.lycanitesmobs.core.entity.projectile.hellfire;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.projectile.misc.LaserEndProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.LaserProjectileEntity;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityHellLaserEnd extends LaserEndProjectileEntity {

    // ==================================================
    //                   Constructors
    // ==================================================
    public EntityHellLaserEnd(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public EntityHellLaserEnd(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6, LaserProjectileEntity laser) {
        super(entityType, world, par2, par4, par6, laser);
    }

    public EntityHellLaserEnd(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity shooter, LaserProjectileEntity laser) {
        super(entityType, world, shooter, laser);
    }

    // ========== Setup Projectile ==========
    public void setup() {
        this.entityName = "helllaserend";
        this.modInfo = LycanitesMobs.modInfo;
    }

    // ========== Stats ==========
    @Override
    public void setStats() {
        super.setStats();
        this.setSpeed(1.0D);
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    @Override
    public String subTypeString() {
        return "events";
    }

    @Override
    public ResourceLocation getTexture() {
        if (TextureManager.getTexture(this.entityName + "End") == null)
            TextureManager.addTexture(this.entityName + "End", "textures/item/" + subTypeString() + "/" + this.entityName.toLowerCase() + "_end.png");
        return TextureManager.getTexture(this.entityName + "End");
    }


    // ==================================================
    //                      Sounds
    // ==================================================
    @Override
    public SoundEvent getLaunchSound() {
        return ObjectManager.getSound(entityName);
    }
}
