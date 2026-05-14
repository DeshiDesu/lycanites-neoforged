package com.lycanitesmobs.core.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class EquipmentSlotTitleComponent implements TooltipComponent {
    private final int color;

    public EquipmentSlotTitleComponent(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
