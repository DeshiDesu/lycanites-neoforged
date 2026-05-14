package com.lycanitesmobs.client.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.renderer.item.EquipmentItemDecorator;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EquipmentItemDecorationRegistration {
    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        Item equipmentItem = ObjectManager.getItem("equipment");
        if (equipmentItem != null) {
            event.register(equipmentItem, new EquipmentItemDecorator());
        }
    }
}
