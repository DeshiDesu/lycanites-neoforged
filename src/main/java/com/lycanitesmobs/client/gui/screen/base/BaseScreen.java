package com.lycanitesmobs.client.gui.screen.base;

import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.util.helpers.DrawHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;

import java.net.URI;

public abstract class BaseScreen extends Screen implements Button.OnPress {
    public DrawHelper drawHelper;
    public Window scaledResolution;
    public float zLevel = 0;

    public BaseScreen(MutableComponent screenName) {
        super(screenName);
    }

    /**
     * Secondary init method called by main init method.
     */
    @Override
    public void init() {
        this.drawHelper = new DrawHelper(minecraft, minecraft.font);
        this.initWidgets();
    }

    /**
     * Initialises all buttons and other widgets that this Screen uses.
     */
    protected abstract void initWidgets();

    /**
     * Draws and updates the GUI.
     *
     * @param guiGraphics  The matrix stack to draw with.
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderWidgets(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderForeground(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Draws the background image.
     *
     * @param matrixStack  The matrix stack to draw with.
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The x position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    protected abstract void renderBackground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks);

    /**
     * Updates widgets like buttons and other controls for this screen. Super renders the button list, called after this.
     *
     * @param matrixStack  The matrix stack to draw with.
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The x position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    protected void renderWidgets(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
    }

    /**
     * Draws foreground elements.
     *
     * @param matrixStack  The matrix stack to draw with.
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The x position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    protected abstract void renderForeground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks);

    @Override
    public void onPress(Button guiButton) {
        if (!(guiButton instanceof ButtonBase)) {
            return;
        }
        this.actionPerformed(((ButtonBase) guiButton).buttonId);
    }

    /**
     * Called when a Button Base is pressed providing the press button's id.
     *
     * @param buttonId The id of the button pressed.
     */
    public abstract void actionPerformed(int buttonId);

    /**
     * Returns a scaled x coordinate.
     *
     * @param x The x float to scale where 1.0 is the entire GUI width.
     * @return A scaled x position.
     */
    public int getScaledX(float x) {
        if (this.scaledResolution == null) {
            this.scaledResolution = this.minecraft.getWindow();
        }

        float targetAspect = 0.5625f;
        float scaledHeight = scaledResolution.getGuiScaledHeight();
        float scaledWidth = scaledResolution.getGuiScaledWidth();
        float currentAspect = (scaledHeight * x) / (scaledWidth * x);

        if (currentAspect < targetAspect) {
            scaledWidth = scaledHeight + (scaledHeight * targetAspect);
        } else if (currentAspect > targetAspect) {
            scaledHeight = scaledWidth + (scaledWidth * targetAspect);
        }

        float guiWidth = scaledWidth * x;
        return Math.round(Math.max(x, guiWidth));
    }

    /**
     * Returns a scaled y coordinate based on the scaled width with an aspect ratio applied to it.
     *
     * @param y The y float to scale where 1.0 is the entire GUI height.
     * @return A scaled y position.
     */
    public int getScaledY(float y) {
        float baseHeight = Math.round((float) this.getScaledX(y) * 0.5625f);
        return Math.round(baseHeight * y);
    }

    /**
     * Opens a URI in the users default web browser.
     *
     * @param uri The URI link to open.
     */
    protected void openURI(URI uri) {
        try {
            Util.getPlatform().openUri(uri);
        } catch (Exception e) {
            LMHelperClass.logWarning("", "Unable to open link: " + uri.toString());
            e.printStackTrace();
        }
    }
}
