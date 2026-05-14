package com.lycanitesmobs.core.entity.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StructureSpawnLocation extends RandomSpawnLocation {

    /**
     * The structure ids to use as spawn locations.
     **/
    public List<ResourceLocation> structureIds = new ArrayList<>();

    /**
     * How close to the player (in blocks) Structures must be. Default: 100.
     **/
    public int structureRange = 100;


    @Override
    public void loadFromJSON(JsonObject json) {
        super.loadFromJSON(json);

        if (json.has("structureIds")) {
            List<String> structureIdStrings = JSONHelper.getJsonStrings(json.getAsJsonArray("structureIds"));
            structureIds.clear();
            for (String structureIdName : structureIdStrings) {
                structureIds.add(new ResourceLocation(structureIdName));
            }
        }

        if (json.has("structureRange"))
            this.structureRange = json.get("structureRange").getAsInt();
    }


    @Override
    public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return new ArrayList<>();
        }

        Registry<Structure> structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);

        List<BlockPos> spawnPositions = new ArrayList<>();
        for (ResourceLocation structureId : this.structureIds) {
            ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, structureId);
            Optional<Holder.Reference<Structure>> holderOpt = structureRegistry.getHolder(structureKey);
            if (holderOpt.isEmpty()) {
                LMHelperClass.logDebug("JSONSpawner", "Structure not found in registry: " + structureId);
                continue;
            }

            HolderSet<Structure> holderSet = HolderSet.direct(holderOpt.get());
            LMHelperClass.logDebug("JSONSpawner", "Getting Nearest " + structureId + " Structures Within Range.");

            BlockPos structurePos = null;
            try {
                var result = serverLevel.getChunkSource().getGenerator()
                        .findNearestMapStructure(serverLevel, holderSet, triggerPos, this.structureRange, false);
                if (result != null) {
                    structurePos = result.getFirst();
                }
            } catch (Exception e) {
                LMHelperClass.logWarning("JSONSpawner", "Error searching for structure " + structureId + ": " + e.getMessage());
            }

            // No Structure:
            if (structurePos == null) {
                continue;
            }

            // Too Far:
            double structureDistance = Math.sqrt(structurePos.distSqr(triggerPos));
            if (structureDistance > this.structureRange) {
                LMHelperClass.logDebug("JSONSpawner", "No " + structureId + " Structures within range, nearest was: " + structureDistance + "/" + this.structureRange + " at: " + structurePos);
                continue;
            }

            // Structure Found:
            LMHelperClass.logDebug("JSONSpawner", "Found a " + structureId + " Structure within range, at: " + structurePos + " distance: " + structureDistance + "/" + this.structureRange);
            spawnPositions.addAll(super.getSpawnPositions(world, player, structurePos));
        }

        return this.sortSpawnPositions(spawnPositions, world, triggerPos);
    }

}
