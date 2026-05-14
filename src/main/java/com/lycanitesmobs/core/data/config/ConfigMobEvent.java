package com.lycanitesmobs.core.data.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigMobEvent {
	public static ConfigMobEvent INSTANCE;

	public final ForgeConfigSpec.ConfigValue<Boolean> mobEventsEnabled;
	public final ForgeConfigSpec.ConfigValue<Boolean> mobEventsRandom;
	public final ForgeConfigSpec.ConfigValue<Integer> defaultMobDuration;
	public final ForgeConfigSpec.ConfigValue<Integer> minEventsRandomDay;
	public final ForgeConfigSpec.ConfigValue<Integer> minTicksUntilEvent;
	public final ForgeConfigSpec.ConfigValue<Integer> maxTicksUntilEvent;

	public final ForgeConfigSpec.ConfigValue<Boolean> altarsEnabled;
	public final ForgeConfigSpec.ConfigValue<Boolean> altarsCheckDimension;

	public ConfigMobEvent(ForgeConfigSpec.Builder builder) {
		builder.push("Mob Events");
		builder.comment("These are various settings that apply to all mob events.");

		this.mobEventsEnabled = builder
				.comment("Set to false to completely disable the entire event.")
				.translation(CoreConfig.CONFIG_PREFIX + "mobevents.enabled")
				.define("enabled", true);

		this.mobEventsRandom = builder
				.comment("Set to false to disable random mob events for every world.")
				.translation(CoreConfig.CONFIG_PREFIX + "mobevents.enabled")
				.define("random.enabled", false);

		this.defaultMobDuration = builder
				.comment("The default temporary time applied to mobs spawned from events, where it will forcefully despawn after the specified time (in ticks). MobSpawns can override this.")
				.translation(CoreConfig.CONFIG_PREFIX + "mobevents.enabled")
				.define("duration", 12000);

		this.minEventsRandomDay = builder
				.comment("If random events are enabled, they wont occur until this day is reached. Set to 0 to have random events enabled from the start of a world.")
				.translation(CoreConfig.CONFIG_PREFIX + "mobevents.enabled")
				.define("random.day.min", 0);

		this.minTicksUntilEvent = builder
				.comment("Minimum time in ticks until a random event can occur. 20 Ticks = 1 Second.")
				.translation(CoreConfig.CONFIG_PREFIX + "mobevents.enabled")
				.define("random.ticks.min", 60 * 60 * 20);

		this.maxTicksUntilEvent = builder
				.comment("Maximum time in ticks until a random event can occur. 20 Ticks = 1 Second.")
				.translation(CoreConfig.CONFIG_PREFIX + "mobevents.enabled")
				.define("random.ticks.max", 120 * 60 * 20);

		this.altarsEnabled = builder
				.comment("Set to false to disable altars, Soulkeys can still be crafted but wont work on Altars.")
				.translation(CoreConfig.CONFIG_PREFIX + "altars.enabled")
				.define("altars.enabled", true);

		this.altarsCheckDimension = builder
				.comment("If set to true, Altars will only activate in dimensions that the monster spawned or event started is allowed in.")
				.translation(CoreConfig.CONFIG_PREFIX + "altars.checkDimension")
				.define("altars.checkDimension", false);

		builder.pop();
	}
}
