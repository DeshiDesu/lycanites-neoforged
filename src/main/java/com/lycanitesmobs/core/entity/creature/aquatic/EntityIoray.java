package com.lycanitesmobs.core.entity.creature.aquatic;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.WanderGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class EntityIoray extends RideableCreatureEntity implements Enemy {

    WanderGoal wanderAI;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityIoray(EntityType<? extends EntityIoray> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.spawnsOnLand = false;
        this.spawnsInWater = true;
        this.hasAttackSound = true;

        this.babySpawnChance = 0D;
        this.canGrow = true;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false).setMaxChaseDistanceSq(4.0F));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setStaminaTime(100).setRange(8.0F).setMinChaseDistance(4.0F).setMountedAttacking(false));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void riderEffects(LivingEntity rider) {
        rider.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, (5 * 20) + 5, 1));
        super.riderEffects(rider);
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // Pathing Weight:
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        int waterWeight = 10;

        Block block = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y, z)).getBlock();
        if (block == Blocks.WATER)
            return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);
        if (this.getCommandSenderWorld().isRaining() && this.getCommandSenderWorld().canSeeSkyFromBelowWater(new BlockPos(x, y, z)))
            return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);

        if (this.getTarget() != null)
            return super.getBlockPathWeight(x, y, z);
        if (this.waterContact())
            return -999999.0F;

        return super.getBlockPathWeight(x, y, z);
    }

    // Swimming:
    @Override
    public boolean isStrongSwimmer() {
        return true;
    }

    // Walking:
    @Override
    public boolean canWalk() {
        return false;
    }

    // ========== Mounted Offset ==========
    @Override
    public double getPassengersRidingOffset() {
        return (double) this.getDimensions(Pose.STANDING).height * 0.6D;
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean canBreatheAir() {
        return false;
    }

    @Override
    public boolean canBurn() {
        return false;
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    BaseProjectileEntity projectile = null;

    @Override
    public void attackRanged(Entity target, float range) {
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("waterjet");
        if (projectileInfo == null) {
            return;
        }

        // Update Laser:
        if (this.projectile != null && this.projectile.isAlive()) {
            this.projectile.projectileLife = 20;
        } else {
            this.projectile = null;
        }

        // Create New Laser:
        if (this.projectile == null) {
            // Type:
            this.projectile = projectileInfo.createProjectile(this.getCommandSenderWorld(), this);

            // Launch:
            this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.getCommandSenderWorld().addFreshEntity(projectile);
        }

        super.attackRanged(target, range);
    }


    // ==================================================
    //                   Mount Ability
    // ==================================================
    BaseProjectileEntity abilityProjectile = null;

    public void mountAbility(Entity rider) {
        if (this.getCommandSenderWorld().isClientSide)
            return;

        if (this.getStamina() < this.getStaminaRecoveryMax() * 2)
            return;

        if (this.hasAttackTarget())
            this.setTarget(null);

        // Update Laser:
        if (this.abilityProjectile != null && this.abilityProjectile.isAlive()) {
            this.abilityProjectile.projectileLife = 20;
        } else {
            this.abilityProjectile = null;
        }

        // Create New Laser:
        if (this.abilityProjectile == null) {
            // Type:
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("waterjet");
            if (projectileInfo == null) {
                return;
            }
            if (this.getControllingPassenger() == null || !(this.getControllingPassenger() instanceof LivingEntity))
                return;

            this.abilityProjectile = projectileInfo.createProjectile(this.getCommandSenderWorld(), this);

            // Launch:
            this.playSound(abilityProjectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.getCommandSenderWorld().addFreshEntity(abilityProjectile);
        }

        this.applyStaminaCost();
    }

    // Dismount:
    @Override
    public void onDismounted(Entity entity) {
        super.onDismounted(entity);
        if (entity != null && entity instanceof LivingEntity) {
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 5 * 20, 1));
        }
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
    //                     Pet Control
    // ==================================================
    @Override
    public boolean petControlsEnabled() {
        return true;
    }
}
