package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelGeist extends CreatureObjModelOld {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelGeist() {
        this(1.0F);
    }

    public ModelGeist(float shadowSize) {
    	// Load Model:
    	this.initModel("geist", LycanitesMobs.modInfo, "entity/geist");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 1.9F, 0.1F);
    	setPartCenter("body", 0F, 1.4F, 0F);
    	setPartCenter("armleft", 0.3F, 1.78F, -0.13F);
    	setPartCenter("armright", -0.3F, 1.78F, -0.13F);
    	setPartCenter("legleft", 0.2F, 0.9F, -0.2F);
    	setPartCenter("legright", -0.2F, 0.9F, -0.2F);
    	
    	// Trophy:
        this.trophyScale = 1.2F;
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
    	float walkSwing = 0.6F;
    	if(partName.equals("armleft"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 2.0F * distance * 0.5F);
    	if(partName.equals("armright"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float)Math.PI) * 2.0F * distance * 0.5F);
    	if(partName.equals("legleft"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float)Math.PI) * 1.4F * distance);
    	if(partName.equals("legright"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 1.4F * distance);
    	
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
