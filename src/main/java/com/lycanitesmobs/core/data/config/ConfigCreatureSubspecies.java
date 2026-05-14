package com.lycanitesmobs.core.data.config;

import com.lycanitesmobs.core.entity.util.CreatureStats;
import com.lycanitesmobs.core.data.info.Variant;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;

public class ConfigCreatureSubspecies {
	public static ConfigCreatureSubspecies INSTANCE;

	public final ForgeConfigSpec.ConfigValue<Integer> baseWeight;
	public Map<String,ForgeConfigSpec.ConfigValue<Integer>> commonWeights = new HashMap<>();
	public Map<String, Map<String, ForgeConfigSpec.ConfigValue<Double>>> variantMultipliers = new HashMap<>();

	public final ForgeConfigSpec.ConfigValue<Integer> uncommonDropScale;
	public final ForgeConfigSpec.ConfigValue<Integer> rareDropScale;

	public final ForgeConfigSpec.ConfigValue<Double> uncommonExperienceScale;
	public final ForgeConfigSpec.ConfigValue<Double> rareExperienceScale;

	public final ForgeConfigSpec.ConfigValue<Integer> uncommonSpawnDayMin;
	public final ForgeConfigSpec.ConfigValue<Integer> rareSpawnDayMin;

	public final ForgeConfigSpec.ConfigValue<Boolean> rareDespawning;

	public final ForgeConfigSpec.ConfigValue<Boolean> rareHealthBars;

	public ConfigCreatureSubspecies(ForgeConfigSpec.Builder builder) {
		builder.push("Creature Variants");
		builder.comment("Global creature variant settings.");

		baseWeight = builder.comment("The minimum base starting level of every mob. Cannot be less than 1.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.baseWeight")
				.define("baseWeight", 400);

		for(String variantName : Variant.SUBSPECIES_NAMES) {
			commonWeights.put(variantName, builder
					.comment("Subspecies weight for " + variantName + ".")
					.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.weights." + variantName)
					.define("weights." + variantName, Variant.COMMON_WEIGHTS.get(variantName)));
		}

		for(String variantName : Variant.SUBSPECIES_NAMES) {
			Map<String, ForgeConfigSpec.ConfigValue<Double>> statMultipliers = new HashMap<>();
			for (String statName : CreatureStats.STAT_NAMES) {
				double defaultValue = 1.0;
				if("uncommon".equals(variantName)) {
					if("health".equals(statName)) {
						defaultValue = 2;
					}
				}
				if("rare".equals(variantName)) {
					if("health".equals(statName)) {
						defaultValue = 20;
					}
					else if("attackSpeed".equals(statName)) {
						defaultValue = 2;
					}
					else if("rangedSpeed".equals(statName)) {
						defaultValue = 2;
					}
					else if("effect".equals(statName)) {
						defaultValue = 2;
					}
				}

				statMultipliers.put(statName, builder
						.comment("Stat multiplier for " + statName + " for " + variantName + " variant.")
						.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.multipliers." + variantName + "." + statName)
						.define("multipliers." + variantName + "." + statName, defaultValue));
			}
			this.variantMultipliers.put(variantName, statMultipliers);
		}

		uncommonDropScale = builder.comment("When a creature with the uncommon variant (Azure, Verdant, etc) dies, its item drops amount is multiplied by this value.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.uncommonDropScale")
				.define("uncommonDropScale", 2);
		rareDropScale = builder.comment("When a creature with the rare variant (Celestial, Lunar, etc) dies, its item drops amount is multiplied by this value.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareDropScale")
				.define("rareDropScale", 5);

		uncommonExperienceScale = builder.comment("When a creature with the uncommon variant (Azure, Verdant, etc) dies, its experience amount is multiplied by this value.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.uncommonExperienceScale")
				.define("uncommonExperienceScale", 2.0D);
		rareExperienceScale = builder.comment("When a creature with the rare variant (Celestial, Lunar, etc) dies, its experience amount is multiplied by this value.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareExperienceScale")
				.define("rareExperienceScale", 10.0D);

		uncommonSpawnDayMin = builder.comment("The minimum amount of days before uncommon variants start to spawn.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.uncommonSpawnDayMin")
				.define("uncommonSpawnDayMin", 0);
		rareSpawnDayMin = builder.comment("The minimum amount of days before rare variants start to spawn.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareSpawnDayMin")
				.define("rareSpawnDayMin", 0);

		rareDespawning = builder.comment("If set to true, rare variants will despawn naturally over time.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareDespawning")
				.define("rareDespawning", false);

		rareHealthBars = builder.comment("If set to true, rare variants such as the Lunar Grue or Celestial Geonach will display boss health bars.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareHealthBars")
				.define("rareHealthBars", false);

		builder.pop();
	}
}
