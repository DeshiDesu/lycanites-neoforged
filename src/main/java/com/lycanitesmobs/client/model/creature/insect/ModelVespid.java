package com.lycanitesmobs.client.model.creature.insect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelVespid extends ModelTemplateInsect {

    public ModelVespid() {
        this(1.0F);
    }

    public ModelVespid(float shadowSize) {
        this.initModel("vespid", LycanitesMobs.modInfo, "entity/vespid");
        this.mouthScaleX = 2;
        this.mouthScaleY = 2;
    }
}
