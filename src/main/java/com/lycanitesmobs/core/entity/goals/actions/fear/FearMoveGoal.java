package com.lycanitesmobs.core.entity.goals.actions.fear;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.creature.aberration.EntityFear;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.*;

/**
 * Applies fear movement to players near the EntityFear.
 * Replaces the old FearHandler's per-player tug/flee logic.
 * <p>
 * The EntityFear acts as the fear source: nearby feared players are pushed away
 * from the fear entity with wobble at higher amplifiers, or experience erratic
 * "ghost tugs" when the entity is far away. The entity's orbiting movement via
 * {@link HauntPlayerGoal} naturally creates dynamic, shifting push directions.
 */
public class FearMoveGoal extends Goal {

    private final EntityFear fearEntity;

    private static final double EFFECT_RANGE = 12.0;
    private static final double STEP_HEIGHT_BOOST = 0.5;
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("a5f6a820-97d3-4be8-a05e-7c3e6e3d8f1a");

    // --- Amplifier scaling ---
    private static final float MAX_AMPLIFIER = 4.0F;

    // --- Sourceless ghost-pull tuning (when entity is far from player) ---
    private static final int TUG_DURATION = 8;
    private static final int TUG_PAUSE_MIN_AT_MAX = 12;
    private static final int TUG_PAUSE_MAX_AT_MAX = 30;
    private static final int TUG_PAUSE_MIN_AT_MIN = 40;
    private static final int TUG_PAUSE_MAX_AT_MIN = 65;
    private static final double TUG_STRENGTH_AT_MIN = 0.04;
    private static final double TUG_STRENGTH_AT_MAX = 0.11;
    private static final double TUG_ANGLE_JITTER = Math.PI * 0.7;

    // --- Proximity flee tuning (entity acts as source) ---
    private static final double FLEE_RANGE = 8.0;
    private static final double FLEE_STRENGTH_AT_MIN = 0.025;
    private static final double FLEE_STRENGTH_AT_MAX = 0.055;
    private static final double FLEE_WOBBLE_MAX = Math.PI * 0.18;

    /** Per-player tug state for the sourceless ghost-pull when entity is far. */
    private final Map<UUID, TugState> tugStates = new HashMap<>();

    /** Track which players have step height applied. */
    private final Set<UUID> steppedPlayers = new HashSet<>();

    public FearMoveGoal(EntityFear fearEntity) {
        this.fearEntity = fearEntity;
        this.setFlags(EnumSet.noneOf(Flag.class)); // Doesn't block other goals.
    }

    @Override
    public boolean canUse() {
        return this.fearEntity.getHauntTarget() != null
                && this.fearEntity.getHauntTarget().isAlive()
                && !this.fearEntity.getCommandSenderWorld().isClientSide;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        List<Player> fearedPlayers = this.fearEntity.getFearedPlayersNearby(EFFECT_RANGE);
        Set<UUID> activeIds = new HashSet<>();

        for (Player player : fearedPlayers) {
            if (isImmune(player)) continue;

            activeIds.add(player.getUUID());
            applyStepHeightBoost(player);

            MobEffect fear = ObjectManager.getEffect("fear");
            MobEffectInstance inst = fear != null ? player.getEffect(fear) : null;
            int amplifier = inst != null ? inst.getAmplifier() : 0;
            float scale = Mth.clamp(amplifier / MAX_AMPLIFIER, 0.0F, 1.0F);

            double distToEntity = player.distanceTo(this.fearEntity);
            double pushX, pushZ;

            if (distToEntity <= FLEE_RANGE) {
                // Entity is close — push player away from entity with wobble.
                double dx = player.getX() - this.fearEntity.getX();
                double dz = player.getZ() - this.fearEntity.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);

                double fleeAngle;
                if (dist < 0.01) {
                    fleeAngle = player.getRandom().nextDouble() * Math.PI * 2.0;
                } else {
                    fleeAngle = Math.atan2(dz, dx);
                }
                fleeAngle += (player.getRandom().nextDouble() - 0.5) * 2.0 * FLEE_WOBBLE_MAX;

                double fleeStrength = Mth.lerp(scale, FLEE_STRENGTH_AT_MIN, FLEE_STRENGTH_AT_MAX);
                pushX = Math.cos(fleeAngle) * fleeStrength;
                pushZ = Math.sin(fleeAngle) * fleeStrength;
            } else {
                // Entity is far — erratic ghost tugs.
                TugState state = tugStates.computeIfAbsent(player.getUUID(), k -> new TugState(player));
                state.tugPhase++;

                if (state.tugPhase <= 0) {
                    pushX = 0;
                    pushZ = 0;
                    if (state.tugPhase == 0) {
                        state.tugAngle += (player.getRandom().nextDouble() - 0.5) * 2.0 * TUG_ANGLE_JITTER;
                        state.tugPhase = 1;
                    }
                } else {
                    double tugStrength = Mth.lerp(scale, TUG_STRENGTH_AT_MIN, TUG_STRENGTH_AT_MAX);
                    pushX = Math.cos(state.tugAngle) * tugStrength;
                    pushZ = Math.sin(state.tugAngle) * tugStrength;

                    if (state.tugPhase >= TUG_DURATION) {
                        int pauseMin = (int) Mth.lerp(scale, TUG_PAUSE_MIN_AT_MIN, TUG_PAUSE_MIN_AT_MAX);
                        int pauseMax = (int) Mth.lerp(scale, TUG_PAUSE_MAX_AT_MIN, TUG_PAUSE_MAX_AT_MAX);
                        int pause = pauseMin + player.getRandom().nextInt(Math.max(pauseMax - pauseMin + 1, 1));
                        state.tugPhase = -pause;
                    }
                }
            }

            if (pushX != 0 || pushZ != 0) {
                Vec3 current = player.getDeltaMovement();
                double clampedY = Math.min(current.y(), 0.0);
                player.setDeltaMovement(current.x() + pushX, clampedY, current.z() + pushZ);

                if (player instanceof ServerPlayer sp) {
                    try {
                        sp.connection.send(new ClientboundSetEntityMotionPacket(player));
                        MessageEntityVelocity msg = new MessageEntityVelocity(sp, pushX, 0, pushZ);
                        LycanitesMobs.PACKET_MANAGER.sendToPlayer(msg, sp);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Clean up step height for players no longer affected.
        Iterator<UUID> it = steppedPlayers.iterator();
        while (it.hasNext()) {
            UUID id = it.next();
            if (!activeIds.contains(id)) {
                Player p = this.fearEntity.getCommandSenderWorld().getPlayerByUUID(id);
                if (p != null) removeStepHeightBoost(p);
                it.remove();
            }
        }

        // Clean up stale tug states.
        tugStates.keySet().retainAll(activeIds);
    }

    @Override
    public void stop() {
        // Remove step height from all tracked players.
        for (UUID id : steppedPlayers) {
            Player p = this.fearEntity.getCommandSenderWorld().getPlayerByUUID(id);
            if (p != null) removeStepHeightBoost(p);
        }
        steppedPlayers.clear();
        tugStates.clear();
    }

    private static boolean isImmune(Player player) {
        if (player instanceof ServerPlayer sp) {
            GameType mode = sp.gameMode.getGameModeForPlayer();
            return mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
        }
        return false;
    }

    private void applyStepHeightBoost(LivingEntity entity) {
        if (steppedPlayers.contains(entity.getUUID())) return;
        AttributeInstance attr = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attr != null && attr.getModifier(STEP_HEIGHT_UUID) == null) {
            attr.addTransientModifier(new AttributeModifier(
                    STEP_HEIGHT_UUID, "Fear step height boost", STEP_HEIGHT_BOOST, AttributeModifier.Operation.ADDITION
            ));
        }
        steppedPlayers.add(entity.getUUID());
    }

    private static void removeStepHeightBoost(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attr != null) {
            attr.removeModifier(STEP_HEIGHT_UUID);
        }
    }

    /** Per-player state for the sourceless ghost-tug mechanic. */
    private static class TugState {
        int tugPhase;
        double tugAngle;

        TugState(Player player) {
            this.tugAngle = player.getRandom().nextDouble() * Math.PI * 2;
            this.tugPhase = -(TUG_PAUSE_MIN_AT_MIN + player.getRandom().nextInt(TUG_PAUSE_MAX_AT_MIN - TUG_PAUSE_MIN_AT_MIN));
        }
    }
}
