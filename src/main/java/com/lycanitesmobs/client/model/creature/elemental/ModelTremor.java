package com.lycanitesmobs.client.model.creature.elemental;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelTremor extends ModelTemplateElemental {

	// ==================================================
  	//                  Constructors
  	// ==================================================
    public ModelTremor() {
        this(1.0F);
    }

    public ModelTremor(float shadowSize) {

		// Load Model:
		this.initModel("tremor", LycanitesMobs.modInfo, "entity/tremor");

		// Trophy:
		this.trophyScale = 1.2F;
		this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
		this.trophyMouthOffset = new float[] {0.0F, -0.25F, 0.0F};
    }


	// ==================================================
	//                 Animate Part
	// ==================================================
	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		if(!"body".equals(partName) && !"mouth".equals(partName)) {
			float vibration = loop * 2;
			if(partName.contains("right")) {
				vibration = -vibration;
			}
			this.translate(Mth.cos(vibration) * 0.01f, Mth.cos(vibration) * 0.01f, Mth.cos(vibration) * 0.01f);
		}

		if(partName.contains("rib") && !partName.contains("07")) {
			float angleX = 0;
			float angleY = 1;
			float angleZ = 0;
			if(!partName.contains("arm")) {
				if (partName.contains("04")) {
					angleZ = 0.3f;
				}
				else if (partName.contains("05")) {
					angleY = 0.6f;
					angleZ = 0.8f;
				}
				else if (partName.contains("06")) {
					angleZ = 0.3f;
				}
			}
			else {
				angleY = 1;
				if (partName.contains("01")) {
					angleX = 0.4f;
					angleZ = -0.45f;
				}
				else if (partName.contains("02")) {
					angleX = 0.4f;
					angleZ = -0.45f;
				}
				else if (partName.contains("03")) {
					angleX = 0.4f;
					angleZ = -0.45f;
				}
				if(partName.contains("armleft")) {
					angleX = -angleX;
				}
			}
			this.angle(loop * 50F, angleX, angleY, angleZ);
		}
	}
}
