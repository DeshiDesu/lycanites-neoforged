package com.lycanitesmobs.client.model.creature.quadruped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelNecrovore extends ModelTemplateQuadruped {

    public ModelNecrovore() {
        this(1.0F);
    }

    public ModelNecrovore(float shadowSize) {
    	this.initModel("necrovore", LycanitesMobs.modInfo, "entity/necrovore");

        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, -0.2F, 0.0F};
    }

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

    	// Idle:
		if(partName.contains("tail")) {
			rotate(0.0F, (float)-Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F + 0.1F), 0.0F);
		}

		// Attack:
		if(partName.equals("lefleftfront") || partName.equals("lefrightfront")) {
			rotate(-75.0F * this.getAttackProgress(), 0.0F, 0.0F);
		}
    }
}
