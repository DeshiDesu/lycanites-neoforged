package com.lycanitesmobs.core.mixin;

import net.minecraftforge.common.world.ModifiableStructureInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin accessor for Forge's {@link ModifiableStructureInfo} to allow setting
 * the modified structure info after initial construction.
 * <p>
 * Forge's coremod redirects all reads of {@code Structure.settings} through
 * {@code modifiableStructureInfo}, so any spawn override injection must go
 * through this layer rather than the raw settings field.
 */
@Mixin(value = ModifiableStructureInfo.class, remap = false)
public interface ModifiableStructureInfoAccessor {
    @Accessor("modifiedStructureInfo")
    ModifiableStructureInfo.StructureInfo getModifiedStructureInfo();

    @Mutable
    @Accessor("modifiedStructureInfo")
    void setModifiedStructureInfo(ModifiableStructureInfo.StructureInfo info);
}
