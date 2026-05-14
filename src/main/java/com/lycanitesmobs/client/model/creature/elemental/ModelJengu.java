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
import org.joml.Vector2f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ModelJengu extends ModelTemplateElemental {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelJengu() {
        this(1.0F);
    }

    public ModelJengu(float shadowSize) {
    	// Load Model:
    	this.initModel("jengu", LycanitesMobs.modInfo, "entity/jengu");
    	
    	// Trophy:
        this.trophyScale = 1.2F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.2F};
    }


	// ==================================================
	//             Add Custom Render Layers
	// ==================================================
	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "", false, CustomRenderStates.BLEND.NORMAL.id, true));
	}


	// ==================================================
	//                 Animate Part
	// ==================================================
	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		float rotX = 0F;
		float rotY = 0F;
		float rotZ = 0F;

		// Effects:
		if(partName.equals("effectouter"))
			rotX = 15F;
		if(partName.equals("effectinner"))
			rotX = 30F;

		// Apply Animations:
		this.rotate(rotX, rotY, rotZ);

		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
	}


	// ==================================================
	//              Get Part Texture Offset
	// ==================================================
	@Override
	public Vector2f getBaseTextureOffset(String partName, Entity entity, boolean trophy, float loop) {
		if(partName.contains("effect")) {
			return super.getBaseTextureOffset(partName, entity, trophy, loop);
		}
		return new Vector2f(0, loop);
	}


	// ==================================================
	//                Can Render Part
	// ==================================================
	@Override
	public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
		if(partName.contains("effect")) {
			return layer != null;
		}
		return layer == null;
	}


	// ==================================================
	//                Get Part Color
	// ==================================================
	/** Returns the coloring to be used for this part and layer. **/
	public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
		if(partName.contains("effect")) {
			return new Vector4f(1, 1, 1, 0.5f);
		}

		return super.getPartColor(partName, entity, layer, trophy, loop);
	}
}
