package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelTemplateElemental extends CreatureObjModel {

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
            this.rotate((float)-Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F - 0.1F), 0.0F, 0.0F);
        }
        if(partName.equals("armleft")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F);
            rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(partName.equals("armright")) {
            rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F);
            rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(partName.equals("body02")) {
            posX -= Mth.cos(loop * 0.09F) * 0.1F;
            posY += Mth.sin(loop * 0.067F) * 0.05F;
        }
        if(partName.equals("body03")) {
            posX += Mth.cos(loop * 0.09F) * 0.1F;
            posY -= Mth.sin(loop * 0.067F) * 0.05F;
        }
        if(partName.equals("body")) {
            float bob = -Mth.sin(loop * 0.1F) * 0.3F;
            posY += bob;
        }

        // Effects:
        if(partName.equals("effectouter")) {
            rotY += loop * 8;
        }
        if(partName.equals("effectinner")) {
            rotY -= loop * 8;
        }

        // Attack:
        if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
            BaseCreatureEntity baseCreatureEntity = (BaseCreatureEntity)entity;
            if(baseCreatureEntity.attackPhaseMax > 0) {
				if (partName.equals("armleft") && baseCreatureEntity.getAttackPhase() == 2)
					rotate(0.0F, -25.0F, 0.0F);
				if (partName.equals("armright") && baseCreatureEntity.getAttackPhase() != 2)
					rotate(0.0F, 25.0F, 0.0F);
			}
			else {
				if (partName.equals("armleft"))
					rotate(0.0F, -25.0F, 0.0F);
				if (partName.equals("armright"))
					rotate(0.0F, 25.0F, 0.0F);
			}
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
