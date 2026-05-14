package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

public class EntityErepede extends RideableCreatureEntity {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityErepede(EntityType<? extends EntityErepede> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = false;
        this.attackCooldownMax = 10;
        this.setupMob();

        // Stats:
        this.setMaxUpStep(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setRange(14.0F).setMinChaseDistance(6.0F));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Movement Speed Modifier ==========
    @Override
    public float getAISpeedModifier() {
        if (this.hasRiderTarget()) {
            BlockState blockState = this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(0, -1, 0));
            if (Material.SAND.contains(blockState.getBlock()) || (Material.AIR.contains(blockState.getBlock()) && Material.SAND.contains(this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(0, -2, 0)).getBlock())))
                return 1.8F;
        }
        return 1.0F;
    }

    @Override
    public double getPassengersRidingOffset() {
        return (double) this.getDimensions(Pose.STANDING).height * 0.9D;
    }


    // ==================================================
    //                   Mount Ability
    // ==================================================
    public void mountAbility(Entity rider) {
        if (this.getCommandSenderWorld().isClientSide)
            return;

        if (this.abilityToggled)
            return;
        if (this.getStamina() < this.getStaminaCost())
            return;

        if (rider instanceof Player) {
            Player player = (Player) rider;
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("mudshot");
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
        return 5;
    }

    public int getStaminaRecoveryWarmup() {
        return 0;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("mudshot", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
        super.attackRanged(target, range);
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
        if (source.is(DamageTypes.CACTUS)) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public float getFallResistance() {
        return 10;
    }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
    }
}
