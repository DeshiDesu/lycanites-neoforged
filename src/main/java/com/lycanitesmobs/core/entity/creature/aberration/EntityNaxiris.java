package com.lycanitesmobs.core.entity.creature.aberration;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class EntityNaxiris extends RideableCreatureEntity {
    public boolean griefing = true;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityNaxiris(EntityType<? extends EntityNaxiris> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = false;

        this.setAttackCooldownMax(20);
        this.solidCollision = true;
        this.setupMob();

        this.setMaxUpStep(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.25D).setRange(40.0F).setMinChaseDistance(10.0F).setLongMemory(false));
    }

    @Override
    public void loadCreatureFlags() {
        this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    @Override
    public void riderEffects(LivingEntity rider) {
        if (rider.hasEffect(MobEffects.DIG_SLOWDOWN))
            rider.removeEffect(MobEffects.DIG_SLOWDOWN);
        if (rider.hasEffect(ObjectManager.getEffect("weight")))
            rider.removeEffect(ObjectManager.getEffect("weight"));
    }


    // ==================================================
    //                    Taking Damage
    // ==================================================
    // ========== On Damage ==========

    /**
     * Called when this mob has received damage.
     **/
    public void onDamage(DamageSource damageSrc, float damage) {
        super.onDamage(damageSrc, damage);

        Entity damageEntity = damageSrc.getEntity();
        if (damageEntity != null && ("mob".equals(damageSrc.getMsgId()) || "player".equals(damageSrc.getMsgId()))) {

            // Eat Buffs:
            if (damageEntity instanceof LivingEntity) {
                LivingEntity targetLiving = (LivingEntity) damageEntity;
                List<MobEffect> goodEffects = new ArrayList<>();
                for (MobEffectInstance effect : targetLiving.getActiveEffects()) {
                    if (ObjectLists.inEffectList("buffs", effect.getEffect()))
                        goodEffects.add(effect.getEffect());
                }
                if (goodEffects.size() > 0 && this.getRandom().nextBoolean()) {
                    if (goodEffects.size() > 1)
                        targetLiving.removeEffect(goodEffects.get(this.getRandom().nextInt(goodEffects.size())));
                    else
                        targetLiving.removeEffect(goodEffects.get(0));
                    float leeching = damage * 1.1F;
                    this.heal(leeching);
                }
            }
        }
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
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("arcanelaserstorm", target, range, 0, new Vector3d(0, 0, 0), 0.6f, 2f, 1F);
        super.attackRanged(target, range);
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        var entity = source.getEntity();
        if (entity instanceof EntityNaxiris)
            return true;
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

    @Override
    public double getMountedZOffset() {
        return (double) this.getDimensions(Pose.STANDING).width * -0.2D;
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
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("arcanelaserstorm");
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
