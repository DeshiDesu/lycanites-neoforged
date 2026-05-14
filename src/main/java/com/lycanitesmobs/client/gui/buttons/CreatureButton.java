package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class CreatureButton extends ButtonBase {
    public CreatureInfo creatureInfo;
    public int summonSetId = 0;

    public CreatureButton(int buttonID, int x, int y, int w, int h, MutableComponent text, int summonSetId, CreatureInfo creatureInfo, Button.OnPress pressable) {
        super(buttonID, x, y, w, h, text, pressable);
        this.summonSetId = summonSetId;
        this.creatureInfo = creatureInfo;
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        super.renderWidget(g, mouseX, mouseY, partialTicks);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        int buttonX = this.getX();
        int buttonY = this.getY();

        ResourceLocation bg = TextureManager.getTexture("GUIInventoryCreature");
        int stateVOffset = 0;
        if (this.isHovered()) stateVOffset = 64;
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(Minecraft.getInstance().player);
        if (playerExt != null && playerExt.selectedSummonSet == this.summonSetId) stateVOffset = 32;

        g.blit(bg, buttonX, buttonY, 193, 187 - stateVOffset, this.width, this.height);

        if (this.creatureInfo != null) {
            ResourceLocation icon = this.creatureInfo.getIcon();
            g.blit(icon, buttonX + 8, buttonY + 8, 0, 0, 16, 16, 16, 16);
        }

        int textColor = 14737632;
        if (!this.active) textColor = -6250336;
        else if (this.isHovered()) textColor = 16777120;

        this.drawHelper.drawCenteredString(g, this.drawHelper.getFontRenderer(), this.getMessage(), buttonX + 5, buttonY + 2, textColor);
    }

}
