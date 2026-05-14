package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelMalwrath extends ModelTemplateBiped {

    public ModelMalwrath() {
        this(1.0F);
    }
    
    public ModelMalwrath(float shadowSize) {
    	this.initModel("malwrath", LycanitesMobs.modInfo, "entity/malwrath");
		this.flightBobScale = 0.1F;
		this.trophyScale = 0.5F;
    }

	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, CustomRenderStates.BLEND.ADD.id, true));
	}

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		// Looking:
		float rotX = 0;
		float rotY = 0;
		if(partName.toLowerCase().equals("body")) {
			rotX += (Math.toDegrees(lookX / (180F / (float)Math.PI)) * 0.75F);
			rotY += (Math.toDegrees(lookY / (180F / (float)Math.PI))) * 0.75F;
		}
		if(partName.equals("eye")) {
			rotX += (Math.toDegrees(lookX / (180F / (float)Math.PI)) * 0.25F);
			rotY += (Math.toDegrees(lookY / (180F / (float)Math.PI))) * 0.25F;
		}
		this.rotate(rotX, rotY, 0);
    }
}
