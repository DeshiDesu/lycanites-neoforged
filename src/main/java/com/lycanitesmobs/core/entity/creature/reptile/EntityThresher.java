package com.lycanitesmobs.core.entity.creature.reptile;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.IGroupBoss;
import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class EntityThresher extends RideableCreatureEntity implements Enemy, IGroupHeavy {
    protected int whirlpoolRange = 8;

    protected int whirlpoolEnergy = 0;
    protected int whirlpoolEnergyMax = 5 * 20;
    protected boolean whirlpoolRecharging = true;
    protected int mountedWhirlpool = 0;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityThresher(EntityType<? extends EntityThresher> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.spawnsOnLand = false;
        this.spawnsInWater = true;
        this.hasAttackSound = true;

        this.babySpawnChance = 0D;
        this.canGrow = true;
        this.setupMob();
        this.hitAreaWidthScale = 2F;
        this.hitAreaHeightScale = 1F;

        // Stats:
        //this.pushthrough = 0.9F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false).setRange(2));
    }

    @Override
    public void loadCreatureFlags() {
        this.whirlpoolRange = this.creatureInfo.getFlag("whirlpoolRange", this.whirlpoolRange);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Whirlpool:
        if (!this.getCommandSenderWorld().isClientSide) {
            if (this.whirlpoolRecharging) {
                if (++this.whirlpoolEnergy >= this.whirlpoolEnergyMax)
                    this.whirlpoolRecharging = false;
            }
            this.whirlpoolEnergy = Math.min(this.whirlpoolEnergy, this.whirlpoolEnergyMax);
            if (this.canWhirlpool()) {
                for (Entity entity : this.getNearbyEntities(Entity.class, null, this.whirlpoolRange)) {
                    if (entity == this || entity == this.getControllingPassenger() || entity instanceof IGroupBoss || entity instanceof IGroupHeavy)
                        continue;
                    if (entity instanceof LivingEntity) {
                        LivingEntity entityLivingBase = (LivingEntity) entity;
                        if (entityLivingBase.hasEffect(ObjectManager.getEffect("weight")) || !this.canAttack(entityLivingBase))
                            continue;
                    }
                    ServerPlayer player = null;
                    if (entity instanceof ServerPlayer) {
                        player = (ServerPlayer) entity;
                        if (player.getAbilities().instabuild)
                            continue;
                    }
                    double xDist = this.position().x() - entity.position().x();
                    double zDist = this.position().z() - entity.position().z();
                    double xzDist = Math.max(Mth.sqrt(LMHelperClass.convertToFloat(xDist * xDist + zDist * zDist)), 0.01D);
                    double factor = 0.1D;
                    entity.push(
                            xDist / xzDist * factor + entity.getDeltaMovement().x() * factor,
                            factor,
                            zDist / xzDist * factor + entity.getDeltaMovement().z() * factor
                    );
                    if (player != null)
                        player.connection.send(new ClientboundSetEntityMotionPacket(entity));
                }
                if (--this.whirlpoolEnergy <= 0)
                    this.whirlpoolRecharging = true;
            }
        }

        if (this.mountedWhirlpool > 0)
            this.mountedWhirlpool--;
    }

    @Override
    public void riderEffects(LivingEntity rider) {
        rider.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, (5 * 20) + 5, 1));
        if (rider.hasEffect(ObjectManager.getEffect("paralysis")))
            rider.removeEffect(ObjectManager.getEffect("paralysis"));
        if (rider.hasEffect(ObjectManager.getEffect("penetration")))
            rider.removeEffect(ObjectManager.getEffect("penetration"));
    }

    // ========== Extra Animations ==========

    /**
     * An additional animation boolean that is passed to all clients through the animation mask.
     **/
    public boolean extraAnimation01() {
        if (this.getCommandSenderWorld().isClientSide) {
            return super.extraAnimation01();
        }
        return this.canWhirlpool();
    }

    // ========== Whirlpool ==========
    public boolean canWhirlpool() {
        if (this.getCommandSenderWorld().isClientSide) {
            return this.extraAnimation01();
        }

        // Out of Water:
        if (!this.isInWater()) {
            return false;
        }

        // Mounted:
        if (this.getControllingPassenger() != null && this.mountedWhirlpool > 0) {
            return true;
        }

        // Attack Target:
        return !this.whirlpoolRecharging && this.hasAttackTarget() && this.distanceTo(this.getTarget()) <= (this.whirlpoolRange * 3);
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
        return (double) this.getDimensions(Pose.STANDING).height * 0.5D;
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


    // ==================================================
    //                   Mount Ability
    // ==================================================
    @Override
    public void mountAbility(Entity rider) {
        if (this.getCommandSenderWorld().isClientSide)
            return;

        if (this.getStamina() < this.getStaminaCost()) {
            return;
        }

        this.applyStaminaCost();
        this.mountedWhirlpool = 20;
    }

    @Override
    public float getStaminaCost() {
        return 1;
    }

    @Override
    public int getStaminaRecoveryWarmup() {
        return 2 * 20;
    }

    @Override
    public float getStaminaRecoveryMax() {
        return 2.0F;
    }

    // Dismount:
    @Override
    public void onDismounted(Entity entity) {
        super.onDismounted(entity);
        if (entity instanceof LivingEntity) {
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
