package com.lycanitesmobs.core.worldgen.dungeon.instance;

import com.lycanitesmobs.core.block.base.BlockFireBase;
import com.lycanitesmobs.core.block.cloud.BlockFrostCloud;
import com.lycanitesmobs.core.block.cloud.BlockPoisonCloud;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.worldgen.dungeon.DeferredBossSpawner;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSector;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonTheme;
import com.lycanitesmobs.core.worldgen.dungeon.definition.SectorLayer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.spawner.MobSpawn;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SectorInstance {
    /** Sector Instances makeup an entire Dungeon Layout. **/

    /**
     * The Dungeon Layout that this instance belongs to.
     **/
    public DungeonLayout layout;

    /**
     * The Dungeon Sector that this instance is using.
     **/
    public DungeonSector dungeonSector;

    /**
     * The connector that this sector is connected to, cannot be null.
     **/
    public SectorConnector parentConnector;

    /**
     * A list of connectors that this sector provides to connect to other sectors
     * with.
     **/
    public List<SectorConnector> connectors = new ArrayList<>();

    /**
     * The room size of this Sector Instance, this includes the inside and inner
     * floor, walls and ceiling. Used for building and sector to sector collision.
     **/
    protected Vector3i roomSize;

    /**
     * The occupied size of this Sector Instance, includes the room size plus
     * additional space taken up by sector layers, structures, padding, etc.
     **/
    protected Vector3i occupiedSize;

    /**
     * The theme this Sector Instance is using.
     **/
    public DungeonTheme theme;

    /**
     * The random light block for this sector instance to use.
     **/
    public BlockState lightBlock;

    /**
     * The random torch block for this sector instance to use.
     **/
    public BlockState torchBlock;

    /**
     * The random stairs block for this sector instance to use.
     **/
    public BlockState stairBlock;

    /**
     * The random pit block for this sector instance to use.
     **/
    public BlockState pitBlock;

    /**
     * How many chunks this sector has been built into. When this equals the total
     * chunks this sector occupies it is considered fully built.
     **/
    public int chunksBuilt = 0;

    /**
     * Constructor
     *
     * @param layout        The Dungeon Layout to create this instance for.
     * @param dungeonSector The Dungeon Sector to create this instance from.
     * @param random        The instance of Random to use.
     */
    public SectorInstance(DungeonLayout layout, DungeonSector dungeonSector, RandomSource random) {
        this.layout = layout;
        this.dungeonSector = dungeonSector;

        // Size:
        this.roomSize = this.dungeonSector.getRandomSize(random);
        this.occupiedSize = new Vector3i(
                this.roomSize.x() + Math.max(1, this.dungeonSector.padding.getX()),
                this.roomSize.y() + this.dungeonSector.padding.getY(),
                this.roomSize.z() + Math.max(1, this.dungeonSector.padding.getZ()));

        // Structures:
        // TODO Structures
    }

    /**
     * Connects this sector to the provided connector. Should be called before init.
     *
     * @param parentConnector The connector that this sector is connecting from.
     */
    public void connect(SectorConnector parentConnector) {
        this.parentConnector = parentConnector;
    }

    /**
     * Initialises this Sector Instance. Must be connected to a parent connector.
     *
     * @param random The instance of Random to use.
     */
    public void init(RandomSource random) {
        if (this.parentConnector == null) {
            LMHelperClass.logWarning("Dungeon", "Skipping SectorInstance.init due to null parentConnector: " + this);
            return;
        }

        // Close Parent Connector:
        this.parentConnector.childSector = this;
        this.parentConnector.closed = true;
        this.layout.openConnectors.remove(this.parentConnector);

        // Theme:
        if (this.dungeonSector.changeTheme || this.parentConnector.parentSector == null) {
            this.theme = this.layout.dungeonInstance.schematic.getRandomTheme(random);
        } else {
            this.theme = this.parentConnector.parentSector.theme;
        }
        this.lightBlock = this.theme.getLight('B', random);
        this.torchBlock = this.theme.getTorch('B', random);
        this.stairBlock = this.theme.getStairs('B', random);
        this.pitBlock = this.theme.getPit('B', random);

        // Create Child Connectors:
        BlockPos boundsMin = this.getRoomBoundsMin();
        BlockPos boundsMax = this.getRoomBoundsMax();
        Vector3i size = this.getRoomSize();
        int centerX = boundsMin.getX() + Math.round((float) size.x() / 2);
        int centerZ = boundsMin.getZ() + Math.round((float) size.z() / 2);

        // Upper Exit:
        int upperConnectorY = this.parentConnector.position.getY() + this.getRoomSize().y();
        if (upperConnectorY < 255) {
            BlockPos blockPos = new BlockPos(centerX, upperConnectorY, centerZ);
            this.addConnector(blockPos, this.parentConnector.level + 1, Direction.UP);
        }

        if ("corridor".equalsIgnoreCase(this.dungeonSector.type) || "room".equalsIgnoreCase(this.dungeonSector.type)
                || "tower".equalsIgnoreCase(this.dungeonSector.type)
                || "entrance".equalsIgnoreCase(this.dungeonSector.type)
                || "bossRoom".equalsIgnoreCase(this.dungeonSector.type)) {

            // Front/Back Exit:
            BlockPos frontPos = this.parentConnector.position;
            Direction frontFacing = Direction.SOUTH;
            BlockPos backPos = this.parentConnector.position;
            Direction backFacing = Direction.NORTH;
            if (this.parentConnector.facing == Direction.SOUTH || this.parentConnector.facing == Direction.UP) {
                frontPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                        this.parentConnector.position.getY(), boundsMax.getZ() + 1);
                frontFacing = Direction.SOUTH;
                backPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                        this.parentConnector.position.getY(), boundsMin.getZ() - 1);
                backFacing = Direction.NORTH;
            } else if (this.parentConnector.facing == Direction.EAST) {
                frontPos = new BlockPos(boundsMax.getX() + 1, this.parentConnector.position.getY(),
                        this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
                frontFacing = Direction.EAST;
                backPos = new BlockPos(boundsMin.getX() - 1, this.parentConnector.position.getY(),
                        this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
                backFacing = Direction.WEST;
            } else if (this.parentConnector.facing == Direction.NORTH) {
                frontPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                        this.parentConnector.position.getY(), boundsMin.getZ() - 1);
                frontFacing = Direction.NORTH;
                backPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                        this.parentConnector.position.getY(), boundsMax.getZ() + 1);
                backFacing = Direction.SOUTH;
            } else if (this.parentConnector.facing == Direction.WEST) {
                frontPos = new BlockPos(boundsMin.getX() - 1, this.parentConnector.position.getY(),
                        this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
                frontFacing = Direction.WEST;
                backPos = new BlockPos(boundsMax.getX() + 1, this.parentConnector.position.getY(),
                        this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
                backFacing = Direction.EAST;
            }
            this.addConnector(frontPos, this.parentConnector.level, frontFacing);
            if ("tower".equalsIgnoreCase(this.dungeonSector.type)) {
                this.addConnector(backPos, this.parentConnector.level, backFacing);
            }

            // Side Exits:
            if ("room".equalsIgnoreCase(this.dungeonSector.type) || "tower".equalsIgnoreCase(this.dungeonSector.type)) {
                BlockPos leftPos = this.parentConnector.position;
                Direction leftFacing = Direction.WEST;
                BlockPos rightPos = this.parentConnector.position;
                Direction rightFacing = Direction.EAST;
                if (this.parentConnector.facing == Direction.SOUTH || this.parentConnector.facing == Direction.NORTH
                        || this.parentConnector.facing == Direction.UP) {
                    leftPos = new BlockPos(boundsMin.getX() - 1, this.parentConnector.position.getY(),
                            this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
                    leftFacing = Direction.WEST;
                    rightPos = new BlockPos(boundsMax.getX() + 1, this.parentConnector.position.getY(),
                            this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
                    rightFacing = Direction.EAST;
                } else if (this.parentConnector.facing == Direction.EAST
                        || this.parentConnector.facing == Direction.WEST) {
                    leftPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                            this.parentConnector.position.getY(), boundsMax.getZ() + 1);
                    leftFacing = Direction.SOUTH;
                    rightPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                            this.parentConnector.position.getY(), boundsMin.getZ() - 1);
                    rightFacing = Direction.NORTH;
                }
                this.addConnector(leftPos, this.parentConnector.level, leftFacing);
                this.addConnector(rightPos, this.parentConnector.level, rightFacing);
            }
        }
        if ("stairs".equalsIgnoreCase(this.dungeonSector.type)) {

            // Lower Exit:
            int y = this.parentConnector.position.getY() - (size.y() * 2);
            if (y > 0) {
                BlockPos blockPos = new BlockPos(centerX, y, boundsMax.getZ() + 1);
                if (this.parentConnector.facing == Direction.EAST) {
                    blockPos = new BlockPos(boundsMax.getX() + 1, y, centerZ);
                } else if (this.parentConnector.facing == Direction.NORTH) {
                    blockPos = new BlockPos(centerX, y, boundsMin.getZ() - 1);
                } else if (this.parentConnector.facing == Direction.WEST) {
                    blockPos = new BlockPos(boundsMin.getX() - 1, y, centerZ);
                }
                this.addConnector(blockPos, this.parentConnector.level - 1, this.parentConnector.facing);
            }
        }

        // LycanitesMobs.logDebug("Dungeon", "Initialised Sector Instance - Bounds: " +
        // this.getOccupiedBoundsMin() + " to " + this.getOccupiedBoundsMax());
    }

    /**
     * Gets a random offset for positioning a sector connector.
     *
     * @param random The instance of random to use.
     * @param length The length to get a random offset from such as the x/z size of
     *               the sector instance.
     * @param start  The world position to add the offset to.
     * @return
     */
    public int getConnectorOffset(RandomSource random, int length, int start) {
        int entrancePadding = 2;
        if (!"room".equalsIgnoreCase(this.dungeonSector.type) || length <= entrancePadding * 2) {
            return start + Math.round((float) length / 2);
        }
        return start + entrancePadding + random.nextInt(length - (entrancePadding * 2)) + 1;
    }

    /**
     * Adds a new child Sector Connector to this Sector Instance.
     *
     * @param blockPos The position of the connector.
     * @param level    The level that the connector is on.
     * @param facing   The facing of the sector.
     * @return The newly created Sector Connector.
     */
    public SectorConnector addConnector(BlockPos blockPos, int level, Direction facing) {
        SectorConnector connector = new SectorConnector(blockPos, this, level, facing);
        this.connectors.add(connector);
        return connector;
    }

    /**
     * Returns a random child connector for a Sector Instance to connect to.
     *
     * @param random The instance of Random to use.
     * @return A random connector.
     */
    public SectorConnector getRandomConnector(RandomSource random, SectorInstance sectorInstance) {
        List<SectorConnector> openConnectors = this.getOpenConnectors(sectorInstance);
        if (openConnectors.isEmpty()) {
            return null;
        }
        if (openConnectors.size() == 1) {
            return openConnectors.get(0);
        }
        return openConnectors.get(random.nextInt(openConnectors.size()));
    }

    /**
     * Returns a list of open connectors where they are not set to closed and have
     * no child Sector Instance connected.
     *
     * @param sectorInstance The sector to get the open connectors for. If null,
     *                       collision checks are skipped.
     * @return A list of open connectors.
     */
    public List<SectorConnector> getOpenConnectors(SectorInstance sectorInstance) {
        List<SectorConnector> openConnectors = new ArrayList<>();
        for (SectorConnector connector : this.connectors) {
            if (connector.canConnect(this.layout, sectorInstance)) {
                openConnectors.add(connector);
            }
        }
        return openConnectors;
    }

    /**
     * Returns a list of every ChunkPos that this Sector Instance occupies.
     * Applies an offset to occupied bounds for generating a chunk position.
     *
     * @return A list of ChunkPos.
     */
    public List<ChunkPos> getChunkPositions() {
        ChunkPos minChunkPos = new ChunkPos(this.getOccupiedBoundsMin().offset(-1, 0, -1));
        ChunkPos maxChunkPos = new ChunkPos(this.getOccupiedBoundsMax().offset(1, 0, 1));
        List<ChunkPos> chunkPosList = new ArrayList<>();
        for (int x = minChunkPos.x; x <= maxChunkPos.x; x++) {
            for (int z = minChunkPos.z; z <= maxChunkPos.z; z++) {
                chunkPosList.add(new ChunkPos(x, z));
            }
        }
        return chunkPosList;
    }

    /**
     * Returns a list of other sectors near this sector instance.
     *
     * @return A list of nearby sector instances.
     */
    public List<SectorInstance> getNearbySectors() {
        List<SectorInstance> nearbySectors = new ArrayList<>();
        for (ChunkPos chunkPos : this.getChunkPositions()) {
            if (this.layout.sectorChunkMap.containsKey(chunkPos)) {
                for (SectorInstance nearbySector : this.layout.sectorChunkMap.get(chunkPos)) {
                    if (!nearbySectors.contains(nearbySector)) {
                        nearbySectors.add(nearbySector);
                    }
                }
            }
        }
        return nearbySectors;
    }

    /**
     * Returns true if this sector instance collides with the provided sector
     * instance.
     *
     * @param sectorInstance The sector instance to check for collision with.
     * @return True on collision.
     */
    public boolean collidesWith(SectorInstance sectorInstance) {
        if (sectorInstance == this || sectorInstance == this.parentConnector.parentSector) {
            return false;
        }

        BlockPos boundsMin = this.getOccupiedBoundsMin();
        BlockPos boundsMax = this.getOccupiedBoundsMax();
        BlockPos targetMin = sectorInstance.getOccupiedBoundsMin();
        BlockPos targetMax = sectorInstance.getOccupiedBoundsMax();

        if (boundsMin.getY() != targetMin.getY() && !sectorInstance.dungeonSector.type.equals("stairs")) {
            return false;
        }

        boolean withinX = boundsMin.getX() >= targetMin.getX() && boundsMin.getX() <= targetMax.getX();
        if (!withinX)
            withinX = boundsMax.getX() >= targetMin.getX() && boundsMax.getX() <= targetMax.getX();
        if (!withinX)
            return false;

        // boolean withinY = boundsMin.getY() >= targetMin.getY() && boundsMin.getY() <=
        // targetMax.getY();
        // if(!withinY)
        // withinY = boundsMax.getY() >= targetMin.getY() && boundsMax.getY() <=
        // targetMax.getY();
        // if(!withinY)
        // return false;

        boolean withinZ = boundsMin.getZ() >= targetMin.getZ() && boundsMin.getZ() <= targetMax.getZ();
        if (!withinZ)
            withinZ = boundsMax.getZ() >= targetMin.getZ() && boundsMax.getZ() <= targetMax.getZ();
        if (!withinZ)
            return false;

        return true;
    }

    /**
     * Returns the room size of this sector. X and Z are swapped when facing EAST or
     * WEST.
     * This is how large the room to be built is excluding extra blocks added for
     * layers or structures, etc.
     * Used for building this sector.
     *
     * @return A vector of the room size.
     */
    public Vector3i getRoomSize() {
        if (this.parentConnector.facing == Direction.EAST || this.parentConnector.facing == Direction.WEST) {
            return new Vector3i(this.roomSize.z(), this.roomSize.y(), this.roomSize.x());
        }
        return this.roomSize;
    }

    /**
     * Returns the collision size of this sector. X and Z are swapped when facing
     * EAST or WEST.
     * This is how large this sector is including extra blocks added for layers or
     * structures, etc.
     * Used for detecting what chunks this sector needs to generate in and sector
     * collision detection.
     *
     * @return A vector of the collision size.
     */
    public Vector3i getOccupiedSize() {
        if (this.parentConnector.facing == Direction.EAST || this.parentConnector.facing == Direction.WEST) {
            return new Vector3i(this.occupiedSize.z(), this.occupiedSize.y(), this.occupiedSize.x());
        }
        return this.occupiedSize;
    }

    /**
     * Returns the minimum xyz position that this Sector Instance from the provided
     * bounds size.
     *
     * @param boundsSize The xyz size to use when calculating bounds.
     * @return The minimum bounds position (corner).
     */
    public BlockPos getBoundsMin(Vector3i boundsSize) {
        BlockPos bounds = new BlockPos(this.parentConnector.position);
        if (this.parentConnector.facing == Direction.UP) {
            bounds = bounds.offset(
                    -(int) Math.ceil((double) boundsSize.x() / 2),
                    0,
                    -(int) Math.ceil((double) boundsSize.z() / 2));
        } else if (this.parentConnector.facing == Direction.SOUTH) {
            bounds = bounds.offset(
                    -(int) Math.ceil((double) boundsSize.x() / 2),
                    0,
                    0);
        } else if (this.parentConnector.facing == Direction.EAST) {
            bounds = bounds.offset(
                    0,
                    0,
                    -(int) Math.ceil((double) boundsSize.z() / 2));
        } else if (this.parentConnector.facing == Direction.NORTH) {
            bounds = bounds.offset(
                    -(int) Math.ceil((double) boundsSize.x() / 2),
                    0,
                    -boundsSize.z());
        } else if (this.parentConnector.facing == Direction.WEST) {
            bounds = bounds.offset(
                    -boundsSize.x(),
                    0,
                    -(int) Math.ceil((double) boundsSize.z() / 2));
        }

        return bounds;
    }

    /**
     * Returns the maximum xyz position that this Sector Instance from the provided
     * bounds size.
     *
     * @param boundsSize The xyz size to use when calculating bounds.
     * @return The maximum bounds position (corner).
     */
    public BlockPos getBoundsMax(Vector3i boundsSize) {
        BlockPos bounds = new BlockPos(this.parentConnector.position);
        if (this.parentConnector.facing == Direction.UP) {
            bounds = bounds.offset(
                    (int) Math.ceil((double) boundsSize.x() / 2),
                    boundsSize.y(),
                    (int) Math.ceil((double) boundsSize.z() / 2));
        } else if (this.parentConnector.facing == Direction.SOUTH) {
            bounds = bounds.offset(
                    (int) Math.floor((double) boundsSize.x() / 2),
                    boundsSize.y(),
                    boundsSize.z());
        } else if (this.parentConnector.facing == Direction.EAST) {
            bounds = bounds.offset(
                    boundsSize.x(),
                    boundsSize.y(),
                    (int) Math.floor((double) boundsSize.z() / 2));
        } else if (this.parentConnector.facing == Direction.NORTH) {
            bounds = bounds.offset(
                    (int) Math.floor((double) boundsSize.x() / 2),
                    boundsSize.y(),
                    0);
        } else if (this.parentConnector.facing == Direction.WEST) {
            bounds = bounds.offset(
                    0,
                    boundsSize.y(),
                    (int) Math.floor((double) boundsSize.z() / 2));
        }
        return bounds;
    }

    /**
     * Returns the minimum xyz position that this Sector Instance occupies.
     *
     * @return The minimum bounds position (corner).
     */
    public BlockPos getOccupiedBoundsMin() {
        BlockPos occupiedBoundsMin = this.getBoundsMin(this.getOccupiedSize());
        if ("stairs".equals(this.dungeonSector.type)) {
            occupiedBoundsMin = occupiedBoundsMin.subtract(new Vec3i(0, this.getRoomSize().y() * 2, 0));
        }
        return occupiedBoundsMin;
    }

    /**
     * Returns the maximum xyz position that this Sector Instance occupies.
     *
     * @return The maximum bounds position (corner).
     */
    public BlockPos getOccupiedBoundsMax() {
        return this.getBoundsMax(this.getOccupiedSize());
    }

    /**
     * Returns the minimum xyz position that this Sector Instance builds from.
     *
     * @return The minimum bounds position (corner).
     */
    public BlockPos getRoomBoundsMin() {
        return this.getBoundsMin(this.getRoomSize());
    }

    /**
     * Returns the maximum xyz position that this Sector Instance builds to.
     *
     * @return The maximum bounds position (corner).
     */
    public BlockPos getRoomBoundsMax() {
        return this.getBoundsMax(this.getRoomSize());
    }

    /**
     * Returns the rounded center block position of this sector.
     *
     * @return The center block position.
     */
    public BlockPos getCenter() {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();

        Vector3i size = this.getRoomSize();
        int centerX = startPos.getX() + Math.round((float) size.x() / 2);
        int centerZ = startPos.getZ() + Math.round((float) size.z() / 2);

        return new BlockPos(centerX, startPos.getY(), centerZ);
    }

    /**
     * Places a block state in the world from this sector.
     *
     * @param worldWriter The world to place a block in. Cannot be the actual World
     *                    during WorldGen.
     * @param chunkPos    The chunk position to build within.
     * @param blockPos    The position to place the block at.
     * @param blockState  The block state to place.
     * @param random      The instance of random, used for random mob spawns or loot
     *                    on applicable blocks, etc.
     */
    public void placeBlock(LevelAccessor worldWriter, ChunkPos chunkPos, BlockPos blockPos, BlockState blockState,
                           Direction facing, RandomSource random) {
        int chunkOffset = 0;
        if (blockPos.getX() < chunkPos.getMinBlockX() + chunkOffset
                || blockPos.getX() > chunkPos.getMaxBlockX() + chunkOffset) {
            return;
        }
        if (blockPos.getY() < worldWriter.getMinBuildHeight() || blockPos.getY() >= worldWriter.getMaxBuildHeight()) {
            return;
        }
        if (blockPos.getZ() < chunkPos.getMinBlockZ() + chunkOffset
                || blockPos.getZ() > chunkPos.getMaxBlockZ() + chunkOffset) {
            return;
        }

        if (DungeonInstance.DISABLE_ACTUAL_PLACEMENT) {
            return;
        }

        if (blockState.getBlock() == Blocks.AIR && worldWriter.getBlockState(blockPos).getBlock() == Blocks.CHEST) {
            return;
        }

        int flags = 2;

        if (blockState.getBlock() instanceof WallTorchBlock) {
            blockState = blockState.setValue(WallTorchBlock.FACING, facing);
            flags = 0;
        }

        if (blockState.getBlock() == Blocks.CHEST) {
            blockState = blockState.setValue(ChestBlock.FACING, facing);
        }

        if (blockState.getBlock() == Blocks.AIR
                || blockState.getBlock() == Blocks.CAVE_AIR
                || (blockState.liquid() && !blockState.getBlock().getFluidState(blockState).isSource())
                || blockState.getBlock() instanceof FireBlock
                || blockState.getBlock() instanceof BlockFireBase
                || blockState.getBlock() instanceof BlockPoisonCloud
                || blockState.getBlock() instanceof BlockFrostCloud) {
            flags = 0;
        }

        if (this.layout.dungeonInstance.schematic.waterlogged && blockPos.getY() < worldWriter.getSeaLevel()) {
            if (blockState.getBlock() == Blocks.AIR || blockState.getBlock() == Blocks.CAVE_AIR) {
                blockState = Blocks.WATER.defaultBlockState();
                flags = 2;
            } else if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, true);
            }
        }

        worldWriter.setBlock(blockPos, blockState, flags);

        if (blockState.getBlock() == Blocks.SPAWNER) {
            BlockEntity tileEntity = worldWriter.getBlockEntity(blockPos);
            if (tileEntity instanceof SpawnerBlockEntity spawner) {
                MobSpawn mobSpawn = this.layout.dungeonInstance.schematic.getRandomMobSpawn(this.parentConnector.level,
                        false, random);
                if (mobSpawn != null && mobSpawn.entityType != null) {
                    spawner.setEntityId(mobSpawn.entityType, random);
                }
            }
            return;
        }

        if (blockState.getBlock() == Blocks.CHEST) {
            BlockEntity tileEntity = worldWriter.getBlockEntity(blockPos);
            if (tileEntity instanceof ChestBlockEntity) {
                ChestBlockEntity chest = (ChestBlockEntity) tileEntity;
                ResourceLocation lootTable = this.layout.dungeonInstance.schematic
                        .getRandomLootTable(this.parentConnector.level, random);
                if (lootTable != null) {
                    chest.setLootTable(lootTable, Objects.hash(blockPos.hashCode(), random));
                }
            }
        }
    }

    /**
     * Builds this sector. Wont build at y level 0 or below, beyond world height or
     * outside of the chunk.
     *
     * @param world    The world to build in.
     * @param chunkPos The chunk position to build within.
     * @param random   The instance of random, used for characters that are random.
     */
    public void build(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        this.clearArea(worldWriter, world, chunkPos, random);
        this.buildFloor(worldWriter, world, chunkPos, random, 0);
        this.buildWalls(worldWriter, world, chunkPos, random);
        this.buildCeiling(worldWriter, world, chunkPos, random);
        if ("stairs".equalsIgnoreCase(this.dungeonSector.type)) {
            this.buildStairs(worldWriter, world, chunkPos, random);
            this.buildFloor(worldWriter, world, chunkPos, random, -(this.getRoomSize().y() * 2));
        }
        if ("tower".equalsIgnoreCase(this.dungeonSector.type)) {
            this.buildStairs(worldWriter, world, chunkPos, random);
        }
        this.buildEntrances(worldWriter, world, chunkPos, random);
        this.chunksBuilt++;
        if ("bossRoom".equalsIgnoreCase(this.dungeonSector.type)) {
            MobSpawn mobSpawn = this.layout.dungeonInstance.schematic.getRandomMobSpawn(this.parentConnector.level,
                    true, random);
            if (mobSpawn != null && (mobSpawn.entityType != null || mobSpawn.mobId != null)) {
                BlockPos bossPos = this.getCenter().offset(0, 1, 0);

                Vector3i size = this.getRoomSize();
                int radius = Math.max(3, Math.max(size.x(), size.z()));

                ResourceKey<Level> dimKey = null;
                ServerLevel serverLevel = null;
                if (this.layout.dungeonInstance.world != null) {
                    dimKey = this.layout.dungeonInstance.world.dimension();
                    serverLevel = (ServerLevel) this.layout.dungeonInstance.world;
                } else if (worldWriter instanceof ServerLevelAccessor sla) {
                    dimKey = sla.getLevel().dimension();
                    serverLevel = sla.getLevel();
                }

                if (dimKey != null) {
                    DeferredBossSpawner.enqueue(dimKey, bossPos, mobSpawn, radius, serverLevel);
                }
            }
        }
    }

    /**
     * Sets the area of this sector to air for building in from within the chunk
     * position.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for
     *                    placement during WorldGen.
     * @param chunkPos    The chunk position to build within.
     * @param random      The instance of random, used for characters that are
     *                    random.
     */
    public void clearArea(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();
        int worldStartX = Math.min(startPos.getX(), stopPos.getX());
        int worldStopX = Math.max(startPos.getX(), stopPos.getX());
        int worldStartY = Math.min(startPos.getY(), stopPos.getY());
        int worldStopY = Math.max(startPos.getY(), stopPos.getY());
        int worldStartZ = Math.min(startPos.getZ(), stopPos.getZ());
        int worldStopZ = Math.max(startPos.getZ(), stopPos.getZ());

        if ("stairs".equalsIgnoreCase(this.dungeonSector.type)) {
            worldStartY = Math.max(1, startPos.getY() - (this.getRoomSize().y() * 2));
        }

        int startX = Math.max(worldStartX, chunkPos.getMinBlockX());
        int stopX = Math.min(worldStopX, chunkPos.getMaxBlockX());
        int startZ = Math.max(worldStartZ, chunkPos.getMinBlockZ());
        int stopZ = Math.min(worldStopZ, chunkPos.getMaxBlockZ());

        if (startX > stopX || startZ > stopZ) {
            return;
        }

        for (int x = startX; x <= stopX; x++) {
            for (int y = worldStartY; y <= worldStopY; y++) {
                if (y <= 0 || y >= world.getMaxBuildHeight())
                    continue;
                for (int z = startZ; z <= stopZ; z++) {
                    this.placeBlock(worldWriter, chunkPos, new BlockPos(x, y, z), Blocks.CAVE_AIR.defaultBlockState(),
                            Direction.SOUTH, random);
                }
            }
        }
    }

    /**
     * Builds the floor of this sector from within the chunk position.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for
     *                    placement during WorldGen.
     * @param chunkPos    The chunk position to build within.
     * @param random      The instance of random, used for characters that are
     *                    random.
     * @param offsetY     The Y offset to build the floor at, useful for multiple
     *                    floor sectors.
     */
    public void buildFloor(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random,
                           int offsetY) {
        BlockPos startPos = this.getRoomBoundsMin().offset(0, offsetY, 0);
        BlockPos stopPos = this.getRoomBoundsMax().offset(0, offsetY, 0);
        int worldStartX = Math.min(startPos.getX(), stopPos.getX());
        int worldStopX = Math.max(startPos.getX(), stopPos.getX());
        int worldStartY = Math.min(startPos.getY(), stopPos.getY());
        int worldStopY = Math.max(startPos.getY(), stopPos.getY());
        int worldStartZ = Math.min(startPos.getZ(), stopPos.getZ());
        int worldStopZ = Math.max(startPos.getZ(), stopPos.getZ());

        int startX = Math.max(worldStartX, chunkPos.getMinBlockX());
        int stopX = Math.min(worldStopX, chunkPos.getMaxBlockX());
        int startZ = Math.max(worldStartZ, chunkPos.getMinBlockZ());
        int stopZ = Math.min(worldStopZ, chunkPos.getMaxBlockZ());

        if (startX > stopX || startZ > stopZ) {
            return;
        }

        for (int layerIndex : this.dungeonSector.floor.layers.keySet()) {
            int y = worldStartY + layerIndex;
            if (y <= 0 || y >= world.getMaxBuildHeight()) {
                continue;
            }
            SectorLayer layer = this.dungeonSector.floor.layers.get(layerIndex);
            for (int x = startX; x <= stopX; x++) {
                List<Character> row = layer.getRow(x - worldStartX, worldStopX - worldStartX);
                for (int z = startZ; z <= stopZ; z++) {
                    char buildChar = layer.getColumn(x - worldStartX, worldStopX - worldStartX, z - worldStartZ,
                            worldStopZ - worldStartZ, row);
                    BlockPos buildPos = new BlockPos(x, y, z);
                    BlockState blockState = this.theme.getFloor(this, buildChar, random);
                    if (blockState.getBlock() != Blocks.CAVE_AIR) {
                        this.placeBlock(worldWriter, chunkPos, buildPos, blockState, Direction.UP, random);
                    }
                }
            }
        }
    }

    /**
     * Builds the walls of this sector from within the chunk position.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for
     *                    placement during WorldGen.
     * @param chunkPos    The chunk position to build within.
     * @param random      The instance of random, used for characters that are
     *                    random.
     */
    public void buildWalls(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();

        Vector3i size = this.getRoomSize();
        int worldStartX = Math.min(startPos.getX(), stopPos.getX());
        int worldStopX = Math.max(startPos.getX(), stopPos.getX());
        int worldStartY = Math.min(startPos.getY() + 1, stopPos.getY());
        int worldStopY = Math.max(startPos.getY() - 1, stopPos.getY());
        int worldStartZ = Math.min(startPos.getZ(), stopPos.getZ());
        int worldStopZ = Math.max(startPos.getZ(), stopPos.getZ());

        if ("stairs".equalsIgnoreCase(this.dungeonSector.type)) {
            worldStartY = Math.max(1, startPos.getY() - (size.y() * 2));
        }

        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMaxX = chunkPos.getMaxBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int chunkMaxZ = chunkPos.getMaxBlockZ();

        int xStart = Math.max(worldStartX, chunkMinX);
        int xStop = Math.min(worldStopX, chunkMaxX);
        int zStart = Math.max(worldStartZ, chunkMinZ);
        int zStop = Math.min(worldStopZ, chunkMaxZ);

        if (xStart > xStop || zStart > zStop) {
            return;
        }

        for (int layerIndex : this.dungeonSector.wall.layers.keySet()) {
            SectorLayer layer = this.dungeonSector.wall.layers.get(layerIndex);
            for (int y = worldStartY; y <= worldStopY; y++) {
                if (y <= 0 || y >= world.getMaxBuildHeight())
                    continue;

                int progressY = y - worldStartY;
                int fullY = worldStopY - worldStartY;
                List<Character> row = layer.getRow(progressY, fullY);

                int frontZ = worldStartZ + layerIndex;
                int backZ = worldStopZ - layerIndex;

                if (frontZ >= chunkMinZ && frontZ <= chunkMaxZ) {
                    for (int x = xStart; x <= xStop; x++) {
                        char buildChar = layer.getColumn(progressY, fullY, x - worldStartX, worldStopX - worldStartX,
                                row);
                        BlockState blockState = this.theme.getWall(this, buildChar, random);
                        if (blockState.getBlock() != Blocks.CAVE_AIR) {
                            this.placeBlock(worldWriter, chunkPos, new BlockPos(x, y, frontZ), blockState,
                                    Direction.SOUTH, random);
                        }
                    }
                }

                if (backZ >= chunkMinZ && backZ <= chunkMaxZ) {
                    for (int x = xStart; x <= xStop; x++) {
                        char buildChar = layer.getColumn(progressY, fullY, x - worldStartX, worldStopX - worldStartX,
                                row);
                        BlockState blockState = this.theme.getWall(this, buildChar, random);
                        if (blockState.getBlock() != Blocks.CAVE_AIR) {
                            this.placeBlock(worldWriter, chunkPos, new BlockPos(x, y, backZ), blockState,
                                    Direction.NORTH, random);
                        }
                    }
                }

                int leftX = worldStartX + layerIndex;
                int rightX = worldStopX - layerIndex;

                if (leftX >= chunkMinX && leftX <= chunkMaxX) {
                    for (int z = zStart; z <= zStop; z++) {
                        char buildChar = layer.getColumn(progressY, fullY, z - worldStartZ, worldStopZ - worldStartZ,
                                row);
                        BlockState blockState = this.theme.getWall(this, buildChar, random);
                        if (blockState.getBlock() != Blocks.CAVE_AIR) {
                            this.placeBlock(worldWriter, chunkPos, new BlockPos(leftX, y, z), blockState,
                                    Direction.EAST, random);
                        }
                    }
                }

                if (rightX >= chunkMinX && rightX <= chunkMaxX) {
                    for (int z = zStart; z <= zStop; z++) {
                        char buildChar = layer.getColumn(progressY, fullY, z - worldStartZ, worldStopZ - worldStartZ,
                                row);
                        BlockState blockState = this.theme.getWall(this, buildChar, random);
                        if (blockState.getBlock() != Blocks.CAVE_AIR) {
                            this.placeBlock(worldWriter, chunkPos, new BlockPos(rightX, y, z), blockState,
                                    Direction.WEST, random);
                        }
                    }
                }
            }
        }
    }

    /**
     * Builds the ceiling of this sector from within the chunk position.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for
     *                    placement during WorldGen.
     * @param chunkPos    The chunk position to build within.
     * @param random      The instance of random, used for characters that are
     *                    random.
     */
    public void buildCeiling(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();
        int worldStartX = Math.min(startPos.getX(), stopPos.getX());
        int worldStopX = Math.max(startPos.getX(), stopPos.getX());
        int worldStartY = Math.min(startPos.getY(), stopPos.getY());
        int worldStopY = Math.max(startPos.getY(), stopPos.getY());
        int worldStartZ = Math.min(startPos.getZ(), stopPos.getZ());
        int worldStopZ = Math.max(startPos.getZ(), stopPos.getZ());

        int startX = Math.max(worldStartX, chunkPos.getMinBlockX());
        int stopX = Math.min(worldStopX, chunkPos.getMaxBlockX());
        int startZ = Math.max(worldStartZ, chunkPos.getMinBlockZ());
        int stopZ = Math.min(worldStopZ, chunkPos.getMaxBlockZ());

        if (startX > stopX || startZ > stopZ) {
            return;
        }

        for (int layerIndex : this.dungeonSector.ceiling.layers.keySet()) {
            int y = worldStopY + layerIndex;
            if (y <= 0 || y >= world.getMaxBuildHeight()) {
                continue;
            }
            SectorLayer layer = this.dungeonSector.ceiling.layers.get(layerIndex);
            for (int x = startX; x <= stopX; x++) {
                List<Character> row = layer.getRow(x - worldStartX, worldStopX - worldStartX);
                for (int z = startZ; z <= stopZ; z++) {
                    char buildChar = layer.getColumn(x - worldStartX, worldStopX - worldStartX, z - worldStartZ,
                            worldStopZ - worldStartZ, row);
                    BlockPos buildPos = new BlockPos(x, y, z);
                    BlockState blockState = this.theme.getCeiling(this, buildChar, random);
                    if (blockState.getBlock() != Blocks.CAVE_AIR) {
                        this.placeBlock(worldWriter, chunkPos, buildPos, blockState, Direction.DOWN, random);
                    }
                }
            }
        }
    }

    /**
     * Builds the entrances of this sector from within the chunk position.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for
     *                    placement during WorldGen.
     * @param chunkPos    The chunk position to build within.
     * @param random      The instance of random, used for characters that are
     *                    random.
     */
    public void buildEntrances(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        this.parentConnector.buildEntrance(worldWriter, world, chunkPos, random);
    }

    /**
     * Builds a set of stairs leading down to a lower room to start the next level.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for
     *                    placement during WorldGen.
     * @param chunkPos    The chunk position to build within.
     * @param random      The instance of random, used for characters that are
     *                    random.
     */
    public void buildStairs(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();

        Vector3i size = this.getRoomSize();
        int centerX = startPos.getX() + Math.round((float) size.x() / 2);
        int centerZ = startPos.getZ() + Math.round((float) size.z() / 2);

        int stairsHeight = this.parentConnector.parentSector.getOccupiedSize().y() - 1;
        if (this.dungeonSector.type.equalsIgnoreCase("stairs")) {
            stairsHeight = size.y() * 2;
        }

        int worldStartX = centerX - 1;
        int worldStopX = centerX + 1;
        int worldStartZ = centerZ - 1;
        int worldStopZ = centerZ + 1;

        int startY = Math.min(startPos.getY(), stopPos.getY());
        int stopY = Math.max(1, startPos.getY() - stairsHeight);

        int startX = Math.max(worldStartX, chunkPos.getMinBlockX());
        int stopX = Math.min(worldStopX, chunkPos.getMaxBlockX());
        int startZ = Math.max(worldStartZ, chunkPos.getMinBlockZ());
        int stopZ = Math.min(worldStopZ, chunkPos.getMaxBlockZ());

        if (startX > stopX || startZ > stopZ) {
            return;
        }

        BlockState floorBlockState = this.theme.getFloor(this, 'B', random);
        BlockState stairsBlockState = this.stairBlock;

        for (int y = startY; y >= stopY; y--) {
            if (y <= 0 || y >= world.getMaxBuildHeight())
                continue;
            for (int x = startX; x <= stopX; x++) {
                for (int z = startZ; z <= stopZ; z++) {
                    BlockState blockState = Blocks.CAVE_AIR.defaultBlockState();

                    if (x == centerX && z == centerZ) {
                        blockState = this.theme.getWall(this, 'B', random);
                    }

                    int step = y % 8;
                    int offsetX = x - worldStartX;
                    int offsetZ = z - worldStartZ;

                    if (step % 4 == 3) {
                        if (offsetX == 0 && offsetZ == 0) {
                            blockState = floorBlockState;
                        } else if (offsetX == 0 && offsetZ == 1) {
                            blockState = stairsBlockState;
                        }
                    }
                    if (step % 4 == 2) {
                        if (offsetX == 0 && offsetZ == 2) {
                            blockState = floorBlockState;
                        } else if (offsetX == 1 && offsetZ == 2) {
                            blockState = stairsBlockState.setValue(StairBlock.FACING, Direction.WEST);
                        }
                    }
                    if (step % 4 == 1) {
                        if (offsetX == 2 && offsetZ == 2) {
                            blockState = floorBlockState;
                        } else if (offsetX == 2 && offsetZ == 1) {
                            blockState = stairsBlockState.setValue(StairBlock.FACING, Direction.SOUTH);
                        }
                    }
                    if (step % 4 == 0) {
                        if (offsetX == 2 && offsetZ == 0) {
                            blockState = floorBlockState;
                        } else if (offsetX == 1 && offsetZ == 0) {
                            blockState = stairsBlockState.setValue(StairBlock.FACING, Direction.EAST);
                        }
                    }

                    BlockPos buildPos = new BlockPos(x, y, z);
                    this.placeBlock(worldWriter, chunkPos, buildPos, blockState, Direction.UP, random);
                }
            }
        }
    }

    /**
     * Spawns a mob in this sector.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for
     *                    placement during WorldGen.
     * @param chunkPos    The chunk position to spawn within.
     * @param blockPos    The position to spawn the mob at.
     * @param mobSpawn    The Mob Spawn entry to use.
     * @param random      The instance of random, used for mob vacations where
     *                    applicable.
     */
    public void spawnMob(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, BlockPos blockPos,
                         MobSpawn mobSpawn, RandomSource random) {
        int chunkOffset = 8;
        if (blockPos.getX() < chunkPos.getMinBlockX() + chunkOffset
                || blockPos.getX() > chunkPos.getMaxBlockX() + chunkOffset) {
            return;
        }
        if (blockPos.getY() < worldWriter.getMinBuildHeight() || blockPos.getY() >= worldWriter.getMaxBuildHeight()) {
            return;
        }
        if (blockPos.getZ() < chunkPos.getMinBlockZ() + chunkOffset
                || blockPos.getZ() > chunkPos.getMaxBlockZ() + chunkOffset) {
            return;
        }


        Level entityWorld = (world != null) ? world
                : (worldWriter instanceof ServerLevelAccessor ? ((ServerLevelAccessor) worldWriter).getLevel() : null);
        if (entityWorld == null) {
            return;
        }

        LivingEntity entityLiving = mobSpawn.createEntity(entityWorld);
        if (entityLiving == null) {
            return;
        }
        entityLiving.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        if (entityLiving instanceof BaseCreatureEntity entityCreature) {
            Vector3i size = this.getRoomSize();
            int radius = Math.max(3, Math.max(size.x(), size.z()));
            entityCreature.setHome(blockPos.getX(), blockPos.getY(), blockPos.getZ(), radius);
        }

        mobSpawn.onSpawned(entityLiving, null);
        if (entityWorld instanceof ServerLevel serverLevel) {
            DeferredLevelActionManager.enqueue(serverLevel, blockPos,
                    "dungeon_room_spawn:" + this.hashCode() + ":" + blockPos.asLong(),
                    level -> level.addFreshEntity(entityLiving));
            return;
        }
        worldWriter.addFreshEntity(entityLiving);
    }

    /**
     * Formats this object into a String.
     *
     * @return A formatted string description of this object.
     */
    @Override
    public String toString() {
        String bounds = "";
        String size = "";
        if (this.parentConnector != null) {
            bounds = " Bounds: " + this.getOccupiedBoundsMin() + " to " + this.getOccupiedBoundsMax();
            size = " Occupies: " + this.getOccupiedSize();
        }
        return "Sector Instance Type: " + (this.dungeonSector == null ? "Unset" : this.dungeonSector.type)
                + " Parent Connector Pos: " + (this.parentConnector == null ? "Unset" : this.parentConnector.position)
                + size + bounds;
    }

    public void generateBuildPlan(DungeonBuildPlan plan) {
        RandomSource random = RandomSource.create(this.layout.dungeonInstance.seed ^ this.hashCode());
        this.planClearArea(plan, random);
        this.planFloor(plan, random, 0);
        this.planWalls(plan, random);
        this.planCeiling(plan, random);
        if ("stairs".equalsIgnoreCase(this.dungeonSector.type)) {
            this.planStairs(plan, random);
            this.planFloor(plan, random, -(this.getRoomSize().y() * 2));
        }
        if ("tower".equalsIgnoreCase(this.dungeonSector.type)) {
            this.planStairs(plan, random);
        }
    }

    private void planClearArea(DungeonBuildPlan plan, RandomSource random) {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();
        int startX = Math.min(startPos.getX(), stopPos.getX());
        int stopX = Math.max(startPos.getX(), stopPos.getX());
        int startY = Math.min(startPos.getY(), stopPos.getY());
        int stopY = Math.max(startPos.getY(), stopPos.getY());
        int startZ = Math.min(startPos.getZ(), stopPos.getZ());
        int stopZ = Math.max(startPos.getZ(), stopPos.getZ());

        if ("stairs".equalsIgnoreCase(this.dungeonSector.type)) {
            startY = Math.max(1, startPos.getY() - (this.getRoomSize().y() * 2));
        }

        for (int x = startX; x <= stopX; x++) {
            for (int y = startY; y <= stopY; y++) {
                if (y <= 0 || y >= this.layout.dungeonInstance.world.getMaxBuildHeight())
                    continue;
                for (int z = startZ; z <= stopZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    ChunkPos chunkPos = new ChunkPos(pos);
                    BlockState state = Blocks.CAVE_AIR.defaultBlockState();
                    DungeonBuildPlan.PlannedBlock pb = new DungeonBuildPlan.PlannedBlock(pos, state, Direction.SOUTH);
                    plan.add(chunkPos, pb);
                }
            }
        }
    }

    private void planFloor(DungeonBuildPlan plan, RandomSource random, int offsetY) {
        BlockPos startPos = this.getRoomBoundsMin().offset(0, offsetY, 0);
        BlockPos stopPos = this.getRoomBoundsMax().offset(0, offsetY, 0);
        int startX = Math.min(startPos.getX(), stopPos.getX());
        int stopX = Math.max(startPos.getX(), stopPos.getX());
        int startY = Math.min(startPos.getY(), stopPos.getY());
        int stopY = Math.max(startPos.getY(), stopPos.getY());
        int startZ = Math.min(startPos.getZ(), stopPos.getZ());
        int stopZ = Math.max(startPos.getZ(), stopPos.getZ());

        for (int layerIndex : this.dungeonSector.floor.layers.keySet()) {
            int y = startY + layerIndex;
            if (y <= 0 || y >= this.layout.dungeonInstance.world.getMaxBuildHeight())
                continue;
            SectorLayer layer = this.dungeonSector.floor.layers.get(layerIndex);
            for (int x = startX; x <= stopX; x++) {
                List<Character> row = layer.getRow(x - startX, stopX - startX);
                for (int z = startZ; z <= stopZ; z++) {
                    char c = layer.getColumn(x - startX, stopX - startX, z - startZ, stopZ - startZ, row);
                    BlockState state = this.theme.getFloor(this, c, random);
                    if (state.getBlock() == Blocks.CAVE_AIR)
                        continue;
                    BlockPos pos = new BlockPos(x, y, z);
                    ChunkPos chunkPos = new ChunkPos(pos);
                    DungeonBuildPlan.PlannedBlock pb = new DungeonBuildPlan.PlannedBlock(pos, state, Direction.UP);
                    plan.add(chunkPos, pb);
                }
            }
        }
    }

    private void planWalls(DungeonBuildPlan plan, RandomSource random) {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();

        Vector3i size = this.getRoomSize();
        int startX = Math.min(startPos.getX(), stopPos.getX());
        int stopX = Math.max(startPos.getX(), stopPos.getX());
        int startY = Math.min(startPos.getY() + 1, stopPos.getY());
        int stopY = Math.max(startPos.getY() - 1, stopPos.getY());
        int startZ = Math.min(startPos.getZ(), stopPos.getZ());
        int stopZ = Math.max(startPos.getZ(), stopPos.getZ());

        if ("stairs".equalsIgnoreCase(this.dungeonSector.type)) {
            startY = Math.max(1, startPos.getY() - (size.y() * 2));
        }

        int maxHeight = this.layout.dungeonInstance.world.getMaxBuildHeight();

        for (int layerIndex : this.dungeonSector.wall.layers.keySet()) {
            SectorLayer layer = this.dungeonSector.wall.layers.get(layerIndex);
            for (int y = startY; y <= stopY; y++) {
                if (y <= 0 || y >= maxHeight) {
                    continue;
                }

                int progressY = y - startY;
                int fullY = stopY - startY;
                List<Character> row = layer.getRow(progressY, fullY);

                for (int x = startX; x <= stopX; x++) {
                    char buildChar = layer.getColumn(progressY, fullY, x - startX, stopX - startX, row);
                    BlockState blockState = this.theme.getWall(this, buildChar, random);
                    if (blockState.getBlock() != Blocks.CAVE_AIR) {
                        BlockPos posFront = new BlockPos(x, y, startZ + layerIndex);
                        ChunkPos chunkFront = new ChunkPos(posFront);
                        plan.add(chunkFront, new DungeonBuildPlan.PlannedBlock(posFront, blockState, Direction.SOUTH));

                        BlockPos posBack = new BlockPos(x, y, stopZ - layerIndex);
                        ChunkPos chunkBack = new ChunkPos(posBack);
                        plan.add(chunkBack, new DungeonBuildPlan.PlannedBlock(posBack, blockState, Direction.NORTH));
                    }
                }

                for (int z = startZ; z <= stopZ; z++) {
                    char buildChar = layer.getColumn(progressY, fullY, z - startZ, stopZ - startZ, row);
                    BlockState blockState = this.theme.getWall(this, buildChar, random);
                    if (blockState.getBlock() != Blocks.CAVE_AIR) {
                        BlockPos posLeft = new BlockPos(startX + layerIndex, y, z);
                        ChunkPos chunkLeft = new ChunkPos(posLeft);
                        plan.add(chunkLeft, new DungeonBuildPlan.PlannedBlock(posLeft, blockState, Direction.EAST));

                        BlockPos posRight = new BlockPos(stopX - layerIndex, y, z);
                        ChunkPos chunkRight = new ChunkPos(posRight);
                        plan.add(chunkRight, new DungeonBuildPlan.PlannedBlock(posRight, blockState, Direction.WEST));
                    }
                }
            }
        }
    }

    private void planCeiling(DungeonBuildPlan plan, RandomSource random) {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();
        int startX = Math.min(startPos.getX(), stopPos.getX());
        int stopX = Math.max(startPos.getX(), stopPos.getX());
        int startY = Math.min(startPos.getY(), stopPos.getY());
        int stopY = Math.max(startPos.getY(), stopPos.getY());
        int startZ = Math.min(startPos.getZ(), stopPos.getZ());
        int stopZ = Math.max(startPos.getZ(), stopPos.getZ());

        int maxHeight = this.layout.dungeonInstance.world.getMaxBuildHeight();

        for (int layerIndex : this.dungeonSector.ceiling.layers.keySet()) {
            int y = stopY + layerIndex;
            if (y <= 0 || y >= maxHeight) {
                continue;
            }
            SectorLayer layer = this.dungeonSector.ceiling.layers.get(layerIndex);
            for (int x = startX; x <= stopX; x++) {
                List<Character> row = layer.getRow(x - startX, stopX - startX);
                for (int z = startZ; z <= stopZ; z++) {
                    char buildChar = layer.getColumn(x - startX, stopX - startX, z - startZ, stopZ - startZ, row);
                    BlockState blockState = this.theme.getCeiling(this, buildChar, random);
                    if (blockState.getBlock() == Blocks.CAVE_AIR)
                        continue;

                    BlockPos buildPos = new BlockPos(x, y, z);
                    ChunkPos chunkPos = new ChunkPos(buildPos);
                    plan.add(chunkPos, new DungeonBuildPlan.PlannedBlock(buildPos, blockState, Direction.DOWN));
                }
            }
        }
    }

    private void planStairs(DungeonBuildPlan plan, RandomSource random) {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();

        Vector3i size = this.getRoomSize();
        int centerX = startPos.getX() + Math.round((float) size.x() / 2);
        int centerZ = startPos.getZ() + Math.round((float) size.z() / 2);

        int stairsHeight = this.parentConnector.parentSector.getOccupiedSize().y() - 1;
        if (this.dungeonSector.type.equalsIgnoreCase("stairs")) {
            stairsHeight = size.y() * 2;
        }
        int startX = centerX - 1;
        int stopX = centerX + 1;
        int startY = Math.min(startPos.getY(), stopPos.getY());
        int stopY = Math.max(1, startPos.getY() - stairsHeight);

        int startZ = centerZ - 1;
        int stopZ = centerZ + 1;

        int maxHeight = this.layout.dungeonInstance.world.getMaxBuildHeight();

        BlockState floorBlockState = this.theme.getFloor(this, 'B', random);
        BlockState stairsBlockState = this.stairBlock;

        for (int y = startY; y >= stopY; y--) {
            if (y <= 0 || y >= maxHeight)
                continue;

            for (int x = startX; x <= stopX; x++) {
                for (int z = startZ; z <= stopZ; z++) {
                    BlockState blockState = Blocks.CAVE_AIR.defaultBlockState();

                    if (x == centerX && z == centerZ) {
                        blockState = this.theme.getWall(this, 'B', random);
                    }

                    int step = y % 8;
                    int offsetX = x - startX;
                    int offsetZ = z - startZ;

                    if (step % 4 == 3) {
                        if (offsetX == 0 && offsetZ == 0) {
                            blockState = floorBlockState;
                        } else if (offsetX == 0 && offsetZ == 1) {
                            blockState = stairsBlockState;
                        }
                    }
                    if (step % 4 == 2) {
                        if (offsetX == 0 && offsetZ == 2) {
                            blockState = floorBlockState;
                        } else if (offsetX == 1 && offsetZ == 2) {
                            blockState = stairsBlockState.setValue(StairBlock.FACING, Direction.WEST);
                        }
                    }
                    if (step % 4 == 1) {
                        if (offsetX == 2 && offsetZ == 2) {
                            blockState = floorBlockState;
                        } else if (offsetX == 2 && offsetZ == 1) {
                            blockState = stairsBlockState.setValue(StairBlock.FACING, Direction.SOUTH);
                        }
                    }
                    if (step % 4 == 0) {
                        if (offsetX == 2 && offsetZ == 0) {
                            blockState = floorBlockState;
                        } else if (offsetX == 1 && offsetZ == 0) {
                            blockState = stairsBlockState.setValue(StairBlock.FACING, Direction.EAST);
                        }
                    }

                    if (blockState.getBlock() == Blocks.CAVE_AIR)
                        continue;

                    BlockPos buildPos = new BlockPos(x, y, z);
                    ChunkPos chunkPos = new ChunkPos(buildPos);
                    plan.add(chunkPos, new DungeonBuildPlan.PlannedBlock(buildPos, blockState, Direction.UP));
                }
            }
        }
    }

}
