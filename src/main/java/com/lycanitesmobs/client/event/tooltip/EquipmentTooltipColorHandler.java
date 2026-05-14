package com.lycanitesmobs.client.event.tooltip;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.tooltip.*;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EquipmentTooltipColorHandler {
    @SubscribeEvent
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        if (!(event.getItemStack().getItem() instanceof ItemEquipmentPart)) return;

        int background = 0xF0100010;
        int borderStart = 0xFF00AA00;
        int borderEnd = 0xFF003300;

        event.setBackground(background);
        event.setBorderStart(borderStart);
        event.setBorderEnd(borderEnd);

        var font = event.getFont();
        var components = event.getComponents();

        int maxWidth = 0;
        for (ClientTooltipComponent c : components) {
            int w = c.getWidth(font);
            if (w > maxWidth) maxWidth = w;
        }

        for (ClientTooltipComponent c : components) {
            if (c instanceof EquipmentSeparatorClientTooltipComponent sep) {
                sep.setTooltipWidth(maxWidth);
            }
            if (c instanceof EquipmentStatBarClientTooltipComponent bar) {
                bar.setTooltipWidth(maxWidth);
            }
            if (c instanceof EquipmentElementIconsClientComponent icons) {
                icons.setTooltipWidth(maxWidth);
            }
            if (c instanceof EquipmentElementTitleClientComponent elemsTitle) {
                elemsTitle.setTooltipWidth(maxWidth);
            }
            if (c instanceof EquipmentSlotTitleClientComponent slotTitle) {
                slotTitle.setTooltipWidth(maxWidth);
            }
            if (c instanceof EquipmentSlotIconClientTooltipComponent slotIcon) {
                slotIcon.setTooltipWidth(maxWidth);
            }
            if (c instanceof EquipmentSlotRowClientTooltipComponent slotRow) {
                slotRow.setTooltipWidth(maxWidth);
            }
        }
    }

}
