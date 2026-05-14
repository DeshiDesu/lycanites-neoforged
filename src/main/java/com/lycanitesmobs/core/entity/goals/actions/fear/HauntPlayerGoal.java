package com.lycanitesmobs.core.entity.goals.actions.fear;

import com.lycanitesmobs.core.entity.creature.aberration.EntityFear;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Drives the EntityFear to orbit smoothly around its haunt target.
 * The entity flies in an elliptical path at varying heights and radii,
 * phasing through blocks (noPhysics = true). Movement is lerped for
 * smooth, ghostly motion with occasional direction changes and dives.
 */
public class HauntPlayerGoal extends Goal {

    private final EntityFear fearEntity;

    // --- Orbit parameters ---
    /** Current angle around the target (radians). */
    private double orbitAngle;
    /** Angular speed (radians per tick), positive = CCW. */
    private double orbitSpeed;
    /** Current orbit radius. */
    private double orbitRadius;
    /** Target orbit radius (lerped toward). */
    private double targetRadius;
    /** Current Y offset above the player's eye height. */
    private double heightOffset;
    /** Target height offset (lerped toward). */
    private double targetHeightOffset;

    // --- Smoothing ---
    private static final double POS_LERP = 0.08;
    private static final double RADIUS_LERP = 0.02;
    private static final double HEIGHT_LERP = 0.03;

    // --- Bounds ---
    private static final double MIN_RADIUS = 2.0;
    private static final double MAX_RADIUS = 6.0;
    private static final double MIN_HEIGHT = -1.0;
    private static final double MAX_HEIGHT = 3.5;
    private static final double MIN_ORBIT_SPEED = 0.03;
    private static final double MAX_ORBIT_SPEED = 0.08;

    /** Ticks until the next parameter shift. */
    private int shiftCooldown;

    public HauntPlayerGoal(EntityFear fearEntity) {
        this.fearEntity = fearEntity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.fearEntity.getHauntTarget() != null && this.fearEntity.getHauntTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        Player target = this.fearEntity.getHauntTarget();
        if (target == null) return;

        // Initialize orbit at a random position around the player.
        this.orbitAngle = this.fearEntity.getRandom().nextDouble() * Math.PI * 2.0;
        this.orbitSpeed = randomSpeed();
        this.orbitRadius = randomRadius();
        this.targetRadius = this.orbitRadius;
        this.heightOffset = 1.0 + this.fearEntity.getRandom().nextDouble() * 2.0;
        this.targetHeightOffset = this.heightOffset;
        this.shiftCooldown = 40 + this.fearEntity.getRandom().nextInt(60);
    }

    @Override
    public void tick() {
        Player target = this.fearEntity.getHauntTarget();
        if (target == null) return;

        // Advance the orbit angle.
        this.orbitAngle += this.orbitSpeed;

        // Periodically shift orbit parameters for unpredictable movement.
        this.shiftCooldown--;
        if (this.shiftCooldown <= 0) {
            this.shiftCooldown = 30 + this.fearEntity.getRandom().nextInt(80);
            this.targetRadius = randomRadius();
            this.targetHeightOffset = MIN_HEIGHT + this.fearEntity.getRandom().nextDouble() * (MAX_HEIGHT - MIN_HEIGHT);

            // Occasionally reverse direction.
            if (this.fearEntity.getRandom().nextFloat() < 0.3F) {
                this.orbitSpeed = -this.orbitSpeed;
            }
            // Shift speed slightly.
            this.orbitSpeed = Mth.clamp(
                    this.orbitSpeed + (this.fearEntity.getRandom().nextDouble() - 0.5) * 0.02,
                    -MAX_ORBIT_SPEED, MAX_ORBIT_SPEED
            );
            if (Math.abs(this.orbitSpeed) < MIN_ORBIT_SPEED) {
                this.orbitSpeed = this.orbitSpeed >= 0 ? MIN_ORBIT_SPEED : -MIN_ORBIT_SPEED;
            }
        }

        // Lerp radius and height toward targets.
        this.orbitRadius = lerp(this.orbitRadius, this.targetRadius, RADIUS_LERP);
        this.heightOffset = lerp(this.heightOffset, this.targetHeightOffset, HEIGHT_LERP);

        // Compute desired world position.
        double targetX = target.getX() + Math.cos(this.orbitAngle) * this.orbitRadius;
        double targetY = target.getY() + target.getEyeHeight() + this.heightOffset;
        double targetZ = target.getZ() + Math.sin(this.orbitAngle) * this.orbitRadius;

        // Smooth lerp current position toward desired orbit point.
        double currentX = this.fearEntity.getX();
        double currentY = this.fearEntity.getY();
        double currentZ = this.fearEntity.getZ();

        double newX = lerp(currentX, targetX, POS_LERP);
        double newY = lerp(currentY, targetY, POS_LERP);
        double newZ = lerp(currentZ, targetZ, POS_LERP);

        // Set velocity so the entity drifts smoothly (noPhysics handles wall phasing).
        Vec3 vel = new Vec3(newX - currentX, newY - currentY, newZ - currentZ);
        this.fearEntity.setDeltaMovement(vel);

        // Orient along the full velocity vector — like a squid swimming.
        // Set rotation directly; FearMesh smoothing handles the visual lag for the trailing effect.
        double hDist = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hDist > 0.001) {
            float moveYaw = (float) (Math.atan2(vel.z, vel.x) * (180.0 / Math.PI)) - 90.0F;
            this.fearEntity.setYRot(moveYaw);
        }
        this.fearEntity.yBodyRot = this.fearEntity.getYRot();
        this.fearEntity.yHeadRot = this.fearEntity.getYRot();

        // Pitch: angle from vertical so the mesh tilts from its upright rest pose.
        // The mesh hangs downward in rest (0°). When moving horizontally this gives -90°,
        // tipping the hood forward and letting tendrils trail behind — like a squid swimming.
        // atan2(hDist, -vel.y): 0° when falling, 90° when horizontal, 180° when rising.
        // Negated so the mesh tips backward (trailing) relative to the facing direction.
        double speed = Math.sqrt(vel.x * vel.x + vel.y * vel.y + vel.z * vel.z);
        if (speed > 0.001) {
            float pitch = -(float) (Math.atan2(hDist, -vel.y) * (180.0 / Math.PI));
            this.fearEntity.setXRot(pitch);
        } else {
            this.fearEntity.setXRot(0); // vertical at rest
        }
    }

    @Override
    public void stop() {
        this.fearEntity.setDeltaMovement(Vec3.ZERO);
    }

    private double randomRadius() {
        return MIN_RADIUS + this.fearEntity.getRandom().nextDouble() * (MAX_RADIUS - MIN_RADIUS);
    }

    private double randomSpeed() {
        double speed = MIN_ORBIT_SPEED + this.fearEntity.getRandom().nextDouble() * (MAX_ORBIT_SPEED - MIN_ORBIT_SPEED);
        return this.fearEntity.getRandom().nextBoolean() ? speed : -speed;
    }

    private static double lerp(double current, double target, double factor) {
        return current + (target - current) * factor;
    }
}
