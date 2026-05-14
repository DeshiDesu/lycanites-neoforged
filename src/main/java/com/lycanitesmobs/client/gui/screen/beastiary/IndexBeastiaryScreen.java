package com.lycanitesmobs.client.gui.screen.beastiary;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.screen.beastiary.lists.BeastiaryIndexList;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.util.VersionChecker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.net.URI;
import java.net.URISyntaxException;

public class IndexBeastiaryScreen extends BeastiaryScreen {
    public BeastiaryIndexList indexList;
    VersionChecker.VersionInfo versionInfo;

    public IndexBeastiaryScreen(Player player) {
        super(player);
        this.versionInfo = VersionChecker.INSTANCE.getLatestVersion();
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();

        int menuWidth = this.colRightWidth;

        int buttonCount = 6;
        int buttonPadding = 2;
        int buttonWidth = Math.round((float) (menuWidth / buttonCount)) - buttonPadding;
        int buttonWidthPadded = buttonWidth + buttonPadding;
        int buttonHeight = 20;
        int buttonX = this.colRightX + buttonPadding;
        int buttonY = this.colRightY + this.colRightHeight - buttonHeight;
        ButtonBase button;

        button = new ButtonBase(100, buttonX, buttonY, buttonWidth, buttonHeight, Component.translatable("Website"), this);
        this.addRenderableWidget(button);
        button = new ButtonBase(101, buttonX + buttonWidthPadded, buttonY, buttonWidth, buttonHeight, Component.translatable("Twitter"), this);
        this.addRenderableWidget(button);
        button = new ButtonBase(102, buttonX + (buttonWidthPadded * 2), buttonY, buttonWidth, buttonHeight, Component.translatable("Patreon"), this);
        this.addRenderableWidget(button);
        button = new ButtonBase(103, buttonX + (buttonWidthPadded * 3), buttonY, buttonWidth, buttonHeight, Component.translatable("Guilded"), this);
        this.addRenderableWidget(button);
        button = new ButtonBase(104, buttonX + (buttonWidthPadded * 4), buttonY, buttonWidth, buttonHeight, Component.translatable("Discord"), this);
        this.addRenderableWidget(button);
        button = new ButtonBase(105, buttonX + (buttonWidthPadded * 5), buttonY, buttonWidth, buttonHeight, Component.translatable("Shard"), this);
        this.addRenderableWidget(button);

        if (this.versionInfo != null) {
            String info = Component.translatable("gui.beastiary.index.description").getString();
            int yOffset = this.colRightY + 13;
            yOffset += this.drawHelper.getWordWrappedHeight(info, this.colRightWidth);

            String version = "\n\u00A7l" + Component.translatable("gui.beastiary.index.version").getString() + ": \u00A7r";
            if (this.versionInfo.isNewer) {
                version += "\u00A74";
            }
            version += LycanitesMobs.versionNumber + "\u00A7r";
            if (this.versionInfo.isNewer) {
                version += " \u00A7l" + Component.translatable("gui.beastiary.index.version.newer").getString()
                        + ": \u00A7r\u00A72" + this.versionInfo.versionNumber + "\u00A7r";
            }

            int versionHeight = this.drawHelper.getWordWrappedHeight(version, this.colRightWidth);
            int listTop = yOffset + versionHeight + 4;

            this.indexList = new BeastiaryIndexList(
                    this,
                    this.colRightWidth,
                    this.colRightHeight,
                    listTop,
                    buttonY - buttonPadding,
                    this.colRightX + 2,
                    this.versionInfo
            );
            this.addRenderableWidget(this.indexList);
        }
    }


    @Override
    public void renderBackground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderWidgets(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidgets(matrixStack, mouseX, mouseY, partialTicks);
        if (this.indexList != null)
            this.indexList.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderForeground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(matrixStack, mouseX, mouseY, partialTicks);

        int yOffset = this.colRightY + 13;
        String info = Component.translatable("gui.beastiary.index.description").getString();
        this.drawHelper.drawStringWrapped(matrixStack, info, this.colRightX + 1, yOffset, this.colRightWidth, 0xFFFFFF, true);
        yOffset += this.drawHelper.getWordWrappedHeight(info, this.colRightWidth);

        if (this.versionInfo == null)
            return;

        // Check Mod Version:
        String version = "\n\u00A7l" + Component.translatable("gui.beastiary.index.version").getString() + ": \u00A7r";
        if (this.versionInfo.isNewer) {
            version += "\u00A74";
        }
        version += LycanitesMobs.versionNumber + "\u00A7r";
        if (this.versionInfo.isNewer) {
            version += " \u00A7l" + Component.translatable("gui.beastiary.index.version.newer").getString() + ": \u00A7r\u00A72" + this.versionInfo.versionNumber + "\u00A7r";
        }
        this.drawHelper.drawStringWrapped(matrixStack, version, this.colRightX + 1, yOffset, this.colRightWidth, 0xFFFFFF, true);
    }

    @Override
    public void actionPerformed(int buttonId) {
        if (buttonId == 100) {
            try {
                this.openURI(new URI(LycanitesMobs.website));
            } catch (URISyntaxException e) {
            }
        }
        if (buttonId == 101) {
            try {
                this.openURI(new URI(LycanitesMobs.twitter));
            } catch (URISyntaxException e) {
            }
        }
        if (buttonId == 102) {
            try {
                this.openURI(new URI(LycanitesMobs.patreon));
            } catch (URISyntaxException e) {
            }
        }
        if (buttonId == 103) {
            try {
                this.openURI(new URI(LycanitesMobs.guilded));
            } catch (URISyntaxException e) {
            }
        }
        if (buttonId == 104) {
            try {
                this.openURI(new URI(LycanitesMobs.discord));
            } catch (URISyntaxException e) {
            }
        }
        if (buttonId == 105) {
            try {
                this.openURI(new URI("https://shardtcg.com"));
            } catch (URISyntaxException e) {
            }
        }
        super.actionPerformed(buttonId);
    }

    @Override
    public MutableComponent getTitle() {
        return Component.translatable("gui.beastiary.index.title");
    }
}
