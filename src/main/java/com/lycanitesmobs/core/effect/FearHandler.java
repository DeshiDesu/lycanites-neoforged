package com.lycanitesmobs.core.effect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.network.message.MessageEntityVelocity;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Handles fear motion logic for a single entity that has the fear effect.
 * Stored on ExtendedEntity — not an entity itself.
 * <p>
 * When no fear source is known, the entity experiences erratic supernatural
 * "tugs" — short bursts of force in shifting directions with pauses between
 * them, as if being yanked around by an invisible presence.
 * <p>
 * When a fear source exists, the entity is pushed away from it with reduced
 * strength and lateral wobble, allowing players to partially resist the pull.
 * <p>
 * Creative and spectator mode players are immune to fear movement.
 */
public class FearHandler {
    private static final double STEP_HEIGHT_BOOST = 0.5;
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("a5f6a820-97d3-4be8-a05e-7c3e6e3d8f1a");

    // --- Amplifier scaling ---
    /** Amplifier at which all values reach their maximum (Fear V = amplifier 4). */
    private static final float MAX_AMPLIFIER = 4.0F;

    // --- Sourceless ghost-pull tuning (min = Fear I, max = Fear V) ---
    /** Ticks a single tug burst lasts. */
    private static final int TUG_DURATION = 8;
    /** Pause range at Fear V (fastest). */
    private static final int TUG_PAUSE_MIN_AT_MAX = 12;
    private static final int TUG_PAUSE_MAX_AT_MAX = 30;
    /** Pause range at Fear I (slowest). */
    private static final int TUG_PAUSE_MIN_AT_MIN = 40;
    private static final int TUG_PAUSE_MAX_AT_MIN = 65;
    /** Tug strength range. */
    private static final double TUG_STRENGTH_AT_MIN = 0.04;
    private static final double TUG_STRENGTH_AT_MAX = 0.11;
    /** Max angular deviation added each new tug (radians). */
    private static final double TUG_ANGLE_JITTER = Math.PI * 0.7;

    // --- Source flee tuning (min = Fear I, max = Fear V) ---
    private static final double FLEE_STRENGTH_AT_MIN = 0.025;
    private static final double FLEE_STRENGTH_AT_MAX = 0.055;
    /** Max lateral wobble angle (radians) applied on top of the flee direction. */
    private static final double FLEE_WOBBLE_MAX = Math.PI * 0.18;

    @Nullable
    private LivingEntity fearSource;
    private double wanderAngle;
    private boolean stepHeightApplied;

    /** Current tug phase: positive = active tug ticks remaining, negative/zero = pause ticks remaining. */
    private int tugPhase;
    /** Direction of the current tug burst. */
    private double tugAngle;

    public FearHandler(@Nullable LivingEntity fearSource) {
        this.fearSource = fearSource;
        this.wanderAngle = Math.random() * Math.PI * 2;
        this.tugAngle = this.wanderAngle;
        this.tugPhase = -(TUG_PAUSE_MIN_AT_MIN + (int) (Math.random() * (TUG_PAUSE_MAX_AT_MIN - TUG_PAUSE_MIN_AT_MIN)));
        this.stepHeightApplied = false;
    }

    public void setFearSource(@Nullable LivingEntity source) {
        this.fearSource = source;
    }

    @Nullable
    public LivingEntity getFearSource() {
        return this.fearSource;
    }

    /**
     * Ticks the fear logic for the given entity.
     *
     * @return false if fear is no longer active and this handler should be removed.
     */
    public boolean tick(LivingEntity entity) {
        if (entity.getCommandSenderWorld().isClientSide) {
            return true;
        }

        MobEffect fear = ObjectManager.getEffect("fear");
        if (fear == null || !entity.hasEffect(fear)) {
            removeStepHeightBoost(entity);
            return false;
        }

        // Creative and spectator players are immune to fear movement.
        if (isImmune(entity)) {
            removeStepHeightBoost(entity);
            return true;
        }

        if (this.fearSource != null && !this.fearSource.isAlive()) {
            this.fearSource = null;
        }

        applyStepHeightBoost(entity);

        // Amplifier scaling: 0.0 at Fear I → 1.0 at Fear V.
        MobEffectInstance inst = entity.getEffect(fear);
        int amplifier = inst != null ? inst.getAmplifier() : 0;
        float scale = Mth.clamp(amplifier / MAX_AMPLIFIER, 0.0F, 1.0F);

        double pushX, pushZ;
        if (this.fearSource != null) {
            // --- Flee from source with wobble ---
            double dx = entity.position().x() - this.fearSource.position().x();
            double dz = entity.position().z() - this.fearSource.position().z();
            double dist = Math.sqrt(dx * dx + dz * dz);

            double fleeAngle;
            if (dist < 0.01) {
                fleeAngle = this.wanderAngle;
            } else {
                fleeAngle = Math.atan2(dz, dx);
            }
            fleeAngle += (entity.getRandom().nextDouble() - 0.5) * 2.0 * FLEE_WOBBLE_MAX;

            double fleeStrength = Mth.lerp(scale, FLEE_STRENGTH_AT_MIN, FLEE_STRENGTH_AT_MAX);
            pushX = Math.cos(fleeAngle) * fleeStrength;
            pushZ = Math.sin(fleeAngle) * fleeStrength;
        } else {
            // --- Sourceless ghost-pull: segmented tugs ---
            tugPhase++;

            if (tugPhase <= 0) {
                pushX = 0;
                pushZ = 0;

                if (tugPhase == 0) {
                    tugAngle += (entity.getRandom().nextDouble() - 0.5) * 2.0 * TUG_ANGLE_JITTER;
                    tugPhase = 1;
                }
            } else {
                double tugStrength = Mth.lerp(scale, TUG_STRENGTH_AT_MIN, TUG_STRENGTH_AT_MAX);
                pushX = Math.cos(tugAngle) * tugStrength;
                pushZ = Math.sin(tugAngle) * tugStrength;

                if (tugPhase >= TUG_DURATION) {
                    // Pause length scales inversely with amplifier (longer at low fear, shorter at high).
                    int pauseMin = (int) Mth.lerp(scale, TUG_PAUSE_MIN_AT_MIN, TUG_PAUSE_MIN_AT_MAX);
                    int pauseMax = (int) Mth.lerp(scale, TUG_PAUSE_MAX_AT_MIN, TUG_PAUSE_MAX_AT_MAX);
                    int pause = pauseMin + entity.getRandom().nextInt(Math.max(pauseMax - pauseMin + 1, 1));
                    tugPhase = -pause;
                }
            }
        }

        if (pushX != 0 || pushZ != 0) {
            Vec3 current = entity.getDeltaMovement();
            double clampedY = Math.min(current.y, 0.0);
            entity.setDeltaMovement(current.x + pushX, clampedY, current.z + pushZ);

            if (entity instanceof ServerPlayer player) {
                try {
                    player.connection.send(new ClientboundSetEntityMotionPacket(entity));
                    MessageEntityVelocity msg = new MessageEntityVelocity(player, pushX, 0, pushZ);
                    LycanitesMobs.PACKET_MANAGER.sendToPlayer(msg, player);
                } catch (Exception ignored) {
                }
            } else {
                entity.hurtMarked = true;
            }
        }

        return true;
    }

    /**
     * Returns true if the entity should be immune to fear movement.
     * Creative and spectator mode players are immune.
     */
    private static boolean isImmune(LivingEntity entity) {
        if (entity instanceof ServerPlayer sp) {
            GameType mode = sp.gameMode.getGameModeForPlayer();
            return mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
        }
        return false;
    }

    private void applyStepHeightBoost(LivingEntity entity) {
        if (this.stepHeightApplied) {
            return;
        }
        AttributeInstance attr = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attr != null && attr.getModifier(STEP_HEIGHT_UUID) == null) {
            attr.addTransientModifier(new AttributeModifier(
                    STEP_HEIGHT_UUID, "Fear step height boost", STEP_HEIGHT_BOOST, AttributeModifier.Operation.ADDITION
            ));
        }
        this.stepHeightApplied = true;
    }

    private void removeStepHeightBoost(LivingEntity entity) {
        if (!this.stepHeightApplied) {
            return;
        }
        AttributeInstance attr = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attr != null) {
            attr.removeModifier(STEP_HEIGHT_UUID);
        }
        this.stepHeightApplied = false;
    }
}
