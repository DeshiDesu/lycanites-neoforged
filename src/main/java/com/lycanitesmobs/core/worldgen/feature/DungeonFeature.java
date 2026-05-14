package com.lycanitesmobs.core.worldgen.feature;

import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.data.config.ConfigDungeons;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;
import java.util.UUID;

public class DungeonFeature extends Feature<NoneFeatureConfiguration> {
    public DungeonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (!(level instanceof WorldGenRegion region)) {
            return false;
        }

        ServerLevel serverLevel = region.getLevel();
        boolean enabled = ConfigDungeons.INSTANCE.dungeonsEnabled.get();
        int dungeonDistance = ConfigDungeons.INSTANCE.dungeonDistance.get();

        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(serverLevel);
        if (!enabled || extendedWorld == null) {
            return false;
        }

        RandomSource random = context.random();
        BlockPos origin = context.origin();

        try {
            int dungeonSizeMax = dungeonDistance;
            ChunkPos chunkPos = new ChunkPos(origin);
            List<DungeonInstance> nearbyDungeons = extendedWorld.getNearbyDungeonInstances(chunkPos, dungeonSizeMax * 2);

            if (nearbyDungeons.isEmpty()) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) {
                            continue;
                        }
                        if (x != 0 && z != 0) {
                            continue;
                        }

                        DungeonInstance dungeonInstance = new DungeonInstance();
                        int yPos = region.getSeaLevel();
                        if (yPos < 64) {
                            yPos = 64;
                        }

                        ChunkPos center = new ChunkPos(
                                chunkPos.x + (dungeonSizeMax * x),
                                chunkPos.z + (dungeonSizeMax * z)
                        );
                        BlockPos dungeonPos = center.getWorldPosition().offset(7, yPos, 7);

                        dungeonInstance.setOrigin(dungeonPos);
                        if (dungeonInstance.init(serverLevel)) {
                            extendedWorld.addDungeonInstance(
                                    dungeonInstance,
                                    new UUID(random.nextLong(), random.nextLong())
                            );
                        }
                    }
                }
            }

        } catch (Exception e) {
            LMHelperClass.logErrorMessageOnceCatchable("Dungeon Error: ", e);
        }

        return true;
    }
}
