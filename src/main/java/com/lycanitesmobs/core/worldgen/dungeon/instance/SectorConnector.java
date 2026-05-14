package com.lycanitesmobs.core.worldgen.dungeon.instance;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3i;

public class SectorConnector {
	/** Sector Connectors link together one sector to another, each sector type has one or more connectors as well as a parent connector. */

	/** The block position of this connector. **/
	public BlockPos position;

	/** The Sector Instance that this connector belongs to. Null when this is used as the starting connector. **/
	public SectorInstance parentSector;

	/** The Sector Instance that is connected to this connector. Can be null if the connection is empty. **/
	public SectorInstance childSector;

	/** The facing direction of this connector. **/
	public Direction facing;

	/** Set to true when this connector is connected to a child sector or cannot be connected to anything (usually due to collisions). **/
	public boolean closed = false;

	/** The dungeon level that this connector is on. **/
	public int level = 0;


	/**
	 * Constructor
	 * @param position The block position of this connector.
	 * @param parentSector The sector that this connector belongs to. If null, collision checks are skipped.
	 * @param level The level that this connector is on.
	 * @param facing The facing of this sector (applied to child sectors).
	 */
	public SectorConnector(BlockPos position, SectorInstance parentSector, int level, Direction facing) {
		this.position = position;
		this.parentSector = parentSector;
		this.level = level;
		this.facing = facing;
	}


	/**
	 * Returns true if this connector can be connected to by the sector instance, checks for sector collisions.
	 * @param dungeonLayout The dungeon layout using this connector.
	 * @param sectorInstance If set, the specific sector instance to connect to, if null then collision checks are ignored.
	 * @return True if the sector instance can connect to this connector.
	 */
	public boolean canConnect(DungeonLayout dungeonLayout, SectorInstance sectorInstance) {
		if(this.closed) {
			return false;
		}
		if(sectorInstance == null) {
			return this.facing != Direction.UP;
		}

		// Vertical Connectors:
		if("tower".equalsIgnoreCase(sectorInstance.dungeonSector.type)) {
			return this.facing == Direction.UP;
		}
		if(this.facing == Direction.UP) {
			return false;
		}

		// Temp Set Connector:
		boolean tempConnector = false;
		if(sectorInstance.parentConnector == null) {
			sectorInstance.parentConnector = this;
			tempConnector = true;
		}

		// Connect To Sector Instance:
		for(SectorInstance nearbySector : sectorInstance.getNearbySectors()) {
			if(sectorInstance.collidesWith(nearbySector)) {
				if(tempConnector) {
					sectorInstance.parentConnector = null;
				}
				return false;
			}
		}

		if(tempConnector) {
			sectorInstance.parentConnector = null;
		}
		return true;
	}


	/**
	 * Builds an entrance at this connectors location within the chunk position.
	 * @param world The world to build in.
	 * @param chunkPos The chunk position to build within.
	 * @param random The instance of random to use.
	 */
	public void buildEntrance(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
		SectorInstance sectorInstance = this.parentSector;
		if(sectorInstance == null) {
			sectorInstance = this.childSector;
		}
		if(sectorInstance == null) {
			return;
		}

		// Get Position and Size:
		Vector3i size = sectorInstance.getRoomSize();
		if(this.childSector != null && sectorInstance != this.childSector) {
			Vector3i childSize = this.childSector.getRoomSize();
			size = new Vector3i(Math.min(size.x(), childSize.x()), Math.min(size.y(), childSize.y()), Math.min(size.z(), childSize.z()));
		}
		int entranceHeight = 2;
		int entranceRadius = 1;
		int entranceClearance = 1;
		int startX = this.position.getX();
		int stopX = this.position.getX();
		int startY = Math.max(1, this.position.getY() + 1);
		int stopY = Math.min(world.getMaxBuildHeight() - 1, this.position.getY() + 1 + entranceHeight);
		int startZ = this.position.getZ();
		int stopZ = this.position.getZ();

		// Calculate Rotation:
		if(this.facing == Direction.SOUTH || this.facing == Direction.NORTH) {
			startX -= entranceRadius;
			stopX += entranceRadius;
			if(size.x() % 2 != 0) {
				startX -= 1;
			}
			startZ -= entranceClearance * 2;
			stopZ += entranceClearance * 2;
		}
		else {
			startZ -= entranceRadius;
			stopZ += entranceRadius;
			if(size.z() % 2 != 0) {
				startZ -= 1;
			}
			startX -= entranceClearance * 2;
			stopX += entranceClearance * 2;
		}

		// Build Entrance:
		for(int x = startX; x <= stopX; x++) {
			for(int y = startY; y <= stopY; y++) {
				for(int z = startZ; z <= stopZ; z++) {
					sectorInstance.placeBlock(worldWriter, chunkPos, new BlockPos(x, y, z), Blocks.CAVE_AIR.defaultBlockState(), this.facing, random);
				}
			}
		}
	}


	/**
	 * Builds a test marker showing where this connector is and its facing.
	 * @param worldWriter The world to create blocks in.
	 * @param world The world being built in. This cannot be used for placement during WorldGen.
	 * @param chunkPos The chunk position to build within.
	 * @param random The instance of random to use.
	 */
	public void buildTest(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
		SectorInstance sectorInstance = this.parentSector;
		if(sectorInstance == null) {
			sectorInstance = this.childSector;
		}
		if(sectorInstance == null) {
			return;
		}

		// Build Center Block Marker:
		sectorInstance.placeBlock(worldWriter, chunkPos, this.position.offset(0, 1, 0), Blocks.GOLD_BLOCK.defaultBlockState(), Direction.SOUTH, random);

		// Build Rotation Block Markers:
		if(this.facing == Direction.SOUTH) {
			for(int z = 1; z <= 3; z++)
				sectorInstance.placeBlock(worldWriter, chunkPos, this.position.offset(0, 1, z), Blocks.REDSTONE_BLOCK.defaultBlockState(), Direction.SOUTH, random);
		}
		else if(this.facing == Direction.EAST) {
			for(int x = 1; x <= 3; x++)
				sectorInstance.placeBlock(worldWriter, chunkPos, this.position.offset(x, 1, 0), Blocks.REDSTONE_BLOCK.defaultBlockState(), Direction.SOUTH, random);
		}
		else if(this.facing == Direction.NORTH) {
			for(int z = -1; z >= -3; z--)
				sectorInstance.placeBlock(worldWriter, chunkPos, this.position.offset(0, 1, z), Blocks.REDSTONE_BLOCK.defaultBlockState(), Direction.SOUTH, random);
		}
		else {
			for(int x = -1; x >= -3; x--)
				sectorInstance.placeBlock(worldWriter, chunkPos, this.position.offset(x, 1, 0), Blocks.REDSTONE_BLOCK.defaultBlockState(), Direction.SOUTH, random);
		}
	}
}
