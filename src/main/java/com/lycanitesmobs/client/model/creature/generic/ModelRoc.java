package com.lycanitesmobs.client.model.creature.generic;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelRoc extends CreatureObjModel {
    public ModelRoc() {
        this(1.0F);
    }
    
    public ModelRoc(float shadowSize) {
    	this.initModel("roc", LycanitesMobs.modInfo, "entity/roc");

        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
		float posX = 0F;
		float posY = 0F;
		float posZ = 0F;
		float rotX = 0F;
		float rotY = 0F;
		float rotZ = 0F;

		// Idle:
		if(partName.equals("mouth")) {
			this.rotate((float)-Math.toDegrees(Mth.cos(loop * 0.2F) * 0.05F), 0.0F, 0.0F);
		}
		if(entity != null && !entity.isInWater()) {
			if (partName.equals("wingleft")) {
				rotX = 20;
				rotX -= Math.toDegrees(Mth.sin(loop * 0.4F) * 0.6F);
				rotZ -= Math.toDegrees(Mth.sin(loop * 0.4F) * 0.6F);
			}
			if (partName.equals("wingright")) {
				rotX = 20;
				rotX -= Math.toDegrees(Mth.sin(loop * 0.4F) * 0.6F);
				rotZ -= Math.toDegrees(Mth.sin(loop * 0.4F + (float) Math.PI) * 0.6F);
			}
		}

		// Tail:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPickupEntity()) {
			if (partName.contains("tail0")) {
				rotX -= 5;
				rotX -= Math.toDegrees(Mth.sin(loop * 0.2F) * 0.02F);
			}
			if (partName.equals("tailclaw")) {
				rotX -= 25;
			}
		}
		else {
			if (partName.contains("tail0")) {
				rotX += 10;
				rotX -= Math.toDegrees(Mth.sin(loop * 0.1F) * 0.1F);
			}
			if (partName.equals("tailclaw")) {
				rotX += Math.toDegrees(Mth.sin(loop * 0.1F) * 0.4F);
			}
		}

		// Body Bob:
		if(partName.equals("body")) {
			float bob = -Mth.sin(loop * 0.2F) * 0.1F;
			if (bob < 0)
				bob = -bob;
			posY += bob;
		}

		// Apply Animations:
		this.translate(posX, posY, posZ);
		this.rotate(rotX, rotY, rotZ);
    }
}
