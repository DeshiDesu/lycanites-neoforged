package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;


public class ModelWildkin extends ModelTemplateBiped {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelWildkin() {
        this(1.0F);
    }

    public ModelWildkin(float shadowSize) {
        // Load Model:
        this.initModel("wildkin", LycanitesMobs.modInfo, "entity/wildkin");

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
    }
}
