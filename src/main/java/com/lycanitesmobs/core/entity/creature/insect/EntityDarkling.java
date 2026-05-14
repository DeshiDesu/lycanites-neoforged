package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class EntityDarkling extends TameableCreatureEntity implements Enemy {

    // Data Manager:
    protected static final EntityDataAccessor<Integer> LATCH_TARGET = SynchedEntityData.defineId(EntityDarkling.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> LATCH_HEIGHT = SynchedEntityData.defineId(EntityDarkling.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> LATCH_ANGLE = SynchedEntityData.defineId(EntityDarkling.class, EntityDataSerializers.FLOAT);

    // Latching
    LivingEntity latchEntity = null;
    int latchEntityID = 0;
    double latchHeight = 0.5D;
    double latchAngle = 90D;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityDarkling(EntityType<? extends EntityDarkling> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEAD;
        this.hasAttackSound = true;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new StealthGoal(this).setStealthTime(20).setStealthAttack(true).setStealthMove(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
    }

    // ========== Init ==========


    /**
     * Initiates the entity setting all the values to be watched by the datawatcher.
     **/
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LATCH_TARGET, 0);
        this.entityData.define(LATCH_HEIGHT, (float) this.latchHeight);
        this.entityData.define(LATCH_ANGLE, (float) this.latchAngle);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Leap:
        if (!this.getCommandSenderWorld().isClientSide && this.hasAttackTarget() && !this.hasLatchTarget() && this.onGround() && !this.getCommandSenderWorld().isClientSide && this.random.nextInt(10) == 0)
            this.leap(6.0F, 0.6D, this.getTarget());

        // Latch:
        if (this.hasLatchTarget()) {
            this.noPhysics = true;

            // Movement:
            Vector3d latchPos = this.getFacingPositionDouble(this.getLatchTarget().position().x(), this.getLatchTarget().position().y() + (this.getLatchTarget().getDimensions(Pose.STANDING).height * this.latchHeight), this.getLatchTarget().position().z(), this.getLatchTarget().getDimensions(Pose.STANDING).width * 0.5D, this.latchAngle);
            this.setPos(latchPos.x, latchPos.y, latchPos.z);
            double distanceX = this.getLatchTarget().position().x() - this.position().x();
            double distanceZ = this.getLatchTarget().position().z() - this.position().z();
            this.yBodyRot = this.yRot = -((float) Mth.atan2(distanceX, distanceZ)) * (180F / (float) Math.PI);

            // Server:
            if (!this.getCommandSenderWorld().isClientSide) {
                if (this.getLatchTarget().isAlive() && !this.isInWater()) {
                    this.setTarget(this.getLatchTarget());
                    if (this.updateTick % 40 == 0) {
                        float damage = this.getAttackDamage(1);
                        if (this.attackMelee(this.getLatchTarget(), damage))
                            this.heal(damage * 2);
                    }
                } else {
                    this.setPos(this.getLatchTarget().position().x(), this.getLatchTarget().position().y(), this.getLatchTarget().position().z());
                    this.setLatchTarget(null);
                    this.noPhysics = false;
                }
            }

            // Client:
            else {
                for (int i = 0; i < 2; ++i) {
                    this.getCommandSenderWorld().addParticle(DustParticleOptions.REDSTONE, this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
                }
            }
        } else
            this.noPhysics = false;
    }


    // ==================================================
    //                     Latching
    // ==================================================
    public LivingEntity getLatchTarget() {
        try {
            if (this.getCommandSenderWorld().isClientSide) {
                this.latchHeight = this.entityData.get(LATCH_HEIGHT);
                this.latchAngle = this.entityData.get(LATCH_ANGLE);
                int latchEntityID = this.getEntityData().get(LATCH_TARGET);
                if (latchEntityID != this.latchEntityID) {
                    this.latchEntity = null;
                    this.latchEntityID = latchEntityID;
                    if (latchEntityID != 0) {
                        Entity possilbeLatchEntity = this.getCommandSenderWorld().getEntity(latchEntityID);
                        if (possilbeLatchEntity != null && possilbeLatchEntity instanceof LivingEntity)
                            this.latchEntity = (LivingEntity) possilbeLatchEntity;
                    }
                }
            }
        } catch (Exception e) {
        }
        return this.latchEntity;
    }

    public void setLatchTarget(LivingEntity entity) {
        this.latchEntity = entity;
        if (this.getCommandSenderWorld().isClientSide)
            return;
        if (entity == null) {
            this.getEntityData().set(LATCH_TARGET, 0);
            return;
        }
        if (ObjectManager.getEffect("repulsion") != null) {
            if ((entity).hasEffect(ObjectManager.getEffect("repulsion"))) {
                this.getEntityData().set(LATCH_TARGET, 0);
                this.latchEntity = null;
                return;
            }
        }
        this.getEntityData().set(LATCH_TARGET, entity.getId());
        this.latchHeight = 0.25D + (0.75D * this.getRandom().nextDouble());
        this.latchAngle = 360 * this.getRandom().nextDouble();
        this.entityData.set(LATCH_HEIGHT, (float) this.latchHeight);
        this.entityData.set(LATCH_ANGLE, (float) this.latchAngle);
    }

    public boolean hasLatchTarget() {
        return this.getLatchTarget() != null;
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Can Attack Class ==========
    @Override
    public boolean canAttackType(EntityType targetType) {
        if (this.hasLatchTarget())
            return false;
        return super.canAttackType(targetType);
    }

    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        // Disable Knockback:
        double targetKnockbackResistance = 0;
        if (target instanceof LivingEntity) {
            targetKnockbackResistance = ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
            ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        }

        // Melee Attack:
        if (!super.attackMelee(target, damageScale))
            return false;

        // Restore Knockback:
        if (target instanceof LivingEntity)
            ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);

        // Latch:
        if (!this.hasLatchTarget() && target instanceof LivingEntity && !this.isInWater()) {
            this.setLatchTarget((LivingEntity) target);
        }

        return true;
    }

    /**
     * Returns true if this entity can see the provided entity.
     **/
    @Override
    public boolean hasLineOfSight(Entity target) {
        if (target == this.getLatchTarget()) {
            return true;
        }
        return super.hasLineOfSight(target);
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    // ========== Movement ==========
    @Override
    public boolean canClimb() {
        return true;
    }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
    }


    // ==================================================
    //                     Stealth
    // ==================================================
    @Override
    public boolean canStealth() {
        if (this.getCommandSenderWorld().isClientSide) return false;
        if (this.isMoving()) return false;
        return this.testLightLevel() <= 0;
    }

    @Override
    public void startStealth() {
        if (this.getCommandSenderWorld().isClientSide) {
            ParticleOptions particle = ParticleTypes.WITCH;
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            for (int i = 0; i < 100; i++)
                this.getCommandSenderWorld().addParticle(particle, this.position().x() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, this.position().y() + 0.5D + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).height), this.position().z() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, d0, d1, d2);
        }
        super.startStealth();
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
