package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBelphegor extends ModelTemplateBiped {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelBelphegor() {
        this(1.0F);
    }

    public ModelBelphegor(float shadowSize) {

		// Load Model:
		this.initModel("belphegor", LycanitesMobs.modInfo, "entity/belphegor");

		// Trophy:
		this.trophyScale = 1.2F;
		this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }
}
