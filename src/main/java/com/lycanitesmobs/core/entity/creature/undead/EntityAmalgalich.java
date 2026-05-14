package com.lycanitesmobs.core.entity.creature.undead;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.IGroupBoss;
import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.util.GoalConditions;
import com.lycanitesmobs.core.entity.goals.actions.FindNearbyPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.*;
import com.lycanitesmobs.core.entity.goals.targeting.CopyMasterAttackTargetGoal;
import com.lycanitesmobs.core.manager.CreatureManager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import com.lycanitesmobs.core.entity.creature.elemental.EntityBanshee;
import com.lycanitesmobs.core.entity.creature.beast.EntityEpion;

public class EntityAmalgalich extends BaseCreatureEntity implements Enemy, IGroupHeavy, IGroupBoss {
    private ForceGoal consumptionGoalP0;
    private ForceGoal consumptionGoalP2;
    private int consumptionDuration = 15 * 20;
    private int consumptionWindUp = 3 * 20;
    private int consumptionAnimationTime = 0;

    public EntityAmalgalich(EntityType<? extends EntityAmalgalich> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEAD;
        this.hasAttackSound = true;
        this.setAttackCooldownMax(30);
        this.hasJumpSound = false;
        //this.pushthrough = 1.0F;
        this.setupMob();
        this.hitAreaWidthScale = 2F;

        // Boss:
        this.damageMax = BaseCreatureEntity.BOSS_DAMAGE_LIMIT;
        this.damageLimit = BaseCreatureEntity.BOSS_DAMAGE_LIMIT;
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(this.nextFindTargetIndex, new FindNearbyPlayersGoal(this));

        this.consumptionDuration = 15 * 20;
        this.consumptionWindUp = 3 * 20;
        this.consumptionAnimationTime = 0;
        int consumptionGoalCooldown = 20 * 20;

        // All Phases:
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new FaceTargetGoal(this));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new HealWhenNoPlayersGoal(this));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new SummonMinionsGoal(this).setMinionInfo("banshee").setAntiFlight(true));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new FireProjectilesGoal(this).setProjectile("spectralbolt").setFireRate(40).setVelocity(1.6F).setScale(8F).setAllPlayers(true).setOverhead(true).setOffset(new Vector3d(0, 6, 0)));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new FireProjectilesGoal(this).setProjectile("spectralbolt").setFireRate(60).setVelocity(1.6F).setScale(8F).setOverhead(true).setOffset(new Vector3d(0, 8, 0)));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new EffectAuraGoal(this).setEffect("decay").setAmplifier(0).setEffectSeconds(5).setRange(52).setCheckSight(false).setTargetTypes(TARGET_TYPES.ENEMY.id));

        // Phase 1:
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new SummonMinionsGoal(this).setMinionInfo("reaper").setSummonRate(20 * 5).setSummonCap(5).setPerPlayer(true)
                .setConditions(new GoalConditions().setBattlePhase(0)));
        this.consumptionGoalP0 = new ForceGoal(this).setRange(64F).setCooldown(consumptionGoalCooldown).setDuration(this.consumptionDuration).setWindUp(this.consumptionWindUp).setForce(-1F).setPhase(0).setDismount(true);
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new EffectAuraGoal(this).setRange(1F).setCooldown(consumptionGoalCooldown + this.consumptionWindUp).setDuration(this.consumptionDuration - this.consumptionWindUp).setTickRate(5).setDamageAmount(1000).setCheckSight(false)
                .setTargetTypes((byte) (TARGET_TYPES.ALLY.id | TARGET_TYPES.ENEMY.id))
                .setConditions(new GoalConditions().setBattlePhase(0)));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, this.consumptionGoalP0);

        // Phase 2:
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new SummonMinionsGoal(this).setMinionInfo("geist").setSummonRate(20 * 5).setSummonCap(5).setPerPlayer(true)
                .setConditions(new GoalConditions().setBattlePhase(1)));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new SummonMinionsGoal(this).setMinionInfo("epion").setSummonRate(20 * 5).setSummonCap(3).setPerPlayer(true)
                .setConditions(new GoalConditions().setBattlePhase(1)));

        // Phase 3:
        this.consumptionGoalP2 = new ForceGoal(this).setRange(64F).setCooldown(consumptionGoalCooldown).setDuration(this.consumptionDuration).setWindUp(this.consumptionWindUp).setForce(-1F).setPhase(2).setDismount(true);
        this.goalSelector.addGoal(this.nextIdleGoalIndex, this.consumptionGoalP2);
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new EffectAuraGoal(this).setRange(1F).setCooldown(consumptionGoalCooldown + this.consumptionWindUp).setDuration(this.consumptionDuration - this.consumptionWindUp).setTickRate(5).setDamageAmount(1000).setCheckSight(false)
                .setTargetTypes((byte) (TARGET_TYPES.ALLY.id | TARGET_TYPES.ENEMY.id))
                .setConditions(new GoalConditions().setBattlePhase(2)));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new FireProjectilesGoal(this).setProjectile("lobdarklings").setFireRate(10 * 20).setVelocity(0.8F).setScale(2F).setRandomCount(3).setAngle(360).setPhase(2));

        super.registerGoals();
    }

    /**
     * Returns a larger bounding box for rendering this large entity.
     **/
    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(200, 50, 200).move(0, -25, 0);
    }

    @Override
    public void onFirstSpawn() {
        super.onFirstSpawn();
        if (this.getArenaCenter() == null) {
            this.setArenaCenter(this.blockPosition());
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Arena Snapping:
        if (this.hasArenaCenter()) {
            BlockPos arenaPos = this.getArenaCenter();
            double arenaY = this.position().y();
            if (this.getCommandSenderWorld().isEmptyBlock(arenaPos))
                arenaY = arenaPos.getY();
            else if (this.getCommandSenderWorld().isEmptyBlock(arenaPos.offset(0, 1, 0)))
                arenaY = arenaPos.offset(0, 1, 0).getY();

            if (this.position().x() != arenaPos.getX() || this.position().y() != arenaY || this.position().z() != arenaPos.getZ())
                this.setPos(arenaPos.getX(), arenaY, arenaPos.getZ());
        }

        // Consumption Animation:
        if (this.getCommandSenderWorld().isClientSide) {
            if (!this.extraAnimation01()) {
                this.consumptionAnimationTime = this.consumptionDuration;
            } else {
                this.consumptionAnimationTime--;
            }
        }
    }

    @Override
    public void updateBattlePhase() {
        double healthNormal = this.getHealth() / this.getMaxHealth();
        if (healthNormal <= 0.2D) {
            this.setBattlePhase(2);
            return;
        }
        if (healthNormal <= 0.6D) {
            this.setBattlePhase(1);
            return;
        }
        this.setBattlePhase(0);
    }

    @Override
    public boolean rollWanderChance() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof EntityBanshee || target instanceof EntityReaper || target instanceof EntityGeist)
            return false;
        return super.canAttack(target);
    }

    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("spectralbolt", target, range, 0, new Vector3d(0, -12, 0), 1.2f, 8f, 0F);
        super.attackRanged(target, range);
    }

    // ========== Consumption Attack ==========
    public float getConsumptionAnimation() {
        if (this.consumptionAnimationTime >= this.consumptionDuration) {
            return 0F;
        }
        int windUpThreshhold = this.consumptionDuration - this.consumptionWindUp;
        if (this.consumptionAnimationTime > windUpThreshhold) {
            return 1F - (float) (this.consumptionAnimationTime - windUpThreshhold) / this.consumptionWindUp;
        }
        float finishingTime = (float) this.consumptionWindUp / 2;
        if (this.consumptionAnimationTime < finishingTime) {
            return (float) this.consumptionAnimationTime / finishingTime;
        }
        return 1F;
    }

    public boolean extraAnimation01() {
        if (this.getCommandSenderWorld().isClientSide) {
            return super.extraAnimation01();
        }

        if (this.getBattlePhase() == 0) {
            return this.consumptionGoalP0.cooldownTime <= 0;
        }
        if (this.getBattlePhase() == 2) {
            return this.consumptionGoalP2.cooldownTime <= 0;
        }

        return super.extraAnimation01();
    }

    @Override
    public boolean addMinion(LivingEntity minion) {
        boolean minionAdded = super.addMinion(minion);
        if (minionAdded && minion instanceof BaseCreatureEntity) {
            BaseCreatureEntity minionCreature = (BaseCreatureEntity) minion;
            minionCreature.targetSelector.addGoal(minionCreature.nextFindTargetIndex++, new CopyMasterAttackTargetGoal(minionCreature));
            if (minion instanceof EntityGeist) {
                minionCreature.goalSelector.addGoal(minionCreature.nextIdleGoalIndex++, new GrowGoal(minionCreature).setGrowthAmount(0.1F).setTickRate(20));
                minionCreature.goalSelector.addGoal(minionCreature.nextIdleGoalIndex++, new SuicideGoal(minionCreature).setCountdown(20 * 20));
            }
        }
        return minionAdded;
    }

    @Override
    public void onMinionDeath(LivingEntity minion, DamageSource damageSource) {
        super.onMinionDeath(minion, damageSource);

        // Shadowfire Clearing:
        if (minion instanceof EntityEpion) {
            int extinguishWidth = 10;
            int extinguishHeight = 30;
            if (!this.getCommandSenderWorld().isClientSide) {
                for (int x = (int) minion.position().x() - extinguishWidth; x <= (int) minion.position().x() + extinguishWidth; x++) {
                    for (int y = (int) minion.position().y() - extinguishHeight; y <= (int) minion.position().y() + 2; y++) {
                        for (int z = (int) minion.position().z() - extinguishWidth; z <= (int) minion.position().z() + extinguishWidth; z++) {
                            Block block = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y, z)).getBlock();
                            if (block == ObjectManager.getBlock("shadowfire")) {
                                BlockPos placePos = new BlockPos(x, y, z);
                                this.getCommandSenderWorld().removeBlock(placePos, true);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onTryToDamageMinion(LivingEntity minion, float damageAmount) {
        super.onTryToDamageMinion(minion, damageAmount);
        if (damageAmount >= 1000) {
            minion.remove(RemovalReason.DISCARDED);
            this.heal(25);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        var entity = source.getEntity();
        if (this.isBlocking())
            return true;
        if (entity instanceof ZombifiedPiglin) {
            entity.remove(RemovalReason.DISCARDED);
            return true;
        }
        if (entity instanceof IronGolem) {
            entity.remove(RemovalReason.DISCARDED);
            return true;
        }
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (!player.getAbilities().invulnerable && player.position().y() > this.position().y() + CreatureManager.getInstance().config.bossAntiFlight) {
                return true;
            }
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean canBurn() {
        return false;
    }

    @Override
    public boolean isBlocking() {
        return super.isBlocking();
    }

    public boolean canAttackWhileBlocking() {
        return true;
    }


    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
    }

    public float getBrightness() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }
}