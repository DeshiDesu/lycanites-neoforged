package com.lycanitesmobs.client.model.creature.legacy;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelWarg extends CreatureObjModelOld {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelWarg() {
        this(1.0F);
    }

    public ModelWarg(float shadowSize) {
        // Load Model:
        this.initModel("warg", LycanitesMobs.modInfo, "entity/warg");


        // Set Rotation Centers:
        setPartCenter("head", 0F, 1.1F, 0.9F);
        setPartCenter("mouth", 0F, 1.4F, 1.0F);

        setPartCenter("body", 0F, 1.0F, 0F);

        setPartCenter("legleftfront", 0.38F, 1.0F, 0.5F);
        setPartCenter("legrightfront", -0.38F, 1.0F, 0.5F);
        setPartCenter("legleftback", 0.28F, 1.0F, -0.3F);
        setPartCenter("legrightback", -0.28F, 1.0F, -0.3F);

        setPartCenter("tail", 0F, 1.0F, -0.8F);

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
    }


    // ==================================================
    //                    Animate Part
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

        // Looking (Mouth):
        if (partName.equals("mouth")) {
            this.centerPartToPart("mouth", "head");
            if (!lockHeadX)
                this.doRotate((float) Math.toDegrees(lookX / (180F / (float) Math.PI)), 0, 0);
            if (!lockHeadY)
                this.doRotate(0, (float) Math.toDegrees(lookY / (180F / (float) Math.PI)), 0);
            this.uncenterPartToPart("mouth", "head");
        }

        // Idle:
        if (partName.equals("mouth")) {
            this.doRotate((float) -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F - 0.1F), 0.0F, 0.0F);
        }
        if (partName.equals("tail")) {
            rotX = (float) -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
            rotY = (float) -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
        }

        // Walking:
        float walkSwing = 0.6F;
        if (partName.equals("legrightfront") || partName.equals("legleftback"))
            rotX += Math.toDegrees(Mth.cos(time * 0.6662F + (float) Math.PI) * walkSwing * distance);
        if (partName.equals("legleftfront") || partName.equals("legrightback"))
            rotX += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);

        // Jump:
        if (entity != null && !entity.onGround() && !entity.isInWater()) {
            if (partName.equals("legleftback") || partName.equals("legrightback"))
                rotX += 25;
            if (partName.equals("legleftfront") || partName.equals("legrightfront"))
                rotX -= 25;
        }

        // Attack:
        if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity) entity).isAttackOnCooldown()) {
            if (partName.equals("mouth")) {
                rotX -= 15.0F;
            }
        }

        // Apply Animations:
        this.doAngle(rotation, angleX, angleY, angleZ);
        this.doRotate(rotX, rotY, rotZ);
        this.doTranslate(posX, posY, posZ);
    }


    // ==================================================
    //              Rotate and Translate
    // ==================================================
    @Override
    public void childScale(String partName) {
        if (partName.equals("head") || partName.equals("mouth"))
            doTranslate(-(getPartCenter(partName)[0] / 2), -(getPartCenter(partName)[1] / 2), -(getPartCenter(partName)[2] / 2));
        else
            super.childScale(partName);
    }
}
