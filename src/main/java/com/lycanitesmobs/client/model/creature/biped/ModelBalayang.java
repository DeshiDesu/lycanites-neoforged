package com.lycanitesmobs.client.model.creature.biped;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBalayang extends ModelTemplateBiped {

	public ModelBalayang() {
		this(1.0F);
	}

	public ModelBalayang(float shadowSize) {
		this.initModel("balayang", LycanitesMobs.modInfo, "entity/balayang");
		this.trophyScale = 1.2F;
	}

	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		BaseCreatureEntity creatureEntity = null;
		if(entity instanceof BaseCreatureEntity)
			creatureEntity = (BaseCreatureEntity)entity;

		// Wings:
		if(entity != null && !entity.onGround() && !entity.isInWater() && (creatureEntity == null || !creatureEntity.hasPerchTarget())) {
			if (partName.equals("wingleft01")) {
				float rotX = -40;
				float rotY = 20.5F;
				float rotZ = (float) Math.toDegrees(Mth.sin(loop * 0.4F * this.wingScale) * 0.6F);
				this.rotate(rotX, rotY, rotZ);
				return;
			}
			if (partName.equals("wingright01")) {
				float rotX = -40;
				float rotY = -20.5F;
				float rotZ = (float) Math.toDegrees(Mth.sin(loop * 0.4F * this.wingScale + (float) Math.PI) * 0.6F);
				this.rotate(rotX, rotY, rotZ);
				return;
			}
			if (partName.equals("wingleft02")) {
				float rotZ = (float) Math.toDegrees(Mth.sin(loop * 0.4F * this.wingScale) * 0.15F);
				this.rotate(0, 0, rotZ);
				return;
			}
			if (partName.equals("wingright02")) {
				float rotZ = (float) Math.toDegrees(Mth.sin(loop * 0.4F * this.wingScale + (float) Math.PI) * 0.15F);
				this.rotate(0, 0, rotZ);
				return;
			}
		}

		// Looking:
		float rotX = 0;
		float rotY = 0;
		if(partName.toLowerCase().equals("body")) {
			rotX += (Math.toDegrees(lookX / (180F / (float)Math.PI)) * 0.75F);
			rotY += (Math.toDegrees(lookY / (180F / (float)Math.PI))) * 0.75F;
		}
		if(partName.equals("eye")) {
			rotX += (Math.toDegrees(lookX / (180F / (float)Math.PI)) * 0.25F);
			rotY += (Math.toDegrees(lookY / (180F / (float)Math.PI))) * 0.25F;
		}
		this.rotate(rotX, rotY, 0);

		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
	}
}
