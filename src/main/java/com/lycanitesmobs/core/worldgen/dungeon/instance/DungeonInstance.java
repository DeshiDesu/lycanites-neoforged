package com.lycanitesmobs.core.worldgen.dungeon.instance;

import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DungeonInstance {
    /** A Dungeon Instance is a dungeon that is active in the world. **/

    /**
     * A unique identifier for this Dungeon Instance.
     **/
    public UUID uuid;

    /**
     * The Schematic this instance builds from. Can be null when a Schematic has been removed or renamed in which case the dungeon is immediately set as complete.
     **/
    public DungeonSchematic schematic;

    /**
     * If true, this dungeon has been fully built and does not need to generate its layout, etc. This is where all chunks this dungeon is in have been loaded.
     **/
    public boolean complete = false;

    /**
     * Stores how many chunks have been built. When this matches the number of chunks that are used by this dungeon, this dungeon is marked as complete.
     **/
    public int chunksBuilt = 0;

    /**
     * The origin block position of this dungeon where it begins building from.
     **/
    public BlockPos originPos;

    /**
     * The minimum xz chunk that sectors that this layout is in.
     **/
    public ChunkPos chunkMin;

    /**
     * The maximum xz chunk that sectors that this layout is in.
     **/
    public ChunkPos chunkMax;

    /**
     * The world that the dungeon should build in.
     **/
    public Level world;

    /**
     * The seed for generating this dungeon. All random decisions are based on this seed so that the Dungeon Layout can generate the same on world reload, etc.
     **/
    public long seed = 0;

    /**
     * The random instance to use when randomly generating, this can be seeded for consistent results.
     **/
    public RandomSource random;

    /**
     * The generated layout of this dungeon, this contains all randomly selected sectors and their structures, etc. This is null on complete dungeons.
     **/
    public DungeonLayout layout;

    public DungeonBuildPlan buildPlan;

    /**
     * Sets the origin position. This must be set before init. Reading from NBT sets this from the NBT data.
     *
     * @param blockPos The exact block position that this dungeon builds from.
     */
    public void setOrigin(BlockPos blockPos) {
        this.originPos = blockPos;
        if (this.chunkMin == null) {
            this.chunkMin = new ChunkPos(blockPos);
        }
        if (this.chunkMax == null) {
            this.chunkMax = new ChunkPos(blockPos);
        }
    }


    /**
     * Initialises this Dungeon where if it's not complete it will generate its layout, etc. Should be called after readFromNBT when loading an existing dungeon and origin must be set.
     *
     * @param world The world that this Instance will build in.
     * @return True on success and false if unable to initialize.
     */
    public boolean init(Level world) {
        this.world = world;
        if (this.complete) {
            return true;
        }
        if (this.world == null || this.originPos == null) {
            LMHelperClass.logWarning("", "Tried to initialise a dungeon with a missing world or origin. " + this);
            return false;
        }

        if (this.schematic == null) {
            List<DungeonSchematic> schematics = new ArrayList<>();
            for (DungeonSchematic schematic : DungeonManager.getInstance().schematics.values()) {
                if (schematic.canBuild(world, this.originPos)) {
                    schematics.add(schematic);
                }
            }
            if (schematics.isEmpty()) {
                LMHelperClass.logDebug("Dungeon", "No valid dungeon schematics found for origin position: " + this.originPos);
                return false;
            }
            if (schematics.size() == 1) {
                this.schematic = schematics.get(0);
            } else {
                this.schematic = schematics.get(this.world.random.nextInt(schematics.size()));
            }
        }

        if (this.seed == 0) {
            this.seed = world.random.nextLong();
        }
        this.random = RandomSource.create(this.seed);

        LMHelperClass.logDebug("Dungeon", "Starting Dungeon Instance Generation For " + this);
        if (this.layout == null) {
            this.layout = new DungeonLayout(this);
            this.layout.generate(this.random);
        }
        this.buildPlan = new DungeonBuildPlan();
        CompletableFuture.runAsync(() -> {
            for (SectorInstance sector : this.layout.sectors) {
                sector.generateBuildPlan(this.buildPlan);
            }
            this.buildPlan.markReady();
        });

        LMHelperClass.logInfo("Dungeon", "Generated New Dungeon Instance " + this);

        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
        if (extendedWorld != null) {
            extendedWorld.setDirty();
        }

        return true;
    }


    /**
     * Returns true if the chunk position is within this dungeon's area.
     *
     * @param chunkPos The chunk position to check.
     * @param padding  Increases the dungeon area, useful for finding chunks within range of this layout as well.
     * @return True if the chunk is within this dungeon's area.
     */
    public boolean isChunkPosWithin(ChunkPos chunkPos, int padding) {
        if (chunkPos.x < this.chunkMin.x - padding || chunkPos.x > this.chunkMax.x + padding) {
            return false;
        }

        if (chunkPos.z < this.chunkMin.z - padding || chunkPos.z > this.chunkMax.z + padding) {
            return false;
        }

        return true;
    }


    /**
     * Builds blocks from every sector within the provided chunk position.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for placement during WorldGen.
     * @param chunkPos    The chunk position to build within.
     */
    public void buildChunk(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        if (this.complete || this.layout == null) {
            return;
        }

        if (this.buildPlan != null && this.buildPlan.isReady()) {
            List<DungeonBuildPlan.PlannedBlock> blocks = this.buildPlan.getForChunk(chunkPos);

            if (blocks.isEmpty()) {
                LMHelperClass.logDebug("Dungeon", "buildChunk: no planned blocks for chunk " + chunkPos + " in dungeon " + this.uuid);
            } else {
                int placed = 0;
                for (DungeonBuildPlan.PlannedBlock block : blocks) {
                    if (placed < 10) {
                        LMHelperClass.logDebug(
                                "Dungeon",
                                "buildChunk: placing planned block " +
                                        block.state.getBlock() + " at " + block.pos +
                                        " in chunk " + chunkPos + " for dungeon " + this.uuid
                        );
                    }
                    SectorInstance anySector = this.layout.sectors.isEmpty() ? null : this.layout.sectors.get(0);
                    if (anySector != null) {
                        block.apply(worldWriter, world, chunkPos, random, anySector);
                        placed++;
                    }
                }
                LMHelperClass.logDebug(
                        "Dungeon",
                        "buildChunk: placed " + placed + " planned blocks in chunk " + chunkPos + " for dungeon " + this.uuid
                );
            }

            int plannedChunkCount = this.buildPlan.getPlannedChunks().size();
            if (++this.chunksBuilt >= plannedChunkCount) {
                this.complete = true;
                LMHelperClass.logDebug(
                        "Dungeon",
                        "buildChunk: dungeon " + this.uuid + " marked complete using buildPlan. chunksBuilt=" +
                                this.chunksBuilt + " plannedChunks=" + plannedChunkCount
                );
            }
            return;
        }

        if (this.buildPlan == null) {
            LMHelperClass.logDebug("Dungeon", "buildChunk: buildPlan is null for dungeon " + this.uuid + " chunk " + chunkPos);
        } else {
            LMHelperClass.logDebug("Dungeon", "buildChunk: buildPlan not ready yet for dungeon " + this.uuid + " chunk " + chunkPos);
        }

        if (!this.layout.sectorChunkMap.containsKey(chunkPos)) {
            LMHelperClass.logDebug("Dungeon", "buildChunk: sectorChunkMap has no entry for chunk " + chunkPos + " for dungeon " + this.uuid);
            return;
        }

        LMHelperClass.logDebug(
                "Dungeon",
                "buildChunk: using sector build for chunk " + chunkPos +
                        " with " + this.layout.sectorChunkMap.get(chunkPos).size() +
                        " sectors for dungeon " + this.uuid
        );

        for (SectorInstance sectorInstance : this.layout.sectorChunkMap.get(chunkPos)) {
            sectorInstance.build(worldWriter, world, chunkPos, random);
        }

        if (++this.chunksBuilt >= this.layout.sectorChunkMap.size()) {
            this.complete = true;
            LMHelperClass.logDebug(
                    "Dungeon",
                    "buildChunk: dungeon " + this.uuid + " marked complete using sectorChunkMap. chunksBuilt=" +
                            this.chunksBuilt + " sectorChunks=" + this.layout.sectorChunkMap.size()
            );
        }
    }


    public void debugBuildChunk(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        LMHelperClass.logDebug(
                "DungeonDebug",
                "debugBuildChunk: instance=" + this.uuid +
                        " complete=" + this.complete +
                        " layout=" + (this.layout != null ? "present" : "null") +
                        " chunk=" + chunkPos
        );

        if (this.complete) {
            LMHelperClass.logDebug("DungeonDebug", "debugBuildChunk: instance complete, skipping " + chunkPos);
            return;
        }

        if (this.layout == null) {
            LMHelperClass.logDebug("DungeonDebug", "debugBuildChunk: layout null for instance " + this.uuid);
            return;
        }

        if (this.buildPlan != null && this.buildPlan.isReady()) {
            List<DungeonBuildPlan.PlannedBlock> blocks = this.buildPlan.getForChunk(chunkPos);
            int blockCount = blocks != null ? blocks.size() : 0;
            LMHelperClass.logDebug(
                    "DungeonDebug",
                    "debugBuildChunk: buildPlan ready for " + chunkPos +
                            " plannedBlockCount=" + blockCount
            );
            return;
        }

        if (!this.layout.sectorChunkMap.containsKey(chunkPos)) {
            LMHelperClass.logDebug("DungeonDebug", "debugBuildChunk: chunk " + chunkPos + " not in sectorChunkMap");
            return;
        }

        int sectors = this.layout.sectorChunkMap.get(chunkPos).size();
        LMHelperClass.logDebug(
                "DungeonDebug",
                "debugBuildChunk: chunk " + chunkPos +
                        " in sectorChunkMap with sectorCount=" + sectors
        );
    }

    /**
     * Loads this Dungeon Instance from the provided NBT data, this is mostly just if the dungeon is built and what its origin position is.
     *
     * @param nbtTagCompound The NBT Data to read from.
     */
    public void readFromNBT(CompoundTag nbtTagCompound) {
        this.uuid = nbtTagCompound.getUUID("Id");
        this.schematic = DungeonManager.getInstance().getSchematic(nbtTagCompound.getString("Schematic"));
        this.seed = nbtTagCompound.getLong("Seed");
        this.complete = nbtTagCompound.getBoolean("Complete");
        this.chunksBuilt = nbtTagCompound.getInt("ChunksBuilt");
        if (this.schematic == null) {
            this.complete = true;
        }
        int[] originPos = nbtTagCompound.getIntArray("OriginPos");
        this.originPos = new BlockPos(originPos[0], originPos[1], originPos[2]);
        int[] chunkMin = nbtTagCompound.getIntArray("ChunkMin");
        this.chunkMin = new ChunkPos(chunkMin[0], chunkMin[1]);
        int[] chunkMax = nbtTagCompound.getIntArray("ChunkMax");
        this.chunkMax = new ChunkPos(chunkMax[0], chunkMax[1]);

        LMHelperClass.logDebug("Dungeon", "Loaded Dungeon Instance from NBT: " + this);
    }

    /**
     * Writes this dungeon to NBT. Should only be called after the this instance has been initialised.
     *
     * @param nbtTagCompound The NBTData to write to.
     * @return The written to NBTData. Null if this Dungeon Instance cannot be saved.
     */
    public CompoundTag writeToNBT(CompoundTag nbtTagCompound) {
        if (this.uuid == null || this.schematic == null)
            return null;

        nbtTagCompound.putUUID("Id", this.uuid);
        nbtTagCompound.putString("Schematic", this.schematic.name);
        nbtTagCompound.putLong("Seed", this.seed);
        nbtTagCompound.putBoolean("Complete", this.complete);
        nbtTagCompound.putInt("ChunksBuilt", this.chunksBuilt);
        nbtTagCompound.putIntArray("OriginPos", new int[]{this.originPos.getX(), this.originPos.getY(), this.originPos.getZ()});
        nbtTagCompound.putIntArray("ChunkMin", new int[]{this.chunkMin.x, this.chunkMin.z});
        nbtTagCompound.putIntArray("ChunkMax", new int[]{this.chunkMax.x, this.chunkMax.z});

        LMHelperClass.logDebug("Dungeon", "Saved Dungeon Instance to NBT: " + this);
        return nbtTagCompound;
    }


    /**
     * Returns a descriptive string of this Dungeon Instance.
     *
     * @return A formatted string.
     */
    @Override
    public String toString() {
        String schematic = "";
        if (this.schematic != null)
            schematic = " - Schematic: " + this.schematic.name;
        String tpCommand = "/tp " + this.originPos.getX() + " " + (this.originPos.getY() + 2) + " " + this.originPos.getZ() + " ";
        return "Dungeon Instance" + schematic + " - TP Command: " + tpCommand + " - Origin: " + this.originPos + " - Complete: " + complete + " - Seed: " + this.seed + " - ID: " + this.uuid;
    }

    public BlockPos getOrigin() {
        return originPos;
    }

    public static boolean DEBUG_PLACEMENT = true;
    public static boolean DISABLE_ACTUAL_PLACEMENT = false;
}
