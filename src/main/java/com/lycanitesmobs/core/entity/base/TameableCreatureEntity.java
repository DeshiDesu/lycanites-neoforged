package com.lycanitesmobs.core.entity.base;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.damagesources.MinionEntityDamageSource;
import com.lycanitesmobs.core.entity.goals.actions.BegGoal;
import com.lycanitesmobs.core.entity.goals.actions.FollowOwnerGoal;
import com.lycanitesmobs.core.entity.goals.actions.StayGoal;
import com.lycanitesmobs.core.entity.goals.targeting.CopyOwnerAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendOwnerGoal;
import com.lycanitesmobs.core.entity.goals.targeting.RevengeOwnerGoal;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.item.consumable.entity.ChargeItem;
import com.lycanitesmobs.core.item.consumable.entity.CreatureTreatItem;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulstone;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public abstract class TameableCreatureEntity extends AgeableCreatureEntity {
    // Stats:
    public float hunger = this.getCreatureHungerMax();
    public float stamina = this.getStaminaMax();
    public float staminaRecovery = 0.5F;
    public float sittingGuardRange = 16F;

    // Owner:
    public UUID ownerUUID;

    // Datawatcher:
    protected static final EntityDataAccessor<Byte> TAMED = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.BYTE); // TODO Tame Type IDs.
    protected static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> STAMINA = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> LOYALTY = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.INT);

    protected TameableCreatureEntity(EntityType<? extends AgeableCreatureEntity> entityType, Level world) {
        super(entityType, world);
    }

    /**
     * Used for the TAMED WATCHER_ID, this holds a series of booleans that describe the tamed status as well as instructed behaviour.
     **/
    public static enum TAMED_ID {
        IS_TAMED((byte) 1), MOVE_SIT((byte) 2), MOVE_FOLLOW((byte) 4),
        STANCE_PASSIVE((byte) 8), STANCE_AGGRESSIVE((byte) 16), STANCE_ASSIST((byte) 32), PVP((byte) 64);
        public final byte id;

        TAMED_ID(byte value) {
            this.id = value;
        }

        public byte getValue() {
            return id;
        }
    }

    // ==================================================
    //                    Constructor
    // ==================================================
	/*protected TameableCreatureEntity(EntityType<? extends TameableCreatureEntity> entityType, Level world) {
		super(entityType, world);
	}
	*/
    // ========== Init ==========
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TAMED, (byte) 0);
        this.entityData.define(OWNER_ID, Optional.empty());
        this.entityData.define(HUNGER, this.getCreatureHungerMax());
        this.entityData.define(STAMINA, this.getStaminaMax());
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        // Greater Targeting:
        this.targetSelector.addGoal(this.nextReactTargetIndex++, new RevengeOwnerGoal(this));
        this.targetSelector.addGoal(this.nextReactTargetIndex++, new CopyOwnerAttackTargetGoal(this));

        // Greater Actions:
        this.goalSelector.addGoal(this.nextTravelGoalIndex++, new StayGoal(this));

        super.registerGoals();

        // Lesster Targeting:
        this.targetSelector.addGoal(this.nextSpecialTargetIndex++, new DefendOwnerGoal(this));

        // Lesser Actions:
        this.goalSelector.addGoal(this.nextTravelGoalIndex++, new FollowOwnerGoal(this).setStrayDistance(CreatureManager.getInstance().config.petFollowDistance).setLostDistance(32).setSpeed(1D));
        this.goalSelector.addGoal(this.nextIdleGoalIndex++, new BegGoal(this));
    }

    // ========== Name ==========
    @Override
    public MutableComponent getName() {
        if (!this.isTamed() || !CreatureManager.getInstance().config.ownerTags) {
            return super.getName().copy();
        }

        MutableComponent ownedName = Component.literal("");
        boolean customName = this.hasCustomName();
        if (customName) {
            ownedName.append(super.getName()).append(" (");
        }

        MutableComponent ownerName = this.getOwnerName().copy();
        String ownerSuffix = "'s ";
        String ownerFormatted = ownerName.getString();
        if (ownerFormatted.length() > 0) {
            if ("s".equalsIgnoreCase(ownerFormatted.substring(ownerFormatted.length() - 1)))
                ownerSuffix = "' ";
        }
        ownedName.append(ownerName).append(ownerSuffix).append(this.getFullName());
        if (customName) {
            ownedName.append(")");
        }

        return ownedName;
    }


    // ==================================================
    //                     Spawning
    // ==================================================
    // ========== Despawning ==========
    @Override
    protected boolean canDespawnNaturally() {
        if (this.isTamed()) {
            return false;
        }
        return super.canDespawnNaturally();
    }

    @Override
    public boolean despawnCheck() {
        if (this.getCommandSenderWorld().isClientSide)
            return false;

        // Bound Pet:
        if (this.getPetEntry() != null) {
            if (this.getPetEntry().entity != this && this.getPetEntry().entity != null)
                return true;
            if (this.getPetEntry().host == null || !this.getPetEntry().host.isAlive()) {
                this.getPetEntry().saveEntityNBT();
                return true;
            }
        }

        if (this.isTamed() && !this.isTemporary)
            return false;
        return super.despawnCheck();
    }

    @Override
    public boolean isPersistant() {
        return this.isTamed() || super.isPersistant();
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Can leash ==========
    @Override
    public boolean canBeLeashed(Player player) {
        if (this.isTamed() && player == this.getPlayerOwner())
            return true;
        return super.canBeLeashed(player);
    }

    // ========== Test Leash ==========
    @Override
    public void testLeash(float distance) {
        if (this.isSitting() && distance > 10.0F)
            this.dropLeash(true, true);
        else
            super.testLeash(distance);
    }


    // ==================================================
    //                       Update
    // ==================================================
    @Override
    public void aiStep() {
        super.aiStep();
        this.staminaUpdate();

        // Owner Buffs:
        if (!this.getCommandSenderWorld().isClientSide() && this.isPet() && this.distanceToSqr(this.getPlayerOwner()) <= 64) {
            this.ownerEffects();
        }
    }

    public void staminaUpdate() {
        if (this.getCommandSenderWorld().isClientSide)
            return;
        if (this.stamina < this.getStaminaMax() && this.staminaRecovery >= this.getStaminaRecoveryMax() / 2)
            this.setStamina(Math.min(this.stamina + this.staminaRecovery, this.getStaminaMax()));
        if (this.staminaRecovery < this.getStaminaRecoveryMax())
            this.staminaRecovery = Math.min(this.staminaRecovery + (this.getStaminaRecoveryMax() / this.getStaminaRecoveryWarmup()), this.getStaminaRecoveryMax());
    }

    /**
     * Grants constant effects to the owner if the owner is nearby.
     */
    public void ownerEffects() {
        // Protect Owner from Effects:
        if (!this.canBurn()) {
            this.getPlayerOwner().addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (5 * 20) + 5, 1));
        }
        for (Object possibleEffect : this.getPlayerOwner().getActiveEffects().toArray(new Object[0])) {
            if (possibleEffect instanceof MobEffectInstance) {
                MobEffectInstance effectInstance = (MobEffectInstance) possibleEffect;
                if (!this.canBeAffected(effectInstance))
                    this.getPlayerOwner().removeEffect(effectInstance.getEffect());
            }
        }
    }

    /**
     * Returns true if the is a standard pet and is tamed with a player owner, not a minion, mount, familiar, etc. Does not check if soulbound.
     *
     * @return True if a standard pet.
     */
    public boolean isPet() {
        if (!this.isTamed() || this.getPlayerOwner() == null) {
            return false;
        }
        if (this.creatureInfo.isMountable()) {
            return false;
        }
        if (this.isTemporary) {
            return false;
        }
        return this.getPetEntry() == null || this.isPetType("pet");
    }

    /**
     * Copies this creature's behaviour to the provided tamed creature.
     *
     * @return The creature to copy pet behaviour to.
     */
    public void copyPetBehaviourTo(TameableCreatureEntity target) {
        target.setPVP(this.isPVP());
        target.setPassive(this.isPassive());
        target.setAssist(this.isAssisting());
        target.setAggressive(this.isAggressive());

        target.setSitting(this.isSitting());
        target.setFollowing(this.isFollowing());
    }


    // ==================================================
    //                       Perching
    // ==================================================
    public boolean canPerch(LivingEntity target) {
        if (!this.creatureInfo.isPerchable()) {
            return false;
        }
        return this.getPlayerOwner() == target;
    }


    // ==================================================
    //                       Interact
    // ==================================================
    // ========== Get Interact Commands ==========
    @Override
    public HashMap<Integer, String> getInteractCommands(Player player, @Nonnull ItemStack itemStack) {
        HashMap<Integer, String> commands = new HashMap<>();
        commands.putAll(super.getInteractCommands(player, itemStack));

        // Perch:
        if (this.canPerch(player) && !player.isShiftKeyDown() && !this.getCommandSenderWorld().isClientSide)
            commands.put(COMMAND_PIORITIES.MAIN.id, "Perch");

            // Open GUI:
        else if (!this.getCommandSenderWorld().isClientSide && player.isShiftKeyDown() && this.isTamed() && (itemStack.isEmpty() || player.isShiftKeyDown()) && player == this.getPlayerOwner()) {
            commands.put(COMMAND_PIORITIES.MAIN.id, "GUI");
        }

        // Server Item Commands:
        if (!this.getCommandSenderWorld().isClientSide && !itemStack.isEmpty() && !player.isShiftKeyDown()) {

            // Taming:
            if (!this.isTamed() && isTamingItem(itemStack) && CreatureManager.getInstance().config.tamingEnabled) {
                commands.put(COMMAND_PIORITIES.IMPORTANT.id, "Tame");
            }

            // Feeding:
            if (this.isTamed() && this.isHealingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Feed");
            }

            // Charges:
            if (this.isTamed() && !this.isTemporary && !this.isPetType("minion") && itemStack.getItem() instanceof ChargeItem) {
                if (this.isLevelingChargeItem(itemStack)) {
                    commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Charge");
                } else {
                    player.displayClientMessage(Component.translatable("item.lycanitesmobs.charge.creature.invalid"), false);
                }
            }

            // Equipment:
            if (this.isTamed() && !this.isBaby() && this.canEquip() && player == this.getPlayerOwner()) {
                String equipSlot = this.inventory.getSlotForEquipment(itemStack);
                if (equipSlot != null && (this.inventory.getEquipmentStack(equipSlot) == null || this.inventory.getEquipmentStack(equipSlot).getItem() != itemStack.getItem()))
                    commands.put(COMMAND_PIORITIES.EQUIPPING.id, "Equip Item");
            }

            // Soulstone:
            if (itemStack.getItem() instanceof ItemSoulstone && this.isTamed()) {
                commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Soulstone");
            }
        }

        // Sit:
        //if(this.isTamed() && this.canSit() && player == this.getPlayerOwner() && !this.getEntityWorld().isRemote)
        //commands.put(CMD_PRIOR.MAIN.id, "Sit");

        return commands;
    }

    // ========== Perform Command ==========
    @Override
    public boolean performCommand(String command, Player player, ItemStack itemStack) {
        // Open GUI:
        if (command.equals("GUI")) {
            this.playTameSound();
            this.openGUI(player);
            return true;
        }

        // Tame:
        if (command.equals("Tame")) {
            this.tame(player);
            this.consumePlayersItem(player, itemStack);
            return true;
        }

        // Feed:
        if (command.equals("Feed")) {
            int healAmount = 4;
            if (itemStack.getItem().isEdible()) {
                healAmount = itemStack.getItem().getFoodProperties().getNutrition();
            }
            this.heal((float) healAmount);
            this.playEatSound();
            if (this.getCommandSenderWorld().isClientSide) {
                ParticleOptions particle = ParticleTypes.HEART;
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;
                for (int i = 0; i < 25; i++)
                    this.getCommandSenderWorld().addParticle(particle, this.position().x() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, this.position().y() + 0.5D + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).height), this.position().z() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, d0, d1, d2);
            }
            this.consumePlayersItem(player, itemStack);
            return true;
        }

        // Charge Experience:
        if (command.equals("Charge")) {
            this.addExperience(this.getExperienceFromChargeItem(itemStack));
            this.consumePlayersItem(player, itemStack);
            return true;
        }

        // Equip Armor:
        if (command.equals("Equip Item")) {
            ItemStack equippedItem = this.inventory.getEquipmentStack(this.inventory.getSlotForEquipment(itemStack));
            if (equippedItem != null)
                this.dropItem(equippedItem);
            ItemStack equipStack = itemStack.copy();
            equipStack.setCount(1);
            this.inventory.setEquipmentStack(equipStack.copy());
            this.consumePlayersItem(player, itemStack);
            return true;
        }

        // Soulstone:
        if (command.equals("Soulstone")) {
            return false; // Allow the soulstone item to activate.
        }

        // Sit:
        if (command.equals("Sit")) {
            this.playTameSound();
            this.setTarget(null);
            this.clearMovement();
            this.setSitting(!this.isSitting());
            this.jumping = false;
            return true;
        }

        // Perch:
        if (command.equals("Perch")) {
            this.playTameSound();
            this.perchOnEntity(player);
            return true;
        }

        return super.performCommand(command, player, itemStack);
    }

    // ========== Can Name Tag ==========
    @Override
    public boolean canNameTag(Player player) {
        if (!this.isTamed())
            return super.canNameTag(player);
        else if (player == this.getPlayerOwner())
            return super.canNameTag(player);
        return false;
    }

    // ========== Perform GUI Command ==========
    @Override
    public void performGUICommand(Player player, int guiCommandID) {
        if (!this.petControlsEnabled())
            return;
        if (player != this.getOwner())
            return;

        // Pet Commands:
        if (guiCommandID == PET_COMMAND_ID.PVP.id) {
            this.setPVP(!this.isPVP());
        } else if (guiCommandID == PET_COMMAND_ID.PASSIVE.id) {
            this.setPassive(true);
        } else if (guiCommandID == PET_COMMAND_ID.DEFENSIVE.id) {
            this.setPassive(false);
            this.setAssist(false);
            this.setAggressive(false);
        } else if (guiCommandID == PET_COMMAND_ID.ASSIST.id) {
            this.setPassive(false);
            this.setAssist(true);
            this.setAggressive(false);
        } else if (guiCommandID == PET_COMMAND_ID.AGGRESSIVE.id) {
            this.setPassive(false);
            this.setAssist(true);
            this.setAggressive(true);
        } else if (guiCommandID == PET_COMMAND_ID.FOLLOW.id) {
            this.setSitting(false);
            this.setFollowing(true);
        } else if (guiCommandID == PET_COMMAND_ID.WANDER.id) {
            this.setSitting(false);
            this.setFollowing(false);
        } else if (guiCommandID == PET_COMMAND_ID.SIT.id) {
            this.setSitting(true);
            this.setFollowing(false);
        }

        // this.playTameSound();

        // Update Pet Entry Summon Set:
        if (this.petEntry != null && this.petEntry.summonSet != null) {
            this.petEntry.summonSet.updateBehaviour(this);
        }

        super.performGUICommand(player, guiCommandID);
    }


    // ==================================================
    //                       Targets
    // ==================================================
    // ========== Teams ==========
    @Override
    public Team getTeam() {
        if (this.isTamed()) {
            Entity owner = this.getOwner();
            if (owner != null)
                return owner.getTeam();
        }
        return super.getTeam();
    }

    /**
     * Returns if this creature is on the same team as the target entity. If PvP is disabled and this creature is tamed then it is considered on the same team as all players and their tames.
     *
     * @param target The entity to check teams with.
     * @return True when on the same team.
     */
    @Override
    public boolean isAlliedTo(Entity target) {
        if (this.getCommandSenderWorld().isClientSide) {
            return super.isAlliedTo(target);
        }

        if (this.isTamed()) {
            // Check If Owner:
            if (target == this.getPlayerOwner() || target == this.getOwner())
                return true;

            // Check Player PvP:
            if (target instanceof Player && (!this.getCommandSenderWorld().getServer().isPvpAllowed() || !this.isPVP())) {
                return true;
            }

            // Check If Tameable:
            if (target instanceof TameableCreatureEntity) {
                TameableCreatureEntity tamedTarget = (TameableCreatureEntity) target;
                if (tamedTarget.isTamed()) {
                    if (!this.getCommandSenderWorld().getServer().isPvpAllowed() || !this.isPVP() || tamedTarget.getPlayerOwner() == this.getPlayerOwner()) {
                        return true;
                    }
                }
            } else if (target instanceof OwnableEntity) {
                OwnableEntity tamedTarget = (OwnableEntity) target;
                if (tamedTarget.getOwner() != null) {
                    if (!this.getCommandSenderWorld().getServer().isPvpAllowed() || !this.isPVP() || tamedTarget.getOwner() == this.getOwner()) {
                        return true;
                    }
                }
            }

            // Check Owner Teams:
            if (this.getPlayerOwner() != null) {
                if (this.getPlayerOwner().getVehicle() == target) {
                    return true;
                }
                return this.getPlayerOwner().isAlliedTo(target);
            } else if (this.getOwner() != null) {
                if (this.getOwner().getVehicle() == target) {
                    return true;
                }
                return this.getOwner().isAlliedTo(target);
            }

            return false;
        }
        return super.isAlliedTo(target);
    }


    // ==================================================
    //                       Attacks
    // ==================================================
    @Override
    public boolean doRangedDamage(Entity target, ThrowableProjectile projectile, float damage, boolean noPierce) {
        float totalDamage = damage * ((float) this.creatureStats.getDamage() / 2);

        // Owner Credit:
        if (target instanceof Mob) {
            Mob mobTarget = (Mob) target;
            if (this.getOwner() instanceof Player && mobTarget.getHealth() > 0 && mobTarget.getHealth() - totalDamage <= 0) {
                DamageSource creditSource = new MinionEntityDamageSource(this.getDamageSource(null).typeHolder(), this.getOwner());
                return target.hurt(creditSource, totalDamage);
            }
        }

        return super.doRangedDamage(target, projectile, damage, noPierce);
    }

    @Override
    public boolean attackEntityAsMob(Entity target, double damageScale) {
        if (!this.isAlive())
            return false;
        if (target == null)
            return false;
        if (!this.hasLineOfSight(target))
            return false;

        float totalDamage = this.getAttackDamage(damageScale);

        // Owner Credit:
        if (target instanceof Mob) {
            Mob mobTarget = (Mob) target;
            if (this.getOwner() instanceof Player && mobTarget.getHealth() > 0 && mobTarget.getHealth() - totalDamage <= 0) {
                DamageSource creditSource = new MinionEntityDamageSource(this.getDamageSource(null).typeHolder(), this.getOwner());
                creditSource.is(DamageTypes.MAGIC);
                return target.hurt(creditSource, totalDamage);
            }
        }

        return super.attackEntityAsMob(target, damageScale);
    }

    // ========== Can Attack ==========
    @Override
    public boolean canAttackType(EntityType targetType) {
        if (this.isPassive())
            return false;
        if (this.isTamed())
            return true;
        return super.canAttackType(targetType);
    }

    @Override
    public boolean canAttack(LivingEntity targetEntity) {
        if (this.isPassive())
            return false;
        if (this.isTamed()) {
            if (this.getOwner() == targetEntity || this.getPlayerOwner() == targetEntity)
                return false;
            if (!this.getCommandSenderWorld().isClientSide) {
                boolean canPVP = this.getCommandSenderWorld().getServer().isPvpAllowed() && this.isPVP();
                if (targetEntity instanceof Player && !canPVP)
                    return false;
                if (targetEntity instanceof TameableCreatureEntity) {
                    TameableCreatureEntity targetTameable = (TameableCreatureEntity) targetEntity;
                    if (targetTameable.isTamed()) {
                        if (!canPVP)
                            return false;
                        if (targetTameable.getPlayerOwner() == this.getPlayerOwner())
                            return false;
                    }
                }
            }
            return true;
        }
        return super.canAttack(targetEntity);
    }

    // ========= Get Damage Source ==========

    /**
     * Returns the damage source to be used by this mob when dealing damage.
     *
     * @param nestedDamageSource This can be null or can be a passed entity damage source for all kinds of use, mainly for minion damage sources.
     * @return
     */
    @Override
    public DamageSource getDamageSource(DamageSource nestedDamageSource) {
//        if(this.isTamed() && this.getOwner() != null) {
//            if(nestedDamageSource == null)
//                nestedDamageSource = (EntityDamageSource)DamageSource.mobAttack(this);
//            return new MinionEntityDamageSource(nestedDamageSource, this.getOwner());
//        }
        return super.getDamageSource(nestedDamageSource);
    }

    // ========== Attacked From ==========
    @Override
    public boolean hurt(DamageSource damageSrc, float damageAmount) {
        if (this.isInvulnerableTo(damageSrc))
            return false;
        else {
            if (!this.isPassive())
                this.setSitting(false);

            Entity entity = damageSrc.getDirectEntity();
            if (entity instanceof ThrowableProjectile)
                entity = ((ThrowableProjectile) entity).getOwner();

            if (this.isTamed() && this.getOwner() == entity)
                return false;

            return super.hurt(damageSrc, damageAmount);
        }
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    // ========== Damage ==========

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        var entity = source.getEntity();

        if (this.isTamed()) {
            if (entity instanceof Player && this.getCommandSenderWorld().getServer() != null && !this.getCommandSenderWorld().getServer().isPvpAllowed()) {
                return true;
            }
            if (entity == this.getPlayerOwner()) {
                return true;
            }
        }

        if (this.isTamed()) {
            if (entity instanceof Player && this.getCommandSenderWorld().getServer() != null && !this.getCommandSenderWorld().getServer().isPvpAllowed()) {
                return true;
            }
            if (entity == this.getPlayerOwner()) {
                return true;
            }
        }

        if (source.is(DamageTypes.IN_WALL) && this.isTamed())
            return true;

        return super.isInvulnerableTo(source);
    }


    // ==================================================
    //                       Owner
    // ==================================================

    /**
     * Sets the owner of this entity via the unique id of the owner entity. Also updates the tamed status of this entity.
     *
     * @param ownerUUID The owner entity UUID.
     */
    public void setOwnerId(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.entityData.set(OWNER_ID, Optional.ofNullable(ownerUUID));
        this.setTamed(ownerUUID != null);
    }

    @Override
    public Entity getOwner() {
        UUID uuid = this.getOwnerId();
        if (uuid == null) {
            return super.getOwner();
        }
        return this.getCommandSenderWorld().getPlayerByUUID(uuid);
    }

    /**
     * Sets the owner of this entity to the provided player entity. Also updates the tamed status of this entity.
     *
     * @param player The player to become the owner.
     */
    public void setPlayerOwner(Player player) {
        this.setOwnerId(player.getUUID());
    }

    @Override
    public UUID getOwnerId() {
        if (this.getCommandSenderWorld().isClientSide) {
            try {
                return this.getUUIDFromDataManager(OWNER_ID).orElse(null);
            } catch (Exception ignored) {
            }
        }
        return this.ownerUUID;
    }

    /**
     * Returns the owner of this entity as a player or null if there is no player owner. This is separated from getOwner and getOwnerId as they behave inconsistently when built.
     *
     * @return The player owner.
     */
    public Player getPlayerOwner() {
        if (this.getCommandSenderWorld().isClientSide) {
            Entity owner = this.getOwner();
            if (owner instanceof Player) {
                return (Player) owner;
            }
            return null;
        }
        if (this.ownerUUID == null) {
            return null;
        }
        return this.getCommandSenderWorld().getPlayerByUUID(this.ownerUUID);
    }

    /**
     * Gets the display name of the entity that owns this entity or an empty string if none. TODO Maybe this hack is no longer needed now.
     *
     * @return The owner display name.
     */
    public Component getOwnerName() {
        Entity owner = this.getOwner();
        if (owner != null) {
            return owner.getDisplayName();
        }
        return Component.literal("");
    }


    // ==================================================
    //                       Taming
    // ==================================================
    @Override
    public boolean isTamed() {
        try {
            return (this.getByteFromDataManager(TAMED) & TAMED_ID.IS_TAMED.id) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void setTamed(boolean isTamed) {
        byte tamed = this.getByteFromDataManager(TAMED);
        if (isTamed) {
            this.entityData.set(TAMED, (byte) (tamed | TAMED_ID.IS_TAMED.id));
            this.spawnEventType = "";
        } else {
            this.entityData.set(TAMED, (byte) (tamed - (tamed & TAMED_ID.IS_TAMED.id)));
        }
        this.setCustomNameVisible(isTamed);
    }

    public boolean isTamingItem(ItemStack itemstack) {
        if (itemstack.isEmpty() || this.creatureInfo.creatureType == null || this.isBoss()) {
            return false;
        }

        if (itemstack.getItem() instanceof CreatureTreatItem) {
            CreatureTreatItem itemTreat = (CreatureTreatItem) itemstack.getItem();
            if (itemTreat.getCreatureType() == this.creatureInfo.creatureType) {
                return this.creatureInfo.isTameable();
            }
        }

        return false;
    }

    // ========== Tame Entity ==========

    /**
     * Attempts to tame this entity to the provided player.
     *
     * @param player The player taming this entity.
     * @return True if the entity is tamed, false on failure.
     */
    public boolean tame(Player player) {
        if (this.getCommandSenderWorld().isClientSide || this.isRareVariant() || this.isBoss()) {
            return super.isTamed();
        }

        ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
        if (extendedPlayer == null) {
            return this.isTamed();
        }

        extendedPlayer.studyCreature(this, CreatureManager.getInstance().config.creatureTreatKnowledge, true, true);

        // Already Tamed:
        if (this.isTamed()) {
            return true;
        }

        // Require Knowledge Rank 2:
        CreatureKnowledge creatureKnowledge = extendedPlayer.getBeastiary().getCreatureKnowledge(this.creatureInfo.getName());
        if (creatureKnowledge == null || creatureKnowledge.rank < 2) {
//			ITextComponent tameMessage = Component.translatable("message.pet.tamefail.knowledge.prefix")
//					.append(" ")
//					.append(this.getSpeciesName())
//					.append(" ")
//					.append(Component.translatable("message.pet.tamefail.knowledge.suffix"));
//			player.displayClientMessage(tameMessage, false);
            return this.isTamed();
        }

        // Increase Reputation:
        CreatureRelationshipEntry relationshipEntry = this.relationships.getOrCreateEntry(player);
        int reputationAmount = 50 + this.getRandom().nextInt(50);
        relationshipEntry.increaseReputation(reputationAmount);

        // Apply Tame:
        if (this.creatureInfo.isTameable() && relationshipEntry.getReputation() >= this.creatureInfo.getTamingReputation()) {
            this.setPlayerOwner(player);
            this.onTamedByPlayer();
            this.unsetTemporary();
            MutableComponent tameMessage = Component.translatable("message.pet.tamed.prefix")
                    .append(" ")
                    .append(this.getSpeciesName())
                    .append(" ")
                    .append(Component.translatable("message.pet.tamed.suffix"));
            player.displayClientMessage(tameMessage, false);
            this.playTameEffect(this.isTamed());
            //player.addStat(ObjectManager.getStat(this.creatureInfo.getName() + ".tame"), 1); TODO Player Stats
            if (this.portalTime > this.getDimensionChangingDelay()) {
                this.portalTime = this.getDimensionChangingDelay();
            }
        }

        this.playTameEffect(this.isTamed());
        return this.isTamed();
    }

    /**
     * Called when this creature is first tamed by a player, this clears movement, targets, etc and sets default the pet behaviour.
     */
    public void onTamedByPlayer() {
        this.refreshAttributes();
        this.clearMovement();
        this.setTarget(null);
        this.setSitting(false);
        this.setFollowing(true);
        this.setPassive(false);
        this.setAggressive(false);
        this.setPVP(true);
        this.playTameSound();
        this.spawnEventType = "";
    }

    @Override
    public void onCreateBaby(AgeableCreatureEntity partner, AgeableCreatureEntity baby) {
        if (this.isTamed() && this.getOwner() instanceof Player && partner instanceof TameableCreatureEntity && baby instanceof TameableCreatureEntity) {
            TameableCreatureEntity partnerTameable = (TameableCreatureEntity) partner;
            TameableCreatureEntity babyTameable = (TameableCreatureEntity) baby;
            if (partnerTameable.getPlayerOwner() == this.getPlayerOwner()) {
                babyTameable.setPlayerOwner((Player) this.getOwner());
            }
        }
        super.onCreateBaby(partner, baby);
    }


    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte status) {
        if (status == 7)
            this.playTameEffect(true);
        else if (status == 6)
            this.playTameEffect(false);
        else
            super.handleEntityEvent(status);
    }

    /**
     * Determines if the provided itemstack can be consumed to heal this entity.
     *
     * @param itemStack The possible healing itemstack.
     * @return True if this entity should eat the itemstack and heal.
     */
    public boolean isHealingItem(ItemStack itemStack) {
        return this.creatureInfo.canEat(itemStack);
    }

    /**
     * Determines if the provided itemstack can be consumed to add experience this entity.
     *
     * @param itemStack The possible leveling itemstack.
     * @return True if this entity should eat the itemstack and gain experience.
     */
    public boolean isLevelingChargeItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ChargeItem) {
            ChargeItem chargeItem = (ChargeItem) itemStack.getItem();
            for (ElementInfo elementInfo : this.getElements()) {
                if (chargeItem.getElements().contains(elementInfo)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines how much experience the provided charge itemstack can grant this creature.
     *
     * @param itemStack The possible leveling itemstack.
     * @return The amount of experience to gain.
     */
    public int getExperienceFromChargeItem(ItemStack itemStack) {
        int experience = 0;
        if (itemStack.getItem() instanceof ChargeItem) {
            ChargeItem chargeItem = (ChargeItem) itemStack.getItem();
            for (ElementInfo elementInfo : this.getElements()) {
                if (chargeItem.getElements().contains(elementInfo)) {
                    experience += ChargeItem.CHARGE_EXPERIENCE;
                }
            }
        }
        return experience;
    }


    // ==================================================
    //                      Breeding
    // ==================================================
    @Override
    public boolean isBreedingItem(ItemStack itemStack) {
        if (!this.creatureInfo.isFarmable()) {
            if (!this.isTamed() || this.isPetType("minion") || this.isPetType("familiar") || this.getHealth() < this.getMaxHealth()) {
                return false;
            }
        }
        return super.isBreedingItem(itemStack);
    }


    // ============================================
    //                   Minions
    // ============================================
    @Override
    public void summonMinion(LivingEntity minion, double angle, double distance) {
        if (this.getPlayerOwner() != null && minion instanceof TameableCreatureEntity) {
            ((TameableCreatureEntity) minion).setPlayerOwner(this.getPlayerOwner());
            this.copyPetBehaviourTo((TameableCreatureEntity) minion);
        }
        super.summonMinion(minion, angle, distance);
    }


    // ==================================================
    //                    Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
    }

    public byte behaviourBitMask() {
        return this.getByteFromDataManager(TAMED);
    }

    // ========== Sitting ==========
    public boolean isSitting() {
        if (!this.isTamed())
            return false;
        return (this.getByteFromDataManager(TAMED) & TAMED_ID.MOVE_SIT.id) != 0;
    }

    public void setSitting(boolean set) {
        if (!this.petControlsEnabled())
            set = false;
        byte tamedStatus = this.getByteFromDataManager(TAMED);
        if (set) {
            this.entityData.set(TAMED, (byte) (tamedStatus | TAMED_ID.MOVE_SIT.id));
            this.setHome((int) this.position().x(), (int) this.position().y(), (int) this.position().z(), this.sittingGuardRange);
        } else {
            this.entityData.set(TAMED, (byte) (tamedStatus - (tamedStatus & TAMED_ID.MOVE_SIT.id)));
            this.hasRestriction();
        }
    }

    // ========== Following ==========
    public boolean isFollowing() {
        if (!this.isTamed())
            return false;
        if (this.getLeashHolder() instanceof LeashFenceKnotEntity)
            return false;
        return (this.getByteFromDataManager(TAMED) & TAMED_ID.MOVE_FOLLOW.id) != 0;
    }

    public void setFollowing(boolean set) {
        if (!this.petControlsEnabled())
            set = false;
        byte tamedStatus = this.getByteFromDataManager(TAMED);
        if (set)
            this.entityData.set(TAMED, (byte) (tamedStatus | TAMED_ID.MOVE_FOLLOW.id));
        else
            this.entityData.set(TAMED, (byte) (tamedStatus - (tamedStatus & TAMED_ID.MOVE_FOLLOW.id)));
    }

    // ========== Passiveness ==========
    public boolean isPassive() {
        if (!this.isTamed())
            return false;
        return (this.getByteFromDataManager(TAMED) & TAMED_ID.STANCE_PASSIVE.id) != 0;
    }

    public void setPassive(boolean set) {
        if (!this.petControlsEnabled())
            set = false;
        byte tamedStatus = this.getByteFromDataManager(TAMED);
        if (set) {
            this.entityData.set(TAMED, (byte) (tamedStatus | TAMED_ID.STANCE_PASSIVE.id));
            this.setTarget(null);
            this.setStealth(0);
        } else
            this.entityData.set(TAMED, (byte) (tamedStatus - (tamedStatus & TAMED_ID.STANCE_PASSIVE.id)));
    }

    // ========== Agressiveness ==========
    @Override
    public boolean isAggressive() {
        if (!this.isTamed())
            return super.isAggressive();
        return (this.getByteFromDataManager(TAMED) & TAMED_ID.STANCE_AGGRESSIVE.id) != 0;
    }

    public void setAggressive(boolean set) {
        if (!this.petControlsEnabled()) {
            set = false;
        }
        byte tamedStatus = this.getByteFromDataManager(TAMED);
        if (set)
            this.entityData.set(TAMED, (byte) (tamedStatus | TAMED_ID.STANCE_AGGRESSIVE.id));
        else
            this.entityData.set(TAMED, (byte) (tamedStatus - (tamedStatus & TAMED_ID.STANCE_AGGRESSIVE.id)));
    }

    // ========== Assist ==========
    public boolean isAssisting() {
        if (!this.isTamed()) {
            return false;
        }
        return (this.getByteFromDataManager(TAMED) & TAMED_ID.STANCE_ASSIST.id) != 0;
    }

    public void setAssist(boolean set) {
        if (!this.petControlsEnabled()) {
            set = true;
        }
        byte tamedStatus = this.getByteFromDataManager(TAMED);
        if (set)
            this.entityData.set(TAMED, (byte) (tamedStatus | TAMED_ID.STANCE_ASSIST.id));
        else
            this.entityData.set(TAMED, (byte) (tamedStatus - (tamedStatus & TAMED_ID.STANCE_ASSIST.id)));
    }

    // ========== PvP ==========
    public boolean isPVP() {
        return (this.getByteFromDataManager(TAMED) & TAMED_ID.PVP.id) != 0;
    }

    public void setPVP(boolean set) {
        if (!this.petControlsEnabled())
            set = false;
        byte tamedStatus = this.getByteFromDataManager(TAMED);
        if (set) {
            this.entityData.set(TAMED, (byte) (tamedStatus | TAMED_ID.PVP.id));
        } else {
            this.setTarget(null);
            this.entityData.set(TAMED, (byte) (tamedStatus - (tamedStatus & TAMED_ID.PVP.id)));
        }
    }


    // ==================================================
    //                       Hunger
    // ==================================================
    public float getCreatureHunger() {
        if (this.getCommandSenderWorld() == null)
            return this.getCreatureHungerMax();
        if (!this.getCommandSenderWorld().isClientSide)
            return this.hunger;
        else {
            try {
                return this.getFloatFromDataManager(HUNGER);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    public void setCreatureHunger(float setHunger) {
        this.hunger = setHunger;
    }

    public float getCreatureHungerMax() {
        return 20;
    }


    // ==================================================
    //                       Stamina
    // ==================================================
    public float getStamina() {
        if (this.getCommandSenderWorld() != null && this.getCommandSenderWorld().isClientSide) {
            try {
                this.stamina = this.getFloatFromDataManager(STAMINA);
            } catch (Exception e) {
            }
        }
        return this.stamina;
    }

    public void setStamina(float setStamina) {
        this.stamina = setStamina;
        if (this.getCommandSenderWorld() != null && !this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(STAMINA, setStamina);
        }
    }

    public float getStaminaMax() {
        return 100;
    }

    public float getStaminaRecoveryMax() {
        return 1F;
    }

    public int getStaminaRecoveryWarmup() {
        return 10 * 20;
    }

    public float getStaminaCost() {
        return 1;
    }

    public void applyStaminaCost() {
        float newStamina = this.getStamina() - this.getStaminaCost();
        if (newStamina < 0)
            newStamina = 0;
        this.setStamina(newStamina);
        this.staminaRecovery = 0;
    }

    // ========== GUI Feedback ==========
    public float getStaminaPercent() {
        return this.getStamina() / this.getStaminaMax();
    }

    // "energy" = Usual blue-orange bar. "toggle" = Solid purple bar for on and off.
    public String getStaminaType() {
        return "energy";
    }


    // ==================================================
    //                      Breeding
    // ==================================================
    // ========== Create Child ==========
    @Override
    public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
        AgeableCreatureEntity spawnedBaby = super.createChild(partner);
        UUID ownerId = this.getOwnerId();
        if (ownerId != null && spawnedBaby != null && spawnedBaby instanceof TameableCreatureEntity) {
            TameableCreatureEntity tamedBaby = (TameableCreatureEntity) spawnedBaby;
            tamedBaby.setOwnerId(ownerId);
        }
        return spawnedBaby;
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    /*@Override
    public boolean shouldDismountInWater(Entity rider) { TODO Possibly in EntityType as some obfuscated method?
        return false;
    }*/

    @Override
    public boolean canBeControlledByRider() {
        return this.isTamed();
    }


    // ==================================================
    //                       Client
    // ==================================================
    protected void playTameEffect(boolean success) {
        ParticleOptions particle = ParticleTypes.HEART;
        if (!success)
            particle = ParticleTypes.SMOKE;

        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.getCommandSenderWorld().addParticle(particle, this.position().x() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, this.position().y() + 0.5D + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).height), this.position().z() + (double) (this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double) this.getDimensions(Pose.STANDING).width, d0, d1, d2);
        }
    }


    // ==================================================
    //                       Visuals
    // ==================================================
    // ========== Coloring ==========

    /**
     * Returns true if this mob can be dyed different colors. Usually for wool and collars.
     *
     * @param player The player to check for when coloring, this is to stop players from dying other players pets. If provided with null it should return if this creature can be dyed in general.
     */
    @Override
    public boolean canBeColored(Player player) {
        if (player == null) return true;
        return this.isTamed() && player == this.getPlayerOwner();
    }


    // ========== Boss Health Bar ==========
    public boolean showBossInfo() {
        if (this.isTamed())
            return false;
        return super.showBossInfo();
    }


    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        // UUID NBT:
        if (nbt.hasUUID("OwnerId")) {
            this.setOwnerId(nbt.getUUID("OwnerId"));
        } else {
            this.setOwnerId(null);
        }

        if (nbt.contains("Sitting")) {
            this.setSitting(nbt.getBoolean("Sitting"));
        } else {
            this.setSitting(false);
        }

        if (nbt.contains("Following")) {
            this.setFollowing(nbt.getBoolean("Following"));
        } else {
            this.setFollowing(true);
        }

        if (nbt.contains("Passive")) {
            this.setPassive(nbt.getBoolean("Passive"));
        } else {
            this.setPassive(false);
        }

        if (nbt.contains("Aggressive")) {
            this.setAggressive(nbt.getBoolean("Aggressive"));
        } else {
            this.setAggressive(false);
        }

        if (nbt.contains("PVP")) {
            this.setPVP(nbt.getBoolean("PVP"));
        } else {
            this.setPVP(true);
        }

        if (nbt.contains("Hunger")) {
            this.setCreatureHunger(nbt.getFloat("Hunger"));
        } else {
            this.setCreatureHunger(this.getCreatureHungerMax());
        }

        if (nbt.contains("Stamina")) {
            this.setStamina(nbt.getFloat("Stamina"));
        }
    }

    // ========== Write ==========
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.getOwnerId() != null) {
            nbt.putUUID("OwnerId", this.getOwnerId());
        }
        nbt.putBoolean("Sitting", this.isSitting());
        nbt.putBoolean("Following", this.isFollowing());
        nbt.putBoolean("Passive", this.isPassive());
        nbt.putBoolean("Aggressive", this.isAggressive());
        nbt.putBoolean("PVP", this.isPVP());
        nbt.putFloat("Hunger", this.getCreatureHunger());
        nbt.putFloat("Stamina", this.getStamina());
    }


    // ==================================================
    //                       Sounds
    // ==================================================
    // ========== Idle ==========

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     **/
    @Override
    public int getAmbientSoundInterval() {
        if (this.isTamed())
            return 600;
        return super.getAmbientSoundInterval();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        String sound = "_say";
        if (this.isTamed() && this.getHealth() < this.getMaxHealth())
            sound = "_beg";
        return ObjectManager.getSound(this.getSoundName() + sound);
    }


    // ========== Tame ==========
    public void playTameSound() {
        this.playSound(ObjectManager.getSound(this.getSoundName() + "_tame"), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // ========== Eat ==========
    public void playEatSound() {
        this.playSound(ObjectManager.getSound(this.getSoundName() + "_eat"), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}
