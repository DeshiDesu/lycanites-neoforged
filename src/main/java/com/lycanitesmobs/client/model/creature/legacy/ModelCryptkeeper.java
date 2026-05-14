package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelCryptkeeper extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelCryptkeeper() {
        this(1.0F);
    }
    
    public ModelCryptkeeper(float shadowSize) {
    	// Load Model:
    	this.initModel("CryptZombie", LycanitesMobs.modInfo, "entity/cryptkeeper");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 1.65F, 0.0F);
    	setPartCenter("body", 0F, 1.65F, 0.0F);
    	setPartCenter("leftarm", 0.4F, 1.6F, 0.0F);
    	setPartCenter("rightarm", -0.4F, 1.6F, 0.0F);
    	setPartCenter("leftleg", 0.1F, 0.85F, 0.0F);
    	setPartCenter("rightleg", -0.1F, 0.85F, 0.0F);
    	
    	// Trophy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, -0.3F, -0.1F};
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
    	
    	// Walking:
    	float walkSwing = 0.6F;
    	if(partName.equals("leftarm"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 2.0F * distance * 0.5F);
    	if(partName.equals("rightarm"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float)Math.PI) * 2.0F * distance * 0.5F);
    	if(partName.equals("leftleg"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float)Math.PI) * 1.4F * distance);
    	if(partName.equals("rightleg"))
    		rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 1.4F * distance);
    	
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
