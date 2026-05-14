package com.lycanitesmobs.client.gui.screen.block;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.screen.base.BaseContainerScreen;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.container.base.BaseContainer;
import com.lycanitesmobs.core.container.block.EquipmentStationContainer;
import com.lycanitesmobs.core.container.slot.EquipmentStationRepairSlot;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.network.message.MessageTileEntityButton;
import com.lycanitesmobs.core.block.blockentity.EquipmentStationTileEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


import java.util.List;

public class EquipmentStationScreen extends BaseContainerScreen<EquipmentStationContainer> {
    public EquipmentStationTileEntity equipmentStation;

    public EquipmentStationScreen(EquipmentStationContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name);
        this.equipmentStation = container.equipmentStation;
    }

    @Override
    public void init() {
        super.init();
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void initWidgets() {

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

            if (forgeSlot instanceof EquipmentStationRepairSlot) {
                slotV += slotHeight * 9;
            }

            guiGraphics.blit(texture, slotX, slotY, slotU, slotV, slotWidth, slotHeight);
        }
    }


    @Override
    protected void renderForeground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.drawHelper.drawString(matrixStack, this.playerInventoryTitle.getString(), this.leftPos + 8, this.topPos + this.imageHeight - 96 + 2, 4210752);
        int backX = (this.width - this.imageWidth) / 2;
        int backY = (this.height - this.imageHeight) / 2;
        this.drawBars(matrixStack, backX, backY);
    }

    protected void drawBars(GuiGraphics matrixStack, int backX, int backY) {
        int barWidth = 100;
        int barHeight = 11;
        int barX = (this.width / 2) - (barWidth / 2);
        int barY = backY + 48;
        int manaBarY = barY + 12;
        int barCenter = barX + (barWidth / 2);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, manaBarY, 0, 1, 1, barWidth, barHeight);
        ResourceLocation forgeTexture = TextureManager.getTexture("GUIEquipmentForge");

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, forgeTexture);

        matrixStack.blit(forgeTexture, barX - 14, barY, 225, 158, 13, 10);
        matrixStack.blit(forgeTexture, barX - 14, manaBarY, 225, 170, 13, 10);

        ItemStack partStack = this.equipmentStation.getItem(1);
        if (!(partStack.getItem() instanceof ItemEquipment)) {
            return;
        }
        ItemEquipment equipmentItem = (ItemEquipment) partStack.getItem();

        // Sharpness:
        int sharpness = equipmentItem.getSharpness(partStack);
        int sharpnessMax = ItemEquipment.SHARPNESS_MAX;
        float sharpnessNormal = (float) sharpness / sharpnessMax;
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarHealth"), barX, barY, 0, sharpnessNormal, 1, barWidth * sharpnessNormal, barHeight);
        String sharpnessText = sharpness + "/" + sharpnessMax;
        this.drawHelper.drawString(matrixStack, sharpnessText, barCenter - (this.drawHelper.getStringWidth(sharpnessText) / 2), barY + 2, 0xFFFFFF, true);

        // Mana:
        int mana = equipmentItem.getMana(partStack);
        int manaMax = ItemEquipment.SHARPNESS_MAX;
        float manaNormal = (float) mana / manaMax;
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, manaBarY, 0, 1, 1, barWidth, barHeight);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarRespawn"), barX, manaBarY, 0, manaNormal, 1, barWidth * manaNormal, barHeight);
        String manaText = mana + "/" + manaMax;
        this.drawHelper.drawString(matrixStack, manaText, barCenter - (this.drawHelper.getStringWidth(manaText) / 2), manaBarY + 2, 0xFFFFFF, true);
    }

    @Override
    public void actionPerformed(int buttonid) {
        MessageTileEntityButton message = new MessageTileEntityButton(buttonid, this.equipmentStation.getBlockPos());
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
    }
}
