package com.lycanitesmobs.client.model.creature.quadruped;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBobeko extends ModelTemplateQuadruped {
    public ModelBobeko() {
        this(1.0F);
    }
    
    public ModelBobeko(float shadowSize) {
    	this.initModel("bobeko", LycanitesMobs.modInfo, "entity/bobeko");

    	this.bigChildHead = true;
    }

    float maxLeg = 0F;
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
		float posX = 0F;
		float posY = 0F;
		float posZ = 0F;
		float rotX = 0F;
		float rotY = 0F;
		float rotZ = 0F;

		if(partName.equals("earleft")) {
			rotZ -= Math.toDegrees(Mth.cos(loop * 0.18F) * 0.05F + 0.05F);
			rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
		}
		if(partName.equals("earright")) {
			rotZ += Math.toDegrees(Mth.cos(loop * 0.18F) * 0.05F + 0.05F);
			rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
		}

		// Apply Animations:
		translate(posX, posY, posZ);
		rotate(rotX, rotY, rotZ);
    }
}
