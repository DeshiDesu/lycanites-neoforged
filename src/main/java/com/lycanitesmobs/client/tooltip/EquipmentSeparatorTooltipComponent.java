package com.lycanitesmobs.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class EquipmentSeparatorTooltipComponent implements TooltipComponent {
    private final int color;

    public EquipmentSeparatorTooltipComponent(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}

