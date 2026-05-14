package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelVentoraptor extends ModelTemplateBiped {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelVentoraptor() {
        this(1.0F);
    }

    public ModelVentoraptor(float shadowSize) {
        // Load Model:
        this.initModel("Ventoraptor", LycanitesMobs.modInfo, "entity/ventoraptor");

        // Trophy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[]{0.0F, -0.1F, -0.3F};

        // Tail:
        this.tailScaleX = 1.5F;
        this.tailScaleY = 0.1F;
    }

    // ==================================================
    //                 Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        if (partName.contains("claw")) {
            float clawX = (float) Math.toDegrees(Mth.cos((loop + time) * 0.1F) * 0.2F);
            this.rotate(clawX, 0, 0);
        }
    }
}
