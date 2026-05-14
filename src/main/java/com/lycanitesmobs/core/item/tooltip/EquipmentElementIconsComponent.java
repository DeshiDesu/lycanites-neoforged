package com.lycanitesmobs.core.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public class EquipmentElementIconsComponent implements TooltipComponent {
    private final List<String> elementNames;
    private final boolean rightSide;

    public EquipmentElementIconsComponent(List<String> elementNames, boolean rightSide) {
        this.elementNames = elementNames;
        this.rightSide = rightSide;
    }

    public List<String> getElementNames() {
        return elementNames;
    }

    public boolean isRightSide() {
        return rightSide;
    }
}
