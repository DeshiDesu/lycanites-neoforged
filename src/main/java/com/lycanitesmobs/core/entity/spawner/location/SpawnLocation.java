package com.lycanitesmobs.core.entity.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.entity.spawner.CoordSorterFurthest;
import com.lycanitesmobs.core.entity.spawner.CoordSorterNearest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnLocation {
    /** Spawn Locations define where spawns will take place, how these work can vary based on the type of Spawn Trigger. **/

    /**
     * The minimum xyz distances in blocks from the central spawn position to spawn from. Required.
     **/
    public Vec3i rangeMin = new Vec3i(0, 0, 0);

    /**
     * The maximum xyz distances in blocks from the central spawn position to spawn from. Required.
     **/
    public Vec3i rangeMax = new Vec3i(0, 0, 0);

    /**
     * The minimum allowed y height.
     **/
    public int yMin = -1;

    /**
     * The maximum allowed y height.
     **/
    public int yMax = -1;

    /**
     * Determines the order that the returned positions should be in. Can be: difficulty, random, near or far (from the trigger position).
     **/
    public String sorting = "difficulty";


    /**
     * Loads this Spawn Condition from the provided JSON data.
     **/
    public static SpawnLocation createFromJSON(JsonObject json) {
        String type = json.get("type").getAsString();
        SpawnLocation spawnLocation = null;

        if ("base".equalsIgnoreCase(type)) {
            spawnLocation = new SpawnLocation();
        } else if ("random".equalsIgnoreCase(type)) {
            spawnLocation = new RandomSpawnLocation();
        } else if ("block".equalsIgnoreCase(type)) {
            spawnLocation = new BlockSpawnLocation();
        } else if ("material".equalsIgnoreCase(type)) {
            spawnLocation = new MaterialSpawnLocation();
        } else if ("structure".equalsIgnoreCase(type)) {
            spawnLocation = new StructureSpawnLocation();
        }

        spawnLocation.loadFromJSON(json);
        return spawnLocation;
    }


    /**
     * Loads this Spawn Location from the provided JSON data.
     **/
    public void loadFromJSON(JsonObject json) {
        this.rangeMin = JSONHelper.getVector3i(json, "rangeMin");

        this.rangeMax = JSONHelper.getVector3i(json, "rangeMax");

        if (json.has("yMin"))
            this.yMin = json.get("yMin").getAsInt();

        if (json.has("yMax"))
            this.yMax = json.get("yMax").getAsInt();

        if (json.has("sorting"))
            this.sorting = json.get("sorting").getAsString();
    }


    /**
     * Returns a list of positions to spawn at.
     **/
    public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
        List<BlockPos> spawnPositions = new ArrayList<>();

        int yOffset = this.getOffset(world.random, this.rangeMin.getY(), this.rangeMax.getY());
        int finalY = triggerPos.getY() + yOffset;

        if ((this.yMax < 0 || finalY <= this.yMax) && (this.yMin < 0 || finalY >= this.yMin)) {
            Vec3i offset = new Vec3i(
                    this.getOffset(world.random, this.rangeMin.getX(), this.rangeMax.getX()),
                    yOffset,
                    this.getOffset(world.random, this.rangeMin.getZ(), this.rangeMax.getZ())
            );
            spawnPositions.add(triggerPos.offset(offset));
        }

        return this.sortSpawnPositions(spawnPositions, world, triggerPos);
    }


    /**
     * Returns a random offset from the provided min and max values.
     **/
    public int getOffset(RandomSource random, int min, int max) {
        if (max <= min) {
            return 0;
        }
        int offset = min + random.nextInt(max - min);
        if (random.nextBoolean()) {
            offset = -offset;
        }
        return offset;
    }


    /**
     * Sorts a list of spawning positions.
     **/
    public List<BlockPos> sortSpawnPositions(List<BlockPos> spawnPositions, Level world, BlockPos triggerPos) {
        String sorting = this.sorting;

        if ("difficulty".equalsIgnoreCase(this.sorting)) {
            sorting = "random";
            if (world.getDifficulty().getId() <= 1) {
                sorting = "far";
            } else if (world.getDifficulty().getId() >= 3) {
                sorting = "near";
            }
        }

        if ("random".equalsIgnoreCase(sorting)) {
            Collections.shuffle(spawnPositions);
        } else if ("near".equalsIgnoreCase(sorting)) {
            Collections.sort(spawnPositions, new CoordSorterNearest(triggerPos));
        } else if ("far".equalsIgnoreCase(sorting)) {
            Collections.sort(spawnPositions, new CoordSorterFurthest(triggerPos));
        }
        return spawnPositions;
    }
}
