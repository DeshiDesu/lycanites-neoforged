package com.lycanitesmobs.client.model.creature.biped;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelJabberwock extends ModelTemplateBiped {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelJabberwock() {
        this(1.0F);
    }
    
    public ModelJabberwock(float shadowSize) {
    	// Load Model:
    	this.initModel("jabberwock", LycanitesMobs.modInfo, "entity/jabberwock");
    	
    	// Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }
}
