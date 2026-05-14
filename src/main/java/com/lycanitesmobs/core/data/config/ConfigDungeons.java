package com.lycanitesmobs.core.data.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigDungeons {
	public static ConfigDungeons INSTANCE;

	public final ForgeConfigSpec.ConfigValue<Boolean> dungeonsEnabled;
	public final ForgeConfigSpec.ConfigValue<Integer> dungeonDistance;
	public final ForgeConfigSpec.ConfigValue<Double> towerChance;

	public ConfigDungeons(ForgeConfigSpec.Builder builder) {
		builder.push("Dungeons");
		builder.comment("Settings for Dungeon generation.");

		this.dungeonsEnabled = builder
				.comment("Set to false to disable all Dungeons.")
				.translation(CoreConfig.CONFIG_PREFIX + "dungeons.enabled")
				.define("enabled", true);

		this.dungeonDistance = builder
				.comment("The average distance in chunks that dungeons are spaced apart from each other.")
				.translation(CoreConfig.CONFIG_PREFIX + "dungeons.distance")
				.define("distance", 40);

		this.towerChance = builder
				.comment("The chance of a dungeon generating a tower on top (0.25 by default).")
				.translation(CoreConfig.CONFIG_PREFIX + "dungeons.tower.chance")
				.define("tower.chance", 0.25D);

		builder.pop();
	}
}
