package com.lycanitesmobs.client.effect;

import com.lycanitesmobs.core.data.config.ConfigClient;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side handler for fear visual effects.
 * <p>
 * Manages a two-phase light dimming system:
 * <ol>
 *   <li><b>Phase 1 — Block light:</b> Torches, lanterns, glowstone, etc. dim first.</li>
 *   <li><b>Phase 2 — Sky light:</b> Once block light is fully suppressed, sunlight/moonlight
 *       begins dimming. Only starts once block dim has reached its target.</li>
 * </ol>
 * <p>
 * Total darkness scales with fear amplifier:
 * <ul>
 *   <li>Fear I   (amp 0): base block dim only (default 0.7)</li>
 *   <li>Fear II  (amp 1): block light fully suppressed</li>
 *   <li>Fear III (amp 2): sky light begins dimming</li>
 *   <li>Fear IV  (amp 3): heavy sky dimming</li>
 *   <li>Fear V   (amp 4): near-blindness</li>
 * </ul>
 * Darkness budget = {@code baseDim + amplifier * perLevelDim}. The first 1.0 of that
 * budget goes to block light; any overflow spills into sky light (also capped at 1.0).
 * <p>
 * Light flicker is driven by the heartbeat amplitude envelope — each beat pulse
 * produces extra darkness, and lights recover between beats. Smoothed via separate
 * attack/decay lerp speeds. Red-dominant creature texture pixels glow fullbright
 * via the {@code FearRedGlow} shader uniform, scaling with overall darkness.
 */
@OnlyIn(Dist.CLIENT)
public class FearVisualHandler {

    private static float currentBlockDim = 0.0F;
    private static float currentSkyDim = 0.0F;
    private static float blockDimTarget = 0.0F;
    private static float smoothedAmplitude = 0.0F;
    private static float currentRedGlow = 0.0F;

    private static final float LERP_SPEED = 0.03F;
    private static final float FLICKER_ATTACK = 0.8F;
    private static final float FLICKER_DECAY = 0.8F;

    private static final int[] cleanPixels = new int[256];
    private static final int[] dimmedPixels = new int[256];
    private static DynamicTexture savedLightTexture;
    private static boolean hasSavedPixels = false;

    public static void tick() {
        float blockTarget = 0.0F;
        float skyTarget = 0.0F;
        float redGlowTarget = 0.0F;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            MobEffect fear = ObjectManager.getEffect("fear");
            if (fear != null && player.hasEffect(fear)) {
                MobEffectInstance instance = player.getEffect(fear);
                int amplifier = instance != null ? instance.getAmplifier() : 0;

                float baseDim = getConfigBaseDim();
                float perLevel = getConfigPerLevel();

                float totalDarkness = baseDim + amplifier * perLevel;
                blockTarget = Math.min(totalDarkness, 1.0F);
                skyTarget = Mth.clamp(totalDarkness - 1.0F, 0.0F, 1.0F);
                redGlowTarget = Mth.clamp(totalDarkness * 0.6F, 0.0F, 1.0F);
            }
        }

        blockDimTarget = blockTarget;
        currentBlockDim = approach(currentBlockDim, blockTarget, LERP_SPEED);

        boolean blockSaturated = currentBlockDim >= blockTarget - 0.02F;
        if (blockSaturated || skyTarget <= 0.0F) {
            currentSkyDim = approach(currentSkyDim, skyTarget, LERP_SPEED);
        }

        currentRedGlow = approach(currentRedGlow, redGlowTarget, LERP_SPEED);

        float rawAmplitude = FearAudioHandler.getHeartbeatAmplitude();
        if (rawAmplitude >= 0.0F) {
            float speed = (rawAmplitude > smoothedAmplitude) ? FLICKER_ATTACK : FLICKER_DECAY;
            smoothedAmplitude = approach(smoothedAmplitude, rawAmplitude, speed);
        } else {
            smoothedAmplitude = approach(smoothedAmplitude, 0.0F, FLICKER_DECAY);
        }

        if (currentBlockDim <= 0.0F && currentSkyDim <= 0.0F) {
            hasSavedPixels = false;
        }
    }

    public static float getBlockDimFactor() {
        if (currentBlockDim <= 0.0F) return 0.0F;

        boolean inverted = isFlickerInverted();
        float flickerRoom = inverted ? blockDimTarget : (1.0F - blockDimTarget);
        if (flickerRoom <= 0.02F) return currentBlockDim;

        float flickerStrength = flickerRoom * 0.5F;
        float flickered;
        if (inverted) {
            flickered = currentBlockDim - smoothedAmplitude * flickerStrength;
        } else {
            flickered = currentBlockDim + smoothedAmplitude * flickerStrength;
        }
        return Mth.clamp(flickered, 0.0F, 1.0F);
    }

    public static boolean isBlockDimSettled() {
        return Math.abs(currentBlockDim - blockDimTarget) < 0.02F;
    }

    public static float getSkyDimFactor() {
        return currentSkyDim;
    }

    public static float getRedGlowIntensity() {
        return currentRedGlow;
    }

    private static float approach(float current, float target, float speed) {
        if (current < target) {
            return Math.min(current + speed, target);
        } else if (current > target) {
            return Math.max(current - speed, target);
        }
        return current;
    }

    private static float getConfigBaseDim() {
        if (ConfigClient.INSTANCE != null && ConfigClient.INSTANCE.fearBlockLightDim != null) {
            return Mth.clamp(ConfigClient.INSTANCE.fearBlockLightDim.get().floatValue(), 0.0F, 1.0F);
        }
        return 0.7F;
    }

    private static float getConfigPerLevel() {
        if (ConfigClient.INSTANCE != null && ConfigClient.INSTANCE.fearDimPerLevel != null) {
            return Mth.clamp(ConfigClient.INSTANCE.fearDimPerLevel.get().floatValue(), 0.0F, 1.0F);
        }
        return 0.3F;
    }

    private static boolean isFlickerInverted() {
        if (ConfigClient.INSTANCE != null && ConfigClient.INSTANCE.fearFlickerInverted != null) {
            return ConfigClient.INSTANCE.fearFlickerInverted.get();
        }
        return false;
    }

    public static void saveCleanPixels(NativeImage pixels, DynamicTexture texture) {
        for (int sky = 0; sky < 16; sky++) {
            for (int block = 0; block < 16; block++) {
                cleanPixels[sky * 16 + block] = pixels.getPixelRGBA(block, sky);
            }
        }
        savedLightTexture = texture;
    }

    public static void saveDimmedPixels(NativeImage pixels) {
        for (int sky = 0; sky < 16; sky++) {
            for (int block = 0; block < 16; block++) {
                dimmedPixels[sky * 16 + block] = pixels.getPixelRGBA(block, sky);
            }
        }
        hasSavedPixels = true;
    }

    public static void uploadDimmedForWorld() {
        if (!hasSavedPixels || savedLightTexture == null) return;
        uploadPixels(dimmedPixels);
    }

    public static void restoreCleanAfterWorld() {
        if (!hasSavedPixels || savedLightTexture == null) return;
        uploadPixels(cleanPixels);
    }

    private static void uploadPixels(int[] source) {
        NativeImage pixels = savedLightTexture.getPixels();
        if (pixels == null) return;
        for (int sky = 0; sky < 16; sky++) {
            for (int block = 0; block < 16; block++) {
                pixels.setPixelRGBA(block, sky, source[sky * 16 + block]);
            }
        }
        savedLightTexture.upload();
    }
}
