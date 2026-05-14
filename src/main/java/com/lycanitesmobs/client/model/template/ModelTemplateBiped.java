package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelTemplateBiped extends CreatureObjModel {
    protected float walkSwing = 0.6F;
    protected float legScaleX = 1F;
    protected float tailScaleX = 1F;
    protected float tailScaleY = 1F;
    protected float flightBobScale = 1F;
    protected float mouthScale = 1F;
    protected float mouthRate = 1F;
    protected float wingScale = 1F;

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

        BaseCreatureEntity creatureEntity = null;
        if(entity instanceof BaseCreatureEntity)
            creatureEntity = (BaseCreatureEntity)entity;

        // Idle:
        if (partName.equals("mouth") || partName.contains("mouthbottom")) {
            this.rotate((float)-Math.toDegrees(Mth.cos(loop * this.mouthRate * 0.09F) * 0.1F - 0.1F) * this.mouthScale, 0.0F, 0.0F);
        }
        if (partName.contains("mouthtop")) {
            this.rotate((float)Math.toDegrees(Mth.cos(loop * this.mouthRate * 0.09F) * 0.1F - 0.1F) * this.mouthScale, 0.0F, 0.0F);
        }
        if (partName.contains("mouthleft")) {
            this.rotate(0.0F, (float)-Math.toDegrees(Mth.cos(loop * this.mouthRate * 0.09F) * 0.1F - 0.1F) * this.mouthScale, 0.0F);
        }
        if (partName.contains("mouthright")) {
            this.rotate(0.0F, (float)Math.toDegrees(Mth.cos(loop * this.mouthRate * 0.09F) * 0.1F - 0.1F) * this.mouthScale, 0.0F);
        }
        if(partName.equals("neck")) {
            this.rotate((float) -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.1F - 0.05F), 0.0F, 0.0F);
        }
        if(partName.contains("armleft")) {
            rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(partName.contains("armright")) {
            rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
            rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
        }
        if(entity == null || entity.onGround() || entity.isInWater()) {
            if(partName.contains("wingleft")) {
                rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
                rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
            }
            if(partName.contains("wingright")) {
                rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
                rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
            }
        }

        // Tail:
        if(partName.contains("tail")) {
            float sine = 0;
            if(partName.equals("tail.002") || partName.equals("tail02")) {
                sine = 1;
            }
            else if(partName.equals("tail.003") || partName.equals("tail03")) {
                sine = 2;
            }
            else if(partName.equals("tail.004")) {
                sine = 3;
            }
            else if(partName.equals("tail.005")) {
                sine = 4;
            }
            else if(partName.equals("tail.006")) {
                sine = 5;
            }
            else if(partName.equals("tail.007")) {
                sine = 6;
            }
            sine = (Mth.sin(sine / 6) - 0.5F);
            float tailRotX = (float)-Math.toDegrees(Mth.cos((loop + time) * 0.1F) * 0.05F - 0.05F);
            float tailRotY = (float)-Math.toDegrees(Mth.cos((loop + time) * sine * 0.1F) * 0.4F);
            tailRotX += Math.toDegrees(Mth.cos(time * 0.5F) * 0.1F * distance * 0.5F);
            tailRotY += Math.toDegrees(Mth.cos(time * 0.25F) * distance * 0.5F);
            this.rotate(tailRotX * this.tailScaleX, tailRotY * this.tailScaleY, 0);
        }

		// Fingers:
		if(partName.contains("finger")) {
			if(partName.contains("thumb")) {
				this.rotate(-(float) Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F - 0.2F), 0, 0);
			}
			else {
				this.rotate((float) Math.toDegrees(Mth.cos(loop * 0.2F) * 0.2F - 0.2F), 0, 0);
			}
		}

        // Walking:
        if(entity == null || entity.onGround() || entity.isInWater()) {
            if(partName.contains("armleft") || partName.equals("wingright")) {
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing) * 1.0F * distance * 0.5F * this.legScaleX);
                if(!partName.contains("lower")) {
                    rotZ -= Math.toDegrees(Mth.cos(time * this.walkSwing) * 0.5F * distance * 0.5F * this.legScaleX);
                }
            }
            if(partName.contains("armright") || partName.equals("wingleft")) {
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing + (float)Math.PI) * 1.0F * distance * 0.5F * this.legScaleX);
                if(!partName.contains("lower")) {
                    rotZ += Math.toDegrees(Mth.cos(time * this.walkSwing + (float) Math.PI) * 0.5F * distance * 0.5F * this.legScaleX);
                }
            }

            if(partName.equals("legleft"))
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing + (float)Math.PI) * 1.4F * distance * this.legScaleX);
            if(partName.equals("legright"))
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing) * 1.4F * distance * this.legScaleX);

            if(partName.equals("legleftupper"))
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing + (float)Math.PI) * 1.4F * (distance * 0.5F) * this.legScaleX);
            if(partName.equals("legleftlower"))
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing + (float)Math.PI) * 1.4F * (distance * 0.5F) * this.legScaleX);
            if(partName.equals("legrightupper"))
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing) * 1.4F * (distance * 0.5F) * this.legScaleX);
            if(partName.equals("legrightlower"))
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing) * 1.4F * (distance * 0.5F) * this.legScaleX);

            if(partName.contains("legleft0"))
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing + (float)Math.PI) * 0.6F * distance * this.legScaleX);
            if(partName.contains("legright0"))
                rotX += Math.toDegrees(Mth.cos(time * this.walkSwing) * 0.6F * distance * this.legScaleX);
        }

        // Jumping/Flying:
        if(entity != null && !entity.onGround() && !entity.isInWater() && (creatureEntity == null || !creatureEntity.hasPerchTarget())) {
            if(partName.contains("wingleft")) {
                rotX = 20;
                rotX -= Math.toDegrees(Mth.sin(loop * 0.4F * this.wingScale) * 0.6F);
                rotZ -= Math.toDegrees(Mth.sin(loop * 0.4F * this.wingScale) * 0.6F);
            }
            if(partName.contains("wingright")) {
                rotX = 20;
                rotX -= Math.toDegrees(Mth.sin(loop * 0.4F * this.wingScale) * 0.6F);
                rotZ -= Math.toDegrees(Mth.sin(loop * 0.4F * this.wingScale + (float)Math.PI) * 0.6F);
            }
            if(partName.contains("legleft")) {
                rotZ += Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
                rotX += Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
            }
            if(partName.contains("legright")) {
                rotZ -= Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F + 0.05F);
                rotX -= Math.toDegrees(Mth.sin(loop * 0.067F) * 0.05F);
            }
            if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isFlying()) {
                if (partName.equals("body")) {
                    float bob = Mth.sin(loop * 0.4F) * 0.15F;
                    posY += bob * this.flightBobScale;
                }
            }
        }

        // Attack:
        if(creatureEntity != null && creatureEntity.hasAttackTarget()) {
            if (partName.equals("mouth") || partName.contains("mouthbottom")) {
                rotX += 20.0F * this.getAttackProgress() * this.mouthScale;
            }
            if (partName.contains("mouthtop")) {
                rotX -= 20.0F * this.getAttackProgress() * this.mouthScale;
            }
            if (partName.contains("mouthleft")) {
                rotY -= 20.0F * this.getAttackProgress() * this.mouthScale;
            }
            if (partName.contains("mouthright")) {
                rotY += 20.0F * this.getAttackProgress() * this.mouthScale;
            }
            if (partName.contains("armleft") || partName.contains("armright")) {
                rotX -= 80.0F * this.getAttackProgress();
            }
        }

        // Riding:
        if(entity.isPassenger()) {
            if (partName.equals("legleft"))
                rotX -= 50;
            if (partName.equals("legright"))
                rotX -= 50;
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
