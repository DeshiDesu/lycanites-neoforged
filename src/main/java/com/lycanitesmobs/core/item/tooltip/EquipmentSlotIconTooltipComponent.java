package com.lycanitesmobs.core.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class EquipmentSlotIconTooltipComponent implements TooltipComponent {
    private final String slotType;

    public EquipmentSlotIconTooltipComponent(String slotType) {
        this.slotType = slotType;
    }

    public String getSlotType() {
        return slotType;
    }
}
