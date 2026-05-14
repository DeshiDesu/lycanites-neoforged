package com.lycanitesmobs.client.event.tooltip;

import com.lycanitesmobs.client.tooltip.EquipmentSeparatorTooltipComponent;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.item.tooltip.*;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EquipmentTooltipGatherHandler {
    @SubscribeEvent
    public static void onGather(RenderTooltipEvent.GatherComponents event) {
        if (!(event.getItemStack().getItem() instanceof ItemEquipmentPart)) return;

        var elements = event.getTooltipElements();
        ItemStack stack = event.getItemStack();
        ItemEquipmentPart part = (ItemEquipmentPart) stack.getItem();
        int level = part.getPartLevel(stack);

        for (int i = 0; i < elements.size(); i++) {
            var either = elements.get(i);
            if (either.left().isEmpty()) continue;

            String s = either.left().get().getString();

            if ("[[SEP]]".equals(s)) {
                elements.set(i, Either.right(new EquipmentSeparatorTooltipComponent(0xFF00FF00)));
                continue;
            }

            if ("[[ELEM_TITLE]]".equals(s)) {
                elements.set(i, Either.right(new EquipmentElementTitleComponent(0xFFFFFFFF, true)));
                continue;
            }

            if (s.startsWith("[[ELEM_ICONS:") && s.endsWith("]]")) {
                String inner = s.substring(13, s.length() - 2);
                List<String> icons = Arrays.asList(inner.split(","));

                elements.set(i, Either.right(new EquipmentElementIconsComponent(icons, true)));
                continue;
            }

            if (s.startsWith("[[SLOT_ICON:") && s.endsWith("]]")) {
                String inner = s.substring(12, s.length() - 2);
                elements.set(i, Either.right(new EquipmentSlotIconTooltipComponent(inner)));
                continue;
            }
            if ("[[SLOT_TITLE]]".equals(s)) {
                elements.set(i, Either.right(new EquipmentSlotTitleComponent(0x55FFFF)));
                continue;
            }
            if (s.startsWith("[[SLOT_ROW:") && s.endsWith("]]")) {

                String inner = s.substring(11, s.length() - 2);
                String[] parts = inner.split(":");
                if (parts.length != 5) {
                    continue;
                }

                String slotType = parts[0];
                int levelMax;
                int experience;
                int experienceMax;

                try {
                    levelMax = Integer.parseInt(parts[2]);
                    experience = Integer.parseInt(parts[3]);
                    experienceMax = Integer.parseInt(parts[4]);
                } catch (NumberFormatException e) {
                    continue;
                }

                EquipmentSlotRowTooltipComponent row =
                        new EquipmentSlotRowTooltipComponent(slotType, level, levelMax, experience, experienceMax);

                elements.set(i, Either.right(row));
                continue;
            }

            if (s.startsWith("[[BAR:") && s.endsWith("]]")) {
                String inner = s.substring(6, s.length() - 2);
                String[] parts = inner.split(":");
                if (parts.length != 3) continue;

                String labelKey = parts[0];
                int value = Integer.parseInt(parts[1]);
                int max = Integer.parseInt(parts[2]);

                int textColor = 0xFF078ca2;

                int barColor;
                if ("equipment.sharpness".equals(labelKey)) {
                    barColor = 0xFF00D7A0;
                } else if ("equipment.mana".equals(labelKey)) {
                    barColor = 0xFF7A8BFF;
                } else {
                    barColor = 0xFFFFFFFF;
                }

                EquipmentStatBarTooltipComponent bar =
                        new EquipmentStatBarTooltipComponent(labelKey, value, max, barColor, textColor);

                elements.set(i, Either.right(bar));
            }

        }
    }

}
