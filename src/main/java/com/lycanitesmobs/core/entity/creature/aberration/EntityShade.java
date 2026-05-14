package com.lycanitesmobs.core.entity.creature.aberration;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.List;

public class EntityShade extends RideableCreatureEntity {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityShade(EntityType<? extends EntityShade> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;
        this.hasJumpSound = true;
        this.canGrow = false;
        this.setupMob();

        this.setMaxUpStep(1.0F);
        this.attackCooldownMax = 40;
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setSpeed(1.5D));
    }


    // ==================================================
    //                   Mount Ability
    // ==================================================
    @Override
    public void mountAbility(Entity rider) {
        if (this.getCommandSenderWorld().isClientSide)
            return;

        if (this.abilityToggled)
            return;
        if (this.getStamina() < this.getStaminaCost())
            return;

        this.specialAttack();
        this.applyStaminaCost();
    }

    public float getStaminaCost() {
        return 100;
    }

    public int getStaminaRecoveryWarmup() {
        return 5 * 20;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }


    // ==================================================
    //                     Movement
    // ==================================================
    // Mounted Y Offset:
    @Override
    public double getPassengersRidingOffset() {
        return (double) this.getDimensions(Pose.STANDING).height * 0.85D;
    }

    @Override
    public double getMountedZOffset() {
        return (double) this.getDimensions(Pose.STANDING).width * 0.25D;
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if (!super.attackMelee(target, damageScale))
            return false;

        // Leech:
        float leeching = this.getEffectStrength(this.getAttackDamage(damageScale) / 4);
        this.heal(leeching);

        if (this.getRandom().nextFloat() <= 0.1F)
            this.specialAttack();

        return true;
    }

    // ========== Special Attack ==========
    public void specialAttack() {
        // Horrific Howl:
        double distance = 5.0D;
        List<LivingEntity> possibleTargets = this.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(distance, distance, distance), possibleTarget -> {
            if (!possibleTarget.isAlive()
                    || possibleTarget == EntityShade.this
                    || EntityShade.this.isEntityPassenger(possibleTarget, EntityShade.this)
                    || EntityShade.this.isAlliedTo(possibleTarget)
                    || !EntityShade.this.canAttackType(possibleTarget.getType())
                    || !EntityShade.this.canAttack(possibleTarget))
                return false;
            return true;
        });
        if (!possibleTargets.isEmpty()) {
            for (LivingEntity possibleTarget : possibleTargets) {
                boolean doDamage = true;
                if (this.getRider() instanceof Player) {
                    if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent((Player) this.getRider(), possibleTarget))) {
                        doDamage = false;
                    }
                }
                if (doDamage) {
                    if (ObjectManager.getEffect("fear") != null) {
                        possibleTarget.addEffect(new MobEffectInstance(ObjectManager.getEffect("fear"), this.getEffectDuration(5), 1));
                        if (possibleTarget instanceof Player player) {
                            EntityFear.spawnForPlayer(player, this);
                        }
                    } else {
                        possibleTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10 * 20, 0));
                    }
                }
            }
        }
        this.playAttackSound();
        this.triggerAttackCooldown();
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    public boolean canBeTempted() {
        return true;
    }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
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
    //                     Immunities
    // ==================================================
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.IN_WALL)) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public float getFallResistance() {
        return 10;
    }
}
