package com.lycanitesmobs.core.worldgen.structure;

import com.lycanitesmobs.core.entity.spawner.condition.WorldSpawnCondition;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonLayout;
import com.lycanitesmobs.core.worldgen.dungeon.instance.SectorInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class LMDungeonStructure extends Structure {

    public static final Codec<LMDungeonStructure> CODEC = RecordCodecBuilder
            .<LMDungeonStructure>mapCodec(instance -> instance.group(
                            settingsCodec(instance),
                            Codec.STRING.fieldOf("schematic_name").forGetter(s -> s.schematicName))
                    .apply(instance, LMDungeonStructure::new))
            .codec();

    private final String schematicName;

    public LMDungeonStructure(StructureSettings settings, String schematicName) {
        super(settings);
        this.schematicName = schematicName;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        DungeonSchematic schematic = DungeonManager.getInstance().getSchematic(this.schematicName);
        if (schematic == null || !schematic.enabled) {
            return Optional.empty();
        }

        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getBlockX(7);
        int centerZ = chunkPos.getBlockZ(7);

        int groundHeight = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState());

        int y = computeYPosition(schematic, context.random(), groundHeight);

        // -- Check biome blacklist from schematic conditions --
        WorldSpawnCondition condition = schematic.getWorldSpawnCondition();
        if (condition != null && !condition.biomeTagBlacklist.isEmpty()) {
            Holder<Biome> biomeHolder = context.chunkGenerator().getBiomeSource()
                    .getNoiseBiome(centerX >> 2, y >> 2, centerZ >> 2, context.randomState().sampler());
            for (String blacklistTag : condition.biomeTagBlacklist) {
                TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, new ResourceLocation(blacklistTag));
                if (biomeHolder.is(tagKey)) {
                    return Optional.empty();
                }
            }
        }

        BlockPos center = new BlockPos(centerX, y, centerZ);

        return Optional.of(new GenerationStub(center, builder -> {
            try {
                DungeonLayout layout = generateLayout(schematic, center, context.random());
                if (layout == null) {
                    return;
                }

                BoundingBox boundingBox = computeBoundingBox(layout, center, context);
                builder.addPiece(new LMDungeonPiece(boundingBox, schematic.name, layout));

                LMHelperClass.logDebug("Dungeon",
                        "Structure scheduled: " + schematic.name + " at " + center);

            } catch (Exception e) {
                LMHelperClass.logErrorMessageOnceCatchable(
                        "Failed to generate dungeon structure " + schematicName + ": ", e);
            }
        }));
    }

    private int computeYPosition(DungeonSchematic schematic, RandomSource random, int groundHeight) {
        var condition = schematic.getWorldSpawnCondition();
        if (condition != null) {
            int minY = condition.getMinY();
            int maxY = condition.getMaxY();
            if (maxY < minY) {
                int t = minY;
                minY = maxY;
                maxY = t;
            }
            int range = Math.max(1, maxY - minY + 1);
            return minY + random.nextInt(range);
        }
        return Math.max(4, groundHeight - 10);
    }

    private DungeonLayout generateLayout(DungeonSchematic schematic, BlockPos origin, RandomSource random) {
        DungeonInstance tempInstance = new DungeonInstance();
        tempInstance.schematic = schematic;
        tempInstance.setOrigin(origin);

        long layoutSeed = random.nextLong();
        tempInstance.seed = layoutSeed;

        RandomSource layoutRandom = RandomSource.create(layoutSeed);
        DungeonLayout layout = new DungeonLayout(tempInstance);
        layout.generate(layoutRandom);

        if (layout.sectors.isEmpty()) {
            LMHelperClass.logDebug("Dungeon",
                    "Layout generation produced no sectors for " + schematic.name);
            return null;
        }

        return layout;
    }

    private BoundingBox computeBoundingBox(DungeonLayout layout, BlockPos center, GenerationContext context) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (SectorInstance sector : layout.sectors) {
            BlockPos sMin = sector.getOccupiedBoundsMin();
            BlockPos sMax = sector.getOccupiedBoundsMax();
            if (sMin != null && sMax != null) {
                minX = Math.min(minX, sMin.getX());
                minY = Math.min(minY, sMin.getY());
                minZ = Math.min(minZ, sMin.getZ());
                maxX = Math.max(maxX, sMax.getX());
                maxY = Math.max(maxY, sMax.getY());
                maxZ = Math.max(maxZ, sMax.getZ());
            }
        }

        if (minX == Integer.MAX_VALUE) {
            minX = center.getX() - 64;
            minZ = center.getZ() - 64;
            maxX = center.getX() + 64;
            maxZ = center.getZ() + 64;
            minY = context.heightAccessor().getMinBuildHeight();
            maxY = context.heightAccessor().getMaxBuildHeight();
        }

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.LM_DUNGEON;
    }
}
