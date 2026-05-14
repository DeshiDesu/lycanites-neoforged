package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;

public class EntityEpion extends RideableCreatureEntity implements Enemy {

    public boolean griefing = true;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityEpion(EntityType<? extends EntityEpion> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEAD;
        this.hasAttackSound = false;
        this.flySoundSpeed = 20;

        this.setupMob();

        this.setMaxUpStep(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setRange(14.0F).setMinChaseDistance(6.0F));
    }

    @Override
    public void loadCreatureFlags() {
        this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
    }

    @Override
    public float getStrafeSpeed() {
        return 1F;
    }

    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Sunlight Explosions:
        if (!this.getCommandSenderWorld().isClientSide && !this.isTamed() && !this.isMinion() && !this.isRareVariant()) {
            if (!this.isFlying() && (this.onGround() || this.isInWater()) && this.isAlive()) {
                int explosionRadius = 2;
                if (this.subspecies != null)
                    explosionRadius = 3;
                explosionRadius = Math.max(2, Math.round((float) explosionRadius * (float) this.sizeScale));
                if (this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.griefing)
                    this.getCommandSenderWorld().explode(this, this.position().x(), this.position().y(), this.position().z(), explosionRadius, Level.ExplosionInteraction.NONE);
                else
                    this.getCommandSenderWorld().explode(this, this.position().x(), this.position().y(), this.position().z(), explosionRadius, Level.ExplosionInteraction.TNT);
                this.remove(RemovalReason.DISCARDED);
            }
        }

        // Particles:
        if (this.getCommandSenderWorld().isClientSide)
            for (int i = 0; i < 2; ++i) {
                this.getCommandSenderWorld().addParticle(ParticleTypes.WITCH, this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
            }
    }

    @Override
    public boolean hasLineOfSight(Entity target) {
        if (this.isRareVariant()) {
            return true;
        }
        return super.hasLineOfSight(target);
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("bloodleech", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
        super.attackRanged(target, range);
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() {
        if (this.getCommandSenderWorld().isClientSide) return true;
        if (this.daylightBurns() && this.getCommandSenderWorld().isDay() && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.griefing) {
            float brightness = this.getBrightness();
            if (brightness > 0.5F && this.getCommandSenderWorld().canSeeSkyFromBelowWater(this.blockPosition()))
                return false;
        }
        return true;
    }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
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

    /**
     * Returns true if this mob should be damaged by the sun.
     **/
    @Override
    public boolean daylightBurns() {
        return !this.isMinion() && !this.hasMaster() && !this.isTamed() && !this.isRareVariant();
    }

    @Override
    public float getFallResistance() {
        return 100;
    }


    // ==================================================
    //                   Mount Ability
    // ==================================================
    public void mountAbility(Entity rider) {
        if (this.getCommandSenderWorld().isClientSide)
            return;

        if (this.getStamina() < this.getStaminaCost())
            return;

        if (rider instanceof Player) {
            Player player = (Player) rider;
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("bloodleech");
            if (projectileInfo != null) {
                BaseProjectileEntity projectile = projectileInfo.createProjectile(this.getCommandSenderWorld(), player);
                this.getCommandSenderWorld().addFreshEntity(projectile);
                this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                this.triggerAttackCooldown();
            }
        }

        this.applyStaminaCost();
    }

    public float getStaminaCost() {
        return 2;
    }

    public int getStaminaRecoveryWarmup() {
        return 0;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }


    // ==================================================
    //                       Visuals
    // ==================================================

    /**
     * Returns this creature's main texture. Also checks for for subspecies.
     **/
    public ResourceLocation getTexture() {
        if (!this.hasCustomName() || !"Vampire Bat".equals(this.getCustomName().getString()))
            return super.getTexture();

        String textureName = this.getTextureName() + "_vampirebat";
        if (TextureManager.getTexture(textureName) == null)
            TextureManager.addTexture(textureName, "textures/entity/" + textureName.toLowerCase() + ".png");
        return TextureManager.getTexture(textureName);
    }

    // ========== Rendering Distance ==========

    /**
     * Returns a larger bounding box for rendering this large entity.
     **/
    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(10, 10, 10).move(0, -5, 0);
    }
}
