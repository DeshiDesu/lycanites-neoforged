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
public class ModelReiver extends ModelTemplateElemental {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelReiver() {
        this(1.0F);
    }
    
    public ModelReiver(float shadowSize) {

    	// Load Model:
    	this.initModel("reiver", LycanitesMobs.modInfo, "entity/reiver");

        // Tropy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.2F};
    }


	// ==================================================
	//             Add Custom Render Layers
	// ==================================================
	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "pulse01", false, CustomRenderStates.BLEND.NORMAL.id, false));
		renderer.addLayer(new LayerCreatureEffect(renderer, "pulse02", false, CustomRenderStates.BLEND.NORMAL.id, false));
		renderer.addLayer(new LayerCreatureEffect(renderer, "pulse03", false, CustomRenderStates.BLEND.NORMAL.id, false));
	}
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
    float maxLeg = 0F;
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		// Effect:
		if(partName.contains("effect")) {
			this.rotate(25, 0, 0);
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

		float alphaSpeed = 10;
		if("pulse02".equals(layer.name)) {
			alphaSpeed = 9.5f;
			loop += 100;
		}
		if("pulse03".equals(layer.name)) {
			alphaSpeed = 9;
			loop += 200;
		}
		float alpha = loop * alphaSpeed % 360;
		return new Vector4f(1, 1, 1, ((float)Math.cos(Math.toRadians(alpha)) / 2) + 0.5f);
	}
}
