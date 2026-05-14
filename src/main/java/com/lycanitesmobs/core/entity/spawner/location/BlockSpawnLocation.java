package com.lycanitesmobs.core.entity.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockSpawnLocation extends SpawnLocation {
    /**
     * A set of blocks to either spawn in or not spawn in depending on if it is a blacklist or whitelist.
     * HashSet for O(1) lookup in isValidBlock, which is called for every Y position in every spawn candidate.
     **/
    public Set<String> blockIds = new HashSet<>();

    /**
     * Determines if the block list is a blacklist or whitelist.
     **/
    public String listType = "whitelist";

    /**
     * If true, positions that can see the sky are allowed.
     **/
    public boolean surface = true;

    /**
     * If true positions that can't see the sky are allowed.
     **/
    public boolean underground = true;

    /**
     * The minimum amount of blocks that must be found in an area for the location to return any positions. Default -1 (ignore).
     **/
    public int blockCost = -1;

    /**
     * If set (above 0), the amount of each block in the blocks list must be found. Default: 0 (disabled)
     **/
    public int requiredBlockTypes = 0;

    /**
     * An offset relative to the spawn block to apply.
     **/
    public Vec3i offset = new Vec3i(0, 0, 0);

    /**
     * Cache for Block → registry ID string lookups. Avoids repeated registry lookups per block
     * in the hot triple-nested scanning loop. Safe to cache since the block registry is immutable at runtime.
     **/
    private final Map<Block, String> blockIdCache = new HashMap<>();


    @Override
    public void loadFromJSON(JsonObject json) {
        if (json.has("blocks"))
            this.blockIds = new HashSet<>(JSONHelper.getJsonStrings(json.get("blocks").getAsJsonArray()));

        if (json.has("listType"))
            this.listType = json.get("listType").getAsString();

        if (json.has("surface"))
            this.surface = json.get("surface").getAsBoolean();

        if (json.has("underground"))
            this.underground = json.get("underground").getAsBoolean();

        if (json.has("blockCost"))
            this.blockCost = json.get("blockCost").getAsInt();

        if (json.has("requiredBlockTypes"))
            this.requiredBlockTypes = json.get("requiredBlockTypes").getAsInt();

        this.offset = JSONHelper.getVector3i(json, "offset");

        super.loadFromJSON(json);
    }

    /**
     * Returns a list of positions to spawn at.
     **/
    @Override
    public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
        List<BlockPos> spawnPositions = new ArrayList<>();
        Map<Block, Integer> validBlocksFound = new HashMap<>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        int yMin = 0;
        if (this.yMin >= 0) {
            yMin = this.yMin;
        }
        int yMax = world.getMaxBuildHeight();
        if (this.yMax >= 0) {
            yMax = Math.min(this.yMax, yMax);
        }

        int startY = Math.max(triggerPos.getY() - this.rangeMax.getY(), yMin);
        int endY = Math.min(triggerPos.getY() + this.rangeMax.getY(), yMax - 1);

        for (int y = startY; y <= endY; y++) {
            for (int x = triggerPos.getX() - this.rangeMax.getX(); x <= triggerPos.getX() + this.rangeMax.getX(); x++) {
                for (int z = triggerPos.getZ() - this.rangeMax.getZ(); z <= triggerPos.getZ() + this.rangeMax.getZ(); z++) {
                    mutablePos.set(x, y, z);
                    BlockState blockState = world.getBlockState(mutablePos);

                    // Ignore Flowing Liquids:
                    if (blockState.getBlock() instanceof IFluidBlock) {
                        float filled = ((IFluidBlock) blockState.getBlock()).getFilledPercentage(world, mutablePos);
                        if (filled != 1 && filled != -1) {
                            continue;
                        }
                    }

                    // Check Block:
                    if (this.isValidBlock(world, mutablePos, blockState)) {
                        BlockPos immutablePos = mutablePos.immutable();
                        spawnPositions.add(immutablePos.offset(this.offset));

                        // Require All:
                        if (this.requiredBlockTypes > 0) {
                            validBlocksFound.merge(blockState.getBlock(), 1, Integer::sum);
                        }
                    }
                }
            }
        }

        // Block Cost:
        if (this.blockCost > 0) {
            if (spawnPositions.size() < this.blockCost) {
                return new ArrayList<>();
            }
        }

        // Require All Block Types:
        if (this.requiredBlockTypes > 0) {
            if (validBlocksFound.size() < this.blockIds.size()) {
                return new ArrayList<>();
            }
            for (int blocksFoundOfType : validBlocksFound.values()) {
                if (blocksFoundOfType < this.requiredBlockTypes) {
                    return new ArrayList<>();
                }
            }
        }

        return this.sortSpawnPositions(spawnPositions, world, triggerPos);
    }

    /**
     * Returns if the provided block position is valid.
     **/
    public boolean isValidBlock(Level world, BlockPos blockPos) {
        return this.isValidBlock(world, blockPos, world.getBlockState(blockPos));
    }

    /**
     * Returns if the provided block position is valid using a pre-fetched BlockState.
     * This avoids a redundant world.getBlockState() call when the caller already has the state.
     **/
    public boolean isValidBlock(Level world, BlockPos blockPos, BlockState blockState) {
        if (!this.surface || !this.underground) {
            int surfaceY = world.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX(), blockPos.getZ());
            boolean isSurface = blockPos.getY() >= surfaceY;
            if (isSurface) {
                if (!this.surface) {
                    return false;
                }
            } else {
                if (!this.underground) {
                    return false;
                }
            }
        }

        if (this.blockIds.isEmpty()) {
            return true;
        }
        Block block = blockState.getBlock();
        String blockId = this.blockIdCache.computeIfAbsent(block,
                b -> LMHelperClass.convertToResourceLocation(b).toString());

        if ("blacklist".equalsIgnoreCase(this.listType)) {
            return !this.blockIds.contains(blockId);
        } else {
            return this.blockIds.contains(blockId);
        }
    }
}
