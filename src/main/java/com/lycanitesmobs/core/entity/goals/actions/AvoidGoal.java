package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import org.joml.Vector3d;

import java.util.EnumSet;


public class AvoidGoal extends Goal {
    // Targets:
    private BaseCreatureEntity host;
    private LivingEntity avoidTarget;
    private BlockPos avoidPos;

    // Properties:
    private double farSpeed = 1.0D;
    private double nearSpeed = 1.2D;
    private double farDistance = 4096.0D;
    private double nearDistance = 49.0D;
    private Class targetClass;
    private float distanceFromEntity = 6.0F;
    private Path pathEntity;
    private int findRandomTargetAwayFromCooldown = 0;
    private int findRandomTargetAwayFromCooldownMax = 60;

    // ==================================================
    //                    Constructor
    // ==================================================
    public AvoidGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }


    // ==================================================
    //                  Set Properties
    // ==================================================
    public AvoidGoal setFarSpeed(double setSpeed) {
        this.farSpeed = setSpeed;
        return this;
    }

    public AvoidGoal setNearSpeed(double setSpeed) {
        this.nearSpeed = setSpeed;
        return this;
    }

    public AvoidGoal setFarDistance(double dist) {
        this.farDistance = dist * dist;
        return this;
    }

    public AvoidGoal setNearDistance(double dist) {
        this.nearDistance = dist * dist;
        return this;
    }

    public AvoidGoal setTargetClass(Class setTargetClass) {
        this.targetClass = setTargetClass;
        return this;
    }


    // ==================================================
    //                  Should Execute
    // ==================================================
    public boolean canUse() {
        this.avoidTarget = this.host.getAvoidTarget();
        if (this.avoidTarget == null)
            return false;

        if (!this.avoidTarget.isAlive())
            return false;

        if (this.targetClass != null && !this.targetClass.isAssignableFrom(this.avoidTarget.getClass()))
            return false;

        if (this.host.distanceToSqr(this.avoidTarget) >= this.farDistance)
            return false;

        if (this.findRandomTargetAwayFromCooldown > 0) {
            this.findRandomTargetAwayFromCooldown--;
            return false;
        }

        Vector3d avoidVector = RandomPositionGenerator.findRandomTargetAwayFrom(
                this.host,
                (int) Math.sqrt(this.farDistance),
                7,
                new Vector3d(this.avoidTarget.position().x(), this.avoidTarget.position().y(), this.avoidTarget.position().z())
        );
        if (avoidVector == null) {
            this.findRandomTargetAwayFromCooldown = this.findRandomTargetAwayFromCooldownMax;
            return false;
        }
        var vec3i = LMHelperClass.convertToVec3i(avoidVector);
        this.avoidPos = new BlockPos(vec3i.getX(), vec3i.getY(), vec3i.getZ());

        if (this.avoidTarget.distanceToSqr(avoidVector.x, avoidVector.y, avoidVector.z) < this.avoidTarget.distanceToSqr(this.host))
            return false;

        if (!this.host.useDirectNavigator()) {
            this.pathEntity = this.host.getNavigation().createPath(avoidVector.x, avoidVector.y, avoidVector.z, 0);
            if (this.pathEntity == null)
                return false;
        }

        return true;
    }


    // ==================================================
    //                 Continue Executing
    // ==================================================
    @Override
    public boolean canContinueToUse() {
        if (this.avoidTarget == null || this.host.getAvoidTarget() == null)
            return false;

        if (!this.host.useDirectNavigator() && this.host.getNavigation().isDone())
            return false;
        if (this.host.useDirectNavigator() && this.host.directNavigator.atTargetPosition())
            return false;

        if (this.host.distanceToSqr(this.avoidTarget) >= this.farDistance)
            return false;

        return true;
    }


    // ==================================================
    //                      Start
    // ==================================================
    @Override
    public void start() {
        if (!this.host.useDirectNavigator())
            this.host.getNavigation().moveTo(this.pathEntity, this.farSpeed);
        else if (this.avoidPos != null)
            this.host.directNavigator.setTargetPosition(this.avoidPos, this.farSpeed);
    }


    // ==================================================
    //                      Reset
    // ==================================================
    @Override
    public void stop() {
        this.avoidTarget = null;
    }


    // ==================================================
    //                      Update
    // ==================================================
    @Override
    public void tick() {
        double distanceSq = this.host.distanceToSqr(this.avoidTarget);

        if (distanceSq < this.nearDistance) {
            if (!this.host.useDirectNavigator())
                this.host.getNavigation().setSpeedModifier(this.nearSpeed);
            else
                this.host.directNavigator.speedModifier = this.nearSpeed;
        } else {
            if (!this.host.useDirectNavigator())
                this.host.getNavigation().setSpeedModifier(this.farSpeed);
            else
                this.host.directNavigator.speedModifier = this.farSpeed;
        }
    }

}
