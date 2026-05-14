package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelRahovart extends CreatureObjModelOld {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelRahovart() {
        this(1.0F);
    }

    public ModelRahovart(float shadowSize) {
    	// Load Model:
    	this.initModel("rahovart", LycanitesMobs.modInfo, "entity/rahovart");

    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 23.275F, 0F);
        setPartCenter("mouth", 0F, 23.075F, -2.5F);
    	setPartCenter("body", 0F, 11.025F, 0F);
    	setPartCenter("armleft", 5.6F, 19.775F, 0F);
    	setPartCenter("armright", -5.6F, 19.775F, 0F);
    	setPartCenter("legleft", 2.45F, 11.2F, 0F);
    	setPartCenter("legright", -2.45F, 11.2F, 0F);
        setPartCenter("tail", 0F, 18.9F, 4.725F);
    	
    	lockHeadX = false;
    	lockHeadY = false;
    	
    	// Trophy:
        this.trophyScale = 0.1F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
    float maxLeg = 0F;
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
    	float pi = (float)Math.PI;
    	float posX = 0F;
    	float posY = 0F;
    	float posZ = 0F;
    	float angleX = 0F;
    	float angleY = 0F;
    	float angleZ = 0F;
    	float rotation = 0F;
    	float rotX = 0F;
    	float rotY = 0F;
    	float rotZ = 0F;

        // Looking:
        if(partName.equals("mouth")) {
            this.centerPartToPart("mouth", "head");
            if(!this.lockHeadX)
                this.doRotate((float)Math.toDegrees(lookX / (180F / (float) Math.PI)), 0, 0);
            if(!this.lockHeadY)
                this.doRotate(0, (float)Math.toDegrees(lookY / (180F / (float)Math.PI)), 0);
            this.uncenterPartToPart("mouth", "head");
        }
    	
    	// Idle:
        if (partName.equals("mouth")) {
            rotX += (Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
        }
    	if(partName.equals("armleft")) {
	        rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
    	}
    	if(partName.equals("armright")) {
	        rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
    	}
        if(partName.equals("tail")) {
            rotX = (float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
            rotY = (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
        }
    	
    	// Walking:
    	float walkSwing = 0.05F;
    	if(partName.equals("armleft"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing) * distance * 0.5F);
    	if(partName.equals("armright"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float)Math.PI) * distance * 0.5F);
    	if(partName.equals("legleft"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float)Math.PI) * distance);
    	if(partName.equals("legright"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing) * distance);
				
		// Attack:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
	    	if(partName.equals("armleft"))
	    		doRotate(-40.0F, 0.0F, 0.0F);
		}
		
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
