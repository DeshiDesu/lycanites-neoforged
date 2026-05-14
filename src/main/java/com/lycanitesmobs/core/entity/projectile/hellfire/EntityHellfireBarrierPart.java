package com.lycanitesmobs.core.entity.projectile.hellfire;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityHellfireBarrierPart extends EntityHellfireWall {

    // ==================================================
 	//                   Constructors
 	// ==================================================
    public EntityHellfireBarrierPart(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public EntityHellfireBarrierPart(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity par2LivingEntity) {
        super(entityType, world, par2LivingEntity);
    }

    public EntityHellfireBarrierPart(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6) {
        super(entityType, world, par2, par4, par6);
    }

    // ========== Setup Projectile ==========
    @Override
    public void setup() {
        this.entityName = "hellfirebarrierpart";
        super.setup();
        this.animationFrameMax = 19;
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    public String getTextureName() {
        return "hellfirebarrier";
    }
}
