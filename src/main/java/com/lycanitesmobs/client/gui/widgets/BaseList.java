package com.lycanitesmobs.client.gui.widgets;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.DrawHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public abstract class BaseList<S> extends AbstractSelectionList<BaseListEntry> {
    public DrawHelper drawHelper;
    public S screen;

    public BaseList(S screen, int width, int height, int top, int bottom, int left, int slotHeight) {
        super(Minecraft.getInstance(), width, height, top, bottom, slotHeight);
        Minecraft minecraft = Minecraft.getInstance();
        this.drawHelper = new DrawHelper(minecraft, minecraft.font);
        this.setLeftPos(left);
        this.screen = screen;
        this.createEntries();
    }

    public BaseList(S screen, int width, int height, int top, int bottom, int left) {
        this(screen, width, height, top, bottom, left, 28);
    }

    @Override
    public int getRowWidth() {
        return this.getWidth();
    }

    protected int getScrollbarWidth() {
        return 6;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRight() - this.getScrollbarWidth();
    }

    /**
     * Creates all List Entries for this List Widget.
     */
    public void createEntries() {
    }

    /**
     * Returns the index of the selected entry.
     *
     * @return The selected entry index, defaults to 0 if none are selected.
     */
    public int getSelectedIndex() {
        if (this.getSelected() != null)
            return this.getSelected().index;
        return 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = guiGraphics.pose();

        try {
            this.renderBackground(guiGraphics);

            double scaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
            int scissorX = (int) ((float) this.getLeft() * scaleFactor);
            int scissorTop = Minecraft.getInstance().getWindow().getScreenHeight() - (int) ((float) this.getTop() * scaleFactor);
            int scissorBottom = Minecraft.getInstance().getWindow().getScreenHeight() - (int) ((float) this.getBottom() * scaleFactor);
            int scissorWidth = (int) ((float) this.getWidth() * scaleFactor);
            int scissorHeight = scissorTop - scissorBottom;

            RenderSystem.enableScissor(scissorX, scissorBottom, scissorWidth, scissorHeight);

            RenderSystem.disableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.resetTextureMatrix();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ZERO,
                    GlStateManager.DestFactor.ONE
            );
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            Matrix4f matrix = matrixStack.last().pose();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(matrix, (float) this.x0, (float) this.y1, 0.0F).color(0, 0, 0, 64).endVertex();
            bufferbuilder.vertex(matrix, (float) this.x1, (float) this.y1, 0.0F).color(0, 0, 0, 64).endVertex();
            bufferbuilder.vertex(matrix, (float) this.x1, (float) this.y0, 0.0F).color(0, 0, 0, 64).endVertex();
            bufferbuilder.vertex(matrix, (float) this.x0, (float) this.y0, 0.0F).color(0, 0, 0, 64).endVertex();
            tessellator.end();

            int listLeft = this.getRowLeft();
            int listTop = this.y0 + 4 - (int) this.getScrollAmount();
            if (this.isFocused()) {
                this.renderHeader(guiGraphics, listLeft, listTop);
            }

            try {
                this.renderList(guiGraphics, mouseX, mouseY, partialTicks);
            } catch (Exception e) {
                LMHelperClass.logError("BaseList renderList error: " + e.getMessage());
            }

            int maxScroll = this.getMaxScroll();
            if (maxScroll > 0) {
                int trackHeight = this.y1 - this.y0;
                int handleHeight = (int) ((float) trackHeight * trackHeight / (float) this.getMaxPosition());
                handleHeight = LMHelperClass.convertToInteger(LMHelperClass.clamp(handleHeight, 32, trackHeight - 8));
                int handleY = (int) this.getScrollAmount() * (trackHeight - handleHeight) / maxScroll + this.y0;
                if (handleY < this.y0) {
                    handleY = this.y0;
                }

                int scrollbarLeft = this.getScrollbarPosition();
                int scrollbarRight = scrollbarLeft + this.getScrollbarWidth();

                RenderSystem.setShader(GameRenderer::getPositionColorShader);

                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(matrix, (float) scrollbarLeft, (float) this.y1, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) scrollbarRight, (float) this.y1, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) scrollbarRight, (float) this.y0, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) scrollbarLeft, (float) this.y0, 0.0F).color(0, 0, 0, 255).endVertex();
                tessellator.end();

                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(matrix, (float) scrollbarLeft, (float) (handleY + handleHeight), 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) scrollbarRight, (float) (handleY + handleHeight), 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) scrollbarRight, (float) handleY, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) scrollbarLeft, (float) handleY, 0.0F).color(128, 128, 128, 255).endVertex();
                tessellator.end();

                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(matrix, (float) scrollbarLeft, (float) (handleY + handleHeight - 1), 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) (scrollbarRight - 1), (float) (handleY + handleHeight - 1), 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) (scrollbarRight - 1), (float) handleY, 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) scrollbarLeft, (float) handleY, 0.0F).color(192, 192, 192, 255).endVertex();
                tessellator.end();
            }

            this.renderDecorations(guiGraphics, mouseX, mouseY);
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.disableScissor();
        }
    }


    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {
    }
}
