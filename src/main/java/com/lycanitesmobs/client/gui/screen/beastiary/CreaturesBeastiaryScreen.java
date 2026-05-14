package com.lycanitesmobs.client.gui.screen.beastiary;

import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.client.gui.screen.beastiary.lists.CreatureDescriptionList;
import com.lycanitesmobs.client.gui.screen.beastiary.lists.CreatureList;
import com.lycanitesmobs.client.gui.screen.beastiary.lists.CreatureTypeList;
import com.lycanitesmobs.client.gui.screen.beastiary.lists.SubspeciesList;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.data.info.creature.Subspecies;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class CreaturesBeastiaryScreen extends BeastiaryScreen {
    public CreatureTypeList creatureTypeList;
    public CreatureList creatureList;
    public SubspeciesList subspeciesList;
    public CreatureDescriptionList descriptionList;

    public CreaturesBeastiaryScreen(Player player) {
        super(player);
    }

    @Override
    public void initWidgets() {
        super.initWidgets();

        this.creatureTypeList = new CreatureTypeList(this, this.colLeftWidth, this.colLeftHeight,
                this.colLeftY, this.colLeftY + this.colLeftHeight, this.colLeftX);
        this.addRenderableWidget(this.creatureTypeList);

        int selectionListsWidth = this.getScaledX(300F / 1920F);

        int creatureListY = this.colRightY;
        int creatureListHeight = Math.round((float) this.colRightHeight * 0.6f);
        this.creatureList = new CreatureList(CreatureList.Type.KNOWLEDGE, this, this.creatureTypeList,
                selectionListsWidth, creatureListHeight,
                creatureListY, creatureListY + creatureListHeight, this.colRightX);
        this.addRenderableWidget(this.creatureList);

        int subspeciesListY = creatureListY + 2 + creatureListHeight;
        int subspeciesListHeight = Math.round((float) this.colRightHeight * 0.4f) - 2;
        this.subspeciesList = new SubspeciesList(this, false,
                selectionListsWidth, subspeciesListHeight,
                subspeciesListY, subspeciesListY + subspeciesListHeight, this.colRightX);
        this.addRenderableWidget(this.subspeciesList);

        int newLine = this.drawHelper.getWordWrappedHeight("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                this.colRightWidth - selectionListsWidth) + 2;
        int descriptionListY = this.colRightY + (newLine * 3);
        this.descriptionList = new CreatureDescriptionList(this,
                this.colRightWidth - selectionListsWidth, this.colRightHeight,
                descriptionListY, this.colRightY + this.colRightHeight,
                this.colRightX + selectionListsWidth + 2);
        this.addRenderableWidget(this.descriptionList);
    }


    @Override
    public void renderBackground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(matrixStack, mouseX, mouseY, partialTicks);

        // Selected Creature Model:
        if (this.playerExt.selectedCreature != null) {
            int marginX = this.getScaledX(240F / 1920F) + 8;
            this.renderCreature(matrixStack, this.playerExt.selectedCreature, this.colRightX + (marginX / 2) + (this.colRightWidth / 2), this.colRightY + (this.colRightHeight / 2), mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidgets(guiGraphics, mouseX, mouseY, partialTicks);

        if (!this.playerExt.getBeastiary().creatureKnowledgeList.isEmpty()
                && this.playerExt.selectedCreature != null
                && this.descriptionList != null) {
            this.descriptionList.setCreatureKnowledge(
                    this.playerExt.beastiary.getCreatureKnowledge(
                            this.playerExt.selectedCreature.getName()
                    )
            );
        }
    }

    @Override
    public void renderForeground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(matrixStack, mouseX, mouseY, partialTicks);

        int marginX = this.getScaledX(280F / 1920F) + 8;
        int nextX = this.colRightX + marginX;
        int nextY = this.colRightY;
        int width = this.colRightWidth - marginX;

        // No Creatures:
        if (this.playerExt.getBeastiary().creatureKnowledgeList.isEmpty()) {
            nextY += this.drawHelper.getWordWrappedHeight("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA", this.colRightWidth) + 2;
            String text = Component.translatable("gui.beastiary.creatures.empty.info").getString();
            this.drawHelper.drawStringWrapped(matrixStack, text, this.colRightX, nextY, this.colRightWidth, 0xFFFFFF, true);
            return;
        }

        // Creature Display:
        if (this.playerExt.selectedCreature != null) {
            Subspecies subspecies = this.playerExt.selectedCreature.getSubspecies(this.playerExt.selectedSubspecies);

            // Model:
            CreatureInfo creatureInfo = this.playerExt.selectedCreature;
            CreatureKnowledge creatureKnowledge = this.playerExt.beastiary.getCreatureKnowledge(this.playerExt.selectedCreature.getName());

            // Element:
            String text = "\u00A7l" + Component.translatable("creature.stat.element").getString() + ": " + "\u00A7r";
            text += creatureInfo.elements != null ? creatureInfo.getElementNames(subspecies).getString() : "None";
            this.drawHelper.drawStringWrapped(matrixStack, text, nextX, nextY, width, 0xFFFFFF, true);

            // Level:
            nextY += 2 + this.drawHelper.getWordWrappedHeight(text, width);
            text = "\u00A7l" + Component.translatable("creature.stat.cost").getString() + ": " + "\u00A7r";
            this.drawHelper.drawStringWrapped(matrixStack, text, nextX, nextY, width, 0xFFFFFF, true);
            this.drawLevel(matrixStack, creatureInfo, TextureManager.getTexture("GUIPetLevel"), nextX + this.drawHelper.getStringWidth(text), nextY);

            // Knowledge Rank:
            nextY += 2 + this.drawHelper.getWordWrappedHeight(text, width);
            text = "\u00A7l" + Component.translatable("creature.stat.knowledge").getString() + ": " + "\u00A7r";
            int rankX = nextX + this.drawHelper.getStringWidth(text);
            int barX = rankX + 29;
            int barWidth = (256 / 4) + 16;
            int barHeight = (32 / 4) + 2;
            int barCenter = barX + (barWidth / 2);

            this.drawHelper.drawStringWrapped(matrixStack, text, nextX, nextY, width, 0xFFFFFF, true);
            this.drawHelper.drawBar(matrixStack, TextureManager.getTexture("GUIPetSpiritEmpty"), rankX, nextY, 0, 9, 9, 3, 10);
            this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, nextY, 0, 1, 1, barWidth, barHeight);

            if (creatureKnowledge != null) {
                this.drawHelper.drawBar(matrixStack, TextureManager.getTexture("GUIPetSpiritUsed"), rankX, nextY, 0, 9, 9, creatureKnowledge.rank, 10);
                float experienceNormal = 1;
                if (creatureKnowledge.getMaxExperience() > 0) {
                    experienceNormal = (float) creatureKnowledge.experience / creatureKnowledge.getMaxExperience();
                }
                this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIBarExperience"), barX, nextY, 0, experienceNormal, 1, barWidth * experienceNormal, barHeight);
                String experienceText = "100%";
                if (creatureKnowledge.getMaxExperience() > 0) {
                    experienceText = creatureKnowledge.experience + "/" + creatureKnowledge.getMaxExperience();
                }
                this.drawHelper.draw(matrixStack, experienceText, barCenter - ((float) this.drawHelper.getStringWidth(experienceText) / 2), nextY + 2, 0xFFFFFF);
            }
        }

        // Creature Type Display:
        else if (this.playerExt.selectedCreatureType != null) {
            // Description:
            nextY += this.drawHelper.getWordWrappedHeight("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA", this.colRightWidth) + 2;
            String text = this.playerExt.selectedCreatureType.getTitle().getString();
            this.drawHelper.drawStringWrapped(matrixStack, text, nextX, nextY, width, 0xFFFFFF, true);

            // Discovered:
            nextY += 12 + this.drawHelper.getFontRenderer().wordWrapHeight(text, colRightWidth);
            text = Component.translatable("gui.beastiary.creatures.discovered").getString() + ": ";
            text += this.playerExt.getBeastiary().getCreaturesDiscovered(this.playerExt.selectedCreatureType);
            text += "/" + this.playerExt.selectedCreatureType.creatures.size();
            this.drawHelper.drawString(matrixStack, text, nextX, nextY, 0xFFFFFF, true);
        }

        // Base Display:
        else {
            nextY += this.drawHelper.getWordWrappedHeight("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA", this.colRightWidth) + 2;
            String text = Component.translatable("gui.beastiary.creatures.select").getString();
            this.drawHelper.drawStringWrapped(matrixStack, text, this.colRightX, nextY, this.colRightWidth, 0xFFFFFF, true);
        }
    }

    @Override
    public MutableComponent getTitle() {
        if (this.creatureList != null && this.playerExt.selectedCreature != null) {
            return Component.literal("");
            //return this.playerExt.selectedCreature.getTitle();
        }
        if (this.creatureTypeList != null && this.playerExt.selectedCreatureType != null) {
            return this.playerExt.selectedCreatureType.getTitle();
        }
        if (this.playerExt.getBeastiary().creatureKnowledgeList.isEmpty()) {
            return Component.translatable("gui.beastiary.creatures.empty.title");
        }
        return Component.translatable("gui.beastiary.creatures");
    }
}
