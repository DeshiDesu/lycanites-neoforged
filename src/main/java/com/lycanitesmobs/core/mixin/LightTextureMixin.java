package com.lycanitesmobs.core.mixin;

import com.lycanitesmobs.client.effect.FearVisualHandler;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dims the lightmap in two phases when the local player has the fear effect.
 * <p>
 * The lightmap is a 16x16 texture: X = block light level (0-15), Y = sky light level (0-15).
 * <p>
 * <b>Phase 1 — Block light:</b> For each sky-light row, lerp every block-light column
 * toward column 0 (zero block light). Torches, lanterns, etc. appear to go out.
 * <p>
 * <b>Phase 2 — Sky light:</b> Lerp every pixel toward row 0 (zero sky light).
 * Sunlight/moonlight fades. At max, only the ambient minimum remains.
 * <p>
 * Both phases operate on the already-computed vanilla lightmap, just before GPU upload.
 */
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {

    @Shadow
    @Final
    private DynamicTexture lightTexture;

    @Inject(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V"
            )
    )
    private void lycanites$dimLightForFear(float partialTick, CallbackInfo ci) {
        float blockDim = FearVisualHandler.getBlockDimFactor();
        float skyDim = FearVisualHandler.getSkyDimFactor();
        if (blockDim <= 0.0F && skyDim <= 0.0F) return;

        NativeImage pixels = this.lightTexture.getPixels();
        if (pixels == null) return;

        FearVisualHandler.saveCleanPixels(pixels, this.lightTexture);

        if (blockDim > 0.0F) {
            for (int sky = 0; sky < 16; sky++) {
                int refColor = pixels.getPixelRGBA(0, sky);
                int refR = (refColor) & 0xFF;
                int refG = (refColor >> 8) & 0xFF;
                int refB = (refColor >> 16) & 0xFF;

                for (int block = 1; block < 16; block++) {
                    int color = pixels.getPixelRGBA(block, sky);
                    int r = (color) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = (color >> 16) & 0xFF;
                    int a = (color >> 24) & 0xFF;

                    r = r + (int) ((refR - r) * blockDim);
                    g = g + (int) ((refG - g) * blockDim);
                    b = b + (int) ((refB - b) * blockDim);

                    pixels.setPixelRGBA(block, sky, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
        }

        if (skyDim > 0.0F) {
            for (int block = 0; block < 16; block++) {
                int refColor = pixels.getPixelRGBA(block, 0);
                int refR = (refColor) & 0xFF;
                int refG = (refColor >> 8) & 0xFF;
                int refB = (refColor >> 16) & 0xFF;

                for (int sky = 1; sky < 16; sky++) {
                    int color = pixels.getPixelRGBA(block, sky);
                    int r = (color) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = (color >> 16) & 0xFF;
                    int a = (color >> 24) & 0xFF;

                    r = r + (int) ((refR - r) * skyDim);
                    g = g + (int) ((refG - g) * skyDim);
                    b = b + (int) ((refB - b) * skyDim);

                    pixels.setPixelRGBA(block, sky, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
        }

        FearVisualHandler.saveDimmedPixels(pixels);
    }
}
