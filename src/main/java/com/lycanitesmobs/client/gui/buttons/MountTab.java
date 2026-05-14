package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.manager.KeyManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MountTab extends Tab {

    public MountTab(int id, OnPress pressable) {
        super(id, Tab.startX, Tab.startY, new ResourceLocation("textures/items/saddle.png"), Component.translatable("Mounts"), pressable);
    }

    @Override
    public void onTabClicked() {
        KeyMapping.set(KeyManager.instance.mountInventory.getKey(), true); //TODO Add a better way that works!
    }

    @Override
    public boolean shouldAddToList() {
        return false;
        /*boolean ridingMount = Minecraft.getMinecraft().player.ridingEntity instanceof EntityCreatureRideable;
        if(ridingMount)
        	this.icon = ((EntityCreatureRideable)Minecraft.getMinecraft().player.ridingEntity).creatureInfo.getSprite();
        return ridingMount;*/
    }
}
