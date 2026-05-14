package com.lycanitesmobs.client.model.creature.quadruped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelMakaAlpha extends ModelTemplateQuadruped {

    public ModelMakaAlpha() {
        this(1.0F);
    }
    
    public ModelMakaAlpha(float shadowSize) {
    	this.initModel("MakaAlpha", LycanitesMobs.modInfo, "entity/makaalpha");

        this.trophyScale = 0.6F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }
}
