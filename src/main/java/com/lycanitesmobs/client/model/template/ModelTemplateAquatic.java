package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelTemplateAquatic extends CreatureObjModel {

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

        // Idle:
        if(partName.equals("body")) {
            this.rotate(0, (float) -Math.toDegrees(Mth.cos(loop * 0.2F) * 0.05F - 0.05F), 0);
        }
        if(partName.equals("mouth")) {
            this.rotate(5F - (float)-Math.toDegrees(Mth.cos(loop * -0.1F) * 0.1F), 0.0F, 0.0F);
        }
        if(partName.equals("neck")) {
            this.rotate((float) -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
        }
        float speed = 0.5f;
        if(distance > 0)
            speed = 1f;
        if(partName.equals("armleft") || partName.equals("legleftfront") || partName.equals("legrightback")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * 0.25F * speed) * 0.1F);
            rotX -= Math.toDegrees(Mth.sin(loop * 0.067F * speed) * 0.05F);
        }
        if(partName.equals("armright") || partName.equals("legrightfront") || partName.equals("legleftback")) {
            rotZ += Math.toDegrees(Mth.cos(loop * 0.25F * speed) * 0.1F);
            rotX += Math.toDegrees(Mth.sin(loop * 0.067F * speed) * 0.05F);
        }
        if(partName.equals("tail")) {
            rotY += (float)-Math.toDegrees(Mth.cos(loop * 0.25f * speed) * 0.25F);
        }

        // Walking:
        if(entity == null || entity.isInWater()) {
            if(partName.equals("body")) {
                rotY += (float)-Math.toDegrees(Mth.cos(time * 0.1F) * 0.2F);
            }
        }

        // Jumping/Flying:
        if(entity != null && !entity.onGround() && !entity.isInWater()) {
            if(partName.equals("wingleft")) {
                rotX = 20;
                rotX -= Math.toDegrees(Mth.sin(loop * 0.4F) * 0.6F);
                rotZ -= Math.toDegrees(Mth.sin(loop * 0.4F) * 0.6F);
            }
            if(partName.equals("wingright")) {
                rotX = 20;
                rotX -= Math.toDegrees(Mth.sin(loop * 0.4F) * 0.6F);
                rotZ -= Math.toDegrees(Mth.sin(loop * 0.4F + (float)Math.PI) * 0.6F);
            }
            if(partName.equals("legleftfront") || partName.equals("legleftback"))
                rotZ -= 25;
            if(partName.equals("legrightfront") || partName.equals("legrightback"))
                rotZ += 25;
        }

        // Attack:
        if(partName.equals("mouth")) {
            rotX += 15 * this.getAttackProgress();
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
