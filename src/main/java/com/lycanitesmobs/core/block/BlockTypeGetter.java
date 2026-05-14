package com.lycanitesmobs.core.block;

import net.minecraft.resources.ResourceLocation;

public interface BlockTypeGetter {
    ResourceLocation getRegistryName();

    void setRegistryName(ResourceLocation registryName);
}
