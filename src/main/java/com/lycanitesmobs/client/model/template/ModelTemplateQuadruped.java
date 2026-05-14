package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelTemplateQuadruped extends CreatureObjModel {
    protected float legAnimationScale = 1;

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
        if(partName.equals("mouth")) {
            this.rotate((float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F - 0.1F), 0.0F, 0.0F);
        }
        if(partName.contains("neck")) {
            this.rotate((float) -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
        }
        if(partName.contains("armleft")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(partName.contains("armright")) {
            rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(partName.contains("tail")) {
            rotX = (float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
            rotY = (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
        }
        if(partName.equals("earleft")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * 0.18F) * 0.05F + 0.05F);
            rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(partName.equals("earright")) {
            rotZ += Math.toDegrees(Mth.cos(loop * 0.18F) * 0.05F + 0.05F);
            rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }

        // Walking:
        if (this.legAnimationScale != 0) {
            float walkSwing = 0.6F * this.legAnimationScale;
            if (partName.contains("armleft") || partName.equals("wingright")) {
                rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 1.0F * distance * 0.5F);
                rotZ -= Math.toDegrees(Mth.cos(time * walkSwing) * 0.5F * distance * 0.5F);
            }
            if (partName.contains("armright") || partName.equals("wingleft")) {
                rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float) Math.PI) * 1.0F * distance * 0.5F);
                rotZ += Math.toDegrees(Mth.cos(time * walkSwing + (float) Math.PI) * 0.5F * distance * 0.5F);
            }
            if (partName.contains("legrightfront") || partName.contains("legleftback") || partName.contains("wingright"))
                rotX += Math.toDegrees(Mth.cos(time * 0.6662F + (float) Math.PI) * walkSwing * distance);
            if (partName.contains("legleftfront") || partName.contains("legrightback") || partName.contains("wingleft"))
                rotX += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);
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
            if(partName.contains("legleftback") || partName.contains("legrightback"))
                rotX += 25;
            if(partName.contains("legleftfront") || partName.contains("legrightfront"))
                rotX -= 25;
        }

        // Attack:
        if(partName.equals("mouth")) {
            rotX -= 15.0F * this.getAttackProgress();
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
