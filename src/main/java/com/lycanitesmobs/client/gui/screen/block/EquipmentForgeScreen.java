package com.lycanitesmobs.client.gui.screen.block;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.screen.base.BaseContainerScreen;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.container.base.BaseContainer;
import com.lycanitesmobs.core.container.block.EquipmentForgeContainer;
import com.lycanitesmobs.core.container.slot.EquipmentForgeSlot;
import com.lycanitesmobs.core.network.message.MessageTileEntityButton;
import com.lycanitesmobs.core.block.blockentity.TileEntityEquipmentForge;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class EquipmentForgeScreen extends BaseContainerScreen<EquipmentForgeContainer> {
    public TileEntityEquipmentForge equipmentForge;
    public String currentMode = "empty";
    public boolean confirmation = false;

    public EquipmentForgeScreen(EquipmentForgeContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name);
        this.equipmentForge = container.equipmentForge;
    }

    @Override
    public void init() {
        super.init();
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void initWidgets() {
        int backX = (this.width - this.imageWidth) / 2;
        int backY = (this.height - this.imageHeight) / 2;
        int buttonSpacing = 2;
        int buttonWidth = 128;
        int buttonHeight = 20;
        int buttonX = backX + this.imageWidth;
        int buttonY = backY;

        String buttonText = "";
        if ("construct".equals(this.currentMode)) {
            buttonText = Component.translatable("gui.equipmentforge.forge").getString();
        } else if ("deconstruct".equals(this.currentMode)) {
            buttonText = Component.translatable("gui.equipmentforge.deconstruct").getString();
        }
        buttonY += buttonSpacing;
        //this.addButton(new ButtonBase(1, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText, this));
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.imageWidth = 176;
        this.imageHeight = 166;

        int backX = (this.width - this.imageWidth) / 2;
        int backY = (this.height - this.imageHeight) / 2;

        ResourceLocation texture = TextureManager.getTexture("GUIEquipmentForge");

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        guiGraphics.blit(texture, backX, backY, 0, 0, this.imageWidth, this.imageHeight);

        this.drawSlots(guiGraphics, backX, backY);
    }

    /**
     * Draws each Equipment Slot.
     *
     * @param backX
     * @param backY
     */
    protected void drawSlots(GuiGraphics guiGraphics, int backX, int backY) {
        ResourceLocation texture = TextureManager.getTexture("GUIEquipmentForge");

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        BaseContainer container = this.getMenu();
        List<Slot> forgeSlots = container.slots.subList(container.inventoryStart, container.inventoryFinish);
        int slotWidth = 18;
        int slotHeight = 18;
        int slotU = 238;
        int slotVBase = 0;

        for (Slot forgeSlot : forgeSlots) {
            int slotX = backX + forgeSlot.x - 1;
            int slotY = backY + forgeSlot.y - 1;
            int slotV = slotVBase;

            if (forgeSlot instanceof EquipmentForgeSlot equipmentSlot) {
                if ("base".equals(equipmentSlot.type)) {
                    slotV += slotHeight;
                } else if ("head".equals(equipmentSlot.type)) {
                    slotV += slotHeight * 2;
                } else if ("blade".equals(equipmentSlot.type)) {
                    slotV += slotHeight * 3;
                } else if ("axe".equals(equipmentSlot.type)) {
                    slotV += slotHeight * 4;
                } else if ("pike".equals(equipmentSlot.type)) {
                    slotV += slotHeight * 5;
                } else if ("pommel".equals(equipmentSlot.type)) {
                    slotV += slotHeight * 6;
                } else if ("jewel".equals(equipmentSlot.type)) {
                    slotV += slotHeight * 7;
                } else if ("aura".equals(equipmentSlot.type)) {
                    slotV += slotHeight * 8;
                }
            }

            guiGraphics.blit(texture, slotX, slotY, slotU, slotV, slotWidth, slotHeight);
        }
    }


    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.drawHelper.drawString(
                guiGraphics,
                this.playerInventoryTitle.getString(),
                this.leftPos + 8,
                this.topPos + this.imageHeight - 96 + 2,
                4210752
        );
    }

    @Override
    public void actionPerformed(int buttonid) {
        MessageTileEntityButton message = new MessageTileEntityButton(buttonid, this.equipmentForge.getBlockPos());
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
    }
}
