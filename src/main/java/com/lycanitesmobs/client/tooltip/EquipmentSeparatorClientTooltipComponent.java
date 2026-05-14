package com.lycanitesmobs.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class EquipmentSeparatorClientTooltipComponent implements ClientTooltipComponent {
    private final EquipmentSeparatorTooltipComponent data;
    private int tooltipWidth;

    public EquipmentSeparatorClientTooltipComponent(EquipmentSeparatorTooltipComponent data) {
        this.data = data;
    }

    public void setTooltipWidth(int tooltipWidth) {
        this.tooltipWidth = tooltipWidth;
    }

    @Override
    public int getHeight() {
        return 9;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (tooltipWidth <= 0) return;

        int inset = 0;
        int left = x + inset;
        int right = x + tooltipWidth - inset;
        int lineY = y + getHeight() / 2;

        if (right > left) {
            graphics.fill(left, lineY, right, lineY + 1, data.getColor());
        }
    }
}
