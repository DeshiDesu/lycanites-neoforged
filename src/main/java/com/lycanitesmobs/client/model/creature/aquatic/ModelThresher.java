package com.lycanitesmobs.client.model.creature.aquatic;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateAquatic;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.specific.LayerThresher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ModelThresher extends ModelTemplateAquatic {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelThresher() {
        this(1.0F);
    }

    public ModelThresher(float shadowSize) {
        // Load Model:
        this.initModel("thresher", LycanitesMobs.modInfo, "entity/thresher");

        // Looking:
        this.lookHeadScaleX = 0.5f;
        this.lookHeadScaleY = 0.5f;
        this.lookNeckScaleX = 0.5f;
        this.lookNeckScaleY = 0.5f;

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
    }


    // ==================================================
    //             Add Custom Render Layers
    // ==================================================
    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerThresher(renderer));
    }


    // ==================================================
    //                Can Render Part
    // ==================================================

    /**
     * Returns true if the part can be rendered on the base layer.
     **/
    @Override
    public boolean canBaseRenderPart(String partName, Entity entity, boolean trophy) {
        if ("effect".equals(partName)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if ("effect".equals(partName)) {
            return layer instanceof LayerThresher;
        }
        return super.canRenderPart(partName, entity, layer, trophy);
    }


    // ==================================================
    //                 Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
        float posX = 0F;
        float posY = 0F;
        float posZ = 0F;
        float rotX = 0F;
        float rotY = 0F;
        float rotZ = 0F;

        // Effect:
        if (partName.contains("effect")) {
            rotY += loop * 30;
            float effectScale = 1.05F + ((0.5F + (Mth.sin(loop / 4) / 2)) / 8);
            this.scale(effectScale, effectScale, effectScale);
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
