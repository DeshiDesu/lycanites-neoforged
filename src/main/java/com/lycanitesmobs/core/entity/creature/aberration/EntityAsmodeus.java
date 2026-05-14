package com.lycanitesmobs.core.entity.creature.aberration;

import com.lycanitesmobs.core.entity.IGroupBoss;
import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.goals.util.GoalConditions;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.FindNearbyPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FaceTargetGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.HealWhenNoPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.SummonMinionsGoal;
import com.lycanitesmobs.core.entity.navigation.ArenaNode;
import com.lycanitesmobs.core.entity.navigation.ArenaNodeNetwork;
import com.lycanitesmobs.core.entity.navigation.ArenaNodeNetworkGrid;
import com.lycanitesmobs.core.entity.projectile.misc.EntityDevilGatling;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class EntityAsmodeus extends BaseCreatureEntity implements Enemy, IGroupHeavy, IGroupBoss {

    // Data Manager:
    protected static final EntityDataAccessor<Byte> ANIMATION_STATES = SynchedEntityData.defineId(EntityAsmodeus.class, EntityDataSerializers.BYTE);

    public enum ANIMATION_STATES_ID {
        SNAP_TO_ARENA((byte) 1), COOLDOWN((byte) 2);
        public final byte id;

        ANIMATION_STATES_ID(byte value) {
            this.id = value;
        }

        public byte getValue() {
            return id;
        }
    }

    // AI:
    public AttackRangedGoal aiRangedAttack;

    public boolean firstPlayerTargetCheck = false;
    public List<EntityAstaroth> astarothMinions = new ArrayList<>();
    public List<EntityGrell> grellMinions = new ArrayList<>();

    // First Phase:
    public int devilstarStreamTime = 0;
    public int devilstarStreamTimeMax = 5 * 20;
    public int devilstarStreamCharge = 20 * 20;
    public int devilstarStreamChargeMax = 20 * 20;

    // Second Phase:
    public int hellshieldAstarothRespawnTime = 0;
    public int hellshieldAstarothRespawnTimeMax = 30;

    // Third Phase:
    public int rebuildAstarothRespawnTime = 0;
    public int rebuildAstarothRespawnTimeMax = 40;

    // Arena Movement:
    public ArenaNodeNetwork arenaNodeNetwork;
    public ArenaNode currentArenaNode;
    public int arenaNodeChangeCooldown = 0;
    public int arenaNodeChangeCooldownMax = 200;
    public int arenaJumpingTime = 0;
    public int arenaJumpingTimeMax = 60;
    protected double jumpHeight = 6D;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityAsmodeus(EntityType<? extends EntityAsmodeus> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;
        this.setAttackCooldownMax(30);
        this.hasJumpSound = true;
        this.solidCollision = true;
        //this.pushthrough = 1.0F;
        this.setupMob();
        this.hitAreaWidthScale = 2F;

        // Boss:
        this.damageMax = BaseCreatureEntity.BOSS_DAMAGE_LIMIT;
        this.damageLimit = BaseCreatureEntity.BOSS_DAMAGE_LIMIT;
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(this.nextFindTargetIndex, new FindNearbyPlayersGoal(this));

        // All Phases:
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new FaceTargetGoal(this));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new HealWhenNoPlayersGoal(this));
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new SummonMinionsGoal(this).setMinionInfo("grigori").setAntiFlight(true));

        this.aiRangedAttack = new AttackRangedGoal(this).setSpeed(1.0D).setStaminaTime(200).setStaminaDrainRate(3).setRange(90.0F).setChaseTime(0).setCheckSight(false);
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, this.aiRangedAttack);

        // Phase 1:
        this.goalSelector.addGoal(this.nextIdleGoalIndex, new SummonMinionsGoal(this).setMinionInfo("trite").setSummonRate(20 * 3).setSummonCap(3).setPerPlayer(true)
                .setConditions(new GoalConditions().setBattlePhase(0)));

        super.registerGoals();
    }

    // ========== Init ==========

    /**
     * Initiates the entity setting all the values to be watched by the data manager.
     **/
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION_STATES, (byte) 0);
    }

    // ========== Rendering Distance ==========

    /**
     * Returns a larger bounding box for rendering this large entity.
     **/
    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(200, 50, 200).move(0, -25, 0);
    }

    // ========== Persistence ==========
    @Override
    public boolean isPersistant() {
        if (this.getMasterTarget() != null && this.getMasterTarget() instanceof EntityAsmodeus)
            return true;
        return super.isPersistant();
    }

    // ========== First Spawn ==========
    @Override
    public void onFirstSpawn() {
        super.onFirstSpawn();
        if (this.getArenaCenter() == null) {
            this.setArenaCenter(this.blockPosition());
        }
    }


    // ==================================================
    //                      Positions
    // ==================================================
    // ========== Arena Center ==========

    /**
     * Sets the central arena point for this mob to use.
     **/
    public void setArenaCenter(BlockPos pos) {
        super.setArenaCenter(pos);
        this.arenaNodeNetwork = new ArenaNodeNetworkGrid(this.getCommandSenderWorld(), pos, 3, 1, 3, 60);
        this.currentArenaNode = this.arenaNodeNetwork.getClosestNode(this.blockPosition());
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Update Phases:
        if (!this.getCommandSenderWorld().isClientSide) {
            this.updatePhases();
            this.updateCurrentArenaNode();
            this.updateArenaMovement();
        }

        // Clean Minion Lists:
        if (this.getCommandSenderWorld().isClientSide && this.updateTick % 200 == 0) {
            if (!this.astarothMinions.isEmpty()) {
                for (EntityAstaroth minion : this.astarothMinions.toArray(new EntityAstaroth[this.astarothMinions.size()])) {
                    if (minion == null || !minion.isAlive() || minion.getMasterTarget() != this)
                        this.astarothMinions.remove(minion);
                }
            }
            if (!this.grellMinions.isEmpty()) {
                for (EntityGrell minion : this.grellMinions.toArray(new EntityGrell[this.grellMinions.size()])) {
                    if (minion == null || !minion.isAlive() || minion.getMasterTarget() != this)
                        this.grellMinions.remove(minion);
                }
            }
        }

        // Update Animation States:
        if (!this.getCommandSenderWorld().isClientSide) {
            byte animationState = 0;
            if (this.aiRangedAttack != null && this.aiRangedAttack.attackOnCooldown)
                animationState += ANIMATION_STATES_ID.COOLDOWN.id;
            this.entityData.set(ANIMATION_STATES, animationState);
        }

        // Client Attack and Cooldown Particles:
        if (this.getCommandSenderWorld().isClientSide) {
            if ((this.entityData.get(ANIMATION_STATES) & ANIMATION_STATES_ID.COOLDOWN.id) > 0) {
                BlockPos particlePos = this.getFacingPosition(this, 13, this.getYHeadRot() - this.yRotO);
                for (int i = 0; i < 4; ++i) {
                    this.getCommandSenderWorld().addParticle(ParticleTypes.LARGE_SMOKE,
                            particlePos.getX() + (this.random.nextDouble() - 0.5D) * 2,
                            particlePos.getY() + (this.getDimensions(Pose.STANDING).height * 0.2D) + this.random.nextDouble() * 2,
                            particlePos.getZ() + (this.random.nextDouble() - 0.5D) * 2,
                            0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Override
    public boolean rollWanderChance() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    // ========== Current Arena Node Update ==========
    public void updateCurrentArenaNode() {
        if (!this.hasArenaCenter())
            return;
        if (this.arenaNodeChangeCooldown > 0) {
            this.arenaNodeChangeCooldown--;
            return;
        }

        // Return to center with no target.
        if (this.getTarget() == null || !this.getTarget().isAlive()) {
            this.setCurrentArenaNode(this.arenaNodeNetwork.centralNode);
            return;
        }

        if (this.currentArenaNode != null)
            this.setCurrentArenaNode(this.currentArenaNode.getClosestAdjacentNode(this.getTarget().blockPosition()));
        else
            this.setCurrentArenaNode(this.arenaNodeNetwork.getClosestNode(this.getTarget().blockPosition()));
    }

    // ========== Set Current Arena Node ==========
    public void setCurrentArenaNode(ArenaNode arenaNode) {
        this.arenaNodeChangeCooldown = this.arenaNodeChangeCooldownMax;
        if (this.currentArenaNode == arenaNode)
            return;
        this.currentArenaNode = arenaNode;

        // Update home position jumping time on node change to new node.
        if (this.currentArenaNode != null && this.currentArenaNode.pos != null) {
            this.arenaJumpingTime = this.arenaJumpingTimeMax;
            this.leap(200, this.jumpHeight, this.currentArenaNode.pos); // First leap for jump height.
            if (this.hasJumpSound)
                this.playJumpSound();
        }
    }

    // ========== Arena Movement Update ==========
    public void updateArenaMovement() {
        this.noPhysics = false;
        if (this.currentArenaNode == null || this.currentArenaNode.pos == null) {
            return;
        }

        // Jumping:
        if (this.arenaJumpingTime > 0) {
            this.arenaJumpingTime--;
            if (this.updateTick % 4 == 0) {
                double dropForce = -0.5D;
                this.noPhysics = this.position().y() > this.currentArenaNode.pos.getY() + 8;
                if (this.position().y() < this.currentArenaNode.pos.getY()) {
                    this.setPos(this.position().x(), this.currentArenaNode.pos.getY(), this.position().z());
                    dropForce = 0;
                }
                this.leap(200, dropForce, this.currentArenaNode.pos); // Leap for XZ movement and negative height for increased weight on update.
            }
            if (this.arenaJumpingTime == 0)
                this.playStepSound(this.currentArenaNode.pos.below(), this.getCommandSenderWorld().getBlockState(this.currentArenaNode.pos.below()));
            return;
        }

        // Snap To Node:
        BlockPos arenaPos = this.currentArenaNode.pos;
        double arenaY = this.position().y();
        if (this.getCommandSenderWorld().isEmptyBlock(arenaPos))
            arenaY = arenaPos.getY();
        else if (this.getCommandSenderWorld().isEmptyBlock(arenaPos.offset(0, 1, 0)))
            arenaY = arenaPos.offset(0, 1, 0).getY();

        if (this.position().x() != arenaPos.getX() || this.position().y() != arenaY || this.position().z() != arenaPos.getZ())
            this.setPos(arenaPos.getX(), arenaY, arenaPos.getZ());
    }


    // ========== Phases Update ==========
    public void updatePhases() {
        int playerCount = Math.max(this.playerTargets.size(), 1);

        // ===== First Phase - Devilstar Stream =====
        if (this.getBattlePhase() == 0) {
            // Devilstar Stream - Fire:
            if (this.devilstarStreamTime > 0) {
                this.devilstarStreamTime--;
                if (this.updateTick % 10 == 0) {
                    for (float angle = 0; angle < 360F; angle += 10F) {
                        this.attackDevilstar(angle);
                    }
                }
            }
            // Devilstar Stream - Recharge:
            else if (this.devilstarStreamCharge > 0) {
                this.devilstarStreamCharge--;
            }
            // Devilstar Stream - Charged
            else {
                this.devilstarStreamCharge = this.devilstarStreamChargeMax;
                this.devilstarStreamTime = this.devilstarStreamTimeMax;
            }
        }


        // ===== Second Phase - Hellshield =====
        else if (this.getBattlePhase() == 1 && this.updateTick % 20 == 0) {
            // Summon Astaroth:
            if (this.astarothMinions.isEmpty() && this.hellshieldAstarothRespawnTime-- <= 0) {
                for (int i = 0; i < 2 * playerCount; i++) {
                    EntityAstaroth minion = (EntityAstaroth) CreatureManager.getInstance().getCreature("astaroth").createEntity(this.getCommandSenderWorld());
                    this.summonMinion(minion, this.getRandom().nextDouble() * 360, 0);
                    minion.setSizeScale(2.5D);
                    this.astarothMinions.add(minion);
                }
                this.hellshieldAstarothRespawnTime = this.hellshieldAstarothRespawnTimeMax;
            }
        }


        // ===== Third Phase - Rebuild =====
        else if (this.updateTick % 20 == 0) {
            if (this.astarothMinions.size() < playerCount * 4) {
                // Summon Astaroth:
                if (this.rebuildAstarothRespawnTime-- <= 0) {
                    for (int i = 0; i < playerCount; i++) {
                        EntityAstaroth minion = (EntityAstaroth) CreatureManager.getInstance().getCreature("astaroth").createEntity(this.getCommandSenderWorld());
                        this.summonMinion(minion, this.getRandom().nextDouble() * 360, 0);
                        minion.setSizeScale(2.5D);
                        this.astarothMinions.add(minion);
                    }
                    this.rebuildAstarothRespawnTime = this.rebuildAstarothRespawnTimeMax;
                }
            }

            // Summon Grell:
            if (this.grellMinions.size() < playerCount * 6 && this.updateTick % 10 * 20 == 0) {
                for (int i = 0; i < 5 * playerCount; i++) {
                    EntityGrell minion = (EntityGrell) CreatureManager.getInstance().getCreature("grell").createEntity(this.getCommandSenderWorld());
                    this.summonMinion(minion, this.getRandom().nextDouble() * 360, 10);
                    minion.setPos(
                            minion.position().x(),
                            minion.position().y() + 10 + this.getRandom().nextInt(20),
                            minion.position().z()
                    );
                    this.grellMinions.add(minion);
                }
            }

            // Heal:
            if (!this.astarothMinions.isEmpty()) {
                float healAmount = this.astarothMinions.size();
                if (((this.getHealth() + healAmount) / this.getMaxHealth()) <= 0.2D)
                    this.heal(healAmount * 2);
            }
        }
    }


    // ==================================================
    //                  Battle Phases
    // ==================================================
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


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Set Attack Target ==========
    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof EntityTrite || target instanceof EntityGrell || target instanceof EntityAstaroth)
            return false;
        return super.canAttack(target);
    }

    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        for (int i = 0; i < 5; i++) {
            this.fireProjectile(EntityDevilGatling.class, target, range, 0, new Vector3d(0, -24, 0), 4f, 2f, 8F);
        }
        this.attackHitscan(target, target instanceof Player ? 1 : 10);
    }

    // ========== Devilstars ==========
    public void attackDevilstar(float angle) {
        // Type:
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("devilstar");
        if (projectileInfo == null) {
            return;
        }
        BaseProjectileEntity projectile = projectileInfo.createProjectile(this.getCommandSenderWorld(), this);

        // Y Offset:
        BlockPos offset = this.getFacingPosition(this, 8, angle);
        projectile.setPos(
                offset.getX(),
                offset.getY() + (this.getDimensions(Pose.STANDING).height * 0.5D),
                offset.getZ()
        );

        // Set Velocities:
        float range = 20 + (20 * this.getRandom().nextFloat());
        BlockPos target = this.getFacingPosition(this, range, angle);
        double d0 = target.getX() - projectile.position().x();
        double d1 = target.getY() - projectile.position().y();
        double d2 = target.getZ() - projectile.position().z();
        float f1 = Mth.sqrt(LMHelperClass.convertToFloat(d0 * d0 + d2 * d2)) * 0.1F;
        float velocity = 1.2F;
        projectile.shoot(d0, d1 + (double) f1, d2, velocity, 0.0F);
        projectile.setProjectileScale(3f);

        // Launch:
        this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.getCommandSenderWorld().addFreshEntity(projectile);
    }


    // ==================================================
    //                      Death
    // ==================================================
    @Override
    public void die(DamageSource damageSource) {
        if (!this.getCommandSenderWorld().isClientSide && CreatureManager.getInstance().getCreature("trite").enabled) {
            int j = 6 + this.random.nextInt(20) + (getCommandSenderWorld().getDifficulty().getId() * 4);
            for (int k = 0; k < j; ++k) {
                float f = ((float) (k % 2) - 0.5F) * this.getDimensions(Pose.STANDING).width / 4.0F;
                float f1 = ((float) (k / 2) - 0.5F) * this.getDimensions(Pose.STANDING).width / 4.0F;
                EntityTrite trite = (EntityTrite) CreatureManager.getInstance().getCreature("trite").createEntity(this.getCommandSenderWorld());
                trite.moveTo(this.position().x() + (double) f, this.position().y() + 0.5D, this.position().z() + (double) f1, this.random.nextFloat() * 360.0F, 0.0F);
                trite.setMinion(true);
                trite.applyVariant(this.getVariantIndex());
                this.getCommandSenderWorld().addFreshEntity(trite);
                if (this.getTarget() != null)
                    trite.setLastHurtByMob(this.getTarget());
            }
        }
        super.die(damageSource);
    }

    // ========== Minion Death ==========
    @Override
    public void onMinionDeath(LivingEntity minion, DamageSource damageSource) {
        if (minion instanceof EntityAstaroth && this.astarothMinions.contains(minion)) {
            this.astarothMinions.remove(minion);
        }
        if (minion instanceof EntityGrell && this.grellMinions.contains(minion)) {
            this.grellMinions.remove(minion);
        }
        super.onMinionDeath(minion, damageSource);
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    // ========== Damage ==========
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

    // ========== Blocking ==========
    @Override
    public boolean isBlocking() {
        if (this.getCommandSenderWorld().isClientSide)
            return super.isBlocking();
        return this.getBattlePhase() == 1 && !this.astarothMinions.isEmpty();
    }

    public boolean canAttackWhileBlocking() {
        return true;
    }


    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("DevilstarStreamCharge")) {
            this.devilstarStreamCharge = nbt.getInt("DevilstarStreamCharge");
        }
        if (nbt.contains("DevilstarStreamTime")) {
            this.devilstarStreamTime = nbt.getInt("DevilstarStreamTime");
        }
        if (nbt.contains("AstarothIDs")) {
            ListTag astarothIDs = nbt.getList("AstarothIDs", 10);
            for (int i = 0; i < astarothIDs.size(); i++) {
                CompoundTag astarothID = astarothIDs.getCompound(i);
                if (astarothID.contains("ID")) {
                    Entity entity = this.getCommandSenderWorld().getEntity(astarothID.getInt("ID"));
                    if (entity != null && entity instanceof EntityAstaroth)
                        this.astarothMinions.add((EntityAstaroth) entity);
                }
            }
        }
    }

    // ========== Write ==========

    /**
     * Used when saving this mob to a chunk.
     **/
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("DevilstarStreamCharge", this.devilstarStreamCharge);
        nbt.putInt("DevilstarStreamTime", this.devilstarStreamTime);
        if (this.getBattlePhase() > 0) {
            ListTag astarothIDs = new ListTag();
            for (EntityAstaroth entityAstaroth : this.astarothMinions) {
                CompoundTag astarothID = new CompoundTag();
                astarothID.putInt("ID", entityAstaroth.getId());
                astarothIDs.add(astarothID);
            }
            nbt.put("AstarothIDs", astarothIDs);
        }
    }


    // ==================================================
    //                   Brightness
    // ==================================================
    public float getBrightness() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }
}
