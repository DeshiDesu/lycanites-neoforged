package com.lycanitesmobs.core.entity.projectile.misc;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class EntityDevilGatling extends BaseProjectileEntity {

    // Properties:
    public Entity shootingEntity;
    public int expireTime = 15;

    // ==================================================
    //                   Constructors
    // ==================================================
    public EntityDevilGatling(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public EntityDevilGatling(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityLivingBase) {
        super(entityType, world, entityLivingBase);
    }

    public EntityDevilGatling(EntityType<? extends BaseProjectileEntity> entityType, Level world, double x, double y, double z) {
        super(entityType, world, x, y, z);
    }

    // ========== Setup Projectile ==========
    public void setup() {
        this.entityName = "devilgatling";
        this.modInfo = LycanitesMobs.modInfo;
        this.setDamage(4);
        this.ripper = true;
    }


    // ==================================================
    //                   Update
    // ==================================================
    @Override
    public void tick() {
        super.tick();

        if (this.position().y() > this.getCommandSenderWorld().getMaxBuildHeight() + 20)
            this.remove(RemovalReason.DISCARDED);

        if (this.tickCount >= this.expireTime * 20)
            this.remove(RemovalReason.DISCARDED);
    }


    // ==================================================
    //                   Movement
    // ==================================================
    // ========== Gravity ==========
    @Override
    protected float getGravity() {
        return 0F;
    }


    // ==================================================
    //                     Impact
    // ==================================================
    //========== Entity Living Collision ==========
    @Override
    public void onDamage(LivingEntity target, float damage, boolean attackSuccess) {
        super.onDamage(target, damage, attackSuccess);

        // Remove Buffs:
        if (this.random.nextBoolean()) {
            List<MobEffect> goodEffects = new ArrayList<>();
            for (Object potionEffectObj : target.getActiveEffects()) {
                if (potionEffectObj instanceof MobEffectInstance) {
                    MobEffect effect = ((MobEffectInstance) potionEffectObj).getEffect();
                    if (effect != null) {
                        if (ObjectLists.inEffectList("buffs", effect))
                            goodEffects.add(effect);
                    }
                }
            }
            if (!goodEffects.isEmpty()) {
                if (goodEffects.size() > 1)
                    target.removeEffect(goodEffects.get(this.random.nextInt(goodEffects.size())));
                else
                    target.removeEffect(goodEffects.get(0));
            }
        }

        if (ObjectManager.getEffect("decay") != null) {
            target.addEffect(new MobEffectInstance(ObjectManager.getEffect("decay"), this.getEffectDuration(60), 0));
        }
    }

    //========== On Impact Particles/Sounds ==========
    @Override
    public void onImpactVisuals() {
        for (int i = 0; i < 8; ++i)
            this.getCommandSenderWorld().addParticle(ParticleTypes.EXPLOSION, this.position().x(), this.position().y(), this.position().z(), 0.0D, 0.0D, 0.0D);
    }


    // ==================================================
    //                      Sounds
    // ==================================================
    @Override
    public SoundEvent getLaunchSound() {
        return ObjectManager.getSound("devilgatling");
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
            TextureManager.addTexture(this.entityName, "textures/item/" + subTypeString() + "/" + this.entityName + ".png");
        return TextureManager.getTexture(this.entityName);
    }
}
