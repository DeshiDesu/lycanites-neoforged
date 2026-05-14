package com.lycanitesmobs.client.gui.screen.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.screen.base.BaseScreen;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.client.gui.buttons.CreatureButton;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.network.message.MessageSummonSetSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class MinionSelectionScreen extends BaseScreen {
    public Player player;
    public ExtendedPlayer playerExt;

    int centerX;
    int centerY;
    int windowWidth;
    int windowHeight;
    int windowX;
    int windowY;

    /**
     * Constructor
     *
     * @param player The player opening this Screen.
     */
    public MinionSelectionScreen(Player player) {
        super(Component.translatable("gui.minion.selection"));
        this.player = player;
        this.playerExt = ExtendedPlayer.getForPlayer(player);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;
        this.windowWidth = 256;
        this.windowHeight = 256;
        this.windowX = this.centerX;
        this.windowY = this.centerY;

        super.init();
    }


    @Override
    protected void initWidgets() {
        int buttonWidth = 32;
        int buttonHeight = 32;
        int buttonX = this.centerX - Math.round(buttonWidth / 2);
        int buttonY = this.centerY - Math.round(buttonHeight / 2);
        ButtonBase button;
        CreatureInfo creatureInfo;
        int offset = 32;

        creatureInfo = this.playerExt.getSummonSet(1).getCreatureInfo();
        button = new CreatureButton(1, buttonX, buttonY - Math.round(offset * 2), buttonWidth, buttonHeight, Component.translatable("1"), 1, creatureInfo, this);
        this.addRenderableWidget(button);

        creatureInfo = this.playerExt.getSummonSet(2).getCreatureInfo();
        button = new CreatureButton(2, buttonX + Math.round(offset * 2), buttonY - Math.round(offset * 0.5F), buttonWidth, buttonHeight, Component.translatable("2"), 2, creatureInfo, this);
        this.addRenderableWidget(button);

        creatureInfo = this.playerExt.getSummonSet(3).getCreatureInfo();
        button = new CreatureButton(3, buttonX + Math.round(offset), buttonY + Math.round(offset * 1.75F), buttonWidth, buttonHeight, Component.translatable("3"), 3, creatureInfo, this);
        this.addRenderableWidget(button);

        creatureInfo = this.playerExt.getSummonSet(4).getCreatureInfo();
        button = new CreatureButton(4, buttonX - Math.round(offset), buttonY + Math.round(offset * 1.75F), buttonWidth, buttonHeight, Component.translatable("4"), 4, creatureInfo, this);
        this.addRenderableWidget(button);

        creatureInfo = this.playerExt.getSummonSet(5).getCreatureInfo();
        button = new CreatureButton(5, buttonX - Math.round(offset * 2), buttonY - Math.round(offset * 0.5F), buttonWidth, buttonHeight, Component.translatable("5"), 5, creatureInfo, this);
        this.addRenderableWidget(button);
    }


    @Override
    protected void renderForeground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    protected void renderWidgets(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (Renderable buttonObj : this.renderables) {
            if (buttonObj instanceof ButtonBase button) {
                button.visible = this.playerExt.getSummonSet(button.buttonId).isUseable();
                button.active = button.buttonId != this.playerExt.selectedSummonSet;
            }
        }
    }


    @Override
    protected void renderBackground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
    }


    @Override
    public void actionPerformed(int buttonId) {
        this.playerExt.setSelectedSummonSet(buttonId);
        MessageSummonSetSelection message = new MessageSummonSetSelection(this.playerExt);
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
    }

    @Override
    public boolean keyPressed(int par1, int par2, int par3) {
        if (par2 == 1 || par2 == Minecraft.getInstance().options.keyInventory.getKey().getValue())
            Minecraft.getInstance().player.closeContainer();
        return super.keyPressed(par1, par2, par3);
    }
}
