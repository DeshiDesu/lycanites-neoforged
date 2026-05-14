package com.lycanitesmobs.client.model.creature.generic;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelStryder extends CreatureObjModel {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelStryder() {
        this(1.0F);
    }

    public ModelStryder(float shadowSize) {
    	// Load Model:
    	this.initModel("stryder", LycanitesMobs.modInfo, "entity/stryder");

		// Scaling:
		this.lookHeadScaleX = 0;
		this.lookHeadScaleY = 0;

        // Trophy:
        this.trophyScale = 0.4F;
        this.trophyOffset = new float[] {0.0F, -0.2F, 0.0F};
        this.bodyIsTrophy = true;
    }
    
    
    // ==================================================
   	//                    Animate Part
   	// ==================================================
    float maxLeg = 0F;
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
    	float posX = 0F;
    	float posY = 0F;
    	float posZ = 0F;
    	float rotX = 0F;
    	float rotY = 0F;
    	float rotZ = 0F;
    	
    	// Idle:
        if(partName.equals("armleft")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(partName.equals("armright")) {
            rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
		
    	// Walking:
    	float walkSwing = 0.15F;
    	if(partName.equals("legleft"))
    		rotX += Math.toDegrees(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * (distance / 2));
    	if(partName.equals("legright"))
    		rotX += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * (distance / 2));
        if(partName.equals("legback"))
            rotX += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);

        // Attack:
        if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
            if(partName.equals("armleft"))
                rotate(-25.0F, 0.0F, 0.0F);
            if(partName.equals("armright"))
                rotate(-25.0F, 0.0F, 0.0F);
        }

        // Pickup:
        if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPickupEntity()) {
            if (partName.equals("armleft") || partName.equals("armright")) {
                rotX += 20D;
            }
        }
    	
    	// Apply Animations:
    	this.rotate(rotX, rotY, rotZ);
    	this.translate(posX, posY, posZ);
    }
}
