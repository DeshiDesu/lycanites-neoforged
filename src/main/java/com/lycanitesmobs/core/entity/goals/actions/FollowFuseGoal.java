package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.IFusable;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.Entity;

public class FollowFuseGoal extends FollowGoal {
    // Targets:
    BaseCreatureEntity host;

    // Fusion:
    double fuseRange = 2;

    // ==================================================
    //                    Constructor
    // ==================================================
    public FollowFuseGoal(BaseCreatureEntity setHost) {
        super(setHost);
        this.host = setHost;
        this.strayDistance = 0;
    }


    // ==================================================
    //                  Should Execute
    // ==================================================
    @Override
    public boolean canUse() {
        if (this.host.isBoss()) {
            return false;
        }
        return super.canUse();
    }


    // ==================================================
    //                  Set Properties
    // ==================================================
    public FollowFuseGoal setSpeed(double setSpeed) {
        this.speed = setSpeed;
        return this;
    }

    public FollowFuseGoal setTargetClass(Class setTargetClass) {
        this.targetClass = setTargetClass;
        return this;
    }

    public FollowFuseGoal setStrayDistance(double setDist) {
        this.strayDistance = setDist;
        return this;
    }

    public FollowFuseGoal setLostDistance(double setDist) {
        this.lostDistance = setDist;
        return this;
    }

    public FollowFuseGoal setFuseRange(double setDist) {
        this.fuseRange = setDist;
        return this;
    }


    // ==================================================
    //                       Target
    // ==================================================
    @Override
    public Entity getTarget() {
        if (this.host instanceof IFusable) {
            return (Entity) ((IFusable) this.host).getFusionTarget();
        }
        return null;
    }

    @Override
    public void setTarget(Entity entity) {
        if (this.host instanceof IFusable && entity instanceof IFusable) {
            ((IFusable) this.host).setFusionTarget((IFusable) entity);
        }
    }

    @Override
    public void onTargetDistance(double distance, Entity followTarget) {
        if (distance > this.fuseRange)
            return;

        // Do Fusion:
        if (this.host instanceof IFusable && followTarget instanceof IFusable) {
            this.host.transform(((IFusable) this.host).getFusionType((IFusable) followTarget), followTarget, true);
        }
    }
}
