package com.lycanitesmobs.client.model.creature.insect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelVespidQueen extends ModelTemplateInsect {

    public ModelVespidQueen() {
        this(1.0F);
    }

    public ModelVespidQueen(float shadowSize) {
        this.initModel("vespidqueen", LycanitesMobs.modInfo, "entity/vespidqueen");
        this.mouthScaleX = 2;
        this.mouthScaleY = 2;
    }
}
