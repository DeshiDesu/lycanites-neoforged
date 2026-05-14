package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelTemplateDragon extends CreatureObjModel {
    public boolean foldWings = true;

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
        if(partName.equals("mouth") || partName.equals("mouthbottom")) {
            this.rotate((float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F - 0.1F), 0.0F, 0.0F);
        }
        if(partName.equals("mouthtop")) {
            this.rotate((float)+Math.toDegrees(Mth.cos(loop * 0.1F) * 0.1F - 0.1F), 0.0F, 0.0F);
        }
        if(partName.equals("neck")) {
            this.rotate((float) -Math.toDegrees(Mth.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
        }
        if(partName.equals("tail") || partName.equals("tail01") || partName.equals("tail02")) {
            rotX += (float)-Math.toDegrees(Mth.cos(loop * 0.1F) * 0.05F - 0.25F);
            rotY += (float)-Math.toDegrees(Mth.cos(loop * 0.05F) * 0.05F);
        }

        // Walking:
        if(entity != null && entity.onGround()) {
            float walkLoop = Mth.cos(time * 0.3F);
            float walkLoopRev = Mth.cos(time * 0.3F + (float)Math.PI);
            float walkIdle = Mth.sin(loop * 0.1F);
            float walkIdleRev = Mth.sin(loop * 0.1F + (float)Math.PI);
            float walkSwing = 0.6F;
            if (partName.equals("legleft") || partName.equals("legrightfront") || partName.equals("armrightfront01") || partName.equals("legleftback")) {
                rotX += Math.toDegrees(walkLoopRev * walkSwing * distance);
            }
            if (partName.equals("legright") || partName.equals("legleftfront") || partName.equals("armleftfront01") || partName.equals("legrightback")) {
                rotX += Math.toDegrees(walkLoop * walkSwing * distance);
            }
            if(partName.equals("armleft01")) {
                rotX += Math.toDegrees(walkLoop * (walkSwing / 2) * distance);
                rotY -= 10;
                rotZ -= 25;
            }
            if(partName.equals("armright01")) {
                rotX += Math.toDegrees(walkLoopRev * (walkSwing / 2) * distance);
                rotY += 10;
                rotZ += 25;
            }

            if(this.foldWings) {
                if(partName.equals("wingleft01")) {
                    rotX += Math.toDegrees(walkIdle * 0.1F);
                    rotZ -= Math.toDegrees(walkIdle * 0.1F);
                    rotZ += 80;
                }
                if(partName.equals("wingright01")) {
                    rotX += Math.toDegrees(walkIdle * 0.1F);
                    rotZ -= Math.toDegrees(walkIdleRev * 0.1F);
                    rotZ -= 80;
                }
                if(partName.equals("wingleft02")) {
                    rotZ += Math.toDegrees(walkIdle * 0.1F);
                    rotZ -= 170;
                    rotX -= 10;
                }
                if(partName.equals("wingright02")) {
                    rotZ -= Math.toDegrees(walkIdle * 0.1F);
                    rotZ += 170;
                    rotX -= 10;
                }

                if (partName.equals("wingleftupper")) {
                    rotX += 20F + Math.toDegrees(walkIdle * 0.1F);
                    rotY += 60F;
                    rotZ -= Math.toDegrees(walkIdle * 0.1F);
                }
                if (partName.equals("wingrightupper")) {
                    rotX += 20F + Math.toDegrees(walkIdle * 0.1F);
                    rotY -= 60F;
                    rotZ -= Math.toDegrees(walkIdleRev * 0.1F);
                }
                if (partName.equals("wingleftlower")) {
                    rotZ += Math.toDegrees((walkIdle * 0.1F) - 0.1F) - 35F;
                }
                if (partName.equals("wingrightlower")) {
                    rotZ -= Math.toDegrees((walkIdle * 0.1F) - 0.1F) - 35F;
                }
            }
        }

        // Jumping/Flying:
        if(entity != null && !entity.onGround()) {
            float flightLoop = Mth.sin(loop * 0.4F);
            float flightLoopRev = Mth.sin(loop * 0.4F + (float)Math.PI);
            if(partName.equals("body")) {
                if(entity.getPassengers().isEmpty()) {
                    posY += flightLoop / 2;
                }
            }
            if(partName.equals("head")) {
                rotX -= Math.toDegrees(flightLoop * 0.05F);
            }

            if(partName.equals("legleftfront") || partName.equals("legrightfront")) {
                rotX += 35;
                rotX += Math.toDegrees(flightLoop * 0.05F);
            }
            if(partName.equals("legleftback") || partName.equals("legrightback")) {
                rotX += 35;
                rotX -= Math.toDegrees(flightLoop * 0.05F);
            }

            if(this.foldWings) {
                if (partName.equals("wingleft")) {
                    rotX -= Math.toDegrees(flightLoop * 0.6F);
                    rotZ -= Math.toDegrees(flightLoop * 0.6F);
                }
                if (partName.equals("wingright")) {
                    rotX -= Math.toDegrees(flightLoop * 0.6F);
                    rotZ -= Math.toDegrees(flightLoopRev * 0.6F);
                }

                if (partName.equals("wingleft01") || partName.equals("armleft01") || partName.equals("wingleftupper")) {
                    rotX -= Math.toDegrees(flightLoop * 0.3F);
                    rotZ -= Math.toDegrees(flightLoop * 0.3F);
                }
                if (partName.equals("wingright01") || partName.equals("armright01") || partName.equals("wingrightupper")) {
                    rotX -= Math.toDegrees(flightLoop * 0.3F);
                    rotZ -= Math.toDegrees(flightLoopRev * 0.3F);
                }

                if (partName.equals("wingleft02") || partName.equals("armleft02")) {
                    rotZ -= Math.toDegrees(flightLoop * 0.3F);
                }
                if (partName.equals("wingright02") || partName.equals("armright02")) {
                    rotZ -= Math.toDegrees(flightLoopRev * 0.3F);
                }
            }
        }

        // Attack:
        if(partName.equals("mouth") || partName.equals("mouthbottom")) {
            rotX -= 15.0F * this.getAttackProgress();
        }
        else if(partName.equals("mouthtop")) {
            rotX += 15.0F * this.getAttackProgress();
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
