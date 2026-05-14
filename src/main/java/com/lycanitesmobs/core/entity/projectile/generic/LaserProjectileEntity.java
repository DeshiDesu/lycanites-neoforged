package com.lycanitesmobs.core.entity.projectile.generic;

import com.lycanitesmobs.core.entity.projectile.misc.LaserEndProjectileEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.util.HashSet;

public class LaserProjectileEntity extends BaseProjectileEntity {
    // Properties:
    public LivingEntity shootingEntity;
    /**
     * The entity that this laser should appear from.
     **/
    public Entity followEntity;
    public int shootingEntityRef = -1;
    public int shootingEntityID = 11;

    public float projectileWidth = 0.2f;
    public float projectileHeight = 0.2f;

    // Laser:
    public LaserEndProjectileEntity laserEnd;
    public int laserEndRef = -1;
    public int laserEndID = 12;

    public int laserTime = 100;
    public int laserDelay = 20;
    public float laserRange;
    public float laserWidth;
    public float laserLength = 10;
    public int laserTimeID = 13;

    // Laser End:
    /**
     * If true, this entity will use the attack target position of the entity that has fired this if possible.
     **/
    public boolean useEntityAttackTarget = true;
    private double targetX;
    private double targetY;
    private double targetZ;

    // Offsets:
    public double offsetX = 0;
    public double offsetY = 0;
    public double offsetZ = 0;
    public int offsetIDStart = 14;

    // Data Parameters:
    protected static final EntityDataAccessor<Integer> SHOOTING_ENTITY_ID = SynchedEntityData.defineId(LaserProjectileEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> LASER_END_ID = SynchedEntityData.defineId(LaserProjectileEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> LASER_TIME = SynchedEntityData.defineId(LaserProjectileEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> OFFSET_X = SynchedEntityData.defineId(LaserProjectileEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> OFFSET_Y = SynchedEntityData.defineId(LaserProjectileEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> OFFSET_Z = SynchedEntityData.defineId(LaserProjectileEntity.class, EntityDataSerializers.FLOAT);

    // ==================================================
    //                   Constructors
    // ==================================================
    public LaserProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
        this.setStats();
        this.setTime(0);
    }

    public LaserProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6, int setTime, int setDelay) {
        super(entityType, world, par2, par4, par6);
        this.laserTime = setTime;
        this.laserDelay = setDelay;
        this.setStats();
    }

    public LaserProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6, int setTime, int setDelay, Entity followEntity) {
        this(entityType, world, par2, par4, par6, setTime, setDelay);
        this.followEntity = followEntity;
    }

    public LaserProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity par2LivingEntity, int setTime, int setDelay) {
        this(entityType, world, par2LivingEntity, setTime, setDelay, null);
    }

    public LaserProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityLiving, int setTime, int setDelay, Entity followEntity) {
        super(entityType, world, entityLiving);
        this.shootingEntity = entityLiving;
        this.laserTime = setTime;
        this.laserDelay = setDelay;
        this.setStats();
        this.followEntity = followEntity;
        this.syncOffset();
    }

    public void setStats() {
        //this.setSize(projectileWidth, projectileHeight);
        this.setRange(16.0F);
        this.setLaserWidth(1.0F);
        this.knockbackChance = 0D;
        this.targetX = this.position().x();
        this.targetY = this.position().y();
        this.targetZ = this.position().z();
        this.entityData.define(SHOOTING_ENTITY_ID, this.shootingEntityRef);
        this.entityData.define(LASER_END_ID, this.laserEndRef);
        this.entityData.define(LASER_TIME, this.laserTime);
        this.entityData.define(OFFSET_X, (float) this.offsetX);
        this.entityData.define(OFFSET_Y, (float) this.offsetY);
        this.entityData.define(OFFSET_Z, (float) this.offsetZ);
        this.noPhysics = true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        if (this.laserEnd == null)
            return super.getBoundingBoxForCulling();
        double distance = this.distanceTo(this.laserEnd);
        return super.getBoundingBoxForCulling().expandTowards(distance, distance, distance);
    }


    // ==================================================
    //                   Properties
    // ==================================================
    public void setOffset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        this.syncOffset();
    }


    // ==================================================
    //                      Update
    // ==================================================
    @Override
    public void tick() {
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(LASER_TIME, this.laserTime);
        } else {
            this.laserTime = this.entityData.get(LASER_TIME);
        }
        this.syncShootingEntity();

        //this.syncOffset(); Broken? :(
        if (!this.getCommandSenderWorld().isClientSide && this.shootingEntity != null) {
            Entity entityToFollow = this.shootingEntity;
            if (this.followEntity != null)
                entityToFollow = this.followEntity;
            double xPos = entityToFollow.position().x() + this.offsetX;
            double yPos = entityToFollow.position().y() + (this.getDimensions(Pose.STANDING).height / 2) + this.offsetY;
            double zPos = entityToFollow.position().z() + this.offsetZ;
            if (entityToFollow instanceof BaseCreatureEntity) {
                BaseCreatureEntity creatureToFollow = (BaseCreatureEntity) entityToFollow;
                xPos = creatureToFollow.getFacingPosition(creatureToFollow, this.offsetX, creatureToFollow.yRotO + 90F).getX();
                zPos = creatureToFollow.getFacingPosition(creatureToFollow, this.offsetZ, creatureToFollow.yRotO).getZ();
            }
            this.setPos(xPos, yPos, zPos);
        }

        if (this.laserTime > 0) {
            this.updateEnd();
            this.laserTime--;
            double minX;
            double maxX;
            double minY;
            double maxY;
            double minZ;
            double maxZ;

            if (this.laserEnd != null) {
                if (this.position().x() - this.getDimensions(Pose.STANDING).width < this.laserEnd.position().x() - this.laserEnd.getDimensions(Pose.STANDING).width)
                    minX = this.position().x() - this.getDimensions(Pose.STANDING).width;
                else
                    minX = this.laserEnd.position().x() - this.laserEnd.getDimensions(Pose.STANDING).width;

                if (this.position().x() + this.getDimensions(Pose.STANDING).width > this.laserEnd.position().x() + this.laserEnd.getDimensions(Pose.STANDING).width)
                    maxX = this.position().x() + this.getDimensions(Pose.STANDING).width;
                else
                    maxX = this.laserEnd.position().x() + this.laserEnd.getDimensions(Pose.STANDING).width;


                if (this.position().y() - this.getDimensions(Pose.STANDING).height < this.laserEnd.position().y() - this.laserEnd.getDimensions(Pose.STANDING).height)
                    minY = this.position().y() - this.getDimensions(Pose.STANDING).height;
                else
                    minY = this.laserEnd.position().y() - this.laserEnd.getDimensions(Pose.STANDING).height;

                if (this.position().y() + this.getDimensions(Pose.STANDING).width > this.laserEnd.position().y() + this.laserEnd.getDimensions(Pose.STANDING).height)
                    maxY = this.position().y() + this.getDimensions(Pose.STANDING).height;
                else
                    maxY = this.laserEnd.position().y() + this.laserEnd.getDimensions(Pose.STANDING).height;


                if (this.position().z() - this.getDimensions(Pose.STANDING).width < this.laserEnd.position().z() - this.laserEnd.getDimensions(Pose.STANDING).width)
                    minZ = this.position().z() - this.getDimensions(Pose.STANDING).width;
                else
                    minZ = this.laserEnd.position().z() - this.laserEnd.getDimensions(Pose.STANDING).width;

                if (this.position().z() + this.getDimensions(Pose.STANDING).width > this.laserEnd.position().z() + this.laserEnd.getDimensions(Pose.STANDING).width)
                    maxZ = this.position().z() + this.getDimensions(Pose.STANDING).width;
                else
                    maxZ = this.laserEnd.position().z() + this.laserEnd.getDimensions(Pose.STANDING).width;
            } else {
                minX = this.position().x() - this.getDimensions(Pose.STANDING).width;
                maxX = this.position().x() + this.getDimensions(Pose.STANDING).width;
                minY = this.position().y() - this.getDimensions(Pose.STANDING).height;
                maxY = this.position().y() + this.getDimensions(Pose.STANDING).height;
                minZ = this.position().z() - this.getDimensions(Pose.STANDING).width;
                maxZ = this.position().z() + this.getDimensions(Pose.STANDING).width;
            }

            this.getBoundingBox().expandTowards(
                    (maxX - minX) - (this.getBoundingBox().maxX - this.getBoundingBox().minX),
                    (maxY - minY) - (this.getBoundingBox().maxY - this.getBoundingBox().minY),
                    (maxZ - minZ) - (this.getBoundingBox().maxZ - this.getBoundingBox().minZ)
            );
        } else if (this.isAlive()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }


    // ==================================================
    //                   Update End
    // ==================================================
    public void updateEnd() {
        if (this.getCommandSenderWorld().isClientSide) {
            this.laserEndRef = this.entityData.get(LASER_END_ID);
            Entity possibleLaserEnd = null;
            if (this.laserEndRef != -1)
                possibleLaserEnd = this.getCommandSenderWorld().getEntity(this.laserEndRef);
            if (possibleLaserEnd != null && possibleLaserEnd instanceof LaserEndProjectileEntity)
                this.laserEnd = (LaserEndProjectileEntity) possibleLaserEnd;
            else {
                this.laserEnd = null;
                return;
            }
        }

        if (this.laserEnd == null)
            fireProjectile();

        if (this.laserEnd == null)
            this.laserEndRef = -1;
        else {
            if (!this.getCommandSenderWorld().isClientSide)
                this.laserEndRef = this.laserEnd.getId();

            // Entity Aiming:
            boolean lockedLaser = false;
            if (this.shootingEntity != null && this.useEntityAttackTarget) {
                if (this.shootingEntity instanceof BaseCreatureEntity && ((BaseCreatureEntity) this.shootingEntity).getTarget() != null) {
                    LivingEntity attackTarget = ((BaseCreatureEntity) this.shootingEntity).getTarget();
                    this.targetX = attackTarget.position().x();
                    this.targetY = attackTarget.position().y() + (attackTarget.getDimensions(Pose.STANDING).height / 2);
                    this.targetZ = attackTarget.position().z();
                    lockedLaser = true;
                } else {
                    Vec3 lookDirection = this.shootingEntity.getLookAngle();
                    this.targetX = this.shootingEntity.position().x() + (lookDirection.x * this.laserRange);
                    this.targetY = this.shootingEntity.position().y() + this.shootingEntity.getEyeHeight() + (lookDirection.y * this.laserRange);
                    this.targetZ = this.shootingEntity.position().z() + (lookDirection.z * this.laserRange);
                }
            }

            // Raytracing:
            HashSet<Entity> excludedEntities = new HashSet<>();
            excludedEntities.add(this);
            if (this.shootingEntity != null)
                excludedEntities.add(this.shootingEntity);
            if (this.followEntity != null)
                excludedEntities.add(this.followEntity);
            HitResult rayTraceResult = LMHelperClass.raytrace(this.getCommandSenderWorld(), this.position().x(), this.position().y(), this.position().z(), this.targetX, this.targetY, this.targetZ, this.laserWidth, this, excludedEntities);

            // Update Laser End Position:
            double newTargetX = this.targetX;
            double newTargetY = this.targetY;
            double newTargetZ = this.targetZ;
            if (rayTraceResult != null && !lockedLaser) {
                newTargetX = rayTraceResult.getLocation().x;
                newTargetY = rayTraceResult.getLocation().y;
                newTargetZ = rayTraceResult.getLocation().z;
            }
            this.laserEnd.onUpdateEnd(newTargetX, newTargetY, newTargetZ);

            // Damage:
            if (this.laserTime % this.laserDelay == 0 && this.isAlive() && rayTraceResult instanceof EntityHitResult) {
                EntityHitResult entityRayTraceResult = (EntityHitResult) rayTraceResult;
                if (this.laserEnd.distanceTo(entityRayTraceResult.getEntity()) <= (this.laserWidth * 10)) {
                    boolean doDamage = true;
                    if (entityRayTraceResult.getEntity() instanceof LivingEntity) {
                        doDamage = this.canDamage((LivingEntity) entityRayTraceResult.getEntity());
                    }
                    if (doDamage)
                        this.updateDamage(entityRayTraceResult.getEntity());
                }
            }
        }

        this.entityData.set(LASER_END_ID, this.laserEndRef);
        if (this.getBeamSound() != null)
            this.playSound(this.getBeamSound(), 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
    }


    // ==================================================
    //                    Laser Time
    // ==================================================
    public void setTime(int time) {
        this.laserTime = time;
    }

    public int getTime() {
        return this.laserTime;
    }


    // ==================================================
    //                 Fire Projectile
    // ==================================================
    public void fireProjectile() {
        Level world = this.getCommandSenderWorld();
        if (world.isClientSide)
            return;

        try {
            if (this.shootingEntity == null) {
                Constructor laserEndConstructor = this.getLaserEndClass().getConstructor(EntityType.class, Level.class, Double.class, Double.class, Double.class, LaserProjectileEntity.class);
                this.laserEnd = (LaserEndProjectileEntity) laserEndConstructor.newInstance(ProjectileManager.getInstance().oldProjectileTypes.get(this.getLaserEndClass()), world, this.position().x(), this.position().y(), this.position().z(), this);
            } else {
                Constructor laserEndConstructor = this.getLaserEndClass().getConstructor(EntityType.class, Level.class, LivingEntity.class, LaserProjectileEntity.class);
                this.laserEnd = (LaserEndProjectileEntity) laserEndConstructor.newInstance(ProjectileManager.getInstance().oldProjectileTypes.get(this.getLaserEndClass()), world, this.shootingEntity, this);
            }

            if (this.getLaunchSound() != null)
                this.playSound(this.getLaunchSound(), 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));

            world.addFreshEntity(laserEnd);
        } catch (Exception e) {
            System.out.println("[WARNING] [LycanitesMobs] EntityLaser was unable to instantiate the EntityLaserEnd.");
            e.printStackTrace();
        }
    }


    // ==================================================
    //               Sync Shooting Entity
    // ==================================================
    public void syncShootingEntity() {
        if (!this.getCommandSenderWorld().isClientSide) {
            if (this.shootingEntity == null) this.shootingEntityRef = -1;
            else this.shootingEntityRef = this.shootingEntity.getId();
            this.entityData.set(SHOOTING_ENTITY_ID, this.shootingEntityRef);
        } else {
            this.shootingEntityRef = this.entityData.get(SHOOTING_ENTITY_ID);
            if (this.shootingEntityRef == -1) this.shootingEntity = null;
            else {
                Entity possibleShootingEntity = this.getCommandSenderWorld().getEntity(this.shootingEntityRef);
                if (possibleShootingEntity != null && possibleShootingEntity instanceof LivingEntity)
                    this.shootingEntity = (LivingEntity) possibleShootingEntity;
                else
                    this.shootingEntity = null;
            }
        }
    }

    public void syncOffset() {
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(OFFSET_X, (float) this.offsetX);
            this.entityData.set(OFFSET_Y, (float) this.offsetY);
            this.entityData.set(OFFSET_Z, (float) this.offsetZ);
        } else {
            this.offsetX = this.entityData.get(OFFSET_X);
            this.offsetY = this.entityData.get(OFFSET_Y);
            this.offsetZ = this.entityData.get(OFFSET_Z);
        }
    }


    // ==================================================
    //                   Get laser End
    // ==================================================
    public LaserEndProjectileEntity getLaserEnd() {
        return this.laserEnd;
    }

    public Class getLaserEndClass() {
        return LaserEndProjectileEntity.class;
    }


    // ==================================================
    //                    Set Target
    // ==================================================
    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }


    // ==================================================
    //                   Movement
    // ==================================================
    // ========== Gravity ==========
    @Override
    protected float getGravity() {
        return 0.0F;
    }


    // ==================================================
    //                     Impact
    // ==================================================
    @Override
    protected void onHit(HitResult rayTraceResult) {
        return;
    }


    // ==================================================
    //                      Damage
    // ==================================================
    public boolean updateDamage(Entity target) {
        boolean attackSuccess = false;
        float damage = this.getDamage(target);
        float damageInit = damage;

        // Prevent Knockback:
        double targetKnockbackResistance = 0;
        if (this.knockbackChance < 1) {
            if (this.knockbackChance <= 0 || this.random.nextDouble() <= this.knockbackChance) {
                if (target instanceof LivingEntity) {
                    targetKnockbackResistance = ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
                    ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
                }
            }
        }

        // Deal Damage:
        if (this.getOwner() instanceof BaseCreatureEntity) {
            BaseCreatureEntity creatureThrower = (BaseCreatureEntity) this.getOwner();
            attackSuccess = creatureThrower.doRangedDamage(target, this, damage, this.isBlockedByEntity(target));
        } else {
            double pierceDamage = 1;
            if (damage <= pierceDamage)
                attackSuccess = target.hurt(this.level().damageSources().thrown(this, this.getOwner()), damage);
            else {
                int hurtResistantTimeBefore = target.invulnerableTime;
                target.hurt(this.level().damageSources().thrown(this, this.getOwner()), (float) pierceDamage);
                target.invulnerableTime = hurtResistantTimeBefore;
                damage -= pierceDamage;
                attackSuccess = target.hurt(this.level().damageSources().thrown(this, this.getOwner()), damage);
            }
        }

        if (target instanceof LivingEntity)
            this.onDamage((LivingEntity) target, damageInit, attackSuccess);

        // Restore Knockback:
        if (this.knockbackChance < 1) {
            if (this.knockbackChance <= 0 || this.random.nextDouble() <= this.knockbackChance) {
                if (target instanceof LivingEntity)
                    ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);
            }
        }

        return attackSuccess;
    }


    // ==================================================
    //                      Stats
    // ==================================================
    public void setRange(float range) {
        this.laserRange = range;
    }

    public void setLaserWidth(float width) {
        this.laserWidth = width;
    }

    public float getLaserWidth() {
        return this.laserWidth;
    }

    public float getLaserAlpha() {
        return 0.25F + (float) (0.1F * Math.sin(this.tickCount));
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    public ResourceLocation getBeamTexture() {
        return null;
    }

    public double[] getLengths() {
        if (this.laserEnd == null)
            return new double[]{0.0D, 0.0D, 0.0D};
        else
            return new double[]{
                    this.laserEnd.position().x() - this.position().x(),
                    this.laserEnd.position().y() - this.position().y(),
                    this.laserEnd.position().z() - this.position().z()
            };
    }

    public float getLength() {
        if (this.laserEnd == null)
            return 0;
        return this.distanceTo(this.laserEnd);
    }

    public float[] getBeamAngles() {
        float[] angles = new float[]{0, 0, 0, 0};
        if (this.laserEnd != null) {
            float dx = (float) (this.laserEnd.position().x() - this.position().x());
            float dy = (float) (this.laserEnd.position().y() - this.position().y());
            float dz = (float) (this.laserEnd.position().z() - this.position().z());
            angles[0] = (float) Math.toDegrees(Math.atan2(dz, dy)) - 90;
            angles[1] = (float) Math.toDegrees(Math.atan2(dx, dz));
            angles[2] = (float) Math.toDegrees(Math.atan2(dx, dy)) - 90;

            // Distance based x/z rotation:
            float dr = (float) Math.sqrt(dx * dx + dz * dz);
            angles[3] = (float) Math.toDegrees(Math.atan2(dr, dy)) - 90;
        }
        return angles;
    }


    // ==================================================
    //                      Sounds
    // ==================================================
    @Override
    public SoundEvent getLaunchSound() {
        return null;
    }

    @Override
    public SoundEvent getBeamSound() {
        return null;
    }


    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========
    @Override
    public void readAdditionalSaveData(CompoundTag nbtTagCompound) {
        if (nbtTagCompound.contains("LaserTime"))
            this.setTime(nbtTagCompound.getInt("LaserTime"));
        if (nbtTagCompound.contains("OffsetX"))
            this.offsetX = nbtTagCompound.getDouble("OffsetX");
        if (nbtTagCompound.contains("OffsetY"))
            this.offsetY = nbtTagCompound.getDouble("OffsetY");
        if (nbtTagCompound.contains("OffsetZ"))
            this.offsetZ = nbtTagCompound.getDouble("OffsetZ");
        super.readAdditionalSaveData(nbtTagCompound);
    }

    // ========== Write ==========
    @Override
    public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
        nbtTagCompound.putInt("LaserTime", this.laserTime);
        nbtTagCompound.putDouble("OffsetX", this.offsetX);
        nbtTagCompound.putDouble("OffsetY", this.offsetY);
        nbtTagCompound.putDouble("OffsetZ", this.offsetZ);
        super.addAdditionalSaveData(nbtTagCompound);
    }
}
