package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public class EntityTremor extends TameableCreatureEntity implements Enemy {

    public EntityTremor(EntityType<? extends EntityTremor> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;

        this.setupMob();

        this.setMaxUpStep(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.getCommandSenderWorld().isClientSide && this.isRareVariant() && !this.isPetType("familiar")) {
            // Random Charging:
            if (this.hasAttackTarget() && this.distanceTo(this.getTarget()) > 1 && this.getRandom().nextInt(20) == 0) {
                if (this.position().y() - 1 > this.getTarget().position().y())
                    this.leap(6.0F, -1.0D, this.getTarget());
                else if (this.position().y() + 1 < this.getTarget().position().y())
                    this.leap(6.0F, 1.0D, this.getTarget());
                else
                    this.leap(6.0F, 0D, this.getTarget());
            }
        }

        // Particles:
        if (this.getCommandSenderWorld().isClientSide)
            for (int i = 0; i < 2; ++i) {
                this.getCommandSenderWorld().addParticle(ParticleTypes.SMOKE, this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
            }
    }

    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if (!super.attackMelee(target, damageScale))
            return false;

        // Explosion:
        int explosionStrength = Math.max(0, this.creatureInfo.getFlag("explosionStrength", 1));
        Level.ExplosionInteraction explosionMode = explosionStrength > 0 && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
        if (this.isPetType("familiar")) {
            explosionStrength = 1;
            explosionMode = Level.ExplosionInteraction.NONE;
        }
        this.getCommandSenderWorld().explode(this, this.position().x(), this.position().y(), this.position().z(), explosionStrength, explosionMode);

        return true;
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        var entity = source.getEntity();
        if (entity instanceof WitherBoss) {
            return true;
        }
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS)) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            this.heal(damage);
            return false;
        }
        return super.hurt(source, damage);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.getEffect() == MobEffects.WITHER) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean canBurn() {
        return false;
    }

    @Override
    public boolean canBeTargetedBy(LivingEntity entity) {
        if (entity instanceof WitherBoss) {
            return false;
        }
        return super.canBeTargetedBy(entity);
    }
}
