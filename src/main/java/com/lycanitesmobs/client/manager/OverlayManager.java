package com.lycanitesmobs.client.manager;

import com.lycanitesmobs.core.util.helpers.DrawHelper;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;

import java.util.ArrayList;

public class OverlayManager {
    public static OverlayManager instance = new OverlayManager();
    public DrawHelper drawHelper;
    public Minecraft minecraft;

    public OverlayManager() {
        this.minecraft = Minecraft.getInstance();
        this.drawHelper = new DrawHelper(minecraft, minecraft.font);
    }

    public static OverlayManager getInstance() {
        return instance;
    }

    /**
     * Custom Debug Text Gui Event because the old one was removed
     */
    public static class Text extends RenderGuiOverlayEvent.Pre {
        private final ArrayList<String> left;
        private final ArrayList<String> right;

        public Text(Window window, GuiGraphics guiGraphics, float partialTick, NamedGuiOverlay overlay, ArrayList<String> left, ArrayList<String> right) {
            super(window, guiGraphics, partialTick, overlay);
            this.left = left;
            this.right = right;
        }


        public ArrayList<String> getLeft() {
            return this.left;
        }

        public ArrayList<String> getRight() {
            return this.right;
        }
    }
}
