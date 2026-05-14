package com.lycanitesmobs.core.util.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.joml.Vector3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JSONHelper {
    private static final Map<List<String>, List<String>> biomeTagCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 1000;

    public static Vec3i getVector3i(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            JsonArray jsonArray = json.get(memberName).getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            int[] coords = new int[3];
            int i = 0;
            while (jsonIterator.hasNext() && i < coords.length) {
                coords[i] = jsonIterator.next().getAsInt();
                i++;
            }
            return new Vec3i(coords[0], coords[1], coords[2]);
        }
        return new Vec3i(0, 0, 0);
    }

    public static Vector3d getVector3d(JsonObject json, String memberName, Vector3d defaultVec) {
        if (json.has(memberName)) {
            JsonArray jsonArray = json.get(memberName).getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            double[] coords = new double[3];
            int i = 0;
            while (jsonIterator.hasNext() && i < coords.length) {
                coords[i] = jsonIterator.next().getAsDouble();
                i++;
            }
            return new Vector3d(coords[0], coords[1], coords[2]);
        }
        return defaultVec;
    }

    public static List<String> getJsonStrings(JsonArray jsonArray) {
        List<String> strings = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            String string = jsonElement.getAsString();
            strings.add(string);
        }
        return strings;
    }

    public static List<Block> getJsonBlocks(JsonObject json) {
        List<Block> blocks = new ArrayList<>();
        if (json.has("blocks")) {
            blocks = getJsonBlocks(json.get("blocks").getAsJsonArray());
        }
        return blocks;
    }

    public static List<Block> getJsonBlocks(JsonArray jsonArray) {
        List<Block> blocks = new ArrayList<>();
        Iterator<JsonElement> jsonIterator = jsonArray.iterator();
        while (jsonIterator.hasNext()) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonIterator.next().getAsString()));
            if (block != null) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    public static List<Item> getJsonItems(JsonArray jsonArray) {
        List<Item> items = new ArrayList<>();
        Iterator<JsonElement> jsonIterator = jsonArray.iterator();
        while (jsonIterator.hasNext()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(jsonIterator.next().getAsString()));
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    public static List<Block> getJsonMaterials(JsonObject json) {
        List<Block> blocks = new ArrayList<>();

        if (json.has("materials")) {
            JsonArray jsonArray = json.get("materials").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();

            while (jsonIterator.hasNext()) {
                String materialName = jsonIterator.next().getAsString();
                List<Block> materialBlocks = null;

                switch (materialName.toLowerCase()) {
                    case "air":
                        materialBlocks = Material.AIR;
                        break;
                    case "lava":
                        materialBlocks = Material.LAVA;
                        break;
                    case "fire":
                        materialBlocks = Material.FIRE;
                        break;
                    case "water":
                        materialBlocks = Material.WATER;
                        break;
                    case "ground":
                    case "earth":
                        materialBlocks = Material.DIRT;
                        break;
                    case "sand":
                        materialBlocks = Material.SAND;
                        break;
                    case "clay":
                        materialBlocks = Material.CLAY;
                        break;
                    case "wood":
                        materialBlocks = Material.WOOD;
                        break;
                    case "rock":
                        materialBlocks = Material.STONE;
                        break;
                    case "grass":
                        materialBlocks = Material.GRASS;
                        break;
                    case "tallplants":
                    case "vine":
                        materialBlocks = Material.REPLACEABLE_PLANT;
                        break;
                    case "plants":
                        materialBlocks = Material.PLANT;
                        break;
                    case "leaves":
                        materialBlocks = Material.LEAVES;
                        break;
                    case "cactus":
                        materialBlocks = Material.CACTUS;
                        break;
                    case "snow":
                        materialBlocks = Material.SNOW;
                        break;
                    case "ice":
                        materialBlocks = Material.ICE;
                        break;
                    case "iron":
                        materialBlocks = Material.METAL;
                        break;
                    case "web":
                        materialBlocks = Material.WEB;
                        break;
                }

                if (materialBlocks != null) {
                    blocks.addAll(materialBlocks);
                }
            }
        }
        return blocks;
    }

    public static List<String> getBiomesFromTags(Level world, List<String> biomeTags) {
        List<String> cachedResult = biomeTagCache.get(biomeTags);
        if (cachedResult != null) {
            return new ArrayList<>(cachedResult);
        }

        LinkedHashSet<String> biomeSet = new LinkedHashSet<>();

        var registryAccess = world.registryAccess();
        var biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

        for (String rawEntry : biomeTags) {
            boolean additive;
            String entry = rawEntry;

            if (!entry.isEmpty()) {
                char c = entry.charAt(0);
                if (c == '+' || c == '-') {
                    additive = c != '-';
                    entry = entry.substring(1);
                } else {
                    additive = true;
                }
            } else {
                additive = true;
            }

            if ("ALL".equalsIgnoreCase(entry)) {
                biomeRegistry.registryKeySet().forEach(key -> {
                    String id = key.location().toString();
                    if (additive) {
                        biomeSet.add(id);
                    } else {
                        biomeSet.remove(id);
                    }
                });
                continue;
            }

            if ("NONE".equalsIgnoreCase(entry)) {
                continue;
            }

            if (!entry.contains(":")) {
                LMHelperClass.logWarning(
                        "Dungeon",
                        "Non-namespaced biome tag entry '" + entry + "' is not supported, skipping"
                );
                continue;
            }

            ResourceLocation tagLoc = new ResourceLocation(entry);
            List<String> selectedBiomeIds = new ArrayList<>();

            TagKey<Biome> vanillaKey =
                    TagKey.create(
                            Registries.BIOME,
                            tagLoc
                    );

            var vanillaSetOpt = biomeRegistry.getTag(vanillaKey);
            if (vanillaSetOpt.isPresent()) {
                vanillaSetOpt.get().forEach(holder ->
                        holder.unwrapKey().ifPresent(key -> {
                            var id = key.location();
                            if (id != null) {
                                selectedBiomeIds.add(id.toString());
                            }
                        })
                );
            } else {
                ITagManager<Biome> forgeTagManager =
                        ForgeRegistries.BIOMES.tags();

                TagKey<Biome> forgeKey =
                        TagKey.create(
                                ForgeRegistries.Keys.BIOMES,
                                tagLoc
                        );

                var forgeTag = forgeTagManager.getTag(forgeKey);
                if (!forgeTag.isEmpty()) {
                    forgeTag.forEach(biome -> {
                        ResourceLocation id = ForgeRegistries.BIOMES.getKey(biome);
                        if (id != null) {
                            selectedBiomeIds.add(id.toString());
                        }
                    });
                }
            }

            if (selectedBiomeIds.isEmpty()) {
                LMHelperClass.logWarning(
                        "Dungeon",
                        "Biome tag '" + entry + "' is empty or missing (vanilla and Forge)"
                );
            } else {
                if (additive) {
                    biomeSet.addAll(selectedBiomeIds);
                } else {
                    biomeSet.removeAll(selectedBiomeIds);
                }
            }
        }

        List<String> biomeList = new ArrayList<>(biomeSet);
        LMHelperClass.logDebug("Dungeon", "Resolved biome tags " + biomeTags + " → " + biomeList.size() + " biomes");

        cacheBiomeTags(biomeTags, biomeList);

        return biomeList;
    }

    /**
     * Cache the result of biome tag resolution to avoid redundant computation
     * @param originalTags The original tag list used as cache key
     * @param resolvedBiomes The resolved biome list to cache
     */
    private static void cacheBiomeTags(List<String> originalTags, List<String> resolvedBiomes) {
        if (biomeTagCache.size() >= MAX_CACHE_SIZE) {
            Iterator<Map.Entry<List<String>, List<String>>> iterator = biomeTagCache.entrySet().iterator();
            int entriesToRemove = MAX_CACHE_SIZE / 4;
            for (int i = 0; i < entriesToRemove && iterator.hasNext(); i++) {
                iterator.next();
                iterator.remove();
            }
        }

        biomeTagCache.put(new ArrayList<>(originalTags), new ArrayList<>(resolvedBiomes));
    }

    /**
     * Clear the biome tag cache, useful for config reloading
     */
    public static void clearBiomeTagCache() {
        biomeTagCache.clear();
    }

    /**
     * Get the current size of the biome tag cache
     * @return The number of cached entries
     */
    public static int getBiomeTagCacheSize() {
        return biomeTagCache.size();
    }

    public static List<Biome> getBiomes(List<String> biomeIds) {
        List<Biome> biomes = new ArrayList<>();
        for (String biomeId : biomeIds) {
            Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeId));
            if (biome != null) {
                biomes.add(biome);
            }
        }
        return biomes;
    }
}
