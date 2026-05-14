package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelNingen extends CreatureObjModelOld {
    	
    	// Animation:
    	boolean attackAlt = false;
    	boolean attacking = false;
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelNingen() {
        this(1.0F);
    }
    
    public ModelNingen(float shadowSize) {
    	// Load Model:
    	this.initModel("Ningen", LycanitesMobs.modInfo, "entity/ningen");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 1.5F, 0.2F);
    	setPartCenter("mouth", 0F, 1.5F, 0.2F);
    	setPartCenter("body", 0F, 1.5F, 0.2F);
    	setPartCenter("leftarm", 0.4F, 1.5F, 0.0F);
    	setPartCenter("rightarm", -0.4F, 1.5F, 0.0F);
    	setPartCenter("tail01", 0F, 1.0F, 0.0F);
    	setPartCenter("tail02", 0F, 0.45F, -0.1F);
    	setPartCenter("tail03", 0F, 0.11F, -0.3F);
    	setPartCenter("tail04", 0F, 0.1F, -0.85F);
    	setPartCenter("tail05", 0F, 0.09F, -1.3F);
    	setPartCenter("tail06", 0F, 0.08F, -1.65F);
    	
    	// Trophy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
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
    	float scaleX = 1F;
    	float scaleY = 1F;
    	float scaleZ = 1F;
    	
    	// Mouth (Follow Head):
    	if(partName.equals("mouth")) {
    		if(!lockHeadX)
    			rotX += Math.toDegrees(lookX / (180F / (float)Math.PI));
    		if(!lockHeadY)
    			rotY += Math.toDegrees(lookY / (180F / (float)Math.PI));
    	}
    	
    	// Mouth (Idle and Attack):
    	if(partName.equals("mouth")) {
    		doTranslate(0.0F, -0.2F, 0.0F);
    		angleX = -1.0F;
    	}
    	if(partName.equals("mouth")) {
    		rotation += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
    		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown())
    			rotation = 20;
        	doAngle(rotation, angleX, angleY, angleZ);
        	rotation = 0F;
    	}
    	if(partName.equals("mouth"))
    		doTranslate(-0.0F, 0.2F, 0.0F);
    	
    	// Arms (Idle and Attack):
    	if(partName.equals("leftarm")) {
	        rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
    		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
    			if(attackAlt)
    				rotY = 40;
    			else
    				rotY = -25;
    		}
    	}
    	if(partName.equals("rightarm")) {
	        rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
    		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
    			if(attackAlt)
    				rotY = 40;
    			else
    				rotY = -25;
    		}
    	}
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown() && !attacking) {
			attackAlt = !attackAlt;
			attacking = true;
		}
		if(entity instanceof BaseCreatureEntity && !((BaseCreatureEntity)entity).isAttackOnCooldown())
			attacking = false;
    	
    	// Arms (Match Body):
    	float walkSwing = 0.1F;
    	float walkOffset = 0.1F;
    	time *= 4;
    	if(partName.equals("leftarm"))
    		doTranslate(-0.4F, 0.0F, 0.2F);
    	if(partName.equals("rightarm"))
    		doTranslate(0.4F, 0.0F, 0.2F);
    	if(partName.equals("leftarm") || partName.equals("rightarm"))
    		doRotate(0.0F, 0.0F, (float)rotateToPoint(0, 0, -0.5F, Mth.sin(time * walkSwing) * walkSwing));
    	if(partName.equals("leftarm"))
    		doTranslate(0.4F, 0.0F, -0.2F);
    	if(partName.equals("rightarm"))
    		doTranslate(-0.4F, 0.0F, -0.2F);
    	
    	// Body and Tail (Walking):
    	if(partName.equals("body")) {
    		rotZ += rotateToPoint(0, 0, -0.5F, Mth.sin(time * walkSwing) * walkSwing);
    	}
    	if(partName.equals("tail01") || partName.equals("tail02") || partName.equals("tail03") || partName.equals("tail04") || partName.equals("tail05") || partName.equals("tail06")) {
    		double[] tailAnimation = new double[] {0.0D, 0.0D, 0.0D, 0.0D};
    		if(partName.equals("tail01"))
    			tailAnimation = getTailRotation(partName, "body", 1, walkSwing, walkOffset, time);
    		if(partName.equals("tail02"))
    			tailAnimation = getTailRotation(partName, "tail01", 2, walkSwing, walkOffset, time);
    		if(partName.equals("tail03"))
    			tailAnimation = getTailRotation(partName, "tail02", 3, walkSwing, walkOffset, time);
    		if(partName.equals("tail04"))
    			tailAnimation = getTailRotation(partName, "tail03", 4, walkSwing, walkOffset, time);
    		if(partName.equals("tail05"))
    			tailAnimation = getTailRotation(partName, "tail04", 5, walkSwing, walkOffset, time);
    		if(partName.equals("tail06"))
    			tailAnimation = getTailRotation(partName, "tail05", 6, walkSwing, walkOffset, time);
    		posX += tailAnimation[0];
			rotY += tailAnimation[2];
			if(partName.equals("tail01") || partName.equals("tail02") || partName.equals("tail03"))
				rotZ += tailAnimation[3];
    	}
    		
    	
    	// Apply Animations:
    	doTranslate(posX, posY, posZ);
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doScale(scaleX, scaleY, scaleZ);
    }
    
    public double[] getTailRotation(String tailName, String targetName, int tailID, float walkSwing, float walkOffset, float time) {
    	double[] animation = new double[4]; // xPos, xRot, yRot, zRot
    	animation[0] = Mth.sin((time + (walkOffset * tailID)) * walkSwing * tailID) * walkSwing;
    	float parentX = Mth.sin((time + (walkOffset * (tailID - 1))) * walkSwing * tailID) * walkSwing;
    	float[] partDifference = comparePartCenters(tailName, targetName);
    	if(tailID == 1)
    		partDifference[1] += 0.5F;
    	double[] rotations = rotateToPoint((float)animation[0], 0.0F, 0.0F, parentX - partDifference[0], -partDifference[1], partDifference[2]);
    	animation[1] = rotations[0];
    	animation[2] = rotations[1];
    	animation[3] = rotations[2];
    	return animation;
    }
}
