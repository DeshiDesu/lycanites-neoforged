package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.screen.beastiary.IndexBeastiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MainTab extends Tab {

	public MainTab(int id, OnPress pressable) {
        super(id, Tab.startX, Tab.startY, new ResourceLocation(LycanitesMobs.MODID, "textures/items/soulgazer.png"), Component.translatable("Index"), pressable);
    }

    public MainTab(int id, int x, int y, OnPress pressable) {
        super(id, x, y, new ResourceLocation(LycanitesMobs.MODID, "textures/items/soulgazer.png"), Component.translatable("Index"), pressable);
    }

    @Override
    public void onTabClicked () {
	    Minecraft.getInstance().setScreen(new IndexBeastiaryScreen(Minecraft.getInstance().player));
    }

    @Override
    public boolean shouldAddToList () {
        return true;
    }
}
