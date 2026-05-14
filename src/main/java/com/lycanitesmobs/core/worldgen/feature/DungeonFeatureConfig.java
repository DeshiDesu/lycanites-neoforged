package com.lycanitesmobs.core.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record DungeonFeatureConfig(String schematic) implements FeatureConfiguration {
    public static final Codec<DungeonFeatureConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("schematic").forGetter(DungeonFeatureConfig::schematic)
            ).apply(instance, DungeonFeatureConfig::new)
    );
}
