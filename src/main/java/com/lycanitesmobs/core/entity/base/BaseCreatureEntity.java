package com.lycanitesmobs.core.entity.base;

import com.google.common.base.Predicate;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.Variant;
import com.lycanitesmobs.core.data.info.creature.*;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.data.info.item.ItemDrop;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.IFusable;
import com.lycanitesmobs.core.entity.IGroupBoss;
import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.util.Targeting;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.container.creature.CreatureContainer;
import com.lycanitesmobs.core.container.provider.CreatureContainerProvider;
import com.lycanitesmobs.core.entity.damagesources.ElementDamageSource;
import com.lycanitesmobs.core.entity.goals.actions.*;
import com.lycanitesmobs.core.entity.goals.targeting.*;
import com.lycanitesmobs.core.entity.item.CustomItemEntity;
import com.lycanitesmobs.core.entity.navigation.CreatureMoveController;
import com.lycanitesmobs.core.entity.navigation.CreaturePathNavigator;
import com.lycanitesmobs.core.entity.navigation.DirectNavigator;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.entity.util.CreatureRelationships;
import com.lycanitesmobs.core.entity.util.CreatureStats;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.container.creature.CreatureInventory;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.item.special.ItemSoulgazer;
import com.lycanitesmobs.core.network.message.MessageCreature;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import com.lycanitesmobs.core.event.SpawnerEventListener;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class BaseCreatureEntity extends PathfinderMob {
    public static final Attribute DEFENSE = (new RangedAttribute(LycanitesMobs.MODID + ":generic.defense", 4.0D, 0.0D, 1024.0D)).setSyncable(true);
    public static final Attribute RANGED_SPEED = (new RangedAttribute(LycanitesMobs.MODID + ":generic.ranged_speed", 4.0D, 0.0D, 1024.0D)).setSyncable(true);

    // Core:
    /**
     * Used to sync the Head Equipment slot of this creature. Currently unused.
     **/
    public static final EntityDataAccessor<ItemStack> EQUIPMENT_HEAD = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
    /**
     * Used to sync the Chest Equipment slot of this creature. Used by Pet (Horse) Armor.
     **/
    public static final EntityDataAccessor<ItemStack> EQUIPMENT_CHEST = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
    /**
     * Used to sync the Legs Equipment slot of this creature. Currently unused.
     **/
    public static final EntityDataAccessor<ItemStack> EQUIPMENT_LEGS = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
    /**
     * Used to sync the Feet Equipment slot of this creature. Currently unused.
     **/
    public static final EntityDataAccessor<ItemStack> EQUIPMENT_FEET = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
    /**
     * Used to sync the Bag Equipment slot of this creature.
     **/
    public static final EntityDataAccessor<ItemStack> EQUIPMENT_BAG = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
    /**
     * Used to sync the Saddle Equipment slot of this creature.
     **/
    public static final EntityDataAccessor<ItemStack> EQUIPMENT_SADDLE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
    /**
     * Used to sync what targets this creature has.
     **/
    protected static final EntityDataAccessor<Byte> TARGET = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);

    // Info:
    /**
     * Used to sync which attack phase this creature is in.
     **/
    protected static final EntityDataAccessor<Byte> ATTACK_PHASE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
    /**
     * Used to sync what animation states this creature is in.
     **/
    protected static final EntityDataAccessor<Byte> ANIMATION_STATE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
    /**
     * Used to sync the current attack cooldown animation, useful for when creature attack cooldowns change dynamically.
     **/
    protected static final EntityDataAccessor<Integer> ANIMATION_ATTACK_COOLDOWN_MAX = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.INT);
    /**
     * Used to sync if this creature is climbing or not. TODO Perhaps move this into ANIMATION_STATE_BITS.
     **/
    protected static final EntityDataAccessor<Byte> CLIMBING = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
    /**
     * Used to sync the stealth percentage of this creature. Where 0.0 is unstealthed and 1.0 is fully stealthed, see burrowing Crusks for an example.
     **/
    protected static final EntityDataAccessor<Float> STEALTH = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.FLOAT);
    /**
     * Used to sync the baby status of this creature.
     **/
    protected static final EntityDataAccessor<Boolean> BABY = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BOOLEAN);
    /**
     * Used to sync the dyed coloring of this creature. This will go towards colorable pet collars or saddles, etc in the future.
     **/
    protected static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
    /**
     * Used to sync the size scale of this creature.
     **/
    protected static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.FLOAT);

    // Size:
    /**
     * Used to sync the stat level of this creature.
     **/
    protected static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.INT);
    /**
     * Used to sync the experience of this creature.
     **/
    protected static final EntityDataAccessor<Integer> EXPERIENCE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.INT);
    /**
     * Used to sync the subspecies used by this creature.
     **/
    protected static final EntityDataAccessor<Byte> SUBSPECIES = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
    /**
     * Used to sync the variant used by this creature.
     **/
    protected static final EntityDataAccessor<Byte> VARIANT = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);

    // Stats:
    /**
     * Used to sync the central arena position that this creature is using if any. See Asmodeus jumping for an example.
     **/
    protected static final EntityDataAccessor<Optional<BlockPos>> ARENA = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    // Boss Health:
    public static int BOSS_DAMAGE_LIMIT = 50;
    /**
     * The Creature Info used by this creature.
     **/
    public CreatureInfo creatureInfo;
    /**
     * The Creature Stats instance used by this Entity instance to get and manage stats.
     **/
    public CreatureStats creatureStats;
    /**
     * The Subspecies of this creature.
     **/
    public Subspecies subspecies = null;
    /**
     * The Variant of this creature, if null this creature is the default common variant.
     **/
    public Variant variant = null;
    /**
     * What attribute is this creature, used for effects such as Bane of Arthropods.
     **/
    public MobType attribute = MobType.UNDEAD;
    /**
     * The Creature's relationships, for advanced memory and taming.
     **/
    public CreatureRelationships relationships;
    /**
     * A class that opens up extra stats and behaviours for NBT based customization.
     **/
    public ExtraMobBehaviour extraMobBehaviour;
    /**
     * The living update tick.
     **/
    public long updateTick = 0;
    /**
     * The name of the event that spawned this mob if any, an empty string ("") if none.
     **/
    public String spawnEventType = "";
    /**
     * The number of the event that spawned this mob. Used for despawning this mob when a new event starts. Ignored if the spawnEventType is blank or the count is less than 0.
     **/
    public int spawnEventCount = -1;
    /**
     * The Pet Entry for this mob, this binds this mob to a entity for special interaction, it will also cause this mob to be removed if the entity it is bound to is removed or dead.
     **/
    public PetEntry petEntry;
    protected boolean boundPetOrphan = false;
    /**
     * If true, this mob will be treated as if it was spawned from an Altar, this is typically called directly by AltarInfo or by events triggered by AltarInfo.
     **/
    public boolean altarSummoned = false;
    /**
     * If true, this mob will show a boss health bar, regardless of other properties, unless overridden by a subclass.
     **/
    public boolean forceBossHealthBar = false;
    /**
     * The Summoning Pedestal that summoned this creature, null if not summoned via a pedestal.
     **/
    public TileEntitySummoningPedestal summoningPedestal;
    /**
     * The main size of this creature, used to override vanilla sizing.
     **/
    public EntityDimensions creatureSize;

    // Abilities:
    /**
     * The size scale of this mob. Randomly varies normally by 10%.
     **/
    public double sizeScale = 1.0D;
    /**
     * A scale relative to this entity's width for melee and ranged hit collision.
     **/
    public float hitAreaWidthScale = 1;
    /**
     * A scale relative to this entity's height for melee and ranged hit collision.
     **/
    public float hitAreaHeightScale = 1;
    /**
     * How many attack phases this mob has, used for varied attack speeds, etc.
     **/
    public byte attackPhaseMax = 0;
    /**
     * Which attack phase this mob is on, used for varied attack speeds, etc.
     **/
    public byte attackPhase = 0;
    /**
     * The current Battle Phase of this mob, each Phase uses different behaviours. Used by bosses.
     **/
    public int battlePhase = 0;
    /**
     * How long this mob should run away for before it stops.
     **/
    public int fleeTime = 200;
    /**
     * How long has this mob been running away for.
     **/
    public int currentFleeTime = 0;

    // Positions:
    /**
     * What percentage of health this mob will run away at, from 0.0F to 1.0F
     **/
    public float fleeHealthPercent = 0;
    /**
     * The maximum amount of damage this mob can take. If 0 or less, this is ignored.
     **/
    public int damageMax = 0;
    /**
     * If above 0, no more than this much health can be lost per second.
     **/
    public float damageLimit = 0;
    /**
     * A client side tick count that increases each render tick, used for smooth animation loops per entity.
     **/
    public float renderTick = 0;
    /**
     * How much damage this creature has taken over the latest second.
     **/
    public float damageTakenThisSec = 0;
    /**
     * How much health this creature had last tick.
     **/
    public float healthLastTick = -1;
    /**
     * The battle range of this boss mob, anything out of this range cannot harm the boss. This will also affect other things related to the boss.
     **/
    public int bossRange = 60;
    /**
     * Whether or not this mob is hostile by default. Use isHostile() when check if this mob is hostile.
     **/
    public boolean isAggressiveByDefault = true;
    /**
     * Whether if this mob is on fire, it should spread it to other entities when melee attacking.
     **/
    public boolean spreadFire = false;
    /**
     * Used to check if the mob was stealth last update.
     **/
    public boolean stealthPrev = false;
    /**
     * When above 0 this mob will be considered blocking and this will count down to 0. Blocking mobs have additional defense.
     **/
    public int currentBlockingTime = 0;

    // Spawning:
    /**
     * How long this mob should usually block for in ticks.
     **/
    public int blockingTime = 60;
    /**
     * The entity picked up by this entity (if any).
     **/
    public LivingEntity pickupEntity;
    /**
     * If true, this entity will have a solid collision box allowing other entities to stand on top of it as well as blocking player movement based on mass more effectively.
     **/
    public boolean solidCollision = false;
    // AI Goals:
    public int nextPriorityGoalIndex;
    public int nextDistractionGoalIndex;
    public int nextCombatGoalIndex;
    public int nextTravelGoalIndex;
    public int nextIdleGoalIndex;
    public int nextReactTargetIndex;
    public int nextSpecialTargetIndex;
    public int nextFindTargetIndex;
    /**
     * Use the onFirstSpawn() method and not this variable. True if this creature has spawned for the first time (naturally or via spawn egg, etc, not reloaded from a saved chunk).
     **/
    public boolean firstSpawn = true;
    /**
     * True if this creature needs to pick a random level during it's onFirstSpawn, this is ignored if firstSpawn is false.
     **/
    public boolean needsInitialLevel = true;

    // Movement:
    /**
     * Should this mob check for block collisions when spawning?
     **/
    public boolean spawnsInBlock = false;
    /**
     * Can this mob spawn where it can't see the sky above?
     **/
    public boolean spawnsUnderground = true;
    /**
     * Can this mob spawn on land (not in liquids)?
     **/
    public boolean spawnsOnLand = true;

    // Targets:
    /**
     * Does this mob spawn inside liquids?
     **/
    public boolean spawnsInWater = false;
    /**
     * If true, this creature will swim in and if set, will suffocate without lava instead of without water.
     **/
    public boolean isLavaCreature = false;
    /**
     * Is this mob a minion? (Minions don't drop items and other things).
     **/
    public boolean isMinion = false;
    /**
     * If true, this mob is temporary and will eventually despawn once the temporaryDuration is at or below 0.
     **/
    public boolean isTemporary = false;
    /**
     * If this mob is temporary, this will count down to 0, once per tick. Once it hits 0, this creature will despawn.
     **/
    public int temporaryDuration = 0;
    /**
     * If true, this mob will not despawn naturally regardless of other rules.
     **/
    public boolean forceNoDespawn = false;
    /**
     * Can be set to true by custom spawners in rare cases. If true, this mob has a higher chance of being a subspecies.
     **/
    public boolean spawnedRare = false;
    /**
     * Set to true when this mob is spawned as a boss, this is used to make non-boss mobs behave like bosses.
     **/
    public boolean spawnedAsBoss = false;
    /**
     * The flight navigator class, a makeshift class that handles flight and free swimming movement, replaces the pathfinder.
     **/
    public DirectNavigator directNavigator;
    /**
     * A list of multiple player targets, used by boss goals.
     **/
    public List<Player> playerTargets = new ArrayList<>();

    // Client:
    /**
     * A list of all minions summoned by this creature.
     **/
    public List<LivingEntity> minions = new ArrayList<>();
    /**
     * A list of player entities that need to have their GUI of this mob reopened on refresh.
     **/
    public List<Player> guiViewers = new ArrayList<>();
    /**
     * Counts from the guiRefreshTime down to 0 when a GUI refresh has been scheduled.
     **/
    public int guiRefreshTick = 0;
    /**
     * The amount of ticks to wait before a GUI refresh.
     **/
    public int guiRefreshTime = 2;
    /**
     * True if this mob should play a sound when attacking. Ranged mobs usually don't use this as their projectiles makes an attack sound instead.
     **/
    public boolean hasAttackSound = false;
    /**
     * True if this mob should play a sound when walking. Usually footsteps.
     **/
    public boolean hasStepSound = true;
    /**
     * True if this mob should play a sound when jumping, used mostly for mounts.
     **/
    public boolean hasJumpSound = false;
    /**
     * The delay in ticks between flying sounds such as wing flapping, set to 0 for no flight sounds.
     **/
    public int flySoundSpeed = 0;
    /**
     * An extra animation boolean.
     **/
    public boolean extraAnimation01 = false;
    /**
     * Holds Information for this mobs boss health should it be displayed in the boss health bar. Used by bosses and rare subspecies.
     **/
    public ServerBossEvent bossInfo;

    // Data Manager:
    /**
     * If positive, this creature entity is only being used for rendering in a GUI, etc and should play animation based off of this instead.
     **/
    public float onlyRenderTicks = -1;
    /**
     * The inventory object of the creature, this is used for managing and using the creature's inventory.
     **/
    public CreatureInventory inventory;
    /**
     * A collection of drops which are used when randomly drop items on death.
     **/
    public List<ItemDrop> drops = new ArrayList<>();
    /**
     * A collection of drops to be stored in NBT data.
     **/
    public List<ItemDrop> savedDrops = new ArrayList<>();
    // Override AI:
    public FindAttackTargetGoal aiTargetPlayer = null;
    public RevengeGoal aiDefendAnimals = null;
    /**
     * Adding custom flying speed function because this was removed in recent versions.
     */
    public float flyingSpeed = this.getControllingPassenger() instanceof Player ? this.getSpeed() * 0.1F : 0.02F;
    /**
     * Set to true when an advanced network sync is required.
     **/
    protected boolean syncQueued = true;
    /**
     * The level of this mob, higher levels increase the stat multipliers by a small amount.
     **/
    protected int mobLevel = 1;
    /**
     * The current amount of experience this creature has.
     **/
    protected int experience = 0;
    /**
     * The cooldown between basic attacks in ticks. Set server side based on AI with an initial value. Used client side to perform attack animations and for cooldown states, etc.
     **/
    protected int attackCooldownMax = 5;
    /**
     * The current cooldown time remaining until the next basic attack is ready. Used client side for attack animations.
     **/
    protected int attackCooldown = 0;
    /**
     * The gorwing age of this mob.
     **/
    protected int growingAge;
    /**
     * A location used for mobs that stick around a certain home spot.
     **/
    protected BlockPos homePosition = new BlockPos(0, 0, 0);
    /**
     * How far this mob can move from their home spot.
     **/
    protected float homeDistanceMax = -1.0F;
    /**
     * A central point set by arenas or events that spawn mobs. Bosses use this to setup arena-based movement.
     **/
    protected BlockPos arenaCenter = null;
    /**
     * A list of Entity Types that this creature is naturally hostile towards, any Attack Targeting Goal will add to this list.
     **/
    protected List<EntityType> hostileTargets = new ArrayList<>();
    /**
     * A list of Entity Classes that this creature is naturally hostile towards, any Attack Targeting Goal will add to this list.
     **/
    protected List<Class<? extends Entity>> hostileTargetClasses = new ArrayList<>();
    /**
     * If true, this creature will not drop any items until a player has damaged it in some way, saved to NBT data.
     **/
    protected boolean dropsRequirePlayerDamage = false;
    /**
     * A forced fix to prevent mobs from endlessly dropping loot when they are unable to die correctly due to their health being altered.
     **/
    protected boolean hasDropped = false;
    /**
     * Whether the mob should use it's leash AI or not.
     **/
    private boolean leashAIActive = false;
    /**
     * Movement AI for mobs that are leashed.
     **/
    private Goal leashMoveTowardsRestrictionAI = new MoveRestrictionGoal(this);
    /**
     * A target used for alpha creatures or connected mobs such as following concapede segements.
     **/
    private LivingEntity masterTarget;
    /**
     * A target used usually for child mobs or connected mobs such as leading concapede segments.
     **/
    private LivingEntity parentTarget;
    /**
     * A target that this mob should usually run away from.
     **/
    private LivingEntity avoidTarget;
    /**
     * A target that this mob just normally always attack if set.
     **/
    private LivingEntity fixateTarget;

    // Interaction:
    /**
     * Used to identify the fixate target when loading this saved entity.
     **/
    private UUID fixateUUID = null;

    // GUI Commands:
    /**
     * The entity that this mob is perching on.
     **/
    private LivingEntity perchTarget;

    // Pet Commands:

    /**
     * Constructor
     *
     * @param entityType The Entity Type.
     * @param world      The world the entity is in.
     */
    protected BaseCreatureEntity(EntityType<? extends BaseCreatureEntity> entityType, Level world) {
        super(entityType, world);

        // Movement:
        this.moveControl = this.createMoveController();

        // Path On Fire or In Lava:
        if (!this.canBurn()) {
            this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
            if (this.canBreatheUnderlava()) {
                this.setPathfindingMalus(BlockPathTypes.LAVA, 1.0F);
                if (!this.canBreatheAir()) {
                    this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
                }
            }
        }

        // Path In Water:
        if (this.waterDamage())
            this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        else if (this.canBreatheUnderwater()) {
            this.setPathfindingMalus(BlockPathTypes.WATER, 1.0F);
            if (!this.canBreatheAir())
                this.setPathfindingMalus(BlockPathTypes.WATER, 8.0F);
        }

        // Swimming:
        if (this.canWade() && this.getNavigation() instanceof CreaturePathNavigator) {
            CreaturePathNavigator pathNavigator = (CreaturePathNavigator) this.getNavigation();
            pathNavigator.setCanFloat(true);
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level().isClientSide) {
            this.applyDynamicAttributes();
        }
    }

    // Items:

    /**
     * Returns Registers attributes and returns an attribute map for assigning to an EntityType.
     *
     * @return Returns a mutable AttributeModifierMap.
     */
    public static AttributeSupplier.Builder registerCustomAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(DEFENSE)
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.ATTACK_SPEED)
                .add(RANGED_SPEED)
                .add(Attributes.FOLLOW_RANGE);
    }

    protected void onKillEntity(LivingEntity entityLivingBase) {
    }

    public boolean getDistanceSq(Vector3f vector3f) {
        return false;
    }

    @Override
    public EntityType getType() {
        if (this.creatureInfo == null) {
            return super.getType();
        }
        return this.creatureInfo.getEntityType();
    }

    /**
     * Registers all Data Manager Parameters and Creature Subsystems.
     */
    @Override
    protected void defineSynchedData() {
        this.creatureInfo = CreatureManager.getInstance().getCreature(this.getClass());
        super.defineSynchedData();
        this.entityData.define(TARGET, (byte) 0);
        this.entityData.define(ATTACK_PHASE, (byte) 0);
        this.entityData.define(ANIMATION_STATE, (byte) 0);
        this.entityData.define(ANIMATION_ATTACK_COOLDOWN_MAX, 0);

        this.entityData.define(CLIMBING, (byte) 0);
        this.entityData.define(STEALTH, 0.0F);

        this.entityData.define(COLOR, (byte) 0);
        this.entityData.define(SIZE, (float) 1D);
        this.entityData.define(LEVEL, 1);
        this.entityData.define(EXPERIENCE, 0);
        this.entityData.define(SUBSPECIES, (byte) 0);
        this.entityData.define(VARIANT, (byte) 0);

        this.entityData.define(ARENA, Optional.empty());
        CreatureInventory.registerData(this.entityData);

        this.loadCreatureFlags();
        this.creatureSize = new EntityDimensions((float) this.creatureInfo.width, (float) this.creatureInfo.height, false);

        this.creatureStats = new CreatureStats(this);
        this.relationships = new CreatureRelationships(this);
        this.extraMobBehaviour = new ExtraMobBehaviour(this);
        this.directNavigator = new DirectNavigator(this);

        // Prepare AI Indexes:
        this.nextPriorityGoalIndex = 10;
        this.nextDistractionGoalIndex = 30;
        this.nextCombatGoalIndex = 50;
        this.nextTravelGoalIndex = 70;
        this.nextIdleGoalIndex = 90;

        this.nextReactTargetIndex = 10;
        this.nextSpecialTargetIndex = 30;
        this.nextFindTargetIndex = 50;
    }


    /**
     * Loads this entity's dynamic attributes.
     */
    public void applyDynamicAttributes() {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.creatureStats.getHealth());
        this.getAttribute(DEFENSE).setBaseValue(this.creatureStats.getDefense());
        this.getAttribute(Attributes.ARMOR).setBaseValue(this.creatureStats.getArmor());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.creatureStats.getSpeed());
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(this.creatureStats.getKnockbackResistance());
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(this.creatureStats.getSight());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.creatureStats.getDamage());
        this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(this.creatureStats.getAttackSpeed());
        this.getAttribute(RANGED_SPEED).setBaseValue(this.creatureStats.getRangedSpeed());
    }

    /**
     * Called when this entity needs to reload its (dynamic) attributes. Should be called when the subspecies, level, etc of this creature changes.
     */
    public void refreshAttributes() {
        this.applyDynamicAttributes();
        this.setHealth(this.getMaxHealth());
        this.refreshBossHealthName();
    }

    /**
     * Called after the Creature Info is applied, should be used to read from creature flags.
     */
    public void loadCreatureFlags() {

    }

    /**
     * Registers all AI Goals for this entity.
     */
    @Override
    protected void registerGoals() {
        // Greater Targeting:
        if (this instanceof IFusable)
            this.targetSelector.addGoal(this.nextSpecialTargetIndex++, new FindFuseTargetGoal(this));
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new AvoidIfHitGoal(this).setHelpCall(true));
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new RevengeGoal(this).setHelpCall(true).setCheckSight(true));

        // Greater Actions:
        this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new PaddleGoal(this));
        this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new StayByWaterGoal(this));
        this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new AvoidGoal(this).setNearSpeed(1.3D).setFarSpeed(1.2D).setNearDistance(5.0D).setFarDistance(20.0D));
        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setTemptDistanceMin(4.0D));
        if (this instanceof IFusable)
            this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new FollowFuseGoal(this).setLostDistance(16));

        super.registerGoals();

        // Lesser Targeting:
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindGroupAttackTargetGoal(this));
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindGroupAvoidTargetGoal(this).setTameTargetting(false));

        // Lesser Actions:
        this.goalSelector.addGoal(this.nextTravelGoalIndex++, new FollowMasterGoal(this).setStrayDistance(12.0D));
        this.goalSelector.addGoal(this.nextIdleGoalIndex++, new WanderGoal(this));
        this.goalSelector.addGoal(this.nextIdleGoalIndex++, new WatchClosestGoal(this).setTargetClass(Player.class));
        this.goalSelector.addGoal(this.nextIdleGoalIndex++, new LookIdleGoal(this));
    }

    /**
     * The final setup stage when constructing this entity, should be called last by the constructors of each specific entity class.
     */
    public void setupMob() {
        // Stats:
        this.setMaxUpStep(0.5F);
        this.inventory = new CreatureInventory(this.getName().getString(), this);

        // Drops:
        this.loadItemDrops();
        if (ItemEquipmentPart.MOB_PART_DROPS.containsKey(this.creatureInfo.getEntityId())) {
            for (ItemEquipmentPart itemEquipmentPart : ItemEquipmentPart.MOB_PART_DROPS.get(this.creatureInfo.getEntityId())) {
                ItemDrop partDrop = new ItemDrop(LMHelperClass.convertToResourceLocation(itemEquipmentPart).toString(), itemEquipmentPart.dropChance).setMaxAmount(1);
                partDrop.bonusAmount = false;
                partDrop.amountMultiplier = false;
                this.drops.add(partDrop);
            }
        }

        // Attack Animation Cooldown:
        this.setAttackCooldownMax(this.attackCooldownMax);
    }

    /**
     * Loads all default item drops, will be ignored if the Enable Default Drops config setting for this mob is set to false, should be overridden to add drops.
     **/
    public void loadItemDrops() {
        this.drops.addAll(this.creatureInfo.drops);
        this.drops.addAll(CreatureManager.getInstance().config.getGlobalDrops());
    }

    /**
     * Adds a saved item drop to this creature instance where it will be read/written from/to NBT Data.
     **/
    public void addSavedItemDrop(ItemDrop itemDrop) {
        this.drops.add(itemDrop);
        this.savedDrops.add(itemDrop);
    }

    @Override
    public int getExperienceReward() {
        float scaledExp = this.creatureInfo.experience;
        if (this.getVariant() != null) {
            if ("uncommon".equals(this.getVariant().rarity))
                scaledExp = Math.round((float) (this.creatureInfo.experience * Variant.UNCOMMON_EXPERIENCE_SCALE));
            else if ("rare".equals(this.getVariant().rarity))
                scaledExp = Math.round((float) (this.creatureInfo.experience * Variant.RARE_EXPERIENCE_SCALE));
        }
        this.xpReward = Math.round(scaledExp);
        return this.xpReward;
    }

    /**
     * Returns the display name of this entity. Use this when displaying its name.
     **/
    @Override
    public Component getName() {
        if (this.hasCustomName())
            return this.getCustomName();
        else
            return this.getFullName();
    }

    /**
     * Returns the display title of this entity.
     **/
    public Component getFullName() {
        String nameFormatting = Component.translatable("entity.lycanitesmobs.creature.name.format").getString();
        String[] nameParts = nameFormatting.split("\\|");
        if (nameParts.length < 4 || nameFormatting.equals("entity.lycanitesmobs.creature.name.format")) {
            nameParts = new String[]{"age", "variant", "subspecies", "species", "level"};
        }

        MutableComponent name = Component.literal("");
        List<Component> nameComponents = new ArrayList<>();
        for (String namePart : nameParts) {
            switch (namePart) {
                case "age":
                    nameComponents.add(this.getAgeName());
                    break;
                case "variant":
                    nameComponents.add(this.getVariantName());
                    break;
                case "subspecies":
                    nameComponents.add(this.getSubspeciesName());
                    break;
                case "species":
                    nameComponents.add(this.getSpeciesName());
                    break;
                case "level":
                    nameComponents.add(this.getLevelName());
                    break;
            }
        }

        boolean first = true;
        for (Component nameComponent : nameComponents) {
            if (nameComponent.getString().isEmpty()) {
                continue;
            }
            if (!first) {
                name.append(" ");
            }
            first = false;
            name.append(nameComponent);
        }

        return name;
    }

    // ========== Item Drops ==========

    /**
     * Returns the species name of this entity.
     **/
    public Component getSpeciesName() {
        return this.creatureInfo.getTitle();
    }

    /**
     * Gets the name of this entity relative to its age, more useful for EntityCreatureAgeable.
     **/
    public Component getAgeName() {
        return Component.literal("");
    }

    /**
     * Returns the variant name (translated) of this entity, returns a blank string if this is a base species mob.
     **/
    public Component getVariantName() {
        if (this.getVariant() != null) {
            return this.getVariant().getTitle();
        }
        return Component.literal("");
    }

    // ========== Name ==========

    /**
     * Returns the subspecies title (translated name) of this entity, returns a blank string if this is a base species mob.
     **/
    public Component getSubspeciesName() {
        if (this.getSubspecies() != null) {
            return this.getSubspecies().getTitle();
        }
        return Component.literal("");
    }

    /**
     * Returns a mobs level to append to the name if above level 1.
     **/
    public Component getLevelName() {
        if (this.getMobLevel() < 2) {
            return Component.literal("");
        }
        return Component.translatable("entity.level").append(" " + this.getMobLevel());
    }

    /**
     * Queues an advanced network sync of this entity to all clients for the next update tick.
     * Called by various Creature Subsystems to sync more specific information.
     * Ignored when called client side, safe to call for convenience.
     */
    public void queueSync() {
        if (this.getCommandSenderWorld().isClientSide()) {
            return;
        }
        this.syncQueued = true;
    }

    /**
     * Performs an advanced network sync of this entity to all clients.
     * Used by various Creature Subsystems to sync more specific information.
     * Ignored when called client side, safe to call for convenience.
     */
    public void doSync() {
        this.syncQueued = false;
        if (this.getCommandSenderWorld().isClientSide()) {
            return;
        }

        // Relationships:
        for (Player player : this.relationships.getPlayers()) {
            CreatureRelationshipEntry relationshipEntry = this.relationships.getEntry(player);
            if (relationshipEntry == null) {
                continue;
            }
            MessageCreature message = new MessageCreature(this, relationshipEntry.getReputation());
            LycanitesMobs.PACKET_MANAGER.sendToPlayer(message, (ServerPlayer) player);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        // TODO Move data manager sync to here and remove these getters.
        super.onSyncedDataUpdated(key);
    }

    public boolean getBoolFromDataManager(EntityDataAccessor<Boolean> key) {
        try {
            return this.getEntityData().get(key);
        } catch (Exception e) {
            return false;
        }
    }

    public byte getByteFromDataManager(EntityDataAccessor<Byte> key) {
        try {
            return this.getEntityData().get(key);
        } catch (Exception e) {
            return 0;
        }
    }


    // ==================================================
    //              Data Manager and Network
    // ==================================================

    public int getIntFromDataManager(EntityDataAccessor<Integer> key) {
        try {
            return this.getEntityData().get(key);
        } catch (Exception e) {
            return 0;
        }
    }

    public float getFloatFromDataManager(EntityDataAccessor<Float> key) {
        try {
            return this.getEntityData().get(key);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getStringFromDataManager(EntityDataAccessor<String> key) {
        try {
            return this.getEntityData().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public Optional<UUID> getUUIDFromDataManager(EntityDataAccessor<Optional<UUID>> key) {
        try {
            return this.getEntityData().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public ItemStack getItemStackFromDataManager(EntityDataAccessor<ItemStack> key) {
        try {
            return this.getEntityData().get(key);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public Optional<BlockPos> getBlockPosFromDataManager(EntityDataAccessor<Optional<BlockPos>> key) {
        try {
            return this.getEntityData().get(key);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Checks if the creature is able to spawn at it's initial position.
     **/
    @Override
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType spawnReason) {
        return this.checkSpawnVanilla(this.getCommandSenderWorld(), spawnReason, this.blockPosition());
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return this.creatureInfo.creatureSpawn.spawnGroupMax;
    }

    /**
     * Performs checks when spawned by a vanilla spawner or possibly another modded spawner if they use the vanilla checks.
     **/
    public boolean checkSpawnVanilla(Level world, MobSpawnType spawnReason, BlockPos pos) {
        if (world.isClientSide) {
            return false;
        }
        if (spawnReason != MobSpawnType.NATURAL && spawnReason != MobSpawnType.SPAWNER) {
            return true;
        }

        LMHelperClass.logDebug("MobSpawns", " ~O==================== Vanilla Spawn Check: " + this.creatureInfo.getName() + " ====================O~");
        LMHelperClass.logDebug("MobSpawns", "Attempting to Spawn: " + this.creatureInfo.getName());
        LMHelperClass.logDebug("MobSpawns", "Target Spawn Location: " + pos);

        // Enabled Check:
        LMHelperClass.logDebug("MobSpawns", "Checking if creature is enabled...");
        if (!this.creatureInfo.enabled || !this.creatureInfo.creatureSpawn.enabled) {
            return false;
        }

        // Peaceful Check:
        LMHelperClass.logDebug("MobSpawns", "Checking for peaceful difficulty...");
        if (!this.creatureInfo.peaceful && this.getCommandSenderWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        // Gamerule Check:
        LMHelperClass.logDebug("MobSpawns", "Checking gamerules...");
        if (!this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return false;
        }

        // Fixed Spawning Checks:
        LMHelperClass.logDebug("MobSpawns", "Fixed spawn check (light level, collisions)...");
        if (!this.fixedSpawnCheck(world, pos)) {
            return false;
        }

        // Mob Spawner Check:
        if (spawnReason == MobSpawnType.SPAWNER) {
            LMHelperClass.logDebug("MobSpawns", "Spawned from Mob Spawner, skipping other checks.");
            LMHelperClass.logDebug("MobSpawns", "Vanilla Spawn Check Passed!");
            return true;
        }
        LMHelperClass.logDebug("MobSpawns", "No Mob Spawner found.");

        // Global Spawn Check:
        LMHelperClass.logDebug("MobSpawns", "Global Spawn Check (Master Dimension List, etc)...");
        if (!CreatureManager.getInstance().spawnConfig.isAllowedGlobal(world)) {
            return false;
        }

        // Environment Spawning Checks:
        LMHelperClass.logDebug("MobSpawns", "Environment spawn check (dimension, group limit, ground type, water, lava, underground)...");
        if (!this.environmentSpawnCheck(world, pos)) {
            return false;
        }

        LMHelperClass.logDebug("MobSpawns", "Vanilla Spawn Check Passed!");
        return true;
    }

    /**
     * First stage checks for vanilla spawning, if this check fails the creature will not spawn.
     **/
    public boolean fixedSpawnCheck(Level world, BlockPos pos) {
        if (!this.checkSpawnLightLevel(world, pos)) {
            return false;
        }

        LMHelperClass.logDebug("MobSpawns", "Checking collision...");
        if (!this.checkSpawnCollision(world, pos)) {
            return false;
        }

        LMHelperClass.logDebug("MobSpawns", "Counting mobs of the same kind, max allowed is: " + this.creatureInfo.creatureSpawn.spawnAreaLimit);
        if (!this.checkSpawnGroupLimit(world, pos, CreatureManager.getInstance().spawnConfig.spawnLimitRange))
            return false;

        return true;
    }

    /**
     * Second stage checks for vanilla spawning, this check is ignored if there is a valid monster spawner nearby.
     **/
    public boolean environmentSpawnCheck(Level world, BlockPos pos) {
        if (this.creatureInfo.creatureSpawn.worldDayMin > 0) {
            int currentDay = (int) Math.floor(world.getGameTime() / 24000D);
            LMHelperClass.logDebug("MobSpawns", "Checking game time, currently on day: " + currentDay + ", must be at least day: " + this.creatureInfo.creatureSpawn.worldDayMin + ".");
            if (currentDay < this.creatureInfo.creatureSpawn.worldDayMin)
                return false;
        }
        LMHelperClass.logDebug("MobSpawns", "Checking dimension.");
        if (!this.isNativeDimension(this.getCommandSenderWorld()))
            return false;
        LMHelperClass.logDebug("MobSpawns", "Checking for liquid (water, lava, ooze, etc).");
        if (!this.spawnsInWater && this.getCommandSenderWorld().containsAnyLiquid(this.getBoundingBox()))
            return false;
        else if (!this.spawnsOnLand && !this.getCommandSenderWorld().containsAnyLiquid(this.getBoundingBox()))
            return false;
        LMHelperClass.logDebug("MobSpawns", "Checking for underground.");
        if (!this.spawnsUnderground && this.isBlockUnderground(pos.getX(), pos.getY() + 1, pos.getZ()))
            return false;
        LMHelperClass.logDebug("MobSpawns", "Checking for nearby bosses.");
        if (!this.checkSpawnBoss(world, pos))
            return false;

        return true;
    }


    // ==================================================
    //                     Spawning
    // ==================================================
    // ========== Can Spawn Here ==========

    // ========== Spawn Dimension Check ==========
    public boolean isNativeDimension(Level world) {
        return this.creatureInfo.creatureSpawn.isAllowedDimension(world);
    }

    /**
     * Returns true if there is no collision stopping this mob from spawning.
     **/
    public boolean checkSpawnCollision(Level world, BlockPos pos) {
        double radius = this.creatureInfo.width;
        double height = this.creatureInfo.height;
        AABB spawnBoundries = new AABB(pos.getX() - radius, pos.getY(), pos.getZ() - radius, pos.getX() + radius, pos.getY() + height, pos.getZ() + radius);
        if (!this.spawnsInBlock && !world.noCollision(spawnBoundries)) {
            return false;
        }
        return true;
    }

    // ========== Vanilla Spawn Check ==========

    /**
     * Returns true if the light level is valid for spawning.
     **/
    public boolean checkSpawnLightLevel(Level world, BlockPos pos) {
        if (this.creatureInfo.creatureSpawn.spawnsInDark && this.creatureInfo.creatureSpawn.spawnsInLight) {
            return true;
        }
        if (!this.creatureInfo.creatureSpawn.spawnsInDark && !this.creatureInfo.creatureSpawn.spawnsInLight) {
            return false;
        }

        byte light = this.testLightLevel(pos);
        if (this.creatureInfo.creatureSpawn.spawnsInDark && light <= 1) {
            return true;
        }

        if (this.creatureInfo.creatureSpawn.spawnsInLight && light >= 2) {
            return true;
        }

        return false;
    }

    // ========== Fixed Spawn Check ==========

    /**
     * Checks for nearby entities of this type, mobs use this so that too many don't spawn in the same area. Returns true if the mob should spawn.
     **/
    public boolean checkSpawnGroupLimit(Level world, BlockPos pos, double range) {
        if (range <= 0) {
            return true;
        }

        // Get Limits:
        int typesLimit = CreatureManager.getInstance().spawnConfig.typeSpawnLimit;
        int speciesLimit = this.creatureInfo.creatureSpawn.spawnAreaLimit;
        if (typesLimit <= 0 && speciesLimit <= 0) {
            return true;
        }

        // Count Nearby Entities:
        List<BaseCreatureEntity> targets = this.getNearbyEntities(BaseCreatureEntity.class, entity -> BaseCreatureEntity.class.isAssignableFrom(entity.getClass()), range);
        int typesFound = 0;
        int speciesFound = 0;
        for (BaseCreatureEntity targetCreature : targets) {
            if (targetCreature.creatureInfo.peaceful == this.creatureInfo.peaceful) {
                typesFound++;
            }
            if (this.creatureInfo.entityClass.isAssignableFrom(targetCreature.getClass())) {
                speciesFound++;
            }
        }

        // Check Limits:
        if (range > 0) {
            if (typesLimit > 0 && typesFound >= typesLimit) {
                return false;
            }
            if (speciesLimit > 0 && speciesFound >= speciesLimit) {
                return false;
            }
        }
        return true;
    }

    // ========== Environment Spawn Check ==========

    /**
     * Checks for nearby bosses, mobs usually shouldn't randomly spawn near a boss.
     **/
    public boolean checkSpawnBoss(Level world, BlockPos pos) {
        CreatureGroup bossGroup = CreatureManager.getInstance().getCreatureGroup("boss");
        if (bossGroup == null)
            return true;
        List bosses = this.getNearbyEntities(BaseCreatureEntity.class, entity -> bossGroup.hasEntity(entity), CreatureManager.getInstance().spawnConfig.spawnLimitRange);
        return bosses.size() == 0;
    }

    /**
     * Combined spawn limit check — performs boss proximity AND group limit checks with a single entity scan
     * instead of two separate getNearbyEntities calls. This is a significant performance optimization as
     * world.getEntitiesOfClass() with large AABBs is expensive.
     *
     * @param world           The world to check in.
     * @param pos             The position to check around.
     * @param groupLimitRange The range for group limit checks.
     * @return True if the mob is allowed to spawn (no boss nearby, within group limits).
     */
    public boolean checkSpawnLimits(Level world, BlockPos pos, double groupLimitRange) {
        CreatureGroup bossGroup = CreatureManager.getInstance().getCreatureGroup("boss");
        int typesLimit = CreatureManager.getInstance().spawnConfig.typeSpawnLimit;
        int speciesLimit = this.creatureInfo.creatureSpawn.spawnAreaLimit;
        double bossRange = CreatureManager.getInstance().spawnConfig.spawnLimitRange;

        double range = Math.max(groupLimitRange, bossRange);
        if (range <= 0 && bossGroup == null) {
            return true;
        }

        List<BaseCreatureEntity> nearby = this.getNearbyEntities(
                BaseCreatureEntity.class,
                entity -> BaseCreatureEntity.class.isAssignableFrom(entity.getClass()),
                range
        );

        if (bossGroup != null && bossRange > 0) {
            for (BaseCreatureEntity target : nearby) {
                if (target.distanceTo(this) <= bossRange && bossGroup.hasEntity(target)) {
                    return false;
                }
            }
        }

        if (groupLimitRange <= 0 || (typesLimit <= 0 && speciesLimit <= 0)) {
            return true;
        }
        int typesFound = 0;
        int speciesFound = 0;
        for (BaseCreatureEntity target : nearby) {
            if (target.distanceTo(this) > groupLimitRange) {
                continue;
            }
            if (target.creatureInfo.peaceful == this.creatureInfo.peaceful) {
                typesFound++;
            }
            if (this.creatureInfo.entityClass.isAssignableFrom(target.getClass())) {
                speciesFound++;
            }
        }
        if (typesLimit > 0 && typesFound >= typesLimit) {
            return false;
        }
        if (speciesLimit > 0 && speciesFound >= speciesLimit) {
            return false;
        }
        return true;
    }

    /**
     * Called once this mob is initially spawned.
     **/
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficultyInstance, MobSpawnType spawnReason, @Nullable SpawnGroupData livingEntityData, @Nullable CompoundTag compoundNBT) {
        livingEntityData = super.finalizeSpawn(world, difficultyInstance, spawnReason, livingEntityData, compoundNBT);
        return livingEntityData;
    }

    // ========== Collision Spawn Check ==========

    // ========== Despawning ==========
    @Override
    public boolean isPersistenceRequired() {
        if (!this.canDespawnNaturally()) {
            return true;
        }
        return super.isPersistenceRequired();
    }

    // ========== Light Level Spawn Check ==========

    /**
     * Returns whether this mob should despawn overtime or not. Config defined forced despawns override everything except tamed creatures and tagged creatures.
     **/
    protected boolean canDespawnNaturally() {
        if (this.creatureInfo.creatureSpawn.despawnForced)
            return true;
        if (!this.creatureInfo.creatureSpawn.despawnNatural)
            return false;
        if (this.creatureInfo.boss || (this.isRareVariant() && !Variant.RARE_DESPAWNING))
            return false;
        if (this.isPersistant() || this.isLeashed() || (this.hasCustomName() && "".equals(this.spawnEventType)))
            return false;
        return true;
    }

    // ========== Group Limit Spawn Check ==========

    /**
     * Returns true if this mob should not despawn in unloaded chunks.
     * Most farmable mobs never despawn, but can be set to despawn in the config where this will kick in.
     * Here mobs can check if they have ever been fed or bred or moved from their home dimension.
     * Farmable mobs can then be set to despawn unless they have been farmed by a player.
     * Useful for the Pinky Nether invasion issues! Also good for water animals that can't spawn as CREATURE.
     * Leashed mobs don't ever despawn naturally and don't rely on this.
     * There is also the vanilla variable persistenceRequired which is handled in vanilla code too.
     **/
    public boolean isPersistant() {
        return this.forceNoDespawn;
    }

    // ========== Boss Spawn Check ==========

    @Override
    public void setPersistenceRequired() {
        super.setPersistenceRequired();
        this.forceNoDespawn = true;
    }

    // ========== Egg Spawn ==========

    /**
     * A check that is constantly done, if this returns true, this entity will be removed, used normally for peaceful difficulty removal and temporary minions.
     **/
    public boolean despawnCheck() {
        if (this.getCommandSenderWorld().isClientSide)
            return false;

        // Disabled Mobs:
        if (!this.creatureInfo.enabled)
            return true;

        // Temporary Mobs:
        if (this.isTemporary && this.temporaryDuration-- <= 0)
            return true;

        // Peaceful:
        if (!this.creatureInfo.peaceful && this.getCommandSenderWorld().getDifficulty() == Difficulty.PEACEFUL && !this.hasCustomName()) {
            return true;
        }

        // Mob Event Despawning:
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(this.getCommandSenderWorld());
        if (worldExt != null) {
            if (!"".equals(this.spawnEventType) && this.spawnEventCount >= 0 && this.spawnEventCount != worldExt.getWorldEventCount()) {
                if (this.isLeashed() || this.isPersistant()) {
                    this.spawnEventType = "";
                    this.spawnEventCount = -1;
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the specified block is underground (unable to see the sky above it). This checks through leaves, plants, grass and vine materials.
     **/
    public boolean isBlockUnderground(int x, int y, int z) {
        if (this.getCommandSenderWorld().canSeeSkyFromBelowWater(new BlockPos(x, y, z)))
            return false;
        for (int j = y; j < this.getCommandSenderWorld().getMaxBuildHeight(); j++) {
            BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, j, z));
            boolean isLeaves = LMHelperClass.hasTag(blockState, BlockTags.LEAVES);
            boolean replaceablePlant = LMHelperClass.hasTag(blockState, BlockTags.REPLACEABLE);
            if (blockState.getBlock() != Blocks.AIR
                    && !isLeaves
                    && LMHelperClass.Materials.isPlant(blockState.getBlock())
                    && !replaceablePlant)
                return true;
        }
        return false;
    }

    /**
     * Returns whether or not this mob is a boss.
     **/
    public boolean isBoss() {
        return this.isBossAlways() || this.spawnedAsBoss;
    }

    /**
     * Returns whether or not this mob is always a boss (any mob can be custom spawned as a boss but some mobs are always bosses).
     **/
    public boolean isBossAlways() {
        return this.creatureInfo.boss;
    }

    @Override
    public boolean canChangeDimensions() {
        return !this.isBoss();
    }

    public void createBossInfo(BossEvent.BossBarColor color, boolean darkenSky) {
        MutableComponent name = this.getName().copy();
        if (this.isBossAlways()) {
            name.append(" (").append(Component.translatable("entity.phase")).append(" " + (this.getBattlePhase() + 1) + ")");
        }
        this.bossInfo = (ServerBossEvent) (new ServerBossEvent(name, color, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(darkenSky);
    }

    // ========== Block Checking ==========
    // TODO Cave Air?

    public BossEvent getBossInfo() {
        if (this.bossInfo == null && this.showBossInfo() && !this.getCommandSenderWorld().isClientSide) {
            if (this.isBoss())
                this.createBossInfo(BossEvent.BossBarColor.RED, false);
            else
                this.createBossInfo(BossEvent.BossBarColor.GREEN, false);
        }
        return this.bossInfo;
    }

    // ========== Boss ==========

    /**
     * Updates the boss name for the health bar.
     **/
    public void refreshBossHealthName() {
        if (this.bossInfo != null) {
            MutableComponent name = this.getFullName().copy();
            if (this.isBossAlways()) {
                name.append(" (").append(Component.translatable("entity.phase")).append(" " + (this.getBattlePhase() + 1) + ")");
            }
            this.bossInfo.setName(name);
        }
    }

    /**
     * Summons the provided entity instance into the world as this creature's minion.
     *
     * @param minion   The entity instance to summon as a minion.
     * @param angle    The spawn position angle of the minion reletive to this creature.
     * @param distance How far from this creature to summon the minion.
     */
    public void summonMinion(LivingEntity minion, double angle, double distance) {
        double angleRadians = Math.toRadians(angle);
        double x = this.position().x() + ((this.getDimensions(this.getPose()).width + distance) * Math.cos(angleRadians) - Math.sin(angleRadians));
        double y = this.position().y() + 1;
        if (minion instanceof BaseCreatureEntity && ((BaseCreatureEntity) minion).isFlying()) {
            y += this.getDimensions(this.getPose()).height / 2;
        }
        double z = this.position().z() + ((this.getDimensions(this.getPose()).width + distance) * Math.sin(angleRadians) + Math.cos(angleRadians));
        minion.moveTo(x, y, z, this.random.nextFloat() * 360.0F, 0.0F);
        this.getCommandSenderWorld().addFreshEntity(minion);
        if (minion instanceof BaseCreatureEntity) {
            ((BaseCreatureEntity) minion).setMinion(true);
            if (!this.isRareVariant()) {
                ((BaseCreatureEntity) minion).applyVariant(this.getVariantIndex());
            }
            ((BaseCreatureEntity) minion).setSubspecies(this.getSubspeciesIndex());
            ((BaseCreatureEntity) minion).setMasterTarget(this);
            ((BaseCreatureEntity) minion).spawnEventType = this.spawnEventType;
            if (this.isTemporary) {
                ((BaseCreatureEntity) minion).setTemporary(this.temporaryDuration);
            }
            ((BaseCreatureEntity) minion).onFirstSpawn();
        }
        if (this.getTarget() != null) {
            minion.setLastHurtByMob(this.getTarget());
        }
        this.addMinion(minion);
    }

    /**
     * Adds a minion to this creature.
     *
     * @param minion The minion to add.
     * @return True fi the minion is added, false if it was already added.
     */
    public boolean addMinion(LivingEntity minion) {
        if (this.minions.contains(minion)) {
            return false;
        }
        this.minions.add(minion);
        return true;
    }

    /**
     * Returns a list of minions.
     *
     * @param filterType An entity type to filter the list by, if null all minions are returned.
     * @return A lsit of minions.
     */
    public List<LivingEntity> getMinions(EntityType filterType) {
        if (filterType == null) {
            return this.minions;
        }
        List<LivingEntity> filteredMinions = new ArrayList<>();
        for (LivingEntity minion : this.minions) {
            if (minion.getType() == filterType) {
                filteredMinions.add(minion);
            }
        }
        return filteredMinions;
    }

    /**
     * Called by minions when they update.
     **/
    public void onMinionUpdate(LivingEntity minion, long tick) {
    }

    /**
     * Called by minions when they die.
     **/
    public void onMinionDeath(LivingEntity minion, DamageSource damageSource) {
    }


    // ============================================
    //                   Minions
    // ============================================

    /**
     * Called by AI Goals that attempt to damage minions.
     **/
    public void onTryToDamageMinion(LivingEntity minion, float damageAmount) {
    }

    /**
     * Returns whether or not this mob is a minion.
     **/
    public boolean isMinion() {
        return this.isMinion;
    }

    /**
     * Set whether this mob is a minion or not, this should be used if this mob is summoned.
     **/
    public void setMinion(boolean minion) {
        this.isMinion = minion;
    }

    /**
     * Make this mob temporary where it will desapwn once the specified duration (in ticks) reaches 0.
     **/
    public void setTemporary(int duration) {
        this.temporaryDuration = duration;
        this.isTemporary = true;
    }

    /**
     * Remove the temporary life duration of this mob, note that this mob will still despawn naturally unless it is set as persistent through other means.
     **/
    public void unsetTemporary() {
        this.isTemporary = false;
        this.temporaryDuration = 0;
    }

    /**
     * Returns true if this mob has a pet entry and is thus bound to another entity.
     **/
    public boolean isBoundPet() {
        return this.hasPetEntry();
    }

    /**
     * Returns true if this mob has a pet entry.
     **/
    public boolean hasPetEntry() {
        return this.getPetEntry() != null;
    }

    /**
     * Returns this mob's pet entry if it has one.
     **/
    public PetEntry getPetEntry() {
        return this.petEntry;
    }

    // ========== Temporary Mob ==========

    /**
     * Sets the pet entry for this mob. Mobs with Pet Entries will be removed when te world is reloaded as the pet Entry will spawn a new INSTANCE of them in on load.
     **/
    public void setPetEntry(PetEntry petEntry) {
        this.petEntry = petEntry;
    }

    /**
     * Returns true if this creature has a pet entry and matches the provided entry type.
     **/
    public boolean isPetType(String type) {
        if (!this.hasPetEntry())
            return false;
        return type.equals(this.getPetEntry().getType());
    }

    // ========== Pet ==========

    /**
     * This is called when the mob is first spawned to the world either through natural spawning or from a Spawn Egg.
     **/
    public void onFirstSpawn() {
        this.firstSpawn = false;
        if (this.hasPetEntry()) {
            if (this.getPetEntry().summonSet != null && this.getPetEntry().summonSet.playerExt != null) {
                this.getPetEntry().summonSet.playerExt.sendPetEntryToPlayer(this.getPetEntry());
            }
            return;
        }
        if (this.isMinion())
            return;
        if (this.needsInitialLevel)
            this.applyLevel(this.getStartingLevel());
        if (this.getSubspeciesIndex() == 0 && this.getVariantIndex() == 0) {
            this.getRandomSubspecies();
            if (CreatureManager.getInstance().config.variantsSpawn && !this.creatureInfo.creatureSpawn.disableVariants)
                this.getRandomVariant();
        }
        if (this.sizeScale == 1.0D) {
            if (CreatureManager.getInstance().config.randomSizes)
                this.getRandomSize();
        }
    }

    // ========== Get Random Subspecies ==========
    public void getRandomSubspecies() {
        if (!this.isMinion()) {
            this.subspecies = this.creatureInfo.getRandomSubspecies(this);
            LMHelperClass.logDebug("Subspecies", "Setting " + this.getSpeciesName().getString() + " subspecies to " + this.subspecies.getTitle().getString());
        }
    }

    // ========== Get Random Variant ==========
    public void getRandomVariant() {
        if (!this.isMinion()) {
            Variant randomVariant = this.getSubspecies().getRandomVariant(this, this.spawnedRare);
            if (randomVariant != null) {
                LMHelperClass.logDebug("Subspecies", "Setting " + this.getSpeciesName().getString() + " to " + randomVariant.getTitle().getString());
                this.applyVariant(randomVariant.index);
            } else {
                LMHelperClass.logDebug("Subspecies", "Setting " + this.getSpeciesName().getString() + " to base variant.");
                this.applyVariant(0);
            }
        }
    }

    // ========== Get Random Size ==========
    public void getRandomSize() {
        double range = CreatureManager.getInstance().config.randomSizeMax - CreatureManager.getInstance().config.randomSizeMin;
        double randomScale = range * this.getRandom().nextDouble();
        double scale = CreatureManager.getInstance().config.randomSizeMin + randomScale;
        if (this.getVariant() != null)
            scale *= this.getVariant().getScale();
        this.setSizeScale(scale);
    }

    /**
     * The age value may be negative or positive or zero. If it's negative, it get's incremented on each tick, if it's
     * positive, it get's decremented each tick. Don't confuse this with EntityLiving.getAge. With a negative value the
     * Entity is considered a child.
     */
    public int getAge() {
        if (this.level().isClientSide) {
            return this.getBoolFromDataManager(BABY) ? -1 : 1;
        } else {
            return this.growingAge;
        }
    }

    // ========== On Spawn ==========

    /**
     * Returns the Entity Size of this creature for the given pose with it's size scale applied.
     *
     * @param pose The pose to get the size of.
     * @return The scaled enity size of this creature.
     */
    @Override
    @Nonnull
    public EntityDimensions getDimensions(Pose pose) {
        if (pose == Pose.SLEEPING) {
            return SLEEPING_DIMENSIONS;
        }
        if (this.creatureSize == null) {
            this.creatureSize = this.getType().getDimensions();
        }
        return this.creatureSize.scale(this.getScale());
    }

    /**
     * Sets the size scale of this creature.
     **/
    public void setSizeScale(double scale) {
        this.sizeScale = scale;
        this.refreshDimensions();
    }

    /**
     * Returns the model scale.
     **/
    @Override
    public float getScale() {
        return (float) this.sizeScale * (float) this.creatureInfo.sizeScale;
    }

    /**
     * Returns the level of this mob, higher levels have higher stats.
     **/
    public int getMobLevel() {
        if (this.getCommandSenderWorld().isClientSide) {
            return this.getIntFromDataManager(LEVEL);
        }
        return this.mobLevel;
    }

    /**
     * Returns the default starting level to use.
     **/
    public int getStartingLevel() {
        int startingLevelMin = Math.max(1, CreatureManager.getInstance().config.startingLevelMin);
        if (CreatureManager.getInstance().config.startingLevelMax > startingLevelMin) {
            return startingLevelMin + this.getRandom().nextInt(CreatureManager.getInstance().config.startingLevelMax - startingLevelMin);
        }
        if (CreatureManager.getInstance().config.levelPerDay > 0 && CreatureManager.getInstance().config.levelPerDayMax > 0) {
            int day = (int) Math.floor(this.getCommandSenderWorld().getGameTime() / 23999D);
            double levelGain = Math.min(CreatureManager.getInstance().config.levelPerDay * day, CreatureManager.getInstance().config.levelPerDayMax);
            startingLevelMin += (int) Math.floor(levelGain);
        }
        if (CreatureManager.getInstance().config.levelPerLocalDifficulty > 0) {
            double levelGain = this.getCommandSenderWorld().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            startingLevelMin += Math.max(0, (int) Math.floor(levelGain - 1.5D));
        }
        return startingLevelMin;
    }


    // ==================================================
    //                       Stats
    // ==================================================

    /**
     * Sets and applies the level of this mob refreshing stats, higher levels have higher stats.
     **/
    public void applyLevel(int level) {
        this.needsInitialLevel = false;
        this.setLevel(level);
        this.refreshAttributes();
    }

    /**
     * Sets the level of this mob without refreshing stats, used when loading from NBT or from applyLevel(). If a level is changed use applyLevel() instead.
     **/
    public void setLevel(int level) {
        this.mobLevel = level;
        this.entityData.set(LEVEL, level);
    }

    /**
     * Increases the level of this mob, higher levels have higher stats.
     **/
    public void addLevel(int level) {
        this.applyLevel(this.mobLevel + level);
    }

    /**
     * Checks this creature's experience and performs a level up if it has enough to do so.
     */
    public void updateLevelExperience() {
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(EXPERIENCE, this.experience);
        }
        if (this.getExperience() >= this.creatureStats.getExperienceForNextLevel()) {
            this.setExperience(this.getExperience() - this.creatureStats.getExperienceForNextLevel());
            this.addLevel(1);

            // Particles:
            //if(this.getEntityWorld().isRemote) {
            for (int i = 0; i < 20; ++i) {
                this.getCommandSenderWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
            }
            //}
        }
    }

    /**
     * Returns how much experience this creature has towards the next level.
     *
     * @return How much experience this creature has.
     */
    public int getExperience() {
        if (this.getCommandSenderWorld().isClientSide) {
            return this.getIntFromDataManager(EXPERIENCE);
        }
        return this.experience;
    }

    /**
     * Sets how much experience this creature has towards the next level.
     */
    public void setExperience(int experience) {
        this.experience = experience;
        this.updateLevelExperience();
    }

    /**
     * Increases how much experience this creature has towards the next level.
     */
    public void addExperience(int experience) {
        this.experience += experience;
        this.updateLevelExperience();
    }

    /**
     * Returns the cooldown time in ticks between melee attacks.
     *
     * @return Melee attack cooldown ticks.
     */
    public int getMeleeCooldown() {
        return Math.round((float) (1.0D / this.getAttribute(Attributes.ATTACK_SPEED).getValue() * 20.0D));
    }

    /**
     * Returns the cooldown time in ticks between ranged attacks.
     *
     * @return Ranged attack cooldown ticks.
     */
    public int getRangedCooldown() {
        return Math.round((float) ((1.0D / this.getAttribute(RANGED_SPEED).getValue()) * 20.0D));
    }

    /**
     * When given a base time (in seconds) this will return the scaled time with difficulty and other modifiers taken into account.
     *
     * @param seconds The base duration in seconds that this effect should last for, this is scaled by stats.
     * @return The scaled effect duration to use.
     */
    public int getEffectDuration(int seconds) {
        return Math.round(seconds * (float) this.creatureStats.getEffect() * 20);
    }

    /**
     * Returns the default amplifier to use for effects.
     *
     * @param scale The scale to multiplier the amplifier by, use one for default.
     * @return Potion effects amplifier.
     */
    public int getEffectAmplifier(float scale) {
        return Math.round((float) this.creatureStats.getAmplifier());
    }

    /**
     * When given a base effect strength value such as a life drain amount, this will return the scaled value with difficulty and other modifiers taken into account.
     *
     * @param value The base effect strength to scale.
     * @return The strength of a special effect.
     */
    public int getEffectStrength(float value) {
        return Math.round((value * (float) (this.creatureStats.getAmplifier())));
    }


    // ========= Attack Speeds ==========

    /**
     * Returns a list of elements that this creature is using.
     *
     * @return A list of elements.
     */
    public List<ElementInfo> getElements() {
        if (this.getSubspecies() != null && !this.getSubspecies().elements.isEmpty()) {
            return this.getSubspecies().elements;
        }
        return this.creatureInfo.elements;
    }

    /**
     * Returns true if this creature has the provided element.
     *
     * @param element The element to check for.
     * @return True if this creature has the provided element.
     */
    public boolean hasElement(ElementInfo element) {
        return this.getElements().contains(element);
    }

    // ========= Effect ==========

    /**
     * Sets the subspecies of this mob by index and refreshes stats. If not a valid ID or 0 it will be set to null which is for base species.
     **/
    public void applyVariant(int variantIndex) {
        this.setVariant(variantIndex);
        this.refreshAttributes();
    }

    /**
     * Gets the subspecies of this mob.
     **/
    public Subspecies getSubspecies() {
        if (this.subspecies == null) {
            this.subspecies = this.creatureInfo.getSubspecies(0);
        }
        return this.subspecies;
    }

    /**
     * Sets the subspecies of this mob by index, if invalid, defaults to base subspecies.
     **/
    public void setSubspecies(int subspeciesIndex) {
        this.subspecies = this.creatureInfo.getSubspecies(subspeciesIndex);
    }

    /**
     * Gets the variant of this mob, will return null if this is a base variant mob.
     **/
    @Nullable
    public Variant getVariant() {
        return this.variant;
    }

    /**
     * Sets the variant of this mob by index without refreshing stats, use applyVariant() if changing to a new variant. If not a valid ID or 0 it will be set to null which is for the base variant.
     **/
    public void setVariant(int variantIndex) {
        this.variant = this.getSubspecies().getVariant(variantIndex);
        if (this.variant != null) {
            float scaledExp = this.creatureInfo.experience;
            if ("uncommon".equals(this.variant.rarity))
                scaledExp = Math.round((float) (this.creatureInfo.experience * Variant.UNCOMMON_EXPERIENCE_SCALE));
            else if ("rare".equals(this.variant.rarity))
                scaledExp = Math.round((float) (this.creatureInfo.experience * Variant.RARE_EXPERIENCE_SCALE));
            this.xpReward = Math.round(scaledExp);
            if ("rare".equals(this.variant.rarity)) {
                this.damageLimit = BOSS_DAMAGE_LIMIT;
                this.damageMax = BOSS_DAMAGE_LIMIT;
            }
        }
    }


    // ==================================================
    //                    Subspecies
    // ==================================================

    /**
     * Gets the subspecies index of this mob.
     * 0 = Base Subspecies
     * 1/2 = Uncommon Species
     * 3+ = Rare Species
     * Most mobs have 2 uncommon subspecies, some have rare subspecies.
     **/
    public int getSubspeciesIndex() {
        return this.getSubspecies().index;
    }

    /**
     * Gets the variant index of this mob.
     * 0 = Base Subspecies
     * 1/2 = Uncommon Species
     * 3+ = Rare Species
     * Most mobs have 2 uncommon variants, some have rare variants.
     **/
    public int getVariantIndex() {
        return this.getVariant() != null ? this.getVariant().index : 0;
    }

    /**
     * Returns true if this creature is a rare variant (and should act like a mini boss).
     *
     * @return True if rare.
     */
    public boolean isRareVariant() {
        return this.getVariant() != null && "rare".equals(this.getVariant().rarity);
    }

    /**
     * Scales the provided Beastiary Creature Knowledge Experience based on this creature's properties, config values, etc.
     *
     * @param knowledgeExperience The experience to apply a scale to.
     * @return The scaled experience.
     */
    public int scaleKnowledgeExperience(int knowledgeExperience) {
        if (this.isBoss()) {
            knowledgeExperience = Math.round((float) CreatureManager.getInstance().config.creatureBossKnowledgeScale * knowledgeExperience);
        } else if (this.getVariant() != null) {
            knowledgeExperience = Math.round((float) CreatureManager.getInstance().config.creatureVariantKnowledgeScale * knowledgeExperience);
        }
        return knowledgeExperience;
    }

    /**
     * The main update tick, all the important updates go here.
     **/
    @Override
    public void tick() {
        super.tick();
        if (this.creatureInfo.dummy) {
            return;
        }
        this.onSyncUpdate();

        Level world = this.getCommandSenderWorld();
        boolean isClient = world.isClientSide;

        if (this.despawnCheck()) {
            if (!this.isBoundPet() || this.isTemporary)
                this.inventory.dropInventory();
            this.remove(RemovalReason.DISCARDED);
        }

        if (this.boundPetOrphan && !this.hasPetEntry()) {
            this.discard();
            return;
        }

        // Fire Immunity Override:
        if (this.isOnFire() && !this.canBurn()) {
            this.clearFire();
        }

        // Not Walking On Land:
        if ((!this.canWalk() && !this.isFlying() && !this.isInWater() && this.isMoving()) || !this.canMove()) {
            this.clearMovement();
        }

        // Climbing/Flying:
        if (!isClient || this.isControlledByLocalInstance()) {
            this.setBesideClimbableBlock(this.horizontalCollision);
            if (!this.onGround() && this.flySoundSpeed > 0 && this.tickCount % 20 == 0)
                this.playFlySound();
        }
        if (!isClient && this.isFlying() && this.hasAttackTarget() && this.updateTick % 40 == 0) {
            this.leap(0, 0.4D);
        }

        // Perching:
        LivingEntity perchTarget = this.getPerchTarget();
        if (perchTarget != null) {
            ExtendedEntity perchEntityExt = ExtendedEntity.getForEntity(this.getPerchTarget());
            if (perchEntityExt != null) {
                Vector3d perchPosition = perchEntityExt.getPerchPosition();
                this.setPos(perchPosition.x, perchPosition.y, perchPosition.z);
                this.setDeltaMovement(perchTarget.getDeltaMovement());
                this.yRotO = perchTarget.yRotO;
            }
            if (perchTarget instanceof Player) {
                ExtendedPlayer perchPlayerExt = ExtendedPlayer.getForPlayer((Player) perchTarget);
                if (perchPlayerExt.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT)) {
                    this.perchOnEntity(null);
                }
            }
        }

        // Boss Health Update:
        if (!isClient && this.isBoss() && this.updateTick % 20 == 0 && this.playerTargets.isEmpty()) {
            this.heal(1);
        }
        if (this.bossInfo != null) {
            this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
        }

        // Beastiary Proximity Discovery:
        if (!isClient && this.updateTick % 20 == 0) {
            for (Player player : world.players()) {
                if (this.distanceToSqr(player) <= 10.0 * 10.0) {
                    ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
                    if (extendedPlayer != null) {
                        CreatureKnowledge creatureKnowledge = extendedPlayer.getBeastiary().getCreatureKnowledge(this.creatureInfo.getName());
                        if (creatureKnowledge == null || creatureKnowledge.rank < 1) {
                            extendedPlayer.studyCreature(this, CreatureManager.getInstance().config.creatureProximityKnowledge, false, false);
                        }
                    }
                }
            }
        }

        // GUI Refresh Tick:
        if (!isClient && this.guiViewers.size() <= 0) {
            this.guiRefreshTick = 0;
        }
        if (!isClient && this.guiRefreshTick > 0) {
            if (--this.guiRefreshTick <= 0) {
                this.refreshGUIViewers();
                this.guiRefreshTick = 0;
            }
        }
    }

    /**
     * Runs through all the AI tasks this mob has on the update, will update the flight navigator if this mob is using it too.
     **/
    @Override
    protected void customServerAiStep() {
        if (this.useDirectNavigator()) {
            directNavigator.updateFlight();
        }
        super.customServerAiStep();
    }

    /**
     * The living tick, behaviour and custom update logic should go here.
     **/
    @Override
    public void aiStep() {
        Level world = this.getCommandSenderWorld();
        boolean isClient = world.isClientSide;

        // Enforce Damage Limit:
        if (this.damageLimit > 0) {
            if (this.healthLastTick < 0)
                this.healthLastTick = this.getHealth();
            if (this.healthLastTick - this.getHealth() > this.damageLimit)
                this.setHealth(this.healthLastTick - this.damageLimit);
            this.healthLastTick = this.getHealth();
            if (!isClient && this.updateTick % 20 == 0) {
                this.damageTakenThisSec = 0;
            }
        }

        super.aiStep();

        if (this.creatureInfo.dummy) {
            return;
        }

        // Attack Updates:
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
            if (this.attackCooldown > this.getAttackCooldownMax()) {
                this.triggerAttackCooldown();
            }
        }
        this.updateBattlePhase();
        this.updateSwingTime();

        // Blocking Updates:
        if (this.currentBlockingTime > 0) {
            this.currentBlockingTime--;
        }
        if (this.currentBlockingTime < 0) {
            this.currentBlockingTime = 0;
        }

        // First Spawn:
        if (!isClient && this.firstSpawn) {
            this.onFirstSpawn();
        }

        // Fixate Target:
        if (!isClient && !this.hasFixateTarget() && this.fixateUUID != null) {
            if (world instanceof ServerLevel serverLevel) {
                Entity foundEntity = serverLevel.getEntity(this.fixateUUID);
                if (foundEntity instanceof LivingEntity livingTarget && foundEntity != this) {
                    this.setFixateTarget(livingTarget);
                }
            }
            this.fixateUUID = null;
        }
        if (this.hasFixateTarget()) {
            this.setTarget(this.getFixateTarget());
        }

        // Remove Creative Attack Target:
        if (this.hasAttackTarget()) {
            if (this.getTarget() instanceof Player) {
                Player targetPlayer = (Player) this.getTarget();
                if (targetPlayer.getAbilities().invulnerable)
                    this.setTarget(null);
            }
        }

        // Fleeing:
        if (this.hasAvoidTarget()) {
            if (this.currentFleeTime-- <= 0)
                this.setAvoidTarget(null);
        }

        // Gliding:
        if (!this.onGround() && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, this.getFallingMod(), 1));
        }

        float brightness = -1;
        if (!isClient) {
            brightness = this.getBrightness();
        }

        // Sunlight Damage:
        if (!isClient && this.daylightBurns() && world.isDay()) {
            if (brightness > 0.5F && this.random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F && world.canSeeSkyFromBelowWater(this.blockPosition())) {
                boolean shouldBurn = true;
                ItemStack helmet = this.inventory.getEquipmentStack("head");
                if (!helmet.isEmpty()) {
                    if (helmet.isDamageableItem()) {
                        helmet.setDamageValue(helmet.getDamageValue() + this.random.nextInt(2));
                        if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
                            this.setCurrentItemOrArmor(4, ItemStack.EMPTY);
                        }
                    }
                    shouldBurn = false;
                }
                if (shouldBurn) {
                    this.setSecondsOnFire(8);
                }
            }
        }

        // Water Damage:
        if (!isClient && this.waterDamage() && this.isInWaterOrRain() && !this.isInLava()) {
            this.hurt(this.level().damageSources().drown(), 1.0F);
        }

        // Suffocation:
        if (!isClient && this.isAlive() && !this.canBreatheAir()) {
            this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
            if (this.getAirSupply() <= -200) {
                this.setAirSupply(-160);
                this.hurt(this.level().damageSources().drown(), 1.0F);
            }
        }

        // Natural Despawn Light Scaling:
        if (!isClient) {
            if (!this.creatureInfo.creatureSpawn.spawnsInLight && brightness > 0.5F) {
                this.noActionTime += 2;
            } else if (!this.creatureInfo.creatureSpawn.spawnsInDark && brightness <= 0.5F) {
                this.noActionTime += 2;
            }
        }

        // Stealth Invisibility:
        if (!isClient) {
            if (this.isStealthed() && !this.isInvisible())
                this.setInvisible(true);
            else if (!this.isStealthed() && this.isInvisible() && !this.hasEffect(MobEffects.INVISIBILITY))
                this.setInvisible(false);
        }
        if (this.isStealthed()) {
            if (this.stealthPrev != this.isStealthed())
                this.startStealth();
            this.onStealth();
        } else if (this.isInvisible() && !this.hasEffect(MobEffects.INVISIBILITY) && !isClient) {
            this.setInvisible(false);
        }
        this.stealthPrev = this.isStealthed();

        // Pickup Items:
        if (this.tickCount % 20 == 0 && !isClient && this.isAlive() && this.canPickupItems())
            this.pickupItems();

        // Entity Pickups:
        if (!isClient && this.pickupEntity != null) {
            if (!this.pickupEntity.isAlive())
                this.dropPickupEntity();
            else if (this.distanceToSqr(this.pickupEntity) > 32D * 32D) {
                this.dropPickupEntity();
            }
        }

        // Boss:
        this.getBossInfo();
        if (this.isBossAlways()) {
            ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
            extendedWorld.bossUpdate(this);
        }

        // Check Minions:
        if (!this.minions.isEmpty()) {
            this.minions.removeIf(minion -> !minion.isAlive());
        }

        // Minion To Master Update:
        if (this.getMasterTarget() != null && this.getMasterTarget() instanceof BaseCreatureEntity)
            ((BaseCreatureEntity) this.getMasterTarget()).onMinionUpdate(this, this.updateTick);

        this.updateTick++;
    }

    public float getBrightness() {
        return LMHelperClass.getBrightness(this);
    }

    /**
     * An update that is called to sync things with the client and server such as various entity targets, attack phases, animations, etc.
     **/
    public void onSyncUpdate() {
        if (this.syncQueued) {
            this.doSync();
        }

        // Sync Target Status:
        if (!this.getCommandSenderWorld().isClientSide) {
            byte targets = 0;
            if (this.getTarget() != null)
                targets += TARGET_BITS.ATTACK.id;
            if (this.getMasterTarget() != null)
                targets += TARGET_BITS.MASTER.id;
            if (this.getParentTarget() != null)
                targets += TARGET_BITS.PARENT.id;
            if (this.getAvoidTarget() != null)
                targets += TARGET_BITS.AVOID.id;
            if (this.getControllingPassenger() != null)
                targets += TARGET_BITS.RIDER.id;
            if (this.getPickupEntity() != null)
                targets += TARGET_BITS.PICKUP.id;
            if (this.getPerchTarget() != null)
                targets += TARGET_BITS.PERCH.id;
            this.entityData.set(TARGET, targets);
        }

        // Attack Phase:
        if (!this.getCommandSenderWorld().isClientSide)
            this.entityData.set(ATTACK_PHASE, this.attackPhase);

        // Animations Server:
        if (!this.getCommandSenderWorld().isClientSide) {
            byte animations = 0;

            // Attack Cooldown:
            if (this.isAttackOnCooldown()) {
                animations += ANIMATION_STATE_BITS.ATTACKED.id;
            }

            // Airborne Animation:
            if (this.onGround())
                animations += ANIMATION_STATE_BITS.GROUNDED.id;

            // Swimming Animation:
            if (this.wasTouchingWater)
                animations += ANIMATION_STATE_BITS.IN_WATER.id;

            // Blocking Animation:
            if (this.isBlocking())
                animations += ANIMATION_STATE_BITS.BLOCKING.id;

            // Blocking Animation:
            if (this.isMinion())
                animations += ANIMATION_STATE_BITS.MINION.id;

            // Extra Animation 01:
            if (this.extraAnimation01())
                animations += ANIMATION_STATE_BITS.EXTRA01.id;

            // Spawned As Boss Animation:
            if (this.spawnedAsBoss)
                animations += ANIMATION_STATE_BITS.BOSS.id;

            this.entityData.set(ANIMATION_STATE, animations);
        }

        // Animations Client:
        else if (this.getCommandSenderWorld().isClientSide) {
            byte animationState = this.getByteFromDataManager(ANIMATION_STATE);
            if ((animationState & ANIMATION_STATE_BITS.ATTACKED.id) > 0) {
                if (!this.isAttackOnCooldown()) {
                    this.triggerAttackCooldown();
                }
            } else {
                this.resetAttackCooldown();
            }
            this.setOnGround((animationState & ANIMATION_STATE_BITS.GROUNDED.id) > 0);
            this.wasTouchingWater = (animationState & ANIMATION_STATE_BITS.IN_WATER.id) > 0;
            this.extraAnimation01 = (animationState & ANIMATION_STATE_BITS.EXTRA01.id) > 0;
            this.spawnedAsBoss = (animationState & ANIMATION_STATE_BITS.BOSS.id) > 0;
        }

        // Is Minion:
        if (this.getCommandSenderWorld().isClientSide) {
            this.isMinion = (this.getByteFromDataManager(ANIMATION_STATE) & ANIMATION_STATE_BITS.MINION.id) > 0;
        }

        // Subspecies:
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(SUBSPECIES, (byte) this.getSubspeciesIndex());
        } else {
            if (this.getSubspeciesIndex() != this.getByteFromDataManager(SUBSPECIES))
                this.setSubspecies(this.getByteFromDataManager(SUBSPECIES));
        }

        // Variant:
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(VARIANT, (byte) this.getVariantIndex());
        } else {
            if (this.getVariantIndex() != this.getByteFromDataManager(VARIANT))
                this.applyVariant(this.getByteFromDataManager(VARIANT));
        }

        // Size:
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(SIZE, (float) this.sizeScale);
        } else {
            if (this.sizeScale != this.getFloatFromDataManager(SIZE)) {
                this.setSizeScale(this.getFloatFromDataManager(SIZE));
            }
        }

        // Arena:
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(ARENA, this.getArenaCenter() != null ? Optional.of(this.getArenaCenter()) : Optional.empty());
        }
    }


    // ==================================================
    //                     Updates
    // ==================================================
    // ========== Main ==========

    // ==================================================
    //                     Movement
    // ==================================================
    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
    }

    // ========== AI ==========

    /**
     * Returns the importance of blocks when searching for random positions, also used when checking if this mob can spawn in locations via vanilla spawners.
     *
     * @param x The x position to check.
     * @param y The y position to check.
     * @param z The z position to check.
     * @return The importance where 0.0F is a standard path, anything higher is a preferred path and lower is a detrimental path.
     */
    public float getBlockPathWeight(int x, int y, int z) {
        if (this.creatureInfo.creatureSpawn.spawnsInDark && !this.creatureInfo.creatureSpawn.spawnsInLight)
            return 0.5F - this.getCommandSenderWorld().getBrightness(LightLayer.BLOCK, new BlockPos(x, y, z));
        if (this.creatureInfo.creatureSpawn.spawnsInLight && !this.creatureInfo.creatureSpawn.spawnsInDark)
            return this.getCommandSenderWorld().getBrightness(LightLayer.BLOCK, new BlockPos(x, y, z)) - 0.5F;
        return 0.0F;
    }

    // ========== Living ==========

    /**
     * Returns true if this entity should use a direct navigator with no pathing.
     * Used mainly for flying 'ghost' mobs that should fly through the terrain.
     */
    public boolean useDirectNavigator() {
        return false;
    }

    /**
     * Returns true if this entity should use swimming movement.
     */
    public boolean shouldSwim() {
        if (!this.isInWater() && !this.isInLava())
            return false;
        if (this.canWade() && this.canBreatheUnderwater()) {
            // If the target is not in water and this entity is at the water surface, don't use water movement:
            boolean targetInWater = true;
            if (this.getTarget() != null)
                targetInWater = this.getTarget().isInWater();
            else if (this.getParentTarget() != null)
                targetInWater = this.getParentTarget().isInWater();
            else if (this.getMasterTarget() != null)
                targetInWater = this.getMasterTarget().isInWater();
            if (!targetInWater) {
                BlockState blockState = this.getCommandSenderWorld().getBlockState(this.blockPosition().above());
                if (blockState.isAir()) {
                    return false;
                }
            }
            return true;
        }
        return this.isStrongSwimmer();
    }
    // ========== Sync Update ==========

    /**
     * Moves the entity, redirects to the direct navigator if this mob should use that instead.
     **/
    @Override
    public void travel(Vec3 direction) {
        if (this.useDirectNavigator()) {
            this.directNavigator.flightMovement(direction.x(), direction.z());
            this.updateLimbSwing();
            return;
        }

        if (this.shouldSwim()) {
            this.travelSwimming(direction);
        } else if (this.isFlying()) {
            this.travelFlying(direction);
        } else {
            super.travel(direction);
        }
    }

    public void travelFlying(Vec3 direction) {
        double flightDampening = 0.91F;
        if (this.onGround()) {
            BlockState groundState = this.getCommandSenderWorld().getBlockState(this.blockPosition().below());
            flightDampening = groundState.getFriction(this.getCommandSenderWorld(), this.blockPosition().below(), this) * 0.91F;
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().multiply(flightDampening, flightDampening, flightDampening));

        this.updateLimbSwing();
    }

    public void travelSwimming(Vec3 direction) {
        super.moveRelative(0.1F, direction);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        if (!this.isMoving() && this.getTarget() == null && !this.isFlying()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
        }

        this.updateLimbSwing();
    }

    /**
     * Updates limb swing animation, used when flying or swimming as their movements don't update it like the standard walking movement.
     **/
    public void updateLimbSwing() {
        this.calculateEntityAnimation(true);
        double distanceX = this.position().x() - this.xo;
        double distanceZ = this.position().z() - this.zo;
        float distance = LMHelperClass.convertToFloat(Math.sqrt(distanceX * distanceX + distanceZ * distanceZ) * 4.0F);
        if (distance > 1.0F) {
            distance = 1.0F;
        }
        this.walkAnimation.setSpeed((distance - this.walkAnimation.speed()) * 0.4F);
    }

    // ========== Should Swim ==========

    /**
     * Called when this entity is constructed for initial navigator.
     **/
    @Override
    protected PathNavigation createNavigation(Level world) {
        return new CreaturePathNavigator(this, world);
    }

    // ========== Move with Heading ==========

    /**
     * Called when this entity is constructed for initial move helper.
     **/
    protected MoveControl createMoveController() {
        return new CreatureMoveController(this);
    }

    /**
     * Returns the movement helper that this entity should use.
     **/
    public PathNavigation getNavigation() {
        return super.getNavigation();
    }

    /**
     * Returns the movement helper that this entity should use.
     **/
    public MoveControl getMoveControl() {
        return super.getMoveControl();
    }

    /**
     * Cuts off all movement for this update, will clear any pathfinder paths, works with the flight navigator too.
     **/
    public void clearMovement() {
        if (!this.useDirectNavigator() && this.getNavigation() != null)
            this.getNavigation().stop();
        else
            this.directNavigator.clearTargetPosition(1.0D);
    }

    // ========== Get New Navigator ==========

    /**
     * Can be overridden to add a random chance of looking around.
     **/
    public boolean rollLookChance() {
        return this.getRandom().nextFloat() < 0.02F;
    }

    // ========== Get New Move Helper ==========

    /**
     * Can be overridden to add a random chance of wandering around.
     **/
    public boolean rollWanderChance() {
        if (this.getBbWidth() >= 3) {
            return this.getRandom().nextDouble() <= 0.0005D;
        }
        return this.getRandom().nextDouble() <= 0.008D;
    }

    // ========== Get Navigator ==========

    /**
     * The leash update that manages all behaviour to do with the entity being leashed or unleashed.
     **/
    @Override
    protected void tickLeash() {
        super.tickLeash();
        if (this.isLeashed() && this.getLeashHolder().getCommandSenderWorld() == this.getCommandSenderWorld()) {
            Entity entity = this.getLeashHolder();
            this.setHome((int) entity.position().x(), (int) entity.position().y(), (int) entity.position().z(), 5);
            float distance = this.distanceTo(entity);
            this.testLeash(distance);

            if (!this.leashAIActive) {
                this.goalSelector.addGoal(2, this.leashMoveTowardsRestrictionAI); // Main Goals
                if (!this.isStrongSwimmer())
                    this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
                this.leashAIActive = true;
            }

            if (distance > 4.0F)
                this.getNavigation().moveTo(entity, 1.0D);

            if (distance > 6.0F) {
                double d0 = (entity.position().x() - this.position().x()) / (double) distance;
                double d1 = (entity.position().y() - this.position().y()) / (double) distance;
                double d2 = (entity.position().z() - this.position().z()) / (double) distance;
                this.setDeltaMovement(this.getDeltaMovement().add(d0 * Math.abs(d0) * 0.4D, d1 * Math.abs(d1) * 0.4D, d2 * Math.abs(d2) * 0.4D));
            }

            if (distance > 10.0F)
                this.dropLeash(true, true);
        } else if (!this.isLeashed() && this.leashAIActive) {
            this.leashAIActive = false;
            this.goalSelector.removeGoal(this.leashMoveTowardsRestrictionAI); // Main Goals
            if (!this.isStrongSwimmer())
                this.setPathfindingMalus(BlockPathTypes.WATER, BlockPathTypes.WATER.getMalus());
            this.hasRestriction();
        }
    }

    // ========== Get Move Helper ==========

    /**
     * ========== Pushed By Water ==========
     * Returns true if this mob should be pushed by water currents.
     * This will usually return false if the mob isStrongSwimmer()
     */
    @Override
    public boolean isPushedByFluid() {
        return !this.isStrongSwimmer() && !this.isBoss();
    }

    // ========== Clear Movement ==========

    /**
     * Returns true if this entity is moving towards a destination (doesn't check if this entity is being pushed, etc though).
     **/
    public boolean isMoving() {
        if (!this.useDirectNavigator())
            return this.getNavigation().getPath() != null;
        else
            return !this.directNavigator.atTargetPosition();
    }

    // ========== Can Be Pushed ==========
    @Override
    public boolean isPushable() {
        return super.isPushable();
    }

    /**
     * Returns whether or not this entity can be leashed to the specified player. Useful for tamed entites.
     **/
    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    // ========== Leash ==========

    /**
     * Called on the update to see if the leash should snap at the given distance.
     **/
    public void testLeash(float distance) {
    }

    /**
     * Used when setting the movement speed of this mob, called by AI classes before movement and is given a speed modifier, a local speed modifier is also applied here.
     **/
    @Override
    public void setSpeed(float speed) {
        super.setSpeed(speed * this.getAISpeedModifier());
    }

    // ========== Is Moving ==========

    /**
     * The local speed modifier of this mob, AI classes will also provide their own modifiers that will be multiplied by this modifier. To be used dynamically by various mob behaviours. Not to be confused with getSpeedMultiplier().
     **/
    public float getAISpeedModifier() {
        if (!this.canWalk() && !this.isInWater() && !this.isFlying()) {
            return 0.1F;
        }
        return 1.0F;
    }

    /**
     * Used to change the falling speed of this entity, 1.0D does nothing.
     **/
    public double getFallingMod() {
        return 1.0D;
    }

    // ========== Can Be Leashed To ==========

    /**
     * Modifies movement resistance in water.
     **/
    @Override
    protected float getWaterSlowDown() {
        if (!this.isPushedByFluid())
            return 1F;
        return 0.8F;
    }

    // ========== Test Leash ==========

    /**
     * When called, this entity will leap forwards with the given distance and height.
     * This is very sensitive, a large distance or height can cause the entity to zoom off for thousands of blocks!
     * A distance of 1.0D is around 10 blocks forwards, a height of 0.5D is about 10 blocks up.
     * Tip: Use a negative height for flying and swimming mobs so that they can swoop down in the air or water.
     **/
    public void leap(double distance, double leapHeight) {
        if (!this.isFlying()) {
            this.playJumpSound();
        }
        double yaw = this.yRotO;
        double pitch = this.xRotO;
        double angle = Math.toRadians(yaw);
        double xAmount = -Math.sin(angle);
        double yAmount = leapHeight;
        double zAmount = Math.cos(angle);
        if (this.isFlying()) {
            yAmount = Math.sin(Math.toRadians(pitch)) * distance + this.getDeltaMovement().y() * 0.2D;
        }
        this.push(
                xAmount * distance + this.getDeltaMovement().x() * 0.2D,
                yAmount,
                zAmount * distance + this.getDeltaMovement().z() * 0.2D
        );
        ForgeHooks.onLivingJump(this);
    }

    // ========== Set AI Speed ==========

    /**
     * When called, this entity will leap towards the given target entity with the given height.
     * This is very sensitive, a large distance or height can cause the entity to zoom off for thousands of blocks!
     * If the target distance is greater than range, the leap will be cancelled.
     * A distance of 1.0D is around 10 blocks forwards, a height of 0.5D is about 10 blocks up.
     * Tip: Use a negative height for flying and swimming mobs so that they can swoop down in the air or water
     **/
    public void leap(float range, double leapHeight, Entity target) {
        if (target == null)
            return;
        this.leap(range, leapHeight, target.blockPosition());
    }

    // ========== Movement Speed Modifier ==========

    /**
     * When called, this entity will leap towards the given target entity with the given height.
     * This is very sensitive, a large distance or height can cause the entity to zoom off for thousands of blocks!
     * If the target distance is greater than range, the leap will be cancelled.
     * A distance of 1.0D is around 10 blocks forwards, a height of 0.5D is about 10 blocks up.
     * Tip: Use a negative height for flying and swimming mobs so that they can swoop down in the air or water
     **/
    public void leap(float range, double leapHeight, BlockPos targetPos) {
        if (targetPos == null)
            return;
        if (!this.isFlying()) {
            this.playJumpSound();
        }
        double distance = targetPos.distSqr(this.blockPosition());
        if (distance > 2.0F * 2.0F && distance <= range * range) {
            double xDist = targetPos.getX() - this.blockPosition().getX();
            double zDist = targetPos.getZ() - this.blockPosition().getZ();
            if (xDist == 0) {
                xDist = 0.05D;
            }
            if (zDist == 0) {
                zDist = 0.05D;
            }
            double xzDist = Math.sqrt(xDist * xDist + zDist * zDist);
            /*this.motionX = xDist / xzDist * 0.5D * 0.8D + this.motionX * 0.2D;
            this.motionZ = zDist / xzDist * 0.5D * 0.8D + this.motionZ * 0.2D;
            this.motionY = leapHeight;*/
            this.push(
                    xDist / xzDist * 0.5D * 0.8D + this.getDeltaMovement().x() * 0.2D,
                    leapHeight,
                    zDist / xzDist * 0.5D * 0.8D + this.getDeltaMovement().z() * 0.2D
            );
            ForgeHooks.onLivingJump(this);
        }
    }

    // ========== Falling Speed Modifier ==========

    /**
     * When called, this entity will strafe sideways with the given distance and height.
     * This is very sensitive, a large distance or height can cause the entity to zoom off for thousands of blocks!
     * A distance of 1.0D is around 10 blocks sideways, a height of 0.5D is about 10 blocks up.
     * Tip: Use a negative height for flying and swimming mobs so that they can swoop down in the air or water.
     **/
    public void strafe(double distance, double leapHeight) {
        boolean opposite = false;
        if (distance < 0) {
            distance = -distance;
            opposite = true;
        }
        float yaw = this.yRotO + (opposite ? -90F : 90F);
        float pitch = this.xRotO;
        double angle = Math.toRadians(yaw);
        double xAmount = -Math.sin(angle);
        double yAmount = leapHeight;
        double zAmount = Math.cos(angle);
        if (this.isFlying()) {
            yAmount = Math.sin(Math.toRadians(pitch)) * distance + this.getDeltaMovement().y() * 0.2D;
        }
        this.push(
                xAmount * distance + this.getDeltaMovement().x() * 0.2D,
                yAmount,
                zAmount * distance + this.getDeltaMovement().z() * 0.2D
        );
    }

    // ========== Water Modifier ==========

    /**
     * Returns true if the target entity is looking at this entity.
     *
     * @param targetEntity The target entity to check.
     * @return True if the target is looking at this entity, additional checks are done for players.
     */
    public boolean isLookingAtMe(Entity targetEntity) {
        if (targetEntity == null) {
            return false;
        }
        Vec3 targetViewVector = targetEntity.getViewVector(1.0F).normalize();
        Vec3 distance = new Vec3(this.getX() - targetEntity.getX(), this.getEyeY() - targetEntity.getEyeY(), this.getZ() - targetEntity.getZ());
        double distanceStraight = distance.length();
        distance = distance.normalize();
        double lookDistance = targetViewVector.dot(distance);
        double lookRange = 1.5D;
        double comparison = 1.0D - (lookRange / distanceStraight);
        if (targetEntity instanceof Player) {
            return lookDistance > comparison && ((Player) targetEntity).hasLineOfSight(this);
        }
        return lookDistance > comparison;
    }

    // ========== Leap ==========

    /**
     * Sets the home position for this entity to stay around and the distance it is allowed to stray from.
     **/
    public void setHome(int x, int y, int z, float distance) {
        this.setHomePosition(x, y, z);
        this.setHomeDistanceMax(distance);
    }

    // ========== Leap to Target ==========

    /**
     * Sets the home position for this entity to stay around.
     **/
    public void setHomePosition(int x, int y, int z) {
        this.homePosition = new BlockPos(x, y, z);
    }

    /**
     * Returns the home position in BlockPos.
     **/
    @Override
    public BlockPos getRestrictCenter() {
        return this.homePosition;
    }

    // ========== Strafe ==========

    /**
     * Gets the distance this mob is allowed to stray from it's home. -1 is used to unlimited distance.
     **/
    public float getHomeDistanceMax() {
        return this.homeDistanceMax;
    }

    /**
     * Sets the distance this mob is allowed to stray from it's home. -1 will turn off the home restriction.
     **/
    public void setHomeDistanceMax(float newDist) {
        this.homeDistanceMax = newDist;
    }


    // ==================================================
    //                     Positions
    // ==================================================
    // ========== Home ==========

    /**
     * Clears the current home position. Returns true if a home was detached.
     **/
    public boolean hasRestriction() {
        if (this.hasHome())
            return false;
        this.setHomeDistanceMax(-1);
        return true;
    }

    /**
     * Returns whether or not this mob has a home set.
     **/
    public boolean hasHome() {
        return this.getRestrictCenter() != null && this.getHomeDistanceMax() >= 0;
    }

    /**
     * Returns whether or not the given XYZ position is near this entity's home position, returns true if no home is set.
     **/
    public boolean positionNearHome(int x, int y, int z) {
        if (!hasHome()) return true;
        return this.getDistanceFromHome(x, y, z) < this.getHomeDistanceMax();
    }

    /**
     * Returns the distance that the specified XYZ position is from the home position.
     **/
    public double getDistanceFromHome(int x, int y, int z) {
        if (!hasHome()) return 0;
        return Math.sqrt(this.homePosition.distSqr(new Vec3i(x, y, z)));
    }

    /**
     * Returns the distance that the entity's position is from the home position.
     **/
    public double getDistanceFromHome() {
        return Math.sqrt(this.homePosition.distSqr(this.blockPosition()));
    }

    /**
     * Returns true if this mob was spawned by an arena and has been set an arena center (typically used arena-based movement by bosses, etc).
     **/
    public boolean hasArenaCenter() {
        return this.getArenaCenter() != null;
    }

    /**
     * Returns the central arena position that this mob is using or null if not set.
     **/
    public BlockPos getArenaCenter() {
        return this.arenaCenter;
    }

    /**
     * Sets the central arena point for this mob to use.
     **/
    public void setArenaCenter(BlockPos pos) {
        this.arenaCenter = pos;
    }

    /**
     * Takes an initial chunk coordinate for a random wander position and then allows the entity to make changes to the position or react to it.
     **/
    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        return wanderPosition;
    }

    /**
     * Takes an initial coordinate and returns an altered Y position relative to the ground using a minimum and maximum distance.
     **/
    public int restrictYHeightFromGround(BlockPos coords, int minY, int maxY) {
        int groundY = this.getGroundY(coords);
        int airYMax = Math.min(this.getAirY(coords), groundY + maxY);
        int airYMin = Math.min(airYMax, groundY + minY);
        if (airYMin >= airYMax)
            return airYMin;
        return airYMin + this.getRandom().nextInt(airYMax - airYMin);
    }

    // ========== Arena Center ==========

    /**
     * Returns the Y position of the ground from the starting X, Y, Z position, this will work for getting the ground of caves or indoor areas too.
     * The Y position returned will be the last air block found before the ground it hit and will thus not be the ground block Y position itself but the air above it.
     **/
    public int getGroundY(BlockPos pos) {
        int y = pos.getY();
        if (y <= 0)
            return 0;
        BlockState startBlock = this.getCommandSenderWorld().getBlockState(pos);
        if (startBlock.isAir()) {
            for (int possibleGroundY = Math.max(0, y - 1); possibleGroundY >= 0; possibleGroundY--) {
                BlockState possibleGroundBlock = this.getCommandSenderWorld().getBlockState(new BlockPos(pos.getX(), possibleGroundY, pos.getZ()));
                if (possibleGroundBlock.isAir())
                    y = possibleGroundY;
                else
                    break;
            }
        }
        return y;
    }

    /**
     * Returns the Y position of the highest air block from the starting x, y, z position until either a solid block is hit or the sky is accessible.
     **/
    public int getAirY(BlockPos pos) {
        int y = pos.getY();
        int yMax = this.getCommandSenderWorld().getMaxBuildHeight() - 1;
        if (y >= yMax)
            return yMax;
        if (this.getCommandSenderWorld().canSeeSkyFromBelowWater(pos))
            return yMax;

        BlockState startBlock = this.getCommandSenderWorld().getBlockState(pos);
        if (startBlock.isAir()) {
            for (int possibleAirY = Math.min(yMax, y + 1); possibleAirY <= yMax; possibleAirY++) {
                BlockState possibleGroundBlock = this.getCommandSenderWorld().getBlockState(new BlockPos(pos.getX(), possibleAirY, pos.getZ()));
                if (possibleGroundBlock.isAir())
                    y = possibleAirY;
                else
                    break;
            }
        }
        return y;
    }

    /**
     * Returns the Y position of the water surface (the first air block found when searching up in water).
     * If the water is covered by a solid block, the highest Y water position will be returned instead.
     * This will search up to 24 blocks up.
     **/
    public int getWaterSurfaceY(BlockPos pos) {
        int y = pos.getY();
        if (y <= 0)
            return 0;
        int yMax = this.getCommandSenderWorld().getMaxBuildHeight() - 1;
        if (y >= yMax)
            return yMax;
        int yLimit = 24;
        yMax = Math.min(yMax, y + yLimit);
        BlockState startBlock = this.getCommandSenderWorld().getBlockState(pos);
        if (startBlock.getBlock() == Blocks.WATER) {
            int possibleSurfaceY = y;
            for (possibleSurfaceY += 1; possibleSurfaceY <= yMax; possibleSurfaceY++) {
                BlockState possibleSurfaceBlock = this.getCommandSenderWorld().getBlockState(new BlockPos(pos.getX(), possibleSurfaceY, pos.getZ()));
                if (possibleSurfaceBlock.isAir())
                    return possibleSurfaceY;
                else if (possibleSurfaceBlock.getBlock() != Blocks.WATER)
                    return possibleSurfaceY - 1;
            }
            return Math.max(possibleSurfaceY - 1, y);
        }
        return y;
    }

    // ========== Get Wander Position ==========

    /**
     * Returns whether or not this mob is allowed to attack the given target class.
     **/
    @Override
    public boolean canAttackType(EntityType<?> entityType) {
        return true;
    }

    // ========== Restrict Y Height From Ground ==========

    /**
     * Returns whether or not this mob is allowed to attack the given target entity.
     **/
    public boolean canAttack(LivingEntity targetEntity) {
        if (this.getCommandSenderWorld().getDifficulty() == Difficulty.PEACEFUL && targetEntity instanceof Player) {
            return false;
        }

        if (!Targeting.isValidTarget(this, targetEntity)) {
            return false;
        }

        // Players:
        if (targetEntity instanceof Player) {
            Player targetPlayer = (Player) targetEntity;
            if (targetPlayer.getAbilities().invulnerable) {
                return false;
            }
        }

        // Team:
        if (this.isAlliedTo(targetEntity)) {
            return false;
        }

        // Relationships:
        CreatureRelationshipEntry relationshipEntry = this.relationships.getEntry(targetEntity);
        if (relationshipEntry != null && !relationshipEntry.canAttack()) {
            return false;
        }

        // Creatures:
        if (targetEntity instanceof BaseCreatureEntity) {
            BaseCreatureEntity targetCreature = (BaseCreatureEntity) targetEntity;

            // Same Species, Target Not Tamed: (Can't compare owners here)
            if (!this.canAttackOwnSpecies() && targetCreature.creatureInfo == this.creatureInfo && !targetCreature.isTamed()) {
                return false;
            }

            // Master:
            if (targetCreature.getMasterTarget() == this) {
                return false;
            }

            // Bosses and Rares:
            if (!(this instanceof IGroupBoss)) {
                if (!this.isTamed()) {
                    if (targetCreature.isBoss()) {
                        return false;
                    }
                    if (this.isRareVariant()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // ========== Get Ground Y Position ==========

    /**
     * Determines if this creature can attack other creatures that are the same species as it.
     *
     * @return True if this creature can attack its own species (other conditions are checked elsewhere).
     */
    public boolean canAttackOwnSpecies() {
        return false;
    }

    // ========== Get Air Y Position ==========

    /**
     * Returns the melee attack range of this creature.
     *
     * @return The attack range.
     */
    public double getPhysicalRange() {
        double range = this.getDimensions(Pose.STANDING).width + 1.5D;
        if (this.isFlying()) {
            range += this.getFlightOffset();
        }
        return range * range;
    }

    // ========== Get Water Surface Y Position ==========

    /**
     * Returns the required attack range.
     *
     * @param attackTarget    The entity to attack.
     * @param additionalReach Extra attack range.
     * @return The maximum attack range.
     */
    public double getMeleeAttackRange(LivingEntity attackTarget, double additionalReach) {
        double creatureRange = this.getPhysicalRange();
        double targetSize = 1;
        if (attackTarget != null) {
            targetSize = (attackTarget.getDimensions(Pose.STANDING).width + 1) * (attackTarget.getDimensions(Pose.STANDING).width + 1);
        }
        return creatureRange + targetSize + additionalReach;
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Can Attack ==========

    public boolean shouldCreatureGroupRevenge(LivingEntity target) {
        boolean shouldRevenge = this.creatureInfo.getGroups().isEmpty();
        boolean shouldPackHunt = false;
        for (CreatureGroup group : this.creatureInfo.getGroups()) {
            if (group.shouldRevenge(target)) {
                shouldRevenge = true;
            }
            if (group.shouldPackHunt(target)) {
                shouldPackHunt = true;
            }
        }
        boolean canPackHunt = shouldPackHunt && this.isInPack();
        return shouldRevenge || canPackHunt;
    }

    public boolean shouldCreatureGroupHunt(LivingEntity target) {
        boolean shouldFlee = false;
        boolean shouldHunt = false;
        boolean shouldPackHunt = false;
        for (CreatureGroup group : this.creatureInfo.getGroups()) {
            if (group.shouldFlee(target)) {
                shouldFlee = true;
            }
            if (group.shouldHunt(target)) {
                shouldHunt = true;
            }
            if (group.shouldPackHunt(target)) {
                shouldPackHunt = true;
            }
        }
        boolean canPackHunt = shouldPackHunt && this.isInPack();
        if (shouldFlee && !canPackHunt) {
            return false;
        }
        return shouldHunt || shouldPackHunt;
    }

    public boolean shouldCreatureGroupFlee(LivingEntity target) {
        if (this.isBoss() || this.isRareVariant() || this.isTamed()) {
            return false;
        }
        boolean shouldFlee = false;
        boolean shouldPackHunt = false;
        for (CreatureGroup group : this.creatureInfo.getGroups()) {
            if (group.shouldFlee(target)) {
                shouldFlee = true;
            }
            if (group.shouldPackHunt(target)) {
                shouldPackHunt = true;
            }
        }
        boolean canPackHunt = shouldPackHunt && this.isInPack();
        return shouldFlee && !canPackHunt;
    }

    /**
     * Gets the attack target of this entity's Master Target Entity.
     **/
    public LivingEntity getMasterAttackTarget() {
        if (this.masterTarget == null)
            return null;
        if (this.masterTarget instanceof Mob)
            return ((Mob) this.masterTarget).getTarget();
        return null;
    }

    /**
     * Gets the attack target of this entity's Parent Target Entity.
     **/
    public LivingEntity getParentAttackTarget() {
        if (this.parentTarget == null)
            return null;
        if (this.parentTarget instanceof Mob)
            return ((Mob) this.parentTarget).getTarget();
        return null;
    }

    /**
     * Used to make this entity perform a melee attack on the target entity with the given damage scale.
     **/
    public boolean attackMelee(Entity target, double damageScale) {
        if (this.isBlocking() && !this.canAttackWhileBlocking()) {
            return false;
        }

        if (this.attackEntityAsMob(target, damageScale)) {

            // Apply Melee Damage Effects If Not Blocked:
            if (target instanceof LivingEntity livingTarget) {
                if (!(livingTarget.isBlocking() && livingTarget.getUseItem().getItem() instanceof ShieldItem)) {
                    // Spread Fire:
                    if (this.spreadFire && this.isOnFire() && this.random.nextFloat() < this.creatureStats.getEffect())
                        target.setSecondsOnFire(this.getEffectDuration(4) / 20);

                    // Element Effects:
                    if (this.creatureStats.getAmplifier() >= 0) {
                        this.applyDebuffs(livingTarget, 1, 1);
                    }
                }
            }

            this.triggerAttackCooldown();
            this.playAttackSound();
            return true;
        }

        return false;
    }

    /**
     * Used to make this entity perform a hitscan attack on the target entity with the given damage scale.
     **/
    public boolean attackHitscan(Entity target, double damageScale) {
        if (this.isBlocking() && !this.canAttackWhileBlocking()) {
            return false;
        }
        if (target == null || !this.hasLineOfSight(target)) {
            return false;
        }

        if (this.attackEntityAsMob(target, damageScale)) {

            // Apply Damage Effects If Not Blocked:
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (!(livingTarget.isBlocking() && livingTarget.getUseItem().getItem() instanceof ShieldItem)) {
                    // Element Effects:
                    if (this.creatureStats.getAmplifier() >= 0) {
                        this.applyDebuffs(livingTarget, 1, 1);
                    }
                }
            }

            this.triggerAttackCooldown();
            this.playAttackSound();
            return true;
        }

        return false;
    }

    /**
     * Used to make this entity fire a ranged attack at the target entity, range is also passed which can be used.
     **/
    public void attackRanged(Entity target, float range) {
        if (this.isBlocking() && !this.canAttackWhileBlocking()) {
            return;
        }

        this.triggerAttackCooldown();
        this.playAttackSound();
    }

    // ========== Targets ==========

    /**
     * Deals damage to target entity from a projectile fired by this entity.
     *
     * @param target     The target entity to damage.
     * @param projectile The projectile that caused the damage.
     * @param damage     The amount of damage the projectile deals. This will be scaled by this creature's damage stat.
     * @param noPierce   If true, this creature's piercing stat will be ignored, used for when blocked by a shield, etc.
     * @return True if damage is dealt.
     */
    public boolean doRangedDamage(Entity target, ThrowableProjectile projectile, float damage, boolean noPierce) {
        damage *= this.creatureStats.getDamage() / 2;
        double pierceDamage = noPierce ? 0 : this.creatureStats.getPierce();

        boolean success;
        if (damage <= pierceDamage) {
            success = target.hurt(this.getDamageSource(target.level().damageSources().thrown(projectile, this)), damage);
        } else {
            int hurtResistantTimeBefore = target.invulnerableTime;
            if (pierceDamage > 0) {
                target.hurt(this.getDamageSource(target.level().damageSources().thrown(projectile, this)), (float) pierceDamage);
            }
            target.invulnerableTime = hurtResistantTimeBefore;
            damage -= pierceDamage;
            success = target.hurt(this.getDamageSource(target.level().damageSources().thrown(projectile, this)), damage);
        }

        // Element Effects:
        if (success && target instanceof LivingEntity && this.creatureStats.getAmplifier() >= 0) {
            this.applyDebuffs((LivingEntity) target, 1, 1);
        }

        return success;
    }

    /**
     * Fires a projectile from this mob.
     *
     * @param projectileName The name of the projectile to fire from a Projectile Info.
     * @param target         The target entity to fire at. If null, the projectile is fired from the facing direction instead.
     * @param range          The range to the target.
     * @param angle          The angle offset away from the target in degrees.
     * @param offset         The xyz offset to fire from. Note that the Y offset is relative to 75% of this mob's height.
     * @param velocity       The velocity of the projectile.
     * @param scale          The size scale of the projectile.
     * @param inaccuracy     How inaccurate the projectile aiming is.
     * @return The newly created projectile.
     */
    public BaseProjectileEntity fireProjectile(String projectileName, Entity target, float range, float angle, Vector3d offset, float velocity, float scale, float inaccuracy) {
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(projectileName);
        if (projectileInfo == null) {
            return null;
        }
        return this.fireProjectile(projectileInfo.createProjectile(this.getCommandSenderWorld(), this), target, range, angle, offset, velocity, scale, inaccuracy);
    }

    // ========== Melee ==========

    /**
     * Fires a projectile from this mob.
     *
     * @param projectileClass The class of the projectile. Must extend EntityProjectileBase.
     * @param target          The target entity to fire at. If null, the projectile is fired from the facing direction instead.
     * @param range           The range to the target.
     * @param angle           The angle offset away from the target in degrees.
     * @param offset          The xyz offset to fire from. Note that the Y offset is relative to 75% of this mob's height.
     * @param velocity        The velocity of the projectile.
     * @param scale           The size scale of the projectile.
     * @param inaccuracy      How inaccurate the projectile aiming is.
     * @return The newly created projectile.
     */
    public BaseProjectileEntity fireProjectile(Class<? extends BaseProjectileEntity> projectileClass, Entity target, float range, float angle, Vector3d offset, float velocity, float scale, float inaccuracy) {
        BaseProjectileEntity projectile = ProjectileManager.getInstance().createOldProjectile(projectileClass, this.getCommandSenderWorld(), this);
        return this.fireProjectile(projectile, target, range, angle, offset, velocity, scale, inaccuracy);
    }

    // ========== Hitscan ==========

    /**
     * Fires a projectile from this mob.
     *
     * @param projectile The projectile instance being fired.
     * @param target     The target entity to fire at. If null, the projectile is fired from the facing direction instead.
     * @param range      The range to the target.
     * @param angle      The angle offset away from the target in degrees.
     * @param offset     The xyz offset to fire from. Note that the Y offset is relative to 75% of this mob's height.
     * @param velocity   The velocity of the projectile.
     * @param scale      The size scale of the projectile.
     * @param inaccuracy How inaccurate the projectile aiming is.
     * @return The fired created projectile.
     */
    public BaseProjectileEntity fireProjectile(BaseProjectileEntity projectile, Entity target, float range, float angle, Vector3d offset, float velocity, float scale, float inaccuracy) {
        if (projectile == null) {
            return null;
        }

        projectile.setPos(
                projectile.position().x() + offset.x * this.sizeScale,
                projectile.position().y() + (this.getDimensions(Pose.STANDING).height / 2) + (offset.y * this.sizeScale),
                projectile.position().z() + offset.z * this.sizeScale
        );
        projectile.setProjectileScale(scale);

        Vector3d facing = this.getFacingPositionDouble(this.position().x(), this.position().y(), this.position().z(), range, angle);
        double distanceX = facing.x - this.position().x();
        double distanceZ = facing.z - this.position().z();
        double distanceXZ = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ) * 0.1D;
        double distanceY = distanceXZ;
        if (target != null) {
            double targetX = target.position().x() - this.position().x();
            double targetZ = target.position().z() - this.position().z();
            double newX = targetX * Math.cos(angle) - targetZ * Math.sin(angle);
            double newY = targetX * Math.sin(angle) + targetZ * Math.cos(angle);
            targetX = newX + this.position().x();
            targetZ = newY + this.position().z();

            distanceX = targetX - this.position().x();
            distanceY = target.getBoundingBox().minY + (target.getDimensions(Pose.STANDING).height * 0.5D) - projectile.position().y() + offset.y;
            distanceZ = targetZ - this.position().z();
        }

        projectile.shoot(distanceX, distanceY, distanceZ, velocity, inaccuracy);
        this.getCommandSenderWorld().addFreshEntity(projectile);

        if (projectile.getLaunchSound() != null) {
            this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        }

        return projectile;
    }

    // ========== Ranged ==========

    /**
     * Returns the current attack phase of this mob, used when deciding which attack to use and which animations to use.
     **/
    public byte getAttackPhase() {
        return this.getByteFromDataManager(ATTACK_PHASE);
    }

    /**
     * Sets the current attack phase of this mobs.
     **/
    public void setAttackPhase(byte setAttackPhase) {
        attackPhase = setAttackPhase;
    }

    /**
     * Moves the attack phase to the next step, will loop back to 0 when the max is passed.
     **/
    public void nextAttackPhase() {
        if (++this.attackPhase > (this.attackPhaseMax - 1)) {
            this.attackPhase = 0;
        }
    }

    /**
     * Called when attacking and makes this entity actually deal damage to the target entity. Not used by projectile based attacks.
     **/
    public boolean attackEntityAsMob(Entity target, double damageScale) {
        if (!this.isAlive())
            return false;
        if (target == null)
            return false;
        if (!this.hasLineOfSight(target))
            return false;

        float damage = this.getAttackDamage(damageScale);

        // TODO Enchanted Weapon Damage and Knockback
        int enchantmentKnockback = 0;
        //if(target instanceof LivingEntity) {
        //damage += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), this.getAttribute(Attributes.ATTACK_DAMAGE));
        //i += EnchantmentHelper.getKnockbackModifier(this, (LivingEntity)target);
        //}

        // Player Shielding:
        boolean targetIsShielding = false;
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            targetIsShielding = targetPlayer.isBlocking() && targetPlayer.getUseItem().getItem() instanceof ShieldItem;
        }

        // Attempt The Attack:
        boolean attackSuccess;
        double pierceDamage = this.creatureStats.getPierce();
        if (targetIsShielding) {
            pierceDamage = 0;
        }
        if (damage <= pierceDamage) {
            attackSuccess = target.hurt(this.getDamageSource(null), damage);
        } else {
            if (pierceDamage > 0) {
                int hurtResistantTimeBefore = target.invulnerableTime;
                target.hurt(this.getDamageSource(null), (float) pierceDamage);
                target.invulnerableTime = hurtResistantTimeBefore;
                damage -= pierceDamage;
            }
            attackSuccess = target.hurt(this.getDamageSource(null), damage);
        }

        // After Successful Attack:
        if (attackSuccess) {
            if (enchantmentKnockback > 0) {
                target.push((double) (-Math.sin(this.yRotO * (float) Math.PI / 180.0F) * (float) enchantmentKnockback * 0.5F), 0.1D, (double) (Math.cos(this.yRotO * (float) Math.PI / 180.0F) * (float) enchantmentKnockback * 0.5F));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1, 0.6D));
            }

            // Fire Enchanted Held Weapons:
            int fireEnchantDuration = EnchantmentHelper.getFireAspect(this);
            if (fireEnchantDuration > 0)
                target.setSecondsOnFire(fireEnchantDuration * 4);

            // Interrupt Shielding Players:
            if (target instanceof Player && this.canInteruptShields(true)) {
                Player targetPlayer = (Player) target;
                if (targetIsShielding && this.canInteruptShields(false)) {
                    ItemStack playerActiveItemStack = targetPlayer.isUsingItem() ? targetPlayer.getUseItem() : ItemStack.EMPTY;
                    targetPlayer.getCooldowns().addCooldown(playerActiveItemStack.getItem(), 100);
                    this.level().broadcastEntityEvent(targetPlayer, (byte) 30);
                }
            }
        }

        return attackSuccess;
    }

    /**
     * Based on isDamageSourceBlocked which is private because of course it is...
     *
     * @param target       The entity to check.
     * @param damageSource The damage source.
     * @return True if the damage can be blocked.
     */
    public boolean canTargetBlockDamageSource(LivingEntity target, DamageSource damageSource) {
        Entity entity = damageSource.getDirectEntity();
        boolean arrowPierce = false;
        if (entity instanceof AbstractArrow) {
            AbstractArrow abstractarrowentity = (AbstractArrow) entity;
            if (abstractarrowentity.getPierceLevel() > 0) {
                arrowPierce = true;
            }
        }
        if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR) && target.isBlocking() && !arrowPierce) {
            Vec3 vector3d2 = damageSource.getSourcePosition();
            if (vector3d2 != null) {
                Vec3 vector3d = target.getViewVector(1.0F);
                Vec3 vector3d1 = vector3d2.vectorTo(target.position()).normalize();
                vector3d1 = new Vec3(vector3d1.x, 0.0D, vector3d1.z);
                if (vector3d1.dot(vector3d) < 0.0D) {
                    return true;
                }
            }
        }
        return false;
    }

    // ========== Phase ==========

    /**
     * Returns how much attack damage this mob does.
     **/
    public float getAttackDamage(double damageScale) {
        float damage = (float) this.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
        damage *= damageScale;
        return damage;
    }

    /**
     * Returns the damage source to be used by this mob when dealing damage.
     *
     * @param nestedDamageSource This can be null or can be a passed damage source for all kinds of use, mainly for minion damage sources. This will override the damage source for EntityBase+Ageable.
     * @return The damage source to use.
     */
    public DamageSource getDamageSource(DamageSource nestedDamageSource) {
        if (nestedDamageSource != null)
            return nestedDamageSource;
        return this.level().damageSources().mobAttack(this);
    }

    /**
     * Returns true if this mob can interrupt players when they are using their shield.
     *
     * @param checkAbility If true, this is just to check if this mob is capable of blocking shields. If false, this will return if this mob should actually interrupt a shield (do random rolls here).
     * @return True to interrupt shielding.
     */
    public boolean canInteruptShields(boolean checkAbility) {
        return false;
    }

    // ========== Deal Damage ==========

    // ==================================================
    //                    Taking Damage
    // ==================================================
    @Override
    protected void actuallyHurt(DamageSource damageSrc, float damageAmount) {
        if (this.isInvulnerableTo(damageSrc)) {
            return;
        }
        damageAmount = ForgeHooks.onLivingHurt(this, damageSrc, damageAmount);
        if (damageAmount <= 0)
            return;

        // Modifiers:
        damageAmount *= this.getDamageModifier(damageSrc);
        damageAmount = this.getDamageAfterArmorAbsorb(damageSrc, damageAmount);
        damageAmount = this.getDamageAfterMagicAbsorb(damageSrc, damageAmount);
        damageAmount = this.getDamageAfterDefense(damageAmount);
        if (this.isBoss() || this.isRareVariant()) {
            if (!(damageSrc.getEntity() instanceof Player))
                damageAmount *= 0.25F;
        }

        // Absorption:
        float damageBeforeAbsorption = damageAmount;
        damageAmount = Math.max(damageAmount - this.getAbsorptionAmount(), 0.0F);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (damageBeforeAbsorption - damageAmount));
        float absorbedDamage = damageBeforeAbsorption - damageAmount;
        if (absorbedDamage > 0.0F && absorbedDamage < 3.4028235E37F && damageSrc.getEntity() instanceof ServerPlayer) {
            ((ServerPlayer) damageSrc.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(absorbedDamage * 10.0F));
        }

        damageAmount = ForgeHooks.onLivingDamage(this, damageSrc, damageAmount);
        if (damageAmount != 0.0F) {
            float healthBeforeDamage = this.getHealth();
            this.getCombatTracker().recordDamage(damageSrc, damageAmount);
            this.setHealth(healthBeforeDamage - damageAmount);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - damageAmount);
        }
    }

    /**
     * Called when this entity has been attacked, uses a DamageSource and damage value.
     **/
    @Override
    public boolean hurt(DamageSource damageSrc, float damageAmount) {
        if (this.getCommandSenderWorld().isClientSide)
            return false;
        if (this.isInvulnerableTo(damageSrc))
            return false;
        if (this.dropsRequirePlayerDamage && damageSrc.getEntity() instanceof Player) {
            this.dropsRequirePlayerDamage = false;
        }
        if (super.hurt(damageSrc, damageAmount)) {
            this.onDamage(damageSrc, damageAmount);

            if (this.isBoss() && this.playerTargets != null && damageSrc.getEntity() != null && damageSrc.getEntity() instanceof Player) {
                if (!this.playerTargets.contains(damageSrc.getEntity())) {
                    this.playerTargets.add((Player) damageSrc.getEntity());
                }
            }

            Entity entity = damageSrc.getDirectEntity();
            if (entity instanceof ThrowableProjectile) {
                entity = ((ThrowableProjectile) entity).getOwner();
            }

            // Damaged by Living Entity:
            if (entity instanceof LivingEntity && this.getRider() != entity && this.getVehicle() != entity) {
                if (entity != this) {
                    this.setLastHurtByMob((LivingEntity) entity);

                    // Decrease Reputation:
                    CreatureRelationshipEntry relationshipEntry = this.relationships.getOrCreateEntry(entity);
                    int reputationAmount = 50 + this.getRandom().nextInt(50);
                    relationshipEntry.decreaseReputation(reputationAmount);
                }
            }
            return true;
        }
        return false;
    }

    // ========== Get Attack Damage ==========

    /**
     * This is provided with how much damage this mob will take and returns the reduced (or sometimes increased) damage with defense applied. Note: Damage Modifiers are applied after this. This also applies the blocking ability.
     **/
    public float getDamageAfterDefense(float damage) {
        float defense = (float) this.creatureStats.getDefense();
        float minDamage = 0F;
        if (this.isBlocking()) {
            if (defense <= 0)
                defense = 1;
            defense *= this.getBlockingMultiplier();
        }
        damage = Math.max(damage - defense, 1);
        if (this.damageMax > 0)
            damage = Math.min(damage, this.damageMax);
        return Math.max(damage, minDamage);
    }

    // ========= Get Damage Source ==========

    /**
     * Called when this mob has received damage.
     **/
    public void onDamage(DamageSource damageSrc, float damage) {
        this.damageTakenThisSec += damage;
    }

    /**
     * A multiplier that alters how much damage this mob receives from the given DamageSource, use for resistances and weaknesses. Note: The defense multiplier is handled before this.
     **/
    public float getDamageModifier(DamageSource damageSrc) {
        return 1.0F;
    }

    /**
     * Called when this entity dies, drops items from the inventory.
     **/
    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.dead)
            return;

        if (this.isBossAlways()) {
            ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(this.getCommandSenderWorld());
            extendedWorld.bossRemoved(this);
        }

        if (!this.getCommandSenderWorld().isClientSide) {
            if (!this.isBoundPet())
                this.inventory.dropInventory();
            if (damageSource.getEntity() != null) {
                if (damageSource.getEntity() instanceof Player) {
                    try {
                        Player player = (Player) damageSource.getEntity();
                        //player.addStat(ObjectManager.getStat(this.creatureInfo.getName() + ".kill"), 1); TODO Player Stats
                        if (!this.isTamed()) {
                            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
                            if (extendedPlayer != null) {
                                extendedPlayer.studyCreature(this, CreatureManager.getInstance().config.creatureKillKnowledge, false, false);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        if (this.getMasterTarget() != null && this.getMasterTarget() instanceof BaseCreatureEntity)
            ((BaseCreatureEntity) this.getMasterTarget()).onMinionDeath(this, damageSource);
    }

    // ========== Attacked From ==========

    /**
     * Returns true if this creature should attack it's attack targets. Used mostly by attack AIs and update methods.
     **/
    public boolean isAggressive() {
        if (this.extraMobBehaviour != null)
            if (this.extraMobBehaviour.aggressiveOverride)
                return true;
        return this.isAggressiveByDefault;
    }

    // ========== Defense ==========

    /**
     * Returns true if this creature is hostile to the provided entity.
     **/
    public boolean isHostileTo(Entity target) {
        if (target == null) {
            return false;
        }
        if (this.hostileTargets.contains(target.getType())) {
            return true;
        }
        if (this.hostileTargetClasses.contains(target.getClass())) {
            return true;
        }
        for (CreatureGroup group : this.creatureInfo.getGroups()) {
            if (group.shouldHunt(target) || group.shouldPackHunt(target)) {
                return true;
            }
        }
        return false;
    }

    // ========== On Damage ==========

    /**
     * Marks this creature as hostile towards the provided entity type.
     **/
    public void setHostileTo(EntityType targetType) {
        if (this.hostileTargets == null)
            this.hostileTargets = new ArrayList<>();
        if (this.hostileTargets.contains(targetType))
            return;
        this.hostileTargets.add(targetType);
    }

    // ========== Damage Modifier ==========

    /**
     * Marks this creature as hostile towards the provided entity class.
     **/
    public void setHostileTo(Class<? extends Entity> targetClass) {
        if (this.hostileTargetClasses == null)
            this.hostileTargetClasses = new ArrayList<>();
        if (this.hostileTargetClasses.contains(targetClass))
            return;
        this.hostileTargetClasses.add(targetClass);
    }


    // ==================================================
    //                      Death
    // ==================================================

    /**
     * Returns true if this mob should defend other entities that cry for help. Used mainly by the revenge AI.
     **/
    public boolean isProtective(Entity entity) {
        return entity.getClass() == this.getClass();
    }


    // ==================================================
    //               Behaviour and Targets
    // ==================================================

    /**
     * Returns true if this mob has an Attack Target.
     **/
    public boolean hasAttackTarget() {
        if (!this.getCommandSenderWorld().isClientSide)
            return this.getTarget() != null;
        else
            return (this.getByteFromDataManager(TARGET) & TARGET_BITS.ATTACK.id) > 0;
    }

    /**
     * Returns this entity's Master Target.
     **/
    public LivingEntity getMasterTarget() {
        return this.masterTarget;
    }

    /**
     * Sets this entity's Master Target
     **/
    public void setMasterTarget(LivingEntity setTarget) {
        this.masterTarget = setTarget;
    }

    /**
     * Returns true if this mob has a Master Target
     **/
    public boolean hasMaster() {
        if (!this.getCommandSenderWorld().isClientSide)
            return this.getMasterTarget() != null;
        else
            return (this.getByteFromDataManager(TARGET) & TARGET_BITS.MASTER.id) > 0;
    }

    /**
     * Returns this entity's Parent Target.
     **/
    public LivingEntity getParentTarget() {
        return this.parentTarget;
    }

    /**
     * Sets this entity's Parent Target
     **/
    public void setParentTarget(LivingEntity setTarget) {
        this.parentTarget = setTarget;
    }

    /**
     * Returns true if this mob has a Parent Target
     **/
    public boolean hasParent() {
        if (!this.getCommandSenderWorld().isClientSide)
            return this.getParentTarget() != null;
        else
            return (this.getByteFromDataManager(TARGET) & TARGET_BITS.PARENT.id) > 0;
    }

    /**
     * Returns this entity's Avoid Target.
     **/
    public LivingEntity getAvoidTarget() {
        return this.avoidTarget;
    }

    /**
     * Sets this entity's Avoid Target
     **/
    public void setAvoidTarget(LivingEntity setTarget) {
        this.currentFleeTime = this.fleeTime;
        this.avoidTarget = setTarget;
    }

    /**
     * Returns true if this mob has a Avoid Target
     **/
    public boolean hasAvoidTarget() {
        if (!this.getCommandSenderWorld().isClientSide)
            return this.getAvoidTarget() != null;
        else
            return (this.getByteFromDataManager(TARGET) & TARGET_BITS.AVOID.id) > 0;
    }

    /**
     * Gets the fixate target of this entity.
     **/
    public LivingEntity getFixateTarget() {
        return this.fixateTarget;
    }

    /**
     * Sets the fixate target of this entity.
     **/
    public void setFixateTarget(LivingEntity target) {
        this.fixateTarget = target;
    }

    /**
     * Returns if the creature has a fixate target.
     **/
    public boolean hasFixateTarget() {
        return this.getFixateTarget() != null;
    }

    /**
     * Returns this entity's Avoid Target.
     **/
    public LivingEntity getPerchTarget() {
        return this.perchTarget;
    }

    /**
     * Sets this entity's Avoid Target
     **/
    public void setPerchTarget(LivingEntity setTarget) {
        this.perchTarget = setTarget;
    }

    /**
     * Returns true if this mob has a Avoid Target
     **/
    public boolean hasPerchTarget() {
        if (!this.getCommandSenderWorld().isClientSide)
            return this.getPerchTarget() != null;
        else
            return (this.getByteFromDataManager(TARGET) & TARGET_BITS.PERCH.id) > 0;
    }

    /**
     * Can be overridden to add a random chance of targeting the provided entity.
     **/
    public boolean rollAttackTargetChance(LivingEntity target) {
        return true;
    }

    /**
     * Returns true if this entity can see the provided entity.
     **/
    @Override
    public boolean hasLineOfSight(Entity target) {
        return super.hasLineOfSight(target);
    }

    /**
     * Returns this entity's Owner Target.
     **/
    public Entity getOwner() {
        return null;
    }

    /**
     * Gets the unique id of the entity that owns this entity.
     *
     * @return The owner entity UUID.
     */
    public UUID getOwnerId() {
        return null;
    }

    /**
     * Returns this entity's Rider Target as an LivingEntity or null if it isn't one, see getRiderTarget().
     **/
    public LivingEntity getRider() {
        if (this.getControllingPassenger() instanceof LivingEntity)
            return (LivingEntity) this.getControllingPassenger();
        else
            return null;
    }

    /**
     * Sets this entity's Rider Target
     **/
    public void setRiderTarget(Entity setTarget) {
        this.addPassenger(setTarget);
    }

    /**
     * Returns true if this mob has a Rider Target
     **/
    public boolean hasRiderTarget() {
        if (!this.getCommandSenderWorld().isClientSide)
            return this.getControllingPassenger() != null;
        else
            return (this.getByteFromDataManager(TARGET) & TARGET_BITS.RIDER.id) > 0;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        if (this.getPassengers().isEmpty()) return null;
        if (this.getPassengers().get(0) instanceof LivingEntity firstPassenger) {
            return firstPassenger;
        }
        return null;
    }

    /**
     * Returns true if this creature can ride the provided entity.
     **/
    @Override
    protected boolean canRide(Entity entity) {
        if (this.isBoss())
            return false;
        return super.canRide(entity);
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return super.isControlledByLocalInstance();
    }

    public boolean canBeControlledByRider() {
        return false;
    }

    /**
     * Called when this creature is being targeted by the provided entity and returns if it should be.
     *
     * @param entity The entity trying to target this entity.
     * @return True if this entity can be targeted.
     */
    public boolean canBeTargetedBy(LivingEntity entity) {
        if (this.isBoss() && entity instanceof BaseCreatureEntity) {
            BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
            if (entityCreature instanceof TameableCreatureEntity) {
                TameableCreatureEntity entityTameable = (TameableCreatureEntity) entity;
                if (entityTameable.getPlayerOwner() != null) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Returns true if this creature considers itself in a pack, used for attack targeting, etc.
     *
     * @return True if in a pack.
     */
    public boolean isInPack() {
        return this.creatureInfo.packSize <= 1 || this.countAllies(10) >= this.creatureInfo.packSize;
    }

    /**
     * Returns the BlockPos in front or behind this entity (using its rotation angle) with the given distance, use a negative distance for behind.
     **/
    public BlockPos getFacingPosition(double distance) {
        return this.getFacingPosition(this, distance, 0D);
    }

    /**
     * Returns the BlockPos in front or behind the provided entity with the given distance and angle offset (in degrees), use a negative distance for behind.
     **/
    public BlockPos getFacingPosition(Entity entity, double distance, double angleOffset) {
        return this.getFacingPosition(entity.position().x(), entity.position().y(), entity.position().z(), distance, entity.yRotO + angleOffset);
    }

    /**
     * Returns the BlockPos in front or behind the provided XYZ coords with the given distance and angle (in degrees), use a negative distance for behind.
     **/
    public BlockPos getFacingPosition(double x, double y, double z, double distance, double angle) {
        angle = Math.toRadians(angle);
        double xAmount = -Math.sin(angle);
        double zAmount = Math.cos(angle);
        return new BlockPos((int) Math.floor(x + (distance * xAmount)), (int) Math.floor(y), (int) Math.floor(z + (distance * zAmount)));
    }

    /**
     * Returns the XYZ in front or behind the provided XYZ coords with the given distance and angle (in degrees), use a negative distance for behind.
     **/
    public Vector3d getFacingPositionDouble(double x, double y, double z, double distance, double angle) {
        if (distance == 0) {
            distance = 1;
        }
        angle = Math.toRadians(angle);
        double xAmount = -Math.sin(angle);
        double zAmount = Math.cos(angle);
        return new Vector3d(x + (distance * xAmount), y, z + (distance * zAmount));
    }

    /**
     * Called every update, this usually manages which phase this mob is using health but it can use any aspect of the mob to determine the Battle Phase and could even be random.
     **/
    public void updateBattlePhase() {

    }


    // ========== Get Facing Coords ==========

    /**
     * Returns the current battle phase.
     **/
    public int getBattlePhase() {
        return this.battlePhase;
    }

    /**
     * Sets the current battle phase.
     **/
    public void setBattlePhase(int phase) {
        if (this.getBattlePhase() == phase)
            return;
        this.battlePhase = phase;
        this.refreshBossHealthName();
        this.playPhaseSound();
    }

    /**
     * Transforms this entity into a new entity instantiated from the given class.
     * transformClass The entity class to transform into.
     * partner If not null, various stats, etc will be shared from this partner.
     * destroyPartner If true and a partner is set, the partner will be removed.
     * return The transformed entity instance. Null on failure (usually when an invalid class is provided).
     */
    public LivingEntity transform(EntityType<? extends LivingEntity> transformType, Entity partner, boolean destroyPartner) {
        if (transformType == null) {
            return null;
        }
        LivingEntity transformedEntity = transformType.create(this.getCommandSenderWorld());
        if (transformedEntity == null) {
            return null;
        }

        // Creature Base:
        if (transformedEntity instanceof BaseCreatureEntity) {
            BaseCreatureEntity transformedCreature = (BaseCreatureEntity) transformedEntity;
            transformedCreature.firstSpawn = false;

            // Temporary:
            if (this.isTemporary) {
                transformedCreature.setTemporary(this.temporaryDuration);
            }

            // Minion:
            if (this.isMinion()) {
                transformedCreature.setMinion(true);
            }

            // Master Target:
            if (this.hasMaster()) {
                transformedCreature.setMasterTarget(this.getMasterTarget());
            }

            // With Partner:
            if (partner != null && partner instanceof BaseCreatureEntity) {
                BaseCreatureEntity partnerCreature = (BaseCreatureEntity) partner;
                Variant fusionVariant = transformedCreature.getSubspecies().getChildVariant(this, this.getVariant(), partnerCreature.getVariant());
                transformedCreature.setSubspecies(this.getSubspeciesIndex());
                transformedCreature.applyVariant(fusionVariant != null ? fusionVariant.index : 0);
                transformedCreature.setSizeScale(this.sizeScale + partnerCreature.sizeScale);

                // Summoning Pedestal:
                if (partnerCreature.summoningPedestal != null) {
                    partnerCreature.summoningPedestal.minions.add(transformedCreature);
                    transformedCreature.summoningPedestal = partnerCreature.summoningPedestal;
                }
                if (this.summoningPedestal != null) {
                    this.summoningPedestal.minions.add(transformedCreature);
                    transformedCreature.summoningPedestal = this.summoningPedestal;
                }

                // Level:
                int transformedLevel = this.getMobLevel();
                if ("lowest".equalsIgnoreCase(CreatureManager.getInstance().config.elementalFusionLevelMix)) {
                    if (transformedLevel > partnerCreature.getMobLevel()) {
                        transformedLevel = partnerCreature.getMobLevel();
                    }
                } else if ("highest".equalsIgnoreCase(CreatureManager.getInstance().config.elementalFusionLevelMix)) {
                    if (transformedLevel < partnerCreature.getMobLevel()) {
                        transformedLevel = partnerCreature.getMobLevel();
                    }
                } else {
                    transformedLevel += partnerCreature.getMobLevel();
                }
                transformedCreature.applyLevel(Math.round(transformedLevel * (float) CreatureManager.getInstance().config.elementalFusionLevelMultiplier));

                // Tamed:
                if (transformedCreature instanceof TameableCreatureEntity) {
                    TameableCreatureEntity fusionTameable = (TameableCreatureEntity) transformedCreature;
                    Entity owner;
                    if (this instanceof TameableCreatureEntity) {
                        owner = ((TameableCreatureEntity) this).getPlayerOwner();
                        if (owner != null) {
                            transformedCreature.applyLevel(transformedLevel);
                            fusionTameable.setPlayerOwner((Player) owner);
                            ((TameableCreatureEntity) this).copyPetBehaviourTo(fusionTameable);
                        }
                    }

                    // Tamed Partner:
                    else {
                        Entity partnerOwner = null;
                        if (partnerCreature instanceof TameableCreatureEntity) {
                            partnerOwner = ((TameableCreatureEntity) partnerCreature).getPlayerOwner();
                            if (partnerOwner != null) {
                                transformedCreature.applyLevel(transformedLevel);
                                fusionTameable.setPlayerOwner((Player) partnerOwner);
                                ((TameableCreatureEntity) partnerCreature).copyPetBehaviourTo(fusionTameable);

                                // Temporary:
                                if (partnerCreature.isTemporary) {
                                    transformedCreature.setTemporary(partnerCreature.temporaryDuration);
                                }

                                // Minion:
                                transformedCreature.setMinion(partnerCreature.isMinion());

                                // Master Target:
                                if (partnerCreature.hasMaster()) {
                                    transformedCreature.setMasterTarget(partnerCreature.getMasterTarget());
                                }
                            }
                        }
                    }
                }
            }

            // Without Partner:
            else {
                transformedCreature.setSubspecies(this.getSubspeciesIndex());
                transformedCreature.applyVariant(this.getVariantIndex());
                transformedCreature.setSizeScale(this.sizeScale);
                transformedCreature.applyLevel(this.getMobLevel());

                // Tamed:
                if (transformedCreature instanceof TameableCreatureEntity) {
                    TameableCreatureEntity fusionTameable = (TameableCreatureEntity) transformedCreature;
                    if (this.getOwner() != null && this.getOwner() instanceof Player) {
                        fusionTameable.setPlayerOwner((Player) this.getOwner());
                        if (this instanceof TameableCreatureEntity) {
                            ((TameableCreatureEntity) this).copyPetBehaviourTo(fusionTameable);
                        }
                    }
                }
            }
        }

        // Transformed Entity:
        transformedEntity.moveTo(this.position().x(), this.position().y(), this.position().z(), this.yRotO, this.xRotO);
        this.getCommandSenderWorld().addFreshEntity(transformedEntity);

        // Remove Parts:
        this.remove(RemovalReason.DISCARDED);
        if (partner != null && destroyPartner) {
            partner.remove(RemovalReason.DISCARDED);
        }

        return transformedEntity;
    }

    /**
     * Returns if this creature is considered to be tamed where it behaves a bit differently.
     *
     * @return True if tamed.
     */
    public boolean isTamed() {
        return false;
    }


    // ==================================================
    //                  Battle Phases
    // ==================================================

    /**
     * Can this entity move currently?
     **/
    public boolean canMove() {
        return !this.isBlocking();
    }

    /**
     * Can this entity move across land currently? Usually used for swimming mobs to prevent land movement.
     **/
    public boolean canWalk() {
        return true;
    }

    /**
     * Can this entity wade through fluids currently (walks on ground and through fluids but does not freely swim).
     **/
    public boolean canWade() {
        return true;
    }


    // ==================================================
    //                    Transform
    // ==================================================

    /**
     * Better name would be isSwimming(). Can this entity swim this tick checks if in fluids, checks for lava instead of water if isLavaCreature is true.
     **/
    @Override
    public boolean isUnderWater() {
        if (this.isLavaCreature) {
            return this.isInLava();
        }
        return super.isUnderWater();
    }


    // ==================================================
    //                       Taming
    // ==================================================

    /**
     * Returns true if this entity should swim to the liquid surface when pathing, by default entities that can't breather underwater will try to surface.
     **/
    public boolean shouldFloat() {
        return !this.canBreatheUnderwater() && !this.canBreatheUnderlava();
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    // ========== Movement ==========

    /**
     * Returns true if this entity should dive underwater/underlava when pathing, by default entities that can breathe underwater or underlava will try to dive.
     **/
    public boolean shouldDive() {
        return this.canBreatheUnderwater() || this.canBreatheUnderlava();
    }

    /**
     * Should this entity use smoother, faster swimming? (This doesn't stop the entity from moving in water but is used for smooth flight-like swimming).
     **/
    public boolean isStrongSwimmer() {
        if (this.extraMobBehaviour != null)
            if (this.extraMobBehaviour.swimmingOverride)
                return true;
        return false;
    }

    /**
     * Can this entity climb currently?
     **/
    public boolean canClimb() {
        return false;
    }

    /**
     * Returns true if this mob is currently flying. If true this entity will use flight navigation, etc.
     **/
    public boolean isFlying() {
        if (this.extraMobBehaviour != null)
            if (this.extraMobBehaviour.flightOverride)
                return true;
        return false;
    }

    /**
     * Returns how high this mob prefers to fly about the ground, usually when randomly wandering.
     **/
    public int getFlyingHeight() {
        if (!this.isFlying())
            return 20;
        return 0;
    }

    /**
     * Returns true if this creature can safely land from its current position.
     **/
    public boolean isSafeToLand() {
        if (this.onGround())
            return true;
        if (this.getCommandSenderWorld().getBlockState(this.blockPosition().below()).isSolid())
            return true;
        if (this.getCommandSenderWorld().getBlockState(this.blockPosition().below(2)).isSolid())
            return true;
        return false;
    }

    /**
     * Returns how high above attack targets this mob should fly when chasing.
     **/
    public double getFlightOffset() {
        return 0D;
    }

    /**
     * Can this entity by tempted (usually lured by an item) currently?
     **/
    public boolean canBeTempted() {
        if (this.isRareVariant() || this.spawnedAsBoss) {
            return false;
        }
        if (this.creatureInfo.isFarmable()) {
            return true;
        }
        if (this.isInPack() && !CreatureManager.getInstance().config.packTreatLuring) {
            return false;
        }
        return this.creatureInfo.isTameable();
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    /**
     * Called when the creature has eaten. Some special AIs use this such as EntityAIEatBlock.
     **/
    public void onEat() {
    }

    /**
     * Can this entity stealth currently?
     **/
    public boolean canStealth() {
        if (this.extraMobBehaviour != null)
            if (this.extraMobBehaviour.stealthOverride)
                return true;
        return false;
    }

    /**
     * Get the current stealth percentage, 0.0F = not stealthed, 1.0F = completely stealthed, used for animation such as burrowing crusks.
     **/
    public float getStealth() {
        return this.getFloatFromDataManager(STEALTH);
    }

    /**
     * Sets the current stealth percentage.
     **/
    public void setStealth(float setStealth) {
        setStealth = Math.min(setStealth, 1);
        setStealth = Math.max(setStealth, 0);
        if (!this.getCommandSenderWorld().isClientSide)
            this.entityData.set(STEALTH, setStealth);
    }

    /**
     * Returns true if this mob is fully stealthed (1.0F or above).
     **/
    public boolean isStealthed() {
        return this.getStealth() >= 1.0F;
    }

    /**
     * Called when this mob is just started stealthing (reach 1.0F or above).
     **/
    public void startStealth() {
    }

    // ========== Stealth ==========

    /**
     * Called while this mob is stealthed on the update, can be used to clear enemies targets that are targeting this mob, although a new event listener is in place now to handle this. The main EventListener also helps handling anti-targeting.
     **/
    public void onStealth() {
        if (!this.getCommandSenderWorld().isClientSide) {
            if (this.getTarget() != null && this.getTarget() instanceof Mob)
                if (((Mob) this.getTarget()).getTarget() != null)
                    ((Mob) this.getTarget()).setTarget(null);
        }
    }

    /**
     * Returns true if this entity is climbing a ladder or wall, can be used for animation.
     **/
    @Override
    public boolean onClimbable() {
        if (this.isFlying() || (this.isStrongSwimmer() && this.isInWater())) return false;
        if (this.canClimb()) {
            return (this.getByteFromDataManager(CLIMBING) & 1) != 0;
        } else
            return super.onClimbable();
    }

    /**
     * Returns whether or not this mob is next to a climbable blocks or not.
     **/
    public boolean isBesideClimbableBlock() {
        return (this.getByteFromDataManager(CLIMBING) & 1) != 0;
    }

    /**
     * Used to set whether this mob is climbing up a block or not.
     **/
    public void setBesideClimbableBlock(boolean collided) {
        if (this.canClimb()) {
            byte climbing = this.getByteFromDataManager(CLIMBING);
            if (collided)
                climbing = (byte) (climbing | 1);
            else climbing &= -2;
            this.entityData.set(CLIMBING, climbing);
        }
    }

    /**
     * Called when the mob has hit the ground after falling, fallDistance is how far it fell and can be translated into fall damage.
     * getFallResistance() is used to reduce falling damage, if it is at or above 100 no falling damage is taken at all.
     **/
    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        if (this.isFlying())
            return false;
        fallDistance -= this.getFallResistance();
        if (this.getFallResistance() >= 100)
            fallDistance = 0;
        return super.causeFallDamage(fallDistance, damageMultiplier, source);
    }

    /**
     * Called when this mob is falling, y is how far the mob has fell so far and onGround is true when it has hit the ground.
     **/
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (!this.isFlying())
            super.checkFallDamage(y, onGround, state, pos);
    }

    // ========== Climbing ==========

    /**
     * When called, this will set the mob as blocking, can be overridden to randomize the blocking duration.
     **/
    public void setBlocking() {
        this.currentBlockingTime = this.blockingTime;
    }

    /**
     * Returns true if this mob is blocking.
     **/
    public boolean isBlocking() {
        if (this.getCommandSenderWorld().isClientSide)
            return (this.getByteFromDataManager(ANIMATION_STATE) & ANIMATION_STATE_BITS.BLOCKING.id) > 0;
        return this.currentBlockingTime > 0;
    }

    /**
     * Returns true if this mob can attack while blocking.
     **/
    public boolean canAttackWhileBlocking() {
        return false;
    }

    // ========== Falling ==========

    /**
     * Returns the blocking defense multiplier, when blocking this mobs defense is multiplied by this, also if this mobs defense is below 1 it will be moved up to one.
     **/
    public int getBlockingMultiplier() {
        return 4;
    }

    // ========== Pickup ==========
    public boolean canPickupEntity(LivingEntity entity) {
        if (this.getPickupEntity() == entity)
            return false;
        if (entity instanceof IGroupBoss)
            return false;
        if (entity instanceof Player && ((Player) entity).isCreative()) {
            return false;
        }
        if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity) entity).hasPickupEntity()) {
            return false;
        }
        if (entity.isSpectator()) {
            return false;
        }
        if (CreatureManager.getInstance().creatureGroups.containsKey("boss") && CreatureManager.getInstance().creatureGroups.get("boss").hasEntity(entity))
            return false;
        boolean heavyTarget = entity instanceof IGroupHeavy || entity.getDimensions(Pose.STANDING).height >= 4 || entity.getDimensions(Pose.STANDING).width >= 4;
        if (heavyTarget && !(this instanceof IGroupHeavy))
            return false;
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
        if (extendedEntity == null)
            return false;
        if ((entity.getVehicle() != null && !(entity.getVehicle() instanceof Boat) && !(entity.getVehicle() instanceof Minecart)) || entity.getControllingPassenger() != null)
            return false;
        if (ObjectManager.getEffect("weight") != null)
            if ((entity).hasEffect(ObjectManager.getEffect("weight")))
                return false;
        if (ObjectManager.getEffect("repulsion") != null)
            if ((entity).hasEffect(ObjectManager.getEffect("repulsion")))
                return false;
        return extendedEntity.pickedUpByEntity == null;
    }

    // ========== Blocking ==========

    public void pickupEntity(LivingEntity entity) {
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
        if (extendedEntity != null)
            extendedEntity.setPickedUpByEntity(this);
        this.pickupEntity = entity;
        this.clearMovement();
    }

    public LivingEntity getPickupEntity() {
        return this.pickupEntity;
    }

    public boolean hasPickupEntity() {
        if (!this.getCommandSenderWorld().isClientSide)
            return this.getPickupEntity() != null;
        else
            return (this.getByteFromDataManager(TARGET) & TARGET_BITS.PICKUP.id) > 0;
    }

    public void dropPickupEntity() {
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPickupEntity());
        if (extendedEntity != null)
            extendedEntity.setPickedUpByEntity(null);
        this.pickupEntity = null;
    }

    public double[] getPickupOffset(Entity entity) {
        return new double[]{0, 0, 0};
    }

    public boolean canAttackWithPickup() {
        return false;
    }

    // ========== Destroy Blocks ==========
    public void destroyArea(int x, int y, int z, float strength, boolean drop) {
        this.destroyArea(x, y, z, strength, drop, 0);
    }

    public void destroyArea(int x, int y, int z, float strength, boolean drop, int range) {
        this.destroyArea(x, y, z, strength, drop, range, null, 0);
    }

    public void destroyArea(int x, int y, int z, float strength, boolean drop, int range, Player player, int chain) {
        range = Math.max(range - 1, 0);
        for (int w = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width) - range); w <= (Math.ceil(this.getDimensions(Pose.STANDING).width) + range); w++) {
            for (int d = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width) - range); d <= (Math.ceil(this.getDimensions(Pose.STANDING).width) + range); d++) {
                for (int h = 0; h <= Math.ceil(this.getDimensions(Pose.STANDING).height); h++) {
                    BlockPos breakPos = new BlockPos(x + w, y + h, z + d);
                    BlockState blockState = this.getCommandSenderWorld().getBlockState(breakPos);
                    if (this.getCommandSenderWorld().getBlockEntity(breakPos) != null) {
                        continue;
                    }
                    float hardness = blockState.getDestroySpeed(this.getCommandSenderWorld(), breakPos);
                    Block material = blockState.getBlock();
                    if (hardness >= 0 && strength >= hardness && strength >= blockState.getBlock().getExplosionResistance() && material != Blocks.WATER && material != Blocks.LAVA) {
                        // If a player is set this is from a spawner in which case don't destroy the central block.
                        if (player == null || !(w == 0 && h == 0 && d == 0)) {
                            SpawnerEventListener.getInstance().onBlockBreak(this.getCommandSenderWorld(), breakPos, blockState, player, chain);
                            this.getCommandSenderWorld().destroyBlock(breakPos, drop);
                        }
                    }
                }
            }
        }
    }

    public void destroyAreaBlock(int x, int y, int z, Class<WoodType> blockClass, boolean drop, int range) {
        for (int w = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width) + range); w <= (Math.ceil(this.getDimensions(Pose.STANDING).width) + range); w++)
            for (int d = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width) + range); d <= (Math.ceil(this.getDimensions(Pose.STANDING).width) + range); d++)
                for (int h = 0; h <= Math.ceil(this.getDimensions(Pose.STANDING).height); h++) {
                    BlockPos breakPos = new BlockPos(x + w, y + h, z + d);
                    if (this.getCommandSenderWorld().getBlockEntity(breakPos) != null) {
                        continue;
                    }
                    BlockState blockState = this.getCommandSenderWorld().getBlockState(breakPos);
                    if (blockClass.isInstance(blockState.getBlock())) {
                        this.getCommandSenderWorld().destroyBlock(breakPos, drop);
                    }
                }
    }

    /**
     * An additional animation boolean that is passed to all clients through the animation mask.
     **/
    public boolean extraAnimation01() {
        return this.extraAnimation01;
    }

    /**
     * Applies all element debuffs to the target entity.
     *
     * @param entity    The entity to debuff.
     * @param duration  The duration in seconders which is multiplied by this creature's stats.
     * @param amplifier The effect amplifier which is multiplied by this creature's stats.
     */
    public void applyDebuffs(LivingEntity entity, int duration, int amplifier) {
        for (ElementInfo element : this.getElements()) {
            element.debuffEntity(entity, this.getEffectDuration(duration), this.getEffectAmplifier(amplifier));
        }
    }

    /**
     * Applies all element buffs to the target entity.
     *
     * @param entity    The entity to buff.
     * @param duration  The duration in seconders which is multiplied by this creature's stats.
     * @param amplifier The effect amplifier which is multiplied by this creature's stats.
     */
    public void applyBuffs(LivingEntity entity, int duration, int amplifier) {
        if (this.creatureStats.getAmplifier() >= 0) {
            for (ElementInfo element : getElements()) {
                element.buffEntity(entity, this.getEffectDuration(duration), this.getEffectAmplifier(amplifier));
            }
        }
    }

    /**
     * Cycles through all of this entity's DropRates and drops random loot, usually called on death. If this mob is a minion, this method is cancelled.
     **/
    @Override
    protected void dropAllDeathLoot(DamageSource damageSource) {
        if (this.getCommandSenderWorld().isClientSide || this.isMinion() || this.isBoundPet() || this.dropsRequirePlayerDamage || this.hasDropped)
            return;
        this.hasDropped = true;

        int variantScale = 1;
        if (this.isRareVariant())
            variantScale = Variant.RARE_DROP_SCALE;
        else if (this.getVariant() != null && "uncommon".equals(this.getVariant().rarity))
            variantScale = Variant.UNCOMMON_DROP_SCALE;

        int lootingLevel = 0;
        if (damageSource.getEntity() != null) {
            lootingLevel = ForgeHooks.getLootingLevel(this, damageSource.getEntity(), damageSource);
        }

        for (ItemDrop itemDrop : this.drops) {
            if (!this.canDropItem(itemDrop)) {
                continue;
            }
            int multiplier = 1;
            if (itemDrop.variantIndex < 0) {
                multiplier *= variantScale;
            }
            if (this.extraMobBehaviour != null && this.extraMobBehaviour.itemDropMultiplierOverride != 1) {
                multiplier = Math.round((float) multiplier * (float) this.extraMobBehaviour.itemDropMultiplierOverride);
            }
            int quantity = itemDrop.getQuantity(this.random, lootingLevel, multiplier);
            if (quantity <= 0) {
                continue;
            }
            this.dropItem(itemDrop.getEntityDropItemStack(this, quantity));
        }

        this.dropExperience();
    }

    public boolean canDropItem(ItemDrop itemDrop) {
        if (itemDrop.subspeciesIndex >= 0 && itemDrop.subspeciesIndex != this.getSubspeciesIndex()) {
            return false;
        }
        if (itemDrop.variantIndex >= 0 && itemDrop.variantIndex != this.getVariantIndex()) {
            return false;
        }
        return true;
    }

    // ========== Extra Animations ==========

    /**
     * Tells this entity to drop the specified itemStack, used by DropRate and InventoryCreature, can be used by anything though.
     **/
    public void dropItem(ItemStack itemStack) {
    	/*if(itemStack.getItem() instanceof ItemEquipmentPart) {
			((ItemEquipmentPart)itemStack.getItem()).randomizeLevel(this.world, itemStack);
		}*/
        this.spawnAtLocation(itemStack, 0.0F);
    }

    /**
     * The vanilla item drop method, overridden to make use of the EntityItemCustom class. I recommend using dropItem() instead.
     **/
    @Override
    public ItemEntity spawnAtLocation(ItemStack itemStack, float heightOffset) {
        if (itemStack.getCount() != 0) {
            CustomItemEntity entityItem = new CustomItemEntity(this.getCommandSenderWorld(), this.position().x(), this.position().y() + (double) heightOffset, this.position().z(), itemStack);
            entityItem.setPickUpDelay(10);
            this.applyDropEffects(entityItem);

            this.getCommandSenderWorld().addFreshEntity(entityItem);
            return entityItem;
        } else {
            return null;
        }
    }

    /**
     * Used to add effects or alter the dropped entity item.
     **/
    public void applyDropEffects(CustomItemEntity entityItem) {
    }


    // ==================================================
    //                      Drops
    // ==================================================

    /**
     * Sets if this creature should no longer drop items until it takes damage from a source belonging to a player.
     *
     * @param requiresPlayerDamage True if this creature should no longer drop items until damaged by a player, false if they should drop items regardless.
     */
    public void setDropsRequirePlayerDamage(boolean requiresPlayerDamage) {
        this.dropsRequirePlayerDamage = requiresPlayerDamage;
    }

    // ==================================================
    //                       Perching
    // ==================================================
    public void perchOnEntity(LivingEntity target) {
        // Unperch:
        if (target == null) {
            if (this.getPerchTarget() != null) {
                ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPerchTarget());
                if (extendedEntity != null) {
                    extendedEntity.setPerchedByEntity(null);
                }
            }
            this.setPerchTarget(null);
            return;
        }

        // Perch:
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(target);
        if (extendedEntity == null) {
            return;
        }
        this.setPerchTarget(target);
        extendedEntity.setPerchedByEntity(this);
    }

    // ========== Drop Item ==========

    /**
     * This adds the provided Player to the guiViewers array list, where on the next GUI refresh it will open the GUI.
     **/
    public void openGUI(Player player) {
        if (this.getCommandSenderWorld().isClientSide)
            return;
        this.addGUIViewer(player);
        this.refreshGUIViewers();
        this.openGUIToPlayer(player);
    }

    // ========== Entity Drop Item ==========

    /**
     * This adds the provided Player to the guiViewers array list, where on the next GUI refresh it will open the GUI.
     **/
    public void addGUIViewer(Player player) {
        if (!this.getCommandSenderWorld().isClientSide)
            this.guiViewers.add(player);
    }

    // ========== Apply Drop Effects ==========

    /**
     * This removes the provided Player from the guiViewers array list.
     **/
    public void removeGUIViewer(Player player) {
        if (!this.getCommandSenderWorld().isClientSide)
            this.guiViewers.remove(player);
    }

    /**
     * Called when all players viewing their entity's gui need to be refreshed. Usually after a GUI command on inventory change. Should be called using scheduleGUIRefresh().
     **/
    public void refreshGUIViewers() {
        if (this.getCommandSenderWorld().isClientSide)
            return;
        if (this.guiViewers.size() > 0) {
            for (Player player : this.guiViewers.toArray(new Player[this.guiViewers.size()])) {
                if (player.containerMenu instanceof CreatureContainer) {
                    if (((CreatureContainer) player.containerMenu).creature == this)
                        this.openGUIToPlayer(player);
                    else
                        this.removeGUIViewer(player);
                }
            }
        }
    }

    /**
     * Actually opens the GUI to the player, should be used by openGUI() for an initial opening and then by refreshGUIViewers() for constant updates.
     **/
    public void openGUIToPlayer(Player player) {
        if (player instanceof ServerPlayer)
            NetworkHooks.openScreen((ServerPlayer) player, new CreatureContainerProvider(this), buf -> buf.writeInt(this.getId()));
    }


    // ==================================================
    //                     Interact
    // ==================================================
    // ========== GUI ==========

    /**
     * Schedules a GUI refresh, normally takes 2 ticks for everything to update for display.
     **/
    public void scheduleGUIRefresh() {
        this.guiRefreshTick = this.guiRefreshTime + 1;
    }

    /**
     * The main interact method that is called when a player right clicks this entity.
     **/
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) { // New process interact
        if (this.hasPerchTarget()) {
            return InteractionResult.FAIL;
        }
        if (this.assessInteractCommand(getInteractCommands(player, player.getItemInHand(hand)), player, player.getItemInHand(hand))) {
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    /**
     * Performs the best possible command and returns true or false if there isn't one.
     **/
    public boolean assessInteractCommand(HashMap<Integer, String> commands, Player player, ItemStack itemStack) {
        if (commands.isEmpty())
            return false;
        int priority = 100;
        for (int testPriority : commands.keySet())
            if (testPriority < priority)
                priority = testPriority;
        if (!commands.containsKey(priority))
            return false;
        return performCommand(commands.get(priority), player, itemStack);
    }

    /**
     * Gets a map of all possible interact events with the key being the priority, lower is better.
     **/
    public HashMap<Integer, String> getInteractCommands(Player player, @Nonnull ItemStack itemStack) {
        HashMap<Integer, String> commands = new HashMap<>();

        // Item Commands:
        if (!itemStack.isEmpty()) {
            // Leash:
            if (itemStack.getItem() == Items.LEAD && this.canBeLeashed(player))
                commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Leash");

            // Name Tag:
            if (itemStack.getItem() == Items.NAME_TAG) {
                if (this.canNameTag(player))
                    return new HashMap<>(); // Cancels all commands so that vanilla can take care of name tagging.
                else
                    commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Name Tag"); // Calls nothing and therefore cancels name tagging.
            }

            // Coloring:
            if (this.canBeColored(player) && itemStack.getItem() instanceof DyeItem) {
                commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Color");
            }

            // Soulgazer:
            if (itemStack.getItem() instanceof ItemSoulgazer) {
                commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Soulgazer");
            }
        }

        return commands;
    }

    /**
     * Performs the given interact command. Could be used outside of the interact method if needed.
     *
     * @param command   The command to perform.
     * @param player    The player that triggered the command.
     * @param itemStack The item the player is holding.
     * @return True if the player's item should not activate, false if it should.
     */
    public boolean performCommand(String command, Player player, ItemStack itemStack) {
        // Leash:
        if ("Leash".equals(command)) {
            this.setLeashedTo(player, true);
            this.consumePlayersItem(player, itemStack);
            return true;
        }

        // Color:
        if ("Color".equals(command) && itemStack.getItem() instanceof DyeItem) {
            DyeItem dye = (DyeItem) itemStack.getItem();
            DyeColor color = dye.getDyeColor();
            if (color != this.getColor()) {
                this.setColor(color);
                this.consumePlayersItem(player, itemStack);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if this mob can be given a new name with a name tag by the provided player entity.
     **/
    public boolean canNameTag(Player player) {
        return true;
    }

    /**
     * Gets whether this mob should always display its nametag client side.
     **/
    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldShowName() {
        if (this.getVariant() != null && !this.hasCustomName())
            return this.renderVariantNameTag();
        return super.shouldShowName();
    }

    // ========== Assess Interact Command ==========

    /**
     * Gets whether this mob should always display its nametag if it's a subspecies.
     **/
    public boolean renderVariantNameTag() {
        return CreatureManager.getInstance().config.subspeciesTags;
    }

    // ========== Get Interact Commands ==========

    /**
     * Consumes 1 item from the the item stack currently held by the specified player.
     **/
    public void consumePlayersItem(Player player, ItemStack itemStack) {
        consumePlayersItem(player, itemStack, 1);
    }

    // ========== Perform Command ==========

    /**
     * Consumes the specified amount from the item stack currently held by the specified player.
     **/
    public void consumePlayersItem(Player player, ItemStack itemStack, int amount) {
        if (!player.getAbilities().invulnerable)
            itemStack.setCount(Math.max(0, itemStack.getCount() - amount));
        if (itemStack.getCount() <= 0)
            player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
    }

    // ========== Can Name Tag ==========

    /**
     * Replaces 1 of the specified itemstack with a new itemstack.
     **/
    public void replacePlayersItem(Player player, ItemStack itemStack, ItemStack newStack) {
        replacePlayersItem(player, itemStack, 1, newStack);
    }

    // ========== Get Render Name Tag ==========

    /**
     * Replaces the specified itemstack and amount with a new itemstack.
     **/
    public void replacePlayersItem(Player player, ItemStack itemStack, int amount, ItemStack newStack) {
        if (!player.getAbilities().invulnerable)
            itemStack.setCount(Math.max(0, itemStack.getCount() - amount));

        if (itemStack.getCount() <= 0)
            player.getInventory().setItem(player.getInventory().selected, newStack);

        else if (!player.getInventory().add(newStack))
            player.drop(newStack, false);

    }

    // ========== Render Subspecies Name Tag ==========

    // ========== Perform GUI Command ==========
    public void performGUICommand(Player player, int guiCommandID) {
        scheduleGUIRefresh();
    }

    // ========== Consume Player's Item ==========

    /**
     * Returns true if this mob is able to carry items.
     **/
    public boolean canCarryItems() {
        return getInventorySize() > 0;
    }

    /**
     * Returns the current size of this mob's inventory. (Some mob inventories can vary in size such as mounts with and without bag items equipped.)
     **/
    public int getInventorySize() {
        return this.inventory.getContainerSize();
    }

    // ========== Replace Player's Item ==========

    /**
     * Returns the maximum possible size of this mob's inventory. (The creature inventory is not actually resized, instead some slots are locked and made unavailable.)
     **/
    public int getInventorySizeMax() {
        return Math.max(this.getNoBagSize(), this.getBagSize());
    }

    /**
     * Returns true if this mob is equipped with a bag item.
     **/
    public boolean hasBag() {
        return this.inventory.getEquipmentStack("bag") != null;
    }

    /**
     * Returns the size of this mob's inventory when it doesn't have a bag item equipped.
     **/
    public int getNoBagSize() {
        if (this.extraMobBehaviour != null)
            if (this.extraMobBehaviour.inventorySizeOverride > 0)
                return this.extraMobBehaviour.inventorySizeOverride;
        return 0;
    }


    // ==================================================
    //                     Equipment
    // ==================================================

    /**
     * Returns the size that this mob's inventory increases by when it is provided with a bag item. (Look at this as the size of the bag item, not the new total creature inventory size.)
     **/
    public int getBagSize() {
        if (this.creatureInfo != null) {
            return this.creatureInfo.bagSize;
        }
        return 5;
    }

    /**
     * Returns true if this mob is able to pick items up off the ground.
     **/
    public boolean canPickupItems() {
        if (this.extraMobBehaviour != null)
            if (this.extraMobBehaviour.itemPickupOverride)
                return true;
        return false;
    }

    /**
     * Returns how much of the specified item stack this creature's inventory can hold. (Stack size, not empty slots, this allows the creature to merge stacks when picking up.)
     **/
    public int getSpaceForStack(ItemStack pickupStack) {
        return this.inventory.getSpaceForStack(pickupStack);
    }

    /**
     * Returns true if the player is allowed to equip this creature with items such as armor or saddles.
     **/
    public boolean canEquip() {
        return this.creatureInfo.isTameable();
    }

    /**
     * A 1.7.10 vanilla method for setting this mobs equipment, takes a slot ID and a stack.
     * 0 = Weapons, Tools or the item to hold out (like how vanilla zombies hold dropped items).
     * 1 = Feet, 2 = Legs, 3 = Chest and 4 = Head
     * 100 = Not used by vanilla but will convert to the bag slot for other mods to use.
     **/
    public void setCurrentItemOrArmor(int slot, ItemStack itemStack) {
        String type = "item";
        if (slot == 0) type = "weapon";
        if (slot == 1) type = "feet";
        if (slot == 2) type = "legs";
        if (slot == 3) type = "chest";
        if (slot == 4) type = "head";
        if (slot == 100) type = "bag";
        this.inventory.setEquipmentStack(type, itemStack);
    }

    /**
     * Returns the equipment grade, used mostly for texturing the armor.
     * For INSTANCE "gold" is returned if it is wearing gold chest armor.
     * Type is a string that is the equipment slot, it can be: feet, legs, chest or head. All lower case.
     **/
    public String getEquipmentName(String type) {
        if (this.inventory.getEquipmentGrade(type) != null)
            return type + this.inventory.getEquipmentGrade(type);
        return null;
    }

    /**
     * Returns the total armor value of this mob.
     **/
    @Override
    public int getArmorValue() {
        return super.getArmorValue() + this.inventory.getArmorValue();
    }

    /**
     * Called on the update if this mob is able to pickup items. Searches for all nearby item entities and picks them up.
     **/
    public void pickupItems() {
        List list = this.getCommandSenderWorld().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0D, 0.0D, 1.0D));
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            ItemEntity entityItem = (ItemEntity) iterator.next();
            if (entityItem.isAlive() && !entityItem.getItem().isEmpty()) {
                ItemStack itemStack = entityItem.getItem();
                int space = this.getSpaceForStack(itemStack);
                if (space > 0) {
                    this.onPickupStack(itemStack);
                    this.doItemPickup(entityItem);
                }
            }
        }
    }

    /**
     * Called when this mob picks up an item entity, provides the itemStack it has picked up.
     **/
    public void onPickupStack(ItemStack itemStack) {
    }

    // ========== Set Equipment ==========
    // Vanilla Conversion: 0 = Weapon/Item,  1 = Feet -> 4 = Head

    public void doItemPickup(ItemEntity entityItem) {
        if (entityItem.isAlive() && !entityItem.getItem().isEmpty()) {
            ItemStack leftoverStack = this.inventory.autoInsertStack(entityItem.getItem());
            if (leftoverStack != null)
                entityItem.setItem(leftoverStack);
            else
                entityItem.remove(RemovalReason.DISCARDED);
        }
    }

    // ========== Get Equipment ==========

    /**
     * Called (by ai goals) when this mob places a block.
     *
     * @param blockPos   The position the block was placed at.
     * @param blockState The block state that was placed.
     */
    public void onBlockPlaced(BlockPos blockPos, BlockState blockState) {

    }

    // ========== Get Total Armor Value ==========

    // ==================================================
    //                     Immunities
    // ==================================================
    // ========== Damage ==========
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // Damage Limit:
        if (this.damageLimit > 0) {
            if (this.damageTakenThisSec >= this.damageLimit)
                return true;
        }

        // Falling Damage:
        if (source == this.level().damageSources().fall()) {
            return this.getFallResistance() >= 100;
        }

        // Fire Damage:
        if (source.is(DamageTypeTags.IS_FIRE) && !this.canBurn()) {
            return true;
        }

        // Element Damage:
        if (source instanceof ElementDamageSource && getElements().contains(((ElementDamageSource) source).getElement())) {
            return false;
        }

        // Entity:
        var entity = source.getEntity();
        if (this.isBoss() || this.isRareVariant()) {
            if (entity == null)
                return true;
            if (this.distanceTo(entity) > this.bossRange) {
                if (entity instanceof Player) {
                    ((Player) entity).displayClientMessage(Component.translatable("boss.damage.protection.range"), true);
                }
                return true;
            }
        }

        // Damage Source:
        if ((source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS)) && (this.isRareVariant() || this.isBoss()))
            return true;
        if (source.is(DamageTypes.IN_WALL))
            return CreatureManager.getInstance().config.suffocationImmunity && this.hasPerchTarget();
        if (source.is(DamageTypes.DROWN))
            return CreatureManager.getInstance().config.drownImmunity;

        return super.isInvulnerableTo(source);
    }

    @Override
    public void lavaHurt() {
        if (!this.canBurn())
            return;
        super.lavaHurt();
    }

    @Override
    public void setSecondsOnFire(int seconds) {
        if (!this.canBurn())
            return;
        super.setSecondsOnFire(seconds);
    }

    /**
     * Returns whether or not the specified potion effect can be applied to this entity.
     **/
    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        for (ElementInfo element : getElements()) {
            if (!element.isEffectApplicable(effectInstance)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether or not this entity can be set on fire, this will block both the damage and the fire effect, use isDamageTypeApplicable() to block fire but keep the effect. isImmuneToFire is now final so that is just false but ignored replaced by this.
     **/
    public boolean canBurn() {
        if (this.extraMobBehaviour != null)
            if (this.extraMobBehaviour.fireImmunityOverride)
                return false;
        for (ElementInfo element : getElements()) {
            if (!element.canBurn()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this mob should be damaged by the sun.
     **/
    public boolean daylightBurns() {
        return false;
    }

    /**
     * Returns true if this mob should be damaged by extreme cold such as from ooze.
     **/
    public boolean canFreeze() {
        for (ElementInfo element : getElements()) {
            if (!element.canFreeze()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this mob should be damaged by water.
     **/
    public boolean waterDamage() {
        return false;
    }

    /**
     * If true, this mob isn't slowed down by webs.
     **/
    public boolean webProof() {
        return false;
    }

    @Override
    public void makeStuckInBlock(BlockState blockState, Vec3 motionMultiplier) {
        if (blockState.getBlock() == Blocks.COBWEB && webProof()) {
            return;
        }
        if (blockState.getBlock() == ObjectManager.getBlock("quickweb") && webProof()) {
            return;
        }
        if (blockState.getBlock() == ObjectManager.getBlock("frostweb") && webProof()) {
            return;
        }
        super.makeStuckInBlock(blockState, motionMultiplier);
    }

    /**
     * Returns the amount of air gained for the tick. Drowning in water is handled by LivingEntity and this isn't called in that case.
     **/
    @Override
    protected int increaseAirSupply(int currentAir) {
        if (this.canBreatheUnderwater() && this.waterContact()) {
            return super.increaseAirSupply(currentAir);
        }
        if (this.canBreatheUnderlava() && this.lavaContact()) {
            return super.increaseAirSupply(currentAir);
        }
        if (this.canBreatheAir()) { // No drowning in lava and this method is called when eyes are not in water so no need to check twice.
            return super.increaseAirSupply(currentAir);
        }

        return this.decreaseAirSupply(currentAir);
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return super.decreaseAirSupply(air);
    }

    /**
     * If true, this creature will gain air when in contact with air.
     **/
    public boolean canBreatheAir() {
        return true;
    }

    // ========== Environmental ==========

    /**
     * If true, this creature will gain air when in contact with water.
     **/
    @Override
    public boolean canBreatheUnderwater() {
        if (this.extraMobBehaviour != null)
            if (this.extraMobBehaviour.waterBreathingOverride)
                return true;
        return false;
    }

    /**
     * If true, this creature will gain air when in contact with lava.
     **/
    public boolean canBreatheUnderlava() {
        return true;
    }

    // Breathing:

    /**
     * Sets the current amount of air this mob has.
     **/
    @Override
    public void setAirSupply(int air) {
        super.setAirSupply(air);
    }

    /**
     * Returns true if this mob is in water.
     **/
    @Override
    public boolean isInWater() {
        return super.isInWater();
    }

    /**
     * Returns true if this mob is in contact with water in any way.
     **/
    public boolean waterContact() {
        if (this.isInWaterRainOrBubble())
            return true;
        if (this.getCommandSenderWorld().isRaining() && !this.isBlockUnderground((int) this.position().x(), (int) this.position().y(), (int) this.position().z()))
            return true;
        return false;
    }

    /**
     * Returns true if this mob is in contact with lava in any water.
     **/
    public boolean lavaContact() {
        return this.isInLava();
    }

    /**
     * Returns true if the specified xyz coordinate is in water swimmable by this mob. (Checks for lava for lava creatures).
     *
     * @param x Block x position.
     * @param y Block y position.
     * @param z Block z position.
     * @return True if swimmable.
     */
    public boolean isSwimmable(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y, z));
        if (this.isLavaCreature && Material.LAVA.contains(blockState.getBlock()))
            return true;
        else if (Material.WATER.contains(blockState.getBlock()))
            return true;
        return false;
    }

    /**
     * Returns how many extra blocks this mob can fall for, the default is around 3.0F I think, if this is set to or above 100 then this mob wont receive falling damage at all.
     **/
    public float getFallResistance() {
        return 0;
    }

    /**
     * Gets the maximum fall height this creature is willing to do when pathing, varies depending on if it has an attack target, health, etc.
     **/
    @Override
    public int getMaxFallDistance() {
        return super.getMaxFallDistance() + (int) this.getFallResistance();
    }

    /**
     * Returns a light rating for the light level of this mob's current position.
     * Dark enough for spawnsInDarkness: 0 = Dark, 1 = Dim
     * Light enough for spawnsInLight: 2 = Light, 3 = Bright
     **/
    public byte testLightLevel() {
        return testLightLevel(this.blockPosition());
    }

    /**
     * Returns a light rating for the light level the specified XYZ position.
     * Dark enough for spawnsInDarkness: 0 = Dark, 1 = Dim
     * Light enough for spawnsInLight: 2 = Light, 3 = Bright
     **/
    public byte testLightLevel(BlockPos pos) {
        BlockState spawnBlockState = this.getCommandSenderWorld().getBlockState(pos);
        if (pos.getY() < 0)
            return 0;
        if (Material.WATER.contains(spawnBlockState.getBlock()) && CreatureManager.getInstance().spawnConfig.useSurfaceLightLevel)
            pos = new BlockPos(pos.getX(), this.getWaterSurfaceY(pos), pos.getZ());
        else
            pos = new BlockPos(pos.getX(), this.getGroundY(pos), pos.getZ());

        int rawLight = this.getCommandSenderWorld().getMaxLocalRawBrightness(pos);
        if (rawLight == 0) return 0;
        if (rawLight <= 8) return 1;
        if (rawLight < 15) return 2;
        return 3;
    }

    /**
     * A client and server friendly solution to check if it is daytime or not.
     **/
    public boolean isDaytime() {
        if (!this.getCommandSenderWorld().isClientSide)
            return this.getCommandSenderWorld().isDay();
        long time = this.getCommandSenderWorld().getDayTime();
        if (time < 12500)
            return true;
        if (time >= 12542 && time < 23460)
            return false;
        return true;
    }

    /**
     * Returns this creature's attribute.
     **/
    @Override
    public MobType getMobType() {
        return this.attribute;
    }

    /**
     * An X Offset used to position the mob that is riding this mob.
     **/
    public double getMountedXOffset() {
        if (this.getSubspecies() != null && this.getSubspecies().mountOffset != null) {
            return (double) this.getDimensions(Pose.STANDING).width * this.getSubspecies().mountOffset.x();
        }
        return (double) this.getDimensions(Pose.STANDING).width * this.creatureInfo.mountOffset.x();
    }


    // ==================================================
    //                     Utilities
    // ==================================================
    // ========== Get Light Type ==========

    /**
     * A Y Offset used to position the mob that is riding this mob.
     **/
    @Override
    public double getPassengersRidingOffset() {
        if (this.getSubspecies() != null && this.getSubspecies().mountOffset != null) {
            return (double) this.getDimensions(Pose.STANDING).height * this.getSubspecies().mountOffset.y();
        }
        return (double) this.getDimensions(Pose.STANDING).height * this.creatureInfo.mountOffset.y();
    }

    /**
     * A Z Offset used to position the mob that is riding this mob.
     **/
    public double getMountedZOffset() {
        if (this.getSubspecies() != null && this.getSubspecies().mountOffset != null) {
            return (double) this.getDimensions(Pose.STANDING).width * this.getSubspecies().mountOffset.z();
        }
        return (double) this.getDimensions(Pose.STANDING).width * this.creatureInfo.mountOffset.z();
    }

    /**
     * Get entities that are near this entity.
     **/
    public <T extends Entity> List<T> getNearbyEntities(Class<? extends T> clazz, Predicate<Entity> predicate, double range) {
        return (List<T>) this.getCommandSenderWorld().getEntitiesOfClass(clazz, this.getBoundingBox().inflate(range, range, range), predicate != null ? predicate : (e) -> true);
    }

    // ========== Creature Attribute ==========

    /**
     * Returns how many entities of the specified Entity Type are within the specified range, used mostly for spawning, mobs that summon other mobs and group behaviours.
     **/
    public int nearbyCreatureCount(EntityType targetType, double range) {
        return this.getNearbyEntities(Entity.class, entity -> entity.getType() == targetType, range).size();
    }

    // ========== Mounted Y Offset ==========

    /**
     * Returns how many ally creatures are within range of this creature, by default allied creatures are creatures of the same type.
     *
     * @param range The range to search in.
     * @return The number of allies within range.
     */
    public int countAllies(double range) {
        return this.getNearbyEntities(Entity.class, entity -> entity.getType() == this.getType(), range).size();
    }

    /**
     * Get the entity closest to this entity.
     **/
    public <T extends Entity> T getNearestEntity(Class<? extends T> clazz, Predicate<Entity> predicate, double range, boolean canAttack) {
        List aoeTargets = this.getNearbyEntities(clazz, predicate, range);
        if (aoeTargets.size() == 0)
            return null;
        double nearestDistance = range + 10;
        T nearestEntity = null;
        for (Object entityObj : aoeTargets) {
            T targetEntity = (T) entityObj;
            if (targetEntity == this)
                continue;
            if (!(targetEntity instanceof LivingEntity))
                continue;
            if (canAttack && !this.canAttack((LivingEntity) targetEntity))
                continue;
            if (targetEntity == this.getControllingPassenger())
                continue;
            double distance = this.distanceTo(targetEntity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestEntity = targetEntity;
            }
        }
        return nearestEntity;
    }

    /**
     * Used when loading this mob from a saved chunk.
     **/
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        if (this.creatureInfo.dummy) {
            super.readAdditionalSaveData(nbt);
            return;
        }

        if (nbt.contains("FirstSpawn")) {
            this.firstSpawn = nbt.getBoolean("FirstSpawn");
        } else {
            this.firstSpawn = true;
        }

        if (nbt.contains("SpawnEventType")) {
            this.spawnEventType = nbt.getString("SpawnEventType");
        }

        if (nbt.contains("SpawnEventCount")) {
            this.spawnEventCount = nbt.getInt("SpawnEventCount");
        }

        if (nbt.contains("Stealth")) {
            this.setStealth(nbt.getFloat("Stealth"));
        }

        if (nbt.contains("IsMinion")) {
            this.setMinion(nbt.getBoolean("IsMinion"));
        }

        if (nbt.contains("IsTemporary") && nbt.getBoolean("IsTemporary") && nbt.contains("TemporaryDuration")) {
            this.setTemporary(nbt.getInt("TemporaryDuration"));
        } else {
            this.unsetTemporary();
        }

        if (nbt.contains("IsBoundPet")) {
            if (nbt.getBoolean("IsBoundPet")) {
                if (!this.hasPetEntry()) {
                    this.boundPetOrphan = true;
                }
            }
        }

        if (nbt.contains("ForceNoDespawn")) {
            if (nbt.getBoolean("ForceNoDespawn")) {
                this.setPersistenceRequired();
            }
        }

        if (nbt.contains("Color")) {
            this.setColor(DyeColor.byId(nbt.getByte("Color")));
        }

        if (nbt.contains("Size")) {
            this.setSizeScale(nbt.getDouble("Size"));
        }

        if (!this.firstSpawn && nbt.contains("Subspecies") && !nbt.contains("Variant")) { // Convert Old Subspecies IDs:
            this.setSubspecies(Variant.getIndexFromOld(nbt.getByte("Subspecies")));
            this.setVariant(Variant.getIndexFromOld(nbt.getByte("Subspecies")));
        } else {
            if (nbt.contains("Subspecies")) {
                this.setSubspecies(nbt.getByte("Subspecies"));
            }
            if (nbt.contains("Variant")) {
                if (this.firstSpawn) {
                    this.applyVariant(nbt.getByte("Variant"));
                } else {
                    this.setVariant(nbt.getByte("Variant"));
                }
            }
        }

        if (nbt.contains("MobLevel")) {
            if (this.firstSpawn) {
                this.applyLevel(nbt.getInt("MobLevel"));
            } else {
                this.setLevel(nbt.getInt("MobLevel"));
            }
        }

        if (nbt.contains("Experience")) {
            this.setExperience(nbt.getInt("Experience"));
        }

        if (nbt.contains("SpawnedAsBoss")) {
            this.spawnedAsBoss = nbt.getBoolean("SpawnedAsBoss");
        }
        if (nbt.contains("SpawnedRare")) {
            this.spawnedRare = nbt.getBoolean("SpawnedRare");
        }

        super.readAdditionalSaveData(nbt);

        this.relationships.load(nbt);
        this.inventory.load(nbt);

        if (nbt.contains("Drops")) {
            ListTag nbtDropList = nbt.getList("Drops", 10);
            for (int i = 0; i < nbtDropList.size(); i++) {
                CompoundTag dropNBT = nbtDropList.getCompound(i);
                ItemDrop drop = new ItemDrop(dropNBT);
                this.addSavedItemDrop(drop);
            }
        }

        if (nbt.contains("DropsRequirePlayerDamage")) {
            this.dropsRequirePlayerDamage = nbt.getBoolean("DropsRequirePlayerDamage");
        }

        if (nbt.contains("ExtraBehaviour")) {
            this.extraMobBehaviour.read(nbt.getCompound("ExtraBehaviour"));
        }

        if (nbt.contains("HomeX") && nbt.contains("HomeY") && nbt.contains("HomeZ") && nbt.contains("HomeDistanceMax")) {
            this.setHome(nbt.getInt("HomeX"), nbt.getInt("HomeY"), nbt.getInt("HomeZ"), nbt.getFloat("HomeDistanceMax"));
        }

        if (nbt.contains("ArenaX") && nbt.contains("ArenaY") && nbt.contains("ArenaZ")) {
            this.setArenaCenter(new BlockPos(nbt.getInt("ArenaX"), nbt.getInt("ArenaY"), nbt.getInt("ArenaZ")));
        }

        if (nbt.contains("FixateUUIDMost") && nbt.contains("FixateUUIDLeast")) {
            this.fixateUUID = new UUID(nbt.getLong("FixateUUIDMost"), nbt.getLong("FixateUUIDLeast"));
        }

        if (nbt.contains("MinionIds")) {
            ListTag minionIds = nbt.getList("MinionIds", 10);
            for (int i = 0; i < minionIds.size(); i++) {
                CompoundTag minionId = minionIds.getCompound(i);
                if (minionId.contains("ID")) {
                    Entity entity = this.getCommandSenderWorld().getEntity(minionId.getInt("ID"));
                    if (entity instanceof LivingEntity)
                        this.addMinion((LivingEntity) entity);
                }
            }
        }
    }

    /**
     * Used when saving this mob to a chunk.
     **/
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        if (this.creatureInfo.dummy) {
            super.addAdditionalSaveData(nbt);
            return;
        }

        nbt.putBoolean("FirstSpawn", this.firstSpawn);
        nbt.putString("SpawnEventType", this.spawnEventType);
        nbt.putInt("SpawnEventCount", this.spawnEventCount);

        nbt.putFloat("Stealth", this.getStealth());
        nbt.putBoolean("IsMinion", this.isMinion());
        nbt.putBoolean("IsTemporary", this.isTemporary);
        nbt.putInt("TemporaryDuration", this.temporaryDuration);
        nbt.putBoolean("IsBoundPet", this.isBoundPet());
        nbt.putBoolean("ForceNoDespawn", this.forceNoDespawn);
        nbt.putByte("Color", (byte) this.getColor().getId());
        nbt.putByte("Subspecies", (byte) this.getSubspeciesIndex());
        nbt.putByte("Variant", (byte) this.getVariantIndex());
        nbt.putDouble("Size", this.sizeScale);
        nbt.putInt("MobLevel", this.getMobLevel());
        nbt.putInt("Experience", this.getExperience());
        nbt.putBoolean("SpawnedAsBoss", this.spawnedAsBoss);
        nbt.putBoolean("SpawnedRare", this.spawnedRare);

        if (this.hasHome()) {
            BlockPos homePos = this.getRestrictCenter();
            nbt.putInt("HomeX", homePos.getX());
            nbt.putInt("HomeY", homePos.getY());
            nbt.putInt("HomeZ", homePos.getZ());
            nbt.putFloat("HomeDistanceMax", this.getHomeDistanceMax());
        }

        if (this.hasArenaCenter()) {
            BlockPos arenaPos = this.getArenaCenter();
            nbt.putInt("ArenaX", arenaPos.getX());
            nbt.putInt("ArenaY", arenaPos.getY());
            nbt.putInt("ArenaZ", arenaPos.getZ());
        }

        if (this.getFixateTarget() != null) {
            nbt.putLong("FixateUUIDMost", this.getFixateTarget().getUUID().getMostSignificantBits());
            nbt.putLong("FixateUUIDLeast", this.getFixateTarget().getUUID().getLeastSignificantBits());
        }

        try {
            this.getAttributes().save();
        } catch (Throwable t) {
            LMHelperClass.logError(t.getMessage());
            t.printStackTrace();
        }

        super.addAdditionalSaveData(nbt);

        this.relationships.save(nbt);
        this.inventory.save(nbt);

        ListTag nbtDropList = new ListTag();
        for (ItemDrop drop : this.savedDrops) {
            CompoundTag dropNBT = new CompoundTag();
            if (drop.writeToNBT(dropNBT)) {
                nbtDropList.add(dropNBT);
            }
        }
        nbt.put("Drops", nbtDropList);
        nbt.putBoolean("DropsRequirePlayerDamage", this.dropsRequirePlayerDamage);

        CompoundTag extTagCompound = new CompoundTag();
        this.extraMobBehaviour.write(extTagCompound);
        nbt.put("ExtraBehaviour", extTagCompound);

        ListTag minionIds = new ListTag();
        for (LivingEntity minion : this.minions) {
            CompoundTag minionId = new CompoundTag();
            minionId.putInt("ID", minion.getId());
            minionIds.add(minionId);
        }
        nbt.put("MinionIds", minionIds);
    }

    /**
     * Returns the current basic attack cooldown.
     **/
    public int getAttackCooldown() {
        return this.attackCooldown;
    }

    /**
     * Returns true if this creature should play it's attack animation.
     **/
    public boolean isAttackOnCooldown() {
        return this.getAttackCooldown() > 0;
    }

    /**
     * Usually called when this mob has just attacked but can be called from other things, puts the attack cooldown to the max where it will start counting down. Clients use this for animation.
     **/
    public void triggerAttackCooldown() {
        this.attackCooldown = this.getAttackCooldownMax();
    }

    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========

    /**
     * Resets the attack cooldown.
     **/
    public void resetAttackCooldown() {
        this.attackCooldown = 0;
        this.setAttackCooldownMax(this.getAttackCooldownMax());
    }

    // ========== Write ==========

    /**
     * Returns the current maximum attack cooldown.
     **/
    public int getAttackCooldownMax() {
        if (!this.getCommandSenderWorld().isClientSide) {
            return this.attackCooldownMax;
        } else {
            return this.getIntFromDataManager(ANIMATION_ATTACK_COOLDOWN_MAX);
        }
    }


    // ==================================================
    //                       Client
    // ==================================================
    // ========== Just Attacked Animation ==========

    /**
     * Sets the current maximum attack cooldown. This will send a messag from server to client to keep the client in sync.
     **/
    public void setAttackCooldownMax(int cooldownMax) {
        this.attackCooldownMax = cooldownMax;
        if (!this.getCommandSenderWorld().isClientSide) {
            this.getEntityData().set(ANIMATION_ATTACK_COOLDOWN_MAX, this.attackCooldownMax);
        }
    }

    /**
     * Returns this creature's main texture. Also checks for for subspecies.
     **/
    public ResourceLocation getTexture() {
        return this.getTexture("");
    }

    /**
     * Returns this creature's main texture. Also checks for for subspecies.
     **/
    public ResourceLocation getTexture(String suffix) {
        String textureName = this.getTextureName();
        if (this.getSubspecies().name != null) {
            textureName += "_" + this.getSubspecies().name;
        }
        if (this.getVariant() != null) {
            textureName += "_" + this.getVariant().color;
        }
        if (!"".equals(suffix)) {
            textureName += "_" + suffix;
        }
        if (TextureManager.getTexture(textureName) == null)
            TextureManager.addTexture(textureName, "textures/entity/" + textureName.toLowerCase() + ".png");
        return TextureManager.getTexture(textureName);
    }

    /**
     * Returns this creature's equipment texture.
     **/
    public ResourceLocation getEquipmentTexture(String equipmentName) {
        if (!this.canEquip())
            return this.getTexture();
        if (this.getSubspecies() != null && this.getSubspecies().name != null) {
            equipmentName = this.getSubspecies().name + "_" + equipmentName;
        }
        return this.getSubTexture(equipmentName);
    }

    /**
     * Returns this creature's equipment texture.
     **/
    public ResourceLocation getSubTexture(String subName) {
        subName = subName.toLowerCase();
        String textureName = this.getTextureName();
        textureName += "_" + subName;
        if (TextureManager.getTexture(textureName) == null)
            TextureManager.addTexture(textureName, "textures/entity/" + textureName.toLowerCase() + ".png");
        return TextureManager.getTexture(textureName);
    }

    /**
     * Gets the name of this creature's texture, normally links to it's code name but can be overridden by subspecies and alpha creatures.
     **/
    public String getTextureName() {
        return this.creatureInfo.getName();
    }


    // ==================================================
    //                       Visuals
    // ==================================================

    /**
     * Returns true if this mob can be dyed different colors. Usually for wool and collars.
     *
     * @param player The player to check for when coloring, this is to stop players from dying other players pets. If provided with null it should return if this creature can be dyed in general.
     * @return True if tis entity can be dyed by the player or if the player is null, if it can be dyed at all (null is passed by the renderer).
     */
    public boolean canBeColored(Player player) {
        return false;
    }

    /**
     * Gets the color ID of this mob.
     *
     * @return A color ID that is used by the static RenderCreature.colorTable array.
     */
    public DyeColor getColor() {
        int colorId = this.getByteFromDataManager(COLOR) & 15;
        return DyeColor.byId(colorId);
    }

    /**
     * Sets the color ID of this mob.
     *
     * @param color The color ID to use (see the static RenderCreature.colorTable array).
     */
    public void setColor(DyeColor color) {
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(COLOR, (byte) (color.getId() & 15));
        }
    }

    // ========== Boss Info ==========
    public boolean showBossInfo() {
        if (this.forceBossHealthBar || this.isBoss())
            return true;
        // Rare subspecies health bar:
        if (this.isRareVariant())
            return Variant.RARE_HEALTH_BARS;
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.getBossInfo() != null)
            this.bossInfo.addPlayer(player);
    }


    // ========== Coloring ==========

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (this.getBossInfo() != null)
            this.bossInfo.removePlayer(player);
    }

    /**
     * Returns the volume of this entity.
     **/
    @Override
    protected float getSoundVolume() {
        if (this.isBoss())
            return 4.0F;
        if (this.isRareVariant())
            return 2.0F;
        return 1.0F;
    }

    /**
     * Returns the name to use for sound assets.
     **/
    public String getSoundName() {
        String soundSuffix = "";
        if (this.getSubspecies() != null && this.getSubspecies().name != null) {
            soundSuffix += "." + this.getSubspecies().name;
        }
        return this.creatureInfo.getName() + soundSuffix;
    }

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     **/
    @Override
    public int getAmbientSoundInterval() {
        return CreatureManager.getInstance().config.idleSoundTicks;
    }

    /**
     * Returns the sound to play when this creature is making a random ambient roar, grunt, etc.
     **/
    @Override
    protected SoundEvent getAmbientSound() {
        return ObjectManager.getSound(this.getSoundName() + "_say");
    }

    /**
     * Returns the sound to play when this creature is damaged.
     **/
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ObjectManager.getSound(this.getSoundName() + "_hurt");
    }


    // ==================================================
    //                       Sounds
    // ==================================================

    /**
     * Returns the sound to play when this creature dies.
     **/
    @Override
    protected SoundEvent getDeathSound() {
        return ObjectManager.getSound(this.getSoundName() + "_death");
    }

    /**
     * Plays an additional footstep sound that this creature makes when moving on the ground (all mobs use the block's stepping sounds by default).
     **/
    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        if (this.isFlying())
            return;
        if (!this.hasStepSound) {
            super.playStepSound(pos, block);
            return;
        }
        this.playSound(ObjectManager.getSound(this.getSoundName() + "_step"), this.getSoundVolume(), 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // ========== Idle ==========

    @Override
    public Fallsounds getFallSounds() {
        return new Fallsounds(SoundEvents.HOSTILE_BIG_FALL, SoundEvents.HOSTILE_SMALL_FALL);
    }

    // ========== Swim ==========
    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.HOSTILE_SWIM;
    }

    // ========== Hurt ==========

    // ========== Splash ==========
    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.HOSTILE_SPLASH;
    }

    // ========== Death ==========

    /**
     * Plays the jump sound when this creature jumps.
     **/
    public void playJumpSound() {
        if (!this.hasJumpSound) return;
        this.playSound(ObjectManager.getSound(this.getSoundName() + "_jump"), this.getSoundVolume(), 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // ========== Step ==========

    /**
     * Plays a flying sound, usually a wing flap, called randomly when flying.
     **/
    public void playFlySound() {
        if (!this.isFlying() || this.hasPerchTarget()) return;
        this.playSound(ObjectManager.getSound(this.getSoundName() + "_fly"), this.getSoundVolume(), 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // ========== Fall ==========
    /*@Override
    protected SoundEvent getFallDamageSound(int height) {

    }*/

    /**
     * Plays an attack sound, called once this creature has attacked. note that ranged attacks normally rely on the projectiles playing their launched sound instead.
     **/
    public void playAttackSound() {
        if (!this.hasAttackSound) return;
        this.playSound(ObjectManager.getSound(this.getSoundName() + "_attack"), this.getSoundVolume(), 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    public float getMeleeAttackAnim(float pt) {
        int max = Math.max(1, this.getAttackCooldownMax());
        float cur = this.attackCooldown;
        float t = (max - (cur - pt)) / (float) max;
        return Mth.clamp(t, 0F, 1F);
    }


    /**
     * Plays a sound for when this mob changes battle phase, normally used by bosses.
     **/
    public void playPhaseSound() {
        if (ObjectManager.getSound(this.creatureInfo.getName() + "_phase") == null)
            return;
        this.playSound(ObjectManager.getSound(this.getSoundName() + "_phase"), this.getSoundVolume() * 2, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // ========== Play Sound ==========
    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (sound == null) {
            LMHelperClass.logErrorMessageOnce("Null Sound trying to be played by: " + this.getType());
            return;
        }
        super.playSound(sound, volume, pitch);
    }

    // ========== Jump ==========

    @Override
    public float getFlyingSpeed() {
        return flyingSpeed;
    }

    // ========== Fly ==========

    public void setFlyingSpeed(float speed) {
        flyingSpeed = speed;
    }

    // ========== Attack ==========

    /**
     * Used for the TARGET watcher bitmap, bitmaps save on many packets and make network performance better!
     **/
    public enum TARGET_BITS {
        ATTACK((byte) 1), MASTER((byte) 2), PARENT((byte) 4), AVOID((byte) 8), RIDER((byte) 16), PICKUP((byte) 32), PERCH((byte) 64);
        public final byte id;

        TARGET_BITS(byte value) {
            this.id = value;
        }

        public byte getValue() {
            return id;
        }
    }

    // ========== Phase ==========

    /**
     * Used for the ANIMATION_STATE watcher bitmap, bitmaps save on many packets and make network performance better!
     **/
    public enum ANIMATION_STATE_BITS {
        ATTACKED((byte) 1), GROUNDED((byte) 2), IN_WATER((byte) 4), BLOCKING((byte) 8), MINION((byte) 16), EXTRA01((byte) 32), BOSS((byte) 64);
        public final byte id;

        ANIMATION_STATE_BITS(byte value) {
            this.id = value;
        }

        public byte getValue() {
            return id;
        }
    }

    /**
     * Used by AI Goals to determine target types.
     **/
    public enum TARGET_TYPES {
        ENEMY((byte) 1), ALLY((byte) 2), SELF((byte) 4);
        public final byte id;

        TARGET_TYPES(byte value) {
            this.id = value;
        }

        public byte getValue() {
            return id;
        }
    }


    /**
     * Used for the tidier interact code, these are used for right click commands to determine which one is more important. The lower the priority number the higher the priority is.
     **/
    public enum COMMAND_PIORITIES {
        OVERRIDE(0), IMPORTANT(1), EQUIPPING(2), ITEM_USE(3), EMPTY_HAND(4), MAIN(5);
        public final int id;

        COMMAND_PIORITIES(int value) {
            this.id = value;
        }

        public int getValue() {
            return id;
        }
    }

    /**
     * A list of GUI command IDs to be used by pet or creature GUIs via a network packet.
     **/
    public enum GUI_COMMAND {
        CLOSE((byte) 0), SITTING((byte) 1), FOLLOWING((byte) 2), PASSIVE((byte) 3), STANCE((byte) 4), PVP((byte) 5), TELEPORT((byte) 6), SPAWNING((byte) 7), RELEASE((byte) 8);
        public byte id;

        GUI_COMMAND(byte i) {
            id = i;
        }
    }

    ;

    /**
     * A list of pet command IDs to be used by pet or creature GUIs via a network packet.
     **/
    public enum PET_COMMAND_ID {
        ACTIVE((byte) 0), TELEPORT((byte) 1), PVP((byte) 2), RELEASE((byte) 3), PASSIVE((byte) 4), DEFENSIVE((byte) 5), ASSIST((byte) 6), AGGRESSIVE((byte) 7), FOLLOW((byte) 8), WANDER((byte) 9), SIT((byte) 10), FLEE((byte) 11);
        public byte id;

        PET_COMMAND_ID(byte i) {
            id = i;
        }
    }

    // ==================================================
    //                  Group Data
    // ==================================================
    public class GroupData implements SpawnGroupData {
        public boolean isChild;

        public GroupData(boolean child) {
            this.isChild = child;
        }
    }
}