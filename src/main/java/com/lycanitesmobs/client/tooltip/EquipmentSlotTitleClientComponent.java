package com.lycanitesmobs.client.tooltip;

import com.lycanitesmobs.core.item.tooltip.EquipmentSlotTitleComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class EquipmentSlotTitleClientComponent implements ClientTooltipComponent {
    private final EquipmentSlotTitleComponent data;
    private int tooltipWidth;

    public EquipmentSlotTitleClientComponent(EquipmentSlotTitleComponent data) {
        this.data = data;
    }

    public void setTooltipWidth(int tooltipWidth) {
        this.tooltipWidth = tooltipWidth;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (tooltipWidth <= 0) return;

        Component title = Component.translatable("equipment.slottype")
                .withStyle(style -> style.withUnderlined(true));

        int textWidth = font.width(title);
        int textX = x + (tooltipWidth - textWidth) / 2;
        int textY = y;

        graphics.drawString(font, title, textX, textY, data.getColor(), false);
    }
}
