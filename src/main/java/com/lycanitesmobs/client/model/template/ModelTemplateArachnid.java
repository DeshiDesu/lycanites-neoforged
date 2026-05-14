package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelTemplateArachnid extends CreatureObjModel {

    // ==================================================
    //                 Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
        float pi = (float)Math.PI;
        float posX = 0F;
        float posY = 0F;
        float posZ = 0F;
        float rotX = 0F;
        float rotY = 0F;
        float rotZ = 0F;

        // Idle:
        if(partName.equals("mouthleft"))
            rotate((float)-Math.toDegrees(Mth.cos(loop * 0.09F + (float)Math.PI) * 0.05F - 0.05F), 0.0F, 0.0F);
        if(partName.equals("mouthright"))
            rotate((float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
        if(partName.equals("armleft")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * 0.25F) * 0.1F);
            rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(partName.equals("armright")) {
            rotZ += Math.toDegrees(Mth.cos(loop * 0.25F) * 0.1F);
            rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(partName.equals("tail")) {
            rotX = (float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
            rotY = (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
        }
        if(partName.equals("wingleft")) {
            rotX = 20;
            rotX -= Math.toDegrees(Mth.sin(loop * 3.2F) * 0.6F);
            rotZ -= Math.toDegrees(Mth.sin(loop * 3.2F) * 0.6F);
        }
        if(partName.equals("wingright")) {
            rotX = 20;
            rotX -= Math.toDegrees(Mth.sin(loop * 3.2F) * 0.6F);
            rotZ -= Math.toDegrees(Mth.sin(loop * 3.2F + (float)Math.PI) * 0.6F);
        }

        // Walking:
        float walkSwing = 0.6F;
        if(partName.equals("legrightfront") || partName.equals("legrightmiddleback") || partName.equals("legleftmiddlefront") || partName.equals("legleftback")) {
            rotX += Math.toDegrees(Mth.cos(time * 0.3331F + (float)Math.PI) * walkSwing * distance);
            rotZ += Math.toDegrees(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance);
        }
        if(partName.equals("legleftfront") || partName.equals("legleftmiddleback") || partName.equals("legrightmiddlefront") || partName.equals("legrightback")) {
            rotX += Math.toDegrees(Mth.cos(time * 0.3331F) * walkSwing * distance);
            rotZ += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);
        }

        // Flying:
        if(entity != null && !entity.onGround() && !entity.isInWater()) {
            if(entity instanceof BaseCreatureEntity) {
                BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
                if(entityCreature.isFlying()) {
                    float bob = -Mth.sin(loop * 0.2F) * 0.3F;
                    if(bob < 0)
                        bob = -bob;
                    posY += bob;
                }
            }
        }

        // Attack:
        if(partName.equals("mouth"))
            rotX += 30 * this.getAttackProgress();
        if(partName.equals("armleft"))
            rotY += -25 * this.getAttackProgress();
        if(partName.equals("armright"))
            rotY += 25 * this.getAttackProgress();
        if(partName.equals("tail"))
            rotX += 45 * this.getAttackProgress();

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
