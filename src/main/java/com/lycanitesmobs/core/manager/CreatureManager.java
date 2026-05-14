package com.lycanitesmobs.core.manager;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.creature.*;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.data.config.ConfigCreatures;
import com.lycanitesmobs.core.entity.util.CreatureStats;
import com.lycanitesmobs.core.entity.util.EntityFactory;
import com.lycanitesmobs.core.entity.spawner.SpawnerMobRegistry;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class CreatureManager extends JSONLoader {
    public static CreatureManager INSTANCE;
    public static String[] DIFFICULTY_NAMES = new String[]{"easy", "normal", "hard"};
    public static double[] DIFFICULTY_DEFAULTS = new double[]{0.8D, 1.0D, 1.1D};

    /**
     * Handles all global creature general config settings.
     **/
    public CreatureConfig config;

    /**
     * Handles all global creature spawning config settings.
     **/
    public CreatureSpawnConfig spawnConfig;

    /**
     * A map of all creatures types by name.
     **/
    public Map<String, CreatureType> creatureTypes = new HashMap<>();

    /**
     * A map of all creatures groups by name.
     **/
    public Map<String, CreatureGroup> creatureGroups = new HashMap<>();

    /**
     * A map of all creatures by name.
     **/
    public Map<String, CreatureInfo> creatures = new HashMap<>();

    /**
     * A map of all creatures by class.
     **/
    public Map<Class, CreatureInfo> creatureClassMap = new HashMap<>();

    /**
     * A list of mods that have loaded with this Creature Manager.
     **/
    public List<ModInfo> loadedMods = new ArrayList<>();

    /**
     * A map containing all the global multipliers for each stat for each difficulty.
     **/
    public Map<String, Double> difficultyMultipliers = new HashMap<>();

    /**
     * A map containing all the global multipliers for each stat for mob level scaling.
     **/
    public Map<String, Double> levelMultipliers = new HashMap<>();

    /**
     * The global multiplier to use for the health of tamed creatures.
     **/
    public double tamedHealthMultiplier = 3;

    /**
     * Set to true if Doomlike Dungeons is loaded allowing mobs to register their Dungeon themes.
     **/
    public boolean dlDungeonsLoaded = false;

    /**
     * Constructor
     */
    public CreatureManager() {
        this.config = new CreatureConfig();
        this.spawnConfig = new CreatureSpawnConfig();
    }

    /**
     * Returns the main Creature Manager instance or creates it and returns it.
     **/
    public static CreatureManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CreatureManager();
        }
        return INSTANCE;
    }

    @NotNull
    private static EntityType.Builder<?> getBuilder(CreatureInfo creatureInfo) {
        EntityType.Builder<?> entityTypeBuilder = EntityType.Builder.of(EntityFactory.getInstance(), creatureInfo.peaceful ? MobCategory.CREATURE : MobCategory.MONSTER);
        //entityTypeBuilder.setCustomClientFactory(EntityFactory.getInstance().createOnClientFunction); // Was created when experimenting with custom spawn packets, not in use currently but may be needed in the future.
        entityTypeBuilder.setTrackingRange(creatureInfo.isBoss() ? 32 : 10);
        entityTypeBuilder.setUpdateInterval(3);
        entityTypeBuilder.setShouldReceiveVelocityUpdates(false);
        entityTypeBuilder.sized((float) creatureInfo.width, (float) creatureInfo.height);
        return entityTypeBuilder;
    }

    /**
     * Called during startup and initially loads everything in this manager.
     *
     * @param modInfo The mod loading this manager.
     */
    public void startup(ModInfo modInfo) {
        // Load From JSON:
        this.loadCreatureTypesFromJSON(modInfo);
        this.loadCreatureGroupsFromJSON(modInfo);
        this.loadCreaturesFromJSON(modInfo);

        // Initialise:
        for (CreatureType creatureType : this.creatureTypes.values()) {
            creatureType.init();
        }
        for (CreatureGroup creatureGroup : this.creatureGroups.values()) {
            creatureGroup.init();
        }
        for (CreatureInfo creatureInfo : this.creatures.values()) {
            final var entityTypeBuilder = getBuilder(creatureInfo);
            ObjectManager.entityTypes.put(creatureInfo.getName(), () -> entityTypeBuilder.build(creatureInfo.getName()));
        }
    }

    /**
     * Loads all JSON Creature Types. Should be done before creatures are loaded so that they can find their type on load.
     **/
    public void loadCreatureTypesFromJSON(ModInfo modInfo) {
        this.loadAllJson(modInfo, "Creature Type", "creaturetypes", "name", true, null, FileLoader.COMMON, StreamLoader.COMMON);
        LMHelperClass.logDebug("Creature", "Complete! " + this.creatureTypes.size() + " JSON Creature Types Loaded In Total.");
    }

    /**
     * Loads all JSON Creature Groups. Should be done before creatures are loaded so that they can find their groups on load.
     **/
    public void loadCreatureGroupsFromJSON(ModInfo modInfo) {
        this.loadAllJson(modInfo, "Creature Group", "creaturegroups", "name", true, null, FileLoader.COMMON, StreamLoader.COMMON);
        LMHelperClass.logDebug("Creature", "Complete! " + this.creatureGroups.size() + " JSON Creature Groups Loaded In Total.");
    }

    /**
     * Loads all JSON Creatures. Should only initially be done on pre-init and before Creature Info is loaded and can then be done in game on reload.
     **/
    public void loadCreaturesFromJSON(ModInfo modInfo) {
        try {
            if (!this.loadedMods.contains(modInfo)) {
                this.loadedMods.add(modInfo);
            }
            this.loadAllJson(modInfo, "Creature", "creatures", "name", true, null, FileLoader.COMMON, StreamLoader.COMMON);
            LMHelperClass.logDebug("Creature", "Complete! " + this.creatures.size() + " JSON Creatures Loaded In Total.");
        } catch (Exception e) {
            LMHelperClass.logError("Error loading Creatures for: " + modInfo.name);
            throw (e);
        }
    }

    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        // Parse Creature Type JSON:
        if ("Creature Type".equals(loadGroup)) {
            CreatureType creatureType = new CreatureType(modInfo);
            creatureType.loadFromJson(json);

            // Already Exists:
            if (this.creatureTypes.containsKey(creatureType.getName())) {
                creatureType = this.creatureTypes.get(creatureType.getName());
                creatureType.loadFromJson(json);
            }

            this.creatureTypes.put(creatureType.getName(), creatureType);
            return;
        }

        // Parse Creature Group JSON:
        if ("Creature Group".equals(loadGroup)) {
            CreatureGroup creatureGroup = new CreatureGroup();
            creatureGroup.loadFromJson(json);

            // Already Exists:
            if (this.creatureGroups.containsKey(creatureGroup.name)) {
                creatureGroup = this.creatureGroups.get(creatureGroup.name);
                creatureGroup.loadFromJson(json);
            }

            this.creatureGroups.put(creatureGroup.name, creatureGroup);
            return;
        }

        // Parse Creature JSON:
        if ("Creature".equals(loadGroup)) {
            CreatureInfo creatureInfo = new CreatureInfo(modInfo);
            if (!creatureInfo.loadFromJson(json)) {
                return;
            }

            // Already Exists:
            if (this.creatures.containsKey(creatureInfo.getName())) {
                creatureInfo = this.creatures.get(creatureInfo.getName());
                if (!creatureInfo.loadFromJson(json)) {
                    return;
                }
            }

            this.creatures.put(creatureInfo.getName(), creatureInfo);
            this.creatureClassMap.put(creatureInfo.entityClass, creatureInfo);
        }
    }

    /**
     * Called during early start up, loads all global configs into this manager.
     **/
    public void loadConfig() {
        this.config.loadConfig();
        this.spawnConfig.loadConfig();

        // Difficulty:
        this.difficultyMultipliers = new HashMap<>();
        for (String difficultyName : DIFFICULTY_NAMES) {
            for (String statName : CreatureStats.STAT_NAMES) {
                this.difficultyMultipliers.put((difficultyName + "-" + statName).toUpperCase(Locale.ENGLISH), ConfigCreatures.INSTANCE.difficultyMultipliers.get(difficultyName).get(statName).get());
            }
        }

        // Level:
        for (String statName : CreatureStats.STAT_NAMES) {
            this.levelMultipliers.put(statName.toUpperCase(Locale.ENGLISH), ConfigCreatures.INSTANCE.levelMultipliers.get(statName).get());
        }
    }


    /**
     * Reloads all Creature JSON.
     */
    public void reload() {
        this.loadConfig();
        SpawnerMobRegistry.SPAWNER_MOB_REGISTRIES.clear();
        for (ModInfo group : this.loadedMods) {
            this.loadCreaturesFromJSON(group);
        }
    }

    /**
     * Gets a creature type by name.
     *
     * @param creatureTypeName The name of the creature type to get.
     * @return The Creature Type.
     */
    public CreatureType getCreatureType(String creatureTypeName) {
        if (!this.creatureTypes.containsKey(creatureTypeName))
            return null;
        return this.creatureTypes.get(creatureTypeName);
    }

    /**
     * Gets a creature group by name.
     *
     * @param creatureGroupName The name of the creature group to get.
     * @return The Creature Group.
     */
    public CreatureGroup getCreatureGroup(String creatureGroupName) {
        if (!this.creatureGroups.containsKey(creatureGroupName))
            return null;
        return this.creatureGroups.get(creatureGroupName);
    }

    /**
     * Gets a creature by name.
     *
     * @param creatureName The name of the creature to get.
     * @return The Creature Info or null.
     */
    @Nullable
    public CreatureInfo getCreature(String creatureName) {
        if (!this.creatures.containsKey(creatureName))
            return null;
        return this.creatures.get(creatureName);
    }

    /**
     * Gets a creature by class.
     *
     * @param creatureClass The class of the creature to get.
     * @return The Creature Info.
     */
    public CreatureInfo getCreature(Class creatureClass) {
        if (!this.creatureClassMap.containsKey(creatureClass))
            return null;
        return this.creatureClassMap.get(creatureClass);
    }

    /**
     * Gets a Creature Entity Type by name.
     *
     * @param creatureName The name of the creature to get.
     * @return The Entity Type or null.
     */
    @Nullable
    public EntityType<? extends LivingEntity> getEntityType(String creatureName) {
        CreatureInfo creatureInfo = this.getCreature(creatureName);
        if (creatureInfo == null)
            return null;
        return creatureInfo.getEntityType();
    }

    /**
     * Gets a creature by entity id.
     *
     * @param entityId The the entity id of the creature to get. Periods will be replaced with semicolons.
     * @return The Creature Info.
     */
    public CreatureInfo getCreatureFromId(String entityId) {
        entityId = entityId.replace(".", ":");
        String[] mobIdParts = entityId.toLowerCase().split(":");
        return this.getCreature(mobIdParts[mobIdParts.length - 1]);
    }

    /**
     * Returns a global difficulty multiplier for a stat.
     *
     * @param difficultyName The difficulty name.
     * @param statName       The stat name.
     * @return The multiplier.
     */
    public double getDifficultyMultiplier(String difficultyName, String statName) {
        String key = difficultyName.toUpperCase(Locale.ENGLISH) + "-" + statName.toUpperCase(Locale.ENGLISH);
        if (!this.difficultyMultipliers.containsKey(key)) {
            return 1;
        }
        return this.difficultyMultipliers.get(key);
    }

    /**
     * Returns a global level multiplier for a stat.
     *
     * @param statName The stat name.
     * @return The multiplier.
     */
    public double getLevelMultiplier(String statName) {
        if (!this.levelMultipliers.containsKey(statName.toUpperCase(Locale.ENGLISH))) {
            return 1;
        }
        return this.levelMultipliers.get(statName.toUpperCase(Locale.ENGLISH));
    }
}
