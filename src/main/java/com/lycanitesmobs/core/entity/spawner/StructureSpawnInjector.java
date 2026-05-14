package com.lycanitesmobs.core.entity.spawner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongSet;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.entity.spawner.location.StructureSpawnLocation;
import com.lycanitesmobs.core.manager.SpawnerManager;
import com.lycanitesmobs.core.mixin.ModifiableStructureInfoAccessor;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraftforge.common.world.ModifiableStructureInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Injects Lycanites Mobs into vanilla/modded structure spawn pools at server start.
 * <p>
 * This replaces the old tick-based {@link StructureSpawnLocation} approach (which called
 * {@code findNearestMapStructure} every 60 seconds) with a one-time registry modification
 * that lets vanilla's {@code NaturalSpawner} handle structure-based spawning natively.
 * <p>
 * Configs are loaded from {@code common/lycanitesmobs/structurespawns/} JSON files.
 */
public class StructureSpawnInjector extends JSONLoader {

    private static StructureSpawnInjector INSTANCE;

    private final List<StructureSpawnConfig> configs = new ArrayList<>();

    public static StructureSpawnInjector getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StructureSpawnInjector();
        }
        return INSTANCE;
    }

    public void loadAllFromJson(ModInfo modInfo) {
        this.configs.clear();
        LMHelperClass.logDebug("structurespawners", "=== BEGIN loading structure spawn configs ===");
        this.loadAllJson(modInfo, "StructureSpawn", "structurespawns", "name", true, "structure_spawn", FileLoader.COMMON, StreamLoader.COMMON);
        LMHelperClass.logDebug("structurespawners", "=== DONE loading. Total configs: " + this.configs.size() + " ===");
        for (StructureSpawnConfig config : this.configs) {
            LMHelperClass.logDebug("structurespawners", "  Config '" + config.name + "': enabled=" + config.enabled
                    + ", category=" + config.category.getName()
                    + ", boundingBox=" + config.boundingBox
                    + ", structures=" + config.structures.size()
                    + ", mobs=" + config.mobs.size());
            for (ResourceLocation structureId : config.structures) {
                LMHelperClass.logDebug("structurespawners", "    structure: " + structureId);
            }
            for (MobEntryConfig mob : config.mobs) {
                LMHelperClass.logDebug("structurespawners", "    mob: " + mob.mobId + " weight=" + mob.weight + " min=" + mob.minCount + " max=" + mob.maxCount);
            }
        }
    }

    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        LMHelperClass.logDebug("structurespawners", "parseJson called with: " + json);
        StructureSpawnConfig config = new StructureSpawnConfig();
        config.loadFromJSON(json);
        this.configs.add(config);
        LMHelperClass.logDebug("structurespawners", "  Parsed config '" + config.name + "' successfully.");
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        LMHelperClass.logDebug("structurespawners", "=== onServerAboutToStart fired. Config count: " + this.configs.size() + " ===");

        // Auto-disable old tick-based structure spawners that are now replaced by injection.
        for (StructureSpawnConfig config : this.configs) {
            if (!config.enabled) {
                LMHelperClass.logDebug("structurespawners", "Skipping disabled config: " + config.name);
                continue;
            }
            Spawner oldSpawner = SpawnerManager.getInstance().spawners.get(config.name);
            if (oldSpawner != null && oldSpawner.enabled) {
                boolean hasStructureLocation = oldSpawner.locations.stream()
                        .anyMatch(loc -> loc instanceof StructureSpawnLocation);
                if (hasStructureLocation) {
                    oldSpawner.enabled = false;
                    LMHelperClass.logDebug("structurespawners",
                            "Auto-disabled old structure spawner '" + config.name
                                    + "' â€” replaced by structure spawn injection.");
                }
            } else {
                LMHelperClass.logDebug("structurespawners", "No old spawner to disable for '" + config.name
                        + "' (spawner=" + (oldSpawner != null ? "exists,enabled=" + oldSpawner.enabled : "null") + ")");
            }
        }

        // Inject spawns into structure registry.
        Registry<Structure> structureRegistry = event.getServer().registryAccess()
                .registryOrThrow(Registries.STRUCTURE);
        LMHelperClass.logDebug("structurespawners", "Structure registry obtained. Total structures in registry: " + structureRegistry.size());
        int injectedCount = 0;

        for (StructureSpawnConfig config : this.configs) {
            if (!config.enabled) continue;

            List<MobSpawnSettings.SpawnerData> spawnerDataList = config.buildSpawnerData();
            LMHelperClass.logDebug("structurespawners", "Config '" + config.name + "': buildSpawnerData returned " + spawnerDataList.size() + " entries.");
            for (MobSpawnSettings.SpawnerData data : spawnerDataList) {
                LMHelperClass.logDebug("structurespawners", "  SpawnerData: entityType=" + ForgeRegistries.ENTITY_TYPES.getKey(data.type) + " weight=" + data.getWeight().asInt() + " min=" + data.minCount + " max=" + data.maxCount);
            }
            if (spawnerDataList.isEmpty()) {
                LMHelperClass.logDebug("structurespawners", "  SKIPPING config '" + config.name + "' â€” no valid spawner data built.");
                continue;
            }

            for (ResourceLocation structureId : config.structures) {
                Structure structure = structureRegistry.get(structureId);
                if (structure == null) {
                    LMHelperClass.logDebug("structurespawners",
                            "  Structure NOT in registry, skipping: " + structureId);
                    continue;
                }

                LMHelperClass.logDebug("structurespawners", "  Injecting into structure: " + structureId + " (class=" + structure.getClass().getSimpleName() + ")");
                injectSpawns(structure, config.category, config.boundingBox, spawnerDataList, structureId);
                injectedCount++;
            }
        }

        LMHelperClass.logDebug("structurespawners",
                "=== Structure spawn injection complete. " + injectedCount + " structure(s) modified. ===");
    }

    /**
     * Enforces per-config caps on the number of creatures inside a structure's bounding box.
     * Fires on every natural spawn attempt after position/placement checks have passed but
     * before the mob is added to the world, so denying here cleanly cancels the spawn.
     */
    @SubscribeEvent
    public void onMobSpawnPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL) return;
        if (this.configs.isEmpty()) return;
        ServerLevel level = event.getLevel().getLevel();

        Mob mob = event.getEntity();
        EntityType<?> type = mob.getType();
        BlockPos pos = mob.blockPosition();

        boolean relevant = false;
        for (StructureSpawnConfig config : this.configs) {
            if (config.enabled && config.maxInStructure >= 0 && config.resolvedTypes.contains(type)) {
                relevant = true;
                break;
            }
        }
        if (!relevant) return;

        Map<Structure, LongSet> structuresAtPos = level.structureManager().getAllStructuresAt(pos);
        if (structuresAtPos.isEmpty()) return;

        Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        for (StructureSpawnConfig config : this.configs) {
            if (!config.enabled || config.maxInStructure < 0) continue;
            if (!config.resolvedTypes.contains(type)) continue;

            for (Structure structure : structuresAtPos.keySet()) {
                ResourceLocation id = structureRegistry.getKey(structure);
                if (id == null || !config.structures.contains(id)) continue;

                StructureStart start = level.structureManager().getStructureAt(pos, structure);
                if (start == StructureStart.INVALID_START) continue;

                BoundingBox bb = start.getBoundingBox();
                AABB aabb = AABB.of(bb);
                int count = level.getEntitiesOfClass(Mob.class, aabb,
                        e -> config.resolvedTypes.contains(e.getType())).size();

                if (count >= config.maxInStructure) {
                    LMHelperClass.logDebug("MobSpawns", "Structure spawn cap reached for '" + config.name
                            + "' in " + id + " (count=" + count + ", max=" + config.maxInStructure + "). Denying spawn of " + type + ".");
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }
    }

    private void injectSpawns(Structure structure, MobCategory category,
                              StructureSpawnOverride.BoundingBoxType bbType,
                              List<MobSpawnSettings.SpawnerData> newSpawns,
                              ResourceLocation structureId) {
        
        
        
        ModifiableStructureInfo modInfo = structure.modifiableStructureInfo();
        LMHelperClass.logDebug("structurespawners", "    [" + structureId + "] modifiableStructureInfo obtained: " + modInfo);
        LMHelperClass.logDebug("structurespawners", "    [" + structureId + "] modifiedStructureInfo is null? " + (modInfo.getModifiedStructureInfo() == null));

        Structure.StructureSettings currentSettings = modInfo.get().structureSettings();
        Map<MobCategory, StructureSpawnOverride> existingOverrides = currentSettings.spawnOverrides();
        LMHelperClass.logDebug("structurespawners", "    [" + structureId + "] existing spawnOverrides categories: " + existingOverrides.keySet());
        LMHelperClass.logDebug("structurespawners", "    [" + structureId + "] existing override for " + category.getName() + ": " + (existingOverrides.containsKey(category) ? "YES" : "NO"));

        
        Map<MobCategory, StructureSpawnOverride> newOverrides = new HashMap<>(existingOverrides);

        StructureSpawnOverride existing = newOverrides.get(category);
        List<MobSpawnSettings.SpawnerData> mergedList;
        if (existing != null) {
            mergedList = new ArrayList<>(existing.spawns().unwrap());
            mergedList.addAll(newSpawns);
            bbType = existing.boundingBox(); 
            LMHelperClass.logDebug("structurespawners", "    [" + structureId + "] Merging with " + existing.spawns().unwrap().size() + " existing entries. BoundingBox preserved: " + bbType);
        } else {
            mergedList = new ArrayList<>(newSpawns);
            LMHelperClass.logDebug("structurespawners", "    [" + structureId + "] No existing override, creating fresh. BoundingBox: " + bbType);
        }

        newOverrides.put(category, new StructureSpawnOverride(
                bbType, WeightedRandomList.create(mergedList)));

        
        Structure.StructureSettings newSettings = new Structure.StructureSettings(
                currentSettings.biomes(),
                newOverrides,
                currentSettings.step(),
                currentSettings.terrainAdaptation()
        );

        
        ModifiableStructureInfoAccessor infoAccessor = (ModifiableStructureInfoAccessor) modInfo;
        infoAccessor.setModifiedStructureInfo(
                new ModifiableStructureInfo.StructureInfo(newSettings));

        
        Structure.StructureSettings verifySettings = structure.modifiableStructureInfo().get().structureSettings();
        Map<MobCategory, StructureSpawnOverride> verifyOverrides = verifySettings.spawnOverrides();
        LMHelperClass.logDebug("structurespawners", "    [" + structureId + "] POST-INJECT verification:");
        LMHelperClass.logDebug("structurespawners", "    [" + structureId + "]   spawnOverrides categories: " + verifyOverrides.keySet());
        StructureSpawnOverride verifyOverride = verifyOverrides.get(category);
        if (verifyOverride != null) {
            LMHelperClass.logDebug("structurespawners", "    [" + structureId + "]   " + category.getName() + " override: bb=" + verifyOverride.boundingBox() + " entries=" + verifyOverride.spawns().unwrap().size());
            for (MobSpawnSettings.SpawnerData data : verifyOverride.spawns().unwrap()) {
                LMHelperClass.logDebug("structurespawners", "    [" + structureId + "]     -> " + ForgeRegistries.ENTITY_TYPES.getKey(data.type) + " w=" + data.getWeight().asInt() + " min=" + data.minCount + " max=" + data.maxCount);
            }
        } else {
            LMHelperClass.logDebug("structurespawners", "    [" + structureId + "]   !!! VERIFICATION FAILED â€” no override found for " + category.getName() + " after injection!");
        }

        
        Map<MobCategory, StructureSpawnOverride> directOverrides = structure.spawnOverrides();
        LMHelperClass.logDebug("structurespawners", "    [" + structureId + "]   structure.spawnOverrides() categories: " + directOverrides.keySet());
        if (directOverrides.containsKey(category)) {
            LMHelperClass.logDebug("structurespawners", "    [" + structureId + "]   structure.spawnOverrides() has " + category.getName() + " with " + directOverrides.get(category).spawns().unwrap().size() + " entries. LOOKS GOOD.");
        } else {
            LMHelperClass.logDebug("structurespawners", "    [" + structureId + "]   !!! structure.spawnOverrides() MISSING " + category.getName() + "! Forge coremod may still be bypassing our injection.");
        }
    }

    

    static class StructureSpawnConfig {
        String name;
        boolean enabled = true;
        MobCategory category = MobCategory.MONSTER;
        StructureSpawnOverride.BoundingBoxType boundingBox = StructureSpawnOverride.BoundingBoxType.PIECE;
        int maxInStructure = -1;
        List<ResourceLocation> structures = new ArrayList<>();
        List<MobEntryConfig> mobs = new ArrayList<>();
        /** Cache of resolved entity types from this config's mob list, for fast membership checks during spawn events. */
        final Set<EntityType<?>> resolvedTypes = new HashSet<>();

        void loadFromJSON(JsonObject json) {
            if (json.has("name"))
                this.name = json.get("name").getAsString();
            if (json.has("enabled"))
                this.enabled = json.get("enabled").getAsBoolean();
            if (json.has("maxInStructure"))
                this.maxInStructure = json.get("maxInStructure").getAsInt();
            if (json.has("category")) {
                String catName = json.get("category").getAsString();
                for (MobCategory cat : MobCategory.values()) {
                    if (cat.getName().equalsIgnoreCase(catName)) {
                        this.category = cat;
                        break;
                    }
                }
            }
            if (json.has("boundingBox")) {
                String bb = json.get("boundingBox").getAsString();
                this.boundingBox = "structure".equalsIgnoreCase(bb)
                        ? StructureSpawnOverride.BoundingBoxType.STRUCTURE
                        : StructureSpawnOverride.BoundingBoxType.PIECE;
            }
            if (json.has("structures")) {
                this.structures.clear();
                for (JsonElement el : json.getAsJsonArray("structures")) {
                    this.structures.add(new ResourceLocation(el.getAsString()));
                }
            }
            if (json.has("mobs")) {
                this.mobs.clear();
                for (JsonElement el : json.getAsJsonArray("mobs")) {
                    MobEntryConfig mob = new MobEntryConfig();
                    mob.loadFromJSON(el.getAsJsonObject());
                    this.mobs.add(mob);
                }
            }
        }

        List<MobSpawnSettings.SpawnerData> buildSpawnerData() {
            List<MobSpawnSettings.SpawnerData> list = new ArrayList<>();
            this.resolvedTypes.clear();
            LMHelperClass.logDebug("structurespawners", "  buildSpawnerData for '" + this.name + "': " + this.mobs.size() + " mob entries to resolve.");
            for (MobEntryConfig mob : this.mobs) {
                LMHelperClass.logDebug("structurespawners", "    Resolving mobId: " + mob.mobId);
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(mob.mobId);
                if (entityType == null) {
                    LMHelperClass.logWarning("structurespawners",
                            "    !!! Entity type NOT FOUND in ForgeRegistries, skipping: " + mob.mobId);
                    continue;
                }
                ResourceLocation resolvedId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                LMHelperClass.logDebug("structurespawners", "    Resolved to: " + resolvedId + " (entityType=" + entityType + ")");
                list.add(new MobSpawnSettings.SpawnerData(
                        entityType, mob.weight, mob.minCount, mob.maxCount));
                this.resolvedTypes.add(entityType);
            }
            LMHelperClass.logDebug("structurespawners", "  buildSpawnerData result: " + list.size() + " valid entries.");
            return list;
        }
    }

    static class MobEntryConfig {
        ResourceLocation mobId;
        int weight = 8;
        int minCount = 1;
        int maxCount = 3;

        void loadFromJSON(JsonObject json) {
            if (json.has("mobId"))
                this.mobId = new ResourceLocation(json.get("mobId").getAsString());
            if (json.has("weight"))
                this.weight = json.get("weight").getAsInt();
            if (json.has("minCount"))
                this.minCount = json.get("minCount").getAsInt();
            if (json.has("maxCount"))
                this.maxCount = json.get("maxCount").getAsInt();
        }
    }
}
