package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelVorach extends ModelTemplateBiped {

    public ModelVorach() {
        this(1.0F);
    }

    public ModelVorach(float shadowSize) {
        this.initModel("vorach", LycanitesMobs.modInfo, "entity/vorach");

        this.trophyScale = 1.2F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
        this.walkSwing = 0.3F;
    }

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Walking:
        if (entity == null || entity.onGround() || entity.isInWater()) {
            if (partName.equals("legback"))
                this.rotate((float) Math.toDegrees(Mth.cos(time * 0.4F - (float) Math.PI) * 1.4F * distance), 0, 0);
        }
    }
}
