package com.lycanitesmobs.core.worldgen.dungeon.instance;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class DungeonBuildPlan {
    private final Map<ChunkPos, List<PlannedBlock>> blocksByChunk = new HashMap<>();
    private boolean ready = false;

    public void add(ChunkPos chunkPos, PlannedBlock block) {
        List<PlannedBlock> list = this.blocksByChunk.computeIfAbsent(chunkPos, k -> new ArrayList<>());
        list.add(block);
    }

    public List<PlannedBlock> getForChunk(ChunkPos chunkPos) {
        return this.blocksByChunk.getOrDefault(chunkPos, Collections.emptyList());
    }

    public Set<ChunkPos> getPlannedChunks() {
        return this.blocksByChunk.keySet();
    }

    public boolean isReady() {
        return this.ready;
    }

    public void markReady() {
        this.ready = true;
    }

    public static class PlannedBlock {
        public final BlockPos pos;
        public final BlockState state;
        public final Direction facing;

        public PlannedBlock(BlockPos pos, BlockState state, Direction facing) {
            this.pos = pos;
            this.state = state;
            this.facing = facing;
        }

        public void apply(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random, SectorInstance sector) {
            LMHelperClass.logDebug(
                    "Dungeon",
                    "PlannedBlock.apply: " + state.getBlock() + " at " + pos + " in chunk " + chunkPos
            );
            sector.placeBlock(worldWriter, chunkPos, this.pos, this.state, this.facing, random);
        }
    }

}
