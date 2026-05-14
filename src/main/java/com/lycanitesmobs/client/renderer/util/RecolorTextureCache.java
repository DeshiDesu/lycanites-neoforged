package com.lycanitesmobs.client.renderer.util;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CPU-side palette-swap cache that replicates the fragment shader's recolor-band
 * logic on texture pixels, then uploads the result as a {@link DynamicTexture}.
 *
 * <p>This allows the recolor/variant system to work under Iris/Oculus shader
 * packs, where our custom uniforms ({@code BaseColor}, {@code VariantColor},
 * etc.) are not available.  The recolored texture is used directly with the
 * vanilla entity render type, so the shader pack sees an already-recolored
 * texture and renders it normally.</p>
 *
 * <h3>Cache key</h3>
 * Variant parameters are quantized to 6-bit precision per channel (64 levels)
 * to avoid floating-point key explosion while keeping visually distinguishable
 * variants in separate cache slots.
 *
 * <h3>Thread safety</h3>
 * The cache is a {@link ConcurrentHashMap} for safe access from the render
 * thread.  Texture creation must happen on the render thread (OpenGL context).
 *
 * <h3>Future: greyscale tinting</h3>
 * When textures are greyscale, a simple {@code RenderSystem.setShaderColor()}
 * multiplication achieves the same result with zero texture overhead.  This
 * cache is only needed for the palette-swap recolor-band system on non-greyscale
 * textures.
 */
public final class RecolorTextureCache {

    private RecolorTextureCache() {
    }

    /**
     * Quantized cache key — avoids float equality issues.
     */
    private record RecolorKey(
            ResourceLocation texture,
            int baseColorPacked,
            int variantColorPacked,
            int paramsPacked
    ) {
    }

    private static final Map<RecolorKey, ResourceLocation> cache = new ConcurrentHashMap<>();

    /**
     * Returns a {@link ResourceLocation} pointing to a texture with the
     * recolor-band applied.  If no recoloring is needed (amount ≈ 0), the
     * original texture is returned unchanged.
     *
     * <p>Results are cached — the first call for a given combination does
     * the CPU work + GPU upload; subsequent calls return instantly.</p>
     */
    public static ResourceLocation getRecoloredTexture(
            ResourceLocation original,
            float baseR, float baseG, float baseB, float baseRange,
            float varR, float varG, float varB, float varAmount) {

        if (varAmount < 0.01F) return original;

        RecolorKey key = makeKey(original, baseR, baseG, baseB, baseRange, varR, varG, varB, varAmount);

        ResourceLocation cached = cache.get(key);
        if (cached != null) return cached;

        ResourceLocation recolored = buildRecoloredTexture(original, baseR, baseG, baseB, baseRange, varR, varG, varB, varAmount);
        if (recolored == null) return original; // fallback on failure
        cache.put(key, recolored);
        return recolored;
    }

    /**
     * Clears the cache and releases all dynamic textures.
     * Call on resource reload or world unload to free VRAM.
     */
    public static void clear() {
        var texManager = Minecraft.getInstance().getTextureManager();
        for (ResourceLocation loc : cache.values()) {
            texManager.release(loc);
        }
        cache.clear();
    }

    private static RecolorKey makeKey(
            ResourceLocation tex,
            float bR, float bG, float bB, float bRange,
            float vR, float vG, float vB, float vAmt) {
        int basePacked = packRGB(bR, bG, bB);
        int varPacked = packRGB(vR, vG, vB);
        int paramPacked = (quantize(bRange) << 8) | quantize(vAmt);
        return new RecolorKey(tex, basePacked, varPacked, paramPacked);
    }

    /**
     * 6-bit quantize (0–63) to reduce cache fragmentation.
     */
    private static int quantize(float f) {
        return Math.min(63, Math.max(0, Math.round(f * 63.0F)));
    }

    private static int packRGB(float r, float g, float b) {
        return (quantize(r) << 12) | (quantize(g) << 6) | quantize(b);
    }

    /**
     * Reads the original texture from the resource pack, applies the recolor
     * band logic per pixel (matching the fragment shader), and uploads the
     * result as a {@link DynamicTexture}.
     */
    private static ResourceLocation buildRecoloredTexture(
            ResourceLocation original,
            float baseR, float baseG, float baseB, float baseRange,
            float varR, float varG, float varB, float varAmount) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(original);
            if (resource.isEmpty()) {
                LMHelperClass.logWarning("", "[Lycanites Mobs] Recolor: texture not found: " + original);
                return null;
            }

            NativeImage image;
            try (InputStream stream = resource.get().open()) {
                image = NativeImage.read(stream);
            }

            int width = image.getWidth();
            int height = image.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getPixelRGBA(x, y);

                    float r = (pixel & 0xFF) / 255.0F;
                    float g = ((pixel >> 8) & 0xFF) / 255.0F;
                    float b = ((pixel >> 16) & 0xFF) / 255.0F;
                    int a = (pixel >> 24) & 0xFF;

                    float dr = r - baseR;
                    float dg = g - baseG;
                    float db = b - baseB;
                    float dist = (float) Math.sqrt(dr * dr + dg * dg + db * db);

                    float influence = baseRange > 0.0F
                            ? Math.max(0.0F, Math.min(1.0F, 1.0F - dist / baseRange))
                            : 0.0F;

                    float blend = varAmount * influence;
                    if (blend > 0.001F) {
                        r = r + (varR - r) * blend;
                        g = g + (varG - g) * blend;
                        b = b + (varB - b) * blend;

                        int ri = Math.min(255, Math.max(0, Math.round(r * 255.0F)));
                        int gi = Math.min(255, Math.max(0, Math.round(g * 255.0F)));
                        int bi = Math.min(255, Math.max(0, Math.round(b * 255.0F)));

                        image.setPixelRGBA(x, y, ri | (gi << 8) | (bi << 16) | (a << 24));
                    }
                }
            }

            DynamicTexture dynamicTexture = new DynamicTexture(image);

            String basePath = original.getPath().replace('/', '_').replace('.', '_');
            RecolorKey key = makeKey(original, baseR, baseG, baseB, baseRange, varR, varG, varB, varAmount);
            String uniqueName = "recolor/" + basePath + "_" + Integer.toHexString(key.hashCode());
            ResourceLocation recoloredLoc = new ResourceLocation(original.getNamespace(), uniqueName);

            Minecraft.getInstance().getTextureManager().register(recoloredLoc, dynamicTexture);

            LMHelperClass.logDebug("", "[Lycanites Mobs] Recolored texture cached: " + recoloredLoc);
            return recoloredLoc;

        } catch (Exception e) {
            LMHelperClass.logWarning("", "[Lycanites Mobs] Failed to recolor texture: " + original + " — " + e.getMessage());
            return null;
        }
    }
}
