package com.lycanitesmobs.core.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class EquipmentSlotRowTooltipComponent implements TooltipComponent {
    private final String slotType;
    private final int level;
    private final int levelMax;
    private final int experience;
    private final int experienceMax;

    public EquipmentSlotRowTooltipComponent(String slotType, int level, int levelMax, int experience, int experienceMax) {
        this.slotType = slotType;
        this.level = level;
        this.levelMax = levelMax;
        this.experience = experience;
        this.experienceMax = experienceMax;
    }

    public String getSlotType() {
        return slotType;
    }

    public int getLevel() {
        return level;
    }

    public int getLevelMax() {
        return levelMax;
    }

    public int getExperience() {
        return experience;
    }

    public int getExperienceMax() {
        return experienceMax;
    }
}
