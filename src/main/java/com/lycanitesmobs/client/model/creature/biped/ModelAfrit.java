package com.lycanitesmobs.client.model.creature.biped;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelAfrit extends ModelTemplateBiped {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelAfrit() {
        this(1.0F);
    }

    public ModelAfrit(float shadowSize) {
        // Load Model:
        this.initModel("afrit", LycanitesMobs.modInfo, "entity/afrit");

        // Trophy:
        this.trophyScale = 1.8F;
    }
}
