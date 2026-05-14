package com.lycanitesmobs.core.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class EquipmentElementTitleComponent implements TooltipComponent {
    private final int color;
    private final boolean rightSide;

    public EquipmentElementTitleComponent(int color, boolean rightSide) {
        this.color = color;
        this.rightSide = rightSide;
    }

    public int getColor() {
        return color;
    }

    public boolean isRightSide() {
        return rightSide;
    }
}
