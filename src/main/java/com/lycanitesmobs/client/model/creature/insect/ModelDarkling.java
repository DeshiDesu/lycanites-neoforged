package com.lycanitesmobs.client.model.creature.insect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;
import com.lycanitesmobs.core.entity.creature.insect.EntityDarkling;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelDarkling extends ModelTemplateInsect {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelDarkling() {
        this(1.0F);
    }

    public ModelDarkling(float shadowSize) {
    	// Load Model:
        this.initModel("darkling", LycanitesMobs.modInfo, "entity/darkling");

		// Scaling:
		this.lookHeadScaleX = 0;
		this.lookHeadScaleY = 0;
		this.mouthScaleX = 1F;
		this.mouthScaleY = 0.1F;

    	// Trophy:
        this.trophyScale = 1.0F;
    }
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Bobbing/Latching:
        if(partName.equals("body") && entity instanceof EntityDarkling) {
            if(((EntityDarkling)entity).hasLatchTarget()) {
                this.rotate(-90F, 0, 0);
            }
            else {
				float bob = Mth.cos(time * 0.6662F + (float) Math.PI) * 0.3F * distance;
				if (bob < 0)
					bob += -bob * 2;
				translate(0, bob, 0);
			}
        }
    }
}
