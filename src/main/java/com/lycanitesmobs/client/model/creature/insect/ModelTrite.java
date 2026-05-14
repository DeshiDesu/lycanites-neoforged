package com.lycanitesmobs.client.model.creature.insect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelTrite extends ModelTemplateInsect {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelTrite() {
        this(1.0F);
    }

	public ModelTrite(float shadowSize) {
		// Load Model:
		this.initModel("trite", LycanitesMobs.modInfo, "entity/trite");

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
	}
}
