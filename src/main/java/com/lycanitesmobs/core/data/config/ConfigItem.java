package com.lycanitesmobs.core.data.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ConfigItem {
    public static ConfigItem INSTANCE;

    public final ForgeConfigSpec.ConfigValue<Double> seasonalDropChance;
    public final ForgeConfigSpec.ConfigValue<Boolean> removeOnNoFireTick;

    public final ForgeConfigSpec.ConfigValue<Boolean> chargesToXPBottles;

    public final ForgeConfigSpec.ConfigValue<Integer> lowEquipmentRepairAmount;
    public final ForgeConfigSpec.ConfigValue<Integer> mediumEquipmentRepairAmount;
    public final ForgeConfigSpec.ConfigValue<Integer> highEquipmentRepairAmount;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> lowEquipmentSharpnessItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> mediumEquipmentSharpnessItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> highEquipmentSharpnessItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> maxEquipmentSharpnessItems;

    public final ForgeConfigSpec.ConfigValue<Boolean> equipmentSpecialNames;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> lowEquipmentManaItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> mediumEquipmentManaItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> highEquipmentManaItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> maxEquipmentManaItems;

    public ConfigItem(ForgeConfigSpec.Builder builder) {
        builder.push("Items");
        builder.comment("Global settings for blocks and items.");

        this.seasonalDropChance = builder
                .comment("The chance of seasonal items dropping such as Winter Gifts. Can be 0-1, 0.25 would be 25%. Set to 0 to disable these drops all together.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.seasonalDropChance")
                .define("seasonalDropChance", 0.1D);

        this.removeOnNoFireTick = builder
                .comment("If set to false, when the doFireTick gamerule is set to false, instead of removing all custom fire such as Hellfire, the fire simply stops spreading instead, this is useful for decorative fire on adventure maps and servers.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.removeOnNoFireTick")
                .define("removeOnNoFireTick", true);

        this.chargesToXPBottles = builder
                .comment("If true, Charge items can be placed into the Infuser with an Empty Bottle to make XP Bottles.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.chargesToXPBottles")
                .define("chargesToXPBottles", true);

        // Repair Amounts
        this.lowEquipmentRepairAmount = builder
                .comment("The amount of Sharpness or Mana Low tier Equipment repair items give.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.lowEquipmentRepairAmount")
                .define("lowEquipmentRepairAmount", 50);

        this.mediumEquipmentRepairAmount = builder
                .comment("The amount of Sharpness or Mana Medium tier Equipment repair items give.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.mediumEquipmentRepairAmount")
                .define("mediumEquipmentRepairAmount", 100);

        this.highEquipmentRepairAmount = builder
                .comment("The amount of Sharpness or Mana High tier Equipment repair items give.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.highEquipmentRepairAmount")
                .define("highEquipmentRepairAmount", 500);

        this.equipmentSpecialNames = builder
                .comment("If true, assembled Equipment pieces will receive dynamic names based on their parts (e.g. 'Venomous Serpixian Hellscythe'). If false, they use the default translation key.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.equipmentSpecialNames")
                .define("equipmentSpecialNames", false);

        // Sharpness
        this.lowEquipmentSharpnessItems = builder
                .comment("A list of ids for items that grant a low amount of Equipment Sharpness.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.lowEquipmentSharpnessItems")
                .defineList("item.equipment.lowEquipmentSharpnessItems", Lists.newArrayList(
                        "minecraft:flint",
                        "minecraft:bone"
                ), o -> o instanceof String);

        this.mediumEquipmentSharpnessItems = builder
                .comment("A list of ids for items that grant a medium amount of Equipment Sharpness.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.mediumEquipmentSharpnessItems")
                .defineList("item.equipment.mediumEquipmentSharpnessItems", Lists.newArrayList(
                        "minecraft:iron_ingot",
                        "minecraft:quartz"
                ), o -> o instanceof String);

        this.highEquipmentSharpnessItems = builder
                .comment("A list of ids for items that grant a high amount of Equipment Sharpness.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.highEquipmentSharpnessItems")
                .defineList("item.equipment.highEquipmentSharpnessItems", Lists.newArrayList(
                        "minecraft:diamond",
                        "minecraft:emerald",
                        "minecraft:prismarine_shard"
                ), o -> o instanceof String);

        this.maxEquipmentSharpnessItems = builder
                .comment("A list of ids for items that grant a maximum amount of Equipment Sharpness.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.maxEquipmentSharpnessItems")
                .defineList("item.equipment.maxEquipmentSharpnessItems", Lists.newArrayList(
                        "minecraft:netherite_ingot",
                        "minecraft:prismarine_crystal"
                ), o -> o instanceof String);

        // Mana
        this.lowEquipmentManaItems = builder
                .comment("A list of ids for items that grant a low amount of Equipment Mana.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.lowEquipmentManaItems")
                .defineList("item.equipment.lowEquipmentManaItems", Lists.newArrayList(
                        "minecraft:redstone",
                        "minecraft:glowstone_dust",
                        "minecraft:slime_ball"
                ), o -> o instanceof String);

        this.mediumEquipmentManaItems = builder
                .comment("A list of ids for items that grant a medium amount of Equipment Mana.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.mediumEquipmentManaItems")
                .defineList("item.equipment.mediumEquipmentManaItems", Lists.newArrayList(
                        "minecraft:lapis_lazuli",
                        "minecraft:blaze_powder",
                        "minecraft:gunpowder",
                        "minecraft:phantom_membrane",
                        "lycanitesmobs:frostyfur",
                        "lycanitesmobs:poisongland",
                        "lycanitesmobs:geistliver"
                ), o -> o instanceof String);

        this.highEquipmentManaItems = builder
                .comment("A list of ids for items that grant a high amount of Equipment Mana.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.highEquipmentManaItems")
                .defineList("item.equipment.highEquipmentManaItems", Lists.newArrayList(
                        "minecraft:experience_bottle",
                        "minecraft:magma_cream"
                ), o -> o instanceof String);

        this.maxEquipmentManaItems = builder
                .comment("A list of ids for items that grant a maximum amount of Equipment Mana.")
                .translation(CoreConfig.CONFIG_PREFIX + "item.equipment.maxEquipmentManaItems")
                .defineList("item.equipment.maxEquipmentManaItems", Lists.newArrayList(
                        "minecraft:nether_star"
                ), o -> o instanceof String);

        builder.pop();
    }
}
