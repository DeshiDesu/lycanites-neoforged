package com.lycanitesmobs.core.data.config;

import com.google.common.collect.Lists;
import com.lycanitesmobs.LycanitesMobs;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ConfigDebug {
	public static ConfigDebug INSTANCE;

	public final ForgeConfigSpec.ConfigValue<Boolean> creatureOverlay;

	public final ForgeConfigSpec.ConfigValue<List<? extends String>> enabled;

	public ConfigDebug(ForgeConfigSpec.Builder builder) {
		builder.push("Debug");
		builder.comment("Set debug options to true to show extra debugging information in the console.");

		this.creatureOverlay = builder
				.comment("Shows debugging info for Elements.")
				.translation(CoreConfig.CONFIG_PREFIX + "elements")
				.define("creatureOverlay", false);

		this.enabled = builder
				.comment("Shows debugging info for Mob Events.")
				.translation(CoreConfig.CONFIG_PREFIX + "debug.enabled")
				.defineList("enabled", Lists.newArrayList(), o -> o instanceof String);

		builder.pop();
	}

	public boolean isEnabled(String debugKey) {
		if(!LycanitesMobs.configReady) {
			return false;
		}

		if(this.enabled.get().isEmpty()) {
			return false;
		}

		for(String enabledDebugKey : this.enabled.get()) {
			if(debugKey.equalsIgnoreCase(enabledDebugKey))
				return true;
		}

		return false;
	}
}
