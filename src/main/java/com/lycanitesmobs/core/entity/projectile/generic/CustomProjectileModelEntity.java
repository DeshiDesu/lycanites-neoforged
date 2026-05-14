package com.lycanitesmobs.core.entity.projectile.generic;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class CustomProjectileModelEntity extends CustomProjectileEntity {

	// ==================================================
	//                   Constructors
	// ==================================================
	public CustomProjectileModelEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
		super(entityType, world);
		this.modInfo = LycanitesMobs.modInfo;
	}

	public CustomProjectileModelEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, ProjectileInfo projectileInfo) {
		super(entityType, world, projectileInfo);
	}

	public CustomProjectileModelEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityLiving, ProjectileInfo projectileInfo) {
		super(entityType, world, entityLiving, projectileInfo);
	}

	public CustomProjectileModelEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, double x, double y, double z, ProjectileInfo projectileInfo) {
		super(entityType, world, x, y, z, projectileInfo);
	}


	// ==================================================
	//                      Visuals
	// ==================================================
	@Override
	public String getTextureName() {
		return this.entityName.toLowerCase();
	}

	@Override
	public ResourceLocation getTexture() {
		if("projectile".equals(this.getTextureName()))
			return null;
		if(TextureManager.getTexture(this.getTextureName()) == null)
			TextureManager.addTexture(this.getTextureName(), "textures/projectile/" + this.getTextureName() + ".png");
		return TextureManager.getTexture(this.getTextureName());
	}

}
