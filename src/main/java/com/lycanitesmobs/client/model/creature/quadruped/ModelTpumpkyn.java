package com.lycanitesmobs.client.model.creature.quadruped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureScrolling;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;

@OnlyIn(Dist.CLIENT)
public class ModelTpumpkyn extends ModelTemplateQuadruped {

    public ModelTpumpkyn() {
        this(1.0F);
    }

    public ModelTpumpkyn(float shadowSize) {
    	this.initModel("tpumpkyn", LycanitesMobs.modInfo, "entity/tpumpkyn");

        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, -0.2F, 0.0F};
    }

	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureScrolling(renderer, "effect", true, CustomRenderStates.BLEND.NORMAL.id, false, new Vector2f(0, 2)));
	}

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {

    	// Standing:
		if (distance < 0.1F && !((BaseCreatureEntity)entity).hasAttackTarget()) {
			if (partName.contains("body")) {
				this.translate(0, -0.25F, 0);
			}
			if (partName.contains("leg")) {
				this.scale(distance, distance, distance);
			}
			if (partName.equals("eyes") || partName.equals("effect")) {
				this.scale(0, 0, 0);
			}
			if (partName.equals("mouth")) {
				this.translate(0, 0.1F, 0);
			}
			return;
		}

    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		if (partName.contains("hair")) {
			float rotZ = (float)Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
			float rotX = (float)Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
			this.rotate(rotX, 0, rotZ);
		}
    }

	@Override
	public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
		if (partName.contains("effect") || partName.contains("eye")) {
			return layer != null && "effect".equals(layer.name);
		}
		return layer == null;
	}
}
