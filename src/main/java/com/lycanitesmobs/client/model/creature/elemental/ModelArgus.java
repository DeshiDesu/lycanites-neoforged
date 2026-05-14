package com.lycanitesmobs.client.model.creature.elemental;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelArgus extends ModelTemplateElemental {

    // ==================================================
    //                  Constructors
    // ==================================================
    public ModelArgus() {
        this(1.0F);
    }

    public ModelArgus(float shadowSize) {

        // Load Model:
        this.initModel("argus", LycanitesMobs.modInfo, "entity/argus");

        // Trophy:
        this.trophyScale = 1.2F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
    }


    // ==================================================
    //             Add Custom Render Layers
    // ==================================================
    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerCreatureEffect(renderer, "flash", true, CustomRenderStates.BLEND.ADD.id, true));
    }


    // ==================================================
    //                Can Render Part
    // ==================================================
    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if (layer instanceof LayerCreatureEffect && entity instanceof BaseCreatureEntity) {
            return ((BaseCreatureEntity) entity).isAttackOnCooldown();
        }
        return true;
    }


    // ==================================================
    //                 Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Arms:
        if (partName.contains("arm")) {
            float rotX = (float) Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F) * 4;
            float rotY = (float) Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F);
            if ("arm01".equals(partName) || "arm02".equals(partName) || "arm03".equals(partName)) {
                this.rotate(rotX, rotY, loop * 8);
                this.translate(Mth.cos(loop) * 0.05F - 0.05F, Mth.cos(loop) * 0.05F - 0.05F, 0);
            } else if ("arm04".equals(partName) || "arm05".equals(partName) || "arm06".equals(partName)) {
                this.rotate(rotX, rotY, -loop * 8);
                this.translate(Mth.cos(-loop) * 0.05F - 0.05F, Mth.cos(-loop) * 0.05F - 0.05F, 0);
            }
        }

        // Tail:
        if (partName.contains("tail")) {
            float tailSwipeX = 32F;
            float tailSwipeY = 32F;
            this.rotate(
                    (Mth.cos(loop * 0.1F) * tailSwipeX) - (tailSwipeX * 90),
                    (Mth.sin(loop * 0.2F) * tailSwipeY) - (tailSwipeY * 90),
                    0
            );
        }

        // Attack:
        if (partName.equals("mouth")) {
            this.rotate(45 * this.getAttackProgress(), 0, 0);
        }
    }
}
