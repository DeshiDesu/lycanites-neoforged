package com.lycanitesmobs.core.manager;

import com.google.common.collect.ImmutableSet;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.liquid.BaseLiquidBlock;
import com.lycanitesmobs.core.manager.FluidManager;
import com.lycanitesmobs.core.worldgen.feature.ChunkSpawnFeature;
import com.lycanitesmobs.core.worldgen.feature.DungeonFeature;
import com.lycanitesmobs.core.worldgen.feature.DungeonFeatureConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldGenManager {
    private static final WorldGenManager INSTANCE = new WorldGenManager();

    public static WorldGenManager getInstance() {
        return INSTANCE;
    }

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, LycanitesMobs.MODID);

    public static final RegistryObject<ChunkSpawnFeature> CHUNK_SPAWN_FEATURE =
            FEATURES.register("chunkspawn", () -> new ChunkSpawnFeature(NoneFeatureConfiguration.CODEC));

/*
    public static final RegistryObject<DungeonFeature> DUNGEON_FEATURE =
            FEATURES.register("dungeon", () -> new DungeonFeature(NoneFeatureConfiguration.CODEC));
*/

    public final Map<String, Holder<PlacedFeature>> fluidPlacedFeatures = new HashMap<>();

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    public void setupFluidFeatures() {
        for (String fluidName : FluidManager.getInstance().worldgenFluidBlocks.keySet()) {
            BaseLiquidBlock fluidBlock = FluidManager.getInstance().worldgenFluidBlocks.get(fluidName);
            BlockState state = fluidBlock.defaultBlockState();

            LakeFeature.Configuration lakeConfig = new LakeFeature.Configuration(
                    BlockStateProvider.simple(state),
                    BlockStateProvider.simple(Blocks.STONE.defaultBlockState())
            );
            var list = new ArrayList<Holder<Block>>();
            list.add(Holder.direct(Blocks.STONE));
            list.add(Holder.direct(Blocks.GRANITE));
            list.add(Holder.direct(Blocks.DIORITE));
            list.add(Holder.direct(Blocks.ANDESITE));
            SpringConfiguration springConfig = new SpringConfiguration(
                    state.getFluidState(),
                    true,
                    4,
                    1,
                    HolderSet.direct(list)
            );

            List<PlacementModifier> lakePlacement;
            List<PlacementModifier> springPlacement;

            boolean isWaterLike = state.getFluidState().isSource();

            if (isWaterLike) {
                lakePlacement = List.of(
                        RarityFilter.onAverageOnceEvery(40),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(8),
                                VerticalAnchor.absolute(256)
                        ),
                        BiomeFilter.biome()
                );
                springPlacement = List.of(
                        InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(8),
                                VerticalAnchor.absolute(256)
                        ),
                        BiomeFilter.biome()
                );
            } else {
                lakePlacement = List.of(
                        RarityFilter.onAverageOnceEvery(40),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(8),
                                VerticalAnchor.absolute(256)
                        ),
                        BiomeFilter.biome()
                );
                springPlacement = List.of(
                        InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(8),
                                VerticalAnchor.absolute(256)
                        ),
                        BiomeFilter.biome()
                );
            }

            Holder<PlacedFeature> lakePlaced = PlacementUtils.inlinePlaced(
                    Feature.LAKE,
                    lakeConfig,
                    lakePlacement.toArray(new PlacementModifier[0])
            );

            Holder<PlacedFeature> springPlaced = PlacementUtils.inlinePlaced(
                    Feature.SPRING,
                    springConfig,
                    springPlacement.toArray(new PlacementModifier[0])
            );

            fluidPlacedFeatures.put(fluidName + "_lake", lakePlaced);
            fluidPlacedFeatures.put(fluidName + "_spring", springPlaced);
        }
    }
}
