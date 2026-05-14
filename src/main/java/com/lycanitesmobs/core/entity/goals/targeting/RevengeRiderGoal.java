package com.lycanitesmobs.core.entity.goals.targeting;

import com.google.common.base.Predicate;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Iterator;
import java.util.List;

public class RevengeRiderGoal extends FindAttackTargetGoal {

    // Targets:
    private TameableCreatureEntity host;

    // Properties:
    private int revengeTime;

    // ==================================================
    //                    Constructor
    // ==================================================
    public RevengeRiderGoal(TameableCreatureEntity setHost) {
        super(setHost);
        this.host = setHost;
        this.tameTargeting = true;
    }


    // ==================================================
    //                  Set Properties
    // ==================================================
    public RevengeRiderGoal setHelpCall(boolean setHelp) {
        this.callForHelp = setHelp;
        return this;
    }

    public RevengeRiderGoal setSightCheck(boolean setSightCheck) {
        this.checkSight = setSightCheck;
        return this;
    }

    public RevengeRiderGoal setOnlyNearby(boolean setNearby) {
        this.nearbyOnly = setNearby;
        return this;
    }

    public RevengeRiderGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
        this.cantSeeTimeMax = setCantSeeTimeMax;
        return this;
    }


    // ==================================================
    //                  Should Execute
    // ==================================================
    public boolean canUse() {
        if (!this.host.hasRiderTarget())
            return false;
        if (this.host.getRider() == null)
            return false;
        int i = this.getRiderRevengeTime();
        if (i == this.revengeTime)
            return false;
        if (!this.isEntityTargetable(this.getRiderRevengeTarget(), false))
            return false;
        return true;
    }


    // ==================================================
    //                 Start Executing
    // ==================================================
    public void start() {
        this.target = this.getRiderRevengeTarget();
        this.revengeTime = this.getRiderRevengeTime();

        try {
            if (this.callForHelp) {
                double d0 = this.getTargetDistance();
                List allies = this.host.getCommandSenderWorld().getEntitiesOfClass(this.host.getClass(), this.host.getBoundingBox().inflate(d0, 4.0D, d0), (Predicate<Entity>) input -> input instanceof LivingEntity);
                Iterator possibleAllies = allies.iterator();

                while (possibleAllies.hasNext()) {
                    BaseCreatureEntity possibleAlly = (BaseCreatureEntity) possibleAllies.next();
                    if (possibleAlly != this.host && possibleAlly.getTarget() == null && !possibleAlly.isAlliedTo(this.target))
                        possibleAlly.setTarget(this.target);
                }
            }
        } catch (Exception e) {
            LMHelperClass.logWarning("", "An exception occurred when selecting help targets in rider revenge, this has been skipped to prevent a crash.");
            e.printStackTrace();
        }

        super.start();
    }


    // ==================================================
    //                  Continue Executing
    // ==================================================
    @Override
    public boolean shouldStopTargeting(LivingEntity target) {
        return target != this.getRiderRevengeTarget();
    }


    // ==================================================
    //                    Rider Revenge
    // ==================================================
    public LivingEntity getRiderRevengeTarget() {
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.host.getRider());
        if (extendedEntity != null) {
            return extendedEntity.lastAttackedEntity;
        }
        return null;
    }

    public int getRiderRevengeTime() {
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.host.getRider());
        if (extendedEntity != null) {
            return extendedEntity.lastAttackedTime;
        }
        return 0;
    }
}
