package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelEnt extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelEnt() {
        this(1.0F);
    }
    
    public ModelEnt(float shadowSize) {
    	// Load Model:
    	this.initModel("Ent", LycanitesMobs.modInfo, "entity/ent");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 1.2F, 0.3F);
    	setPartCenter("body", 0F, 1.2F, 0.3F);
    	setPartCenter("leftarm", 0.3F, 1.1F, 0F);
    	setPartCenter("rightarm", -0.3F, 1.1F, 0F);
    	
    	setPartCenter("frontmiddleleg", 0F, 0.3F, 0.3F);
    	setPartCenter("frontleftleg", 0.3F, 0.3F, 0.15F);
    	setPartCenter("frontrightleg", -0.3F, 0.3F, 0.15F);
    	
    	setPartCenter("backmiddleleg", 0F, 0.3F, -0.3F);
    	setPartCenter("backleftleg", 0.3F, 0.3F, -0.15F);
    	setPartCenter("backrightleg", -0.3F, 0.3F, -0.15F);
    	
    	lockHeadX = true;
    	lockHeadY = true;
    	
    	// Trophy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.3F};
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
		
    	// Leg Angles:
    	if(partName.equals("frontleftleg") || partName.equals("backleftleg")
    			|| partName.equals("frontrightleg") || partName.equals("backrightleg")
    			|| partName.equals("frontmiddleleg") || partName.equals("backmiddleleg"))
    		angleY = 1F;
    	/*if(partName.equals("frontmiddleleg")) angleY = 90F / 360F;
    	if(partName.equals("frontleftleg")) angleY = 35F / 360F;
    	if(partName.equals("frontrightleg")) angleY = -35F / 360F;
    	if(partName.equals("backmiddleleg")) angleY = -90F / 360F;
    	if(partName.equals("backleftleg")) angleY = -35F / 360F;
    	if(partName.equals("backrightleg")) angleY = 35F / 360F;*/
    	
    	// Walking:
    	float walkSwing = 0.6F;
    	if(partName.equals("frontrightleg") || partName.equals("frontleftleg") || partName.equals("backleftleg") || partName.equals("backrightleg"))
    		rotation += Math.toDegrees(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance);
    	if(partName.equals("frontmiddleleg") || partName.equals("backmiddleleg"))
    		rotation += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);
				
		// Attack:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
	    	if(partName.equals("leftarm"))
	    		doRotate(0.0F, -25.0F, 0.0F);
	    	if(partName.equals("rightarm"))
	    		doRotate(0.0F, 25.0F, 0.0F);
		}
		
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
