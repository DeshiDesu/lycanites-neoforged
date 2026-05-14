package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelGeken extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelGeken() {
        this(1.0F);
    }
    
    public ModelGeken(float shadowSize) {
    	// Load Model:
        this.initModel("Geken", LycanitesMobs.modInfo, "entity/geken");
    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 1.7F, 0.1F);
    	setPartCenter("body", 0F, 1.7F, 0.1F);
    	setPartCenter("leftarm", 0.25F, 1.5F, 0F);
    	setPartCenter("rightarm", -0.25F, 1.5F, 0F);
    	setPartCenter("leftleg", 0.2F, 0.9F, -0.05F);
    	setPartCenter("rightleg", -0.2F, 0.9F, -0.05F);
    	setPartCenter("tail", 0F, 0.8F, -0.3F);
    	
    	// Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, -0.3F, -0.4F};
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
    	if(partName.equals("leftarm")) {
	        rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
    	}
    	if(partName.equals("rightarm")) {
	        rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
    	}
    	if(partName.equals("tail")) {
    		rotX = (float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
    		rotY = (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
    	}
    	
    	// Walking:
    	if(entity == null || entity.onGround() || entity.isInWater()) {
	    	float walkSwing = 0.6F;
	    	if(partName.equals("leftarm")) {
	    		rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 1.0F * distance * 0.5F);
				rotZ -= Math.toDegrees(Mth.cos(time * walkSwing) * 0.5F * distance * 0.5F);
	    	}
	    	if(partName.equals("rightarm")) {
	    		rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float)Math.PI) * 1.0F * distance * 0.5F);
				rotZ += Math.toDegrees(Mth.cos(time * walkSwing + (float)Math.PI) * 0.5F * distance * 0.5F);
	    	}
	    	if(partName.equals("leftleg"))
	    		rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float)Math.PI) * 1.4F * distance);
	    	if(partName.equals("rightleg"))
	    		rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 1.4F * distance);
    	}
		
		// Attack:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
	    	if(((BaseCreatureEntity)entity).getAttackPhase() % 1 == 0 && partName.equals("leftarm"))
	    		rotX += 20.0F;
	    	else if(((BaseCreatureEntity)entity).getAttackPhase() % 2 == 0 && partName.equals("rightarm"))
	    		rotX += 20.0F;
		}
		
		// Jump:
		if(entity != null && !entity.onGround() && !entity.isInWater()) {
	    	if(partName.equals("leftarm")) {
		        rotZ -= 10;
		        rotX -= 50;
	    	}
	    	if(partName.equals("rightarm")) {
		        rotZ += 10;
		        rotX -= 50;
	    	}
	    	if(partName.equals("leftleg"))
	    		rotX += 50;
	    	if(partName.equals("rightleg"))
	    		rotX += 50;
		}
    	
    	// Apply Animations:
		this.doAngle(rotation, angleX, angleY, angleZ);
    	this.doRotate(rotX, rotY, rotZ);
    	this.doTranslate(posX, posY, posZ);
    }
}
