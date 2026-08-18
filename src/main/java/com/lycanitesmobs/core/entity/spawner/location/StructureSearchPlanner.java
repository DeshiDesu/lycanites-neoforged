package com.lycanitesmobs.core.entity.spawner.location;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Pure planning logic for structure searches. Keeping this separate from world access makes
 * unit conversion, deduplication, and locator call counts testable without starting Minecraft.
 */
final class StructureSearchPlanner {

    private static final int BLOCKS_PER_CHUNK = 16;

    private StructureSearchPlanner() {
    }

    static int deriveSearchRadiusChunks(int structureRangeBlocks) {
        if (structureRangeBlocks <= 0) {
            return 0;
        }
        return (int) (((long) structureRangeBlocks + BLOCKS_PER_CHUNK - 1L) / BLOCKS_PER_CHUNK);
    }

    static int resolveSearchRadiusChunks(int structureRangeBlocks, Integer explicitRadiusChunks) {
        if (explicitRadiusChunks != null && explicitRadiusChunks >= 0) {
            return explicitRadiusChunks;
        }
        return deriveSearchRadiusChunks(structureRangeBlocks);
    }

    static long rangeSquared(int structureRangeBlocks) {
        if (structureRangeBlocks < 0) {
            return -1L;
        }
        return (long) structureRangeBlocks * structureRangeBlocks;
    }

    static boolean isWithinRange(double distanceSquared, int structureRangeBlocks) {
        if (distanceSquared < 0.0D || !Double.isFinite(distanceSquared)) {
            return false;
        }
        long acceptedDistanceSquared = rangeSquared(structureRangeBlocks);
        return acceptedDistanceSquared >= 0L && distanceSquared <= acceptedDistanceSquared;
    }

    static <I, T> List<T> resolveDistinct(
            Iterable<I> configuredIds,
            Function<I, Optional<T>> resolver,
            Consumer<I> unresolvedHandler
    ) {
        Objects.requireNonNull(configuredIds, "configuredIds");
        Objects.requireNonNull(resolver, "resolver");
        Objects.requireNonNull(unresolvedHandler, "unresolvedHandler");

        LinkedHashSet<I> distinctIds = new LinkedHashSet<>();
        configuredIds.forEach(distinctIds::add);

        LinkedHashSet<T> resolvedValues = new LinkedHashSet<>();
        for (I configuredId : distinctIds) {
            Optional<T> resolved = Objects.requireNonNull(
                    resolver.apply(configuredId),
                    "resolver returned null"
            );
            if (resolved.isPresent()) {
                resolvedValues.add(resolved.get());
            } else {
                unresolvedHandler.accept(configuredId);
            }
        }
        return List.copyOf(resolvedValues);
    }

    static <T, R> List<R> executeSearches(
            SearchMode mode,
            List<T> resolvedStructures,
            Function<List<T>, R> locator
    ) {
        Objects.requireNonNull(resolvedStructures, "resolvedStructures");
        Objects.requireNonNull(locator, "locator");

        List<T> distinctStructures = List.copyOf(new LinkedHashSet<>(resolvedStructures));
        if (distinctStructures.isEmpty()) {
            return List.of();
        }

        SearchMode effectiveMode = mode == null ? SearchMode.PER_ID : mode;
        List<R> results = new ArrayList<>();
        if (effectiveMode == SearchMode.NEAREST_ANY) {
            addIfPresent(results, locator.apply(distinctStructures));
            return results;
        }

        for (T structure : distinctStructures) {
            addIfPresent(results, locator.apply(List.of(structure)));
        }
        return results;
    }

    private static <R> void addIfPresent(List<R> results, R result) {
        if (result != null) {
            results.add(result);
        }
    }

    enum SearchMode {
        PER_ID("per_id"),
        NEAREST_ANY("nearest_any");

        private final String serializedName;

        SearchMode(String serializedName) {
            this.serializedName = serializedName;
        }

        static SearchMode parse(String configuredValue, Consumer<String> warningHandler) {
            Objects.requireNonNull(warningHandler, "warningHandler");
            if (configuredValue == null) {
                return PER_ID;
            }

            String normalizedValue = configuredValue.trim().toLowerCase(Locale.ROOT);
            for (SearchMode mode : values()) {
                if (mode.serializedName.equals(normalizedValue)) {
                    return mode;
                }
            }

            warningHandler.accept("Unknown structureSearchMode '" + configuredValue
                    + "'; using 'per_id'. Expected 'per_id' or 'nearest_any'.");
            return PER_ID;
        }

        String serializedName() {
            return this.serializedName;
        }
    }
}
