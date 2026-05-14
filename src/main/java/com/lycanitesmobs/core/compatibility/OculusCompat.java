package com.lycanitesmobs.core.compatibility;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

/**
 * Optional dependency bridge for Oculus (Iris on Forge).
 *
 * <p>Detection uses {@link ModList}, and all Iris API calls are isolated in
 * {@link IrisHelper} so that this class can be loaded safely even when
 * Oculus is not installed.  No reflection is used — {@code IrisHelper} is
 * only referenced inside methods guarded by {@link #oculusLoaded}, ensuring
 * the JVM never attempts to load {@code net.irisshaders.*} classes when
 * Oculus is absent.</p>
 */
public class OculusCompat {

    private static boolean oculusLoaded;

    /** Call once during mod setup (FMLCommonSetupEvent). */
    public static void init() {
        oculusLoaded = ModList.get().isLoaded("oculus");
        if (oculusLoaded) {
            LMHelperClass.logInfo("", "[Lycanites Mobs] Oculus detected – Iris shader compatibility enabled.");
        }
    }

    /** {@code true} when Oculus is present in the mod list. */
    public static boolean isOculusLoaded() {
        return oculusLoaded;
    }

    /**
     * {@code true} when Oculus is loaded <b>and</b> a shader pack is
     * currently active.  Safe to call every frame.
     */
    public static boolean isShaderPackActive() {
        if (!oculusLoaded) return false;
        return IrisHelper.isShaderPackInUse();
    }

    /**
     * Maps our blend mode + glow to a vanilla entity {@link RenderType} that
     * Iris knows how to intercept.  Only call when {@link #isShaderPackActive()}
     * returns {@code true}.
     *
     * @param texture  Entity texture.
     * @param blending Blend mode ordinal (NORMAL/ADD/SUB).
     * @param glow     Whether this layer is emissive.
     * @return A vanilla entity RenderType that Iris will route through gbuffers_entities.
     */
    public static RenderType getIrisEntityRenderType(ResourceLocation texture, int blending, boolean glow) {
        return IrisHelper.getIrisEntityRenderType(texture, blending, glow);
    }
}
