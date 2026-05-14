package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelNaxiris extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelNaxiris() {
        this(1.0F);
    }
    
    public ModelNaxiris(float shadowSize) {
    	// Load Model:
    	this.initModel("Naxiris", LycanitesMobs.modInfo, "entity/naxiris");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 2.0F, 0F);
    	setPartCenter("mouth", 0F, 1F, -0.5F);
    	setPartCenter("eye", 0F, 2.6F, 1.3F);

    	setPartCenter("tentacleleft01", 1.6F, 2.7F, -0.3F);
    	setPartCenter("tentacleleft02", 0.6F, 3.6F, -0.4F);
    	setPartCenter("tentacleleft03", 1.2F, 3.1F, -0.5F);
    	setPartCenter("tentacleleft04", 1.4F, 2.6F, -0.6F);
    	setPartCenter("tentacleleft05", 0.9F, 3.0F, -0.9F);

    	setPartCenter("tentacleright01", -1.6F, 2.7F, -0.3F);
    	setPartCenter("tentacleright02", -0.6F, 3.6F, -0.4F);
    	setPartCenter("tentacleright03", -1.2F, 3.1F, -0.5F);
    	setPartCenter("tentacleright04", -1.4F, 2.6F, -0.6F);
    	setPartCenter("tentacleright05", -0.9F, 3.0F, -0.9F);
    	
    	this.lockHeadX = true;
    	this.lockHeadY = true;

        // Trophy:
        this.trophyScale = 0.2F;
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
    	
		centerPartToPart(partName, "head");
    	rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.015F + 0.015F);
		uncenterPartToPart(partName, "head");
    	
    	// Look:
    	if(partName.equals("eye")) {
	    	rotX += Math.toDegrees(lookX / (180F / (float)Math.PI));
	    	rotY += Math.toDegrees(lookY / (180F / (float)Math.PI));
    	}
    	
    	// Idle:
    	if(partName.equals("mouth")) {
    		rotX = Mth.cos(loop * 0.09F) * 0.05F - 0.05F;
    	}
		float bob = -Mth.sin(loop * 0.05F) * 0.3F;
		posY += bob;
    	
    	float animationScaleZ = 0.09F;
    	float animationScaleY = 0.07F;
    	float animationScaleX = 0.05F;
    	float animationDistanceZ = 0.25F;
    	float animationDistanceY = 0.2F;
    	float animationDistanceX = 0.15F;
    	if(partName.equals("tentacleleft01")) {
	        rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	if(partName.equals("tentacleleft02")) {
	        rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX += Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	if(partName.equals("tentacleleft03")) {
	        rotZ += Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	if(partName.equals("tentacleleft04")) {
	        rotZ += Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY -= Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX += Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	if(partName.equals("tentacleleft05")) {
	        rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY -= Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	
    	if(partName.equals("tentacleright01")) {
	        rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX += Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	if(partName.equals("tentacleright02")) {
	        rotZ += Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX += Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	if(partName.equals("tentacleright03")) {
	        rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	if(partName.equals("tentacleright04")) {
	        rotZ += Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY -= Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	if(partName.equals("tentacleright05")) {
	        rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
	        rotY -= Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
	        rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
    	}
    	
		// Attack:
    	if((entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown())) {
			if(partName.equals("mouth"))
				rotX += 20F;
			if(partName.contains("tentacleleft"))
				rotZ -= 25F;
			if(partName.contains("tentacleright"))
				rotZ += 25F;
		}
		
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
