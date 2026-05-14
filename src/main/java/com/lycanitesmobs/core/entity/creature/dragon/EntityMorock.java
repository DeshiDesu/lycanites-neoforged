package com.lycanitesmobs.core.entity.creature.dragon;

import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class EntityMorock extends RideableCreatureEntity implements Enemy, IGroupHeavy {

    protected boolean wantsToLand;
    protected boolean isLanded;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityMorock(EntityType<? extends EntityMorock> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;
        this.flySoundSpeed = 20;
        this.setupMob();

        this.setMaxUpStep(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Land/Fly:
        if (!this.getCommandSenderWorld().isClientSide) {
            if (this.isLanded) {
                this.wantsToLand = false;
                if (this.hasPickupEntity() || this.getControllingPassenger() != null || this.isLeashed() || this.isInWater() || (!this.isTamed() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean())) {
                    this.leap(1.0D, 1.0D);
                    this.isLanded = false;
                }
            } else {
                if (this.wantsToLand) {
                    if (!this.isLanded && this.isSafeToLand()) {
                        this.isLanded = true;
                    }
                } else {
                    if (!this.hasPickupEntity() && !this.hasAttackTarget() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean()) {
                        this.wantsToLand = true;
                    }
                }
            }
            if (this.hasPickupEntity() || this.getControllingPassenger() != null || this.hasAttackTarget() || this.isInWater()) {
                this.wantsToLand = false;
            } else if (this.isTamed() && !this.isLeashed()) {
                this.wantsToLand = true;
            }
        }

        // Random Leaping:
        if (!this.isTamed() && !this.getCommandSenderWorld().isClientSide) {
            if (this.hasAttackTarget()) {
                if (this.random.nextInt(10) == 0)
                    this.leap(6.0F, 0D, this.getTarget());
            } else {
                if (this.random.nextInt(50) == 0 && this.isMoving())
                    this.leap(2.0D, 0D);
            }
        }
    }

    @Override
    public void riderEffects(LivingEntity rider) {
        if (rider.hasEffect(MobEffects.WEAKNESS))
            rider.removeEffect(MobEffects.WEAKNESS);
        if (rider.hasEffect(MobEffects.DIG_SLOWDOWN))
            rider.removeEffect(MobEffects.DIG_SLOWDOWN);
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Get Wander Position ==========
    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        if (this.wantsToLand || !this.isLanded) {
            BlockPos groundPos;
            for (groundPos = wanderPosition.below(); groundPos.getY() > 0 && this.getCommandSenderWorld().getBlockState(groundPos).getBlock() == Blocks.AIR; groundPos = groundPos.below()) {
            }
            if (this.getCommandSenderWorld().getBlockState(groundPos).isSolid()) {
                return groundPos.above();
            }
        }
        if (this.hasPickupEntity() && this.getPickupEntity() instanceof Player)
            wanderPosition = new BlockPos(wanderPosition.getX(), this.restrictYHeightFromGround(wanderPosition, 6, 14), wanderPosition.getZ());
        return wanderPosition;
    }

    // ========== Get Flight Offset ==========
    public double getFlightOffset() {
        if (!this.wantsToLand) {
            super.getFlightOffset();
        }
        return 0;
    }

    @Override
    public boolean rollWanderChance() {
        if (this.isFlying())
            return this.getRandom().nextDouble() <= 0.25D;
        return this.getRandom().nextDouble() <= 0.008D;
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() {
        return !this.isLanded || this.hasPickupEntity();
    }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public float getFallResistance() {
        return 100;
    }


    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() {
        return 0;
    }

    @Override
    public int getBagSize() {
        return this.creatureInfo.bagSize;
    }

    // ==================================================
    //                      Movement
    // ==================================================
    @Override
    public double getPassengersRidingOffset() {
        return (double) this.getDimensions(Pose.STANDING).height * 0.8D;
    }


    // ==================================================
    //                   Mount Ability
    // ==================================================
    @Override
    public void mountAbility(Entity rider) {
        if (this.abilityToggled)
            return;
        if (this.getStamina() < this.getStaminaCost())
            return;

        this.playJumpSound();
        if (this.getCommandSenderWorld().isClientSide()) {
            this.leap(4.0D, 0D);
        }

        this.applyStaminaCost();
    }

    public float getStaminaCost() {
        return 20;
    }

    public int getStaminaRecoveryWarmup() {
        return 5 * 20;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }
}
