package com.lycanitesmobs.client.model.creature.generic;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.creature.undead.EntityAmalgalich;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelAmalgalich extends CreatureObjModel {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelAmalgalich() {
        this(1.0F);
    }

    public ModelAmalgalich(float shadowSize) {
        // Load Model:
        this.initModel("amalgalich", LycanitesMobs.modInfo, "entity/amalgalich");

        // Scaling:
        this.lookHeadScaleX = 0F;
        this.lookHeadScaleY = 0.75F;
        this.lookNeckScaleX = 0.25F;
        this.lookNeckScaleY = 0.25F;

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
    }


    // ==================================================
    //                      Layers
    // ==================================================
    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerCreatureEffect(renderer, "fire", true, CustomRenderStates.BLEND.ADD.id, true));
    }

    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if (layer != null && entity instanceof EntityAmalgalich) {
            return ((EntityAmalgalich) entity).getConsumptionAnimation() > 0;
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

        // Consumption:
        if (entity instanceof EntityAmalgalich && ((EntityAmalgalich) entity).getConsumptionAnimation() > 0) {
            float consumptionScale = ((EntityAmalgalich) entity).getConsumptionAnimation();
            if (consumptionScale == 1) {
                loop *= 8 * consumptionScale;
            }
            if (partName.equals("body")) {
                posY -= 8F * consumptionScale;
            }
            if (partName.equals("legleftfront01") || partName.equals("legleftback01")) {
                rotZ += 65F * consumptionScale;
            }
            if (partName.equals("legrightfront01") || partName.equals("legrightback01")) {
                rotZ -= 65F * consumptionScale;
            }
            if (partName.equals("legleftfront03") || partName.equals("legleftback03")) {
                rotZ -= 25F * consumptionScale;
            }
            if (partName.equals("legrightfront03") || partName.equals("legrightback03")) {
                rotZ += 25F * consumptionScale;
            }
            if (partName.equals("neck")) {
                rotX += 20F * consumptionScale;
            }
            if (partName.equals("head")) {
                rotX -= 20F * consumptionScale;
            }
            if (partName.equals("mouth")) {
                rotX += 40F * consumptionScale;
            }
            if (partName.equals("armleft01")) {
                rotZ += 35F * consumptionScale;
            }
            if (partName.equals("armright01")) {
                rotZ -= 35F * consumptionScale;
            }
        }

        // Idle:
        if (partName.equals("mouth")) {
            this.rotate((float) -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F - 0.1F), 0.0F, 0.0F);
        }
        if (partName.equals("neck")) {
            this.rotate((float) -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
        }
        if (partName.contains("armleft")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if (partName.contains("armright")) {
            rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if (partName.contains("tail")) {
            rotX = (float) -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
            rotY = (float) -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
        }

        // Fingers:
        else if (partName.equals("fingerleft01") || partName.equals("fingerright01")) {
            this.rotate((float) Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F), 0, 0);
        } else if (partName.equals("fingerleft02") || partName.equals("fingerright02")) {
            this.rotate((float) Math.toDegrees(Mth.cos((loop + 20) * 0.2F) * 0.2F), 0, 0);
        } else if (partName.equals("fingerleft03") || partName.equals("fingerright03")) {
            this.rotate((float) Math.toDegrees(Mth.cos((loop + 40) * 0.2F) * 0.2F), 0, 0);
        }

        // Walking:
        float walkSwing = 0.1F;
        if (partName.contains("armleft")) {
            rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 1.0F * distance * 0.5F);
            rotZ -= Math.toDegrees(Mth.cos(time * walkSwing) * 0.5F * distance * 0.5F);
        }
        if (partName.contains("armright")) {
            rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float) Math.PI) * 1.0F * distance * 0.5F);
            rotZ += Math.toDegrees(Mth.cos(time * walkSwing + (float) Math.PI) * 0.5F * distance * 0.5F);
        }
        if (partName.contains("legrightfront") || partName.contains("legleftback"))
            rotX += Math.toDegrees(Mth.cos(time * 0.6662F + (float) Math.PI) * walkSwing * distance);
        if (partName.contains("legleftfront") || partName.contains("legrightback"))
            rotX += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
