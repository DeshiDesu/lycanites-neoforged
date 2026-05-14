package com.lycanitesmobs.client.model.creature.biped;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelApollyon extends ModelTemplateBiped {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelApollyon() {
        this(1.0F);
    }

    public ModelApollyon(float shadowSize) {

        // Load Model:
        this.initModel("apollyon", LycanitesMobs.modInfo, "entity/apollyon");
    }

    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, CustomRenderStates.BLEND.ADD.id, true));
    }
}
