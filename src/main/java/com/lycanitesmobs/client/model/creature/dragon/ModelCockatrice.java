package com.lycanitesmobs.client.model.creature.dragon;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateDragon;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelCockatrice extends ModelTemplateDragon {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelCockatrice() {
        this(1.0F);
    }

    public ModelCockatrice(float shadowSize) {
        // Load Model:
        this.initModel("cockatrice", LycanitesMobs.modInfo, "entity/cockatrice");

		// Looking:
		this.lookHeadScaleX = 0.8f;
		this.lookHeadScaleY = 0.8f;
		this.lookNeckScaleX = 0.2f;
		this.lookNeckScaleY = 0.2f;

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[] {0.0F, -0.25F, 0.0F};
    }

    // ==================================================
    //                 Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        float rotX = 0F;
        float rotY = 0F;
        float rotZ = 0F;

		if(partName.equals("wattle")) {
			rotZ += (float) -Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F + 0.1F);
		}

		if(partName.equals("tail") || partName.equals("tail01") || partName.equals("tail02")) {
			rotX += (float) -Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F - 0.1F);
			rotY += (float) -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.4F - 0.2F);
		}

        if(entity != null && !entity.onGround()) {
			float flightLoop = Mth.sin(loop * 0.4F);
			float flightLoopRev = Mth.sin(loop * 0.4F + (float)Math.PI);

            if (partName.equals("wingleftupper")) {
				this.rotate(80F, -(float)Math.toDegrees(flightLoop * 0.3F), -35F);
				this.scale(1, 1, -1);
            }
            if (partName.equals("wingrightupper")) {
				this.rotate(80F, (float)Math.toDegrees(flightLoop * 0.3F), 35F);
				this.scale(1, 1, -1);
            }

			if (partName.equals("legleft")) {
				rotX += 30F + Math.toDegrees(flightLoop * 0.3F);
			}
			if (partName.equals("legright")) {
				rotX += 30F + Math.toDegrees(flightLoop * 0.3F);
			}
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
    }
}
