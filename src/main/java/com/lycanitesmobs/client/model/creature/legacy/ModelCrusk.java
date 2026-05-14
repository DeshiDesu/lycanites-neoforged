package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelCrusk extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelCrusk() {
        this(1.0F);
    }
    
    public ModelCrusk(float shadowSize) {
    	// Load Model:
    	this.initModel("Crusk", LycanitesMobs.modInfo, "entity/crusk");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 0.7F, 2.4F);
    	setPartCenter("topleftmouth", 0F, 0.7F, 2.4F);
    	setPartCenter("toprightmouth", 0F, 0.7F, 2.4F);
    	setPartCenter("bottomleftmouth", 0F, 0.7F, 2.4F);
    	setPartCenter("bottomrightmouth", 0F, 0.7F, 2.4F);
    	setPartCenter("body", 0F, 0.7F, 1.8F);
    	setPartCenter("body01", 0F, 0.7F, 1.2F);
    	setPartCenter("body02", 0F, 0.7F, 0.6F);
    	setPartCenter("body03", 0F, 0.7F, 0.0F);
    	setPartCenter("body04", 0F, 0.7F, -0.6F);
    	setPartCenter("body05", 0F, 0.7F, -1.2F);
    	setPartCenter("body06", 0F, 0.7F, -1.8F);
    	setPartCenter("body07", 0F, 0.7F, -2.4F);
    	setPartCenter("body08", 0F, 0.7F, -3.0F);
    	setPartCenter("body09", 0F, 0.7F, -3.6F);
    	
    	this.lockHeadX = true;
    	this.lockHeadY = true;
    	
    	// Trophy:
        this.trophyScale = 0.6F;
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
    	
    	// No Crusk trophy animation due to head offset.
    	if(scale < 0)
    		return;
    	
    	// Mouth (Idle, Attack, Sitting):
    	if(partName.equals("topleftmouth")) {
    		doTranslate(0.3F, 0.3F, 0.7F);
    		angleX = -0.5F;
    		angleY = 0.5F;
    	}
    	if(partName.equals("toprightmouth")) {
    		doTranslate(-0.3F, 0.3F, 0.7F);
    		angleX = -0.5F;
    		angleY = -0.5F;
    	}
    	if(partName.equals("bottomleftmouth")) {
    		doTranslate(0.3F, -0.3F, 0.7F);
    		angleX = 0.5F;
    		angleY = 0.5F;
    	}
    	if(partName.equals("bottomrightmouth")) {
    		doTranslate(-0.3F, -0.3F, 0.7F);
    		angleX = 0.5F;
    		angleY = -0.5F;
    	}
    	if(partName.equals("topleftmouth") || partName.equals("toprightmouth") || partName.equals("bottomleftmouth") || partName.equals("bottomrightmouth")) {
    		rotation += -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
    		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown())
    			rotation = -20;
    		if(entity instanceof TameableCreatureEntity && ((TameableCreatureEntity)entity).isSitting())
    			rotation += 20;
        	doAngle(rotation, angleX, angleY, angleZ);
        	rotation = 0F;
    	}
    	if(partName.equals("topleftmouth"))
    		doTranslate(-0.3F, -0.3F, -0.7F);
    	if(partName.equals("toprightmouth"))
    		doTranslate(0.3F, -0.3F, -0.7F);
    	if(partName.equals("bottomleftmouth"))
    		doTranslate(-0.3F, 0.3F, -0.7F);
    	if(partName.equals("bottomrightmouth"))
    		doTranslate(0.3F, 0.3F, -0.7F);
    	
    	// Walking:
    	float walkSwing = 0.8F;
    	if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).getStealth() > 0 && ((BaseCreatureEntity)entity).getStealth() < 1)
    		time = loop;
    	time /= 2;
    	if(partName.equals("head") || partName.equals("topleftmouth") || partName.equals("toprightmouth") || partName.equals("bottomleftmouth") || partName.equals("bottomrightmouth")) {
    		posX += Mth.sin((time - walkSwing) * walkSwing) * walkSwing;
    	}
    	if(partName.equals("body")) {
    		posX += Mth.sin(time * walkSwing) * walkSwing;
    		float parentX = Mth.sin((time - walkSwing) * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	if(partName.equals("body01")) {
    		posX += Mth.sin((time + walkSwing) * walkSwing) * walkSwing;
    		float parentX = Mth.sin(time * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	if(partName.equals("body02")) {
    		posX += Mth.sin((time + (walkSwing * 2)) * walkSwing) * walkSwing;
    		float parentX = Mth.sin((time + walkSwing) * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	if(partName.equals("body03")) {
    		posX += Mth.sin((time + (walkSwing * 3)) * walkSwing) * walkSwing;
    		float parentX = Mth.sin((time + (walkSwing * 2)) * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	if(partName.equals("body04")) {
    		posX += Mth.sin((time + (walkSwing * 4)) * walkSwing) * walkSwing;
    		float parentX = Mth.sin((time + (walkSwing * 3)) * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	if(partName.equals("body05")) {
    		posX += Mth.sin((time + (walkSwing * 5)) * walkSwing) * walkSwing;
    		float parentX = Mth.sin((time + (walkSwing * 4)) * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	if(partName.equals("body06")) {
    		posX += Mth.sin((time + (walkSwing * 6)) * walkSwing) * walkSwing;
    		float parentX = Mth.sin((time + (walkSwing * 5)) * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	if(partName.equals("body07")) {
    		posX += Mth.sin((time + (walkSwing * 7)) * walkSwing) * walkSwing;
    		float parentX = Mth.sin((time + (walkSwing * 6)) * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	if(partName.equals("body08")) {
    		posX += Mth.sin((time + (walkSwing * 8)) * walkSwing) * walkSwing;
    		float parentX = Mth.sin((time + (walkSwing * 7)) * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	if(partName.equals("body09")) {
    		posX += Mth.sin((time + (walkSwing * 9)) * walkSwing) * walkSwing;
    		float parentX = Mth.sin((time + (walkSwing * 8)) * walkSwing) * walkSwing;
    		rotY += rotateToPoint(0, posX, -0.6F, parentX);
    	}
    	
    	// Stealth:
    	if(entity instanceof BaseCreatureEntity)
    		posY -= (2 * ((BaseCreatureEntity)entity).getStealth());
    	
    	// Apply Animations:
    	doTranslate(posX, posY, posZ);
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doScale(scaleX, scaleY, scaleZ);
    }
}
