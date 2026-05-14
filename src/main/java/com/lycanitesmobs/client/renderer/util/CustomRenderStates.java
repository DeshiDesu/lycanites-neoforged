package com.lycanitesmobs.client.renderer.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.function.Supplier;


public class CustomRenderStates extends RenderStateShard {
    public static ShaderInstance POS_TEX_NORMAL_SHADER;
    public static final TransparencyStateShard DEFAULT_ALPHA = RenderStateShard.NO_TRANSPARENCY;
    public static final Vector4f WHITE = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
    public static LightmapStateShard DIFFUSE_LIGHTING = new LightmapStateShard(true);
    public static LightmapStateShard NO_DIFFUSE_LIGHTING = new LightmapStateShard(false);
    public static VertexFormat POS_COL_TEX_LIGHT_FADE_NORMAL;
    public static VertexFormat POS_COL_TEX_NORMAL;
    public static final VertexFormat POS_TEX_NORMAL = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                    .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
                    .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
                    .put("Padding", DefaultVertexFormat.ELEMENT_PADDING)
                    .build()
    );


    public enum BLEND {
        NORMAL(0), ADD(1), SUB(2);
        public final int id;

        BLEND(int value) {
            this.id = value;
        }

        public int getValue() {
            return id;
        }
    }

    protected static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("lm_additive_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    protected static final RenderStateShard.TransparencyStateShard SUBTRACTIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("lm_subtractive_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    private static final RenderType[] OBJ_RENDER_TYPES = new RenderType[BLEND.values().length * 2];
    public static final RenderType OBJ_RENDER_TYPE = RenderType.create(
            "lm_obj_pos_tex_normal",
            POS_TEX_NORMAL,
            VertexFormat.Mode.TRIANGLES,
            65536,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> POS_TEX_NORMAL_SHADER))
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(true)
    );

    public static final RenderType FEAR_RENDER_TYPE = RenderType.create(
            "lm_fear_translucent",
            POS_TEX_NORMAL,
            VertexFormat.Mode.TRIANGLES,
            65536,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> POS_TEX_NORMAL_SHADER))
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(true)
    );

    public static final RenderType OBJ_TRANSLUCENT_NO_DEPTH_WRITE = RenderType.create(
            "lm_obj_pos_tex_normal_translucent_no_depth_write",
            POS_TEX_NORMAL,
            VertexFormat.Mode.TRIANGLES,
            65536,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> POS_TEX_NORMAL_SHADER))
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(true)
    );

    public static final RenderType OBJ_CUTOUT = RenderType.create(
            "lm_obj_cutout",
            POS_TEX_NORMAL,
            VertexFormat.Mode.TRIANGLES, 256, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> POS_TEX_NORMAL_SHADER))
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setTransparencyState(NO_TRANSPARENCY)
                    .createCompositeState(true)
    );

    static {
        for (BLEND blend : BLEND.values()) {
            for (int glow = 0; glow < 2; glow++) {
                OBJ_RENDER_TYPES[blend.id * 2 + glow] = RenderType.create(
                        "lm_obj_" + blend + (glow == 1 ? "_glow" : ""),
                        POS_TEX_NORMAL,
                        VertexFormat.Mode.TRIANGLES,
                        256,
                        true,
                        false,
                        RenderType.CompositeState.builder()
                                .setShaderState(new ShaderStateShard(() -> POS_TEX_NORMAL_SHADER))
                                .setCullState(NO_CULL)
                                .setLightmapState(LIGHTMAP)
                                .setOverlayState(OVERLAY)
                                .setDepthTestState(LEQUAL_DEPTH_TEST)
                                .setWriteMaskState(COLOR_DEPTH_WRITE)
                                .setTransparencyState(
                                        blend == BLEND.ADD ? ADDITIVE_TRANSPARENCY
                                                : blend == BLEND.SUB ? SUBTRACTIVE_TRANSPARENCY
                                                : TRANSLUCENT_TRANSPARENCY
                                )
                                .createCompositeState(true)
                );
            }
        }
    }

    /**
     * The lengths I go to to keep old rendering logic...
     */
    @OnlyIn(Dist.CLIENT)
    public static void assertThread(Supplier<Boolean> p_assertThread_0_) {
        if (!(Boolean) p_assertThread_0_.get()) {
            throw new IllegalStateException("Rendersystem called from wrong thread");
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void setupOutline() {
        RenderSystem.assertOnGameThread();
        _texEnv(8960, 8704, 34160);
        color1arg(7681, 34168);
    }

    @OnlyIn(Dist.CLIENT)
    public static void _texEnv(int p_227643_0_, int p_227643_1_, int p_227643_2_) {
        RenderSystem.assertOnGameThread();
        GL11.glTexEnvi(p_227643_0_, p_227643_1_, p_227643_2_);
    }

    @OnlyIn(Dist.CLIENT)
    private static void color1arg(int p_227751_0_, int p_227751_1_) {
        _texEnv(8960, 34161, p_227751_0_);
        _texEnv(8960, 34176, p_227751_1_);
        _texEnv(8960, 34192, 768);
    }

    @OnlyIn(Dist.CLIENT)
    public static void teardownOutline() {
        RenderSystem.assertOnGameThread();
        _texEnv(8960, 8704, 8448);
        color3arg(8448, 5890, 34168, 34166);
    }

    @OnlyIn(Dist.CLIENT)
    private static void color3arg(int p_227720_0_, int p_227720_1_, int p_227720_2_, int p_227720_3_) {
        _texEnv(8960, 34161, p_227720_0_);
        _texEnv(8960, 34176, p_227720_1_);
        _texEnv(8960, 34192, 768);
        _texEnv(8960, 34177, p_227720_2_);
        _texEnv(8960, 34193, 768);
        _texEnv(8960, 34178, p_227720_3_);
        _texEnv(8960, 34194, 770);
    }

    @OnlyIn(Dist.CLIENT)
    public static final TexturingStateShard OUTLINE_TEXTURING = new TexturingStateShard("outline_texturing", () -> {
        assertThread(RenderSystem::isOnGameThread);
        setupOutline();
    }, CustomRenderStates::teardownOutline);

    /**
     * End of old rendering logic, subject to be removed once I understand render states more
     */
    public static final RenderType OBJ_OUTLINE_RENDER_TYPE = RenderType.create(
            "lm_obj_outline_no_cull",
            POS_TEX_NORMAL,
            VertexFormat.Mode.TRIANGLES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setOutputState(OUTLINE_TARGET)
                    .createCompositeState(false)
    );


    public CustomRenderStates(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
    }

    public static RenderType getObjVBORenderType(int blending, boolean glow) {
        if (blending == BLEND.NORMAL.getValue() && !glow) {
            return OBJ_CUTOUT;
        }
        return OBJ_RENDER_TYPES[(blending << 1) | (glow ? 1 : 0)];
    }


    public static RenderType getObjRenderType(ResourceLocation texture, int blending, boolean glow) {
        if (POS_COL_TEX_LIGHT_FADE_NORMAL == null) {
            ImmutableMap.Builder<String, VertexFormatElement> vertexFormatMapBuilder = ImmutableMap.builder();
            vertexFormatMapBuilder.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
            vertexFormatMapBuilder.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
            vertexFormatMapBuilder.put("UV0", DefaultVertexFormat.ELEMENT_UV0);
            vertexFormatMapBuilder.put("UV1", DefaultVertexFormat.ELEMENT_UV1);
            vertexFormatMapBuilder.put("UV2", DefaultVertexFormat.ELEMENT_UV2);
            vertexFormatMapBuilder.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
            vertexFormatMapBuilder.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
            POS_COL_TEX_LIGHT_FADE_NORMAL = new VertexFormat(vertexFormatMapBuilder.build());
        }

        RenderStateShard.TransparencyStateShard transparencyState = TRANSLUCENT_TRANSPARENCY;
        if (blending == BLEND.ADD.getValue()) {
            transparencyState = ADDITIVE_TRANSPARENCY;
        } else if (blending == BLEND.SUB.getValue()) {
            transparencyState = SUBTRACTIVE_TRANSPARENCY;
        }

        RenderType.CompositeState renderTypeState = RenderType.CompositeState.builder()
                .setShaderState(new ShaderStateShard(GameRenderer::getPositionColorTexLightmapShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(transparencyState)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);

        return RenderType.create(
                "lm_obj_translucent_no_cull",
                POS_COL_TEX_LIGHT_FADE_NORMAL,
                VertexFormat.Mode.TRIANGLES,
                256,
                true,
                false,
                renderTypeState
        );
    }

    public static RenderType getObjColorOnlyRenderType(ResourceLocation texture, int blending, boolean glow) {
        if (POS_COL_TEX_LIGHT_FADE_NORMAL == null) {
            ImmutableMap.Builder<String, VertexFormatElement> vertexFormatMapBuilder = ImmutableMap.builder();
            vertexFormatMapBuilder.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
            vertexFormatMapBuilder.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
            vertexFormatMapBuilder.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
            vertexFormatMapBuilder.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
            POS_COL_TEX_LIGHT_FADE_NORMAL = new VertexFormat(vertexFormatMapBuilder.build());
        }

        RenderStateShard.TransparencyStateShard transparencyState = TRANSLUCENT_TRANSPARENCY;
        if (blending == BLEND.ADD.getValue()) {
            transparencyState = ADDITIVE_TRANSPARENCY;
        } else if (blending == BLEND.SUB.getValue()) {
            transparencyState = SUBTRACTIVE_TRANSPARENCY;
        }

        RenderType.CompositeState renderTypeState = RenderType.CompositeState.builder()
                .setShaderState(new ShaderStateShard(GameRenderer::getPositionColorShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(transparencyState)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);

        return RenderType.create(
                "lm_obj_translucent_no_cull",
                POS_COL_TEX_LIGHT_FADE_NORMAL,
                VertexFormat.Mode.TRIANGLES,
                256,
                true,
                false,
                renderTypeState
        );
    }

    public static RenderType getObjOutlineRenderType(ResourceLocation texture) {
        if (POS_COL_TEX_LIGHT_FADE_NORMAL == null) {
            ImmutableMap.Builder<String, VertexFormatElement> vertexFormatMapBuilder = ImmutableMap.builder();
            vertexFormatMapBuilder.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
            vertexFormatMapBuilder.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
            vertexFormatMapBuilder.put("UV0", DefaultVertexFormat.ELEMENT_UV0);
            vertexFormatMapBuilder.put("UV1", DefaultVertexFormat.ELEMENT_UV1);
            vertexFormatMapBuilder.put("UV2", DefaultVertexFormat.ELEMENT_UV2);
            vertexFormatMapBuilder.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
            vertexFormatMapBuilder.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
            POS_COL_TEX_LIGHT_FADE_NORMAL = new VertexFormat(vertexFormatMapBuilder.build());
        }

        RenderType.CompositeState renderTypeState = RenderType.CompositeState.builder()
                .setShaderState(new ShaderStateShard(GameRenderer::getPositionColorTexShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setCullState(NO_CULL)
                .setDepthTestState(NO_DEPTH_TEST)
                .setTexturingState(OUTLINE_TEXTURING)
                .setOutputState(OUTLINE_TARGET)
                .createCompositeState(false);

        return RenderType.create(
                "lm_obj_outline_no_cull",
                POS_COL_TEX_LIGHT_FADE_NORMAL,
                VertexFormat.Mode.TRIANGLES,
                256,
                true,
                false,
                renderTypeState
        );
    }


    public static RenderType getSpriteRenderType(@Nullable ResourceLocation texture) {
        if (texture == null) texture = MissingTextureAtlasSprite.getLocation();
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new ShaderStateShard(GameRenderer::getPositionColorTexShader))
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setCullState(NO_CULL)
                .createCompositeState(true);

        return RenderType.create(
                "lm_sprite",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS,
                256, true, false, state
        );
    }

}
