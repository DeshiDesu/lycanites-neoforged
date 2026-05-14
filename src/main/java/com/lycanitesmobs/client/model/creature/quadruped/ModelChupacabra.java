package com.lycanitesmobs.client.model.creature.quadruped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelChupacabra extends ModelTemplateQuadruped {

    public ModelChupacabra() {
        this(1.0F);
    }

    public ModelChupacabra(float shadowSize) {
        this.initModel("chupacabra", LycanitesMobs.modInfo, "entity/chupacabra");

        this.trophyScale = 0.8F;
        this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
    }

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Attack:
        if (partName.equals("lefleftfront") || partName.equals("lefrightfront")) {
            rotate(-75.0F * this.getAttackProgress(), 0.0F, 0.0F);
        }
    }
}
