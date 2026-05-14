package com.lycanitesmobs.client.effect;

import com.lycanitesmobs.core.data.config.ConfigClient;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side handler for fear audio muffling and heartbeat lifecycle.
 * <p>
 * When the player has the {@code fear} MobEffect, all sounds are muffled with
 * volume reduction and pitch lowering to simulate suppressed hearing. A looping
 * heartbeat sound plays for the duration of the effect, exempt from muffling.
 * <p>
 * Heartbeat playback speed is controlled by the {@code fearHeartbeatSpeed}
 * client config combined with the fear amplifier level. The config value sets
 * a base pitch (0.5 = half speed / deeper, 2.0 = double speed / higher) and
 * the fear amplifier scales on top. OpenAL handles seamless looping — no
 * retrigger scheduling is needed.
 * <p>
 * Muffling intensity scales with fear amplifier (+{@value MUFFLE_PER_LEVEL} per level).
 * Distance-based attenuation creates a shrinking "hearing bubble" — sounds at
 * the player's position retain more volume, while those at or beyond the configured
 * audio range receive full muffling.
 * <p>
 * The heartbeat amplitude envelope (decoded from the OGG by {@link HeartbeatAmplitudeMap})
 * is exposed via {@link #getHeartbeatAmplitude()} for the visual flicker system.
 */
@OnlyIn(Dist.CLIENT)
public class FearAudioHandler {

    private static float currentMuffle = 0.0F;
    private static FearHeartbeatSound activeHeartbeat = null;
    private static long heartbeatStartTick = 0;
    private static long tickCount = 0;
    /** Accumulated audio-time offset from pitch changes mid-playback. */
    private static float audioTimeAccum = 0.0F;
    private static float lastPitch = 1.0F;

    private static final float MUFFLE_PER_LEVEL = 0.25F;
    private static final float LERP_SPEED = 0.03F;
    private static final float MIN_DISTANCE_MUFFLE = 0.3F;
    private static final float MAX_PITCH_REDUCTION = 0.5F;

    /** Base speed factor at Fear I. */
    private static final float BASE_SPEED = 0.85F;
    /** Additional speed per fear amplifier level. */
    private static final float SPEED_PER_LEVEL = 0.1625F;
    /** Maximum fear-amplifier speed multiplier (Fear V cap). */
    private static final float MAX_FEAR_SPEED = 1.5F;

    public static void tick() {
        tickCount++;
        float target = 0.0F;
        boolean hasFear = false;
        int amplifier = 0;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            MobEffect fear = ObjectManager.getEffect("fear");
            if (fear != null && player.hasEffect(fear)) {
                hasFear = true;
                MobEffectInstance instance = player.getEffect(fear);
                amplifier = instance != null ? instance.getAmplifier() : 0;

                float baseMuffle = getConfigMuffleFactor();
                target = Mth.clamp(baseMuffle + amplifier * MUFFLE_PER_LEVEL, 0.0F, 1.0F);
            }
        }

        currentMuffle = approach(currentMuffle, target, LERP_SPEED);

        if (hasFear && mc.level != null) {
            HeartbeatAmplitudeMap.ensureLoaded();
            float effectivePitch = computeEffectivePitch(amplifier);

            if (activeHeartbeat == null || activeHeartbeat.isStopped()) {
                if (FearVisualHandler.isBlockDimSettled()) {
                    SoundEvent heartbeatEvent = ObjectManager.getSound("effect_heartbeat");
                    if (heartbeatEvent != null) {
                        activeHeartbeat = new FearHeartbeatSound(heartbeatEvent, effectivePitch);
                        heartbeatStartTick = tickCount;
                        audioTimeAccum = 0.0F;
                        lastPitch = effectivePitch;
                        mc.getSoundManager().play(activeHeartbeat);
                    }
                }
            } else {
                if (effectivePitch != lastPitch) {
                    long elapsed = tickCount - heartbeatStartTick;
                    audioTimeAccum += (elapsed / 20.0F) * lastPitch;
                    heartbeatStartTick = tickCount;
                    lastPitch = effectivePitch;
                }
                activeHeartbeat.updatePitch(effectivePitch);
            }
        } else {
            if (activeHeartbeat != null && !activeHeartbeat.isStopped()) {
                activeHeartbeat.markStopped();
                mc.getSoundManager().stop(activeHeartbeat);
            }
            activeHeartbeat = null;
            audioTimeAccum = 0.0F;
        }
    }

    public static float getHeartbeatAmplitude() {
        if (activeHeartbeat == null || activeHeartbeat.isStopped()) return -1.0F;
        if (!HeartbeatAmplitudeMap.isReady()) return -1.0F;

        long elapsedTicks = tickCount - heartbeatStartTick;
        float audioTimeSec = audioTimeAccum + (elapsedTicks / 20.0F) * lastPitch;
        return HeartbeatAmplitudeMap.getAmplitude(audioTimeSec);
    }

    public static float getMuffleFactor() {
        return currentMuffle;
    }

    public static boolean isActive() {
        return currentMuffle > 0.001F;
    }

    public static float computeVolumeScale(SoundInstance sound) {
        if (!isActive()) return 1.0F;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return 1.0F;

        float audioRange = getConfigAudioRange();
        double distance;

        if (sound.isRelative()) {
            distance = 0.0;
        } else {
            double dx = sound.getX() - player.getX();
            double dy = sound.getY() - player.getY();
            double dz = sound.getZ() - player.getZ();
            distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        float distanceFactor = Mth.clamp((float) (distance / audioRange), 0.0F, 1.0F);
        float muffle = currentMuffle * Mth.lerp(distanceFactor, MIN_DISTANCE_MUFFLE, 1.0F);
        return Math.max(1.0F - muffle, 0.0F);
    }

    public static float computePitchScale() {
        if (!isActive()) return 1.0F;
        return 1.0F - currentMuffle * MAX_PITCH_REDUCTION;
    }

    private static float approach(float current, float target, float speed) {
        if (current < target) {
            return Math.min(current + speed, target);
        } else if (current > target) {
            return Math.max(current - speed, target);
        }
        return current;
    }

    private static float getConfigMuffleFactor() {
        if (ConfigClient.INSTANCE != null && ConfigClient.INSTANCE.fearMuffleFactor != null) {
            return Mth.clamp(ConfigClient.INSTANCE.fearMuffleFactor.get().floatValue(), 0.0F, 1.0F);
        }
        return 0.8F;
    }

    private static float getConfigAudioRange() {
        if (ConfigClient.INSTANCE != null && ConfigClient.INSTANCE.fearAudioRange != null) {
            return Math.max(ConfigClient.INSTANCE.fearAudioRange.get().floatValue(), 1.0F);
        }
        return 8.0F;
    }

    private static float getConfigHeartbeatSpeed() {
        if (ConfigClient.INSTANCE != null && ConfigClient.INSTANCE.fearHeartbeatSpeed != null) {
            return Mth.clamp(ConfigClient.INSTANCE.fearHeartbeatSpeed.get().floatValue(), 0.1F, 4.0F);
        }
        return 1.0F;
    }

    /**
     * Combines the config base speed with the fear-amplifier scaling to produce
     * the final pitch value used by the looping heartbeat sound.
     */
    private static float computeEffectivePitch(int amplifier) {
        float configSpeed = getConfigHeartbeatSpeed();
        float fearSpeed = Math.min(BASE_SPEED + amplifier * SPEED_PER_LEVEL, MAX_FEAR_SPEED);
        return Mth.clamp(configSpeed * fearSpeed, 0.1F, 4.0F);
    }
}
