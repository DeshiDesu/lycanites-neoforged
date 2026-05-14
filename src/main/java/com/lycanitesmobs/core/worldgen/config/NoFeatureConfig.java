package com.lycanitesmobs.core.worldgen.config;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class NoFeatureConfig implements FeatureConfiguration {

    public static final NoFeatureConfig INSTANCE = new NoFeatureConfig();
    public static final Codec<NoFeatureConfig> CODEC = Codec.unit(() -> {
        return INSTANCE;
    });


    public NoFeatureConfig() {
    }

}