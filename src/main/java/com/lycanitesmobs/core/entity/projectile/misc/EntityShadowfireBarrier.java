package com.lycanitesmobs.core.entity.projectile.misc;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.IGroupBoss;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.creature.demon.EntityRahovart;
import com.lycanitesmobs.core.data.info.ObjectLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class EntityShadowfireBarrier extends BaseProjectileEntity {

    // Properties:
    public Entity shootingEntity;

    // ==================================================
    //                   Constructors
    // ==================================================
    public EntityShadowfireBarrier(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public EntityShadowfireBarrier(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity par2LivingEntity) {
        super(entityType, world, par2LivingEntity);
    }

    public EntityShadowfireBarrier(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6) {
        super(entityType, world, par2, par4, par6);
    }

    // ========== Setup Projectile ==========
    public void setup() { // Size 10F
        this.entityName = "shadowfirebarrier";
        this.modInfo = LycanitesMobs.modInfo;
        this.setDamage(10);
        this.setProjectileScale(20F);
        this.movement = false;
        this.ripper = true;
        this.pierceBlocks = true;
        this.projectileLife = 2 * 20;
        this.animationFrameMax = 19;
        this.textureTiling = 2;
        this.noPhysics = true;
        this.waterProof = true;
        this.lavaProof = true;
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
        super.tick();

        if (!this.getCommandSenderWorld().isClientSide) {
            List list = this.getCommandSenderWorld().getEntities(this, this.getBoundingBox().inflate(this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z()).expandTowards(1.0D, 1.0D, 1.0D));

            for (int j = 0; j < list.size(); ++j) {
                Entity entity = (Entity) list.get(j);
                this.onHit(new EntityHitResult(entity));
            }
        }
    }

    public boolean handleWaterMovement() {
        return false;
    }

    //========== Entity Living Collision ==========
    @Override
    public boolean onEntityLivingDamage(LivingEntity livingEntity) {
        MobEffect decay = ObjectManager.getEffect("decay");
        if (decay != null) {
            MobEffectInstance effect = new MobEffectInstance(decay, 5 * 20, 0);
            if (livingEntity.canBeAffected(effect))
                livingEntity.addEffect(effect);
        }

        MobEffectInstance blindness = new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20, 0);
        if (livingEntity.canBeAffected(blindness)) {
            livingEntity.addEffect(blindness);
        }
        return true;
    }

    //========== Do Damage Check ==========
    @Override
    public boolean canDamage(LivingEntity targetEntity) {
        LivingEntity owner = (LivingEntity) this.getShooter();
        if (owner == null) {
            if (targetEntity instanceof EntityRahovart)
                return false;
        }
        return super.canDamage(targetEntity);
    }

    //========== On Damage ==========
    @Override
    public void onDamage(LivingEntity target, float damage, boolean attackSuccess) {

        // Remove Good Potion Effects:
        for (MobEffectInstance potionEffect : target.getActiveEffects().toArray(new MobEffectInstance[target.getActiveEffects().size()])) {
            if (ObjectLists.inEffectList("buffs", potionEffect.getEffect()))
                target.removeEffect(potionEffect.getEffect());
        }

        boolean obliterate = true;
        if (target instanceof Player)
            obliterate = false;
        else if (target instanceof OwnableEntity) {
            obliterate = !(((OwnableEntity) target).getOwner() instanceof Player);
        } else if (target instanceof TameableCreatureEntity) {
            obliterate = !(((TameableCreatureEntity) target).getOwner() instanceof Player);
        }
        if (target instanceof IGroupBoss)
            obliterate = false;
        if (obliterate)
            target.setHealth(0);
        super.onDamage(target, damage, attackSuccess);
    }


    // ==================================================
    //                      Sounds
    // ==================================================
    @Override
    public SoundEvent getLaunchSound() {
        return ObjectManager.getSound("hellfirewall");
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
