package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelRemobra extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelRemobra() {
        this(1.0F);
    }
    
    public ModelRemobra(float shadowSize) {
    	// Load Model:
    	this.initModel("Remobra", LycanitesMobs.modInfo, "entity/remobra");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("body", 0F, 0.8F, 0.0F);
    	setPartCenter("tail", 0F, 0.6F, -0.1F);
    	setPartCenter("leftwing", 0.1F, 0.8F, -0.1F);
    	setPartCenter("rightwing", -0.1F, 0.8F, -0.1F);
    	
    	// Trophy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.1F};
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
    	if(partName.equals("leftwing")) {
    		rotX = 20;
	        rotX -= Math.toDegrees(Mth.sin(loop * 0.4F) * 0.6F);
		    rotZ -= Math.toDegrees(Mth.sin(loop * 0.4F) * 0.6F);
    	}
    	if(partName.equals("rightwing")) {
    		rotX = 20;
	        rotX -= Math.toDegrees(Mth.sin(loop * 0.4F) * 0.6F);
	        rotZ -= Math.toDegrees(Mth.sin(loop * 0.4F + (float)Math.PI) * 0.6F);
    	}
    	if(partName.equals("tail")) {
	        rotX -= Math.toDegrees(Mth.sin(loop * 0.1F) * 0.6F);
    	}
		float bob = -Mth.sin(loop * 0.2F) * 0.3F;
		if(bob < 0) bob = -bob;
		posY += bob;
		
    	// Apply Animations:
    	doTranslate(posX, posY, posZ);
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    }
}
