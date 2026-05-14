package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelGrigori extends ModelTemplateBiped {
    public ModelGrigori() {
        this(1.0F);
    }

    public ModelGrigori(float shadowSize) {
		this.initModel("grigori", LycanitesMobs.modInfo, "entity/grigori");
		this.mouthRate = 8;
		this.mouthScale = -1;
		this.flightBobScale = 0.1F;
		this.trophyScale = 0.5F;
    }

	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

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
	}
}
