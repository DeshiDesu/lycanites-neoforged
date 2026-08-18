package com.lycanitesmobs.core.entity.spawner.location;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.config.ConfigDebug;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureSpawnLocationTest {

    @BeforeAll
    static void initializeProjectLogging() {
        ConfigDebug.INSTANCE = new ConfigDebug(new ModConfigSpec.Builder());
    }

    @Test
    void existingJsonLoadsWithPerIdAndDerivedRadius() {
        JsonObject json = structureJson(100);

        StructureSpawnLocation location = new StructureSpawnLocation();
        location.loadStructureSearchConfiguration(json);

        assertEquals(100, location.structureRange);
        assertEquals(1, location.structureIds.size());
        assertEquals(
                StructureSearchPlanner.SearchMode.PER_ID,
                location.getStructureSearchMode()
        );
        assertEquals(7, location.getSearchRadiusChunks());
    }

    @Test
    void validExplicitChunkRadiusIsLoadedAndUsed() {
        JsonObject json = structureJson(256);
        json.addProperty("structureSearchRadiusChunks", 23);
        StructureSpawnLocation location = new StructureSpawnLocation();

        location.loadStructureSearchConfiguration(json);

        assertEquals(23, location.getSearchRadiusChunks());
        assertEquals(256, location.structureRange);
    }

    @Test
    void missingExplicitChunkRadiusDerivesFromRange() {
        StructureSpawnLocation location = new StructureSpawnLocation();

        location.loadStructureSearchConfiguration(structureJson(256));

        assertEquals(16, location.getSearchRadiusChunks());
    }

    @Test
    void invalidExplicitChunkRadiusFallsBackToDerivedRange() {
        JsonObject json = structureJson(256);
        json.addProperty("structureSearchRadiusChunks", -1);
        StructureSpawnLocation location = new StructureSpawnLocation();

        location.loadStructureSearchConfiguration(json);

        assertEquals(16, location.getSearchRadiusChunks());
    }

    @Test
    void overflowedExplicitChunkRadiusFallsBackToDerivedRange() {
        JsonObject json = structureJson(256);
        json.addProperty("structureSearchRadiusChunks", 2_147_483_648L);
        StructureSpawnLocation location = new StructureSpawnLocation();

        location.loadStructureSearchConfiguration(json);

        assertEquals(16, location.getSearchRadiusChunks());
    }

    @Test
    void negativeStructureRangeFallsBackToDefault() {
        JsonObject json = structureJson(-1);
        StructureSpawnLocation location = new StructureSpawnLocation();

        location.loadStructureSearchConfiguration(json);

        assertEquals(
                StructureSpawnLocation.DEFAULT_STRUCTURE_RANGE_BLOCKS,
                location.structureRange
        );
        assertEquals(7, location.getSearchRadiusChunks());
    }

    @Test
    void nearestAnyAndCaseVariationsLoadFromJson() {
        JsonObject json = structureJson(256);
        json.addProperty("structureSearchMode", "NeArEsT_AnY");
        StructureSpawnLocation location = new StructureSpawnLocation();

        location.loadStructureSearchConfiguration(json);

        assertEquals(
                StructureSearchPlanner.SearchMode.NEAREST_ANY,
                location.getStructureSearchMode()
        );
    }

    @Test
    void unknownModeFallsBackToPerId() {
        JsonObject json = structureJson(256);
        json.addProperty("structureSearchMode", "everything");
        StructureSpawnLocation location = new StructureSpawnLocation();

        location.loadStructureSearchConfiguration(json);

        assertEquals(
                StructureSearchPlanner.SearchMode.PER_ID,
                location.getStructureSearchMode()
        );
    }

    @Test
    void acceptedStructureInvokesSuperclassSpawnPositionSeam() {
        TrackingStructureSpawnLocation location = new TrackingStructureSpawnLocation();
        location.structureRange = 5;
        List<BlockPos> spawnPositions = new ArrayList<>();
        BlockPos structurePos = new BlockPos(3, 4, 0);

        boolean accepted = location.appendSpawnPositionsForLocatedStructure(
                spawnPositions,
                null,
                null,
                BlockPos.ZERO,
                structurePos
        );

        assertTrue(accepted);
        assertEquals(1, location.generationCalls);
        assertEquals(List.of(structurePos.above()), spawnPositions);
    }

    @Test
    void outOfRangeStructureDoesNotInvokeSuperclassSpawnPositionSeam() {
        TrackingStructureSpawnLocation location = new TrackingStructureSpawnLocation();
        location.structureRange = 5;
        List<BlockPos> spawnPositions = new ArrayList<>();

        boolean accepted = location.appendSpawnPositionsForLocatedStructure(
                spawnPositions,
                null,
                null,
                BlockPos.ZERO,
                new BlockPos(3, 4, 1)
        );

        assertFalse(accepted);
        assertEquals(0, location.generationCalls);
        assertTrue(spawnPositions.isEmpty());
    }

    private static JsonObject structureJson(int structureRange) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "structure");
        json.addProperty("structureRange", structureRange);
        JsonArray structureIds = new JsonArray();
        structureIds.add("minecraft:village_plains");
        json.add("structureIds", structureIds);
        return json;
    }

    private static final class TrackingStructureSpawnLocation extends StructureSpawnLocation {
        private int generationCalls;

        @Override
        protected List<BlockPos> getSpawnPositionsAroundStructure(
                Level world,
                Player player,
                BlockPos structurePos
        ) {
            this.generationCalls++;
            return List.of(structurePos.above());
        }
    }
}
