package com.lycanitesmobs.client.model.creature.dragon;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateDragon;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelZoataur extends ModelTemplateDragon {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelZoataur() {
        this(1.0F);
    }

    public ModelZoataur(float shadowSize) {
        // Load Model:
        this.initModel("zoataur", LycanitesMobs.modInfo, "entity/zoataur");

        this.foldWings = false;

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.2F};
    }


    // ==================================================
    //                 Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Idle:
        float walkIdle = Mth.sin(loop * 0.1F);
        float walkIdleRev = Mth.sin(loop * 0.1F + (float) Math.PI);
        if (partName.equals("wingleft01")) {
            this.rotate(0, (float) Math.toDegrees(walkIdle * 0.1F), 0);
        }
        if (partName.equals("wingright01")) {
            this.rotate(0, (float) Math.toDegrees(walkIdle * 0.1F), 0);
        }
        if (partName.equals("wingleft02")) {
            this.rotate(0, -(float) Math.toDegrees(walkIdle * 0.1F), 0);
        }
        if (partName.equals("wingright02")) {
            this.rotate(0, -(float) Math.toDegrees(walkIdle * 0.1F), 0);
        }

        // Blocking:
        if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity) entity).isBlocking()) {
            if (partName.equals("wingleft01")) {
                this.rotate(0, -45, 0);
            } else if (partName.equals("wingleft02")) {
                this.rotate(0, -75, 0);
            } else if (partName.equals("wingright01")) {
                this.rotate(0, 45, 0);
            } else if (partName.equals("wingright02")) {
                this.rotate(0, 75, 0);
            }
        } else {
            if (partName.equals("wingleft01")) {
                this.rotate(35, 50, 0);
            } else if (partName.equals("wingleft02")) {
                this.rotate(0, 60, 0);
            } else if (partName.equals("wingright01")) {
                this.rotate(35, -50, 0);
            } else if (partName.equals("wingright02")) {
                this.rotate(0, -60, 0);
            }
        }
    }
}
