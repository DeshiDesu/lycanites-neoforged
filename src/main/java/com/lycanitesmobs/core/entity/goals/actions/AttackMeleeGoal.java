package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class AttackMeleeGoal extends Goal {
    // Targets:
    private BaseCreatureEntity host;
    private LivingEntity attackTarget;
    private Path pathToTarget;

    // Properties:
    private double speed = 1.0D;
    private Class targetClass;
    private boolean longMemory = true;
    private int attackTime;
    private double attackRange = 0.5D;
    private float maxChaseDistance = 1024F;
    private double damageScale = 1.0D;
    public boolean enabled = true;
    protected int phase = -1;

    // Pathing:
    private int failedPathFindingPenalty;
    private int failedPathFindingPenaltyMax = 5;
    private int failedPathFindingPenaltyPlayerMax = 0;
    private int repathTime;

    // ==================================================
    //                    Constructor
    // ==================================================
    public AttackMeleeGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }


    // ==================================================
    //                  Set Properties
    // ==================================================
    public AttackMeleeGoal setSpeed(double setSpeed) {
        this.speed = setSpeed;
        return this;
    }

    public AttackMeleeGoal setDamageScale(double scale) {
        this.damageScale = scale;
        return this;
    }

    public AttackMeleeGoal setTargetClass(Class setTargetClass) {
        this.targetClass = setTargetClass;
        return this;
    }

    public AttackMeleeGoal setLongMemory(boolean setMemory) {
        this.longMemory = setMemory;
        return this;
    }

    public AttackMeleeGoal setRange(double range) {
        this.attackRange = range;
        return this;
    }

    public AttackMeleeGoal setMaxChaseDistanceSq(float distance) {
        this.maxChaseDistance = distance * distance;
        return this;
    }

    public AttackMeleeGoal setMaxChaseDistance(float distance) {
        this.maxChaseDistance = distance;
        return this;
    }

    public AttackMeleeGoal setMissRate(int rate) {
        this.failedPathFindingPenaltyMax = rate;
        return this;
    }

    public AttackMeleeGoal setEnabled(boolean setEnabled) {
        this.enabled = setEnabled;
        return this;
    }

    /**
     * Sets the battle phase to restrict this goal to.
     *
     * @param phase The phase to restrict to, if below 0 phases are ignored.
     * @return This goal for chaining.
     */
    public AttackMeleeGoal setPhase(int phase) {
        this.phase = phase;
        return this;
    }


    // ==================================================
    //                   Should Execute
    // ==================================================
    @Override
    public boolean canUse() {
        if (!this.enabled)
            return false;

        if (this.phase >= 0 && this.phase != this.host.getBattlePhase()) {
            return false;
        }

        // With Pickup:
        if (this.host.hasPickupEntity() && !this.host.canAttackWithPickup()) {
            return false;
        }

        this.attackTarget = this.host.getTarget();
        if (this.attackTarget == null)
            return false;
        if (!this.attackTarget.isAlive())
            return false;
        if (this.host.distanceToSqr(this.attackTarget.position().x(), this.attackTarget.getBoundingBox().minY, this.attackTarget.position().z()) > this.maxChaseDistance)
            return false;
        if (this.targetClass != null && !this.targetClass.isAssignableFrom(this.attackTarget.getClass()))
            return false;

        if (--this.repathTime <= 0) {
            // Set Path:
            if (!this.host.useDirectNavigator()) {
                if (this.host.isFlying()) {
                    this.pathToTarget = this.host.getNavigation().createPath(this.attackTarget.position().x(), this.attackTarget.getBoundingBox().minY + this.host.getFlightOffset(), this.attackTarget.position().z(), 0);
                } else {
                    this.pathToTarget = this.host.getNavigation().createPath(this.attackTarget, 0);
                }
                this.repathTime = 4 + this.host.getRandom().nextInt(7);

                return this.pathToTarget != null;
            }

            // Set Direct Target:
            else {
                return this.host.directNavigator.setTargetPosition(new BlockPos((int) Math.floor(attackTarget.position().x()), (int) Math.floor(attackTarget.position().y() + this.host.getFlightOffset()), (int) Math.floor(attackTarget.position().z())), this.speed);
            }
        }

        return true;
    }


    // ==================================================
    //                  Continue Executing
    // ==================================================
    @Override
    public boolean canContinueToUse() {
        if (!this.enabled)
            return false;
        if (this.phase >= 0 && this.phase != this.host.getBattlePhase()) {
            return false;
        }
        this.attackTarget = this.host.getTarget();
        if (this.attackTarget == null)
            return false;
        if (this.targetClass != null && !this.targetClass.isAssignableFrom(this.attackTarget.getClass()))
            return false;
        if (!this.host.isAlive() || !this.attackTarget.isAlive())
            return false;
        if (this.host.distanceToSqr(this.attackTarget.position().x(), this.attackTarget.getBoundingBox().minY, this.attackTarget.position().z()) > this.maxChaseDistance)
            return false;
        if (!this.longMemory)
            if (!this.host.useDirectNavigator() && this.host.getNavigation().isDone())
                return false;
            else if (this.host.useDirectNavigator() && (this.host.directNavigator.atTargetPosition() || !this.host.directNavigator.isTargetPositionValid()))
                return false;
        return this.host.positionNearHome(Mth.floor(attackTarget.position().x()), Mth.floor(attackTarget.position().y()), Mth.floor(attackTarget.position().z()));
    }


    // ==================================================
    //                   Start Executing
    // ==================================================
    @Override
    public void start() {
        if (!this.host.useDirectNavigator()) {
            this.host.getNavigation().moveTo(this.pathToTarget, this.speed);
        } else if (attackTarget != null) {
            this.host.directNavigator.setTargetPosition(new BlockPos((int) attackTarget.position().x(), (int) (attackTarget.getBoundingBox().minY + this.host.getFlightOffset()), (int) attackTarget.position().z()), speed);
        }
        this.repathTime = 0;
    }


    // ==================================================
    //                       Reset
    // ==================================================
    @Override
    public void stop() {
        this.host.getNavigation().stop();
        this.host.directNavigator.clearTargetPosition(1.0D);
        this.attackTarget = null;
    }


    // ==================================================
    //                       Update
    // ==================================================
    @Override
    public void tick() {
        this.host.getLookControl().setLookAt(this.attackTarget, 30.0F, 30.0F);

        // Path To Target:
        if (this.longMemory || this.host.getSensing().hasLineOfSight(this.attackTarget)) {
            if (!this.host.useDirectNavigator() && --this.repathTime <= 0) {
                this.repathTime = this.failedPathFindingPenalty + 4 + this.host.getRandom().nextInt(7);
                if (this.host.isFlying()) {
                    this.host.getNavigation().moveTo(this.attackTarget.position().x(), this.attackTarget.getBoundingBox().minY + this.host.getFlightOffset(), this.attackTarget.position().z(), this.speed);
                } else {
                    this.host.getNavigation().moveTo(this.attackTarget, this.speed);
                }
                if (this.host.getNavigation().getPath() != null) {
                    Node finalPathPoint = this.host.getNavigation().getPath().getEndNode();
                    if (finalPathPoint != null && this.attackTarget.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1) {
                        this.failedPathFindingPenalty = 0;
                    } else {
                        this.failedPathFindingPenalty += this.getFailedPathFindingPenalty();
                    }
                } else {
                    this.failedPathFindingPenalty += this.getFailedPathFindingPenalty();
                }
            } else if (this.host.useDirectNavigator()) {
                this.host.directNavigator.setTargetPosition(new BlockPos((int) this.attackTarget.position().x(), (int) (this.attackTarget.getBoundingBox().minY + this.host.getFlightOffset()), (int) this.attackTarget.position().z()), speed);
            }
        }

        // Damage Target:
        //LMHelperClass.logDebug("Attack", "Attack range: " + this.host.getMeleeAttackRange(this.attackTarget, this.attackRange) + "/" + this.host.distanceToSqr(this.attackTarget.position().x(), this.attackTarget.getBoundingBox().minY, this.attackTarget.position().z()));
        if (this.host.distanceToSqr(this.attackTarget.position().x(), this.attackTarget.getBoundingBox().minY, this.attackTarget.position().z()) <= this.host.getMeleeAttackRange(this.attackTarget, this.attackRange)) {
            if (--this.attackTime <= 0) {
                this.attackTime = this.host.getMeleeCooldown();
                if (!this.host.getMainHandItem().isEmpty())
                    this.host.swing(InteractionHand.MAIN_HAND);
                this.host.attackMelee(this.attackTarget, this.damageScale);
            }

            // Move helper won't change the Yaw if the target is already close by
            double d0 = this.host.position().x() - this.attackTarget.position().x();
            double d1 = this.host.position().z() - this.attackTarget.position().z();
            float targetYaw = (float) (Math.atan2(d1, d0) * 180.0D / Math.PI) + 90.0F;
            float currentYaw = this.host.getYRot();
            float delta = Mth.wrapDegrees(targetYaw - currentYaw);
            if (delta < -30.0F) delta = -30.0F;
            if (delta > 30.0F) delta = 30.0F;
            float newYaw = currentYaw + delta;
            this.host.setYRot(newYaw);
            this.host.yBodyRot = newYaw;
            this.host.yHeadRot = newYaw;
        }
    }

    /**
     * Gets the time in ticks to calculate a new melee chase path to the target.
     * This can be expensive for servers but can making chasing bad if too high so a different value is used for players vs other mobs.
     *
     * @return The time in ticks to wait before calculating a new chase path.
     */
    public int getFailedPathFindingPenalty() {
        if (this.attackTarget instanceof Player || this.host.isTamed()) {
            return this.failedPathFindingPenaltyPlayerMax;
        }
        return this.failedPathFindingPenaltyMax;
    }
}
