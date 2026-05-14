package com.lycanitesmobs.client.tooltip;

import com.lycanitesmobs.core.item.tooltip.EquipmentStatBarTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class EquipmentStatBarClientTooltipComponent implements ClientTooltipComponent {
    private final EquipmentStatBarTooltipComponent data;
    private int tooltipWidth;

    public EquipmentStatBarClientTooltipComponent(EquipmentStatBarTooltipComponent data) {
        this.data = data;
    }

    public void setTooltipWidth(int tooltipWidth) {
        this.tooltipWidth = tooltipWidth;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (tooltipWidth <= 0) return;

        int inset = 4;
        int left = x + inset;
        int right = x + tooltipWidth - inset;
        if (right <= left) return;

        String text = Component.translatable(data.getLabelKey()).getString()
                + ": " + data.getValue() + "/" + data.getMax();

        float scale = 0.75F;
        int textPixelWidth = (int) (font.width(text) * scale);
        int barWidth = right - left;

        int textX = left + (barWidth - textPixelWidth) / 2;
        int textY = y;

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(textX, textY, 0);
        pose.scale(scale, scale, 1.0F);
        graphics.drawString(font, text, 0, 0, data.getTextColor(), false);
        pose.popPose();

        int textHeight = (int) (font.lineHeight * scale);

        int barTop = y + textHeight + 1;
        int barHeight = 4;
        int barBottom = barTop + barHeight;

        int bgColor = 0xAA000000;
        graphics.fill(left, barTop, right, barBottom, bgColor);

        float ratio = data.getMax() <= 0 ? 0F : (float) data.getValue() / (float) data.getMax();
        if (ratio < 0F) ratio = 0F;
        if (ratio > 1F) ratio = 1F;

        int filledRight = left + Math.round((right - left) * ratio);
        if (filledRight > left) {
            graphics.fill(left + 1, barTop + 1, filledRight - 1, barBottom - 1, data.getBarColor());
        }
    }
}
