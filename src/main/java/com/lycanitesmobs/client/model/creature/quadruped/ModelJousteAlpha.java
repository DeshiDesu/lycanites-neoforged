package com.lycanitesmobs.client.model.creature.quadruped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelJousteAlpha extends ModelTemplateQuadruped {
	public ModelJousteAlpha() {
		this(1.0F);
	}

	public ModelJousteAlpha(float shadowSize) {
		this.initModel("joustealpha", LycanitesMobs.modInfo, "entity/joustealpha");
		this.trophyScale = 1.0F;
		this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};

		this.lookHeadScaleX = 0.25F;
		this.lookHeadScaleY = 0.25F;
		this.lookNeckScaleX = 0.5F;
		this.lookNeckScaleY = 0.5F;

		this.legAnimationScale = 0;
	}

	float maxLeg = 0F;
	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		float posY = 0F;
		float rotX = 0F;
		float rotY = 0F;
		float rotZ = 0F;
		float rotation = 0F;
		float angleY = 0F;
		float angleZ = 0F;

		this.lookHeadScaleX = 0.25F;
		this.lookHeadScaleY = 0.25F;
		this.lookNeckScaleX = 0.5F;
		this.lookNeckScaleY = 0.5F;

		// Leg Angles:
		if(partName.equals("legleftfront") || partName.equals("legleftback")
				|| partName.equals("legrightfront") || partName.equals("legrightback")) {
			angleZ = 1F;
		}
		if(partName.equals("legleftfront")) {
			angleY = 35F / 360F;
		}
		if(partName.equals("legleftback")) {
			angleY = -35F / 360F;
		}
		if(partName.equals("legrightfront")) {
			angleY = -35F / 360F;
		}
		if(partName.equals("legrightback")) {
			angleY = 35F / 360F;
		}

		// Walking:
		float walkSwing = 0.3F;
		if (partName.equals("legrightfront") || partName.equals("legleftback")) {
			rotation += Math.toDegrees(Mth.cos(time * 0.6662F + (float) Math.PI) * walkSwing * distance);
		}
		else if (partName.equals("legleftfront") || partName.equals("legrightback")) {
			rotation += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);
		}
		else if (partName.equals("body")) {
			float bob = Mth.cos(time * 0.6662F + (float) Math.PI) * walkSwing * distance;
			if (bob < 0) {
				bob += -bob * 2;
			}
			posY += bob;
		}

		// Attack:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
			if(partName.equals("mouth")) {
				this.rotate(30.0F, 0.0F, 0.0F);
			}
		}

		this.angle(rotation, 0, angleY, angleZ);
		this.rotate(rotX, rotY, rotZ);
		this.translate(0, posY, 0);
	}
}
