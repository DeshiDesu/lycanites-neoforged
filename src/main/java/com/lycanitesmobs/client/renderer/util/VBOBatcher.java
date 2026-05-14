package com.lycanitesmobs.client.renderer.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import com.lycanitesmobs.client.effect.FearVisualHandler;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import org.joml.Vector4f;

import static com.lycanitesmobs.client.renderer.util.CustomRenderStates.POS_TEX_NORMAL_SHADER;

public class VBOBatcher {

    public static class VBODrawCommand {
        private final VertexBuffer vbo;
        private final int vertexCount;
        private final VertexFormat format;
        private final Matrix4f modelMatrix;
        private final ResourceLocation texLocation;

        public static int minimumBrightness = 0;

        static float frameNvScale = 0.0F;
        static float frameMinLightU = 0.0F;
        static float frameMinLightV = 0.0F;

        private static final Matrix4f MODELVIEW_SCRATCH = new Matrix4f();
        private static final Matrix3f NORMAL_SCRATCH = new Matrix3f();

        private float red = 1.0F, green = 1.0F, blue = 1.0F, alpha = 1.0F;
        private float textureOffsetX, textureOffsetY;
        private float overlayOffsetX, overlayOffsetY;
        private float lightOffsetX, lightOffsetY;
        private float baseR = 1.0F, baseG = 1.0F, baseB = 1.0F, baseRange = 0.1F;
        private float variantR = 1.0F, variantG = 1.0F, variantB = 1.0F, variantAmount = 0.0F;
        private float tintR = 1.0F;
        private float tintG = 1.0F;
        private float tintB = 1.0F;
        private float tintAmount = 0.0F;

        /**
         * Keeps the old ctor shape: (vbo, vertexCount, format, matrix, tex)
         */
        public VBODrawCommand(VertexBuffer vbo,
                              int vertexCount,
                              VertexFormat format,
                              Matrix4f matrix,
                              ResourceLocation texLocation) {
            this.vbo = vbo;
            this.vertexCount = vertexCount;
            this.format = format;
            this.modelMatrix = new Matrix4f(matrix);
            this.texLocation = texLocation;
        }

        private float alphaCutoff = 0.0F;

        public VBODrawCommand setAlphaCutoff(float cutoff) {
            this.alphaCutoff = cutoff;
            return this;
        }

        public VBODrawCommand setColor(Vector4f color) {
            return setColor(color.x, color.y, color.z, color.w);
        }

        public VBODrawCommand setVariantPalette(
                float baseR, float baseG, float baseB, float baseRange,
                float variantR, float variantG, float variantB, float variantAmount) {
            this.baseR = baseR;
            this.baseG = baseG;
            this.baseB = baseB;
            this.baseRange = baseRange;
            this.variantR = variantR;
            this.variantG = variantG;
            this.variantB = variantB;
            this.variantAmount = variantAmount;
            return this;
        }

        public VBODrawCommand setVariantTint(float r, float g, float b, float amount) {
            this.tintR = r;
            this.tintG = g;
            this.tintB = b;
            this.tintAmount = amount;
            return this;
        }

        public VBODrawCommand setRecolorBand(float r, float g, float b, float range) {
            this.baseR = r;
            this.baseG = g;
            this.baseB = b;
            this.baseRange = range;
            return this;
        }

        public VBODrawCommand setColor(float r, float g, float b, float a) {
            this.red = r;
            this.green = g;
            this.blue = b;
            this.alpha = a;
            return this;
        }

        public VBODrawCommand setTextureOffset(Vector2f offset) {
            return setTextureOffset(offset.x * 0.01F, -offset.y * 0.01F);
        }

        public VBODrawCommand setTextureOffset(float u, float v) {
            this.textureOffsetX = u;
            this.textureOffsetY = v;
            return this;
        }

        public VBODrawCommand setOverlayOffset(float white, boolean red) {
            return setOverlayOffset(OverlayTexture.u(white), OverlayTexture.v(red));
        }

        public VBODrawCommand setOverlayOffset(float u, float v) {
            this.overlayOffsetX = u;
            this.overlayOffsetY = v;
            return this;
        }

        public VBODrawCommand setLightOffset(int packed) {
            return setLightOffset(LightTexture.block(packed) << 4, LightTexture.sky(packed) << 4);
        }

        public VBODrawCommand setLightOffset(float u, float v) {
            this.lightOffsetX = u;
            this.lightOffsetY = v;
            return this;
        }

        private static boolean loggedOnce;

        private static void logShaderMissingOnce(VertexFormat fmt) {
            if (!loggedOnce) {
                loggedOnce = true;
                LogManager.getLogger().warn(
                        "[Lycanites Mobs] No shader bound for format '{}'. " +
                                "Bind a shader in RenderType or switch to a vanilla format (e.g. POSITION_TEX_COLOR_NORMAL). Skipping draw to avoid crash.",
                        fmt);
            }
        }

        private static Supplier<ShaderInstance> pickShaderForFormat(VertexFormat fmt) {
            if (fmt == DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL)
                return GameRenderer::getPositionTexColorNormalShader;
            if (fmt == DefaultVertexFormat.POSITION_COLOR)
                return GameRenderer::getPositionColorShader;
            if (fmt == DefaultVertexFormat.POSITION_TEX)
                return GameRenderer::getPositionTexShader;
            if (fmt == DefaultVertexFormat.POSITION_COLOR_TEX)
                return GameRenderer::getPositionColorTexShader;
            if (fmt == DefaultVertexFormat.POSITION_TEX_COLOR)
                return GameRenderer::getPositionTexColorShader;
            if (fmt == DefaultVertexFormat.POSITION)
                return GameRenderer::getPositionShader;
            if (fmt == CustomRenderStates.POS_TEX_NORMAL
                    && POS_TEX_NORMAL_SHADER != null) {
                return () -> POS_TEX_NORMAL_SHADER;
            }
            return null;
        }

        /**
         * Returns true if this draw command uses a vanilla entity vertex format,
         * meaning it should defer to the RenderType's shader (which Iris replaces).
         */
        private boolean isEntityFormat() {
            return format == DefaultVertexFormat.NEW_ENTITY;
        }

        public void draw() {
            draw(false);
        }

        public void draw(boolean skipTextureBind) {
            if (vbo == null)
                return;

            if (!skipTextureBind) {
                RenderSystem.setShaderTexture(0, texLocation);
            }

            ShaderInstance shader;

            if (isEntityFormat()) {
                float finalR = red;
                float finalG = green;
                float finalB = blue;
                if (tintAmount > 0.001F) {
                    finalR = red * (1.0F - tintAmount) + red * tintR * tintAmount;
                    finalG = green * (1.0F - tintAmount) + green * tintG * tintAmount;
                    finalB = blue * (1.0F - tintAmount) + blue * tintB * tintAmount;
                }
                RenderSystem.setShaderColor(corrected(finalR), corrected(finalG), corrected(finalB), corrected(alpha));

                shader = RenderSystem.getShader();
                if (shader == null) {
                    RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
                    shader = RenderSystem.getShader();
                }
            } else {
                RenderSystem.setShaderColor(corrected(red), corrected(green), corrected(blue), corrected(alpha));
                shader = RenderSystem.getShader();

                if (format == CustomRenderStates.POS_TEX_NORMAL && POS_TEX_NORMAL_SHADER != null) {
                    if (shader != POS_TEX_NORMAL_SHADER) {
                        RenderSystem.setShader(() -> POS_TEX_NORMAL_SHADER);
                        shader = POS_TEX_NORMAL_SHADER;
                    }
                } else if (shader == null) {
                    var pick = pickShaderForFormat(format);
                    if (pick != null) {
                        RenderSystem.setShader(pick);
                        shader = pick.get();
                    }
                }
            }

            if (shader == null) {
                logShaderMissingOnce(format);
                return;
            }

            var uv = shader.getUniform("UvOffset");
            if (uv != null)
                uv.set(textureOffsetX, textureOffsetY);

            var overlay = shader.getUniform("OverlayUv");
            if (overlay != null)
                overlay.set(overlayOffsetX, overlayOffsetY);

            var light = shader.getUniform("LightUv");
            if (light != null) {
                float u = lightOffsetX;
                float v = lightOffsetY;

                if (frameNvScale > 0.0F) {
                    u = Mth.lerp(frameNvScale, u, 240.0F);
                    v = Mth.lerp(frameNvScale, v, 240.0F);
                }

                u = Math.max(u, frameMinLightU);
                v = Math.max(v, frameMinLightV);

                light.set(u, v);
            }

            var cutoff = shader.getUniform("AlphaCutoff");
            if (cutoff != null)
                cutoff.set(alphaCutoff);

            var baseColor = shader.getUniform("BaseColor");
            if (baseColor != null)
                baseColor.set(baseR, baseG, baseB);

            var baseRangeUni = shader.getUniform("BaseRange");
            if (baseRangeUni != null)
                baseRangeUni.set(baseRange);

            var tintColor = shader.getUniform("VariantColor");
            if (tintColor != null)
                tintColor.set(tintR, tintG, tintB);

            var tintAmt = shader.getUniform("VariantAmount");
            if (tintAmt != null)
                tintAmt.set(tintAmount);

            // viewMat = RenderSystem.getModelViewMatrix() holds the camera/view matrix.
            // modelView = view * per-draw model. Reuse scratch to avoid allocation.
            var viewMat = RenderSystem.getModelViewMatrix();
            var modelView = MODELVIEW_SCRATCH.set(viewMat).mul(this.modelMatrix);
            var projection = RenderSystem.getProjectionMatrix();

            // Creature parts use uniform scale + rotation only, so mat3(modelView) is
            // orthonormal up to a scalar. normalize() in the vertex shader corrects the
            // scale factor, so the full invert+transpose is not needed.
            var normalMatUni = shader.getUniform("NormalMat");
            if (normalMatUni != null) {
                normalMatUni.set(NORMAL_SCRATCH.set(modelView));
            }

            // SunDirectionVS, SpecularColor, SpecularIntensity, SpecularPower uniforms
            // are defined in the shader JSON for resource pack override support but are
            // not uploaded here — mobs use matte (lightmap-only) shading by default.

            var fearGlow = shader.getUniform("FearRedGlow");
            if (fearGlow != null)
                fearGlow.set(FearVisualHandler.getRedGlowIntensity());

            vbo.bind();
            vbo.drawWithShader(modelView, projection, shader);
            VertexBuffer.unbind();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        private static float corrected(float f) {
            return ((int) (f * 255.0F) & 0xFF) / 255.0F;
        }

    }

    private static final VBOBatcher INSTANCE = new VBOBatcher();
    private final Map<RenderType, List<VBODrawCommand>> batchedDrawCommands = new LinkedHashMap<>();
    private final Map<RenderType, List<VBODrawCommand>> deferredDrawCommands = new LinkedHashMap<>();

    private static final Comparator<VBODrawCommand> TEX_COMPARATOR =
            Comparator.comparing(c -> c.texLocation, Comparator.nullsFirst(Comparator.naturalOrder()));

    public static VBOBatcher getInstance() {
        return INSTANCE;
    }

    public void queue(RenderType renderType, VBODrawCommand drawCommand) {
        batchedDrawCommands.computeIfAbsent(renderType, k -> new ArrayList<>()).add(drawCommand);
    }

    public void queueDeferred(RenderType renderType, VBODrawCommand drawCommand) {
        deferredDrawCommands.computeIfAbsent(renderType, k -> new ArrayList<>()).add(drawCommand);
    }

    private static void updateFrameLightingFactors() {
        Minecraft mc = Minecraft.getInstance();
        Entity cam = mc.getCameraEntity();
        VBODrawCommand.frameNvScale = 0.0F;
        if (cam instanceof LivingEntity le && mc.level != null
                && (le.hasEffect(MobEffects.NIGHT_VISION) || le.hasEffect(MobEffects.CONDUIT_POWER))) {
            VBODrawCommand.frameNvScale = GameRenderer.getNightVisionScale(le, mc.getFrameTime());
        }
        VBODrawCommand.frameMinLightU = LightTexture.block(VBODrawCommand.minimumBrightness) << 4;
        VBODrawCommand.frameMinLightV = LightTexture.sky(VBODrawCommand.minimumBrightness) << 4;
    }

    public void endBatches() {
        if (batchedDrawCommands.isEmpty())
            return;

        updateFrameLightingFactors();

        batchedDrawCommands.forEach((renderType, drawCommands) -> {
            if (drawCommands.isEmpty())
                return;

            renderType.setupRenderState();

            for (VBODrawCommand cmd : drawCommands) {
                cmd.draw();
            }

            renderType.clearRenderState();
            drawCommands.clear();
        });

        batchedDrawCommands.clear();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void endDeferredBatches() {
        if (deferredDrawCommands.isEmpty())
            return;

        updateFrameLightingFactors();

        deferredDrawCommands.forEach((renderType, drawCommands) -> {
            if (drawCommands.isEmpty())
                return;

            if (drawCommands.size() > 1) {
                drawCommands.sort(TEX_COMPARATOR);
            }

            renderType.setupRenderState();

            ResourceLocation lastTex = null;
            boolean first = true;
            for (VBODrawCommand cmd : drawCommands) {
                ResourceLocation tex = cmd.texLocation;
                if (first || !Objects.equals(tex, lastTex)) {
                    if (tex != null) {
                        RenderSystem.setShaderTexture(0, tex);
                    }
                    lastTex = tex;
                    first = false;
                }
                cmd.draw(true);
            }

            renderType.clearRenderState();
            drawCommands.clear();
        });

        deferredDrawCommands.clear();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
