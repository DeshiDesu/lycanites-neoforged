package com.lycanitesmobs.client.model.creature.quadruped;


import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelAstarothVoid extends ModelAstaroth {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelAstarothVoid() {
        this(1.0F);
    }

    public ModelAstarothVoid(float shadowSize) {
    	// Load Model:
    	this.initModel("astaroth_void", LycanitesMobs.modInfo, "entity/astaroth_void");

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
    boolean tentacleFlip = false;
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		if(partName.contains("tentacle")) {
			float loopOffset = 0;
			if(partName.contains("front")) {
				loopOffset += 10;
			}
			else if(partName.contains("middle")) {
				loopOffset += 20;
			}
			else if(partName.contains("back")) {
				loopOffset += 30;
			}

			if(partName.contains("left")) {
				this.rotate(
						(float) Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F),
						(float) Math.toDegrees(Mth.sin((loop + (loopOffset / 2)) * 0.2F) * 0.25F) - 10,
						(float) -Math.toDegrees(Mth.cos((loop + (loopOffset / 2)) * 0.09F) * 0.1F)
				);
			}
			else {
				this.rotate(
						(float) Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F),
						(float) -Math.toDegrees(Mth.sin((loop + (loopOffset / 2)) * 0.2F) * 0.25F) - 10,
						(float) -Math.toDegrees(Mth.cos((loop + (loopOffset / 2)) * 0.09F) * 0.1F)
				);
			}
		}
    }
}
