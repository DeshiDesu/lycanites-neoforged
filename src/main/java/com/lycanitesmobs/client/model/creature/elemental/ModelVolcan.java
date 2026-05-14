package com.lycanitesmobs.client.model.creature.elemental;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelVolcan extends ModelTemplateElemental {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelVolcan() {
        this(1.0F);
    }

    public ModelVolcan(float shadowSize) {

        // Load Model:
        this.initModel("volcan", LycanitesMobs.modInfo, "entity/volcan");

        // Trophy:
        this.trophyScale = 1.2F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
    }

    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerCreatureEffect(renderer, "glow", "glow", true, CustomRenderStates.BLEND.ADD.getValue(), true));
    }

    @Override
    public int getBrightness(String partName, LayerCreatureBase layer, BaseCreatureEntity entity, int brightness) {
        return ClientManager.FULL_BRIGHT;
    }

    @Override
    public boolean getGlow(BaseCreatureEntity entity, LayerCreatureBase layer) {
        if (layer != null) {
            return super.getGlow(entity, layer);
        }
        return true;
    }

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Hands:
        if (partName.contains("hand")) {
            if (partName.contains("right")) {
                this.angle(loop * 10F, 0, -0.9F, 0.5F);
            } else {
                this.angle(loop * 10F, 0, 0.9F, -0.5F);
            }
        }

        // Effects:
        if (partName.equals("effect01") || partName.equals("effect03")) {
            this.rotate(0, loop * 8, 0);
        }
        if (partName.equals("effect02") || partName.equals("effect04")) {
            this.rotate(0, loop * -8, 0);
        }
    }
}
