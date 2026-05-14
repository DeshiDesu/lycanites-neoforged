package com.lycanitesmobs.client.renderer.item;

import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IItemModelRenderer {
    void bindItemTexture(ResourceLocation location);

    List<LayerItem> addLayer(LayerItem renderLayer);
}
