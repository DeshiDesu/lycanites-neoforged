package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelAspid extends ModelTemplateBiped {
    public ModelAspid() {
        this(1.0F);
    }
    
    public ModelAspid(float shadowSize) {
    	this.initModel("aspid", LycanitesMobs.modInfo, "entity/aspid");

		this.legScaleX = 0.25F;
		this.tailScaleX = 2F;
		this.tailScaleY = 0.5F;

    	// Trophy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.2F};
    }
}
