package com.lycanitesmobs.core.entity.spawner.location;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureSearchPlannerTest {

    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "15, 1",
            "16, 1",
            "17, 2",
            "100, 7",
            "256, 16"
    })
    void derivesChunkRadiusFromBlockRange(int rangeBlocks, int expectedRadiusChunks) {
        assertEquals(
                expectedRadiusChunks,
                StructureSearchPlanner.deriveSearchRadiusChunks(rangeBlocks)
        );
    }

    @Test
    void largeBlockRangeDoesNotOverflow() {
        assertEquals(
                134_217_728,
                StructureSearchPlanner.deriveSearchRadiusChunks(Integer.MAX_VALUE)
        );
        assertTrue(StructureSearchPlanner.rangeSquared(Integer.MAX_VALUE) > 0L);
    }

    @Test
    void negativeBlockRangeProducesSafeZeroRadiusAndRejectsResults() {
        assertEquals(0, StructureSearchPlanner.deriveSearchRadiusChunks(-1));
        assertFalse(StructureSearchPlanner.isWithinRange(0.0D, -1));
    }

    @Test
    void validExplicitRadiusIsUsed() {
        assertEquals(
                23,
                StructureSearchPlanner.resolveSearchRadiusChunks(256, 23)
        );
    }

    @Test
    void missingExplicitRadiusDerivesFromBlockRange() {
        assertEquals(
                16,
                StructureSearchPlanner.resolveSearchRadiusChunks(256, null)
        );
    }

    @Test
    void invalidExplicitRadiusFallsBackToDerivedRadius() {
        assertEquals(
                16,
                StructureSearchPlanner.resolveSearchRadiusChunks(256, -4)
        );
    }

    @Test
    void explicitRadiusDoesNotReplaceBlockDistanceAcceptance() {
        assertEquals(64, StructureSearchPlanner.resolveSearchRadiusChunks(16, 64));
        assertFalse(StructureSearchPlanner.isWithinRange(17.0D * 17.0D, 16));
    }

    @Test
    void missingModeDefaultsToPerIdWithoutWarning() {
        List<String> warnings = new ArrayList<>();

        assertEquals(
                StructureSearchPlanner.SearchMode.PER_ID,
                StructureSearchPlanner.SearchMode.parse(null, warnings::add)
        );
        assertTrue(warnings.isEmpty());
    }

    @Test
    void parsesPerIdMode() {
        assertEquals(
                StructureSearchPlanner.SearchMode.PER_ID,
                StructureSearchPlanner.SearchMode.parse("per_id", warning -> {
                })
        );
    }

    @Test
    void parsesNearestAnyMode() {
        assertEquals(
                StructureSearchPlanner.SearchMode.NEAREST_ANY,
                StructureSearchPlanner.SearchMode.parse("nearest_any", warning -> {
                })
        );
    }

    @Test
    void modeParsingIsCaseInsensitiveAndTrimsWhitespace() {
        assertEquals(
                StructureSearchPlanner.SearchMode.NEAREST_ANY,
                StructureSearchPlanner.SearchMode.parse("  NeArEsT_AnY  ", warning -> {
                })
        );
    }

    @Test
    void unknownModeWarnsAndFallsBackToPerId() {
        List<String> warnings = new ArrayList<>();

        assertEquals(
                StructureSearchPlanner.SearchMode.PER_ID,
                StructureSearchPlanner.SearchMode.parse("all_at_once", warnings::add)
        );
        assertEquals(1, warnings.size());
        assertTrue(warnings.getFirst().contains("all_at_once"));
        assertTrue(warnings.getFirst().contains("per_id"));
    }

    @Test
    void nearestAnyUsesOneLocatorCallForSeveralStructures() {
        List<List<String>> calls = new ArrayList<>();

        List<String> results = StructureSearchPlanner.executeSearches(
                StructureSearchPlanner.SearchMode.NEAREST_ANY,
                List.of("village_a", "village_b", "village_c"),
                structures -> {
                    calls.add(structures);
                    return structures.getFirst();
                }
        );

        assertEquals(1, calls.size());
        assertEquals(List.of("village_a", "village_b", "village_c"), calls.getFirst());
        assertEquals(List.of("village_a"), results);
    }

    @Test
    void perIdUsesOneLocatorCallPerDistinctStructure() {
        List<List<String>> calls = new ArrayList<>();

        StructureSearchPlanner.executeSearches(
                StructureSearchPlanner.SearchMode.PER_ID,
                List.of("village_a", "village_b", "village_a"),
                structures -> {
                    calls.add(structures);
                    return structures.getFirst();
                }
        );

        assertEquals(List.of(List.of("village_a"), List.of("village_b")), calls);
    }

    @Test
    void duplicateIdsMissingIdsAndDuplicateResolvedValuesAreSkippedDeterministically() {
        Map<String, String> registry = new HashMap<>();
        registry.put("a", "holder_a");
        registry.put("b", "holder_b");
        registry.put("alias_b", "holder_b");
        List<String> missing = new ArrayList<>();

        List<String> resolved = StructureSearchPlanner.resolveDistinct(
                List.of("a", "a", "missing", "b", "alias_b"),
                id -> Optional.ofNullable(registry.get(id)),
                missing::add
        );

        assertEquals(List.of("holder_a", "holder_b"), resolved);
        assertEquals(List.of("missing"), missing);
    }

    @Test
    void mixtureOfValidAndInvalidIdsStillSearchesValidEntries() {
        AtomicInteger calls = new AtomicInteger();
        List<String> resolved = StructureSearchPlanner.resolveDistinct(
                List.of("valid", "missing"),
                id -> "valid".equals(id) ? Optional.of("holder") : Optional.empty(),
                ignored -> {
                }
        );

        List<String> results = StructureSearchPlanner.executeSearches(
                StructureSearchPlanner.SearchMode.NEAREST_ANY,
                resolved,
                structures -> {
                    calls.incrementAndGet();
                    return "found";
                }
        );

        assertEquals(1, calls.get());
        assertEquals(List.of("found"), results);
    }

    @Test
    void emptyOrEntirelyMissingStructuresProduceNoSearch() {
        AtomicInteger calls = new AtomicInteger();
        List<String> resolved = StructureSearchPlanner.resolveDistinct(
                List.of("missing"),
                ignored -> Optional.empty(),
                ignored -> {
                }
        );

        List<String> results = StructureSearchPlanner.executeSearches(
                StructureSearchPlanner.SearchMode.PER_ID,
                resolved,
                structures -> {
                    calls.incrementAndGet();
                    return "unexpected";
                }
        );

        assertTrue(results.isEmpty());
        assertEquals(0, calls.get());
    }

    @Test
    void distanceAcceptanceIncludesInsideAndBoundaryButRejectsOutside() {
        assertTrue(StructureSearchPlanner.isWithinRange(99.0D, 10));
        assertTrue(StructureSearchPlanner.isWithinRange(100.0D, 10));
        assertFalse(StructureSearchPlanner.isWithinRange(100.000_001D, 10));
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 0",
            "1.0, 1",
            "24.99, 5",
            "25.0, 5",
            "25.01, 5",
            "65536.0, 256",
            "65536.01, 256"
    })
    void squaredComparisonMatchesPreviousBlockDistanceSemantics(
            double distanceSquared,
            int rangeBlocks
    ) {
        boolean previousSemantics = Math.sqrt(distanceSquared) <= rangeBlocks;

        assertEquals(
                previousSemantics,
                StructureSearchPlanner.isWithinRange(distanceSquared, rangeBlocks)
        );
    }
}
