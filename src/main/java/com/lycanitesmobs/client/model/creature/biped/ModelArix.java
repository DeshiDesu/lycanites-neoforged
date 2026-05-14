package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelArix extends ModelTemplateBiped {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelArix() {
        this(1.0F);
    }

    public ModelArix(float shadowSize) {
    	// Load Model:
    	this.initModel("arix", LycanitesMobs.modInfo, "entity/arix");

        // Tropy:
        this.trophyScale = 1.8F;
        this.trophyOffset = new float[] {0.0F, -0.05F, -0.1F};
    }
}
