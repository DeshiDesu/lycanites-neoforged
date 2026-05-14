package com.lycanitesmobs.core.worldgen.structure;

import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonLayout;
import com.lycanitesmobs.core.worldgen.dungeon.instance.SectorInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class LMDungeonPiece extends StructurePiece {

    private String schematicName;
    private DungeonLayout layout;
    private long layoutSeed;
    private BlockPos originPos;

    public LMDungeonPiece(BoundingBox boundingBox, String schematicName, DungeonLayout layout) {
        super(ModStructurePieceTypes.LM_DUNGEON_PIECE, 0, boundingBox);
        this.schematicName = schematicName;
        this.layout = layout;
        this.originPos = layout.dungeonInstance.originPos;
        this.layoutSeed = layout.dungeonInstance.seed;
    }

    public LMDungeonPiece(CompoundTag tag) {
        super(ModStructurePieceTypes.LM_DUNGEON_PIECE, tag);
        this.schematicName = tag.getString("SchematicName");
        this.layoutSeed = tag.getLong("LayoutSeed");
        int[] origin = tag.getIntArray("Origin");
        this.originPos = new BlockPos(origin[0], origin[1], origin[2]);
        this.layout = regenerateLayout();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("SchematicName", this.schematicName);
        tag.putLong("LayoutSeed", this.layoutSeed);
        tag.putIntArray("Origin", new int[] {
                this.originPos.getX(),
                this.originPos.getY(),
                this.originPos.getZ()
        });
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager,
            ChunkGenerator chunkGenerator, RandomSource random,
            BoundingBox chunkBoundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        if (this.layout == null) {
            LMHelperClass.logWarning("Dungeon",
                    "postProcess: layout is null for " + this.schematicName + ", skipping chunk " + chunkPos);
            return;
        }

        if (!this.layout.sectorChunkMap.containsKey(chunkPos)) {
            return;
        }

        if (this.layout.dungeonInstance.world == null) {
            this.layout.dungeonInstance.world = worldGenLevel.getLevel();
        }

        try {
            for (SectorInstance sector : this.layout.sectorChunkMap.get(chunkPos)) {
                sector.build(worldGenLevel, worldGenLevel.getLevel(), chunkPos, random);
            }

            LMHelperClass.logDebug("Dungeon",
                    "postProcess: built " + this.layout.sectorChunkMap.get(chunkPos).size() +
                            " sectors in chunk " + chunkPos + " for " + this.schematicName);
        } catch (Exception e) {
            LMHelperClass.logErrorMessageOnceCatchable(
                    "postProcess failed for " + this.schematicName + " chunk " + chunkPos + ": ", e);
        }
    }

    private DungeonLayout regenerateLayout() {
        DungeonSchematic schematic = DungeonManager.getInstance().getSchematic(this.schematicName);
        if (schematic == null) {
            LMHelperClass.logWarning("Dungeon",
                    "Cannot regenerate layout: schematic '" + this.schematicName + "' not found");
            return null;
        }

        DungeonInstance tempInstance = new DungeonInstance();
        tempInstance.schematic = schematic;
        tempInstance.setOrigin(this.originPos);
        tempInstance.seed = this.layoutSeed;

        RandomSource layoutRandom = RandomSource.create(this.layoutSeed);
        DungeonLayout newLayout = new DungeonLayout(tempInstance);
        newLayout.generate(layoutRandom);

        if (newLayout.sectors.isEmpty()) {
            LMHelperClass.logWarning("Dungeon",
                    "Regenerated layout has no sectors for " + this.schematicName);
            return null;
        }

        LMHelperClass.logDebug("Dungeon",
                "Regenerated layout from seed for " + this.schematicName +
                        " with " + newLayout.sectors.size() + " sectors");
        return newLayout;
    }
}
