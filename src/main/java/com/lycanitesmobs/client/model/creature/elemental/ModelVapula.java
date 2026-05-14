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
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ModelVapula extends ModelTemplateElemental {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelVapula() {
        this(1.0F);
    }

    public ModelVapula(float shadowSize) {

        // Load Model:
        this.initModel("vapula", LycanitesMobs.modInfo, "entity/vapula");

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
        renderer.addLayer(new LayerCreatureEffect(renderer, "", true, CustomRenderStates.BLEND.NORMAL.id, true));
    }


    // ==================================================
    //                 Animate Part
    // ==================================================
    float maxLeg = 0F;

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Crystals:
        if (partName.contains("effect")) {
            this.shiftOrigin(partName, "crystals");
            this.rotate(0, 0, loop * 8);
            this.shiftOriginBack(partName, "crystals");
        }

        // Fingers:
        else if (partName.equals("fingerleft01") || partName.equals("fingerright01")) {
            this.rotate(0, (float) Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F - 0.2F), 0);
        } else if (partName.equals("fingerleft02") || partName.equals("fingerright02")) {
            this.rotate(0, (float) Math.toDegrees(Mth.cos((loop + 20) * 0.2F) * 0.2F - 0.2F), 0);
        } else if (partName.equals("fingerleft03") || partName.equals("fingerright03")) {
            this.rotate(0, (float) Math.toDegrees(Mth.cos((loop + 40) * 0.2F) * 0.2F - 0.2F), 0);
        }
    }


    // ==================================================
    //                Can Render Part
    // ==================================================

    /**
     * Returns true if the part can be rendered on the base layer.
     **/
    @Override
    public boolean canBaseRenderPart(String partName, Entity entity, boolean trophy) {
        return true;
    }

    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if (!this.isCrystal(partName)) {
            return layer == null;
        }

        if (layer instanceof LayerCreatureEffect) {
            if (partName.contains("effect")) {
                if (entity instanceof BaseCreatureEntity) {
                    int attackPhase = ((BaseCreatureEntity) entity).getAttackPhase();
                    if (((BaseCreatureEntity) entity).isAttackOnCooldown() && attackPhase == 0) {
                        return false;
                    }
                    if ("effect01".equals(partName)) {
                        return attackPhase <= 7;
                    } else if ("effect02".equals(partName)) {
                        return attackPhase <= 6;
                    } else if ("effect03".equals(partName)) {
                        return attackPhase <= 5;
                    } else if ("effect04".equals(partName)) {
                        return attackPhase <= 4;
                    } else if ("effect05".equals(partName)) {
                        return attackPhase <= 3;
                    } else if ("effect06".equals(partName)) {
                        return attackPhase <= 2;
                    } else if ("effect07".equals(partName)) {
                        return attackPhase <= 1;
                    } else if ("effect08".equals(partName)) {
                        return attackPhase <= 0;
                    }
                }
            } else {
                return true;
            }
        }

        return false;
    }

    protected boolean isCrystal(String partName) {
        if ("eye".equals(partName) || "crystals".equals(partName)) {
            return true;
        }
        return partName.contains("effect") || partName.contains("finger");
    }


    // ==================================================
    //                Get Part Color
    // ==================================================

    /**
     * Returns the coloring to be used for this part and layer.
     **/
    @Override
    public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
        if (this.isCrystal(partName)) {
            float glowSpeed = 40;
            float glow = loop * glowSpeed % 360;
            float color = ((float) Math.cos(Math.toRadians(glow)) * 0.1f) + 0.9f;
            return new Vector4f(color, color, color, 1);
        }

        return super.getPartColor(partName, entity, layer, trophy, loop);
    }
}
