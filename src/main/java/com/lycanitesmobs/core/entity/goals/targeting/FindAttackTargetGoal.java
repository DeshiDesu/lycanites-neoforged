package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class FindAttackTargetGoal extends TargetingGoal {
    // Targets:
    private List<EntityType> targetTypes = new ArrayList<>();
    private List<Class<? extends Entity>> targetClasses = new ArrayList<>();

    // Properties:
    protected boolean targetPlayers;
    private boolean requirePack = false;
    protected boolean tameTargeting = false;

    // ==================================================
    //                    Constructor
    // ==================================================
    public FindAttackTargetGoal(BaseCreatureEntity setHost) {
        super(setHost);
        this.setFlags(EnumSet.of(Flag.TARGET));
    }


    // ==================================================
    //                  Set Properties
    // ==================================================
    public FindAttackTargetGoal setCheckSight(boolean bool) {
        this.checkSight = bool;
        return this;
    }

    public FindAttackTargetGoal addTargets(EntityType... targets) {
        this.targetTypes.addAll(Arrays.asList(targets));
        for (EntityType targetType : targets) {
            this.host.setHostileTo(targetType);
            if (targetType == EntityType.PLAYER)
                this.targetPlayers = true;
        }
        return this;
    }

    public FindAttackTargetGoal addTargets(Class<? extends Entity>... targets) {
        this.targetClasses.addAll(Arrays.asList(targets));
        for (Class<? extends Entity> targetType : targets) {
            this.host.setHostileTo(targetType);
            if (targetType.isAssignableFrom(Player.class))
                this.targetPlayers = true;
        }
        return this;
    }

    public FindAttackTargetGoal setOnlyNearby(boolean setNearby) {
        this.nearbyOnly = setNearby;
        return this;
    }

    public FindAttackTargetGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
        this.cantSeeTimeMax = setCantSeeTimeMax;
        return this;
    }

    public FindAttackTargetGoal setRange(double range) {
        this.targetingRange = range;
        return this;
    }

    public FindAttackTargetGoal setHelpCall(boolean setHelp) {
        this.callForHelp = setHelp;
        return this;
    }

    public FindAttackTargetGoal setTameTargetting(boolean setTargetting) {
        this.tameTargeting = setTargetting;
        return this;
    }

    /**
     * Makes the creature require a pack for this targeting to.
     **/
    public FindAttackTargetGoal requiresPack() {
        this.requirePack = true;
        return this;
    }


    // ==================================================
    //                    Host Target
    // ==================================================
    @Override
    protected LivingEntity getTarget() {
        return this.host.getTarget();
    }

    @Override
    protected void setTarget(LivingEntity newTarget) {
        this.host.setTarget(newTarget);
    }


    // ==================================================
    //                 Valid Target Check
    // ==================================================
    @Override
    protected boolean isValidTarget(LivingEntity target) {
        // Target Type Check:
        if (!this.targetTypes.isEmpty() && !this.targetTypes.contains(target.getType())) {
            return false;
        }

        // Target Class Check:
        if (!this.targetClasses.isEmpty()) {
            boolean isTargetClass = false;
            for (Class<? extends Entity> targetClass : this.targetClasses) {
                if (targetClass.isAssignableFrom(target.getClass())) {
                    isTargetClass = true;
                    break;
                }
            }
            if (!isTargetClass) {
                return false;
            }
        }

        // Tamed Targeting Check:
        if (!this.tameTargeting && this.host.isTamed()) {
            return false;
        }

        // Type Check:
        if (!this.host.canAttackType(target.getType())) {
            return false;
        }

        // Entity Check:
        if (!this.host.canAttack(target)) {
            return false;
        }

        // Relationships Check:
        CreatureRelationshipEntry relationshipEntry = this.host.relationships.getEntry(target);
        if (relationshipEntry != null && !relationshipEntry.canHunt()) {
            return false;
        }

        // Pack Check:
        if (this.requirePack && !this.host.isInPack()) {
            return false;
        }
        return true;
    }


    // ==================================================
    //                   Should Execute
    // ==================================================
    @Override
    public boolean canUse() {
        if (!this.host.isAggressive() || this.host.hasFixateTarget()) {
            return false;
        }

        if (this.targetPlayers) {
            if (this.host.updateTick % 10 != 0) {
                return false;
            }
        } else {
            if (this.host.updateTick % 40 != 0) {
                return false;
            }
        }

        this.target = null;

        double distance = this.getTargetDistance();
        double heightDistance = 4.0D + this.host.getBbHeight();
        if (this.host.useDirectNavigator())
            heightDistance = distance;

        this.target = this.getNewTarget(distance, heightDistance, distance);
        if (this.target == null || !this.host.rollAttackTargetChance(this.target)) {
            return false;
        }
        if (this.callForHelp)
            this.callNearbyForHelp();

        return true;
    }

    @Override
    public boolean shouldStopTargeting(LivingEntity target) {
        return !this.isValidTarget(target);
    }


    // ==================================================
    //                  Get New Target
    // ==================================================
    @Override
    public LivingEntity getNewTarget(double rangeX, double rangeY, double rangeZ) {
        // Faster Player Targeting:
        if (this.targetPlayers) {
            LivingEntity newTarget = null;
            try {
                List<? extends Player> players = this.host.getCommandSenderWorld().players();
                if (!players.isEmpty()) {
                    List<Player> possibleTargets = new ArrayList<>();
                    for (Player player : players) {
                        if (this.targetSelector.test(player)) {
                            possibleTargets.add(player);
                        }
                    }
                    if (!possibleTargets.isEmpty()) {
                        Collections.sort(possibleTargets, this.nearestSorter);
                        newTarget = possibleTargets.get(0);
                    }
                }
            } catch (Exception e) {
                LMHelperClass.logWarning("", "An exception occurred when player target selecting, this has been skipped to prevent a crash.");
                e.printStackTrace();
            }

            // Return player target unless other entities should also be targeted.
            if (newTarget != null) {
                return newTarget;
            }
        }

        if (this.host.updateTick % 40 == 0) {
            return super.getNewTarget(rangeX, rangeY, rangeZ);
        }

        return null;
    }
}
