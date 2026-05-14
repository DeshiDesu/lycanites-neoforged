package com.lycanitesmobs.client.gui.screen.base;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public class BaseOverlayScreen extends BaseGui {
    public int mountMessageTimeMax = 10 * 20;
    public int mountMessageTime = 0;

    // ==================================================
    //                     Constructor
    // ==================================================
    public BaseOverlayScreen(Minecraft minecraft) {
        super(Component.translatable("gui.overlay"));
    }

    public static BaseOverlayScreen newInstance() {
        return new BaseOverlayScreen(Minecraft.getInstance());
    }

    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");

}
