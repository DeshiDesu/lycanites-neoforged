package com.lycanitesmobs.core.entity.spawner.location;

import com.google.gson.JsonElement;
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

    private static final String LOG_KEY = "JSONSpawner";
    static final int DEFAULT_STRUCTURE_RANGE_BLOCKS = 100;

    /**
     * The structure ids to use as spawn locations.
     **/
    public List<ResourceLocation> structureIds = new ArrayList<>();

    /**
     * Maximum accepted distance in blocks from the trigger to a structure anchor.
     **/
    public int structureRange = DEFAULT_STRUCTURE_RANGE_BLOCKS;

    private Integer structureSearchRadiusChunks;
    private StructureSearchPlanner.SearchMode structureSearchMode =
            StructureSearchPlanner.SearchMode.PER_ID;

    private static void logWarning(String warning) {
        LMHelperClass.logWarning("", "[" + LOG_KEY + "] " + warning);
    }

    @Override
    public void loadFromJSON(JsonObject json) {
        super.loadFromJSON(json);

        this.loadStructureSearchConfiguration(json);
    }

    void loadStructureSearchConfiguration(JsonObject json) {
        if (json.has("structureIds")) {
            List<String> structureIdStrings = JSONHelper.getJsonStrings(json.getAsJsonArray("structureIds"));
            this.structureIds.clear();
            for (String structureIdName : structureIdStrings) {
                try {
                    this.structureIds.add(ResourceLocation.parse(structureIdName));
                } catch (RuntimeException e) {
                    logWarning(
                            "Invalid structure ID '" + structureIdName + "'; this entry will be skipped.");
                }
            }
        }

        this.structureRange = DEFAULT_STRUCTURE_RANGE_BLOCKS;
        if (json.has("structureRange")) {
            Integer configuredRange = this.readNonNegativeInt(
                    json,
                    "structureRange",
                    "using the default of " + DEFAULT_STRUCTURE_RANGE_BLOCKS + " blocks"
            );
            if (configuredRange != null) {
                this.structureRange = configuredRange;
            }
        }

        this.structureSearchRadiusChunks = null;
        if (json.has("structureSearchRadiusChunks")) {
            this.structureSearchRadiusChunks = this.readNonNegativeInt(
                    json,
                    "structureSearchRadiusChunks",
                    "deriving the chunk radius from structureRange"
            );
        }

        this.structureSearchMode = StructureSearchPlanner.SearchMode.PER_ID;
        if (json.has("structureSearchMode")) {
            try {
                this.structureSearchMode = StructureSearchPlanner.SearchMode.parse(
                        json.get("structureSearchMode").getAsString(),
                        StructureSpawnLocation::logWarning
                );
            } catch (RuntimeException e) {
                logWarning(
                        "Invalid structureSearchMode value; using 'per_id'. "
                                + "Expected 'per_id' or 'nearest_any'.");
            }
        }
    }

    private Integer readNonNegativeInt(JsonObject json, String memberName, String fallbackDescription) {
        JsonElement value = json.get(memberName);
        try {
            int parsedValue = value.getAsBigDecimal().intValueExact();
            if (parsedValue < 0) {
                throw new IllegalArgumentException("value must be non-negative");
            }
            return parsedValue;
        } catch (RuntimeException e) {
            logWarning(
                    "Invalid " + memberName + " value " + value
                            + "; expected a non-negative whole number, " + fallbackDescription + ".");
            return null;
        }
    }

    int getSearchRadiusChunks() {
        return StructureSearchPlanner.resolveSearchRadiusChunks(
                this.structureRange,
                this.structureSearchRadiusChunks
        );
    }

    StructureSearchPlanner.SearchMode getStructureSearchMode() {
        return this.structureSearchMode;
    }

    @Override
    public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return new ArrayList<>();
        }
        if (this.structureRange < 0) {
            logWarning(
                    "structureRange must not be negative; skipping this structure search.");
            return new ArrayList<>();
        }
        if (this.structureIds.isEmpty()) {
            return new ArrayList<>();
        }

        Registry<Structure> structureRegistry = serverLevel.registryAccess()
                .registryOrThrow(Registries.STRUCTURE);
        List<Holder.Reference<Structure>> resolvedStructures =
                this.resolveStructures(structureRegistry);
        if (resolvedStructures.isEmpty()) {
            return new ArrayList<>();
        }

        int searchRadiusChunks = this.getSearchRadiusChunks();
        List<BlockPos> structurePositions = StructureSearchPlanner.executeSearches(
                this.structureSearchMode,
                resolvedStructures,
                structures -> this.locateStructures(
                        serverLevel,
                        structures,
                        triggerPos,
                        searchRadiusChunks
                )
        );

        List<BlockPos> spawnPositions = new ArrayList<>();
        for (BlockPos structurePos : structurePositions) {
            this.appendSpawnPositionsForLocatedStructure(
                    spawnPositions,
                    world,
                    player,
                    triggerPos,
                    structurePos
            );
        }
        return this.sortSpawnPositions(spawnPositions, world, triggerPos);
    }

    private List<Holder.Reference<Structure>> resolveStructures(Registry<Structure> structureRegistry) {
        return StructureSearchPlanner.resolveDistinct(
                this.structureIds,
                structureId -> {
                    if (structureId == null) {
                        return Optional.empty();
                    }
                    ResourceKey<Structure> structureKey =
                            ResourceKey.create(Registries.STRUCTURE, structureId);
                    return structureRegistry.getHolder(structureKey);
                },
                structureId -> LMHelperClass.logDebug(
                        LOG_KEY,
                        "Structure not found in registry: " + structureId
                )
        );
    }

    private BlockPos locateStructures(
            ServerLevel serverLevel,
            List<Holder.Reference<Structure>> structures,
            BlockPos triggerPos,
            int searchRadiusChunks
    ) {
        HolderSet<Structure> holderSet = HolderSet.direct(structures);
        LMHelperClass.logDebug(LOG_KEY,
                "Locating nearest structure for " + structures.size()
                        + " configured structure holder(s) within "
                        + searchRadiusChunks + " chunk(s) using "
                        + this.structureSearchMode.serializedName() + " mode.");
        try {
            return this.findNearestStructure(
                    serverLevel,
                    holderSet,
                    triggerPos,
                    searchRadiusChunks
            );
        } catch (Exception e) {
            logWarning(
                    "Error searching for configured structures in "
                            + this.structureSearchMode.serializedName() + " mode: "
                            + e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    protected BlockPos findNearestStructure(
            ServerLevel serverLevel,
            HolderSet<Structure> structures,
            BlockPos triggerPos,
            int searchRadiusChunks
    ) {
        var result = serverLevel.getChunkSource().getGenerator()
                .findNearestMapStructure(
                        serverLevel,
                        structures,
                        triggerPos,
                        searchRadiusChunks,
                        false
                );
        return result == null ? null : result.getFirst();
    }

    boolean appendSpawnPositionsForLocatedStructure(
            List<BlockPos> spawnPositions,
            Level world,
            Player player,
            BlockPos triggerPos,
            BlockPos structurePos
    ) {
        double distanceSquared = structurePos.distSqr(triggerPos);
        long acceptedDistanceSquared = StructureSearchPlanner.rangeSquared(this.structureRange);
        if (!StructureSearchPlanner.isWithinRange(distanceSquared, this.structureRange)) {
            LMHelperClass.logDebug(LOG_KEY,
                    "No configured structure within block range; nearest squared distance was "
                            + distanceSquared + "/" + acceptedDistanceSquared
                            + " at: " + structurePos);
            return false;
        }

        LMHelperClass.logDebug(LOG_KEY,
                "Found a configured structure within block range at: " + structurePos
                        + " squared distance: " + distanceSquared
                        + "/" + acceptedDistanceSquared);
        spawnPositions.addAll(this.getSpawnPositionsAroundStructure(world, player, structurePos));
        return true;
    }

    protected List<BlockPos> getSpawnPositionsAroundStructure(
            Level world,
            Player player,
            BlockPos structurePos
    ) {
        return super.getSpawnPositions(world, player, structurePos);
    }

}
