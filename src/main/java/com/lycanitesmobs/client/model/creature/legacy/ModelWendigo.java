package com.lycanitesmobs.client.model.creature.legacy;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelWendigo extends CreatureObjModelOld {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelWendigo() {
        this(1.0F);
    }

    public ModelWendigo(float shadowSize) {
        // Load Model:
        this.initModel("Wendigo", LycanitesMobs.modInfo, "entity/wendigo");

        // Set Rotation Centers:
        setPartCenter("head", 0F, 4.392F, -0.216F);
        setPartCenter("mouth", 0F, 4.4568F, -1.1376F);
        setPartCenter("body", 0F, 3.908F, 0.3F);
        setPartCenter("armleft", 0.504F, 4.032F, -0.036F);
        setPartCenter("armright", -0.504F, 4.032F, -0.036F);
        setPartCenter("legleft", 0.216F, 2.592F, 0F);
        setPartCenter("legright", -0.216F, 2.592F, 0F);

        this.lockHeadY = true;

        // Tropy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
    }


    // ==================================================
    //                 Animate Part
    // ==================================================
    float maxLeg = 0F;

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
        float pi = (float) Math.PI;
        float posX = 0F;
        float posY = 0F;
        float posZ = 0F;
        float angleX = 0F;
        float angleY = 0F;
        float angleZ = 0F;
        float rotation = 0F;
        float rotX = 0F;
        float rotY = 0F;
        float rotZ = 0F;

        // Looking:
        if (partName.equals("mouth")) {
            this.centerPartToPart("mouth", "head");
            if (!this.lockHeadX)
                this.doRotate((float) Math.toDegrees(lookX / (180F / (float) Math.PI)), 0, 0);
            if (!this.lockHeadY)
                this.doRotate(0, (float) Math.toDegrees(lookY / (180F / (float) Math.PI)), 0);
            this.uncenterPartToPart("mouth", "head");
        }

        // Idle:
        if (partName.equals("mouth")) {
            rotX += (Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
        }
        if (partName.equals("armleft")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if (partName.equals("armright")) {
            rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }

        // Walking:
        if (entity == null || entity.onGround() || entity.isInWater()) {
            float walkSwing = 0.6F;
            if (partName.equals("armleft")) {
                rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 1.0F * distance * 0.5F);
                rotZ -= Math.toDegrees(Mth.cos(time * walkSwing) * 0.5F * distance * 0.5F);
            }
            if (partName.equals("armright")) {
                rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float) Math.PI) * 1.0F * distance * 0.5F);
                rotZ += Math.toDegrees(Mth.cos(time * walkSwing + (float) Math.PI) * 0.5F * distance * 0.5F);
            }
            if (partName.equals("legleft"))
                rotX += Math.toDegrees(Mth.cos(time * walkSwing + (float) Math.PI) * 1.4F * distance);
            if (partName.equals("legright"))
                rotX += Math.toDegrees(Mth.cos(time * walkSwing) * 1.4F * distance);
        }

        // Attack:
        if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity) entity).isAttackOnCooldown()) {
            if (partName.equals("armleft") || partName.equals("armright"))
                rotX -= 65.0F;
            if (partName.equals("armleft"))
                rotY -= 20.0F;
            if (partName.equals("armright"))
                rotY += 20.0F;
        }

        // Apply Animations:
        this.doAngle(rotation, angleX, angleY, angleZ);
        this.doRotate(rotX, rotY, rotZ);
        this.doTranslate(posX, posY, posZ);
    }
}
