package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelWraamon extends ModelTemplateBiped {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelWraamon() {
        this(1.0F);
    }

    public ModelWraamon(float shadowSize) {
        // Load Model:
        this.initModel("wraamon", LycanitesMobs.modInfo, "entity/wraamon");

        // Looking:
        this.lookHeadScaleX = 0.8F;
        this.lookHeadScaleY = 0.8F;
        this.lookNeckScaleX = 0.2F;
        this.lookNeckScaleY = 0.2F;

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
    }


    // ==================================================
    //             Add Custom Render Layers
    // ==================================================
    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, CustomRenderStates.BLEND.ADD.id, true));
    }


    // ==================================================
    //                    Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        // Standing:
        if (this.currentModelState != null) {
            if (this.currentModelState.getBoolean("standing")) {
                if (partName.equals("body")) {
                    this.rotate(-45, 0, 0);
                } else if (partName.equals("head")) {
                    this.rotate(30, 0, 0);
                } else if (partName.contains("leg")) {
                    this.rotate(45, 0, 0);
                } else if (partName.contains("tail")) {
                    this.rotate(50, 0, 0);
                }
            }
            if (this.currentModelState.getFloat("standingChangeTime") <= 0) {
                this.currentModelState.setBoolean("standing", !this.currentModelState.getBoolean("standing"));
                this.currentModelState.setFloat("standingChangeTime", (2 * 20) + (8 * 20 * entity.getRandom().nextFloat()));
            }
            if (partName.equals("body")) {
                this.currentModelState.setFloat("standingChangeTime", this.currentModelState.getFloat("standingChangeTime") - 1);
            }
        }

        // Super:
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Idle:
        if (partName.equals("nose")) {
            this.rotate((float) -Math.toDegrees(Mth.cos(loop * 0.2F) * 0.1F), (float) -Math.toDegrees(Mth.cos(loop * 0.4F) * 0.1F), 0.0F);
        } else if (partName.equals("tailleft")) {
            float rotX = (float) -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
            float rotY = (float) -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
            this.rotate(rotX, rotY, 0);
        } else if (partName.equals("tailright")) {
            float rotX = (float) Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
            float rotY = (float) Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
            this.rotate(rotX, rotY, 0);
        }
    }
}
