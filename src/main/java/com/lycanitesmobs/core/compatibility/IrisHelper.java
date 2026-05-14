package com.lycanitesmobs.core.compatibility;

import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Isolated Iris API bridge — this class is <b>only ever loaded</b> when
 * Oculus is present (guarded by {@link OculusCompat#isOculusLoaded()}).
 *
 * <p>Keeping all {@code net.irisshaders.*} imports in this single class
 * means the rest of the codebase compiles and runs cleanly without Oculus
 * on the classpath.</p>
 */
public final class IrisHelper {

    private IrisHelper() {}

    /**
     * Direct call to Iris API to avoid reflection
     */
    public static boolean isShaderPackInUse() {
        try {
            return IrisApi.getInstance().isShaderPackInUse();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Maps our internal blend-mode + glow flags to the best vanilla entity
     * {@link RenderType} that Iris knows how to intercept.
     *
     * <p>Iris wraps these types with {@code EntityRenderStateShard}, routes
     * them to the shader-pack's {@code gbuffers_entities} program, and
     * extends the vertex format to {@code IrisVertexFormats.ENTITY}
     * automatically.</p>
     *
     * @param texture  The entity texture.
     * @param blending One of {@link CustomRenderStates.BLEND} ordinals.
     * @param glow     Whether this layer should be fullbright / emissive.
     */
    public static RenderType getIrisEntityRenderType(ResourceLocation texture, int blending, boolean glow) {
        if (glow || blending == CustomRenderStates.BLEND.ADD.getValue()) {
            return RenderType.entityTranslucentEmissive(texture);
        }
        if (blending == CustomRenderStates.BLEND.SUB.getValue()) {
            return RenderType.entityTranslucent(texture);
        }
        return RenderType.entityCutoutNoCull(texture);
    }

    /** Whether we are currently inside a shadow-rendering pass. */
    public static boolean isRenderingShadowPass() {
        try {
            return IrisApi.getInstance().isRenderingShadowPass();
        } catch (Exception e) {
            return false;
        }
    }
}
