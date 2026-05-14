package com.lycanitesmobs.core.event.mobevent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.client.event.mobevent.MobEventPlayerClient;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.event.MobEventListener;
import com.lycanitesmobs.core.manager.MobEventManager;
import com.lycanitesmobs.core.event.mobevent.effects.MobEventEffect;
import com.lycanitesmobs.core.event.mobevent.trigger.MobEventTrigger;
import com.lycanitesmobs.core.entity.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MobEvent {

    /**
     * A list of all Spawn Conditions which determine if the Triggers should be active or not.
     **/
    protected List<SpawnCondition> conditions = new ArrayList<>();

    /**
     * A list of all <Mob Event Triggers which listen to events and ticks and attempt to start an event, if conditions are met.
     **/
    protected List<MobEventTrigger> triggers = new ArrayList<>();

    /**
     * A list of all Mob Event Effects which perform various actions during an event.
     **/
    protected List<MobEventEffect> effects = new ArrayList<>();

    /**
     * The unique name of this mob event.
     **/
    public String name = "mobevent";

    /**
     * The title of this mob event, can be the same as other mob events, this is used for the event's printed name, textures and sounds.
     **/
    public String title = "mobevent";

    /**
     * Whether this event is active or not, this bypasses all checks and triggers, etc.
     **/
    protected boolean enabled = true;

    /**
     * The channel that this event should use. Can be: world (affects all players in the world), boss (for boss events) or player (the event is unique to the player, for area events).
     **/
    public String channel = "world";

    /**
     * How long (in ticks) this mob event will be in the spawning phase for. After this the event is considered over but spawned mobs may linger.
     **/
    public int duration = 60 * 20;

    /**
     * Determines how many Conditions must be met. If 0 or less all are required.
     **/
    protected int conditionsRequired = 0;

    /**
     * If set to true, this mob will ignore Group Limit checks.
     **/
    protected boolean interuptWorldEvents = false;

    /**
     * The minimum amount of levels to give a mob spawned by this event.
     **/
    protected int levelBoostMin = 0;

    /**
     * The maximum amount of levels to give a mob spawned by this event.
     **/
    protected int levelBoostMax = 9;


    /**
     * Loads this Spawner from the provided JSON data.
     **/
    public void loadFromJSON(JsonObject json) {
        this.name = json.get("name").getAsString();
        this.title = this.name;

        if (json.has("title"))
            this.title = json.get("title").getAsString();

        if (json.has("enabled"))
            this.enabled = json.get("enabled").getAsBoolean();

        if (json.has("channel"))
            this.channel = json.get("channel").getAsString();

        if (json.has("duration"))
            this.duration = json.get("duration").getAsInt();

        if (json.has("conditionsRequired"))
            this.conditionsRequired = json.get("conditionsRequired").getAsInt();

        if (json.has("interuptWorldEvents"))
            this.interuptWorldEvents = json.get("interuptWorldEvents").getAsBoolean();

        if (json.has("levelBoostMin"))
            this.levelBoostMin = json.get("levelBoostMin").getAsInt();

        if (json.has("levelBoostMax"))
            this.levelBoostMax = json.get("levelBoostMax").getAsInt();

        // Conditions:
        if (json.has("conditions")) {
            JsonArray jsonArray = json.get("conditions").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
                SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
                if (spawnCondition != null) this.conditions.add(spawnCondition);
            }
        }

        // Triggers:
        if (json.has("triggers")) {
            JsonArray jsonArray = json.get("triggers").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject triggerJson = jsonIterator.next().getAsJsonObject();
                MobEventTrigger mobEventTrigger = MobEventTrigger.createFromJSON(triggerJson, this);
                this.triggers.add(mobEventTrigger);
                if (this.enabled) {
                    MobEventListener.getInstance().addTrigger(mobEventTrigger);
                }
            }
        }

        // Effects:
        if (json.has("effects")) {
            JsonArray jsonArray = json.get("effects").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject effectJson = jsonIterator.next().getAsJsonObject();
                MobEventEffect mobEventEffect = MobEventEffect.createFromJSON(effectJson);
                this.effects.add(mobEventEffect);
            }
        }
    }


    /**
     * Remove this Spawner. This removes all Triggers from the Event Listener and does other cleanup.
     **/
    public void destroy() {
        for (MobEventTrigger mobEventTrigger : this.triggers) {
            MobEventListener.getInstance().removeTrigger(mobEventTrigger);
        }
        MobEventManager.getInstance().removeMobEvent(this);
    }


    /**
     * Returns the translated name of this event.
     **/
    public MutableComponent getTitle() {
        return Component.translatable("mobevent." + this.title + ".name");
    }


    /**
     * Returns whether this Mob Events is enabled or not.
     **/
    public boolean isEnabled() {
        return this.enabled;
    }


    /**
     * Returns true if this event is able to start on the provided extended world.
     *
     * @param world  The world to start the event in.
     * @param player The player that triggered the event, this can be null for world based events where all player based checks will fail.
     **/
    public boolean canStart(Level world, Player player) {
        if (world == null) {
            return false;
        }

        if (!this.enabled || !MobEventManager.getInstance().mobEventsEnabled) {
            LMHelperClass.logDebug("MobEvents", "Mob Events System Disabled");
            return false;
        }

        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        if (worldExt == null) {
            return false;
        }

        if ("world".equalsIgnoreCase(this.channel) && worldExt.getWorldEvent() != null && !this.interuptWorldEvents) {
            return false;
        }

        BlockPos position = null;
        if (player != null) {
            position = player.blockPosition();
        }

        // Global Conditions:
        if (!MobEventManager.getInstance().globalEventConditions.isEmpty()) {
            LMHelperClass.logDebug("MobEvents", "Global Conditions Required: " + MobEventManager.getInstance().globalEventConditions.size());
            for (SpawnCondition condition : MobEventManager.getInstance().globalEventConditions) {
                if (!condition.isMet(world, player, position)) {
                    LMHelperClass.logDebug("MobEvents", "Global Condition: " + condition + "Failed");
                    return false;
                }
            }
        }

        // Conditions:
        if (this.conditions.isEmpty()) {
            LMHelperClass.logDebug("MobEvents", "No Conditions");
            return true;
        }

        LMHelperClass.logDebug("MobEvents", "Conditions Required: " + (this.conditionsRequired > 0 ? this.conditionsRequired : "All"));
        int conditionsMet = 0;
        int conditionsRequired = this.conditionsRequired > 0 ? this.conditionsRequired : this.conditions.size();
        for (SpawnCondition condition : this.conditions) {
            boolean met = condition.isMet(world, player, position);
            LMHelperClass.logDebug("MobEvents", "Condition: " + condition + " " + (met ? "Passed" : "Failed"));
            if (met) {
                if (++conditionsMet >= conditionsRequired) {
                    LMHelperClass.logDebug("MobEvents", "Sufficient Conditions Met");
                    return true;
                }
            }
        }

        LMHelperClass.logDebug("MobEvents", "Insufficient Conditions Met: " + conditionsMet + "/" + conditionsRequired);
        return false;
    }

    /**
     * Triggers this Mob Event where it will start. This does not check conditions.
     *
     * @param world   The world the event is taking place in.
     * @param player  The player that triggered the event, this can be null for world based events where all player based checks will fail.
     * @param pos     Where the event origin will be. This is used by effects for generating structures as well as Mob Event Spawn Triggers and other things.
     * @param level   The level of the event.
     * @param variant The variant to spawn, if less than 0 a random variant is picked as normal.
     * @return
     */
    public boolean trigger(Level world, Player player, BlockPos pos, int level, int variant) {
        LMHelperClass.logDebug("MobEvents", "~O==================== Mob Event Triggered: " + this.name + " ====================O~");
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        if (worldExt == null) {
            return false;
        }

        if ("world".equalsIgnoreCase(this.channel)) {
            worldExt.startWorldEvent(this);
        } else if (pos != null) {
            worldExt.startMobEvent(this, player, pos, level, variant);
        }
        return true;
    }

    /**
     * Called when this Mob Event updates.
     *
     * @param world   The world the event is taking place in.
     * @param player  The player that triggered the event, this can be null for world based events where all player based checks will fail.
     * @param pos     Where the event origin will be. This is used by effects for generating structures as well as Mob Event Spawn Triggers and other things.
     * @param level   The level of the event.
     * @param ticks   How many ticks the event has been active for.
     * @param variant The variant to spawn, if less than 0 a random variant is picked as normal.
     */
    public void onUpdate(Level world, Player player, BlockPos pos, int level, int ticks, int variant) {
        for (MobEventEffect mobEventEffect : this.effects) {
            mobEventEffect.onUpdate(world, player, pos, level, ticks, variant);
        }
    }

    /**
     * Called whenever a mob is spawned by this mob event. The Spawner must have "mobEventSpawner" set to true for this to be called by it.
     *
     * @param entity  The spawned entity.
     * @param world   The world the event is taking place in.
     * @param player  The player that triggered the event, this can be null for world based events where all player based checks will fail.
     * @param pos     Where the event origin will be. This is used by effects for generating structures as well as Mob Event Spawn Triggers and other things.
     * @param level   The level of the event.
     * @param ticks   How many ticks the event has been active for.
     * @param variant The variant to spawn, if less than 0 a random variant is picked as normal.
     */
    public void onSpawn(LivingEntity entity, Level world, Player player, BlockPos pos, int level, int ticks, int variant) {
        for (MobEventEffect mobEventEffect : this.effects) {
            mobEventEffect.onSpawn(entity, world, player, pos, level, ticks);
        }
        if (entity instanceof BaseCreatureEntity) {
            BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
            entityCreature.onFirstSpawn();
            int levelBoost = this.levelBoostMin;
            if (this.levelBoostMax > this.levelBoostMin) {
                levelBoost += entityCreature.getRandom().nextInt(this.levelBoostMax - this.levelBoostMin + 1);
            }
            entityCreature.addLevel(levelBoost);

            if (!entityCreature.isTemporary && !"boss".equalsIgnoreCase(this.channel)) {
                entityCreature.setTemporary(MobEventManager.getInstance().defaultMobDuration);
            }

            if (variant >= 0) {
                entityCreature.applyVariant(variant);
            }
        }
    }


    /**
     * Creates and returns a Server Side event player instance for this event.
     *
     * @param world The world the event is taking place in.
     * @return
     */
    public MobEventPlayerServer getServerEventPlayer(Level world) {
        return new MobEventPlayerServer(this, world);
    }

    /**
     * Creates and returns a Client Side event player instance for this event.
     *
     * @param world The world the event is taking place in.
     * @return
     */
    public MobEventPlayerClient getClientEventPlayer(Level world) {
        return new MobEventPlayerClient(this, world);
    }
}
