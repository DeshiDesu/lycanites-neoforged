package com.lycanitesmobs.client.model.creature.legacy;


import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelSalamander extends CreatureObjModelOld {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelSalamander() {
        this(1.0F);
    }

    public ModelSalamander(float shadowSize) {
    	// Load Model:
    	this.initModel("salamander", LycanitesMobs.modInfo, "entity/salamander");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 0.497F, 0.9F);

    	setPartCenter("body", 0F, 0.53F, 0F);

    	setPartCenter("legleftfront", 0.5F, 0.6F, 0.4F);
    	setPartCenter("legrightfront", -0.5F, 0.6F, 0.4F);
        setPartCenter("legleftback", 0.4F, 0.6F, -1.1F);
        setPartCenter("legrightback", -0.4F, 0.6F, -1.1F);
    	
    	setPartCenter("tail", 0F, 0.6F, -1.6F);

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, -0.2F, 0.0F};
    }

	@Override
	public int getBrightness(String partName, LayerCreatureBase layer, BaseCreatureEntity entity, int brightness) {
		return ClientManager.FULL_BRIGHT;
	}

	@Override
	public boolean getGlow(BaseCreatureEntity entity, LayerCreatureBase layer) {
		if(layer != null) {
			return super.getGlow(entity, layer);
		}
		return true;
	}
    
    
    // ==================================================
   	//                    Animate Part
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
        if(partName.equals("mouth")) {
            this.doRotate((float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F - 0.1F), 0.0F, 0.0F);
        }
    	if(partName.equals("tail")) {
    		rotX = (float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
    		rotY = (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
    	}
		
    	// Walking:
    	float walkSwing = 0.3F;
    	if(partName.equals("legrightfront") || partName.equals("legleftback"))
    		rotY += Math.toDegrees(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance);
    	if(partName.equals("legleftfront") || partName.equals("legrightback"))
    		rotY += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);

        // Jump:
        if(entity != null && !entity.onGround() && !entity.isInWater()) {
            if(partName.equals("legleftback") || partName.equals("legrightback"))
                rotX += 25;
            if(partName.equals("legleftfront") || partName.equals("legrightfront"))
                rotX -= 25;
        }

        // Attack:
        if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
            if(((BaseCreatureEntity)entity).getAttackPhase() % 1 == 0 && partName.equals("legleftfront"))
                rotX -= 20.0F;
            else if(((BaseCreatureEntity)entity).getAttackPhase() % 2 == 0 && partName.equals("legrightfront"))
                rotX -= 20.0F;
        }
    	
    	// Apply Animations:
		this.doAngle(rotation, angleX, angleY, angleZ);
    	this.doRotate(rotX, rotY, rotZ);
    	this.doTranslate(posX, posY, posZ);
    }


    // ==================================================
    //              Rotate and Translate
    // ==================================================
    @Override
    public void childScale(String partName) {
        if(partName.equals("head") || partName.equals("mouth"))
            doTranslate(-(getPartCenter(partName)[0] / 2), -(getPartCenter(partName)[1] / 2), -(getPartCenter(partName)[2] / 2));
        else
            super.childScale(partName);
    }
}
