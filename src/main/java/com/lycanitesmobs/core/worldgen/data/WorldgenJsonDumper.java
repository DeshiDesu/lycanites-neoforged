package com.lycanitesmobs.core.worldgen.data;

import com.google.gson.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.liquid.BaseLiquidBlock;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.manager.FluidManager;
import com.lycanitesmobs.core.manager.WorldGenManager;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldgenJsonDumper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void dumpIfDev() {
        if (FMLEnvironment.production) {
            return;
        }
        try {
            dumpAll();
        } catch (Exception e) {
            LycanitesMobs.LOGGER.error("Failed dumping worldgen json", e);
        }
    }

    private static void dumpAll() throws IOException {
        Path root = Paths.get(System.getProperty("user.dir"), "generatedData", "data", LycanitesMobs.MODID);
        dumpConfiguredFeatureChunkSpawn(root);
        dumpConfiguredFeatureDungeon(root);
        dumpPlacedFeatureChunkSpawn(root);
        dumpPlacedFeatureDungeon(root);
        dumpFluidFeatures(root);
        dumpBiomeModifiers(root);
        dumpTags(root);
    }


    private static void dumpTags(Path root) throws IOException {
        dumpPlacedFeatureTags(root);
        dumpBiomeTags(root);
        dumpDungeonBiomeTags(root);
    }

    private static void dumpDungeonBiomeTags(Path root) throws IOException {
        DungeonManager dungeonManager = DungeonManager.getInstance();
        Map<String, JsonObject> schematicJsons = dungeonManager.loadDungeonsFromJSON("schematics", LycanitesMobs.modInfo);

        for (Map.Entry<String, JsonObject> entry : schematicJsons.entrySet()) {
            String name = entry.getKey();
            JsonObject schematicJson = entry.getValue();

            List<String> selectors = collectDungeonBiomeSelectors(schematicJson);
            if (selectors.isEmpty()) {
                continue;
            }

            JsonObject tagObj = new JsonObject();
            tagObj.addProperty("replace", false);

            JsonArray values = new JsonArray();
            for (String s : selectors) {
                values.add(s);
            }
            tagObj.add("values", values);

            String tagPath = "tags/worldgen/biome/has_" + name + "_dungeon.json";
            write(root, tagPath, tagObj);
        }
    }

    private static void dumpPlacedFeatureTags(Path root) throws IOException {
        WorldGenManager worldGen = WorldGenManager.getInstance();
        Map<String, List<String>> fluidToFeatures = new HashMap<>();

        for (String key : worldGen.fluidPlacedFeatures.keySet()) {
            int idx = key.indexOf('_');
            String fluid = idx >= 0 ? key.substring(0, idx) : key;
            String featureId = LycanitesMobs.MODID + ":" + key;
            fluidToFeatures.computeIfAbsent(fluid, k -> new ArrayList<>()).add(featureId);
        }

        for (Map.Entry<String, List<String>> entry : fluidToFeatures.entrySet()) {
            String fluid = entry.getKey();
            List<String> features = entry.getValue();

            JsonObject tag = new JsonObject();
            tag.addProperty("replace", false);
            JsonArray values = new JsonArray();
            for (String f : features) {
                values.add(f);
            }
            tag.add("values", values);

            String fileName = "tags/worldgen/placed_feature/" + fluid + "_pools.json";
            write(root, fileName, tag);
        }
    }

    private static void dumpBiomeTags(Path root) throws IOException {
        WorldGenManager worldGen = WorldGenManager.getInstance();
        Map<String, BiomeSpec> seen = new HashMap<>();

        for (String key : worldGen.fluidPlacedFeatures.keySet()) {
            int idx = key.indexOf('_');
            String fluid = idx >= 0 ? key.substring(0, idx) : key;

            if (seen.containsKey(fluid)) {
                continue;
            }

            BiomeSpec spec = biomeSpecForFluid(fluid);
            if (spec == null) {
                continue;
            }
            seen.put(fluid, spec);

            String tagPath = "tags/worldgen/biome/has_" + fluid + "_pools.json";
            write(root, tagPath, biomeTag(spec.biomes));
        }
    }

    private static JsonObject biomeTag(List<String> biomes) {
        JsonObject root = new JsonObject();
        root.addProperty("replace", false);

        JsonArray values = new JsonArray();
        for (String b : biomes) {
            values.add(b);
        }
        root.add("values", values);

        return root;
    }


    private static JsonObject biomeAddFeatureTag(String placedFeatureTag, String biomesTag, String step) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "forge:add_features");
        root.addProperty("biomes", "#" + biomesTag);
        root.addProperty("features", "#" + placedFeatureTag);
        root.addProperty("step", step);
        return root;
    }

    private static void dumpBiomeModifiers(Path root) throws IOException {
        dumpChunkspawnAndDungeon(root);

        WorldGenManager worldGen = WorldGenManager.getInstance();
        Map<String, List<String>> fluidToFeatures = new HashMap<>();

        for (String key : worldGen.fluidPlacedFeatures.keySet()) {
            int idx = key.indexOf('_');
            String fluid = idx >= 0 ? key.substring(0, idx) : key;
            String featureId = LycanitesMobs.MODID + ":" + key;
            fluidToFeatures.computeIfAbsent(fluid, k -> new ArrayList<>()).add(featureId);
        }

        for (Map.Entry<String, List<String>> entry : fluidToFeatures.entrySet()) {
            String fluid = entry.getKey();
            List<String> features = entry.getValue();
            BiomeSpec spec = biomeSpecForFluid(fluid);
            if (spec == null) {
                continue;
            }

            String biomeTagId = LycanitesMobs.MODID + ":has_" + fluid + "_pools";
            String fileName = "forge/biome_modifier/" + fluid + "_pools.json";
            write(root, fileName, biomeAddFeatureUsingBiomeTag(features, biomeTagId, spec.step));
        }
    }

    private static JsonObject biomeAddFeatureUsingBiomeTag(List<String> placedFeatures, String biomeTagId, String step) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "forge:add_features");
        root.addProperty("biomes", "#" + biomeTagId);

        JsonArray features = new JsonArray();
        for (String f : placedFeatures) {
            features.add(f);
        }
        root.add("features", features);

        root.addProperty("step", step);
        return root;
    }

    private static List<String> collectDungeonBiomeSelectors(JsonObject schematicJson) {
        List<String> selectors = new ArrayList<>();

        if (schematicJson.has("conditions")) {
            JsonArray conditions = schematicJson.getAsJsonArray("conditions");
            for (JsonElement condElement : conditions) {
                if (!condElement.isJsonObject()) {
                    continue;
                }
                JsonObject condition = condElement.getAsJsonObject();
                if (!condition.has("type")) {
                    continue;
                }
                String type = condition.get("type").getAsString();
                if (!"world".equalsIgnoreCase(type)) {
                    continue;
                }

                String biomeTagListType = condition.has("biomeTagListType")
                        ? condition.get("biomeTagListType").getAsString()
                        : "whitelist";
                String biomeIdListType = condition.has("biomeIdListType")
                        ? condition.get("biomeIdListType").getAsString()
                        : "whitelist";

                if (condition.has("biomeTags") && !"blacklist".equalsIgnoreCase(biomeTagListType)) {
                    JsonArray biomeTags = condition.getAsJsonArray("biomeTags");
                    for (JsonElement tagElement : biomeTags) {
                        String tag = tagElement.getAsString();
                        selectors.add("#" + tag);
                    }
                }

                if (condition.has("biomeIds") && !"blacklist".equalsIgnoreCase(biomeIdListType)) {
                    JsonArray biomeIds = condition.getAsJsonArray("biomeIds");
                    for (JsonElement idElement : biomeIds) {
                        String id = idElement.getAsString();
                        selectors.add(id);
                    }
                }
            }
        }

        return selectors;
    }

    private static void dumpChunkspawnAndDungeon(Path root) throws IOException {
        write(root, "forge/biome_modifier/add_chunkspawn.json",
                biomeAddFeature(
                        "lycanitesmobs:chunkspawn",
                        "#minecraft:is_overworld",
                        "top_layer_modification"
                ));

        DungeonManager dungeonManager = DungeonManager.getInstance();
        Map<String, JsonObject> schematicJsons = dungeonManager.loadDungeonsFromJSON("schematics", LycanitesMobs.modInfo);

        for (Map.Entry<String, JsonObject> entry : schematicJsons.entrySet()) {
            String name = entry.getKey();
            JsonObject schematicJson = entry.getValue();
            String featureId = LycanitesMobs.MODID + ":dungeon_" + name;
            String fileName = "forge/biome_modifier/add_dungeon_" + name + ".json";

            List<String> selectors = collectDungeonBiomeSelectors(schematicJson);
            if (selectors.isEmpty()) {
                JsonObject modifier = biomeAddFeature(
                        featureId,
                        "#minecraft:is_overworld",
                        "top_layer_modification"
                );
                write(root, fileName, modifier);
            } else {
                String biomeTagId = LycanitesMobs.MODID + ":has_" + name + "_dungeon";
                JsonObject modifier = biomeAddFeatureUsingBiomeTag(
                        List.of(featureId),
                        biomeTagId,
                        "top_layer_modification"
                );
                write(root, fileName, modifier);
            }
        }
    }

    private static class BiomeSpec {
        final List<String> biomes;
        final String step;

        BiomeSpec(List<String> biomes, String step) {
            this.biomes = biomes;
            this.step = step;
        }
    }

    private static BiomeSpec biomeSpecForFluid(String fluid) {
        switch (fluid) {
            case "poison":
                return new BiomeSpec(
                        List.of(
                                "minecraft:swamp",
                                "minecraft:mushroom_fields"
                        ),
                        "lakes"
                );
            case "acid":
                return new BiomeSpec(
                        List.of(
                                "minecraft:desert",
                                "minecraft:badlands",
                                "minecraft:the_end"
                        ),
                        "lakes"
                );
            case "ooze":
                return new BiomeSpec(
                        List.of(
                                "minecraft:ice_spikes",
                                "minecraft:snowy_plains",
                                "minecraft:windswept_hills"
                        ),
                        "lakes"
                );
            case "moglava":
                return new BiomeSpec(
                        List.of(
                                "minecraft:nether_wastes",
                                "minecraft:crimson_forest",
                                "minecraft:warped_forest",
                                "minecraft:basalt_deltas",
                                "minecraft:soul_sand_valley",
                                "minecraft:jungle",
                                "minecraft:bamboo_jungle"
                        ),
                        "lakes"
                );
            default:
                return null;
        }
    }

    private static JsonObject biomeAddFeature(String placedFeature, String biomesTag, String step) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "forge:add_features");
        root.addProperty("biomes", biomesTag);
        root.addProperty("features", placedFeature);
        root.addProperty("step", step);
        return root;
    }

    private static JsonObject biomeAddFeatureMulti(List<String> placedFeatures, List<String> biomesList, String step) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "forge:add_features");

        JsonArray biomes = new JsonArray();
        for (String b : biomesList) {
            biomes.add(b);
        }
        root.add("biomes", biomes);

        JsonArray features = new JsonArray();
        for (String f : placedFeatures) {
            features.add(f);
        }
        root.add("features", features);

        root.addProperty("step", step);
        return root;
    }


    private static void write(Path root, String relativePath, JsonObject json) throws IOException {
        Path file = root.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(json), StandardCharsets.UTF_8);
    }

    private static void dumpConfiguredFeatureChunkSpawn(Path root) throws IOException {
        JsonObject rootJson = new JsonObject();
        rootJson.addProperty("type", LycanitesMobs.MODID + ":chunkspawn");
        rootJson.add("config", new JsonObject());
        write(root, "worldgen/configured_feature/chunkspawn.json", rootJson);
    }

    private static void dumpConfiguredFeatureDungeon(Path root) throws IOException {
        DungeonManager dungeonManager = DungeonManager.getInstance();

        for (DungeonSchematic schematic : dungeonManager.schematics.values()) {
            JsonObject rootJson = new JsonObject();
            rootJson.addProperty("type", LycanitesMobs.MODID + ":dungeon");

            JsonObject config = new JsonObject();
            config.addProperty("schematic", schematic.name);
            rootJson.add("config", config);

            String fileName = "worldgen/configured_feature/dungeon_" + schematic.name + ".json";
            write(root, fileName, rootJson);
        }
    }

    private static JsonObject uniformHeight(int min, int max) {
        JsonObject uniform = new JsonObject();
        uniform.addProperty("type", "minecraft:uniform");
        JsonObject minObj = new JsonObject();
        minObj.addProperty("absolute", min);
        JsonObject maxObj = new JsonObject();
        maxObj.addProperty("absolute", max);
        uniform.add("min_inclusive", minObj);
        uniform.add("max_inclusive", maxObj);
        return uniform;
    }

    private static JsonObject trapezoidHeight(int min, int max) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:trapezoid");
        JsonObject minObj = new JsonObject();
        minObj.addProperty("absolute", min);
        JsonObject maxObj = new JsonObject();
        maxObj.addProperty("absolute", max);
        obj.add("min_inclusive", minObj);
        obj.add("max_inclusive", maxObj);
        return obj;
    }


    private static void dumpPlacedFeatureChunkSpawn(Path root) throws IOException {
        JsonObject rootJson = new JsonObject();
        rootJson.addProperty("feature", LycanitesMobs.MODID + ":chunkspawn");
        JsonArray placement = new JsonArray();

        JsonObject inSquare = new JsonObject();
        inSquare.addProperty("type", "minecraft:in_square");
        placement.add(inSquare);

        JsonObject heightRange = new JsonObject();
        heightRange.addProperty("type", "minecraft:height_range");
        heightRange.add("height", uniformHeight(0, 256));
        placement.add(heightRange);

        JsonObject biome = new JsonObject();
        biome.addProperty("type", "minecraft:biome");
        placement.add(biome);

        rootJson.add("placement", placement);
        write(root, "worldgen/placed_feature/chunkspawn.json", rootJson);
    }

    private static void dumpPlacedFeatureDungeon(Path root) throws IOException {
        DungeonManager dungeonManager = DungeonManager.getInstance();

        for (DungeonSchematic schematic : dungeonManager.schematics.values()) {
            JsonObject rootJson = new JsonObject();
            rootJson.addProperty("feature", LycanitesMobs.MODID + ":dungeon_" + schematic.name);
            JsonArray placement = new JsonArray();

            JsonObject inSquare = new JsonObject();
            inSquare.addProperty("type", "minecraft:in_square");
            placement.add(inSquare);

            JsonObject heightmap = new JsonObject();
            heightmap.addProperty("type", "minecraft:heightmap");
            heightmap.addProperty("heightmap", "WORLD_SURFACE_WG");
            placement.add(heightmap);

            JsonObject biome = new JsonObject();
            biome.addProperty("type", "minecraft:biome");
            placement.add(biome);

            rootJson.add("placement", placement);

            String fileName = "worldgen/placed_feature/dungeon_" + schematic.name + ".json";
            write(root, fileName, rootJson);
        }
    }

    private static void dumpFluidFeatures(Path root) throws IOException {
        Map<String, BaseLiquidBlock> map = FluidManager.getInstance().worldgenFluidBlocks;
        for (Map.Entry<String, BaseLiquidBlock> entry : map.entrySet()) {
            String fluidName = entry.getKey();
            BaseLiquidBlock block = entry.getValue();

            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
            ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(block.getFluid());
            if (blockId == null || fluidId == null) {
                continue;
            }

            dumpConfiguredLake(root, fluidName, blockId);
            dumpConfiguredSpring(root, fluidName, fluidId);
            dumpPlacedLake(root, fluidName);
            dumpPlacedSpring(root, fluidName);
        }
    }

    private static void dumpConfiguredLake(Path root, String fluidName, ResourceLocation blockId) throws IOException {
        JsonObject rootJson = new JsonObject();
        rootJson.addProperty("type", "minecraft:lake");

        JsonObject config = new JsonObject();

        JsonObject fluidProvider = new JsonObject();
        fluidProvider.addProperty("type", "minecraft:simple_state_provider");
        JsonObject fluidState = new JsonObject();
        fluidState.addProperty("Name", blockId.toString());
        fluidProvider.add("state", fluidState);
        config.add("fluid", fluidProvider);

        JsonObject barrierProvider = new JsonObject();
        barrierProvider.addProperty("type", "minecraft:simple_state_provider");
        JsonObject barrierState = new JsonObject();
        barrierState.addProperty("Name", "minecraft:stone");
        barrierProvider.add("state", barrierState);
        config.add("barrier", barrierProvider);

        rootJson.add("config", config);

        write(root, "worldgen/configured_feature/" + fluidName + "_lake.json", rootJson);
    }

    private static void dumpConfiguredSpring(Path root, String fluidName, ResourceLocation fluidId) throws IOException {
        JsonObject rootJson = new JsonObject();
        rootJson.addProperty("type", "minecraft:spring_feature");

        JsonObject config = new JsonObject();

        JsonObject state = new JsonObject();
        state.addProperty("Name", fluidId.toString());
        config.add("state", state);

        config.addProperty("requires_block_below", true);
        config.addProperty("rock_count", 4);
        config.addProperty("hole_count", 1);

        JsonArray validBlocks = new JsonArray();
        validBlocks.add("minecraft:stone");
        validBlocks.add("minecraft:granite");
        validBlocks.add("minecraft:diorite");
        validBlocks.add("minecraft:andesite");
        config.add("valid_blocks", validBlocks);

        rootJson.add("config", config);

        write(root, "worldgen/configured_feature/" + fluidName + "_spring.json", rootJson);
    }


    private static JsonObject namedBlock(String id) {
        JsonObject obj = new JsonObject();
        obj.addProperty("Name", id);
        return obj;
    }

    private static void dumpPlacedLake(Path root, String fluidName) throws IOException {
        JsonObject rootJson = new JsonObject();
        rootJson.addProperty("feature", LycanitesMobs.MODID + ":" + fluidName + "_lake");

        JsonArray placement = new JsonArray();

        JsonObject rarity = new JsonObject();
        rarity.addProperty("type", "minecraft:rarity_filter");
        rarity.addProperty("chance", 120);
        placement.add(rarity);

        JsonObject inSquare = new JsonObject();
        inSquare.addProperty("type", "minecraft:in_square");
        placement.add(inSquare);

        JsonObject heightmap = new JsonObject();
        heightmap.addProperty("type", "minecraft:heightmap");
        heightmap.addProperty("heightmap", "WORLD_SURFACE_WG");
        placement.add(heightmap);

        JsonObject biome = new JsonObject();
        biome.addProperty("type", "minecraft:biome");
        placement.add(biome);

        rootJson.add("placement", placement);

        write(root, "worldgen/placed_feature/" + fluidName + "_lake.json", rootJson);
    }

    private static void dumpPlacedSpring(Path root, String fluidName) throws IOException {
        JsonObject rootJson = new JsonObject();
        rootJson.addProperty("feature", LycanitesMobs.MODID + ":" + fluidName + "_spring");

        JsonArray placement = new JsonArray();

        JsonObject inSquare = new JsonObject();
        inSquare.addProperty("type", "minecraft:in_square");
        placement.add(inSquare);

        JsonObject heightRange = new JsonObject();
        heightRange.addProperty("type", "minecraft:height_range");
        heightRange.add("height", trapezoidHeight(8, 256));
        placement.add(heightRange);

        JsonObject biome = new JsonObject();
        biome.addProperty("type", "minecraft:biome");
        placement.add(biome);

        rootJson.add("placement", placement);

        write(root, "worldgen/placed_feature/" + fluidName + "_spring.json", rootJson);
    }

}
