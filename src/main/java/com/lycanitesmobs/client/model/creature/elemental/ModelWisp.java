package com.lycanitesmobs.client.model.creature.elemental;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureScrolling;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ModelWisp extends ModelTemplateElemental {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelWisp() {
        this(1.0F);
    }

    public ModelWisp(float shadowSize) {

        // Load Model:
        this.initModel("wisp", LycanitesMobs.modInfo, "entity/wisp");

        // Trophy:
        this.trophyScale = 1.2F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
    }


    // ==================================================
    //             Add Custom Render Layers
    // ==================================================
    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerCreatureEffect(renderer, "ball", true, CustomRenderStates.BLEND.NORMAL.id, true));
        renderer.addLayer(new LayerCreatureEffect(renderer, "ball_glow", "ball", true, CustomRenderStates.BLEND.ADD.id, true));
        renderer.addLayer(new LayerCreatureScrolling(renderer, "hair", true, CustomRenderStates.BLEND.NORMAL.id, true, new Vector2f(0, 4)));
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


    // ==================================================
    //                 Animate Part
    // ==================================================
    float maxLeg = 0F;

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Hair:
        if (partName.equals("haircenter")) {
            this.rotate((float) Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F), 0, 0);
        } else if (partName.equals("hairleft")) {
            this.rotate((float) Math.toDegrees(Mth.cos(loop * 0.05F) * 0.1F), (float) Math.toDegrees(Mth.cos(loop * 0.1F) * 0.2F - 0.2F), 0);
        } else if (partName.equals("hairright")) {
            this.rotate((float) Math.toDegrees(Mth.cos(loop * 0.05F) * 0.1F), -(float) Math.toDegrees(Mth.cos(loop * 0.1F) * 0.2F - 0.2F), 0);
        } else if (partName.contains("fringeleft")) {
            this.rotate(
                    -(float) Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F),
                    0,
                    -(float) Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F)
            );
        } else if (partName.contains("fringeright")) {
            this.rotate(
                    (float) Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F),
                    0,
                    (float) Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F)
            );
        }

        // Arms:
        else if (partName.equals("armleft")) {
            this.rotate(0, 0, (float) Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F));
        } else if (partName.equals("armright")) {
            this.rotate(0, 0, -(float) Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F));
        }
        if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity) entity).isAttackOnCooldown()) {
            if (partName.equals("armleft"))
                rotate(0F, 25.0F, 25.0F);
            if (partName.equals("armright"))
                rotate(0F, -25.0F, -25.0F);
        }
    }


    // ==================================================
    //                Can Render Part
    // ==================================================
    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if (partName.contains("ball") && entity instanceof BaseCreatureEntity && ((BaseCreatureEntity) entity).isAttackOnCooldown()) {
            return false;
        }
        if (partName.equals("ball01")) {
            return layer != null && "ball".equals(layer.name);
        }
        if (partName.equals("ball02") || partName.equals("ball03")) {
            return layer != null && "ball_glow".equals(layer.name);
        }
        if (partName.contains("fringe")) {
            return layer != null && "hair".equals(layer.name);
        }
        if (partName.contains("hair")) {
            return layer != null && "hair".equals(layer.name);
        }
        return layer == null;
    }


    // ==================================================
    //                Get Part Color
    // ==================================================

    /**
     * Returns the coloring to be used for this part and layer.
     **/
    @Override
    public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
        if (layer != null && ("ball".equals(layer.name) || "ball_glow".equals(layer.name))) {
            float glowSpeed = 40;
            float glow = loop * glowSpeed % 360;
            float color = ((float) Math.cos(Math.toRadians(glow)) * 0.1f) + 0.9f;
            return new Vector4f(color, color, color, 1);
        }

        return super.getPartColor(partName, entity, layer, trophy, loop);
    }
}
