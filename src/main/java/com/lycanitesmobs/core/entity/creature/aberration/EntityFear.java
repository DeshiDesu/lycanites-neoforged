package com.lycanitesmobs.core.entity.creature.aberration;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.fear.FearMoveGoal;
import com.lycanitesmobs.core.entity.goals.actions.fear.HauntPlayerGoal;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * An invisible, intangible entity that haunts players afflicted with the Fear effect.
 * Spawned when a player gains Fear; circles around them for 15-30 seconds,
 * applying fear movement via {@link FearMoveGoal} and rendering as black particles.
 * Replaces the old FearHandler tug logic with entity-driven AI goals.
 */
public class EntityFear extends BaseCreatureEntity {

    /**
     * The player this fear entity is haunting.
     */
    @Nullable
    public Player hauntTarget;

    private static final Map<UUID, Long> spawnCooldowns = new HashMap<>();

    /**
     * Spawns an EntityFear to haunt the given player, if one isn't already present nearby.
     *
     * @param player The player to haunt.
     * @param source The creature that caused the fear (unused for now, available for future use).
     */
    @SuppressWarnings("unchecked")
    public static void spawnForPlayer(Player player, @Nullable LivingEntity source) {
        Level world = player.getCommandSenderWorld();
        if (world.isClientSide) return;

        MobEffect fear = ObjectManager.getEffect("fear");
        if (fear == null || !player.hasEffect(fear)) return;

        // Expected count = amplitude + 1 (fear 0 → 1 entity, fear 1 → 2, etc.)
        int expectedCount = player.getEffect(fear).getAmplifier() + 1;

        UUID playerUUID = player.getUUID();
        Long lastSpawn = spawnCooldowns.get(playerUUID);
        if (lastSpawn != null && world.getGameTime() - lastSpawn < 20L) return;

        List<EntityFear> existing = world.getEntitiesOfClass(EntityFear.class,
                player.getBoundingBox().inflate(40), e -> player.equals(e.hauntTarget));
        if (existing.size() >= expectedCount) return;

        CreatureInfo fearInfo = CreatureManager.getInstance().creatures.get("fear");
        if (fearInfo == null || fearInfo.entityType == null) return;

        EntityFear fearEntity = (EntityFear) fearInfo.entityType.create(world);
        if (fearEntity == null) return;

        fearEntity.setHauntTarget(player);
        world.addFreshEntity(fearEntity);
        spawnCooldowns.put(playerUUID, world.getGameTime());
    }


    /**
     * Remaining ticks before this entity despawns. Randomized 15-30 seconds (300-600 ticks).
     */
    private int hauntTicksRemaining;

    public EntityFear(EntityType<? extends EntityFear> entityType, Level world) {
        super(entityType, world);

        this.hasStepSound = false;
        this.hasAttackSound = false;
        this.spreadFire = false;

        this.noPhysics = true;
        this.setNoGravity(true);

        // Disable default LookControl — it resets xRot to 0 every tick via resetXRotOnTick(),
        // which kills the pitch tilt set by HauntPlayerGoal. Rotation is fully goal-driven.
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {}
        };

        this.hauntTicksRemaining = 300 + this.random.nextInt(301); // 15-30 seconds

        this.setupMob();
    }

    @Override
    public void setupMob() {
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new HauntPlayerGoal(this));
        this.goalSelector.addGoal(1, new FearMoveGoal(this));
    }

    public void setHauntTarget(@Nullable Player player) {
        this.hauntTarget = player;
        if (player != null) {
            this.moveTo(player.getX(), player.getY() + 1.5, player.getZ(), 0, 0);
        }
    }

    @Nullable
    public Player getHauntTarget() {
        return this.hauntTarget;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.getCommandSenderWorld().isClientSide) {
            return;
        }

        // Countdown and remove.
        this.hauntTicksRemaining--;
        if (this.hauntTicksRemaining <= 0) {
            if (this.hauntTarget != null) spawnCooldowns.remove(this.hauntTarget.getUUID());
            this.discard();
            return;
        }

        // Remove if haunt target changed dimensions (entity lives in a single level).
        if (this.hauntTarget != null && !this.hauntTarget.getCommandSenderWorld().equals(this.getCommandSenderWorld())) {
            spawnCooldowns.remove(this.hauntTarget.getUUID());
            this.discard();
            return;
        }

        // Remove if haunt target is gone or lost fear.
        if (this.hauntTarget == null || !this.hauntTarget.isAlive()) {
            if (this.hauntTarget != null) spawnCooldowns.remove(this.hauntTarget.getUUID());
            this.discard();
            return;
        }
        MobEffect fear = ObjectManager.getEffect("fear");
        if (fear == null || !this.hauntTarget.hasEffect(fear)) {
            spawnCooldowns.remove(this.hauntTarget.getUUID());
            this.discard();
            return;
        }

        // If the target gets too far (e.g. teleport), snap to them.
        double distSq = this.distanceToSqr(this.hauntTarget);
        if (distSq > 30 * 30) {
            this.moveTo(this.hauntTarget.getX(), this.hauntTarget.getY() + 1.5, this.hauntTarget.getZ(), 0, 0);
        }
    }

    /**
     * Returns a list of players within haunt range that have the fear effect.
     */
    public List<Player> getFearedPlayersNearby(double range) {
        MobEffect fear = ObjectManager.getEffect("fear");
        AABB box = this.getBoundingBox().inflate(range);
        return this.getCommandSenderWorld().getEntitiesOfClass(Player.class, box, p -> {
            if (!p.isAlive()) return false;
            if (fear == null) return false;
            return p.hasEffect(fear);
        });
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return false;
    }

    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        return false;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canDespawnNaturally() {
        return false;
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public boolean useDirectNavigator() {
        return true;
    }

    @Override
    public void loadItemDrops() {
    }

    @Override
    public boolean rollWanderChance() {
        return false;
    }

    // ==================================================
    //              World Interaction Prevention
    // ==================================================

    /** Immune to fire and lava — ghosts don't burn. */
    @Override
    public boolean fireImmune() {
        return true;
    }

    /** Never render fire even if somehow set ablaze. */
    @Override
    public boolean isOnFire() {
        return false;
    }

    /** Cannot collide with other entities. */
    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    /** Prevents being targeted by attacks. */
    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        return true;
    }

    /** Immune to block slowdowns (cobweb, sweet berry, powder snow, etc). */
    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
    }

    /** Cannot be pushed by anything. */
    @Override
    public void push(double x, double y, double z) {
    }

    /** No block stepping effects (footsteps, crop trampling, etc). */
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    /** Immune to drowning / fluid interaction. */
    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    /** Cannot follow players through portals. */
    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    /** Not affected by explosions. */
    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    /** Prevents leashing. */
    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }
}
