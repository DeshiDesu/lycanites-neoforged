package com.lycanitesmobs.client.tooltip;

import com.lycanitesmobs.core.item.tooltip.EquipmentSlotRowTooltipComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EquipmentSlotRowClientTooltipComponent implements ClientTooltipComponent {
    private final EquipmentSlotRowTooltipComponent data;
    private int tooltipWidth;
    private static final ResourceLocation GUI_ICONS =
            new ResourceLocation("minecraft", "textures/gui/icons.png");

    public EquipmentSlotRowClientTooltipComponent(EquipmentSlotRowTooltipComponent data) {
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

        int colWidth = tooltipWidth / 3;
        int leftColX = x;
        int topY = y - 3;
        int barYOffset = 12;

        Component levelTitle = Component.translatable("equipment.level");
        int levelTitleWidth = font.width(levelTitle);
        int levelTitleX = leftColX + (colWidth - levelTitleWidth) / 2;
        int levelTitleY = topY - barYOffset;
        graphics.drawString(font, levelTitle, levelTitleX, levelTitleY, 0xFFFFD700, false);

        int levelBarsTop = levelTitleY + font.lineHeight + 1;
        int levelBarsHeight = 4;
        int levelBarsWidth = colWidth - 12;
        int levelBarsLeft = leftColX + (colWidth - levelBarsWidth) / 2;

        int segments = 3;
        int segmentSpacing = 2;
        int segmentWidth = (levelBarsWidth - (segmentSpacing * (segments - 1))) / segments;

        int clampedLevel = data.getLevel();
        if (clampedLevel < 0) clampedLevel = 0;
        if (clampedLevel > segments) clampedLevel = segments;

        for (int i = 0; i < segments; i++) {
            int segLeft = levelBarsLeft + i * (segmentWidth + segmentSpacing);
            int segRight = segLeft + segmentWidth;
            int segTop = levelBarsTop;
            int segBottom = levelBarsTop + levelBarsHeight;

            int color = i < clampedLevel ? 0xFF00D764 : 0xFF555555;
            graphics.fill(segLeft, segTop, segRight, segBottom, color);
        }
        if (data.getLevel() < data.getLevelMax()) {


            int xpBarTexWidth = 182;
            int xpBarTexHeight = 5;

            int xpBarWidth = levelBarsWidth - 2;
            int xpBarLeft = levelBarsLeft;
            int xpBarTop = levelBarsTop + levelBarsHeight + 3;

            float scale = (float) xpBarWidth / (float) xpBarTexWidth;

            int xpMax = data.getExperienceMax();
            float ratio = 0F;
            if (xpMax > 0) {
                ratio = (float) data.getExperience() / (float) xpMax;
                if (ratio < 0F) ratio = 0F;
                if (ratio > 1F) ratio = 1F;
            }

            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(xpBarLeft, xpBarTop, 0);
            pose.scale(scale, 1F, 1F);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_ICONS);

            graphics.blit(GUI_ICONS, 0, 0, 0, 64, xpBarTexWidth, xpBarTexHeight);

            int filledTexWidth = (int) (xpBarTexWidth * ratio);
            if (filledTexWidth > 0) {
                graphics.blit(GUI_ICONS, 0, 0, 0, 69, filledTexWidth, xpBarTexHeight);
            }

            pose.popPose();
        }
    }

}
