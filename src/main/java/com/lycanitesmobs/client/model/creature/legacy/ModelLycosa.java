package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelLycosa extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelLycosa() {
        this(1.0F);
    }
    
    public ModelLycosa(float shadowSize) {
    	// Load Model:
        this.initModel("Lycosa", LycanitesMobs.modInfo, "entity/lycosa");
    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 0.3F, 0.2F);
    	setPartCenter("innerleftmouth", 0.07F, 0.3F, 0.35F);
    	setPartCenter("innerrightmouth", -0.07F, 0.3F, 0.35F);
    	setPartCenter("outerleftmouth", 0.29F, 0.3F, 0.25F);
    	setPartCenter("outerrightmouth", -0.29F, 0.3F, 0.25F);
    	setPartCenter("body", 0F, 0.3F, 0.2F);
    	
    	setPartCenter("frontleftleg", 0.25F, 0.3F, 0.17F);
    	setPartCenter("middlefrontleftleg", 0.25F, 0.3F, 0.05F);
    	setPartCenter("middlebackleftleg", 0.25F, 0.3F, -0.07F);
    	setPartCenter("backleftleg", 0.25F, 0.3F, -0.19F);
    	
    	setPartCenter("frontrightleg", -0.25F, 0.3F, 0.17F);
    	setPartCenter("middlefrontrightleg", -0.25F, 0.3F, 0.05F);
    	setPartCenter("middlebackrightleg", -0.25F, 0.3F, -0.07F);
    	setPartCenter("backrightleg", -0.25F, 0.3F, -0.19F);
    	
    	this.lockHeadX = true;
    	this.lockHeadY = true;
    	
    	// Trophy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.35F};
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
    	if(partName.equals("innerleftmouth"))
    		doRotate((float)-Math.toDegrees(Mth.cos(loop * 0.09F + (float) Math.PI) * 0.05F - 0.05F), 0.0F, 0.0F);
    	if(partName.equals("innerrightmouth"))
    		doRotate((float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
    	if(partName.equals("outerleftmouth"))
    		rotY += (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F - 0.05F);
    	if(partName.equals("outerrightmouth"))
    		rotY -= (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F - 0.05F);
    	
    	// Walking:
    	float walkSwing = 0.6F;
    	if(partName.equals("frontrightleg") || partName.equals("middlebackrightleg") || partName.equals("middlefrontleftleg") || partName.equals("backleftleg")) {
    		rotX += Math.toDegrees(Mth.cos(time * 0.3331F + (float)Math.PI) * walkSwing * distance);
    		rotZ += Math.toDegrees(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance);
    	}
    	if(partName.equals("frontleftleg") || partName.equals("middlebackleftleg") || partName.equals("middlefrontrightleg") || partName.equals("backrightleg")) {
    		rotX += Math.toDegrees(Mth.cos(time * 0.3331F) * walkSwing * distance);
    		rotZ += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);
    	}
    	
		// Attack:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
	    	if(partName.equals("innerleftmouth") || partName.equals("innerrightmouth")) {
	    		rotX += 20.0F;
	    	}
	    	if(partName.equals("outerleftmouth"))
	    		rotY -= 15F;
	    	if(partName.equals("outerrightmouth"))
	    		rotY += 15F;
		}
		
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
