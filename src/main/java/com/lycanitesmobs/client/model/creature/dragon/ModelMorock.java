package com.lycanitesmobs.client.model.creature.dragon;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateDragon;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ModelMorock extends ModelTemplateDragon {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelMorock() {
        this(1.0F);
    }

    public ModelMorock(float shadowSize) {
        // Load Model:
        this.initModel("morock", LycanitesMobs.modInfo, "entity/morock");

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

        float rotX = 0F;
        float rotY = 0F;
        float rotZ = 0F;

        // Walking:
        if(entity != null && entity.onGround()) {
            float walkIdle = Mth.sin(loop * 0.1F);
            if(partName.equals("armleft02")) {
                rotZ += 160 + Math.toDegrees(walkIdle * 0.05F);
            }
            if(partName.equals("armright02")) {
                rotZ -= 160 + Math.toDegrees(walkIdle * 0.05F);
            }
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
    }
}
