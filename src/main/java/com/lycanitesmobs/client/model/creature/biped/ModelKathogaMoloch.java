package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelKathogaMoloch extends ModelTemplateBiped {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelKathogaMoloch() {
        this(1.0F);
    }

    public ModelKathogaMoloch(float shadowSize) {

		// Load Model:
		this.initModel("kathoga_moloch", LycanitesMobs.modInfo, "entity/kathoga_moloch");

		// Head/Neck:
		this.lookHeadScaleX = 0.5F;
		this.lookHeadScaleY = 0.5F;
		this.lookNeckScaleX = 0.5F;
		this.lookNeckScaleY = 0.5F;
		this.bigChildHead = true;

		// Trophy:
		this.trophyScale = 1.2F;
		this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }
}
