package com.lycanitesmobs.core.entity;

import net.minecraft.resources.ResourceLocation;

public interface EntityTypeGetter {
    ResourceLocation getRegistryName();
    void setRegistryName(ResourceLocation registryName);
}
