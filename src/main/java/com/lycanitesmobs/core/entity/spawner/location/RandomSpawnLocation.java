package com.lycanitesmobs.core.entity.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public class RandomSpawnLocation extends BlockSpawnLocation {

    /**
     * How many random positions to select.
     **/
    protected int limit = 32;

    /**
     * If true positions require a solid walkable block underneath rather than using insideBlock.
     **/
    protected boolean solidGround = false;

    /**
     * This scales the x and z range values by this amount when on Easy or Peaceful Difficulty.
     **/
    protected double easyDifficultyRangeScale = 1.5D;

    /**
     * This scales the x and z range values by this amount when on Normal Difficulty.
     **/
    protected double normalDifficultyRangeScale = 1D;

    /**
     * This scales the x and z range values by this amount when on Hard and above Difficulties.
     **/
    protected double hardDifficultyRangeScale = 0.5D;


    @Override
    public void loadFromJSON(JsonObject json) {
        if (json.has("limit"))
            this.limit = json.get("limit").getAsInt();

        if (json.has("solidGround"))
            this.solidGround = json.get("solidGround").getAsBoolean();

        if (json.has("easyDifficultyRangeScale"))
            this.easyDifficultyRangeScale = json.get("easyDifficultyRangeScale").getAsDouble();

        if (json.has("normalDifficultyRangeScale"))
            this.normalDifficultyRangeScale = json.get("normalDifficultyRangeScale").getAsDouble();

        if (json.has("hardDifficultyRangeScale"))
            this.hardDifficultyRangeScale = json.get("hardDifficultyRangeScale").getAsDouble();

        this.listType = "whitelist";
        this.blockIds.add(LMHelperClass.convertToResourceLocation(Blocks.AIR).toString());
        this.blockIds.add(LMHelperClass.convertToResourceLocation(Blocks.CAVE_AIR).toString());
        this.blockIds.add(LMHelperClass.convertToResourceLocation(Blocks.TALL_GRASS).toString());
        super.loadFromJSON(json);
    }

    @Override
    public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
        List<BlockPos> spawnPositions = new ArrayList<>();

        for (int i = 0; i < this.limit; i++) {
            BlockPos randomPos = this.getRandomPosition(world, player, triggerPos);
            if (randomPos != null) {
                spawnPositions.add(randomPos);
            }
        }

        return this.sortSpawnPositions(spawnPositions, world, triggerPos);
    }


    /**
     * Gets a random spawn position. Handles three cases:
     * 1. Surface-only (surface=true, underground=false): Uses heightmap to find ground level.
     * 2. Underground-only (surface=false, underground=true) with yMax set: Y scan down from yMax.
     * 3. Both (surface=true, underground=true): Y scan centred on heightmap Y so land/surface
     *    spawners still populate the overworld surface even when the trigger player is deep
     *    underground. Falls back to player Y when the heightmap is outside the Y scan range
     *    (so tight-range player-centred spawners like `darkness` still work for cave players).
     *
     * @param world      The world to search for coordinates in.
     * @param player     Player that triggered the spawn.
     * @param triggerPos The trigger position to search around.
     * @return Returns a BlockPos or null if no coord was found.
     */
    public BlockPos getRandomPosition(Level world, Player player, BlockPos triggerPos) {
        int[] xz = this.getRandomXZCoord(world, triggerPos);
        int x = xz[0];
        int z = xz[1];

        if (!world.getChunkSource().hasChunk(x >> 4, z >> 4)) {
            return null;
        }

        int heightmapY = Integer.MIN_VALUE;
        if (this.surface || this.underground) {
            if (this.surface) {
                heightmapY = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY();
            }
        }

        BlockPos surfacePos = null;
        if (this.surface && heightmapY != Integer.MIN_VALUE) {
            surfacePos = this.findSurfaceSpawnPos(world, x, z, heightmapY);
        }

        if (this.surface && !this.underground) {
            return surfacePos;
        }

        int scanOriginY;
        if (!this.surface && this.underground && this.yMax >= 0) {
            // Underground-only with an explicit yMax cap: scan down from yMax.
            scanOriginY = this.yMax;
        } else if (this.surface && heightmapY != Integer.MIN_VALUE
                && Math.abs(heightmapY - triggerPos.getY()) <= this.rangeMax.getY()) {
            // Surface+underground AND the heightmap is within reach of the Y scan range:
            // anchor on heightmap so wide-range land spawners keep their surface bias.
            scanOriginY = heightmapY;
        } else {
            // Fallback: use the player's actual Y. Covers underground-only without yMax,
            // and tight-range surface+underground spawners where heightmap is unreachable
            // (e.g. `darkness` with rangeMax.Y=2 and the player deep in a cave).
            scanOriginY = triggerPos.getY();
        }

        int y = this.getRandomYCoord(world, new BlockPos(x, scanOriginY, z));
        if (y == Integer.MIN_VALUE) {
            return surfacePos;
        }

        BlockPos feetPos = new BlockPos(x, y, z);
        BlockPos headPos = feetPos.above();

        if (!this.isValidBlock(world, feetPos)) {
            return surfacePos;
        }

        if (!super.isValidBlock(world, headPos)) {
            return surfacePos;
        }

        return feetPos;
    }


    /**
     * Finds a valid surface spawn position at the given XZ using a pre-computed heightmap Y.
     *
     * @param world      The world to search in.
     * @param x          The X coordinate.
     * @param z          The Z coordinate.
     * @param heightmapY The pre-computed heightmap Y for this x,z.
     * @return A valid surface BlockPos, or null if no valid position found.
     */
    private BlockPos findSurfaceSpawnPos(Level world, int x, int z, int heightmapY) {
        BlockPos groundPos = new BlockPos(x, heightmapY, z);
        if (this.solidGround && !world.getBlockState(groundPos.below()).canOcclude()) {
            return null;
        }

        if (heightmapY < world.getMinBuildHeight() || heightmapY >= world.getMaxBuildHeight()) {
            return null;
        }

        if (this.yMin >= 0 && heightmapY < this.yMin) {
            return null;
        }
        if (this.yMax >= 0 && heightmapY > this.yMax) {
            return null;
        }

        BlockPos feetPos = new BlockPos(x, heightmapY, z);
        BlockPos headPos = feetPos.above();

        if (!this.isValidBlock(world, feetPos)) {
            return null;
        }

        if (!super.isValidBlock(world, headPos)) {
            return null;
        }

        return feetPos;
    }

    /**
     * Gets a random XZ position from the trigger position.
     *
     * @param world      The world that the coordinates are being selected in, mainly for getting Random.
     * @param triggerPos The trigger position to randomize around.
     * @return An integer array containing two ints the X and Z position.
     */
    public int[] getRandomXZCoord(Level world, BlockPos triggerPos) {
        double difficultyScale = this.normalDifficultyRangeScale;
        if (world.getDifficulty().getId() <= 1) {
            difficultyScale = this.easyDifficultyRangeScale;
        } else if (world.getDifficulty().getId() >= 3) {
            difficultyScale = this.hardDifficultyRangeScale;
        }

        int xPos = 0;
        int rangeMaxX = Math.round((float) this.rangeMax.getX() * (float) difficultyScale);
        int rangeMinX = Math.round((float) this.rangeMin.getX() * (float) difficultyScale);
        if (rangeMaxX * difficultyScale > 0) {
            xPos = world.random.nextInt(rangeMaxX);
            if (world.random.nextBoolean()) {
                xPos += rangeMinX;
            } else {
                xPos = -xPos - rangeMinX;
            }
        }

        int zPos = 0;
        int rangeMaxZ = Math.round((float) this.rangeMax.getZ() * (float) difficultyScale);
        int rangeMinZ = Math.round((float) this.rangeMin.getZ() * (float) difficultyScale);
        if (rangeMaxZ * difficultyScale > 0) {
            zPos = world.random.nextInt(rangeMaxZ);
            if (world.random.nextBoolean()) {
                zPos += rangeMinZ;
            } else {
                zPos = -zPos - rangeMinZ;
            }
        }

        return new int[]{triggerPos.getX() + xPos, triggerPos.getZ() + zPos};
    }

    /**
     * Gets a random Y position from the provided XYZ position using the provided range and range max radii.
     *
     * @param world      The world that the coordinates are being selected in, mainly for getting Random.
     * @param triggerPos The position to search from using XZ coords and up and down within range of the Y coord.
     * @return The y position, Integer.MIN_VALUE if a valid position could not be found.
     */
    public int getRandomYCoord(Level world, BlockPos triggerPos) {
        double difficultyScale = this.normalDifficultyRangeScale;
        if (world.getDifficulty().getId() <= 1) {
            difficultyScale = this.easyDifficultyRangeScale;
        } else if (world.getDifficulty().getId() >= 3) {
            difficultyScale = this.hardDifficultyRangeScale;
        }
        int rangeMaxY = Math.round((float) this.rangeMax.getY() * (float) difficultyScale);
        int rangeMinY = Math.round((float) this.rangeMin.getY() * (float) difficultyScale);

        int originX = triggerPos.getX();
        int originY = triggerPos.getY();
        int originZ = triggerPos.getZ();

        int minY = Math.max(originY - rangeMaxY, world.getMinBuildHeight());
        if (this.yMin >= 0) {
            minY = Math.max(minY, this.yMin);
        }
        int maxY = Math.min(originY + rangeMaxY, world.getMaxBuildHeight() - 1);
        if (this.yMax >= 0) {
            maxY = Math.min(maxY, this.yMax);
        }

        int reservoirLow = Integer.MIN_VALUE;
        int countLow = 0;
        int reservoirHigh = Integer.MIN_VALUE;
        int countHigh = 0;

        BlockPos.MutableBlockPos spawnPos = new BlockPos.MutableBlockPos(originX, minY, originZ);
        BlockPos.MutableBlockPos headPos = new BlockPos.MutableBlockPos(originX, minY + 1, originZ);

        for (int nextY = minY; nextY <= maxY; nextY++) {
            if (nextY > originY - rangeMinY && nextY < originY + rangeMinY)
                nextY = originY + rangeMinY;

            spawnPos.setY(nextY);
            headPos.setY(nextY + 1);

            if (this.isValidBlock(world, spawnPos)) {
                boolean lastYPos = false;

                int heightmapSurfaceY = world.getHeight(Heightmap.Types.OCEAN_FLOOR, spawnPos.getX(), spawnPos.getZ());
                if (nextY >= heightmapSurfaceY) {
                    if (!this.solidGround) {
                        int floatRange = maxY - nextY;
                        if (floatRange > 1) {
                            if (world.getBlockState(spawnPos).getBlock() != Blocks.AIR) {
                                floatRange = this.getValidBlockHeight(world, spawnPos, maxY);
                            }
                            nextY += world.random.nextInt(floatRange + 1) - 1;
                            spawnPos.setY(nextY);
                            headPos.setY(nextY + 1);
                        }
                    }
                    lastYPos = true;
                }

                if (super.isValidBlock(world, headPos)) {
                    if (nextY <= 64) {
                        countLow++;
                        if (world.random.nextInt(countLow) == 0) reservoirLow = nextY;
                    } else {
                        countHigh++;
                        if (world.random.nextInt(countHigh) == 0) reservoirHigh = nextY;
                    }
                }

                if (lastYPos) {
                    break;
                }
            }
        }

        if (reservoirHigh != Integer.MIN_VALUE && (countLow == 0 || world.random.nextFloat() > 0.25F)) {
            return reservoirHigh;
        } else if (reservoirLow != Integer.MIN_VALUE) {
            return reservoirLow;
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Returns if the provided block position is valid.
     **/
    @Override
    public boolean isValidBlock(Level world, BlockPos blockPos) {
        if (!super.isValidBlock(world, blockPos)) {
            return false;
        }
        if (this.solidGround) {
            boolean groundOk = this.posHasGround(world, blockPos);
            return groundOk;
        }
        return true;
    }


    /**
     * Returns true if the specified position has a block underneath it that a mob can safely stand on.
     **/
    public boolean posHasGround(Level world, BlockPos pos) {
        if (pos == null || pos.getY() <= world.getMinBuildHeight())
            return false;
        BlockState possibleGroundBlock = world.getBlockState(pos.below());
        try {
            if (possibleGroundBlock.canOcclude())
                return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Returns the height of valid blocks from the starting position checking upwards until the position no longer has a valid block or maxY is reached.
     **/
    public int getValidBlockHeight(Level world, BlockPos startPos, int maxY) {
        int y;
        for (y = startPos.getY(); y <= maxY; y++) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            if (!this.isValidBlock(world, checkPos)) {
                break;
            }
        }
        return y - startPos.getY();
    }
}
