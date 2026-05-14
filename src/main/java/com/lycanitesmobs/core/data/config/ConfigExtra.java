package com.lycanitesmobs.core.data.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ConfigExtra {
	public static ConfigExtra INSTANCE;

	public final ForgeConfigSpec.ConfigValue<Boolean> disableSneakDismount;
	public final ForgeConfigSpec.ConfigValue<Integer> summoningPedestalRedstoneTime;
	public final ForgeConfigSpec.ConfigValue<Boolean> versionCheckerEnabled;
	public final ForgeConfigSpec.ConfigValue<Boolean> disableNausea;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> familiarBlacklist;

	public ConfigExtra(ForgeConfigSpec.Builder builder) {
		builder.push("Extra");
		builder.comment("Other extra config settings, some of the aren't necessarily specific to Lycanites Mobs.");

		this.disableSneakDismount = builder
				.comment("Set to true to prevent players from dismounting from Lycanites Mobs mounts when pressing shift (useful for shift to fly/swim down).")
				.translation(CoreConfig.CONFIG_PREFIX + "disableSneakDismount")
				.define("disableSneakDismount", true);

		this.summoningPedestalRedstoneTime = builder
				.comment("How much summoning time (in ticks) 1 redstone dust provides. 20 ticks = 1 second, default is 12000 (10 minutes).")
				.translation(CoreConfig.CONFIG_PREFIX + "summoningpedestal.redstonetime")
				.define("summoningpedestal.redstonetime", 10 * 60 * 20);

		this.versionCheckerEnabled = builder
				.comment("Set to false to disable the version checker.")
				.translation(CoreConfig.CONFIG_PREFIX + "version.checker")
				.define("version.checker", true);

		this.disableNausea = builder
				.comment("Set to true to disable the nausea debuff on players.")
				.translation(CoreConfig.CONFIG_PREFIX + "disableNausea")
				.define("disableNausea", false);

		this.familiarBlacklist = builder
				.comment("Donation Familiars help support the development of this mod but can be turned of for individual players be adding their username to this list.")
				.translation(CoreConfig.CONFIG_PREFIX + "familiars.blacklist")
				.defineList("familiars.blacklist", Lists.newArrayList(), o -> o instanceof String);

		builder.pop();
	}
}
