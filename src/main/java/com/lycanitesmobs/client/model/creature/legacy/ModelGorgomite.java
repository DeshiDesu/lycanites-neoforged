package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelGorgomite extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelGorgomite() {
        this(1.0F);
    }
    
    public ModelGorgomite(float shadowSize) {
    	// Load Model:
    	this.initModel("Gorgomite", LycanitesMobs.modInfo, "entity/gorgomite");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 0.4F, 0.45F);
    	setPartCenter("leftmouth", 0.2F, 0.4F, 0.7F);
    	setPartCenter("rightmouth", -0.2F, 0.4F, 0.7F);
    	setPartCenter("body", 0F, 0.4F, 0.45F);
    	setPartCenter("frontleftleg", 0.35F, 0.5F, 0.2F);
    	setPartCenter("backleftleg", 0.35F, 0.5F, -0.3F);
    	setPartCenter("frontrightleg", -0.35F, 0.5F, 0.2F);
    	setPartCenter("backrightleg", -0.35F, 0.5F, -0.3F);
    	
    	this.lockHeadX = true;
    	this.lockHeadY = true;
    	
    	// Trophy:
        this.trophyScale = 1.2F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.2F};
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
    	
    	// Head Rotation:
    	if(partName.equals("leftmouth") || partName.equals("rightmouth")) {
            this.centerPartToPart(partName, "head");
    		if(!this.lockHeadX)
    			rotX += Math.toDegrees(lookX / (180F / (float)Math.PI));
    		if(!this.lockHeadY)
    			rotY += Math.toDegrees(lookY / (180F / (float)Math.PI));
            this.uncenterPartToPart(partName, "head");
    	}
    	
    	// Idle:
    	if(partName.equals("leftmouth"))
    		doRotate((float)-Math.toDegrees(Mth.cos(loop * 0.09F + (float)Math.PI) * 0.05F - 0.05F), 0.0F, 0.0F);
    	if(partName.equals("rightmouth"))
    		doRotate((float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
    	
    	// Walking:
    	float walkSwing = 0.6F;
    	if(partName.equals("frontrightleg") || partName.equals("backleftleg")) {
    		rotX += Math.toDegrees(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance);
    	}
    	if(partName.equals("frontleftleg") || partName.equals("backrightleg")) {
    		rotX += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);
    	}
		
		// Attack:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
	    	if(partName.equals("leftmouth") || partName.equals("rightmouth")) {
	    		rotX += 20.0F;
	    	}
		}
		
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
