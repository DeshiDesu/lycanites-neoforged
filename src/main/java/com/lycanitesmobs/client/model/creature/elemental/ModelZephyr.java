package com.lycanitesmobs.client.model.creature.elemental;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.specific.LayerZephyr;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;

@OnlyIn(Dist.CLIENT)
public class ModelZephyr extends ModelTemplateElemental {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelZephyr() {
        this(1.0F);
    }

    public ModelZephyr(float shadowSize) {

        // Load Model:
        this.initModel("zephyr", LycanitesMobs.modInfo, "entity/zephyr");

        // Trophy:
        this.trophyScale = 1.2F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
    }


    // ==================================================
    //             Add Custom Render Layers
    // ==================================================
    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerZephyr(renderer));
    }


    // ==================================================
    //                Can Render Part
    // ==================================================

    /**
     * Returns true if the part can be rendered on the base layer.
     **/
    @Override
    public boolean canBaseRenderPart(String partName, Entity entity, boolean trophy) {
        if (partName.contains("ribbon")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if (partName.contains("ribbon")) {
            return layer instanceof LayerZephyr;
        }
        return super.canRenderPart(partName, entity, layer, trophy);
    }


    // ==================================================
    //              Get Part Texture Offset
    // ==================================================
    @Override
    public Vector2f getBaseTextureOffset(String partName, Entity entity, boolean trophy, float loop) {
        if (partName.contains("ribbon")) {
            return new Vector2f(-loop * 25, 0);
        }
        return super.getBaseTextureOffset(partName, entity, trophy, loop);
    }
}
