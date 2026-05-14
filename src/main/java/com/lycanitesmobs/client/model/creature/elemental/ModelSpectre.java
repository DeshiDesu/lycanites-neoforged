package com.lycanitesmobs.client.model.creature.elemental;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.creature.elemental.EntitySpectre;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelSpectre extends ModelTemplateElemental {

	// ==================================================
  	//                  Constructors
  	// ==================================================
    public ModelSpectre() {
        this(1.0F);
    }

    public ModelSpectre(float shadowSize) {

		// Load Model:
		this.initModel("spectre", LycanitesMobs.modInfo, "entity/spectre");

		// Trophy:
		this.trophyScale = 1.2F;
		this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
		this.trophyMouthOffset = new float[] {0.0F, -0.25F, 0.0F};
    }


	// ==================================================
	//             Add Custom Render Layers
	// ==================================================
	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, CustomRenderStates.BLEND.ADD.id, true));
		renderer.addLayer(new LayerCreatureEffect(renderer, "", false, CustomRenderStates.BLEND.SUB.id, true));
	}


	// ==================================================
	//                Can Render Part
	// ==================================================
	@Override
	public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
		if("effect01".equals(partName)) {
			return layer != null && "".equals(layer.name);
		}
		if("effect02".equals(partName) || "effect03".equals(partName)) {
			if(entity instanceof EntitySpectre && layer != null && "".equals(layer.name)) {
				return ((EntitySpectre)entity).canPull();
			}
			return false;
		}
		return layer == null || "glow".equals(layer.name);
	}


	// ==================================================
	//                 Animate Part
	// ==================================================
	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		// Pulling:
		boolean isPulling = false;
		if(entity instanceof EntitySpectre) {
			isPulling = ((EntitySpectre)entity).canPull();
		}

		// Loop Offset:
		float loopOffset = 0;
		if(partName.contains("upper")) {
			loopOffset += 10;
		}
		else if(partName.contains("middle")) {
			loopOffset += 20;
		}
		else if(partName.contains("lower")) {
			loopOffset += 30;
		}

		if("effect01".equals(partName)) {
			this.rotate(25, 0, -loop * 10);
			float effectScale = 1 + ((float)Math.cos(loop / 10) * 0.1f);
			if(isPulling) {
				effectScale *= 2;
			}
			this.scale(effectScale, effectScale, effectScale);
		}
		else if("effect02".equals(partName)) {
			this.rotate(0, 0, -loop * 10);
			float effectScale = 2 + ((float)Math.cos(loop / 10));
			this.scale(effectScale, effectScale, effectScale);
		}
		else if("effect03".equals(partName)) {
			this.rotate(0, 0, loop * 10);
			float effectScale = 2 + ((float)Math.cos(loop / 10));
			this.scale(effectScale, effectScale, effectScale);
		}

		else if(partName.contains("armleft")) {
			this.rotate(
					(float)Math.toDegrees(Mth.sin(loop * 0.034F) * 0.05F),
					(float)Math.toDegrees(Mth.sin((loop + (loopOffset / 2)) * 0.1F) * 0.25F) - 10,
					(float)-Math.toDegrees(Mth.cos((loop + (loopOffset / 2)) * 0.09F) * 0.1F)
			);
		}
		else if(partName.contains("armright")) {
			this.rotate(
					(float)Math.toDegrees(Mth.sin(loop * 0.034F) * 0.05F),
					(float)-Math.toDegrees(Mth.sin((loop + (loopOffset / 2)) * 0.1F) * 0.25F) + 10,
					(float)Math.toDegrees(Mth.cos((loop + (loopOffset / 2)) * 0.09F) * 0.1F)
			);
		}

		else if(partName.contains("mouthleft")) {
			this.rotate((float)Math.cos(loop / 10) * 4, (float)Math.cos(loop / 10) * 4, 0);
		}
		else if(partName.contains("mouthright")) {
			this.rotate((float)Math.cos(loop / 10) * 4, -(float)Math.cos(loop / 10) * 4, 0);
		}

		else if(partName.contains("mawleft")) {
			this.rotate(0, (float)Math.cos(loop / 10) * 10 + (isPulling ? 90 : 0), 0);
		}
		else if(partName.contains("mawright")) {
			this.rotate(0, -(float) Math.cos(loop / 10) * 10 - (isPulling ? 90 : 0), 0);
		}
	}
}
