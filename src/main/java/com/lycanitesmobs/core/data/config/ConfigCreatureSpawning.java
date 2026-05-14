package com.lycanitesmobs.core.data.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigCreatureSpawning {
	public static ConfigCreatureSpawning INSTANCE;

	public final ForgeConfigSpec.ConfigValue<Integer> typeSpawnLimit;
	public final ForgeConfigSpec.ConfigValue<Double> spawnLimitRange;
	public final ForgeConfigSpec.ConfigValue<Boolean> disableAllSpawning;
	public final ForgeConfigSpec.ConfigValue<Boolean> enforceBlockCost;
	public final ForgeConfigSpec.ConfigValue<Double> spawnWeightScale;
	public final ForgeConfigSpec.ConfigValue<Boolean> useSurfaceLightLevel;
	public final ForgeConfigSpec.ConfigValue<Boolean> ignoreWorldGenSpawning;
	public final ForgeConfigSpec.ConfigValue<Boolean> controlVanillaSpawns;

	public final ForgeConfigSpec.ConfigValue<String> globalDimensionList;
	public final ForgeConfigSpec.ConfigValue<Boolean> globalDimensionWhitelist;

	public final ForgeConfigSpec.ConfigValue<Boolean> disableDungeonSpawners;
	public final ForgeConfigSpec.ConfigValue<Double> dungeonSpawnerWeightScale;

	public ConfigCreatureSpawning(ForgeConfigSpec.Builder builder) {
		builder.push("Global Spawning");
		builder.comment("These settings are used by everything. It is recommended to leave them as they are however low end machines might benefit from a few tweaks here");

		typeSpawnLimit = builder.comment("The limit of how many mobs of the same type (peaceful or not peaceful) can spawn within the limit search range. For individual creature species limits, see the creature json configs.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.typeSpawnLimit")
				.define("typeSpawnLimit", 64);
		spawnLimitRange = builder.comment("When spawned from a vanilla spawner, this is how far a mob should search from in blocks when checking how many of its kind have already spawned. Custom Spawners have it defined in their json file instead.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.spawnLimitRange")
				.define("spawnLimitRange", 32D);
		disableAllSpawning = builder.comment("If true, all mobs from this mod will not spawn at all.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.disableAllSpawning")
				.define("disableAllSpawning", false);
		enforceBlockCost = builder.comment("If true, mobs will double check if their required blocks are nearby, such as Cinders needing so many blocks of fire.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.enforceBlockCost")
				.define("enforceBlockCost", true);
		spawnWeightScale = builder.comment("Scales the spawn weights of all mobs from this mod. For example, you can use this to quickly half the spawn rates of mobs from this mod compared to vanilla/other mod mobs by setting it to 0.5.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.spawnWeightScale")
				.define("spawnWeightScale", 1.0D);
		useSurfaceLightLevel = builder.comment("If true, when water mobs spawn, instead of checking the light level of the block the mob is spawning at, the light level of the surface (if possible) is checked. This stops mobs like Jengus from spawning at the bottom of deep rivers during the day, set to false for the old way.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.useSurfaceLightLevel")
				.define("useSurfaceLightLevel", true);
		ignoreWorldGenSpawning = builder.comment("If true, when new world chunks are generated, no mobs from this mod will pre-spawn (mobs will still attempt to spawn randomly afterwards). Set this to true if you are removing mobs from vanilla dimensions as the vanilla WorldGen spawning ignores mob spawn conditions.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.ignoreWorldGenSpawning")
				.define("ignoreWorldGenSpawning", false);
		controlVanillaSpawns = builder.comment("If true, some vanilla spawns in various biomes will be removed, note that vanilla mobs should still be easy to find, only they will be more biome specific.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.controlVanillaSpawns")
				.define("controlVanillaSpawns", false);

		globalDimensionList = builder.comment("A global comma separated list of dimension ids that overrides every other spawn setting in both the configs and json spawners. Use this to quickly stop all mobs from spawning in certain dimensions, etc.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.globalDimensionList")
				.define("globalDimensionList", "");
		globalDimensionWhitelist = builder.comment("If set to true the global dimension list acts as a whitelist, otherwise it is a blacklist.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.globalDimensionWhitelist")
				.define("globalDimensionWhitelist", false);

		disableDungeonSpawners = builder.comment("If true, newly generated dungeons wont create spawners with mobs from this mod.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.disableDungeonSpawners")
				.define("disableDungeonSpawners", false);
		dungeonSpawnerWeightScale = builder.comment("Scales the weight of dungeons using spawners from this mod. For example, you can half the chances all dungeons having spawners with mobs from this mod in them by setting this to 0.5.")
				.translation(CoreConfig.CONFIG_PREFIX + "spawning.dungeonSpawnerWeightScale")
				.define("dungeonSpawnerWeightScale", 1.0D);

		builder.pop();
	}
}
