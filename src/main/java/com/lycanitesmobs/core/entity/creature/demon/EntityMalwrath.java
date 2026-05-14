package com.lycanitesmobs.core.entity.creature.demon;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import com.lycanitesmobs.core.entity.creature.aberration.EntityAsmodeus;
import com.lycanitesmobs.core.entity.creature.aberration.EntityAstaroth;
import com.lycanitesmobs.core.entity.creature.aberration.EntityTrite;
import com.lycanitesmobs.core.entity.creature.elemental.EntityWraith;

public class EntityMalwrath extends RideableCreatureEntity {
    public boolean griefing = true;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityMalwrath(EntityType<? extends EntityMalwrath> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEAD;
        this.hasAttackSound = false;

        this.setupMob();

        this.setMaxUpStep(1.0F);
        this.hitAreaWidthScale = 1.5F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.25D).setRange(40.0F).setMinChaseDistance(10.0F).setLongMemory(false));
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.GHAST));
    }

    @Override
    public void loadCreatureFlags() {
        this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        if (!this.getCommandSenderWorld().isClientSide && this.isRareVariant() && this.hasAttackTarget() && this.tickCount % 20 == 0) {
            this.allyUpdate();
        }

        super.aiStep();
    }

    @Override
    public void riderEffects(LivingEntity rider) {
        if (rider.hasEffect(MobEffects.WITHER))
            rider.removeEffect(MobEffects.WITHER);
        if (rider.isOnFire())
            rider.clearFire();
    }

    // ========== Spawn Minions ==========
    public void allyUpdate() {
        if (this.getCommandSenderWorld().isClientSide)
            return;

        // Spawn Minions:
        if (CreatureManager.getInstance().getCreature("wraith").enabled) {
            if (this.nearbyCreatureCount(CreatureManager.getInstance().getCreature("wraith").getEntityType(), 64D) < 10) {
                float random = this.random.nextFloat();
                if (random <= 0.1F) {
                    this.spawnAlly(this.position().x() - 2 + (random * 4), this.position().y(), this.position().z() - 2 + (random * 4));
                }
            }
        }
    }

    public void spawnAlly(double x, double y, double z) {
        EntityWraith minion = (EntityWraith) CreatureManager.getInstance().getCreature("wraith").createEntity(this.getCommandSenderWorld());
        minion.moveTo(x, y, z, this.yRot, this.xRot);
        minion.setMinion(true);
        minion.setMasterTarget(this);
        this.getCommandSenderWorld().addFreshEntity(minion);
        if (this.getTarget() != null) {
            minion.setLastHurtByMob(this.getTarget());
        }
        minion.setSizeScale(this.sizeScale);
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    // ========== Movement ==========
    public boolean isFlying() {
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
    //                      Attacks
    // ==================================================
    // ========== Set Attack Target ==========
    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof EntityTrite || target instanceof EntityAstaroth || target instanceof EntityAsmodeus || target instanceof EntityWraith)
            return false;
        return super.canAttack(target);
    }

    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("demonicblast", target, range, 0, new Vector3d(0, 0, 0), 0.6f, 2f, 1F);
        super.attackRanged(target, range);
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        var entity = source.getEntity();
        if (entity instanceof EntityMalwrath)
            return true;
        if (source.is(DamageTypes.EXPLOSION)) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean canBurn() {
        return false;
    }


    // ==================================================
    //                      Movement
    // ==================================================
    @Override
    public double getPassengersRidingOffset() {
        return (double) this.getDimensions(Pose.STANDING).height * 0.9D;
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

        if (this.hasPickupEntity()) {
            this.dropPickupEntity();
            return;
        }

        if (this.getStamina() < this.getStaminaCost())
            return;

        if (rider instanceof Player) {
            Player player = (Player) rider;
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("demonicblast");
            if (projectileInfo != null) {
                BaseProjectileEntity projectile = projectileInfo.createProjectile(this.getCommandSenderWorld(), player);
                this.getCommandSenderWorld().addFreshEntity(projectile);
                this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                this.triggerAttackCooldown();
            }
        }

        this.applyStaminaCost();
    }

    public float getStaminaCost() {
        return 10;
    }

    public int getStaminaRecoveryWarmup() {
        return 2 * 20;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }


    // ==================================================
    //                   Brightness
    // ==================================================
    @Override
    public float getBrightness() {
        if (isAttackOnCooldown())
            return 1.0F;
        else
            return super.getBrightness();
    }
}