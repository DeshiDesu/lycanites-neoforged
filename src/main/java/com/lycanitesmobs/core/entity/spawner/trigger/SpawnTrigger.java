package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import com.lycanitesmobs.core.entity.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public abstract class SpawnTrigger {
    /** Spawn Triggers can respond to various events or ticks and will start a spawn. **/

    /**
     * The Spawner using this Trigger.
     **/
    public Spawner spawner;

    /**
     * How much this influences the Spawner's trigger count by, usually just 1. If 0 this Trigger will instead completely reset the count, if negative it will reduce the count.
     **/
    public int count = 1;

    /**
     * The Chance of triggering.
     **/
    public double chance = 1;

    /**
     * A list of Trigger specific Conditions that this Trigger will check.
     **/
    public List<SpawnCondition> conditions = new ArrayList<>();

    /**
     * Determines how many Trigger specific Conditions must be met. If 0 or less all are required.
     **/
    public int conditionsRequired = 0;

    /**
     * How long (in ticks) until this trigger can be started again, this is done per player.
     **/
    public int cooldown = 0;

    /**
     * Stores the age tick of each player when they last attempted to sleep.
     **/
    protected Map<Player, Long> playerUsedTicks = new HashMap<>();


    /**
     * Creates a Spawn Trigger from the provided JSON data.
     **/
    public static SpawnTrigger createFromJSON(JsonObject json, Spawner spawner) {
        String type = json.get("type").getAsString();
        SpawnTrigger spawnTrigger = null;

        if ("world".equalsIgnoreCase(type)) {
            spawnTrigger = new WorldSpawnTrigger(spawner);
        } else if ("player".equalsIgnoreCase(type) || "tick".equalsIgnoreCase(type)) { // Tick is now deprecated but should act like player for backwards compatibility.
            spawnTrigger = new PlayerSpawnTrigger(spawner);
        } else if ("entitySpawned".equalsIgnoreCase(type)) {
            spawnTrigger = new EntitySpawnedSpawnTrigger(spawner);
        } else if ("kill".equalsIgnoreCase(type)) {
            spawnTrigger = new KillSpawnTrigger(spawner);
        } else if ("chunk".equalsIgnoreCase(type)) {
            spawnTrigger = new ChunkSpawnTrigger(spawner);
        } else if ("block".equalsIgnoreCase(type)) {
            spawnTrigger = new BlockSpawnTrigger(spawner);
        } else if ("ore".equalsIgnoreCase(type)) {
            spawnTrigger = new OreBlockSpawnTrigger(spawner);
        } else if ("crop".equalsIgnoreCase(type)) {
            spawnTrigger = new CropBlockSpawnTrigger(spawner);
        } else if ("tree".equalsIgnoreCase(type)) {
            spawnTrigger = new TreeBlockSpawnTrigger(spawner);
        } else if ("sleep".equalsIgnoreCase(type)) {
            spawnTrigger = new SleepSpawnTrigger(spawner);
        } else if ("fishing".equalsIgnoreCase(type)) {
            spawnTrigger = new FishingSpawnTrigger(spawner);
        } else if ("explosion".equalsIgnoreCase(type)) {
            spawnTrigger = new ExplosionSpawnTrigger(spawner);
        } else if ("mobEvent".equalsIgnoreCase(type)) {
            spawnTrigger = new MobEventSpawnTrigger(spawner);
        } else if ("mix".equalsIgnoreCase(type)) {
            spawnTrigger = new MixBlockSpawnTrigger(spawner);
        }

        spawnTrigger.loadFromJSON(json);
        return spawnTrigger;
    }


    /**
     * Constructor
     **/
    public SpawnTrigger(Spawner spawner) {
        this.spawner = spawner;
    }

    /**
     * Loads this Spawn Trigger from the provided JSON data.
     **/
    public void loadFromJSON(JsonObject json) {
        if (json.has("count"))
            this.count = json.get("count").getAsInt();

        if (json.has("chance"))
            this.chance = json.get("chance").getAsDouble();

        if (json.has("conditionsRequired"))
            this.conditionsRequired = json.get("conditionsRequired").getAsInt();

        if (json.has("conditions")) {
            JsonArray jsonArray = json.get("conditions").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
                SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
                if (spawnCondition != null) this.conditions.add(spawnCondition);
            }
        }

        if (json.has("cooldown"))
            this.cooldown = json.get("cooldown").getAsInt();
    }


    /**
     * Triggers an actual spawn.
     **/
    public boolean trigger(Level world, Player player, BlockPos triggerPos, int level, int chain) {
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);

        // Cooldown:
        if (this.cooldown > 0 && playerExt != null) {
            long lastUsedTicks = 0;
            if (!this.playerUsedTicks.containsKey(player)) {
                this.playerUsedTicks.put(player, lastUsedTicks);
            } else {
                lastUsedTicks = this.playerUsedTicks.get(player);
            }
            if (playerExt.timePlayed < lastUsedTicks + this.cooldown) {
                return false;
            }
        }

        // Check Trigger Specific Conditions:
        if (!this.triggerConditionsMet(world, player, triggerPos)) {
            return false;
        }

        return this.spawner.trigger(world, player, this, triggerPos, level, this.count, chain);
    }

    /**
     * Checks all Conditions specific to this Trigger.
     **/
    public boolean triggerConditionsMet(Level world, Player player, BlockPos triggerPos) {
        if (this.conditions.size() == 0) {
            return true;
        }

        int conditionsMet = 0;
        int conditionsRequired = this.conditionsRequired > 0 ? this.conditionsRequired : this.conditions.size();
        for (SpawnCondition condition : this.conditions) {
            boolean met = condition.isMet(world, player, triggerPos);
            if (met) {
                if (++conditionsMet >= conditionsRequired) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Used to apply effects, etc any mobs that have spawned because of this trigger.
     **/
    public void applyToEntity(LivingEntity entityLiving) {
        return;
    }
}
