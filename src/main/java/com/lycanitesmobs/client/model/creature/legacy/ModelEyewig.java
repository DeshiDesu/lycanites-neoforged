package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelEyewig extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelEyewig() {
        this(1.0F);
    }
    
    public ModelEyewig(float shadowSize) {
    	// Load Model:
    	this.initModel("Eyewig", LycanitesMobs.modInfo, "entity/eyewig");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 0.5F, 1.0F);
    	setPartCenter("leftmouth", 0.25F, 0.55F, 0.55F);
    	setPartCenter("rightmouth", -0.25F, 0.55F, 0.55F);
    	setPartCenter("body", 0F, 0.5F, 0F);
    	setPartCenter("frontleftleg", 0.35F, 0.55F, 0.3F);
    	setPartCenter("middleleftleg", 0.35F, 0.55F, 0F);
    	setPartCenter("backleftleg", 0.35F, 0.55F, -0.3F);
    	setPartCenter("frontrightleg", -0.35F, 0.55F, 0.3F);
    	setPartCenter("middlerightleg", -0.35F, 0.55F, 0F);
    	setPartCenter("backrightleg", -0.35F, 0.55F, -0.3F);
    	
    	// Trophy:
        this.trophyScale = 0.8F;
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
    	if(partName.equals("leftmouth"))
    		rotY += (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F - 0.05F);
    	if(partName.equals("rightmouth"))
    		rotY -= (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F - 0.05F);
		
    	// Leg Angles:
    	if(partName.equals("frontleftleg") || partName.equals("middleleftleg") || partName.equals("backleftleg")
    			|| partName.equals("frontrightleg") || partName.equals("middlerightleg") || partName.equals("backrightleg"))
    		angleZ = 1F;
    	if(partName.equals("frontleftleg")) angleY = 15F / 360F;
    	if(partName.equals("backleftleg")) angleY = -15F / 360F;
    	if(partName.equals("frontrightleg")) angleY = -15F / 360F;
    	if(partName.equals("backrightleg")) angleY = 15F / 360F;
    	
    	// Walking:
    	float walkSwing = 0.3F;
    	if(partName.equals("frontrightleg") || partName.equals("middleleftleg") || partName.equals("backrightleg"))
    		rotation += Math.toDegrees(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance);
    	if(partName.equals("frontleftleg") || partName.equals("middlerightleg") || partName.equals("backleftleg"))
    		rotation += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);
		float bob = Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance;
		if(bob < 0) bob += -bob * 2;
		//posY += bob;
		
		// Attack:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
	    	if(partName.equals("leftmouth"))
	    		rotY -= 15F;
	    	if(partName.equals("rightmouth"))
	    		rotY += 15F;
		}
		
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
    
    
    // ==================================================
   	//              Rotate and Translate
   	// ==================================================
    @Override
    public void childScale(String partName) {
    	super.childScale(partName);
    	if(partName.equals("head") || partName.equals("mouth")) {
    		doScale(2F, 2F, 2F);
    		doTranslate(0F, -(getPartCenter(partName)[1] / 2), 0F);
    	}
    }
}
