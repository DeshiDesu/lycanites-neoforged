package com.lycanitesmobs.core.entity.base;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

public class BaseProjectileEntity extends ThrowableProjectile {
    public String entityName = "projectile";
    public ModInfo modInfo;
    public long updateTick;
    protected Entity owner;

    // Properties:
    public boolean movement = true;

    // Stats:
    protected float projectileScale = 1F;
    public int projectileLife = 200;
    public int damage = 1;
    public int pierce = 1;
    public double weight = 1.0D;
    public double knockbackChance = 1;
    public int bonusDamage = 0;

    // Flags:
    public boolean waterProof = false;
    public boolean lavaProof = false;
    public boolean cutsGrass = false;
    public boolean ripper = false;
    public boolean pierceBlocks = false;

    // Texture and Animation:
    public int animationFrame = 0;
    public int animationFrameMax = 0;
    public int textureTiling = 1;
    public float textureScale = 1;
    public float textureOffsetY = 0;
    public boolean clientOnly = false;
    public float rollSpeed = 0;

    // Data Manager:
    protected static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(BaseProjectileEntity.class, EntityDataSerializers.FLOAT);


    // ==================================================
    //			    Constructors
    // ==================================================
    public BaseProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
        this.setup();
    }

    public BaseProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityLiving) {
        this(entityType, world);
        this.setPos(entityLiving.position().x(), entityLiving.position().y() + entityLiving.getEyeHeight(), entityLiving.position().z());
        this.shootFromRotation(entityLiving, entityLiving.xRotO, entityLiving.yRotO, 0.0F, 1.1F, 1.0F); // Shoot from entity
        this.setOwner(entityLiving);
        this.setup();
    }

    public BaseProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, double x, double y, double z) {
        this(entityType, world);
        this.setPos(x, y, z);
        this.setup();
    }

    @Override
    public EntityType getType() {
        if (ProjectileManager.getInstance().oldProjectileTypes.get(this.getClass()) == null) {
            return super.getType();
        }
        return ProjectileManager.getInstance().oldProjectileTypes.get(this.getClass());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    // ========== Setup Projectile ==========
    public void setup() {

    }

    public String getStringFromDataManager(EntityDataAccessor<String> key) {
        try {
            return this.getEntityData().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(SCALE, this.projectileScale);
        this.setProjectileScale(this.projectileScale);
    }

    @Override
    public void setOwner(Entity entity) {
        super.setOwner(entity);
        this.owner = entity;
    }

    public Entity getShooter() {
        return this.owner;
    }

    public LivingEntity getOwner() {
        if (this.owner instanceof LivingEntity) {
            return (LivingEntity) this.owner;
        }
        return null;
    }


    // ==================================================
    //				        Update
    // ==================================================
    @Override
    public void tick() {
        this.updateTick++;

        if (!this.movement) {
            this.setOnGround(false);
            this.portalTime = this.getDimensionChangingDelay();
        }
        double initX = this.position().x();
        double initY = this.position().y();
        double initZ = this.position().z();

        super.tick();

        if (!this.movement) {
            this.setPos(initX, initY, initZ);
            this.setDeltaMovement(0, 0, 0);
            this.setPos(this.position().x(), this.position().y(), this.position().z());
        }

        // Terrain Destruction
        if (!this.getCommandSenderWorld().isClientSide || this.clientOnly) {
            if (!this.waterProof && this.isInWater())
                this.remove(RemovalReason.DISCARDED);
            else if (!this.lavaProof && this.isInLava())
                this.remove(RemovalReason.DISCARDED);
        }

        // Life Timeout:
        if (!this.getCommandSenderWorld().isClientSide || this.clientOnly) {
            if (this.projectileLife-- <= 0) {
                this.remove(RemovalReason.DISCARDED);
            }
        }

        // Sync Scale:
        if (this.getCommandSenderWorld().isClientSide) {
            float updatedScale = this.entityData.get(SCALE);
            if (this.projectileScale != updatedScale) {
                this.refreshDimensions();
                this.projectileScale = updatedScale;
            }
        }

        // Animation:
        if (this.animationFrameMax > 0) {
            if (this.animationFrame == this.animationFrameMax || this.animationFrame < 0)
                this.animationFrame = 0;
            else
                this.animationFrame++;
        }
    }

    /**
     * This is an expensive check for when there are a lot of projectiles. The isLavaProof death check is checked on impact instead for significantly greater performance.
     *
     * @return Always false for performance.
     */
    @Override
    public boolean isInLava() {
        return false;
    }


    // ==================================================
    //				  Movement
    // ==================================================
    // ========== Gravity ==========
    @Override
    protected float getGravity() {
        return (float) this.weight * 0.03F;
    }

    @Override
    public boolean isPushedByFluid() {
        if (this.waterProof || this.lavaProof) {
            return false;
        }
        return super.isPushedByFluid();
    }


    // ==================================================
    //                     Impact
    // ==================================================
    @Override
    protected void onHit(HitResult rayTraceResult) {
        if (this.getCommandSenderWorld().isClientSide)
            return;
        boolean collided = false;
        boolean entityCollision = false;
        boolean doDamage = true;
        boolean blockCollision = false;
        BlockPos impactPos = new BlockPos(LMHelperClass.convertToVec3i(this.position()));

        // Entity Hit:
        Entity entityHit = null;
        if (rayTraceResult.getType() == HitResult.Type.ENTITY && rayTraceResult instanceof EntityHitResult) {
            entityHit = ((EntityHitResult) rayTraceResult).getEntity();
        }
        if (entityHit != null) {
            if (this.getOwner() != null) {
                if (entityHit == this.getOwner()) {
                    return;
                }
            }
            if (entityHit instanceof LivingEntity) {
                doDamage = this.canDamage((LivingEntity) entityHit);
            }
            if (!this.getCommandSenderWorld().isClientSide) {
                if (this.getOwner() == null || entityHit != this.getOwner()) {
                    this.onEntityCollision(entityHit);
                }
            }
            if (doDamage) {
                if (entityHit instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) entityHit;
                    boolean attackSuccess;
                    float damage = this.getDamage(target);

                    if (damage != 0) {
                        float damageInit = damage;

                        // Prevent Knockback:
                        double targetKnockbackResistance = 0;
                        boolean stopKnockback = false;
                        if (this.knockbackChance < 1) {
                            if (this.knockbackChance <= 0 || this.random.nextDouble() <= this.knockbackChance) {
                                if (target instanceof LivingEntity) {
                                    targetKnockbackResistance = target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
                                    target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
                                    stopKnockback = true;
                                }
                            }
                        }

                        // Deal Damage:
                        if (this.getOwner() instanceof BaseCreatureEntity) {
                            BaseCreatureEntity creatureThrower = (BaseCreatureEntity) this.getOwner();
                            attackSuccess = creatureThrower.doRangedDamage(target, this, damage, this.isBlockedByEntity(target));
                        } else {
                            double pierceDamage = this.pierce;
                            if (damage <= pierceDamage)
                                attackSuccess = target.hurt(this.level().damageSources().magic(), damage);
                            else {
                                int hurtResistantTimeBefore = target.invulnerableTime;
                                target.hurt(this.level().damageSources().magic(), (float) pierceDamage);
                                target.invulnerableTime = hurtResistantTimeBefore;
                                damage -= pierceDamage;
                                attackSuccess = target.hurt(this.level().damageSources().thrown(this, this.getOwner()), damage);
                            }
                        }

                        // Apply Damage Effects If Not Blocking:
                        this.onEntityLivingDamage(target); // Old Projectiles
                        this.onDamage(target, damageInit, attackSuccess); // JSON Projectiles

                        // Restore Knockback:
                        if (stopKnockback) {
                            target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);
                        }
                    }
                }
            }
            collided = true;
            entityCollision = true;

            impactPos = entityHit.blockPosition();
            if (!this.getCommandSenderWorld().isClientSide && this.canDestroyBlock(impactPos)) {
                try {
                    this.placeBlock(this.getCommandSenderWorld(), impactPos);
                } catch (Exception e) {
                }
            }
        }

        // Block Hit:
        else if (rayTraceResult.getType() == HitResult.Type.BLOCK && rayTraceResult instanceof BlockHitResult blockRayTraceResult) {
            impactPos = blockRayTraceResult.getBlockPos();
            BlockState blockState = this.getCommandSenderWorld().getBlockState(impactPos);
            if (blockState.getBlock() instanceof TallGrassBlock || blockState.getBlock() == Blocks.TALL_GRASS) {
                if (this.cutsGrass) {
                    level().destroyBlock(impactPos, false);
                }
            } else {
                collided = blockState.isSolid();
                if (!this.waterProof && blockState.getBlock() == Blocks.WATER) {
                    collided = true;
                }
                if (!this.lavaProof && blockState.getBlock() == Blocks.LAVA) {
                    collided = true;
                }
            }

            if (collided) {
                blockCollision = true;
 			  /*switch(rayTraceResult.sideHit) { TODO Block Collision Side
				case DOWN:
 					 --j;
 					 break;
				case UP:
 					 ++j;
 					 break;
				case SOUTH:
 					 --k;
 					 break;
				case NORTH:
 					 ++k;
 					 break;
				case WEST:
 					 --i;
 					 break;
				case EAST:
 					 ++i;
 			  }*/

                if (!this.getCommandSenderWorld().isClientSide && this.canDestroyBlock(impactPos.above())) {
                    try {
                        this.placeBlock(this.getCommandSenderWorld(), impactPos.above());
                    } catch (Exception e) {
                    }
                }
            }
        }

        if (collided && (!entityCollision || doDamage)) {
            // Impact Particles:
            if (!this.getCommandSenderWorld().isClientSide) {
                this.onImpactComplete(impactPos);
            } else {
                this.onImpactVisuals();
            }

            // Remove Projectile:
            boolean entityPierced = this.ripper && entityCollision;
            boolean blockPierced = this.pierceBlocks && blockCollision;
            if ((!this.getCommandSenderWorld().isClientSide || this.clientOnly) && !entityPierced && !blockPierced) {
                this.remove(RemovalReason.DISCARDED);
                if (this.getImpactSound() != null) {
                    this.playSound(this.getImpactSound(), 1.0F, 1.0F / (this.getCommandSenderWorld().random.nextFloat() * 0.4F + 0.8F));
                }
            }
        }
    }

    /**
     * Determines if the provided entity is blocking this projectile, takes into account where players are looking also.
     *
     * @param targetEntity The entity to check.
     * @return True if this projectile is being blocked by the entity.
     */
    public boolean isBlockedByEntity(Entity targetEntity) {
        if (!(targetEntity instanceof LivingEntity)) {
            return false;
        }
        LivingEntity targetLiving = (LivingEntity) targetEntity;
        if (!targetLiving.isBlocking()) {
            return false;
        }

        // Check Player Blocking Angle:
        if (targetLiving instanceof Player) {
            Vec3 targetViewVector = targetLiving.getViewVector(1.0F).normalize();
            Vec3 distance = new Vec3(this.getX() - targetLiving.getX(), this.getEyeY() - targetLiving.getEyeY(), this.getZ() - targetLiving.getZ());
            double distanceStraight = distance.length();
            distance = distance.normalize();
            double lookDistance = targetViewVector.dot(distance);
            double lookRange = 1.5D;
            double comparison = 1.0D - (lookRange / distanceStraight);
            return lookDistance > comparison && targetLiving.hasLineOfSight(this);
        }

        return true;
    }

    //========== Do Damage Check ==========
    public boolean canDamage(LivingEntity targetEntity) {
        if (this.getCommandSenderWorld().isClientSide)
            return false;

        LivingEntity owner = this.getOwner();
        if (owner != null) {
            if (owner instanceof BaseCreatureEntity) {
                BaseCreatureEntity ownerCreature = (BaseCreatureEntity) owner;
                if (!ownerCreature.canAttack(targetEntity)) {
                    return false;
                }
            }

            // Player Damage Event:
            if (owner instanceof Player) {
                if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent((Player) owner, targetEntity))) {
                    return false;
                }
                if (targetEntity instanceof TameableCreatureEntity) {
                    TameableCreatureEntity targetCreature = (TameableCreatureEntity) targetEntity;
                    if (targetCreature.getPlayerOwner() == owner)
                        return false;
                }
            }

            // Player PVP:
            if (!this.getCommandSenderWorld().getServer().isPvpAllowed()) {
                if (owner instanceof Player) {
                    if (targetEntity instanceof Player) {
                        return false;
                    }
                    if (targetEntity instanceof TameableCreatureEntity) {
                        TameableCreatureEntity tamedTarget = (TameableCreatureEntity) targetEntity;
                        if (tamedTarget.isTamed()) {
                            return false;
                        }
                    }
                }
            }

            // Friendly Fire:
            if (owner.isAlliedTo(targetEntity) && CreatureManager.getInstance().config.friendlyFire) {
                return false;
            }
        }

        return true;
    }


    /**
     * Called when this projectile damages an entity (successfully or on failure).
     *
     * @param target        The entity damaged.
     * @param damage        The full amount of damage that was meant to be dealt.
     * @param attackSuccess True if the entity was damaged, false if it wasn't.
     */
    public void onDamage(LivingEntity target, float damage, boolean attackSuccess) {
    }

    //========== Entity Collision ==========
    public void onEntityCollision(Entity entity) {
    }

    //========== Entity Living Damage ==========
    public boolean onEntityLivingDamage(LivingEntity entityLiving) {
        return true;
    }

    //========== Can Destroy Block ==========
    public boolean canDestroyBlock(BlockPos pos) {
        return this.getCommandSenderWorld().getBlockState(pos).canBeReplaced(Fluids.WATER);
    }

    //========== Place Block ==========
    public void placeBlock(Level world, BlockPos pos) {
        //world.setBlock(pos, ObjectManager.getBlock("BlockName").blockID);
    }

    //========== On Impact Splash/Ricochet Server Side ==========
    public void onImpactComplete(BlockPos impactPos) {
    }

    //========== On Impact Particles/Sounds Client Side ==========
    public void onImpactVisuals() {
        //for(int i = 0; i < 8; ++i)
        //this.getEntityWorld().addParticle("particlename", this.getPositionVec().getX(), this.getPositionVec().getY(), this.getPositionVec().getZ(), 0.0D, 0.0D, 0.0D);
    }


    // ==================================================
    //				Collision
    // ==================================================
    @Override
    public boolean isPickable() {
        return false;
    }


    // ==================================================
    //				 Attacked
    // ==================================================
    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        return false;
    }


    // ==================================================
    //				  Scale
    // ==================================================
    public void setProjectileScale(float scale) {
        this.projectileScale = scale;
        if (this.getCommandSenderWorld().isClientSide && !this.clientOnly)
            return;
        if (this.getOwner() != null && this.getOwner() instanceof BaseCreatureEntity) {
            this.projectileScale *= this.getOwner().getScale();
        }
        this.entityData.set(SCALE, this.getProjectileScale());
        this.refreshDimensions();
    }

    public float getProjectileScale() {
        return this.projectileScale;
    }

    @Override
    @Nonnull
    public EntityDimensions getDimensions(Pose pose) {
        return this.getType().getDimensions().scale(this.getProjectileScale());
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling();
    }

    public float getTextureOffsetY() {
        return 0;
    }


    // ==================================================
    //				  Damage
    // ==================================================
    public void setDamage(int damage) {
        this.damage = damage;
    }

    public float getDamage(Entity entity) {
        float damage = (float) this.damage + this.bonusDamage;
        if (this.getOwner() != null) {
            // 20% Extra Damage From Players vs Entities
            if ((this.getOwner() instanceof Player || this.getOwner().getControllingPassenger() instanceof Player) && !(entity instanceof Player))
                damage *= 1.2f;
        }
        return damage;
    }

    public void setPierce(int pierce) {
        this.pierce = pierce;
    }

    public int getPierce() {
        return this.pierce;
    }

    /**
     * When given a base time (in seconds) this will return the scaled time with difficulty and other modifiers taken into account
     * seconds - The base duration in seconds that this effect should last for.
     **/
    public int getEffectDuration(int seconds) {
        if (this.getOwner() != null && this.getOwner() instanceof BaseCreatureEntity)
            return Math.round((float) ((BaseCreatureEntity) this.getOwner()).getEffectDuration(seconds) / 5);
        return seconds * 20;
    }

    /**
     * When given a base effect strength value such as a life drain amount, this will return the scaled value with difficulty and other modifiers taken into account
     * value - The base effect strength.
     **/
    public float getEffectStrength(float value) {
        if (this.getOwner() != null && this.getOwner() instanceof BaseCreatureEntity)
            return ((BaseCreatureEntity) this.getOwner()).getEffectStrength(value);
        return value;
    }

    /**
     * Sets additional damage to be dealt by this projectile on top of it's base damage.
     */
    public void setBonusDamage(int bonusDamage) {
        this.bonusDamage = bonusDamage;
    }


    // ==================================================
    //				  Utility
    // ==================================================
    // ========== Get Facing Coords ==========

    /**
     * Returns the XYZ coordinate in front or behind this entity (using its rotation angle) with the given distance, use a negative distance for behind.
     **/
    public double[] getFacingPosition(double distance) {
        return this.getFacingPosition(this, distance, 0D);
    }

    /**
     * Returns the XYZ coordinate in front or behind the provided entity with the given distance and angle offset (in degrees), use a negative distance for behind.
     **/
    public double[] getFacingPosition(Entity entity, double distance, double angleOffset) {
        double angle = Math.toRadians(entity.yRotO) + angleOffset;
        double xAmount = -Math.sin(angle);
        double zAmount = Math.cos(angle);
        double[] coords = new double[3];
        coords[0] = entity.position().x() + (distance * xAmount);
        coords[1] = entity.position().y();
        coords[2] = entity.position().z() + (distance * zAmount);
        return coords;
    }


    // ==================================================
    //				   NBT
    // ==================================================
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putFloat("ProjectileScale", this.projectileScale);
        compound.putInt("ProjectileLife", this.projectileLife);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("ProjectileScale")) {
            this.setProjectileScale(compound.getFloat("ProjectileScale"));
        }
        if (compound.contains("ProjectileLife")) {
            this.projectileLife = compound.getInt("ProjectileLife");
        }
    }

    public float getBrightness() {
        return LMHelperClass.getBrightness(this);
    }

    // ==================================================
    //				  Visuals
    // ==================================================
    public String getTextureName() {
        return this.entityName.toLowerCase();
    }

    public String subTypeString() {
        return "charges";
    }

    public ResourceLocation getTexture() {
        if (TextureManager.getTexture(this.getTextureName()) == null)
            TextureManager.addTexture(this.getTextureName(), "textures/item/" + subTypeString() + "/" + this.getTextureName() + ".png");
        return TextureManager.getTexture(this.getTextureName());
    }


    // ==================================================
    //				  Sounds
    // ==================================================
    public SoundEvent getLaunchSound() {
        return ObjectManager.getSound(this.entityName);
    }

    public SoundEvent getImpactSound() {
        return ObjectManager.getSound(this.entityName + "_impact");
    }

    public SoundEvent getBeamSound() {
        return ObjectManager.getSound(this.entityName);
    }
}
