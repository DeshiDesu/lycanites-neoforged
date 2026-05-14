package com.lycanitesmobs.client.model.creature.insect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelTriteVoid extends ModelTemplateInsect {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelTriteVoid() {
        this(1.0F);
    }

	public ModelTriteVoid(float shadowSize) {
		// Load Model:
		this.initModel("trite_void", LycanitesMobs.modInfo, "entity/trite_void");

		// Scales:
		this.mouthScaleX = 1.0F;
		this.mouthScaleY = 0.1F;

		// Trophy:
		this.trophyScale = 1.0F;
	}


	// ==================================================
	//                 Animate Part
	// ==================================================
	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		// Walking Bob:
		if(partName.equals("body")) {
			float bob = Mth.cos(time * 0.6662F + (float) Math.PI) * 0.3F * distance;
			if (bob < 0)
				bob += -bob * 2;
			translate(0, bob, 0);
		}

		// Tentacles:
		if(partName.contains("tentacle")) {
			float loopOffset = 0;
			if(partName.contains("front")) {
				loopOffset += 10;
			}
			else if(partName.contains("back")) {
				loopOffset += 30;
			}

			if(partName.contains("left")) {
				this.rotate(
						(float) Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F),
						(float) Math.toDegrees(Mth.sin((loop + (loopOffset / 2)) * 0.2F) * 0.25F) - 10,
						(float) -Math.toDegrees(Mth.cos((loop + (loopOffset / 2)) * 0.09F) * 0.1F)
				);
			}
			else {
				this.rotate(
						(float) Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F),
						(float) -Math.toDegrees(Mth.sin((loop + (loopOffset / 2)) * 0.2F) * 0.25F) - 10,
						(float) -Math.toDegrees(Mth.cos((loop + (loopOffset / 2)) * 0.09F) * 0.1F)
				);
			}
		}
	}
}
