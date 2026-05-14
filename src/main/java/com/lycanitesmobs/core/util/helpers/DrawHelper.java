package com.lycanitesmobs.core.util.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public class DrawHelper {
    protected Minecraft minecraft;
    protected Font fontRenderer;

    public DrawHelper(Minecraft minecraft, Font fontRenderer) {
        this.minecraft = minecraft;
        this.fontRenderer = fontRenderer;
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public Font getFontRenderer() {
        return this.fontRenderer;
    }

    public void drawString(GuiGraphics guiGraphics, String text, float x, float y, int color, boolean shadow) {
        guiGraphics.drawString(this.getFontRenderer(), text, (int) x, (int) y, color, shadow);
    }

    public void drawString(GuiGraphics guiGraphics, String text, int x, int y, int color) {
        this.drawString(guiGraphics, text, x, y, color, false);
    }

    public void drawCenteredString(GuiGraphics g, Font f, Component text, int x, int y, int color) {
        g.drawString(f, text, x - f.width(text) / 2, y, color, false);
    }

    public void drawStringCentered(GuiGraphics matrixStack, String text, int x, int y, int color, boolean shadow) {
        int textWidth = this.getStringWidth(text);
        int textOffset = textWidth / 2;
        this.drawString(matrixStack, text, x - textOffset, y, color, shadow);
    }

    public int draw(GuiGraphics guiGraphics, String string, float x, float y, int color) {
        guiGraphics.drawString(this.getFontRenderer(), string, (int) x, (int) y, color, false);
        return this.getStringWidth(string);
    }

    public int drawShadow(GuiGraphics guiGraphics, String string, float x, float y, int color) {
        guiGraphics.drawString(this.getFontRenderer(), string, (int) x, (int) y, color, true);
        return this.getStringWidth(string);
    }

    public void drawStringWrapped(GuiGraphics g, String text, int x, int y, int wrapWidth, int color, boolean shadow) {
        List<FormattedCharSequence> lines = this.getFontRenderer().split(Component.literal(text), wrapWidth);
        int lineY = y;
        for (FormattedCharSequence line : lines) {
            g.drawString(this.getFontRenderer(), line, x, lineY, color, shadow);
            lineY += 10;
        }
    }

    public int getStringWidth(String text) {
        return this.getFontRenderer().width(text);
    }

    public int getWordWrappedHeight(String text, int wrapWidth) {
        return this.getFontRenderer().wordWrapHeight(text, wrapWidth);
    }

    public void drawTexture(GuiGraphics g, ResourceLocation texture, float x, float y, float z, float u, float v, float width, float height) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        Matrix4f m = g.pose().last().pose();
        Tesselator t = Tesselator.getInstance();
        var buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(m, x, y + height, z).uv(0f, v).endVertex();
        buf.vertex(m, x + width, y + height, z).uv(u, v).endVertex();
        buf.vertex(m, x + width, y, z).uv(u, 0f).endVertex();
        buf.vertex(m, x, y, z).uv(0f, 0f).endVertex();
        t.end();

        RenderSystem.disableBlend();
    }

    public void drawTextureTiled(PoseStack matrixStack, ResourceLocation texture, float x, float y, float z, float u, float v, float width, float height, float resolution) {
        this.preDraw();
        this.getMinecraft().getTextureManager().bindForSetup(texture);
        float scale = 0.00390625F * resolution;
        Tesselator tesselator = Tesselator.getInstance();
        var buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(x, y + height, z).uv((u + 0) * scale, (v + height) * scale).endVertex();
        buffer.vertex(x + width, y + height, z).uv((u + width) * scale, (v + height) * scale).endVertex();
        buffer.vertex(x + width, y, z).uv((u + width) * scale, (v + 0) * scale).endVertex();
        buffer.vertex(x, y, z).uv((u + 0) * scale, (v + 0) * scale).endVertex();
        tesselator.end();
        this.postDraw();
    }

    public void drawBar(GuiGraphics matrixStack, ResourceLocation texture, int x, int y, float z, float width, float height, int segments, int segmentLimit) {
        boolean reverse = segmentLimit < 0;
        if (reverse) {
            segmentLimit = -segmentLimit;
        }
        for (int i = 0; i < segments; i++) {
            int currentSegment = i;
            if (reverse) {
                currentSegment = segmentLimit - i - 1;
            }
            this.drawTexture(matrixStack, texture, x + (width * currentSegment), y, z, 1, 1, width, height);
        }
    }

    public void preDraw() {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void postDraw() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Deprecated
    public void drawTexturedModalRect(GuiGraphics matrixStack, int x, int y, int u, int v, int width, int height) {
        this.drawTexturedModalRect(matrixStack, x, y, u, v, width, height, 1);
    }

    @Deprecated
    public void drawTexturedModalRect(GuiGraphics guiGraphics, int x, int y, int u, int v, int width, int height, int resolution) {
        this.preDraw();
        float scale = 0.00390625F * resolution;
        Matrix3f normal = guiGraphics.pose().last().normal();
        Matrix4f matrix = guiGraphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        var buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, x, y + height, 0).uv(u * scale, (v + height) * scale).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).uv((u + width) * scale, (v + height) * scale).endVertex();
        buffer.vertex(matrix, x + width, y, 0).uv((u + width) * scale, v * scale).endVertex();
        buffer.vertex(matrix, x, y, 0).uv(u * scale, v * scale).endVertex();
        tesselator.end();
        this.postDraw();
    }
}
