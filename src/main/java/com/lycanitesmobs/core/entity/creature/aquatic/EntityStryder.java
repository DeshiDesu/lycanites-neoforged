package com.lycanitesmobs.core.entity.creature.aquatic;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class EntityStryder extends RideableCreatureEntity implements IGroupHeavy {
    protected int pickupCooldown = 100;

    public EntityStryder(EntityType<? extends EntityStryder> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.hasAttackSound = true;

        this.babySpawnChance = 0D;
        this.canGrow = true;
        this.setupMob();
        this.hitAreaWidthScale = 1.5f;
        this.hitAreaHeightScale = 1;

        this.setPathfindingMalus(BlockPathTypes.WATER, 0F);
        this.setMaxUpStep(4.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }

    private int pickupTime = 0;

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.getCommandSenderWorld().isClientSide) {
            // Drop Owner When Tamed:
            if (this.isTamed() && this.hasPickupEntity() && this.getPickupEntity() == this.getOwner())
                this.dropPickupEntity();

            // Entity Pickup Update:
            if (this.hasPickupEntity()) {
                ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPickupEntity());
                if (extendedEntity != null)
                    extendedEntity.setPickedUpByEntity(this);

                if (this.isTamed() && !this.canAttack(this.getPickupEntity())) {
                    this.getPickupEntity().addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, this.getEffectDuration(5), 1));
                } else if (this.pickupTime++ % 40 == 0) {
                    this.attackEntityAsMob(this.getPickupEntity(), 0.5F);
                    if (this.getPickupEntity() instanceof LivingEntity) {
                        if (ObjectManager.getEffect("penetration") != null)
                            this.getPickupEntity().addEffect(new MobEffectInstance(ObjectManager.getEffect("penetration"), this.getEffectDuration(5), 1));
                    }
                }
            } else {
                if (this.pickupCooldown > 0)
                    this.pickupCooldown--;
                this.pickupTime = 0;
            }
        }
    }

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

        LivingEntity nearestTarget = this.getNearestEntity(LivingEntity.class, null, 4, false);
        if (this.canPickupEntity(nearestTarget))
            this.pickupEntity(nearestTarget);

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

    @Override
    public float getAISpeedModifier() {
        if (this.isInWater()) // Checks specifically just for water.
            return 1F;
        if (this.waterContact()) // Checks for water, rain, etc.
            return 0.9F;
        return 0.75F;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canStandOnFluid(FluidState fluid) {
        if (this.getControllingPassenger() instanceof Player) {
            Player player = (Player) this.getControllingPassenger();
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            if (playerExt != null && playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.DESCEND)) {
                return false;
            }
        }
        return fluid.is(FluidTags.WATER);
    }

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

    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        BlockPos groundPos;
        for (groundPos = wanderPosition.below(); groundPos.getY() > 0 && !this.getCommandSenderWorld().getBlockState(groundPos).isSolid(); groundPos = groundPos.below()) {
        }
        return groundPos.above();
    }

    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if (!super.attackMelee(target, damageScale))
            return false;

        // Pickup:
        if (target instanceof LivingEntity) {
            LivingEntity entityLivingBase = (LivingEntity) target;
            if (this.canPickupEntity(entityLivingBase) && this.hasLineOfSight(target)) {
                this.pickupEntity(entityLivingBase);
                this.pickupCooldown = 100;
            }
        }

        return true;
    }

    /**
     * Called when this mob has received damage. Here there is a random chance of this mob dropping any picked up entities.
     **/
    @Override
    public void onDamage(DamageSource damageSrc, float damage) {
        if (this.hasPickupEntity() && !this.isTamed() && this.getRandom().nextFloat() <= 0.25F)
            this.dropPickupEntity();
        super.onDamage(damageSrc, damage);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public float getFallResistance() {
        return 20;
    }

    @Override
    public double[] getPickupOffset(Entity entity) {
        return new double[]{0, 5.5D, 0};
    }

    public boolean canPickupEntity(LivingEntity entity) {
        if (!this.isTamed() && this.pickupCooldown > 0)
            return false;
        return super.canPickupEntity(entity);
    }

    public void dropPickupEntity() {
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPickupEntity());
        if (extendedEntity != null)
            extendedEntity.setPickedUpByEntity(null);
        this.pickupEntity = null;
    }

    @Override
    public int getNoBagSize() {
        return 0;
    }

    @Override
    public int getBagSize() {
        return this.creatureInfo.bagSize;
    }

    public boolean petControlsEnabled() {
        return true;
    }
}
