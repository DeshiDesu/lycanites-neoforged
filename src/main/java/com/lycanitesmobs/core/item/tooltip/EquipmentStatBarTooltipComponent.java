package com.lycanitesmobs.core.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class EquipmentStatBarTooltipComponent implements TooltipComponent {
    private final String labelKey;
    private final int value;
    private final int max;
    private final int barColor;
    private final int textColor;

    public EquipmentStatBarTooltipComponent(String labelKey, int value, int max, int barColor, int textColor) {
        this.labelKey = labelKey;
        this.value = value;
        this.max = max;
        this.barColor = barColor;
        this.textColor = textColor;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public int getValue() {
        return value;
    }

    public int getMax() {
        return max;
    }

    public int getBarColor() {
        return barColor;
    }

    public int getTextColor() {
        return textColor;
    }
}
