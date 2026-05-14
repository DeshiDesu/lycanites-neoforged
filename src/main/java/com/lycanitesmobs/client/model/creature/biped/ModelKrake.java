package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelKrake extends ModelTemplateBiped {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelKrake() {
        this(1.0F);
    }

    public ModelKrake(float shadowSize) {
    	// Load Model:
    	this.initModel("krake", LycanitesMobs.modInfo, "entity/krake");

    	// Scaling:
		this.mouthScale = 2.5F;
		this.bigChildHead = false; // TODO Apply scaling animation first and then make transforms relative to that.

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, -0.2F, 0.0F};
    }
    
    
    // ==================================================
   	//                    Animate Part
   	// ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance * 0.5F, loop, lookY, lookX, scale);

		if(partName.contains("spike")) {
			this.rotate((float)-Math.toDegrees(Mth.sin(loop * 0.2F) * 0.1F), 0, 0);
		}
    }
}
