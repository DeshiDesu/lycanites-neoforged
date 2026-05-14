package com.lycanitesmobs.client.model.creature.elemental;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureScrolling;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ModelBanshee extends ModelTemplateElemental {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelBanshee() {
        this(1.0F);
    }

    public ModelBanshee(float shadowSize) {
    	// Load Model:
    	this.initModel("banshee", LycanitesMobs.modInfo, "entity/banshee");
    	
    	// Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }


	// ==================================================
	//             Add Custom Render Layers
	// ==================================================
	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "eye", true, CustomRenderStates.BLEND.NORMAL.id, true));
		renderer.addLayer(new LayerCreatureScrolling(renderer, "hair", false, CustomRenderStates.BLEND.NORMAL.id, true, new Vector2f(0, -16)));
	}
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

    	// Hands
		float bob = -Mth.sin(loop * 0.2F) * 0.15F;
		if(partName.equals("handleft")) {
			this.translate(bob / 4, bob, this.getAttackProgress());
		}
		else if(partName.equals("handright")) {
			this.translate(-bob / 4, -bob, this.getAttackProgress());
		}

		// Eyes:
		if(partName.toLowerCase().equals("eyeleft")) {
			this.rotate(
					(float)Math.toDegrees(lookX / (180F / (float)Math.PI)),
					(float)Math.toDegrees((lookY - 15F) / (180F / (float)Math.PI)),
					0
			);
		}
		else if(partName.toLowerCase().equals("eyeright")) {
			this.rotate(
					(float)Math.toDegrees(lookX / (180F / (float)Math.PI)),
					(float)Math.toDegrees((lookY + 15F) / (180F / (float)Math.PI)),
					0
			);
		}

		// Hair
		else if(partName.equals("hair01")) {
			this.rotate((float)Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F - 0.2F), 0, 0);
		}
		else if(partName.equals("hair02")) {
			this.rotate((float)Math.toDegrees(Mth.cos(loop * 0.1F) * 0.2F - 0.2F), 0, 0);
		}
		
    	// Fingers:
		else if(partName.equals("fingerleft01") || partName.equals("fingerright01")) {
			this.rotate((float)Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F - 0.2F), 0, 0);
    	}
		else if(partName.equals("fingerleft02") || partName.equals("fingerright02")) {
			this.rotate((float)Math.toDegrees(Mth.cos((loop + 20) * 0.2F) * 0.2F - 0.2F), 0, 0);
		}
		else if(partName.equals("fingerleft03") || partName.equals("fingerright03")) {
			this.rotate((float)Math.toDegrees(Mth.cos((loop + 40) * 0.2F) * 0.2F - 0.2F), 0, 0);
		}
		else if(partName.equals("fingerleft04") || partName.equals("fingerright04")) {
			this.rotate((float)Math.toDegrees(Mth.cos((loop + 60) * 0.2F) * 0.2F - 0.2F), 0, 0);
		}
		else if(partName.equals("fingerleft05") || partName.equals("fingerright05")) {
			this.rotate((float)Math.toDegrees(Mth.cos((loop + 60) * 0.2F) * 0.2F + 0.2F), 0, 0);
		}
    }


	// ==================================================
	//                Can Render Part
	// ==================================================
	@Override
	public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
		if(partName.contains("eye")) {
			return layer != null && "eye".equals(layer.name);
		}
		if(partName.contains("hair")) {
			return layer != null && "hair".equals(layer.name);
		}
		return layer == null;
	}


	// ==================================================
	//                Get Part Color
	// ==================================================
	/** Returns the coloring to be used for this part and layer. **/
	@Override
	public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
		return super.getPartColor(partName, entity, layer, trophy, loop);
	}
}
