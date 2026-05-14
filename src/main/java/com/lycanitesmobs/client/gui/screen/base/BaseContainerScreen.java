package com.lycanitesmobs.client.gui.screen.base;

import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.util.helpers.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class BaseContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements Button.OnPress {
    public DrawHelper drawHelper;

    public BaseContainerScreen(T container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name);
    }

    /**
     * Secondary init method called by main init method.
     */
    @Override
    protected void init() {
        super.init();

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
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * Draws the background image.
     *
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    protected abstract void renderBackground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks);

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
    }

    /**
     * Updates widgets like buttons and other controls for this screen. Super renders the button list, called after this.
     *
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < this.renderables.size(); ++i) {
            this.renderables.get(i).render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Draws foreground elements.
     *
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The y position of the mouse cursor.
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
}
