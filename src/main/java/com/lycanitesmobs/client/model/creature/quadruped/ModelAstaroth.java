package com.lycanitesmobs.client.model.creature.quadruped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelAstaroth extends ModelTemplateQuadruped {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelAstaroth() {
        this(1.0F);
    }
    
    public ModelAstaroth(float shadowSize) {
    	// Load Model:
    	this.initModel("astaroth", LycanitesMobs.modInfo, "entity/astaroth");

		// Looking:
		this.lookHeadScaleX = 0F;
		this.lookHeadScaleY = 0F;
		this.lookNeckScaleX = 0F;
		this.lookNeckScaleY = 0F;

        // Trophy:
        this.trophyScale = 0.6F;
    }
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
    float maxLeg = 0F;
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

    	// Walking - Bobbing:
		if(partName.equals("body")) {
			float bob = Mth.cos(time * 0.6662F + (float) Math.PI) * 0.3F * distance;
			if (bob < 0) {
				bob += -bob * 2;
			}
			this.translate(0, bob, 0);
		}

		// Tail:
		if(partName.equals("tail01") || partName.equals("tail03")) {
			float rotX = (float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
			float rotY = (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
			this.rotate(rotX, rotY, 0);
		}
		else if(partName.equals("tail02")) {
			float rotX = (float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
			float rotY = (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
			this.rotate(-rotX, rotY, 0);
		}

		// Fingers:
		else if(partName.equals("fingerleft")) {
			this.rotate(
					0,
					(float)Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F - 0.2F),
					0);
		}
		else if(partName.equals("fingerright")) {
			this.rotate(
					0,
					(float)Math.toDegrees(Mth.cos((loop + 20) * 0.2F) * 0.2F - 0.2F),
					0);
		}
		else if(partName.equals("fingertop")) {
			this.rotate(
					(float)Math.toDegrees(Mth.sin(loop * 0.2F) * 0.2F - 0.2F),
					(float)Math.toDegrees(Mth.cos((loop + 40) * 0.2F) * 0.2F - 0.2F),
					0);
		}
    }
}
