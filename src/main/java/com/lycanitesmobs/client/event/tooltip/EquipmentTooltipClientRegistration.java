package com.lycanitesmobs.client.event.tooltip;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.tooltip.*;
import com.lycanitesmobs.core.item.tooltip.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EquipmentTooltipClientRegistration {
    @SubscribeEvent
    public static void registerFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(EquipmentSeparatorTooltipComponent.class, EquipmentSeparatorClientTooltipComponent::new);
        event.register(EquipmentStatBarTooltipComponent.class, EquipmentStatBarClientTooltipComponent::new);
        event.register(EquipmentElementTitleComponent.class, EquipmentElementTitleClientComponent::new);
        event.register(EquipmentElementIconsComponent.class, EquipmentElementIconsClientComponent::new);
        event.register(EquipmentSlotIconTooltipComponent.class, EquipmentSlotIconClientTooltipComponent::new);
        event.register(EquipmentSlotTitleComponent.class, EquipmentSlotTitleClientComponent::new);
        event.register(EquipmentSlotRowTooltipComponent.class, EquipmentSlotRowClientTooltipComponent::new);

    }

}
