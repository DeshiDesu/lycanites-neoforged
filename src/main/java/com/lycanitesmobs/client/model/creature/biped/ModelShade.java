package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelShade extends ModelTemplateBiped {
    public ModelShade() {
        this(1.0F);
    }

    public ModelShade(float shadowSize) {
    	this.initModel("shade", LycanitesMobs.modInfo, "entity/shade");

    	// Looking:
		this.lookHeadScaleX = 0.8F;
		this.lookHeadScaleY = 0.8F;
		this.lookNeckScaleX = 0.2F;
		this.lookNeckScaleY = 0.2F;

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, -0.2F, 0.0F};
    }

	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "eyes", true, CustomRenderStates.BLEND.ADD.id, true));
	}

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	if(entity instanceof BaseCreatureEntity && entity.getControllingPassenger() != null) {
			time = time * 0.25F;
			distance = distance * 0.8F;
		}
    	super.animatePart(partName, entity, time, distance * 0.5F, loop, lookY, lookX, scale);

		if(partName.equals("mouth")) {
			this.rotate((float)-Math.toDegrees(Mth.cos(loop * 0.2F) * 0.1F), 0.0F, 0.0F);
		}
    }
}
