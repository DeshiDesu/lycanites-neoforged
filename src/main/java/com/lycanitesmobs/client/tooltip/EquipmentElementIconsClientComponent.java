package com.lycanitesmobs.client.tooltip;

import com.lycanitesmobs.core.item.tooltip.EquipmentElementIconsComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public class EquipmentElementIconsClientComponent implements ClientTooltipComponent {

    private final EquipmentElementIconsComponent data;
    private int tooltipWidth;

    public EquipmentElementIconsClientComponent(EquipmentElementIconsComponent data) {
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
        int yOffset = -25;
        int iconSize = 10;
        int spacing = 2;

        int baseX = x;
        int areaWidth = tooltipWidth;

        if (data.isRightSide()) {
            int colWidth = tooltipWidth / 3;
            baseX = x + colWidth * 2;
            areaWidth = colWidth;
        }

        int totalWidth = data.getElementNames().size() * (iconSize + spacing) - spacing;
        int left = baseX + (areaWidth - totalWidth) / 2;
        int top = y + 1 + yOffset;

        for (String elementName : data.getElementNames()) {
            ResourceLocation icon = new ResourceLocation(
                    "lycanitesmobs",
                    "textures/gui/element/" + elementName + ".png"
            );

            Vector3f tint = getTintForElement(elementName);

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(tint.x, tint.y, tint.z, 1.0F);

            graphics.blit(icon, left, top, 0, 0, iconSize, iconSize, iconSize, iconSize);

            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();

            left += iconSize + spacing;
        }
    }


    private Vector3f getTintForElement(String elementName) {
        switch (elementName) {
            case "acid":
                return new Vector3f(0.60F, 0.95F, 0.25F);
            case "aether":
                return new Vector3f(0.80F, 0.85F, 1.00F);
            case "air":
                return new Vector3f(0.70F, 0.90F, 1.00F);
            case "arbour":
                return new Vector3f(0.30F, 0.70F, 0.30F);
            case "arcane":
                return new Vector3f(0.65F, 0.30F, 0.95F);
            case "chaos":
                return new Vector3f(1.00F, 0.30F, 0.30F);
            case "earth":
                return new Vector3f(0.55F, 0.40F, 0.20F);
            case "fae":
                return new Vector3f(0.90F, 0.40F, 0.95F);
            case "fire":
                return new Vector3f(1.00F, 0.40F, 0.05F);
            case "frost":
                return new Vector3f(0.60F, 0.90F, 1.00F);
            case "lava":
                return new Vector3f(1.00F, 0.25F, 0.00F);
            case "light":
                return new Vector3f(1.00F, 1.00F, 0.60F);
            case "lightning":
                return new Vector3f(0.95F, 0.95F, 1.00F);
            case "nether":
                return new Vector3f(0.65F, 0.00F, 0.00F);
            case "order":
                return new Vector3f(0.90F, 0.90F, 1.00F);
            case "phase":
                return new Vector3f(0.70F, 0.10F, 1.00F);
            case "poison":
                return new Vector3f(0.40F, 0.85F, 0.30F);
            case "quake":
                return new Vector3f(0.50F, 0.35F, 0.15F);
            case "shadow":
                return new Vector3f(0.20F, 0.00F, 0.30F);
            case "void":
                return new Vector3f(0.10F, 0.00F, 0.25F);
            case "water":
                return new Vector3f(0.25F, 0.45F, 1.00F);
            default:
                return new Vector3f(1.00F, 1.00F, 1.00F);
        }
    }

}

