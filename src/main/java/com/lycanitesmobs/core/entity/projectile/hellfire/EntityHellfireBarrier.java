package com.lycanitesmobs.core.entity.projectile.hellfire;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.creature.demon.EntityRahovart;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityHellfireBarrier extends BaseProjectileEntity {

    // Properties:
    public EntityHellfireWall[][] hellfireWalls;
    protected int hellfireWidth = 5;
    protected int hellfireHeight = 3;
    protected int hellfireSize = 10;
    public boolean wall = false;
    public int time = 0;
    public int timeMax = 2 * 20;
    public float angle = 90;
    public double rotation = 0;

    // ==================================================
    //                   Constructors
    // ==================================================
    public EntityHellfireBarrier(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public EntityHellfireBarrier(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity par2LivingEntity) {
        super(entityType, world, par2LivingEntity);
    }

    public EntityHellfireBarrier(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6) {
        super(entityType, world, par2, par4, par6);
    }

    // ========== Setup Projectile ==========
    public void setup() { // Size 2F
        this.entityName = "hellfirebarrier";
        this.modInfo = LycanitesMobs.modInfo;
        this.setDamage(0);
        this.setProjectileScale(0F);
        this.movement = false;
        this.ripper = true;
        this.pierceBlocks = true;
        this.projectileLife = 5 * 20;
        this.animationFrameMax = 59;
        this.noPhysics = true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }


    // ==================================================
    //                      Update
    // ==================================================
    @Override
    public void tick() {
        if (this.getCommandSenderWorld().isClientSide)
            return;

        // Time Update:
        if (this.time++ >= this.timeMax)
            this.remove(RemovalReason.DISCARDED);

        // Populate:
        if (this.hellfireWalls == null) {
            hellfireWalls = new EntityHellfireWall[this.hellfireHeight][this.hellfireWidth];
            for (int row = 0; row < this.hellfireHeight; row++) {
                for (int col = 0; col < this.hellfireWidth; col++) {
                    if (this.getOwner() != null) {
                        if (this.wall) {
                            hellfireWalls[row][col] = new EntityHellfireWall(ProjectileManager.getInstance().oldProjectileTypes.get(EntityHellfireWall.class), this.getCommandSenderWorld(), (LivingEntity) this.getShooter());
                        } else {
                            hellfireWalls[row][col] = new EntityHellfireBarrierPart(ProjectileManager.getInstance().oldProjectileTypes.get(EntityHellfireBarrierPart.class), this.getCommandSenderWorld(), (LivingEntity) this.getShooter());
                        }
                    } else {
                        if (this.wall) {
                            hellfireWalls[row][col] = new EntityHellfireWall(ProjectileManager.getInstance().oldProjectileTypes.get(EntityHellfireWall.class), this.getCommandSenderWorld(), this.position().x(), this.position().y() + (this.hellfireSize * row), this.position().z());
                        } else {
                            hellfireWalls[row][col] = new EntityHellfireBarrierPart(ProjectileManager.getInstance().oldProjectileTypes.get(EntityHellfireBarrierPart.class), this.getCommandSenderWorld(), this.position().x(), this.position().y() + (this.hellfireSize * row), this.position().z());
                        }
                    }

                    double rotationRadians = Math.toRadians(this.rotation);
                    double x = (((float) col / this.hellfireWidth) * (this.hellfireSize * (this.hellfireWidth - 1))) * Math.cos(rotationRadians) + Math.sin(rotationRadians);
                    double z = (((float) col / this.hellfireWidth) * (this.hellfireSize * (this.hellfireWidth - 1))) * Math.sin(rotationRadians) - Math.cos(rotationRadians);
                    this.hellfireWalls[row][col].setPos(
                            this.position().x() + x,
                            this.position().y() + (this.hellfireSize * row),
                            this.position().z() + z
                    );
                    this.hellfireWalls[row][col].projectileLife = 2 * 20;

                    this.getCommandSenderWorld().addFreshEntity(hellfireWalls[row][col]);
                    this.hellfireWalls[row][col].setProjectileScale(this.hellfireSize * 2.5F);
                }
            }
        }

        // Move:
        for (int row = 0; row < this.hellfireHeight; row++) {
            for (int col = 0; col < this.hellfireWidth; col++) {

                double rotationRadians = Math.toRadians(this.rotation);
                double x = (((float) col / this.hellfireWidth) * (this.hellfireSize * (this.hellfireWidth - 1))) * Math.cos(rotationRadians) + Math.sin(rotationRadians);
                double z = (((float) col / this.hellfireWidth) * (this.hellfireSize * (this.hellfireWidth - 1))) * Math.sin(rotationRadians) - Math.cos(rotationRadians);
                this.hellfireWalls[row][col].setPos(
                        this.position().x() + x,
                        this.position().y() + (this.hellfireSize * row),
                        this.position().z() + z
                );
                this.hellfireWalls[row][col].projectileLife = 2 * 20;

                if (!this.isAlive())
                    this.hellfireWalls[row][col].remove(RemovalReason.DISCARDED);
            }
        }
    }


    // ==================================================
    //                     Impact
    // ==================================================
    //========== Entity Living Collision ==========
    @Override
    public boolean onEntityLivingDamage(LivingEntity entityLiving) {
        if (!entityLiving.isInvulnerableTo(entityLiving.level().damageSources().onFire()))
            entityLiving.setSecondsOnFire(this.getEffectDuration(10) / 20);
        return true;
    }

    //========== Do Damage Check ==========
    public boolean canDamage(LivingEntity targetEntity) {
        LivingEntity owner = (LivingEntity) this.getShooter();
        if (owner == null) {
            if (targetEntity instanceof EntityRahovart)
                return false;
        }
        return super.canDamage(targetEntity);
    }


    // ==================================================
    //                      Sounds
    // ==================================================
    @Override
    public SoundEvent getLaunchSound() {
        return ObjectManager.getSound("hellfirewave");
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


    // ==================================================
    //                      Visuals
    // ==================================================
    @Override
    public String subTypeString() {
        return "events";
    }

    public ResourceLocation getTexture() {
        if (TextureManager.getTexture(this.entityName) == null)
            TextureManager.addTexture(this.entityName, "textures/particles/" + subTypeString() + "/" + this.entityName + ".png");
        return TextureManager.getTexture(this.entityName);
    }
}
