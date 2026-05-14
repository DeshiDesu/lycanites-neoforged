package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.core.util.helpers.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ButtonBase extends Button {
    public DrawHelper drawHelper;
    public int buttonId;



    // ==================================================
    //                    Constructor
    // ==================================================
    public ButtonBase(int buttonId, int x, int y, int width, int height, Component text, Button.OnPress pressable) {
        super(x, y, width, height, text, pressable,Button.DEFAULT_NARRATION);
        Minecraft minecraft = Minecraft.getInstance();
        this.drawHelper = new DrawHelper(minecraft, minecraft.font);
        this.buttonId = buttonId;
    }
}
