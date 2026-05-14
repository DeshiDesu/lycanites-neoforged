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
public class ModelXaphan extends ModelTemplateElemental {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelXaphan() {
        this(1.0F);
    }

    public ModelXaphan(float shadowSize) {
        // Load Model:
        this.initModel("xaphan", LycanitesMobs.modInfo, "entity/xaphan");

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
    }


    // ==================================================
    //             Add Custom Render Layers
    // ==================================================
    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerCreatureScrolling(renderer, "ball", true, CustomRenderStates.BLEND.NORMAL.id, true, new Vector2f(0, 4)));
        renderer.addLayer(new LayerCreatureEffect(renderer, "ring", true, CustomRenderStates.BLEND.ADD.id, true));
    }


    // ==================================================
    //                 Animate Part
    // ==================================================
    float maxLeg = 0F;

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Fingers:
        if (partName.equals("fingerleft01") || partName.equals("fingerright01")) {
            this.rotate((float) Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F - 0.2F), 0, 0);
        } else if (partName.equals("fingerleft02") || partName.equals("fingerright02")) {
            this.rotate((float) Math.toDegrees(Mth.cos((loop + 20) * 0.2F) * 0.2F - 0.2F), 0, 0);
        } else if (partName.equals("fingerleft03") || partName.equals("fingerright03")) {
            this.rotate((float) Math.toDegrees(Mth.cos((loop + 40) * 0.2F) * 0.2F - 0.2F), 0, 0);
        }

        // Shoulders:
        else if (partName.equals("shoulderleft")) {
            this.rotate(
                    (float) Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F),
                    0,
                    (float) Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F)
            );
        } else if (partName.equals("shoulderright")) {
            this.rotate(
                    (float) Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F),
                    0,
                    (float) -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F)
            );
        }

        // Fingers:
        else if (partName.equals("effect01")) {
            this.rotate(0, loop * 16, 0);
        }

        // Balls:
        else if (partName.contains("ball")) {
            this.shiftOrigin(partName, "body");
            this.rotate(0, loop * -8, 0);
            this.shiftOriginBack(partName, "body");
            float ballScale = (float) (1 + 0.2 * (Mth.cos(loop)));
            if (partName.equals("ball02")) {
                ballScale = (float) (1 + 0.2 * (Mth.cos(loop + 20)));
            } else if (partName.equals("ball03")) {
                ballScale = (float) (1 + 0.2 * (Mth.cos(loop + 40)));
            } else if (partName.equals("ball04")) {
                ballScale = (float) (1 + 0.2 * (Mth.cos(loop + 60)));
            }
            this.scale(ballScale, ballScale, ballScale);
        }

        // Balls:
        else if (partName.contains("spine")) {
            float coil = 8F;
            this.rotate((Mth.cos(loop * 0.1F) * coil) - (coil * 90), 0, 0);
        }
    }


    // ==================================================
    //                Can Render Part
    // ==================================================
    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if ("effect01".equals(partName)) {
            return layer != null && "ring".equals(layer.name);
        }
        if (partName.contains("ball")) {
            return layer != null && "ball".equals(layer.name);
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
        return super.getPartColor(partName, entity, layer, trophy, loop);
    }
}
