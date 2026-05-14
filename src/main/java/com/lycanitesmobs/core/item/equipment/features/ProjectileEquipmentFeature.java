package com.lycanitesmobs.core.item.equipment.features;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ProjectileEquipmentFeature extends EquipmentFeature {
    /**
     * The name of the projectile to spawn.
     **/
    public String projectileName;

    /**
     * How this feature spawns projectiles. Can be: 'hit' (when damaging an entity), 'primary' (left click) or 'secondary' (right click).
     **/
    public String projectileTrigger = "secondary";

    /**
     * The pattern to fire projectiles in. Can be 'simple', 'spread' or 'ring'.
     **/
    public String projectilePattern = "simple";

    /**
     * The chance of firing a projectile for the hit trigger.
     **/
    public double hitChance = 0.05;

    /**
     * The cooldown (in ticks) for primary and secondary triggers.
     **/
    public int cooldown = 2;

    /**
     * How many projectiles to fire per round.
     **/
    public int count = 1;

    /**
     * The x spread for the spread projectile pattern.
     **/
    public double spreadX = 0;

    /**
     * The y spread for the spread projectile pattern.
     **/
    public double spreadY = 0;

    /**
     * The range in degrees for the ring pattern.
     **/
    public double ringRange = 0;

    /**
     * Additional damage added to the projectile.
     **/
    public int bonusDamage = 0;

    /**
     * Stores a channeled projectile currently in use by this feature, used for lasers.
     **/
    private BaseProjectileEntity channeledProjectile;


    @Override
    public void loadFromJSON(JsonObject json) {
        super.loadFromJSON(json);

        this.projectileName = json.get("projectileName").getAsString();

        if (json.has("projectileTrigger"))
            this.projectileTrigger = json.get("projectileTrigger").getAsString();

        if (json.has("projectilePattern"))
            this.projectilePattern = json.get("projectilePattern").getAsString();

        if (json.has("hitChance"))
            this.hitChance = json.get("hitChance").getAsDouble();

        if (json.has("cooldown"))
            this.cooldown = json.get("cooldown").getAsInt();

        if (json.has("count"))
            this.count = json.get("count").getAsInt();

        if (json.has("spreadX"))
            this.spreadX = json.get("spreadX").getAsDouble();

        if (json.has("spreadY"))
            this.spreadY = json.get("spreadY").getAsDouble();

        if (json.has("ringRange"))
            this.ringRange = json.get("ringRange").getAsDouble();

        if (json.has("bonusDamage"))
            this.bonusDamage = json.get("bonusDamage").getAsInt();
    }

    @Override
    public boolean isActive(ItemStack itemStack, int level) {
        if (!super.isActive(itemStack, level)) {
            return false;
        }
        return ProjectileManager.getInstance().getProjectile(this.projectileName) != null;
    }

    @Override
    public MutableComponent getDescription(ItemStack itemStack, int level) {
        if (!this.isActive(itemStack, level)) {
            return null;
        }

        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
        MutableComponent description = Component.translatable("equipment.feature." + this.featureType).append(" ")
                .append(projectileInfo.getTitle());

        if (this.bonusDamage != 0) {
            description.append(" +" + this.bonusDamage);
        }

        if (!"simple".equals(this.projectilePattern)) {
            description.append(" ")
                    .append(Component.translatable("equipment.feature.projectile.pattern." + this.projectilePattern));
        }

        description.append(" ")
                .append(Component.translatable("equipment.feature.projectile.trigger." + this.projectileTrigger));
        if ("hit".equals(this.projectileTrigger)) {
            description.append(" " + String.format("%.0f", this.hitChance * 100) + "%");
        } else {
            description.append(" " + String.format("%.1f", (float) this.cooldown / 20) + "s");
        }

        return description;
    }

    @Override
    public MutableComponent getSummary(ItemStack itemStack, int level) {
        if (!this.isActive(itemStack, level)) {
            return null;
        }
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
        MutableComponent summary = projectileInfo.getTitle().copy();
        if (this.bonusDamage != 0) {
            summary.append(" +" + this.bonusDamage);
        }
        return summary;
    }

    /**
     * Called when a player left clicks to use their equipment.
     *
     * @param world   The world the player is in.
     * @param shooter The player using the equipment.
     * @param hand    The hand the player is holding the equipment in.
     */
    public boolean onUsePrimary(Level world, Player shooter, InteractionHand hand) {
        if (!"primary".equalsIgnoreCase(this.projectileTrigger)) {
            return false;
        }
        ExtendedEntity shooterExt = ExtendedEntity.getForEntity(shooter);
        if (shooterExt == null) {
            return false;
        }
        if (shooterExt.getProjectileCooldown(1, this.projectileName) > 0) {
            return false;
        }
        shooterExt.setProjectileCooldown(1, this.projectileName, this.cooldown);
        this.fireProjectile(shooter);
        return true;
    }

    /**
     * Called when a player right click begins to use their equipment.
     *
     * @param world   The world the player is in.
     * @param shooter The player using the equipment.
     * @param hand    The hand the player is holding the equipment in.
     * @return True so that the item becomes active.
     */
    public boolean onUseSecondary(Level world, Player shooter, InteractionHand hand) {
        return "secondary".equalsIgnoreCase(this.projectileTrigger);
    }

    /**
     * Called when an entity is holding right-click to use their equipment's secondary ability.
     *
     * @param shooter The entity using the equipment.
     * @param count   How long (in ticks) the equipment has been used for.
     */
    public boolean onHoldSecondary(LivingEntity shooter, int count) {
        if (!"secondary".equalsIgnoreCase(this.projectileTrigger)) {
            return false;
        }
        ExtendedEntity shooterExt = ExtendedEntity.getForEntity(shooter);
        if (shooterExt == null) {
            return false;
        }
        if (shooterExt.getProjectileCooldown(2, this.projectileName) > 0) {
            return false;
        }
        shooterExt.setProjectileCooldown(2, this.projectileName, this.cooldown);
        this.fireProjectile(shooter);
        return true;
    }

    /**
     * Called when an entity is hit by equipment with this feature.
     *
     * @param itemStack The ItemStack being hit with.
     * @param target    The target entity being hit.
     * @param attacker  The entity using this item to hit.
     */
    public boolean onHitEntity(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        if (target == null || attacker == null || attacker.getCommandSenderWorld().isClientSide || attacker.isShiftKeyDown() || !"hit".equals(this.projectileTrigger)) { // isSneaking()
            return false;
        }

        // Fire Projectile:
        if (attacker.getRandom().nextDouble() <= this.hitChance) {
            this.fireProjectile(attacker);
            return true;
        }

        return false;
    }

    /**
     * Fires a projectile from this feature.
     *
     * @param shooter The entity firing the projectile.
     */
    public void fireProjectile(LivingEntity shooter) {
        if (shooter == null || shooter.getCommandSenderWorld().isClientSide || this.count <= 0) {
            return;
        }

        // Projectile Channeling:
        if (this.channeledProjectile != null) {
            if (!this.channeledProjectile.isAlive()) {
                this.channeledProjectile = null;
            } else {
                this.channeledProjectile.projectileLife = 20;
                return;
            }
        }

        Level world = shooter.getCommandSenderWorld();
        BaseProjectileEntity mainProjectile = null;
        Vec3 firePos = new Vec3(shooter.position().x(), shooter.position().y() + (shooter.getDimensions(Pose.STANDING).height * 0.65), shooter.position().z());
        double offsetX = 0;

        // Patterns:
        if ("spread".equals(this.projectilePattern)) {
            for (int i = 0; i < this.count; i++) {
                double yaw = shooter.yRotO + (this.spreadX * shooter.getRandom().nextDouble()) - (this.spreadX / 2);
                double pitch = shooter.xRotO + (this.spreadY * shooter.getRandom().nextDouble()) - (this.spreadY / 2);
                ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
                BaseProjectileEntity projectile = projectileInfo.createProjectile(world, shooter);
                projectile.setPos(firePos.x, firePos.y, firePos.z);
                projectile.shootFromRotation(shooter, (float) pitch, (float) yaw - (float) offsetX, 0, (float) projectileInfo.velocity, 0);
                projectile.setOwner(shooter);
                projectile.setBonusDamage(this.bonusDamage);
                world.addFreshEntity(projectile);
                mainProjectile = projectile;
            }
        } else if ("ring".equals(this.projectilePattern)) {
            double angle = this.ringRange / this.count;
            for (int i = 0; i < this.count; i++) {
                double yaw = shooter.yRotO + (angle * i) - (this.ringRange / 2);
                ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
                BaseProjectileEntity projectile = projectileInfo.createProjectile(world, shooter);
                projectile.setPos(firePos.x, firePos.y, firePos.z);
                projectile.setBonusDamage(this.bonusDamage);
                world.addFreshEntity(projectile);
                projectile.shootFromRotation(shooter, shooter.xRotO, (float) yaw - (float) offsetX, 0, (float) projectileInfo.velocity, 0);
                projectile.setOwner(shooter);
                mainProjectile = projectile;
            }
        } else {
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
            mainProjectile = projectileInfo.createProjectile(world, shooter);
            mainProjectile.setPos(firePos.x, firePos.y, firePos.z);
            mainProjectile.shootFromRotation(shooter, shooter.xRotO, shooter.yRotO - (float) offsetX, 0, (float) projectileInfo.velocity, 0);
            mainProjectile.setOwner(shooter);
            mainProjectile.setBonusDamage(this.bonusDamage);
            world.addFreshEntity(mainProjectile);
        }

        // Channeling:
        if (this.count == 1 && mainProjectile instanceof CustomProjectileEntity) {
            CustomProjectileEntity customProjectileEntity = (CustomProjectileEntity) mainProjectile;
            if (customProjectileEntity.shouldChannel()) {
                this.channeledProjectile = customProjectileEntity;
            }
        }

        if (shooter instanceof Player && mainProjectile != null) {
            world.playSound(null, shooter.blockPosition(), mainProjectile.getLaunchSound(), SoundSource.NEUTRAL, 0.5F, 0.4F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }

    /**
     * Returns the Vec3f in front or behind the provided entity's position coords with the given distance and angle (in degrees), use a negative distance for behind.
     **/
    public Vec3 getFacingPosition(LivingEntity entity, double distance, double angle) {
        angle = Math.toRadians(angle);
        double xAmount = -Math.sin(angle);
        double zAmount = Math.cos(angle);
        return new Vec3(entity.position().x() + (distance * xAmount), entity.position().y(), entity.position().z() + (distance * zAmount));
    }
}
