package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelConba extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelConba() {
        this(1.0F);
    }
    
    public ModelConba(float shadowSize) {
    	// Load Model:
        this.initModel("Conba", LycanitesMobs.modInfo, "entity/conba");
    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 0.6F, 0.3F);
    	setPartCenter("mouth", 0F, 0.5F, 0.5F);
    	setPartCenter("body", 0F, 0.7F, 0F);
    	setPartCenter("armleft", 0.3F, 0.7F, 0F);
    	setPartCenter("armright", -0.3F, 0.7F, 0F);
    	setPartCenter("legleft", 0.2F, 0.5F, -0.5F);
    	setPartCenter("legright", -0.2F, 0.5F, -0.5F);

        // Tropy:
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
    	
    	// Looking (Mouth):
    	if(partName.equals("mouth")) {
    		this.centerPartToPart("mouth", "head");
    		if(!lockHeadX)
    			this.doRotate((float)Math.toDegrees(lookX / (180F / (float)Math.PI)), 0, 0);
    		if(!lockHeadY)
    			this.doRotate(0, (float)Math.toDegrees(lookY / (180F / (float)Math.PI)), 0);
    		this.uncenterPartToPart("mouth", "head");
    	}
    	
    	// Idle:
    	if(partName.equals("mouth")) {
    		this.doRotate((float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
    	}
    	
    	// Walking:
    	float walkSwing = 0.3F;
    	if(partName.equals("armright") || partName.equals("legleft"))
    		rotX += Math.toDegrees(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance);
    	if(partName.equals("armleft") || partName.equals("legright"))
    		rotX += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);
				
		// Attack:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
	    	if(partName.equals("armleft"))
	    		doRotate(-135.0F, 0.0F, 0.0F);
	    	if(partName.equals("armright"))
	    		doRotate(-135.0F, 0.0F, 0.0F);
		}
		
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
