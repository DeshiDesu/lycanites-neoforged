package com.lycanitesmobs.core.worldgen.feature;

import com.lycanitesmobs.core.event.SpawnerEventListener;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;


public class ChunkSpawnFeature extends Feature<NoneFeatureConfiguration> {
    public ChunkSpawnFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (level instanceof WorldGenRegion region) {
            BlockPos origin = context.origin();
            ChunkPos chunkPos = new ChunkPos(origin);
            SpawnerEventListener.getInstance().onChunkGenerate(
                    region.getLevel().dimension().location().toString(),
                    chunkPos
            );
        }
        return true;
    }
}
