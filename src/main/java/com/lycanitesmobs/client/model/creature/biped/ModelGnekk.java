package com.lycanitesmobs.client.model.creature.biped;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelGnekk extends ModelTemplateBiped {
    public ModelGnekk() {
        this(1.0F);
    }

    public ModelGnekk(float shadowSize) {
    	// Load Model:
    	this.initModel("gnekk", LycanitesMobs.modInfo, "entity/gnekk");

        // Trophy:
        this.trophyScale = 1.8F;
    }

	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, CustomRenderStates.BLEND.ADD.id, true));
	}

	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		// Perching:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPerchTarget()) {
			if("armleft".equalsIgnoreCase(partName) || "armright".equalsIgnoreCase(partName))
			this.rotate(45, 0, 0);
		}
	}
}
