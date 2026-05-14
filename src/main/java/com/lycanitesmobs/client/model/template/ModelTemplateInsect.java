package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelTemplateInsect extends CreatureObjModel {
    public double mouthScaleX = 0F;
    public double mouthScaleY = 1.0F;

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
        if(partName.equals("mouthleft") || partName.equals("antennaleft") || partName.equals("antennaleftfront") || partName.equals("antennarightback")) {
            rotX -= this.mouthScaleX * -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F + 0.05F);
            rotY += this.mouthScaleY * -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F - 0.05F);
        }
        if(partName.equals("mouthright") || partName.equals("antennaright") || partName.equals("antennarightfront") || partName.equals("antennaleftback")) {
            rotX += this.mouthScaleX * -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F - 0.05F);
            rotY -= this.mouthScaleY * -Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F - 0.05F);
        }
        if(partName.equals("tail")) {
            rotX = (float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.05F);
            rotY = (float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F);
        }
        if(partName.equals("wingleft") || partName.equals("winglefttop") || partName.equals("wingleftbottom") || partName.equals("wingrightmiddle")) {
            rotX = 20;
            rotX -= Math.toDegrees(Mth.sin(loop * 3.2F) * 0.6F);
            rotZ -= Math.toDegrees(Mth.sin(loop * 3.2F) * 0.6F);
        }
        if(partName.equals("wingright") || partName.equals("wingrighttop") || partName.equals("wingrightbottom") || partName.equals("wingleftmiddle")) {
            rotX = 20;
            rotX -= Math.toDegrees(Mth.sin(loop * 3.2F) * 0.6F);
            rotZ -= Math.toDegrees(Mth.sin(loop * 3.2F + (float)Math.PI) * 0.6F);
        }

        // Walking:
        float walkSwing = 0.3F;
        if(partName.equals("legrightfront") || partName.equals("legleftmiddle") || partName.equals("legrightback")) {
            rotX += Math.toDegrees(Mth.cos(time * 0.3331F + (float)Math.PI) * walkSwing * distance);
            rotZ += Math.toDegrees(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance);
        }
        if(partName.equals("legleftfront") || partName.equals("legrightmiddle") || partName.equals("legleftback")) {
            rotX += Math.toDegrees(Mth.cos(time * 0.3331F) * walkSwing * distance);
            rotZ += Math.toDegrees(Mth.cos(time * 0.6662F) * walkSwing * distance);
        }

        // Flying:
        if(entity != null && !entity.onGround() && !entity.isInWater()) {
            if(entity instanceof BaseCreatureEntity) {
                BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
                if(entityCreature.isFlying() && partName.equals("body")) {
                    float bob = -Mth.sin(loop * 0.2F) * 0.3F;
                    if(bob < 0)
                        bob = -bob;
                    posY += bob;
                }
            }
        }

        // Attack:
        if(partName.equals("leftmouth")) {
            rotX += this.mouthScaleX * 15F * this.getAttackProgress();
            rotY -= this.mouthScaleY * 15F * this.getAttackProgress();
        }
        if(partName.equals("rightmouth")) {
            rotX += this.mouthScaleX * 15F * this.getAttackProgress();
            rotY += this.mouthScaleY * 15F * this.getAttackProgress();
        }
        if(partName.equals("tail")) {
            rotX += 45 * this.getAttackProgress();
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
