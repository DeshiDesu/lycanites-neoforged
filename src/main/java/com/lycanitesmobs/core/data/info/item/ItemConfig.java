package com.lycanitesmobs.core.data.info.item;

import com.lycanitesmobs.core.data.config.ConfigItem;

import java.util.List;

public class ItemConfig {
    public static double seasonalItemDropChance = 0.1D;
    public static boolean removeOnNoFireTick = true;

    public static boolean chargesToXPBottles = true;

    public static boolean equipmentSpecialNames = false;

    public static int lowEquipmentRepairAmount = 50;
    public static int mediumEquipmentRepairAmount = 100;
    public static int highEquipmentRepairAmount = 500;

    public static List<? extends String> lowEquipmentSharpnessItems;
    public static List<? extends String> mediumEquipmentSharpnessItems;
    public static List<? extends String> highEquipmentSharpnessItems;
    public static List<? extends String> maxEquipmentSharpnessItems;

    public static List<? extends String> lowEquipmentManaItems;
    public static List<? extends String> mediumEquipmentManaItems;
    public static List<? extends String> highEquipmentManaItems;
    public static List<? extends String> maxEquipmentManaItems;

    public static void loadGlobalSettings() {
        seasonalItemDropChance = ConfigItem.INSTANCE.seasonalDropChance.get();
        removeOnNoFireTick = ConfigItem.INSTANCE.removeOnNoFireTick.get();

        chargesToXPBottles = ConfigItem.INSTANCE.chargesToXPBottles.get();

        equipmentSpecialNames = ConfigItem.INSTANCE.equipmentSpecialNames.get();

        lowEquipmentRepairAmount = ConfigItem.INSTANCE.lowEquipmentRepairAmount.get();
        mediumEquipmentRepairAmount = ConfigItem.INSTANCE.mediumEquipmentRepairAmount.get();
        highEquipmentRepairAmount = ConfigItem.INSTANCE.highEquipmentRepairAmount.get();

        lowEquipmentSharpnessItems = ConfigItem.INSTANCE.lowEquipmentSharpnessItems.get();
        mediumEquipmentSharpnessItems = ConfigItem.INSTANCE.mediumEquipmentSharpnessItems.get();
        highEquipmentSharpnessItems = ConfigItem.INSTANCE.highEquipmentSharpnessItems.get();
        maxEquipmentSharpnessItems = ConfigItem.INSTANCE.maxEquipmentSharpnessItems.get();

        lowEquipmentManaItems = ConfigItem.INSTANCE.lowEquipmentManaItems.get();
        mediumEquipmentManaItems = ConfigItem.INSTANCE.mediumEquipmentManaItems.get();
        highEquipmentManaItems = ConfigItem.INSTANCE.highEquipmentManaItems.get();
        maxEquipmentManaItems = ConfigItem.INSTANCE.maxEquipmentManaItems.get();
    }
}