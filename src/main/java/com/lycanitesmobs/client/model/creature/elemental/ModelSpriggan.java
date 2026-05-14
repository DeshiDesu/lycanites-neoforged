package com.lycanitesmobs.client.model.creature.elemental;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelSpriggan extends ModelTemplateElemental {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelSpriggan() {
        this(1.0F);
    }

    public ModelSpriggan(float shadowSize) {
    	// Load Model:
    	this.initModel("spriggan", LycanitesMobs.modInfo, "entity/spriggan");
    	
    	// Trophy:
        this.trophyScale = 1.2F;
    }
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		float rotX = 0F;
		float rotY = 0F;
		float rotZ = 0F;

		// Effects:
		if(partName.equals("effectouter"))
			rotX = 20F;
		if(partName.equals("effectinner"))
			rotX = -20F;

		// Apply Animations:
		this.rotate(rotX, rotY, rotZ);

    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
    }
}
