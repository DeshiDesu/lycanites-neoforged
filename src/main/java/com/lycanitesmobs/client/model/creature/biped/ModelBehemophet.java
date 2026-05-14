package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBehemophet extends ModelTemplateBiped {
    public ModelBehemophet() {
        this(1.0F);
    }

    public ModelBehemophet(float shadowSize) {
		this.initModel("behemophet", LycanitesMobs.modInfo, "entity/behemophet");

		this.trophyScale = 1.2F;
		this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }

	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		// Pickup:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPickupEntity()) {
			if (partName.contains("armleft")) {
				this.rotate(-45, 0, -10);
			}
			else if (partName.contains("armright")) {
				this.rotate(-45, 0, 10);
			}
		}
	}
}
