package com.lycanitesmobs.client.model.creature.generic;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelTriffid extends CreatureObjModel {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelTriffid() {
        this(1.0F);
    }

    public ModelTriffid(float shadowSize) {
        // Load Model:
        this.initModel("triffid", LycanitesMobs.modInfo, "entity/triffid");

        // Looking:
        this.lookHeadScaleX = 0.5f;
        this.lookHeadScaleY = 0.5f;
        this.lookBodyScaleX = 0.5f;
        this.lookBodyScaleY = 0.5f;

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[] {0.0F, -0.25F, 0.0F};
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

        // Idle:
        if(partName.equals("mouthtop")) {
            rotX -= Mth.cos(loop * 0.2F) * 4F;
        }
        if(partName.equals("mouthbottom")) {
            rotX += Mth.cos(loop * 0.2F) * 4F;
        }

        float animationScaleZ = 0.09F;
        float animationScaleY = 0.07F;
        float animationScaleX = 0.05F;
        float animationDistanceZ = 0.25F;
        float animationDistanceY = 0.2F;
        float animationDistanceX = 0.15F;
        if(partName.equals("body")) {
            rotZ += (animationDistanceZ / 2) - Math.toDegrees(Mth.cos(loop * 0.02F) * (animationDistanceZ / 2));
            rotX += (animationDistanceX / 2) - Math.toDegrees(Mth.sin(loop * 0.01F) * (animationDistanceX / 2));
        }
        if(partName.equals("tentacleleftmiddle")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
            rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
            rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
        }
        if(partName.equals("tentaclerightfront")) {
            rotZ += Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
            rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
            rotX += Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
        }
        if(partName.equals("tentaclerightback")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
            rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
            rotX += Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
        }
        if(partName.equals("armleftmiddle")) {
            rotZ += Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
            rotY -= Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
            rotX += Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
        }
        if(partName.equals("armleftfront")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
            rotY += Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
            rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
        }
        if(partName.equals("armrightback")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
            rotY -= Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
            rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
        }
        if(partName.equals("armrightmiddle")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ);
            rotY -= Math.toDegrees(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY);
            rotX -= Math.toDegrees(Mth.sin(loop * animationScaleX) * animationDistanceX);
        }

        // Attack:
        if(partName.contains("body"))
            rotX += 50F * this.getAttackProgress();
        else if(partName.contains("head"))
            rotX -= 50F * this.getAttackProgress();
        else if(partName.equals("mouthtop"))
            rotX -= 20F * this.getAttackProgress();
        else if(partName.equals("mouthbottom"))
            rotX += 20F * this.getAttackProgress();
        else
            rotX -= 50F * this.getAttackProgress();

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
