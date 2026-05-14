package com.lycanitesmobs.core.worldgen;

import com.lycanitesmobs.core.worldgen.config.NoPlacementConfig;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class AlwaysPlacement implements PlacementModifierType<NoPlacementConfig> {
    @Override
    public Codec<NoPlacementConfig> codec() {
        return NoPlacementConfig.CODEC;
    }
}
