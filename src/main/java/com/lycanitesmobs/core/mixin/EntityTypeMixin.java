package com.lycanitesmobs.core.mixin;

import com.lycanitesmobs.core.entity.EntityTypeGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = EntityType.class, remap = false)
public class EntityTypeMixin implements EntityTypeGetter {
    @Unique
    public ResourceLocation registryName;

    public void setRegistryName(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }
}
