package com.lycanitesmobs.core.entity.base;

import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.goals.actions.FollowParentGoal;
import com.lycanitesmobs.core.entity.goals.actions.MateGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindParentGoal;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.item.ItemDrop;
import com.lycanitesmobs.core.data.info.Variant;
import com.lycanitesmobs.core.item.consumable.entity.ItemCustomSpawnEgg;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.HashMap;

public abstract class AgeableCreatureEntity extends BaseCreatureEntity {

    // Size:
    private float scaledWidth = -1.0F;
    private float scaledHeight;

    // Targets:
    private AgeableCreatureEntity breedingTarget;

    // Growth:
    public int growthTime = -24000;
    public boolean canGrow = true;
    public double babySpawnChance = 0D;

    // Breeding:
    public int loveTime;
    private int loveTimeMax = 600;
    public int breedingCooldown = 6000;

    public boolean hasBeenFarmed = false;

    // Datawatcher:
    protected static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(AgeableCreatureEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> LOVE = SynchedEntityData.defineId(AgeableCreatureEntity.class, EntityDataSerializers.INT);

    // ==================================================
    //                    Constructor
    // ==================================================
    protected AgeableCreatureEntity(EntityType<? extends AgeableCreatureEntity> entityType, Level world) {
        super(entityType, world);
    }

    // ========== Init ==========
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AGE, 0);
        this.entityData.define(LOVE, 0);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        // Greater Actions:
        this.goalSelector.addGoal(this.nextTravelGoalIndex++, new MateGoal(this).setMateDistance(5.0D));

        super.registerGoals();

        // Lesser Targeting:
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindParentGoal(this).setSightCheck(false).setDistance(32.0D));

        // Lesser Actions:
        this.goalSelector.addGoal(this.nextTravelGoalIndex++, new FollowParentGoal(this).setSpeed(1.0D).setStrayDistance(3.0D));
    }

    // ========== Setup ==========
    @Override
    public void setupMob() {
        if (this.babySpawnChance > 0D && this.random.nextDouble() < this.babySpawnChance)
            this.setGrowingAge(growthTime);
        super.setupMob();
    }

    // ========== Name ==========
    @Override
    public Component getAgeName() {
        if (this.isBaby())
            return Component.translatable("entity.baby");
        else
            return super.getAgeName();
    }


    // ==================================================
    //                       Spawning
    // ==================================================
    @Override
    public boolean isPersistant() {
        if (this.hasBeenFarmed)
            return true;
        return super.isPersistant();
    }

    public void setFarmed() {
        this.hasBeenFarmed = true;
        if (this.portalTime > this.getDimensionChangingDelay())
            this.portalTime = this.getDimensionChangingDelay();
    }

    // ========== Get Random Subspecies ==========
    @Override
    public void getRandomSubspecies() {
        if (this.isBaby())
            return;
        super.getRandomSubspecies();
    }

    @Override
    public void getRandomVariant() {
        if (this.isBaby())
            return;
        super.getRandomVariant();
    }


    // ==================================================
    //                       Update
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Growing:
        if (this.getCommandSenderWorld().isClientSide)
            this.setScaleForAge(this.isBaby());
        else if (this.canGrow) {
            int age = this.getGrowingAge();
            if (age < 0) {
                ++age;
                this.setGrowingAge(age);
            } else if (age > 0) {
                --age;
                this.setGrowingAge(age);
            }
        }

        // Breeding:
        if (!this.canBreed())
            this.loveTime = 0;

        if (!this.getCommandSenderWorld().isClientSide)
            this.entityData.set(LOVE, this.loveTime);
        if (this.getCommandSenderWorld().isClientSide)
            this.loveTime = this.getIntFromDataManager(LOVE);

        if (this.isInLove()) {
            this.setFarmed();
            --this.loveTime;
            if (this.getCommandSenderWorld().isClientSide) {
                ParticleOptions particle = ParticleTypes.HEART;
                if (this.loveTime % 10 == 0) {
                    double d0 = this.random.nextGaussian() * 0.02D;
                    double d1 = this.random.nextGaussian() * 0.02D;
                    double d2 = this.random.nextGaussian() * 0.02D;
                    this.getCommandSenderWorld().addParticle(particle, this.position().x() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, this.position().y() + 0.5D + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).height), this.position().z() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, d0, d1, d2);
                }
            }
        }
    }

    // ========== AI Update ==========
    @Override
    protected void customServerAiStep() {
        if (!this.canBreed())
            this.loveTime = 0;
        super.customServerAiStep();
    }

    @Override
    public boolean canDropItem(ItemDrop itemDrop) {
        if (itemDrop.adultOnly && this.isBaby()) {
            return false;
        }
        return super.canDropItem(itemDrop);
    }


    // ==================================================
    //                      Interact
    // ==================================================
    // ========== Get Interact Commands ==========
    @Override
    public HashMap<Integer, String> getInteractCommands(Player player, @Nonnull ItemStack itemStack) {
        HashMap<Integer, String> commands = new HashMap<>();
        commands.putAll(super.getInteractCommands(player, itemStack));

        // Item Commands:
        if (!itemStack.isEmpty()) {

            // Spawn Egg:
            if (itemStack.getItem() instanceof ItemCustomSpawnEgg) {
                CreatureInfo creatureInfo = ((ItemCustomSpawnEgg) itemStack.getItem()).getCreatureInfo(itemStack);
                if (creatureInfo == this.creatureInfo)
                    commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Spawn Baby");
            }

            // Breeding Item:
            if (this.isBreedingItem(itemStack) && this.canBreed() && !this.isInLove())
                commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Breed");
        }

        return commands;
    }

    // ========== Perform Command ==========
    @Override
    public boolean performCommand(String command, Player player, ItemStack itemStack) {

        // Spawn Baby:
        if (command.equals("Spawn Baby") && !this.getCommandSenderWorld().isClientSide && itemStack.getItem() instanceof ItemCustomSpawnEgg) {
            ItemCustomSpawnEgg itemCustomSpawnEgg = (ItemCustomSpawnEgg) itemStack.getItem();
            CreatureInfo spawnEggCreatureInfo = itemCustomSpawnEgg.getCreatureInfo(itemStack);
            if (spawnEggCreatureInfo != null) {
                if (spawnEggCreatureInfo.entityClass != null && spawnEggCreatureInfo.entityClass.isAssignableFrom(this.getClass())) {
                    AgeableCreatureEntity baby = this.createChild(this);
                    if (baby != null) {
                        baby.setGrowingAge(baby.growthTime);
                        baby.moveTo(this.position().x(), this.position().y(), this.position().z(), 0.0F, 0.0F);
                        baby.setFarmed();
                        this.getCommandSenderWorld().addFreshEntity(baby);
                        if (itemStack.hasCustomHoverName()) {
                            baby.setCustomName(itemStack.getHoverName());
                        }
                        this.consumePlayersItem(player, itemStack);
                    }
                }
            }
            return true;
        }

        // Breed:
        if (command.equals("Breed")) {
            if (this.breed()) {
                this.consumePlayersItem(player, itemStack);
                return true;
            }
        }

        return super.performCommand(command, player, itemStack);
    }


    // ==================================================
    //                        Age
    // ==================================================
    public int getGrowingAge() {
        return this.getIntFromDataManager(AGE);
    }

    public void setGrowingAge(int age) {
        this.entityData.set(AGE, age);
        this.setScaleForAge(this.isBaby());
    }

    public void addGrowth(int growth) {
        int age = this.getGrowingAge();
        age += growth * 20;
        if (age > 0)
            age = 0;
        this.setGrowingAge(age);
    }

    @Override
    public boolean isBaby() {
        return this.getGrowingAge() < 0;
    }

    /**
     * Returns if this creature should follow parents. By default only returns true if this creature is a baby.
     *
     * @return Returns true if parents should be searched for and followed.
     */
    public boolean shouldFollowParent() {
        return this.isBaby();
    }

    /**
     * Returns if this creature should look for a parent to follow if it has none already.
     *
     * @return True if this creature should actively seek parents.
     */
    public boolean shouldFindParent() {
        return this.isBaby();
    }


    // ==================================================
    //                        Size
    // ==================================================
    public double setScaleForAge(boolean adult) {
        return adult ? 0.5F : 1.0F;
    }


    // ==================================================
    //                      Breeding
    // ==================================================
    @Override
    public boolean canBeTempted() {
        return !this.isInLove() && super.canBeTempted();
    }

    // ========== Targets ==========
    public AgeableCreatureEntity getBreedingTarget() {
        return this.breedingTarget;
    }

    public void setBreedingTarget(AgeableCreatureEntity target) {
        this.breedingTarget = target;
    }

    // ========== Create Child ==========
    public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
        return (AgeableCreatureEntity) this.creatureInfo.createEntity(this.getCommandSenderWorld());
    }

    // ========== Breeding Item ==========
    public boolean isBreedingItem(ItemStack itemStack) {
        if (!this.creatureInfo.isFarmable() || this.getAirSupply() <= -100) {
            return false;
        }
        return this.creatureInfo.canEat(itemStack);
    }

    // ========== Valid Partner ==========
    public boolean canBreedWith(AgeableCreatureEntity partner) {
        if (partner == this)
            return false;
        if (partner.getClass() != this.getClass())
            return false;
        if (this.getSubspecies() != partner.getSubspecies()) {
            return false;
        }
        return this.isInLove() && partner.isInLove();
    }

    // ========== Love Check ==========
    public boolean isInLove() {
        return this.loveTime > 0;
    }

    // ========== Mate Check ==========
    public boolean canMate() {
        return this.isInLove();
    }

    // ========== Breed ==========
    public boolean breed() {
        if (!this.canBreed())
            return false;
        this.loveTime = this.loveTimeMax;
        return true;
    }

    public boolean canBreed() {
        return this.getGrowingAge() == 0;
    }

    // ========== Procreate ==========
    public void procreate(AgeableCreatureEntity partner) {
        AgeableCreatureEntity baby = this.createChild(partner);

        if (baby != null) {
            this.finishBreeding();
            partner.finishBreeding();
            baby.setGrowingAge(baby.growthTime);
            baby.setSubspecies(this.getSubspeciesIndex());
            Variant babyVariant = this.getSubspecies().getChildVariant(this, this.getVariant(), partner.getVariant());
            baby.applyVariant(babyVariant != null ? babyVariant.index : 0);
            baby.moveTo(this.position().x(), this.position().y(), this.position().z(), this.yRotO, this.xRotO);

            for (int i = 0; i < 7; ++i) {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;
                this.getCommandSenderWorld().addParticle(ParticleTypes.HEART, this.position().x() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, this.position().y() + 0.5D + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).height), this.position().z() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, d0, d1, d2);
            }

            this.onCreateBaby(partner, baby);

            this.getCommandSenderWorld().addFreshEntity(baby);

            for (Player player : this.getCommandSenderWorld().players()) {
                if (this.distanceTo(player) <= 10) {
                    ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
                    if (extendedPlayer != null) {
                        extendedPlayer.studyCreature(this, CreatureManager.getInstance().config.creatureBreedKnowledge, false, true);
                    }
                }
            }
        }
    }

    public void onCreateBaby(AgeableCreatureEntity partner, AgeableCreatureEntity baby) {

    }

    public void finishBreeding() {
        this.setGrowingAge(this.breedingCooldown);
        this.setBreedingTarget(null);
        this.loveTime = 0;
    }


    // ==================================================
    //                       NBT
    // ==================================================
    // ========== Read ==========
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("Age")) {
            this.setGrowingAge(nbt.getInt("Age"));
        } else {
            this.setGrowingAge(0);
        }

        if (nbt.contains("InLove")) {
            this.loveTime = nbt.getInt("InLove");
        } else {
            this.loveTime = 0;
        }

        if (nbt.contains("HasBeenFarmed")) {
            if (nbt.getBoolean("HasBeenFarmed")) {
                this.setFarmed();
            }
        }
    }

    // ========== Write ==========
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Age", this.getGrowingAge());
        nbt.putInt("InLove", this.loveTime);
        nbt.putBoolean("HasBeenFarmed", this.hasBeenFarmed);
    }


}
