package com.lycanitesmobs.core.manager;

import com.google.gson.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import com.lycanitesmobs.core.entity.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class SpawnerManager extends JSONLoader {
    /**
     * This manages all Spawners, it load them and can also destroy them. Spawners are then ran by Spawn Triggers which are called from the SpawnerEventListener.
     **/

    public static SpawnerManager INSTANCE;

    public Map<String, Spawner> spawners = new HashMap<>();
    public List<SpawnCondition> globalSpawnConditions = new ArrayList<>();


    /**
     * Returns the main SpawnerManager INSTANCE or creates it and returns it.
     **/
    public static SpawnerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpawnerManager();
        }
        return INSTANCE;
    }


    /**
     * Loads all JSON Spawners.
     **/
    public void loadAllFromJson(ModInfo modInfo) {
        // Spawners:
        this.loadAllJson(modInfo, "Spawner", "spawners", "name", true, "spawner", FileLoader.COMMON, StreamLoader.COMMON);
        LMHelperClass.logDebug("Spawner", "Complete! " + this.spawners.size() + " JSON Spawners Loaded In Total.");

        // Mob Event Spawners:
        this.loadAllJson(modInfo, "Spawner", "mobevents", "name", true, "spawner", FileLoader.COMMON, StreamLoader.COMMON);
        LMHelperClass.logDebug("Spawner", "Complete! " + this.spawners.size() + " JSON Spawners Loaded In Total.");

        // Load Global Spawn Conditions:
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        String configPath = new File(".") + "/config/" + LycanitesMobs.MODID + "/";
        this.globalSpawnConditions.clear();

        JsonObject defaultGlobalJson;
        if (FileLoader.COMMON.ready) {
            Path defaultGlobalPath = FileLoader.COMMON.getPath("globalspawner.json");
            defaultGlobalJson = this.loadJsonObject(gson, defaultGlobalPath);
        } else {
            defaultGlobalJson = this.loadJsonObject(gson, StreamLoader.COMMON.getStream("globalspawner.json"));
        }
        if (defaultGlobalJson == null) {
            LMHelperClass.logWarning("", "Could not find Global Spawning JSON.");
        }

        File customGlobalFile = new File(configPath + "globalspawner.json");
        JsonObject customGlobalJson = null;
        if (customGlobalFile.exists()) {
            customGlobalJson = this.loadJsonObject(gson, customGlobalFile.toPath());
        }

        JsonObject globalJson = this.writeDefaultJSONObject(gson, "globalspawner", defaultGlobalJson, customGlobalJson);
        if (globalJson.has("conditions")) {
            JsonArray jsonArray = globalJson.get("conditions").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject spawnConditionJson = jsonIterator.next().getAsJsonObject();
                SpawnCondition spawnCondition = SpawnCondition.createFromJSON(spawnConditionJson);
                if (spawnCondition != null) this.globalSpawnConditions.add(spawnCondition);
            }
        }
    }


    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        Spawner spawner = new Spawner();
        spawner.loadFromJSON(json);
        this.addSpawner(spawner);
    }


    /**
     * Reloads all JSON Spawners.
     **/
    public void reload() {
        for (Spawner spawner : this.spawners.values().toArray(new Spawner[this.spawners.size()])) {
            spawner.destroy();
        }

        this.loadAllFromJson(LycanitesMobs.modInfo);
    }


    /**
     * Adds a new Spawner to this Manager.
     **/
    public void addSpawner(Spawner spawner) {
        if (this.spawners.containsKey(spawner.name)) {
            LMHelperClass.logWarning("", "[Spawner Manager] Tried to add a Spawner with a name that is already in use: " + spawner.name);
            return;
        }
        if (this.spawners.values().contains(spawner)) {
            LMHelperClass.logWarning("", "[Spawner Manager] Tried to add a Spawner that is already added: " + spawner.name);
            return;
        }
        this.spawners.put(spawner.name, spawner);
    }


    /**
     * Removes a Spawner from this Manager.
     **/
    public void removeSpawner(Spawner spawner) {
        if (!this.spawners.containsKey(spawner.name)) {
            LMHelperClass.logWarning("", "[Spawner Manager] Tried to remove a Spawner that hasn't been added: " + spawner.name);
            return;
        }
        this.spawners.remove(spawner.name);
    }
}
