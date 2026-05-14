package com.lycanitesmobs.client.obj.model;

import com.lycanitesmobs.client.model.creature.base.ModelObjState;
import com.lycanitesmobs.client.obj.geometry.ObjPart;
import com.lycanitesmobs.client.obj.geometry.Vertex;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.RecolorTextureCache;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.client.renderer.util.VBOBatcher.VBODrawCommand;
import com.lycanitesmobs.core.compatibility.OculusCompat;
import com.lycanitesmobs.core.util.math.Vector3o;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class VBOObjModel extends ObjModel {

    public static RenderType renderType;
    public static ResourceLocation tex;
    public static boolean renderNormal;
    public static boolean renderOutline;

    /**
     * When true, use the entity-format path (for Iris/Oculus compatibility).
     */
    public static boolean useEntityFormat;
    /**
     * The Iris-compatible entity render type for the base (non-recolored) texture.
     */
    public static RenderType irisRenderType;
    public static int irisBlending;
    public static boolean irisGlow;
    public static boolean deferFlush;

    private static final Matrix4f IRIS_MV_SCRATCH = new Matrix4f();
    private static final BufferBuilder IRIS_TEMP_BUFFER = new BufferBuilder(65536);
    private static VertexBuffer irisTempVbo = null;

    public float baseR = 1.0F, baseG = 1.0F, baseB = 1.0F, baseRange = 0.1F;
    public float variantR = 1.0F, variantG = 1.0F, variantB = 1.0F, variantAmount = 0.0F;

    public VBOObjModel(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    public VBOObjModel(ResourceLocation resourceLocation, ResourceManager resourceManager) {
        super(resourceLocation, resourceManager);
    }

    public void applyVariantFromState(ModelObjState state) {
        if (state == null) return;
        this.baseR = state.baseR;
        this.baseG = state.baseG;
        this.baseB = state.baseB;
        this.baseRange = state.baseRange;
        this.variantR = state.variantR;
        this.variantG = state.variantG;
        this.variantB = state.variantB;
        this.variantAmount = state.variantAmount;
    }

    @Override
    public void renderPart(VertexConsumer vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f,
                           int brightness, int fade, ObjPart objPart, Vector4f color, Vector2f textureOffset) {
        if (vertexBuilder != null) {
            super.renderPart(vertexBuilder, matrix3f, matrix4f, brightness, fade, objPart, color, textureOffset);
            return;
        }

        if (useEntityFormat && irisRenderType != null) {
            ResourceLocation recoloredTex = tex;
            if (variantAmount > 0.01F) {
                recoloredTex = RecolorTextureCache.getRecoloredTexture(
                        tex, baseR, baseG, baseB, baseRange,
                        variantR, variantG, variantB, variantAmount);
            }

            RenderType rt = recoloredTex != tex
                    ? OculusCompat.getIrisEntityRenderType(recoloredTex, irisBlending, irisGlow)
                    : irisRenderType;

            if (objPart.mesh.normals == null || objPart.mesh.normals.length != objPart.mesh.vertices.length) {
                objPart.mesh.computeVertexNormalsIfNeeded();
            }

            int[] indices = objPart.mesh.indices;
            Vertex[] vertices = objPart.mesh.vertices;
            Vector3o[] normals = objPart.mesh.normals;
            float texOX = textureOffset.x * 0.01f;
            float texOY = textureOffset.y * 0.01f;

            IRIS_TEMP_BUFFER.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
            for (int i = 0; i < indices.length; i++) {
                int idx = indices[i];
                Vertex v = vertices[idx];
                Vector3o n = (normals != null && idx < normals.length && normals[idx] != null)
                        ? normals[idx] : new Vector3o(0, 1, 0);

                IRIS_TEMP_BUFFER
                        .vertex(v.getPos().x(), v.getPos().y(), v.getPos().z())
                        .color(255, 255, 255, 255)
                        .uv(v.getTexCoords().x + texOX, 1f - (v.getTexCoords().y + texOY))
                        .overlayCoords(0, 10 - fade)
                        .uv2(brightness)
                        .normal(n.x(), n.y(), n.z())
                        .endVertex();
            }

            if (irisTempVbo == null) {
                irisTempVbo = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            }
            irisTempVbo.bind();
            irisTempVbo.upload(IRIS_TEMP_BUFFER.end());

            IRIS_MV_SCRATCH.set(RenderSystem.getModelViewMatrix()).mul(matrix4f);
            RenderSystem.setShaderTexture(0, recoloredTex);
            rt.setupRenderState();

            ShaderInstance shader = RenderSystem.getShader();
            if (shader != null) {
                var overlay = shader.getUniform("OverlayUv");
                if (overlay != null) {
                    overlay.set(0.0F, 10.0F - fade);
                }
                RenderSystem.setShaderColor(color.x(), color.y(), color.z(), color.w());
                irisTempVbo.drawWithShader(IRIS_MV_SCRATCH, RenderSystem.getProjectionMatrix(), shader);
            }

            rt.clearRenderState();
            VertexBuffer.unbind();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }

        if (renderNormal) {
            int blockLight = brightness & 0xFFFF;
            int skyLight = brightness & 0xFFFF0000;
            blockLight = Math.min(blockLight + (4 << 4), 15 << 4);
            // Master offset for general mob lighting
            int boost = 0;
            blockLight = Math.min(blockLight + (boost << 4), 15 << 4);
            int boostedBrightness = skyLight | blockLight;
            VBODrawCommand cmd = new VBODrawCommand(
                    objPart.mesh.getVbo(),
                    objPart.mesh.indices.length,
                    renderType.format(),
                    matrix4f,
                    tex
            )
                    .setColor(color)
                    .setTextureOffset(textureOffset)
                    .setOverlayOffset(0.0F, 10.0F - fade)
                    .setLightOffset(boostedBrightness)
                    .setRecolorBand(
                            baseR,
                            baseG,
                            baseB,
                            baseRange
                    )
                    .setVariantTint(
                            variantR,
                            variantG,
                            variantB,
                            variantAmount
                    );

            if (renderType == CustomRenderStates.OBJ_CUTOUT) {
                cmd.setAlphaCutoff(0.1F);
            }

            // Defer only cutout/opaque-ish creature draws. Translucent/additive/subtractive
            // layers rely on submission order for stable self-overlap, which mobs like
            // Nymph expose immediately through their paper-thin wing and hair planes.
            boolean shouldDefer = deferFlush && renderType == CustomRenderStates.OBJ_CUTOUT;

            if (shouldDefer) {
                VBOBatcher.getInstance().queueDeferred(renderType, cmd);
            } else {
                VBOBatcher.getInstance().queue(renderType, cmd);
            }
        }

        if (renderOutline) {
            VBODrawCommand outlineCmd = new VBODrawCommand(
                    objPart.mesh.getVbo(),
                    objPart.mesh.indices.length,
                    renderType != null ? renderType.format() : CustomRenderStates.POS_TEX_NORMAL,
                    matrix4f,
                    tex
            );
            boolean shouldDeferOutline = deferFlush && renderType == CustomRenderStates.OBJ_CUTOUT;
            if (shouldDeferOutline) {
                VBOBatcher.getInstance().queueDeferred(CustomRenderStates.OBJ_OUTLINE_RENDER_TYPE, outlineCmd);
            } else {
                VBOBatcher.getInstance().queue(CustomRenderStates.OBJ_OUTLINE_RENDER_TYPE, outlineCmd);
            }
        }
    }

    public static float[] hexToRGB(int hex) {
        float r = ((hex >> 16) & 0xFF) / 255.0F;
        float g = ((hex >> 8) & 0xFF) / 255.0F;
        float b = (hex & 0xFF) / 255.0F;
        return new float[]{r, g, b};
    }
}
