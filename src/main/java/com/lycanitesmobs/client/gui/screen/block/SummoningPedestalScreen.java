package com.lycanitesmobs.client.gui.screen.block;

import com.lycanitesmobs.client.gui.screen.base.BaseContainerScreen;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.client.gui.buttons.MainTab;
import com.lycanitesmobs.client.gui.widgets.SummoningPedestalList;
import com.lycanitesmobs.core.container.block.SummoningPedestalContainer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11;

public class SummoningPedestalScreen extends BaseContainerScreen<SummoningPedestalContainer> {
    public Player player;
    public ExtendedPlayer playerExt;
    public TileEntitySummoningPedestal summoningPedestal;
    public SummonSet summonSet;

    public AbstractSelectionList list;

    public int centerX;
    public int centerY;
    public int windowWidth;
    public int windowHeight;
    public int halfX;
    public int halfY;
    public int windowX;
    public int windowY;

    public static int TAB_BUTTON_ID = 55555;

    public SummoningPedestalScreen(SummoningPedestalContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name);
        this.summoningPedestal = container.summoningPedestal;
        this.player = playerInventory.player;
        this.playerExt = ExtendedPlayer.getForPlayer(this.player);
        this.summonSet = this.summoningPedestal.summonSet;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();

        this.renderables.clear();
        this.windowWidth = 256;
        this.windowHeight = 166;
        this.halfX = this.windowWidth / 2;
        this.halfY = this.windowHeight / 2;
        this.windowX = (this.width / 2) - (this.windowWidth / 2);
        this.windowY = (this.height / 2) - (this.windowHeight / 2);
        this.centerX = this.windowX + (this.windowWidth / 2);
        this.centerY = this.windowY + (this.windowHeight / 2);
    }

    @Override
    protected void initWidgets() {
        int buttonSpacing = 2;
        int buttonWidth = (this.windowWidth / 4) - (buttonSpacing * 2);
        int buttonHeight = 20;
        int buttonX = this.windowX + 6;
        int buttonY = this.windowY;

        buttonX = this.centerX + buttonSpacing;
        int buttonXRight = buttonX + buttonWidth + buttonSpacing;
        buttonY = this.windowY + 39 + buttonSpacing;

        buttonY += buttonHeight + (buttonSpacing * 2);
        this.addRenderableWidget(new ButtonBase(BaseCreatureEntity.GUI_COMMAND.SITTING.id, buttonX, buttonY, buttonWidth * 2, buttonHeight, Component.translatable("..."), this));

        buttonY += buttonHeight + (buttonSpacing * 2);
        this.addRenderableWidget(new ButtonBase(BaseCreatureEntity.GUI_COMMAND.PASSIVE.id, buttonX, buttonY, buttonWidth, buttonHeight, Component.translatable("..."), this));
        this.addRenderableWidget(new ButtonBase(BaseCreatureEntity.GUI_COMMAND.STANCE.id, buttonXRight, buttonY, buttonWidth, buttonHeight, Component.translatable("..."), this));

        buttonY += buttonHeight + (buttonSpacing * 2);
        this.addRenderableWidget(new ButtonBase(BaseCreatureEntity.GUI_COMMAND.PVP.id, buttonX, buttonY, buttonWidth * 2, buttonHeight, Component.translatable("..."), this));

        if (this.hasPets() && this.summoningPedestal.summonSet != null) {
            this.selectMinion(this.summoningPedestal.summonSet.summonType);
        }
        int listWidth = (this.windowWidth / 2) - (buttonSpacing * 4);
        int listHeight = this.windowHeight - (39 + buttonSpacing) - 16;
        int listTop = this.windowY + 39 + buttonSpacing;
        int listBottom = listTop + listHeight;
        int listX = this.windowX + (buttonSpacing * 2);
        this.list = new SummoningPedestalList(this, this.playerExt, listWidth, listHeight, listTop, listBottom, listX);
        this.addRenderableWidget(this.list);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ResourceLocation texture = this.getTexture();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        guiGraphics.blit(texture, this.windowX, this.windowY, 0, 0, this.windowWidth, this.windowHeight);
        guiGraphics.blit(texture, this.windowX + 40, this.windowY + this.windowHeight, 40, 224, this.windowWidth - 80, 29);

        if (!this.hasPets()) {
            return;
        }

        this.drawFuel(guiGraphics);
        this.drawCapacityBar(guiGraphics);
        this.drawProgressBar(guiGraphics);
    }


    @Override
    public void renderWidgets(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (Object buttonObj : this.renderables) {
            if (buttonObj instanceof ButtonBase) {
                ButtonBase button = (ButtonBase) buttonObj;

                // Tab:
                if (button instanceof MainTab) {
                    button.active = true;
                    button.visible = true;
                    continue;
                }

                // Inactive:
                if (!this.hasSelectedPet()) {
                    button.active = false;
                    button.visible = false;
                    continue;
                }

                // Behaviour Buttons:
                if (button.buttonId == BaseCreatureEntity.GUI_COMMAND.SITTING.id)
                    button.setMessage(Component.translatable(Component.translatable("gui.pet.sit").getString() + ": " + (this.summonSet.getSitting() ? Component.translatable("common.yes").getString() : Component.translatable("common.no").getString())));

                if (button.buttonId == BaseCreatureEntity.GUI_COMMAND.PASSIVE.id)
                    button.setMessage(Component.translatable(Component.translatable("gui.pet.passive").getString() + ": " + (this.summonSet.getPassive() ? Component.translatable("common.yes").getString() : Component.translatable("common.no").getString())));

                if (button.buttonId == BaseCreatureEntity.GUI_COMMAND.STANCE.id)
                    button.setMessage(Component.translatable((this.summonSet.getAggressive() ? Component.translatable("gui.pet.aggressive").getString() : Component.translatable("gui.pet.defensive").getString())));

                if (button.buttonId == BaseCreatureEntity.GUI_COMMAND.PVP.id)
                    button.setMessage(Component.translatable(Component.translatable("gui.pet.pvp").getString() + ": " + (this.summonSet.getPVP() ? Component.translatable("common.yes").getString() : Component.translatable("common.no").getString())));
            }
        }

        // Pet List:
        if (this.hasPets()) {
            this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        super.renderWidgets(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.hasPets()) {
            this.drawHelper.drawString(guiGraphics, Component.translatable("gui.beastiary.summoning.empty.title").getString(), this.centerX - 96, this.windowY + 6, 0xFFFFFF);
            this.drawHelper.drawStringWrapped(guiGraphics, Component.translatable("gui.beastiary.summoning.empty.info").getString(), this.windowX + 16, this.windowY + 30, this.windowWidth - 32, 0xFFFFFF, false);
            return;
        }

        this.drawHelper.drawStringCentered(guiGraphics, this.getTitle().getString(), this.centerX, this.windowY + 6, 0xFFFFFF, false);
        this.drawHelper.drawString(guiGraphics, this.getEnergyTitle().getString(), this.windowX + 16, this.windowY + 20, 0xFFFFFF);
    }


    @Override
    public void actionPerformed(int buttonId) {
        // Inactive:
        if (!this.hasSelectedPet()) {
            return;
        }

        // Behaviour Button:
        if (buttonId == BaseCreatureEntity.GUI_COMMAND.SITTING.id)
            this.summonSet.sitting = !this.summonSet.sitting;
        if (buttonId == BaseCreatureEntity.GUI_COMMAND.FOLLOWING.id)
            this.summonSet.following = !this.summonSet.following;
        if (buttonId == BaseCreatureEntity.GUI_COMMAND.PASSIVE.id)
            this.summonSet.passive = !this.summonSet.passive;
        if (buttonId == BaseCreatureEntity.GUI_COMMAND.STANCE.id)
            this.summonSet.aggressive = !this.summonSet.aggressive;
        if (buttonId == BaseCreatureEntity.GUI_COMMAND.PVP.id)
            this.summonSet.pvp = !this.summonSet.pvp;

        if (buttonId < 100) {
            this.sendCommandsToServer();
        }
    }

    /*@Override
    protected void keyPressed(char par1, int par2) {
        if(par2 == 1 || par2 == this.mc.gameSettings.keyBindInventory.getKeyCode())
            this.mc.player.closeScreen();
        super.keyTyped(par1, par2);
    }*/

    public MutableComponent getTitle() {
        return Component.translatable("gui." + "summoningpedestal");
    }

    public MutableComponent getEnergyTitle() {
        return Component.translatable("stat.portal");
    }

    public void drawFuel(GuiGraphics matrixStack) {
        int fuelX = this.windowX + 132;
        int fuelY = this.windowY + 42;
        this.drawHelper.drawTexturedModalRect(matrixStack, fuelX, fuelY, 47, 170, 18, 18);

        int barWidth = 38;
        int barHeight = 11;
        int barX = fuelX + 22;
        int barY = fuelY + 3;
        int barU = 218;
        int barV = 225;
        this.drawHelper.drawTexturedModalRect(matrixStack, barX, barY, barU, barV + barHeight, barWidth, barHeight);

        barWidth = Math.round((float) barWidth * ((float) this.summoningPedestal.summoningFuel / this.summoningPedestal.summoningFuelMax));
        this.drawHelper.drawTexturedModalRect(matrixStack, barX, barY, barU, barV, barWidth, barHeight);
    }

    public void drawCapacityBar(GuiGraphics guiGraphics) {
        int energyBarWidth = 9;
        int energyBarHeight = 9;
        int energyBarX = this.windowX + 16;
        int energyBarY = this.windowY + 40 - energyBarHeight;
        this.drawHelper.drawBar(guiGraphics, TextureManager.getTexture("GUIPetSpiritEmpty"), energyBarX, energyBarY, 0, energyBarWidth, energyBarHeight, 10, 10);
        this.drawHelper.drawBar(guiGraphics, TextureManager.getTexture("GUIPetSpirit"), energyBarX, energyBarY, 0, energyBarWidth, energyBarHeight, this.summoningPedestal.capacity / this.summoningPedestal.capacityCharge, 10);
    }

    public void drawProgressBar(GuiGraphics guiGraphics) {
        int barWidth = (256 / 4) + 16;
        int barHeight = (32 / 4) + 2;
        int barX = this.centerX + 2;
        int barY = this.windowY + 26;
        this.drawHelper.drawTexture(guiGraphics, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);

        float respawnNormal = ((float) this.summoningPedestal.summonProgress / this.summoningPedestal.summonProgressMax);
        this.drawHelper.drawTexture(guiGraphics, TextureManager.getTexture("GUIPetBarRespawn"), barX, barY, 0, respawnNormal, 1, barWidth * respawnNormal, barHeight);
    }

    public void sendCommandsToServer() {
        this.summoningPedestal.sendSummonSetToServer(this.summonSet);
    }

    public void selectMinion(String minionName) {
        if (this.summonSet == null) {
            if (this.summoningPedestal.summonSet == null) {
                this.summoningPedestal.summonSet = new SummonSet(this.playerExt);
                this.summoningPedestal.sendSummonSetToServer(this.summoningPedestal.summonSet);
            }
            this.summonSet = this.summoningPedestal.summonSet;
        }
        this.summonSet.setSummonType(minionName);
        this.sendCommandsToServer();
    }

    public String getSelectedMinionName() {
        if (this.summonSet == null)
            return null;
        return this.summonSet.summonType;
    }

    public boolean hasPets() {
        return this.playerExt.getBeastiary().getSummonableList().size() > 0;
    }

    public boolean hasSelectedPet() {
        return this.hasPets() && this.summonSet != null && !this.summonSet.summonType.equals("");
    }

    protected ResourceLocation getTexture() {
        return TextureManager.getTexture("GUISummoningPedestal");
    }
}
