package com.lycanitesmobs.core.data.info;


import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.config.ConfigCreatureSubspecies;
import com.lycanitesmobs.core.entity.util.CreatureStats;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Variant {
    // ========== Global ==========
    /** The weight used by the default subspecies. **/
    public static int BASE_WEIGHT = 400;

    /** Common weights used by most subspecies. **/
    public static Map<String, Integer> COMMON_WEIGHTS = new HashMap<String, Integer>() {{
    	put("common", 100);
    	put("uncommon", 20);
    	put("rare", 2);
    	put("legendary", 1);
    }};

    public static String[] SUBSPECIES_NAMES = new String[] {"uncommon", "rare"};

	/** A static map containing all the global multipliers for each stat for each subspecies. **/
	public static Map<String, Double> STAT_MULTIPLIERS = new HashMap<>();

    /** The drop amount scale of uncommon subspecies. **/
    public static int UNCOMMON_DROP_SCALE = 2;

    /** The drop amount scale of rare subspecies. **/
    public static int RARE_DROP_SCALE = 5;

    /** The scale of experience for uncommon subspecies. **/
    public static double UNCOMMON_EXPERIENCE_SCALE = 2.0D;

    /** The scale of experience for uncommon subspecies. **/
    public static double RARE_EXPERIENCE_SCALE = 10.0D;

	/** The minimum amount of days before uncommon species start to spawn. **/
	public static int UNCOMMON_SPAWN_DAY_MIN = 0;

	/** The minimum amount of days before rare species start to spawn. **/
	public static int RARE_SPAWN_DAY_MIN = 0;

	/** If true, rare subspecies are able to despawn. **/
	public static boolean RARE_DESPAWNING = false;

    /** Whether rare subspecies should show boss health bars or not. **/
    public static boolean RARE_HEALTH_BARS = false;

	// ========== General ==========
	/** The index of this variant in MobInfo. Set by MobInfo when added. Should never be 0 as that is used by the default variant and will result in this variant being ignored. **/
	public int index;

	/** The size scale of this variant. **/
	public double scale = 1.0D;

	/** The color of this variant. **/
	public String color;

	/** The rarity of this variant. **/
	public String rarity = "uncommon";

	/** The weight of this variant, used when randomly determining the variant of a mob. **/
	public int weight = BASE_WEIGHT;


	/**
	 * Loads global Subspecies config values, etc.
	 */
	public static void loadGlobalSettings() {
        BASE_WEIGHT = ConfigCreatureSubspecies.INSTANCE.baseWeight.get();
		for(String subspeciesName : Variant.SUBSPECIES_NAMES) {
			COMMON_WEIGHTS.put(subspeciesName,  ConfigCreatureSubspecies.INSTANCE.commonWeights.get(subspeciesName).get());
		}

		STAT_MULTIPLIERS = new HashMap<>();
        for(String subspeciesName : SUBSPECIES_NAMES) {
            for(String statName : CreatureStats.STAT_NAMES) {
                STAT_MULTIPLIERS.put((subspeciesName + "-" + statName).toUpperCase(Locale.ENGLISH), ConfigCreatureSubspecies.INSTANCE.variantMultipliers.get(subspeciesName).get(statName).get());
            }
        }

        UNCOMMON_DROP_SCALE = ConfigCreatureSubspecies.INSTANCE.uncommonDropScale.get();
        RARE_DROP_SCALE = ConfigCreatureSubspecies.INSTANCE.rareDropScale.get();

        UNCOMMON_EXPERIENCE_SCALE = ConfigCreatureSubspecies.INSTANCE.uncommonExperienceScale.get();
        RARE_EXPERIENCE_SCALE = ConfigCreatureSubspecies.INSTANCE.rareExperienceScale.get();

		UNCOMMON_SPAWN_DAY_MIN = ConfigCreatureSubspecies.INSTANCE.uncommonSpawnDayMin.get();
		RARE_SPAWN_DAY_MIN = ConfigCreatureSubspecies.INSTANCE.rareSpawnDayMin.get();

		RARE_DESPAWNING = ConfigCreatureSubspecies.INSTANCE.rareDespawning.get();

		RARE_HEALTH_BARS = ConfigCreatureSubspecies.INSTANCE.rareHealthBars.get();
    }


    public static Variant createFromJSON(CreatureInfo creatureInfo, JsonObject json) {
		// Rarity:
		String rarity = "uncommon";
		if(json.has("rarity")) {
			rarity = json.get("rarity").getAsString().toLowerCase();
		}
		else if(json.has("type")) {
			rarity = json.get("type").getAsString().toLowerCase();
		}

		// Color:
		String color = null;
		if(json.has("color")) {
			color = json.get("color").getAsString().toLowerCase();
		}
		else if(json.has("name")) {
			color = json.get("name").getAsString().toLowerCase();
		}
		if(color == null) {
			LMHelperClass.logWarningMessage("[Creature] Skipping subspecies variant with no color defined. If you have updated the mod, clear your creature configs.");
			return null;
		}

		// Create Variant:
		Variant variant = new Variant(color, rarity, json.get("index").getAsInt());

		// Scale:
		if(json.has("scale")) {
			variant.scale = json.get("scale").getAsDouble();
		}

		return variant;
	}


	/**
	 * Constructor for creating a color/skin Subspecies based on a rarity.
	 * @param color The color of the Variant.
	 * @param rarity The rarity of the Variant ('common', 'uncommon' or 'rare').
	 * @param index The index of the Variant, 0 is reserved for no color variation.
	 */
	public Variant(String color, String rarity, int index) {
        this.color = color;
        this.rarity = rarity;
		this.index = index;
        this.weight = COMMON_WEIGHTS.get(rarity);
    }


	/**
	 * Gets the display name of this Subspecies.
	 * @return The Subspecies title.
	 */
	public MutableComponent getTitle() {
		return Component.translatable("subspecies." + this.color);
    }


	/**
	 * Gets the size scale of this subspecies.
	 * @return The size scale.
	 */
	public double getScale() {
		return this.scale;
	}


	/**
	 * Returns if this Subspecies is allowed to be used on the spawned entity.
	 * @return True if this Subspecies is allowed.
	 */
	public boolean canSpawn(LivingEntity entityLiving) {
		if(entityLiving != null) {
			Level world = entityLiving.getCommandSenderWorld();

			// Spawn Day Limit:
			int day = (int)Math.floor(world.getGameTime() / 23999D);
			int spawnDayMin = 0;
			if("uncommon".equalsIgnoreCase(this.rarity)) {
				spawnDayMin = UNCOMMON_SPAWN_DAY_MIN;
			}
			else if("rare".equalsIgnoreCase(this.rarity)) {
				spawnDayMin = RARE_SPAWN_DAY_MIN;
			}
			if(day < spawnDayMin) {
				return false;
			}
		}
		return true;
	}


	@Override
	public String toString() {
		return this.color + " - " + this.weight;
	}


	public static int getIndexFromOld(int oldIndex) {
		return oldIndex % 4;
	}
}
