package com.lycanitesmobs.client.renderer.entity.projectile;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ProjectileRenderFactory<T extends BaseProjectileEntity> {
    protected ProjectileInfo projectileInfo;

    protected String oldProjectileName;
    protected Class oldProjectileClass;
    protected boolean oldModel;


    public ProjectileRenderFactory(ProjectileInfo projectileInfo) {
        this.projectileInfo = projectileInfo;
    }

    public ProjectileRenderFactory(String projectileName, Class projectileClass, boolean hasModel) {
        this.oldProjectileName = projectileName;
        this.oldProjectileClass = projectileClass;
        this.oldModel = hasModel;
    }

    public EntityRenderer<? super T> createRenderFor(EntityRendererProvider.Context manager) {
        // Old Projectile Obj Models:
        if (this.oldModel) {
            try {
                return new ProjectileModelRenderer(manager, this.oldProjectileName);
            } catch (Exception e) {
                LMHelperClass.logWarning("", "An exception occurred when creating an old projectile renderer:");
                e.printStackTrace();
            }
        }

        // Old Projectile Item Sprite Models:
        if (this.oldProjectileClass != null) {
            return new ProjectileSpriteRenderer(manager, this.oldProjectileClass);
        }

        // New JSON Projectile:
        if (this.projectileInfo != null && this.projectileInfo.modelClassName != null) {
            try {
                return new ProjectileModelRenderer(manager, this.projectileInfo);
            } catch (Exception e) {
                LMHelperClass.logWarning("", "An exception occurred when creating a projectile renderer:");
                e.printStackTrace();
            }
        }
        return new ProjectileSpriteRenderer(manager, CustomProjectileEntity.class);
    }

}
