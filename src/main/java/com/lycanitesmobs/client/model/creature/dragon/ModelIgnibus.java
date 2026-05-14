package com.lycanitesmobs.client.model.creature.dragon;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateDragon;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelIgnibus extends ModelTemplateDragon {
    public ModelIgnibus() {
        this(1.0F);
    }

    public ModelIgnibus(float shadowSize) {
        // Load Model:
        this.initModel("ignibus", LycanitesMobs.modInfo, "entity/ignibus");

        // Looking:
        this.lookHeadScaleX = 0.5f;
        this.lookHeadScaleY = 0.5f;
        this.lookNeckScaleX = 0.5f;
        this.lookNeckScaleY = 0.5f;

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[] {0.0F, -0.25F, 0.0F};
    }

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Jumping/Flying:
        if(entity != null && !entity.onGround()) {
            RideableCreatureEntity rideableCreatureEntity = (RideableCreatureEntity)entity;

            if (rideableCreatureEntity.hasRiderTarget()) {
                if (partName.equals("body")) {
                    this.rotate(0, 0, 0);
                }
                if (partName.equals("neck")) {
                    this.rotate(70, 0, 0);
                }
            }

            else {
                if (partName.equals("body")) {
                    this.rotate(-45 * (1 - distance), 0, 0);
                }
                if (partName.equals("neck")) {
                    this.rotate(50, 0, 0);
                }
                if (partName.equals("head")) {
                    this.rotate(20 * (1 - distance), 0, 0);
                }
                if (partName.equals("legleftback") || partName.equals("legrightback")) {
                    this.rotate(20 * (1 - distance), 0, 0);
                }
                if (partName.equals("legleftfront")) {
                    this.rotate((float) ((40 * (1 - distance)) + Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F)), 120 * (1 - distance), (float) ((-40 * (1 - distance)) + Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
                }
                if (partName.equals("legrightfront")) {
                    this.rotate((float) ((40 * (1 - distance)) + Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F)), -120 * (1 - distance), (float) ((40 * (1 - distance)) + Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
                }
                if (partName.contains("foot")) {
                    this.rotate(90 * (1 - distance), 0, 0);
                }
                if (partName.contains("tail")) {
                    this.rotate(20 * (1 - distance), 0, 0);
                }
            }
        }
    }
}
