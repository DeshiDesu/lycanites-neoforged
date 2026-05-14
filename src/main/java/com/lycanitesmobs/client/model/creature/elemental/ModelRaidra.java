package com.lycanitesmobs.client.model.creature.elemental;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ModelRaidra extends ModelTemplateElemental {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelRaidra() {
        this(1.0F);
    }

    public ModelRaidra(float shadowSize) {
    	// Load Model:
    	this.initModel("raidra", LycanitesMobs.modInfo, "entity/raidra");
    	
    	// Trophy:
        this.trophyScale = 1.2F;
        this.trophyOffset = new float[] {0.0F, -0.2F, 0.2F};
    }


	// ==================================================
	//             Add Custom Render Layers
	// ==================================================
	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, CustomRenderStates.BLEND.ADD.id, true));
		renderer.addLayer(new LayerCreatureEffect(renderer, "pulse01", true, CustomRenderStates.BLEND.ADD.id, true));
		renderer.addLayer(new LayerCreatureEffect(renderer, "pulse02", true, CustomRenderStates.BLEND.ADD.id, true));
		renderer.addLayer(new LayerCreatureEffect(renderer, "pulse03", true, CustomRenderStates.BLEND.ADD.id, true));
	}
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		if("effectouter".equals(partName) || "effectinner".equals(partName)) {
			this.rotate(-15, 0, 0);
		}

		if(partName.equals("armeffectleft")) {
			this.angle(loop * 10F, 0, 0.6F, -0.8F);
		}
		if(partName.equals("armeffectright")) {
			this.angle(loop * 10F, 0, -0.6F, 0.8F);
		}

		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
    }


	// ==================================================
	//                Get Part Color
	// ==================================================
	/** Returns the coloring to be used for this part and layer. **/
	public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
		if(layer == null) {
			return super.getPartColor(partName, entity, layer, trophy, loop);
		}

		float alphaTime = 15;
		if("pulse02".equals(layer.name)) {
			alphaTime = 20;
		}
		if("pulse03".equals(layer.name)) {
			alphaTime = 25;
		}

		float alpha = (loop % alphaTime / alphaTime) * 2;
		if(alpha > 1) {
			alpha = -(alpha - 1);
		}

		return new Vector4f(1, 1, 1, alpha);
	}
}
