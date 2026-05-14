package com.lycanitesmobs.client.renderer.item;

import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class EquipmentItemDecorator implements IItemDecorator {

    private static final int BAR_WIDTH = 13;

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!(stack.getItem() instanceof ItemEquipment equipment)) {
            return false;
        }

        int sharpness = equipment.getSharpness(stack);
        int mana = equipment.getMana(stack);
        boolean showSharpness = sharpness < ItemEquipment.SHARPNESS_MAX;
        boolean showMana = mana < ItemEquipment.MANA_MAX;

        if (!showSharpness && !showMana) {
            return false;
        }

        int barX = xOffset + 2;

        if (showSharpness) {
            int barY = yOffset + 13;
            float ratio = (float) sharpness / ItemEquipment.SHARPNESS_MAX;
            int fillWidth = Math.round(BAR_WIDTH * ratio);
            int color = getSharpnessColor(ratio);

            graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 2, 0xFF000000);
            graphics.fill(barX, barY, barX + fillWidth, barY + 1, color | 0xFF000000);
        }

        if (showMana) {
            int barY = showSharpness ? yOffset + 11 : yOffset + 13;
            float ratio = (float) mana / ItemEquipment.MANA_MAX;
            int fillWidth = Math.round(BAR_WIDTH * ratio);
            int color = getManaColor(ratio);

            graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 2, 0xFF000000);
            graphics.fill(barX, barY, barX + fillWidth, barY + 1, color | 0xFF000000);
        }

        return true;
    }

    /**
     * Returns the sharpness bar color using the same gradient as vanilla durability (green to red).
     */
    private static int getSharpnessColor(float ratio) {
        int r = Math.min(255, (int) ((1.0F - ratio) * 2.0F * 255));
        int g = Math.min(255, (int) (ratio * 2.0F * 255));
        return (r << 16) | (g << 8);
    }

    /**
     * Returns the mana bar color as a blue gradient (dark blue when low, bright blue when full).
     */
    private static int getManaColor(float ratio) {
        int r = (int) (30 * ratio);
        int g = (int) (100 + 155 * ratio);
        int b = 255;
        return (r << 16) | (g << 8) | b;
    }
}
