package com.lycanitesmobs.core.data.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigClient {
    public static ConfigClient INSTANCE;

    public final ForgeConfigSpec.ConfigValue<Boolean> modelMultipass;
    public final ForgeConfigSpec.ConfigValue<Boolean> inventoryTab;
    public final ForgeConfigSpec.ConfigValue<Double> fearBlockLightDim;
    public final ForgeConfigSpec.ConfigValue<Double> fearDimPerLevel;
    public final ForgeConfigSpec.ConfigValue<Double> fearMuffleFactor;
    public final ForgeConfigSpec.ConfigValue<Double> fearAudioRange;
    public final ForgeConfigSpec.ConfigValue<Boolean> fearFlickerInverted;
    public final ForgeConfigSpec.ConfigValue<Double> fearHeartbeatSpeed;

    public ConfigClient(ForgeConfigSpec.Builder builder) {
        builder.push("Client");
        builder.comment("Client side settings, defaults are recommended except for low end systems or for compatibility with mods that alter rendering.");

        this.modelMultipass = builder
                .comment("Set to false to disable multipass rendering. This renders model layers twice so that they can show each over through alpha textures, disable for performance on low end systems.")
                .translation(CoreConfig.CONFIG_PREFIX + "client.modelMultipass")
                .define("modelMultipass", true);

        this.inventoryTab = builder
                .comment("Set to false to disable the inventory tabs added by this mod.")
                .translation(CoreConfig.CONFIG_PREFIX + "client.inventoryTab")
                .define("inventoryTab", true);

        this.fearBlockLightDim = builder
                .comment("Base block light dimming at Fear I. Block light dims first, then sky light. 0.0 = no dimming, 1.0 = block light fully suppressed.")
                .translation(CoreConfig.CONFIG_PREFIX + "client.fearBlockLightDim")
                .defineInRange("fearBlockLightDim", 0.7D, 0.0D, 1.0D);

        this.fearDimPerLevel = builder
                .comment("Additional total darkness per fear amplifier level. The darkness budget (base + amplifier * this) fills block light first, then overflows into sky light. At Fear V (amplifier 4) with defaults: block light is fully gone and sky light is 90% dimmed (near-blindness).")
                .translation(CoreConfig.CONFIG_PREFIX + "client.fearDimPerLevel")
                .defineInRange("fearDimPerLevel", 0.3D, 0.0D, 1.0D);

        this.fearMuffleFactor = builder
                .comment("Base audio muffle factor at Fear I. Controls how much sound volume is reduced. 0.0 = no muffling, 1.0 = complete silence at audio range distance. Scales up with fear amplifier (+0.15 per level). At Fear III+ with defaults, sounds at audio range are fully muffled.")
                .translation(CoreConfig.CONFIG_PREFIX + "client.fearMuffleFactor")
                .defineInRange("fearMuffleFactor", 0.8D, 0.0D, 1.0D);

        this.fearAudioRange = builder
                .comment("Maximum audible distance (in blocks) when fear audio muffling is active. Sounds at the player's position are only partially muffled (30% of the muffle factor), while sounds at or beyond this distance receive full muffling. Lower values create a tighter 'hearing bubble'.")
                .translation(CoreConfig.CONFIG_PREFIX + "client.fearAudioRange")
                .defineInRange("fearAudioRange", 8.0D, 1.0D, 64.0D);

        this.fearFlickerInverted = builder
                .comment("When false (default), lights dim on each heartbeat pulse and recover between beats. When true, lights are dim between beats and briefly brighten on each pulse.")
                .translation(CoreConfig.CONFIG_PREFIX + "client.fearFlickerInverted")
                .define("fearFlickerInverted", true);

        this.fearHeartbeatSpeed = builder
                .comment("Global speed multiplier for the fear heartbeat. Controls both audio playback speed (pitch) and repetition rate. 1.0 = normal speed. 0.5 = half speed (slower, deeper heartbeat with longer gaps). 2.0 = double speed (faster, higher heartbeat). Fear amplifier still scales the repetition rate on top of this.")
                .translation(CoreConfig.CONFIG_PREFIX + "client.fearHeartbeatSpeed")
                .defineInRange("fearHeartbeatSpeed", 0.7D, 0.1D, 4.0D);

        builder.pop();
    }
}
