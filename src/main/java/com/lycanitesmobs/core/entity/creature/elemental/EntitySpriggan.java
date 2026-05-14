package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraftforge.common.IPlantable;

public class EntitySpriggan extends TameableCreatureEntity implements Enemy {
    public int farmingRate = 20;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntitySpriggan(EntityType<? extends EntitySpriggan> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = false;

        this.setupMob();

        this.setMaxUpStep(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setStaminaTime(100).setRange(8.0F).setMinChaseDistance(4.0F));
    }

    @Override
    public void loadCreatureFlags() {
        this.farmingRate = this.creatureInfo.getFlag("farmingRate", this.farmingRate);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    private int farmingTick = 0;

    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Water Healing:
        if (this.getAirSupply() >= 0) {
            if (this.isInWater())
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3 * 20, 2));
            else if (this.isInWaterRainOrBubble())
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3 * 20, 1));
        }

        // Farming:
        int currentFarmingRate = this.farmingRate;
        if (this.isTamed() && currentFarmingRate > 0) {
            if (this.isPetType("familiar"))
                currentFarmingRate = this.farmingRate * 10;
            if (this.subspecies != null)
                currentFarmingRate = Math.max(1, Math.round((float) this.farmingRate / 3));
            this.farmingTick++;
            int farmingRange = 16;
            int farmingHeight = 4;
            for (int x = (int) this.position().x() - farmingRange; x <= (int) this.position().x() + farmingRange; x++) {
                for (int y = (int) this.position().y() - farmingHeight; y <= (int) this.position().y() + farmingHeight; y++) {
                    for (int z = (int) this.position().z() - farmingRange; z <= (int) this.position().z() + farmingRange; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        Block farmingBlock = this.getCommandSenderWorld().getBlockState(pos).getBlock();
                        if (farmingBlock instanceof IPlantable && farmingBlock instanceof BonemealableBlock && farmingBlock != Blocks.TALL_GRASS) {

                            // Boost Crops Every X Seconds:
                            if (!this.getCommandSenderWorld().isClientSide && this.getCommandSenderWorld() instanceof ServerLevel && this.farmingTick % (currentFarmingRate) == 0) {
                                /*if(farmingBlock.getTickRandomly()) {
                                    this.getEntityWorld().scheduleBlockUpdate(pos, farmingBlock, currentFarmingRate, 1);
                                }*/

                                BonemealableBlock growableBlock = (BonemealableBlock) farmingBlock;
                                if (growableBlock.isValidBonemealTarget(this.getCommandSenderWorld(), pos, this.getCommandSenderWorld().getBlockState(pos), this.getCommandSenderWorld().isClientSide())) {
                                    if (growableBlock.isBonemealSuccess(this.getCommandSenderWorld(), this.getRandom(), pos, this.getCommandSenderWorld().getBlockState(pos))) {
                                        growableBlock.performBonemeal((ServerLevel) this.getCommandSenderWorld(), this.getRandom(), pos, this.getCommandSenderWorld().getBlockState(pos));
                                    }
                                }
                            }

                            // Crop Growth Effect:
                            if (this.getCommandSenderWorld().isClientSide && this.farmingTick % 40 == 0) {
                                double d0 = this.getRandom().nextGaussian() * 0.02D;
                                double d1 = this.getRandom().nextGaussian() * 0.02D;
                                double d2 = this.getRandom().nextGaussian() * 0.02D;
                                this.getCommandSenderWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, (double) ((float) x + this.getRandom().nextFloat()), (double) y + (double) this.getRandom().nextFloat(), (double) ((float) z + this.getRandom().nextFloat()), d0, d1, d2);
                            }
                        }
                    }
                }
            }
        }

        // Particles:
        if (this.getCommandSenderWorld().isClientSide && !CreatureManager.getInstance().config.disableBlockParticles)
            for (int i = 0; i < 2; ++i) {
                this.getCommandSenderWorld().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TALL_GRASS.defaultBlockState()),
                        this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width,
                        this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height,
                        this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width,
                        0.0D, 0.0D, 0.0D);
            }
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    BaseProjectileEntity projectile = null;

    @Override
    public void attackRanged(Entity target, float range) {
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("lifedrain");
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
    //                    Taking Damage
    // ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
        if (damageSrc.is(DamageTypeTags.IS_FIRE))
            return 2.0F;
        if (damageSrc.getEntity() != null) {
            ItemStack heldItem = ItemStack.EMPTY;
            if (damageSrc.getEntity() instanceof LivingEntity) {
                LivingEntity entityLiving = (LivingEntity) damageSrc.getEntity();
                if (!entityLiving.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                    heldItem = entityLiving.getItemInHand(InteractionHand.MAIN_HAND);
                }
            }
            if (ObjectLists.isAxe(heldItem)) {
                return 2.0F;
            }
        }
        return super.getDamageModifier(damageSrc);
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
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

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
}
