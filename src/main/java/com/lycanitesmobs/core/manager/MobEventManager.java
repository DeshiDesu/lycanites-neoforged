package com.lycanitesmobs.core.manager;

import com.google.gson.*;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.event.mobevent.MobEventPlayerClient;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.data.config.ConfigMobEvent;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.event.mobevent.MobEventSchedule;
import com.lycanitesmobs.core.entity.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.nio.file.Path;
import java.util.*;


public class MobEventManager extends JSONLoader {
    // Global:
    protected static MobEventManager INSTANCE;

    // Mob Events:
    public Map<String, MobEvent> mobEvents = new HashMap<>();
    public List<MobEventSchedule> mobEventSchedules = new ArrayList<>();
    public List<SpawnCondition> globalEventConditions = new ArrayList<>();

    // Properties:
    public boolean mobEventsEnabled = true;
    public boolean mobEventsRandom = false;
    /**
     * The default temporary time applied to mobs spawned from events, where it will forcefully despawn after the specified time (in ticks). MobSpawns can override this.
     **/
    public int defaultMobDuration = 12000;
    public int minEventsRandomDay = 0;
    public int minTicksUntilEvent = 60 * 60 * 20;
    public int maxTicksUntilEvent = 120 * 60 * 20;


    /**
     * Returns the main Mob Event Manager instance.
     **/
    public static MobEventManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MobEventManager();
        }
        return INSTANCE;
    }


    /**
     * Called during early start up, loads all global event configs into the manager.
     **/
    public void loadConfig() {
        this.mobEventsEnabled = ConfigMobEvent.INSTANCE.mobEventsEnabled.get();
        this.mobEventsRandom = ConfigMobEvent.INSTANCE.mobEventsRandom.get();
        this.defaultMobDuration = ConfigMobEvent.INSTANCE.defaultMobDuration.get();
        this.minEventsRandomDay = ConfigMobEvent.INSTANCE.minEventsRandomDay.get();
        this.minTicksUntilEvent = ConfigMobEvent.INSTANCE.minTicksUntilEvent.get();
        this.maxTicksUntilEvent = ConfigMobEvent.INSTANCE.maxTicksUntilEvent.get();
    }


    /**
     * Loads all JSON Creature Types. Should be done before creatures are loaded so that they can find their type on load.
     **/
    public void loadAllFromJson(ModInfo groupInfo) {
        try {
            this.loadAllJson(groupInfo, "Mob Event", "mobevents", "name", true, "event", FileLoader.COMMON, StreamLoader.COMMON);
            LMHelperClass.logDebug("MobEvents", "Complete! " + this.mobEvents.size() + " JSON Mob Events Loaded In Total.");
        } catch (Exception e) {
        }

        this.loadGlobalEventConditions();
        this.loadScheduledEvents();
    }

    public void loadGlobalEventConditions() {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        String configPath = new File(".") + "/config/" + LycanitesMobs.MODID + "/";
        this.globalEventConditions.clear();

        JsonObject defaultGlobalJson;
        if (FileLoader.SERVER.ready) {
            Path defaultGlobalPath = FileLoader.SERVER.getPath("globalmobevent.json");
            defaultGlobalJson = this.loadJsonObject(gson, defaultGlobalPath);
        } else {
            defaultGlobalJson = this.loadJsonObject(gson, StreamLoader.SERVER.getStream("globalmobevent.json"));
        }
        if (defaultGlobalJson == null) {
            LMHelperClass.logWarning("", "Could not find Global Mob Event JSON.");
        }

        File customGlobalFile = new File(configPath + "globalmobevent.json");
        JsonObject customGlobalJson = null;
        if (customGlobalFile.exists()) {
            customGlobalJson = this.loadJsonObject(gson, customGlobalFile.toPath());
        }

        JsonObject globalJson = this.writeDefaultJSONObject(gson, "globalmobevent", defaultGlobalJson, customGlobalJson);
        if (globalJson.has("conditions")) {
            JsonArray jsonArray = globalJson.get("conditions").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject spawnConditionJson = jsonIterator.next().getAsJsonObject();
                SpawnCondition spawnCondition = SpawnCondition.createFromJSON(spawnConditionJson);
                if (spawnCondition != null) this.globalEventConditions.add(spawnCondition);
            }
        }
    }

    public void loadScheduledEvents() {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        String configPath = new File(".") + "/config/" + LycanitesMobs.MODID + "/";
        this.mobEventSchedules.clear();

        JsonObject defaultScheduleJson;
        if (FileLoader.SERVER.ready) {
            Path defaultSchedulePath = FileLoader.SERVER.getPath("mobeventschedule.json");
            defaultScheduleJson = this.loadJsonObject(gson, defaultSchedulePath);
        } else {
            defaultScheduleJson = this.loadJsonObject(gson, StreamLoader.SERVER.getStream("mobeventschedule.json"));
        }

        File customScheduleFile = new File(configPath + "mobeventschedule.json");
        JsonObject customScheduleJson = null;
        if (customScheduleFile.exists()) {
            customScheduleJson = this.loadJsonObject(gson, customScheduleFile.toPath());
        }

        JsonObject scheduleJson = this.writeDefaultJSONObject(gson, "mobeventschedule", defaultScheduleJson, customScheduleJson);
        if (scheduleJson.has("schedules")) {
            JsonArray jsonArray = scheduleJson.get("schedules").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject scheduleEntryJson = jsonIterator.next().getAsJsonObject();
                MobEventSchedule mobEventSchedule = MobEventSchedule.createFromJSON(scheduleEntryJson);
                this.mobEventSchedules.add(mobEventSchedule);
            }
        }
        if (this.mobEventSchedules.size() > 0) {
            LMHelperClass.logDebug("MobEvents", "Loaded " + this.mobEventSchedules.size() + " Mob Event Schedules.");
        }
    }

    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        LMHelperClass.logDebug("MobEvents", "Loading Mob Event JSON: " + json);
        MobEvent mobEvent = new MobEvent();
        mobEvent.loadFromJSON(json);
        this.addMobEvent(mobEvent);
    }


    /**
     * Reloads all JSON Mob Events.
     **/
    public void reload() {
        LMHelperClass.logDebug("MobEvents", "Destroying JSON Mob Events!");
        for (MobEvent mobEvent : this.mobEvents.values().toArray(new MobEvent[this.mobEvents.size()])) {
            mobEvent.destroy();
        }

        this.loadAllFromJson(LycanitesMobs.modInfo);
    }


    /**
     * Adds the provided Mob Event.
     *
     * @param mobEvent The Mob Event instance to add.
     **/
    public void addMobEvent(MobEvent mobEvent) {
        if (mobEvent == null)
            return;
        this.mobEvents.put(mobEvent.name, mobEvent);
        try {
            ObjectManager.addSound("mobevent_" + mobEvent.title.toLowerCase(), "mobevent." + mobEvent.title.toLowerCase());
        } catch (Exception e) {
        }
    }


    /**
     * Removes a Mob Event from this Manager.
     **/
    public void removeMobEvent(MobEvent mobEvent) {
        if (!this.mobEvents.containsKey(mobEvent.name)) {
            LMHelperClass.logWarning("", "[MobEvents] Tried to remove a Mob Event that hasn't been added: " + mobEvent.name);
            return;
        }
        this.mobEvents.remove(mobEvent.name);
    }


    /**
     * Gets a Mob Event by name.
     *
     * @return Null if the event does not exist.
     **/
    public MobEvent getMobEvent(String mobEventName) {
        if (!this.mobEvents.containsKey(mobEventName)) {
            return null;
        }
        return this.mobEvents.get(mobEventName);
    }


    /**
     * Called every tick in a world and updates any active Server Side Mob Event players.
     **/
    @SubscribeEvent
    public void onWorldUpdate(TickEvent.LevelTickEvent event) {
        Level world = event.level;
        if (world.isClientSide)
            return;
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        if (worldExt == null) {
            return;
        }

        // Only Tick On World Time Ticks:
        if (worldExt.lastEventUpdateTime == world.getGameTime()) {
            return;
        }
        worldExt.lastEventUpdateTime = world.getGameTime();

        // Only Run If Players Are Present:
        if (world.players().isEmpty()) {
            return;
        }

        // Update World Mob Event Player:
        if (worldExt.serverWorldEventPlayer != null) {
            worldExt.serverWorldEventPlayer.onUpdate();
        }

        // Update Mob Event Players:
        if (!worldExt.serverMobEventPlayers.isEmpty()) {
            for (MobEventPlayerServer mobEventPlayerServer : worldExt.serverMobEventPlayers.values().toArray(new MobEventPlayerServer[worldExt.serverMobEventPlayers.size()])) {
                mobEventPlayerServer.onUpdate();
            }
        }
    }


    /**
     * Updates the client side mob event players if active in the player's current world.
     **/
    @SubscribeEvent
    public void onClientUpdate(TickEvent.ClientTickEvent event) {
        if (ClientManager.getInstance().getClientPlayer() == null)
            return;

        Level world = ClientManager.getInstance().getClientPlayer().getCommandSenderWorld();
        if (!world.isClientSide)
            return;
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        if (worldExt == null)
            return;

        // Update Mob Event Players:
        for (MobEventPlayerClient mobEventPlayerClient : worldExt.clientMobEventPlayers.values()) {
            mobEventPlayerClient.onUpdate();
        }

        // Update World Mob Event Player:
        if (worldExt.clientWorldEventPlayer != null) {
            worldExt.clientWorldEventPlayer.onUpdate();
        }
    }
}
