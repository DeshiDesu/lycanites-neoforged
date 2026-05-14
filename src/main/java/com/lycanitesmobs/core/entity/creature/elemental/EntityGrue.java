package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import com.lycanitesmobs.core.data.info.ObjectLists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class EntityGrue extends TameableCreatureEntity implements Enemy {
    public static final byte ATTACK_NONE = 0, ATTACK_SWIPE = 1, ATTACK_BITE = 2;
    private static final EntityDataAccessor<Byte> ATTACK_ANIM = SynchedEntityData.defineId(EntityGrue.class, EntityDataSerializers.BYTE);

    private int teleportTime = 60;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityGrue(EntityType<? extends EntityGrue> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;
        this.spawnsInWater = true;
        this.setupMob();

        this.setMaxUpStep(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new StealthGoal(this).setStealthTime(20).setStealthAttack(true).setStealthMove(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Random Target Teleporting:
        if (!this.getCommandSenderWorld().isClientSide && this.hasAttackTarget()) {
            if (this.teleportTime-- <= 0) {
                this.teleportTime = 60 + this.getRandom().nextInt(40);
                BlockPos teleportPosition = this.getFacingPosition(this.getTarget(), -this.getTarget().getDimensions(Pose.STANDING).width - 1D, 0);
                if (this.canTeleportTo(teleportPosition)) {
                    this.playJumpSound();
                    this.setPos(teleportPosition.getX(), teleportPosition.getY(), teleportPosition.getZ());
                }
            }
        }

        // Particles:
        if (this.getCommandSenderWorld().isClientSide)
            for (int i = 0; i < 2; ++i) {
                this.getCommandSenderWorld().addParticle(ParticleTypes.WITCH, this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
            }
    }

    /**
     * Checks if this entity can teleport to the provided block position.
     *
     * @param pos The position to teleport to.
     * @return True if it's safe to teleport.
     */
    public boolean canTeleportTo(BlockPos pos) {
        for (int y = 0; y <= 1; y++) {
            BlockState blockState = this.getCommandSenderWorld().getBlockState(pos.offset(0, y, 0));
            if (blockState.canOcclude())
                return false;
        }
        return true;
    }


    // ==================================================
    //                     Stealth
    // ==================================================
    @Override
    public boolean canStealth() {
        if (this.getCommandSenderWorld().isClientSide) return false;
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
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if (!super.attackMelee(target, damageScale))
            return false;

        boolean bite = this.getRandom().nextFloat() < 0.5F;
        this.startAttackAnim(bite);
        // Leech:
        if (this.isRareVariant() && target instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) target;
            List<MobEffect> goodEffects = new ArrayList<>();
            for (MobEffectInstance effectInstance : targetLiving.getActiveEffects()) {
                if (ObjectLists.inEffectList("buffs", effectInstance.getEffect()))
                    goodEffects.add(effectInstance.getEffect());
            }
            if (goodEffects.size() > 0) {
                if (goodEffects.size() > 1)
                    targetLiving.removeEffect(goodEffects.get(this.getRandom().nextInt(goodEffects.size())));
                else
                    targetLiving.removeEffect(goodEffects.get(0));
                float leeching = Math.max(1, this.getAttackDamage(damageScale) / 2);
                this.heal(leeching);
            }
        }

        return true;
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACK_ANIM, ATTACK_NONE);
    }

    public void startAttackAnim(boolean bite) {
        this.entityData.set(ATTACK_ANIM, bite ? ATTACK_BITE : ATTACK_SWIPE);
    }

    public byte getAttackAnimType() {
        return this.entityData.get(ATTACK_ANIM);
    }

    public float getMeleeAttackAnim(float pt) {
        int max = Math.max(1, this.getAttackCooldownMax());
        float cur = this.attackCooldown;
        float t = (max - (cur - pt)) / (float) max;
        return Mth.clamp(t, 0F, 1F);
    }

    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public boolean isStrongSwimmer() {
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

    /**
     * Returns true if this mob should be damaged by the sun.
     **/
    public boolean daylightBurns() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }


    // ==================================================
    //                       Visuals
    // ==================================================
    @Override
    public ResourceLocation getTexture(String suffix) {
        if (!this.hasCustomName() || !"Shadow Clown".equals(this.getCustomName().getString()))
            return super.getTexture(suffix);

        String textureName = this.getTextureName() + "_shadowclown";
        if (!"".equals(suffix)) {
            textureName += "_" + suffix;
        }
        if (TextureManager.getTexture(textureName) == null)
            TextureManager.addTexture(textureName, "textures/entity/" + textureName.toLowerCase() + ".png");
        return TextureManager.getTexture(textureName);
    }
}
