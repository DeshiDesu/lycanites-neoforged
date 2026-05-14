package com.lycanitesmobs.client.gui.screen.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.screen.base.BaseContainerScreen;
import com.lycanitesmobs.client.gui.screen.base.BaseGui;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.container.base.BaseContainer;
import com.lycanitesmobs.core.container.creature.CreatureContainer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.container.creature.CreatureInventory;
import com.lycanitesmobs.core.network.message.MessageEntityGUICommand;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class CreatureInventoryScreen extends BaseContainerScreen<CreatureContainer> {
    public BaseCreatureEntity creature;
    public CreatureInventory creatureInventory;

    /**
     * Constructor
     */
    public CreatureInventoryScreen(CreatureContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name);
        this.creature = container.creature;
        this.creatureInventory = container.creature.inventory;
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

        if (!(this.creature instanceof TameableCreatureEntity)) {
            return;
        }

        TameableCreatureEntity pet = (TameableCreatureEntity) this.creature;
        if (!pet.petControlsEnabled()) {
            return;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int buttonSpacing = 2;
        int buttonWidth = 128;
        int buttonHeight = 20;
        int buttonX = backX + this.imageWidth;
        int buttonY = backY;

        buttonY += buttonSpacing;
        Button button = new ButtonBase(
                BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id,
                buttonX + buttonSpacing,
                buttonY,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.pet.follow"),
                this
        );
        if (pet.isFollowing() && !pet.isSitting()) {
            button.active = false;
        }
        this.addRenderableWidget(button);

        buttonY += buttonHeight + (buttonSpacing * 2);
        button = new ButtonBase(
                BaseCreatureEntity.PET_COMMAND_ID.WANDER.id,
                buttonX + buttonSpacing,
                buttonY,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.pet.wander"),
                this
        );
        if (!pet.isFollowing() && !pet.isSitting()) {
            button.active = false;
        }
        this.addRenderableWidget(button);

        buttonY += buttonHeight + (buttonSpacing * 2);
        button = new ButtonBase(
                BaseCreatureEntity.PET_COMMAND_ID.SIT.id,
                buttonX + buttonSpacing,
                buttonY,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.pet.sit"),
                this
        );
        if (!pet.isFollowing() && pet.isSitting()) {
            button.active = false;
        }
        this.addRenderableWidget(button);

        buttonY += buttonHeight + (buttonSpacing * 2);
        button = new ButtonBase(
                BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id,
                buttonX + buttonSpacing,
                buttonY,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.pet.passive"),
                this
        );
        if (pet.isPassive()) {
            button.active = false;
        }
        this.addRenderableWidget(button);

        buttonY += buttonHeight + (buttonSpacing * 2);
        button = new ButtonBase(
                BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id,
                buttonX + buttonSpacing,
                buttonY,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.pet.defensive"),
                this
        );
        if (!pet.isPassive() && !pet.isAssisting() && !pet.isAggressive()) {
            button.active = false;
        }
        this.addRenderableWidget(button);

        buttonY += buttonHeight + (buttonSpacing * 2);
        button = new ButtonBase(
                BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id,
                buttonX + buttonSpacing,
                buttonY,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.pet.assist"),
                this
        );
        if (!pet.isPassive() && pet.isAssisting() && !pet.isAggressive()) {
            button.active = false;
        }
        this.addRenderableWidget(button);

        buttonY += buttonHeight + (buttonSpacing * 2);
        button = new ButtonBase(
                BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id,
                buttonX + buttonSpacing,
                buttonY,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.pet.aggressive"),
                this
        );
        if (!pet.isPassive() && pet.isAggressive()) {
            button.active = false;
        }
        this.addRenderableWidget(button);

        buttonY += buttonHeight + (buttonSpacing * 2);
        Component pvpLabel = Component.translatable("gui.pet.pvp")
                .append(": ")
                .append(pet.isPVP() ? Component.translatable("common.yes") : Component.translatable("common.no"));
        this.addRenderableWidget(new ButtonBase(
                BaseCreatureEntity.PET_COMMAND_ID.PVP.id,
                buttonX + buttonSpacing,
                buttonY,
                buttonWidth,
                buttonHeight,
                pvpLabel,
                this
        ));
    }

    @Override
    protected void renderForeground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.creature == null || this.creature.isDeadOrDying()) {
            this.onClose();
        }

        this.drawHelper.drawString(matrixStack, this.playerInventoryTitle.getString(), this.leftPos + 8, this.topPos + this.imageHeight - 96 + 2, 4210752);
        int backX = (this.width - this.imageWidth) / 2;
        int backY = (this.height - this.imageHeight) / 2;
        this.drawBars(matrixStack, backX, backY);
    }

    protected void drawBars(GuiGraphics matrixStack, int backX, int backY) {
        int barWidth = 100;
        int barHeight = 11;
        int barX = backX - barWidth;
        int barY = backY + 54 + 18;
        int barCenter = barX + (barWidth / 2);

        // Health Bar:
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
        float healthNormal = Math.min(1, this.creature.getHealth() / this.creature.getMaxHealth());
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarHealth"), barX, barY, 0, healthNormal, 1, barWidth * healthNormal, barHeight);
        String healthText = Component.translatable("entity.health").getString() + ": " + String.format("%.0f", this.creature.getHealth()) + "/" + String.format("%.0f", this.creature.getMaxHealth());
        this.drawHelper.draw(matrixStack, healthText, barCenter - ((float) this.drawHelper.getStringWidth(healthText) / 2), barY + 2, 0xFFFFFF);
        barY += barHeight + 1;

        // XP Bar:
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
        float experienceNormal = Math.min(1, (float) this.creature.getExperience() / this.creature.creatureStats.getExperienceForNextLevel());
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIBarExperience"), barX, barY, 0, experienceNormal, 1, barWidth * experienceNormal, barHeight);
        String experienceText = Component.translatable("entity.experience").getString() + ": " + this.creature.getExperience() + "/" + this.creature.creatureStats.getExperienceForNextLevel();
        this.drawHelper.draw(matrixStack, experienceText, barCenter - ((float) this.drawHelper.getStringWidth(experienceText) / 2), barY + 2, 0xFFFFFF);
        barY += barHeight + 1;

        // Level:
        String levelText = Component.translatable("entity.level").getString() + ": " + this.creature.getMobLevel();
        this.drawHelper.draw(matrixStack, levelText, barCenter - ((float) this.drawHelper.getStringWidth(levelText) / 2), barY + 2, 0xFFFFFF);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        this.imageWidth = 176;
        this.imageHeight = 166;

        int backX = (this.width - this.imageWidth) / 2;
        int backY = (this.height - this.imageHeight) / 2;

        // Main GUI background (exactly like 1.16, but via GuiGraphics)
        guiGraphics.blit(
                TextureManager.getTexture("GUIInventoryCreature"),
                backX,
                backY,
                0, 0,
                this.imageWidth,
                this.imageHeight,
                256, 256
        );

        this.drawFrames(guiGraphics, backX, backY, mouseX, mouseY);
        this.drawSlots(guiGraphics, backX, backY);
    }

    protected void drawFrames(GuiGraphics guiGraphics, int backX, int backY, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        var tex = TextureManager.getTexture("GUIInventoryCreature");

        // Status Frame:
        int statusWidth = 90;
        int statusHeight = 54;
        guiGraphics.blit(
                tex,
                backX + 79,
                backY + 17,
                0,
                256 - statusHeight,
                statusWidth,
                statusHeight,
                256, 256
        );

        // Creature Frame:
        int creatureFrameWidth = 54;
        int creatureFrameHeight = 54;
        guiGraphics.blit(
                tex,
                backX - creatureFrameWidth + 1,
                backY + 17,
                statusWidth,
                256 - creatureFrameHeight,
                creatureFrameWidth,
                creatureFrameHeight,
                256, 256
        );

        double creatureWidth = creature.getBbWidth();
        double creatureHeight = creature.getBbHeight();
        int scale = (int) Math.round((2.5F / Math.max(creatureWidth, creatureHeight)) * 10);

        BaseGui.renderLivingEntity(
                guiGraphics,
                backX + 26 - creatureFrameWidth + 1,
                backY + 60,
                scale,
                (float) backX - mouseX,
                (float) backY - mouseY,
                this.creature
        );
    }

    protected void drawSlots(GuiGraphics guiGraphics, int backX, int backY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        var tex = TextureManager.getTexture("GUIInventoryCreature");

        BaseContainer container = this.getMenu();
        List<Slot> creatureSlots = container.slots.subList(container.specialStart, container.inventoryFinish + 1);
        int slotWidth = 18;
        int slotHeight = 18;
        int slotU = 238;
        int slotVBase = 0;

        for (Slot creatureSlot : creatureSlots) {
            int slotX = backX + creatureSlot.x - 1;
            int slotY = backY + creatureSlot.y - 1;
            int slotV = slotVBase;

            String slotType = creatureInventory.getTypeFromSlot(creatureSlot.getSlotIndex());
            if (slotType != null) {
                if (slotType.equals("saddle")) {
                    slotV += slotHeight;
                } else if (slotType.equals("bag")) {
                    slotV += slotHeight * 2;
                } else if (slotType.equals("chest")) {
                    slotV += slotHeight * 3;
                }
            }

            guiGraphics.blit(
                    tex,
                    slotX,
                    slotY,
                    slotU,
                    slotV,
                    slotWidth,
                    slotHeight,
                    256, 256
            );
        }
    }


    @Override
    public void actionPerformed(int buttonId) {
        MessageEntityGUICommand message = new MessageEntityGUICommand(buttonId, this.creature);
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
    }
}
