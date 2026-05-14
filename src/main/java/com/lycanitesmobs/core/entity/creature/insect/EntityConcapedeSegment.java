package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.FollowParentGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

import java.util.UUID;

public class EntityConcapedeSegment extends AgeableCreatureEntity {

    // Parent UUID:
    /**
     * Used to identify the parent segment when loading this saved entity, set to null when found or lost for good.
     **/
    UUID parentUUID = null;

    public BaseCreatureEntity backSegment;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityConcapedeSegment(EntityType<? extends EntityConcapedeSegment> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.ARTHROPOD;
        this.hasAttackSound = true;
        this.hasStepSound = false;

        this.canGrow = true;
        this.babySpawnChance = 0D;
        this.isAggressiveByDefault = false;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(this.nextTravelGoalIndex++, new FollowParentGoal(this).setSpeed(1.0D).setStrayDistance(0));
        super.registerGoals();
    }

    // ==================================================
    //                      Spawning
    // ==================================================
    // ========== Natural Spawn Check ==========

    /**
     * Second stage checks for spawning, this check is ignored if there is a valid monster spawner nearby.
     **/
    @Override
    public boolean environmentSpawnCheck(Level world, BlockPos pos) {
        if (this.getNearbyEntities(EntityConcapedeHead.class, null, CreatureManager.getInstance().spawnConfig.spawnLimitRange).size() <= 0)
            return false;
        return super.environmentSpawnCheck(world, pos);
    }

    // ========== Get Random Subspecies ==========
    @Override
    public void getRandomVariant() {
        if (this.subspecies == null && !this.hasParent()) {
            this.subspecies = this.creatureInfo.getRandomSubspecies(this);
        }

        if (this.hasParent() && this.getParentTarget() instanceof BaseCreatureEntity) {
            this.applyVariant(((BaseCreatureEntity) this.getParentTarget()).getSubspeciesIndex());
        }
    }

    // ========== Despawning ==========

    /**
     * Returns whether this mob should despawn overtime or not. Config defined forced despawns override everything except tamed creatures and tagged creatures.
     **/
    @Override
    protected boolean canDespawnNaturally() {
        if (!super.canDespawnNaturally())
            return false;
        return !this.hasParent();
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        Level world = this.getCommandSenderWorld();
        boolean isClient = world.isClientSide;

        // Try to Load Parent from UUID:
        if (!isClient && !this.hasParent() && this.parentUUID != null && this.updateTick > 0 && this.updateTick % 40 == 0) {
            if (world instanceof ServerLevel serverLevel) {
                Entity foundEntity = serverLevel.getEntity(this.parentUUID);
                if (foundEntity instanceof AgeableCreatureEntity parent && parent != this) {
                    this.setParentTarget(parent);
                }
            }
            this.parentUUID = null;
        }

        super.aiStep();

        // Concapede Connections:
        if (!isClient) {
            // Check if back segment is alive:
            if (this.backSegment != null) {
                if (!this.backSegment.isAlive())
                    this.backSegment = null;
            }

            // Check if front segment is alive:
            if (this.hasParent()) {
                if (!this.getParentTarget().isAlive())
                    this.setParentTarget(null);
            }

            // Force position to front with offset:
            if (this.hasParent()) {
                this.getLookControl().setLookAt(this.getParentTarget(), 360.0F, 360.0F);
                this.lookAt(this.getParentTarget(), 360, 360);

                Vector3d parentPos = this.getFacingPositionDouble(this.getParentTarget().getX(), this.getParentTarget().getY(), this.getParentTarget().getZ(), -0.65D, this.getParentTarget().yRot);
                double segmentPullThreshold = 0.15D;
                double segmentDistanceSq = this.distanceToSqr(LMHelperClass.convertToVec3(parentPos));
                if (segmentDistanceSq > segmentPullThreshold * segmentPullThreshold) {
                    double dragAmount = segmentPullThreshold / 2;
                    Vector3d posVector = new Vector3d(this.getX(), this.getY(), this.getZ());
                    Vector3d dragPos = this.getFacingPositionDouble(parentPos.x, parentPos.y, parentPos.z, dragAmount, posVector.dot(parentPos));
                    double distY = (parentPos.y - this.getY());
                    double dragY = this.getY() + (distY / 2);
                    this.setPos(dragPos.x, dragY, dragPos.z);
                }
            }

            // Growth Into Head:
            if (this.getGrowingAge() <= 0)
                this.setGrowingAge(-this.growthTime);
        }
    }

    @Override
    public boolean rollLookChance() {
        if (this.hasParent())
            return false;
        return super.rollLookChance();
    }

    @Override
    public boolean rollWanderChance() {
        if (this.hasParent())
            return false;
        return super.rollWanderChance();
    }


    // ==================================================
    //                        Age
    // ==================================================
    @Override
    public void setGrowingAge(int age) {
        if (this.hasParent())
            age = -this.growthTime;
        super.setGrowingAge(age);
        if (age == 0 && !this.getCommandSenderWorld().isClientSide) {
            EntityConcapedeHead concapedeHead = (EntityConcapedeHead) CreatureManager.getInstance().getCreature("concapede").createEntity(this.getCommandSenderWorld());
            concapedeHead.copyPosition(this);
            concapedeHead.firstSpawn = false;
            concapedeHead.setGrowingAge(-this.growthTime / 4);
            concapedeHead.setSizeScale(this.sizeScale);
            concapedeHead.applyVariant(this.getVariantIndex());
            this.getCommandSenderWorld().addFreshEntity(concapedeHead);
            if (this.backSegment != null)
                this.backSegment.setParentTarget(concapedeHead);
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean shouldFollowParent() {
        return true; // Follow as an adult.
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Pathing Weight ==========
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if (block != Blocks.AIR) {
            if (Material.GRASS.contains(blockState.getBlock()))
                return 10F;
            if (Material.DIRT.contains(blockState.getBlock()))
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }

    // ========== Can leash ==========
    @Override
    public boolean canBeLeashed(Player player) {
        return !this.hasParent();
    }

    // ========== Falling Speed Modifier ==========
    @Override
    public double getFallingMod() {
        if (this.getCommandSenderWorld().isClientSide)
            return 0.0D;
        if (this.hasParent() && this.getParentTarget().position().y() > this.position().y())
            return 0.0D;
        return super.getFallingMod();
    }

    @Override
    public boolean useDirectNavigator() {
        return this.hasParent();
    }

    @Override
    public boolean isPushable() {
        return false;
    }


    // ==================================================
    //                     Targets
    // ==================================================
    @Override
    public void setParentTarget(LivingEntity setTarget) {
        if (setTarget != this) {
            if (setTarget instanceof EntityConcapedeSegment)
                ((EntityConcapedeSegment) setTarget).backSegment = this;
            if (setTarget instanceof EntityConcapedeHead)
                ((EntityConcapedeHead) setTarget).backSegment = this;
        }
        super.setParentTarget(setTarget);
    }


    // ==================================================
    //                     Interact
    // ==================================================
    // ========== Render Subspecies Name Tag ==========

    /**
     * Gets whether this mob should always display its nametag if it's a subspecies.
     **/
    @Override
    public boolean renderVariantNameTag() {
        return !this.hasParent();
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean canClimb() {
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

    @Override
    public float getFallResistance() {
        return 100;
    }


    // ==================================================
    //                      Breeding
    // ==================================================
    // ========== Create Child ==========
    @Override
    public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
        return null;
    }

    // ========== Breed ==========
    public boolean breed() {
        if (!this.canBreed())
            return false;
        this.setGrowingAge(0);
        return true;
    }

    @Override
    public boolean canBreed() {
        return !this.hasParent();
    }

    @Override
    public boolean shouldFindParent() {
        return false;
    }


    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========

    /**
     * Used when loading this mob from a saved chunk.
     **/
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.hasUUID("ParentUUID")) {
            this.parentUUID = nbt.getUUID("ParentUUID");
        }
        super.readAdditionalSaveData(nbt);
    }

    // ========== Write ==========

    /**
     * Used when saving this mob to a chunk.
     **/
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.hasParent()) {
            nbt.putUUID("ParentUUID", this.getParentTarget().getUUID());
        }
    }
}
