package com.lycanitesmobs.core.capabilities.level;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonBuildPlan;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import com.lycanitesmobs.core.capabilities.util.BossEntry;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.manager.MobEventManager;
import com.lycanitesmobs.client.event.mobevent.MobEventPlayerClient;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.network.message.MessageMobEvent;
import com.lycanitesmobs.core.network.message.MessageWorldEvent;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class ExtendedWorld extends SavedData implements INBTSerializable<CompoundTag> {
    public static String EXT_PROP_NAME = "LycanitesMobs";
    public static Map<Level, ExtendedWorld> loadedExtWorlds = new HashMap<>();

    /**
     * The world INSTANCE to work with.
     **/
    public Level world;
    public boolean initialized = false;
    public long lastSpawnerTime = 0;
    public long lastEventScheduleTime = 0;
    public long lastEventUpdateTime = 0;

    // Mob Events World Config:
    public boolean useTotalWorldTime = true;

    // Mob Events:
    public Map<String, MobEventPlayerServer> serverMobEventPlayers = new HashMap<>();
    public Map<String, MobEventPlayerClient> clientMobEventPlayers = new HashMap<>();
    public MobEventPlayerServer serverWorldEventPlayer = null;
    public MobEventPlayerClient clientWorldEventPlayer = null;
    private long worldEventStartTargetTime = 0;
    private long worldEventLastStartedTime = 0;
    private String worldEventName = "";
    private int worldEventCount = -1;

    // Entities:
    public Map<UUID, BossEntry> bosses = new HashMap<>();

    // Dungeons:
    public Map<UUID, DungeonInstance> dungeons = new HashMap<>();


    // ==================================================
    //                   Get for World
    // ==================================================
    public static ExtendedWorld getForWorld(Level world) {
        if (world == null) {
            //LycanitesMobs.logWarning("", "Tried to access an ExtendedWorld from a null World.");
            return null;
        }

        ExtendedWorld worldExt;

        // Already Loaded:
        if (loadedExtWorlds.containsKey(world)) {
            worldExt = loadedExtWorlds.get(world);
            return worldExt;
        }

        // Server Side:
        if (world instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) world;
            world.increaseMaxEntityRadius(25);
            ExtendedWorld worldSavedData = serverWorld.getDataStorage().get(tag -> new ExtendedWorld(), EXT_PROP_NAME);
            if (worldSavedData != null) {
                worldExt = worldSavedData;
                worldExt.world = world;
                worldExt.init();
            } else {
                worldExt = new ExtendedWorld(world);
                serverWorld.getDataStorage().set(EXT_PROP_NAME, worldExt);
            }
        }

        // Client Side: (Only used as a per world object instance for running events, etc.)
        else {
            worldExt = new ExtendedWorld(world);
            worldExt.init();
        }

        loadedExtWorlds.put(world, worldExt);
        return worldExt;
    }


    // ==================================================
    //                     Constructor
    // ==================================================
    public ExtendedWorld() {
        super();
    }

    public ExtendedWorld(Level world) {
        super();
        this.world = world;
    }


    // ==================================================
    //                        Init
    // ==================================================
    public void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;

        // Initial Tick Times:
        this.lastSpawnerTime = this.world.getGameTime() - 1;
        this.lastEventScheduleTime = this.world.getGameTime() - 1;
        this.lastEventUpdateTime = this.world.getGameTime() - 1;

        // Start Saved Events:
        if (!this.world.isClientSide && !"".equals(this.getWorldEventName()) && this.serverWorldEventPlayer == null) {
            long savedLastStartedTime = this.getWorldEventLastStartedTime();
            this.startMobEvent(this.getWorldEventName(), null, new BlockPos(0, 0, 0), 1, -1); // TODO Swap to read/write from NBT on Server Players.
            if (this.serverWorldEventPlayer != null) {
                this.serverWorldEventPlayer.changeStartedWorldTime(savedLastStartedTime);
            }
        }
    }


    // ==================================================
    //                    Get Properties
    // ==================================================
    public long getWorldEventStartTargetTime() {
        return this.worldEventStartTargetTime;
    }

    public long getWorldEventLastStartedTime() {
        return this.worldEventLastStartedTime;
    }

    public String getWorldEventName() {
        return this.worldEventName;
    }

    public MobEvent getWorldEvent() {
        if (this.getWorldEventName() == null || "".equals(this.getWorldEventName())) {
            return null;
        }
        return MobEventManager.getInstance().getMobEvent(this.getWorldEventName());
    }

    public int getWorldEventCount() {
        return this.worldEventCount;
    }


    // ==================================================
    //                    Set Properties
    // ==================================================
    public void setWorldEventStartTargetTime(long setLong) {
        if (this.worldEventStartTargetTime != setLong)
            this.setDirty();
        this.worldEventStartTargetTime = setLong;
        if (setLong > 0)
            LMHelperClass.logDebug("MobEvents", "Next random mob will start after " + ((this.worldEventStartTargetTime - this.world.getGameTime()) / 20) + "secs.");
    }

    public void setWorldEventLastStartedTime(long setLong) {
        if (this.worldEventLastStartedTime != setLong)
            this.setDirty();
        this.worldEventLastStartedTime = setLong;
    }

    public void setWorldEventName(String setString) {
        if (!this.worldEventName.equals(setString))
            this.setDirty();
        this.worldEventName = setString;
    }

    public void increaseMobEventCount() {
        this.worldEventCount++;
    }


    // ==================================================
    //                Random Event Delay
    // ==================================================

    /**
     * Gets a random time until the next random event will start.
     **/
    public int getRandomEventDelay(RandomSource random) {
        int min = Math.max(200, MobEventManager.getInstance().minTicksUntilEvent);
        int max = Math.max(200, MobEventManager.getInstance().maxTicksUntilEvent);
        if (max <= min)
            return min;

        return min + random.nextInt(max - min);
    }


    // ==================================================
    //                     World Event
    // ==================================================

    /**
     * Starts the provided Mob Event on the provided world.
     *
     **/
    public void startWorldEvent(MobEvent mobEvent) {
        if (mobEvent == null) {
            LMHelperClass.logWarning("", "Tried to start a null world event, stopping any event instead.");
            this.stopWorldEvent();
            return;
        }

        // Server Side:
        if (!this.world.isClientSide) {
            boolean extended = false;
            if (this.serverWorldEventPlayer != null) {
                extended = this.serverWorldEventPlayer.mobEvent == mobEvent;
            }
            if (!extended) {
                this.serverWorldEventPlayer = mobEvent.getServerEventPlayer(this.world);
            }
            this.serverWorldEventPlayer.extended = extended;

            this.setWorldEventName(mobEvent.name);
            this.increaseMobEventCount();
            this.setWorldEventStartTargetTime(0);
            this.setWorldEventLastStartedTime(this.world.getGameTime());
            this.serverWorldEventPlayer.onStart();
            this.updateAllClientsEvents();
        }

        // Client Side:
        if (this.world.isClientSide) {
            boolean extended = false;
            if (this.clientWorldEventPlayer != null) {
                extended = this.clientWorldEventPlayer.mobEvent == mobEvent;
            }
            if (!extended) {
                this.clientWorldEventPlayer = mobEvent.getClientEventPlayer(this.world);
            }
            this.clientWorldEventPlayer.extended = extended;
            if (ClientManager.getInstance().getClientPlayer() != null) {
                this.clientWorldEventPlayer.onStart(ClientManager.getInstance().getClientPlayer());
            }
        }
    }

    /**
     * Stops the World Event.
     **/
    public void stopWorldEvent() {
        // Server Side:
        if (this.serverWorldEventPlayer != null) {
            this.serverWorldEventPlayer.onFinish();
            this.setWorldEventName("");
            this.serverWorldEventPlayer = null;
            this.updateAllClientsEvents();
        }

        // Client Side:
        if (this.clientWorldEventPlayer != null) {
            if (ClientManager.getInstance().getClientPlayer() != null)
                this.clientWorldEventPlayer.onFinish(ClientManager.getInstance().getClientPlayer());
            this.clientWorldEventPlayer = null;
        }
    }


    // ==================================================
    //                     Mob Events
    // ==================================================

    /**
     * Starts a provided Mob Event (provided by INSTANCE) on the provided world.
     *
     **/
    public void startMobEvent(MobEvent mobEvent, Player player, BlockPos pos, int level, int variant) {
        if (mobEvent == null) {
            LMHelperClass.logWarning("", "Tried to start a null mob event.");
            return;
        }

        // Server Side:
        if (!this.world.isClientSide) {
            MobEventPlayerServer mobEventPlayerServer = mobEvent.getServerEventPlayer(this.world);
            this.serverMobEventPlayers.put(mobEvent.name, mobEventPlayerServer);
            mobEventPlayerServer.player = player;
            mobEventPlayerServer.origin = pos;
            mobEventPlayerServer.level = level;
            mobEventPlayerServer.variant = variant;
            mobEventPlayerServer.onStart();
            this.updateAllClientsEvents();
        }

        // Client Side:
        if (this.world.isClientSide) {
            MobEventPlayerClient mobEventPlayerClient;
            if (this.clientMobEventPlayers.containsKey(mobEvent.name)) {
                mobEventPlayerClient = this.clientMobEventPlayers.get(mobEvent.name);
            } else {
                mobEventPlayerClient = mobEvent.getClientEventPlayer(this.world);
                this.clientMobEventPlayers.put(mobEvent.name, mobEventPlayerClient);
            }
            if (ClientManager.getInstance().getClientPlayer() != null) {
                mobEventPlayerClient.onStart(ClientManager.getInstance().getClientPlayer());
            }
        }
    }

    /**
     * Starts a provided Mob Event (provided by name) on the provided world.
     **/
    public MobEvent startMobEvent(String mobEventName, Player player, BlockPos pos, int level, int variant) {
        MobEvent mobEvent;
        if (MobEventManager.getInstance().mobEvents.containsKey(mobEventName)) {
            mobEvent = MobEventManager.getInstance().mobEvents.get(mobEventName);
            if (!mobEvent.isEnabled()) {
                LMHelperClass.logWarning("", "Tried to start a mob event that was disabled with the name: '" + mobEventName + "' on " + (this.world.isClientSide ? "Client" : "Server"));
                return null;
            }
        } else {
            LMHelperClass.logWarning("", "Tried to start a mob event with the invalid name: '" + mobEventName + "' on " + (this.world.isClientSide ? "Client" : "Server"));
            return null;
        }

        mobEvent.trigger(world, player, pos, level, variant);
        return mobEvent;
    }

    /**
     * Stops a Mob Event.
     *
     **/
    public void stopMobEvent(String mobEventName) {
        // Server Side:
        if (!this.world.isClientSide) {
            if (this.serverMobEventPlayers.containsKey(mobEventName)) {
                this.serverMobEventPlayers.get(mobEventName).onFinish();
                this.serverMobEventPlayers.remove(mobEventName);
                this.updateAllClientsEvents();
            }
        }

        // Client Side:
        if (this.world.isClientSide) {
            if (this.clientMobEventPlayers.containsKey(mobEventName)) {
                if (ClientManager.getInstance().getClientPlayer() != null) {
                    this.clientMobEventPlayers.get(mobEventName).onFinish(ClientManager.getInstance().getClientPlayer());
                }
                this.clientMobEventPlayers.remove(mobEventName);
            }
        }
    }


    // ==================================================
    //            Get Active Mob Event Players
    // ==================================================

    /**
     * Returns a Mob Event Server Player if an event by the provided event name is currently active, otherwise null.
     **/
    public MobEventPlayerServer getMobEventPlayerServer(String mobEventName) {
        if (mobEventName == null || "".equals(mobEventName)) {
            return null;
        }

        if (mobEventName.equals(this.getWorldEventName())) {
            return this.serverWorldEventPlayer;
        }

        if (this.serverMobEventPlayers.containsKey(mobEventName)) {
            return this.serverMobEventPlayers.get(mobEventName);
        }

        return null;
    }

    /**
     * Returns a Mob Event Client Player if an event by the provided event name is currently active, otherwise null.
     **/
    public MobEventPlayerClient getMobEventPlayerClient(String mobEventName) {
        if (mobEventName == null || "".equals(mobEventName)) {
            return null;
        }

        if (mobEventName.equals(this.worldEventName)) {
            return this.clientWorldEventPlayer;
        }

        if (this.clientMobEventPlayers.containsKey(mobEventName)) {
            return this.clientMobEventPlayers.get(mobEventName);
        }

        return null;
    }


    // ==================================================
    //                     Entities
    // ==================================================

    /**
     * Called by bosses to let this world know that they are active, this will add them to the boss list if they are not already in it.
     **/
    public void bossUpdate(Entity entity) {
        if (entity.isAlive()) {
            if (!this.bosses.containsKey(entity.getUUID())) {
                this.bosses.put(entity.getUUID(), new BossEntry());
            }
            this.bosses.get(entity.getUUID()).update(entity);
        }
    }

    /**
     * Overrides the boss nearby range for the provided entity.
     **/
    public void overrideBossRange(Entity entity, int rangeOverride) {
        if (this.bosses.containsKey(entity.getUUID())) {
            this.bosses.get(entity.getUUID()).nearbyRange = rangeOverride;
        }
    }

    /**
     * Called by bosses to let this world know that they are being removed.
     **/
    public void bossRemoved(Entity entity) {
        this.bosses.remove(entity.getUUID());
    }

    /**
     * Returns true if a boss is nearby.
     *
     * @param pos The position to search around.
     * @return True if a boss is present.
     */
    public boolean isBossNearby(Vec3 pos) {
        for (BossEntry bossEntry : this.bosses.values()) {
            if (bossEntry == null || bossEntry.entity == null || !bossEntry.entity.isAlive()) {
                continue;
            }
            if (bossEntry.entity.distanceToSqr(pos) <= bossEntry.nearbyRange * bossEntry.nearbyRange) {
                return true;
            }
        }
        return false;
    }


    // ==================================================
    //                  Update Clients
    // ==================================================

    /**
     * Sends a packet to all clients updating their events for the provided world.
     **/
    public void updateAllClientsEvents() {
        BlockPos pos = this.serverWorldEventPlayer != null ? this.serverWorldEventPlayer.origin : new BlockPos(0, 0, 0);
        int level = this.serverWorldEventPlayer != null ? this.serverWorldEventPlayer.level : 0;
        int subspecies = this.serverWorldEventPlayer != null ? this.serverWorldEventPlayer.variant : -1;
        MessageWorldEvent message = new MessageWorldEvent(this.getWorldEventName(), pos, level, subspecies);
        LycanitesMobs.PACKET_MANAGER.sendToWorld(message, this.world);
        for (MobEventPlayerServer mobEventPlayerServer : this.serverMobEventPlayers.values()) {
            MessageMobEvent messageMobEvent = new MessageMobEvent(mobEventPlayerServer.mobEvent != null ? mobEventPlayerServer.mobEvent.name : "", mobEventPlayerServer.origin, mobEventPlayerServer.level, mobEventPlayerServer.variant);
            LycanitesMobs.PACKET_MANAGER.sendToWorld(messageMobEvent, this.world);
        }
    }


    // ==================================================
    //                     Dungeons
    // ==================================================

    /**
     * Adds a new Dungeon Instance to this world where it can be found for generation, etc. Gives a new UUID.
     *
     * @param dungeonInstance The Dungeon Instance to add.
     * @param uuid            The UUID to give to the new Dungeon Instance.
     */
    public void addDungeonInstance(DungeonInstance dungeonInstance, UUID uuid) {
        dungeonInstance.uuid = uuid;
        this.dungeons.put(dungeonInstance.uuid, dungeonInstance);
        this.setDirty();
    }


    /**
     * Returns all Dungeon Instances loaded for the provided world within range of the chunk position.
     *
     * @param chunkPos The chunk position to search around.
     * @param range    The range from the chunk position.
     * @return A list of Dungeon Instances found.
     */
    public List<DungeonInstance> getNearbyDungeonInstances(ChunkPos chunkPos, int range) {
        List<DungeonInstance> nearbyDungeons = new ArrayList<>();
        for (DungeonInstance dungeonInstance : this.dungeons.values()) {
            if (dungeonInstance.world == null) {
                dungeonInstance.init(this.world);
            }
            if (dungeonInstance.isChunkPosWithin(chunkPos, range)) {
                nearbyDungeons.add(dungeonInstance);
            }
        }
        return nearbyDungeons;
    }


    // ==================================================
    //                    Read From NBT
    // ==================================================
    public void load(CompoundTag nbtTagCompound) {
        // Events:
        if (nbtTagCompound.contains("WorldEventStartTargetTime")) {
            this.worldEventStartTargetTime = nbtTagCompound.getInt("WorldEventStartTargetTime");
        }
        if (nbtTagCompound.contains("WorldEventLastStartedTime")) {
            this.worldEventLastStartedTime = nbtTagCompound.getInt("WorldEventLastStartedTime");
        }
        if (nbtTagCompound.contains("WorldEventName")) {
            this.worldEventName = nbtTagCompound.getString("WorldEventName");
        }
        if (nbtTagCompound.contains("WorldEventCount")) {
            this.worldEventCount = nbtTagCompound.getInt("WorldEventCount");
        }
        // TODO Load all active mob events, not just the world event.

        // Dungeons:
        if (nbtTagCompound.contains("Dungeons")) {
            ListTag nbtDungeonList = nbtTagCompound.getList("Dungeons", 10);
            for (int i = 0; i < nbtDungeonList.size(); i++) {
                try {
                    CompoundTag dungeonNBT = nbtDungeonList.getCompound(i);
                    DungeonInstance dungeonInstance = new DungeonInstance();
                    dungeonInstance.readFromNBT(dungeonNBT);
                    if (dungeonInstance.uuid != null && !this.dungeons.containsKey(dungeonInstance.uuid)) {
                        this.dungeons.put(dungeonInstance.uuid, dungeonInstance);
                    }
                } catch (Exception e) {
                    LMHelperClass.logWarning("Dungeon", "An exception occurred when loading a dungeon from NBT.");
                }
            }
        }
    }


    // ==================================================
    //                    Write To NBT
    // ==================================================
    @Override
    public CompoundTag save(CompoundTag nbtTagCompound) {
        // Events:
        nbtTagCompound.putLong("WorldEventStartTargetTime", this.worldEventStartTargetTime);
        nbtTagCompound.putLong("WorldEventLastStartedTime", this.worldEventLastStartedTime);
        nbtTagCompound.putString("WorldEventName", this.worldEventName);
        nbtTagCompound.putInt("WorldEventCount", this.worldEventCount);
        // TODO Save all active mob events, not just the world event.

        // Dungeons:
        ListTag nbtDungeonList = new ListTag();
        for (DungeonInstance dungeonInstance : this.dungeons.values()) {
            CompoundTag dungeonNBT = new CompoundTag();
            dungeonNBT = dungeonInstance.writeToNBT(dungeonNBT);
            if (dungeonNBT != null) {
                nbtDungeonList.add(dungeonNBT);
            }
        }
        nbtTagCompound.put("Dungeons", nbtDungeonList);

        return nbtTagCompound;
    }

    @Override
    public void deserializeNBT(CompoundTag p_deserializeNBT_1_) {
        this.load(p_deserializeNBT_1_);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.save(new CompoundTag());
    }

    public long getWorldSeed() {
        return this.world.getServer().getWorldData().worldGenOptions().seed();
    }

    public void buildDungeonInLoadedChunks(DungeonInstance instance) {
        if (!(this.world instanceof ServerLevel serverLevel)) return;
        if (instance.chunkMin == null || instance.chunkMax == null) return;

        LMHelperClass.logDebug(
                "DungeonDebug",
                "buildDungeonInLoadedChunks: instance=" + instance.uuid
                        + " chunkMin=" + instance.chunkMin
                        + " chunkMax=" + instance.chunkMax
        );

        int total = 0;
        for (int cx = instance.chunkMin.x; cx <= instance.chunkMax.x; cx++) {
            for (int cz = instance.chunkMin.z; cz <= instance.chunkMax.z; cz++) {
                total++;
            }
        }
        LMHelperClass.logDebug("DungeonDebug", "buildDungeonInLoadedChunks: totalChunksInRect=" + total);

        int processed = 0;
        for (int cx = instance.chunkMin.x; cx <= instance.chunkMax.x; cx++) {
            for (int cz = instance.chunkMin.z; cz <= instance.chunkMax.z; cz++) {
                if (!serverLevel.getChunkSource().hasChunk(cx, cz)) continue;

                ChunkPos chunkPos = new ChunkPos(cx, cz);
                RandomSource random = RandomSource.create(chunkPos.toLong() ^ this.getWorldSeed());

                LMHelperClass.logDebug("DungeonDebug", "buildDungeonInLoadedChunks: building " + chunkPos + " (" + processed + "/" + total + ")");
                instance.buildChunk(serverLevel, serverLevel, chunkPos, random);
                processed++;
            }
        }

        LMHelperClass.logDebug("DungeonDebug", "buildDungeonInLoadedChunks: done, processed=" + processed + " of " + total);
    }


    public void debugDryRunDungeonPlacement(DungeonInstance instance) {
        if (!(this.world instanceof ServerLevel serverLevel)) return;
        if (instance.chunkMin == null || instance.chunkMax == null) {
            LMHelperClass.logDebug("DungeonDebug", "debugDryRunDungeonPlacement: no chunk bounds for instance " + instance.uuid + " " + instance);
            return;
        }

        LMHelperClass.logDebug(
                "DungeonDebug",
                "debugDryRunDungeonPlacement: instance=" + instance.uuid +
                        " chunkMin=[" + instance.chunkMin.x + "," + instance.chunkMin.z + "]" +
                        " chunkMax=[" + instance.chunkMax.x + "," + instance.chunkMax.z + "]"
        );

        for (int cx = instance.chunkMin.x; cx <= instance.chunkMax.x; cx++) {
            for (int cz = instance.chunkMin.z; cz <= instance.chunkMax.z; cz++) {
                boolean loaded = serverLevel.getChunkSource().hasChunk(cx, cz);
                LMHelperClass.logDebug(
                        "DungeonDebug",
                        "debugDryRunDungeonPlacement: instance=" + instance.uuid +
                                " chunk=[" + cx + "," + cz + "] loaded=" + loaded
                );
                if (!loaded) continue;

                ChunkPos chunkPos = new ChunkPos(cx, cz);
                RandomSource random = RandomSource.create(chunkPos.toLong() ^ this.getWorldSeed());
                instance.debugBuildChunk(serverLevel, serverLevel, chunkPos, random);
            }
        }
    }

    public void debugDungeonInLoadedChunks(DungeonInstance instance) {
        if (!(this.world instanceof ServerLevel serverLevel)) return;
        if (instance.chunkMin == null || instance.chunkMax == null) {
            LMHelperClass.logDebug("DungeonDebug", "debugDungeonInLoadedChunks: no chunk bounds for instance " + instance.uuid + " " + instance);
            return;
        }

        for (int cx = instance.chunkMin.x; cx <= instance.chunkMax.x; cx++) {
            for (int cz = instance.chunkMin.z; cz <= instance.chunkMax.z; cz++) {
                boolean hasChunk = serverLevel.getChunkSource().hasChunk(cx, cz);
                LMHelperClass.logDebug(
                        "DungeonDebug",
                        "debugDungeonInLoadedChunks: instance=" + instance.uuid +
                                " chunk=[" + cx + "," + cz + "] loaded=" + hasChunk
                );
                if (!hasChunk) continue;

                ChunkPos chunkPos = new ChunkPos(cx, cz);
                RandomSource random = RandomSource.create(chunkPos.toLong() ^ this.getWorldSeed());
                instance.debugBuildChunk(serverLevel, serverLevel, chunkPos, random);
            }
        }
    }


}
