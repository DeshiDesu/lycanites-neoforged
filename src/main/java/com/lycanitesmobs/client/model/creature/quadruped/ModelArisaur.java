package com.lycanitesmobs.client.model.creature.quadruped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelArisaur extends ModelTemplateQuadruped {
    public ModelArisaur() {
        this(1.0F);
    }
    
    public ModelArisaur(float shadowSize) {
    	this.initModel("arisaur", LycanitesMobs.modInfo, "entity/arisaur");

        this.trophyScale = 0.5F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.6F};

		this.lookHeadScaleX = 0.5F;
		this.lookHeadScaleY = 0.5F;
		this.lookNeckScaleX = 0.25F;
		this.lookNeckScaleY = 0.25F;
    }

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

    	if (partName.contains("vine")) {
    		float rotX = 0;
    		float rotZ = 0;
			if (partName.contains("left")) {
				rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
			}
			if (partName.contains("right")) {
				rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
			}
			if (!partName.contains("body")) {
				rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
			}
    		this.rotate(rotX, 0, rotZ);
		}
    }
}
