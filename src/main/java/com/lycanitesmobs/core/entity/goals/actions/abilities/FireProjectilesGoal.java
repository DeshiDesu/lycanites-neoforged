package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.goals.BaseGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

import java.util.EnumSet;


public class FireProjectilesGoal extends BaseGoal {
    protected String projectileName;
    protected Class<? extends BaseProjectileEntity> projectileClass;
    protected float velocity = 0.6F;
    protected float inaccuracy = 0F;
    protected float scale = 1F;
    protected float angle = 0F;
    protected Vector3d offset = new Vector3d(0.0, 0.0, 0.0);
    protected int fireRate = 60;
    protected boolean allPlayers = false;
    protected int randomCount = 0;
    protected int phase = -1;
    protected boolean overhead = false;

    private int cooldownTime = 0;
    private Entity attackTarget;


    /**
     * Constrcutor
     *
     * @param setHost The creature using this goal.
     */
    public FireProjectilesGoal(BaseCreatureEntity setHost) {
        super(setHost);
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    /**
     * Sets the battle phase to restrict this goal to.
     *
     * @param phase The phase to restrict to, if below 0 phases are ignored.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setPhase(int phase) {
        this.phase = phase;
        return this;
    }

    /**
     * Sets the projectile via info to fire.
     *
     * @param projectileName The projectile via name to fire.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setProjectile(String projectileName) {
        this.projectileName = projectileName;
        return this;
    }

    /**
     * Sets the projectile class to fire.
     *
     * @param projectileClass The projectile class to fire.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setProjectile(Class<? extends BaseProjectileEntity> projectileClass) {
        this.projectileClass = projectileClass;
        return this;
    }

    /**
     * Sets the rate of firing (in ticks).
     *
     * @param fireRate The firing tick rate.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setFireRate(int fireRate) {
        this.fireRate = fireRate;
        return this;
    }

    /**
     * Sets the velocity of firing.
     *
     * @param velocity The firing velocity.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setVelocity(float velocity) {
        this.velocity = velocity;
        return this;
    }

    /**
     * Sets the scale of the projectile.
     *
     * @param scale The projectile scale.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setScale(float scale) {
        this.scale = scale;
        return this;
    }

    /**
     * Sets the inaccuracy of firing.
     *
     * @param inaccuracy The firing inaccuracy.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setInaccuracy(int inaccuracy) {
        this.inaccuracy = inaccuracy;
        return this;
    }

    /**
     * Sets the angle offset of firing.
     *
     * @param angle The firing angle offset.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setAngle(int angle) {
        this.angle = angle;
        return this;
    }

    /**
     * Sets the xyz offset to fire from.
     *
     * @param offset The firing angle offset.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setOffset(Vector3d offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Sets overhead projectile summoning where instead the projectile is spawned above the target.
     *
     * @param overhead Whether to enable overhead or not.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setOverhead(boolean overhead) {
        this.overhead = overhead;
        return this;
    }

    /**
     * Sets if projectiles should be fired at all players.
     *
     * @param allPlayers True to target all players (requires FindNearbyPlayers goal) otherwise the current attack target is used.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setAllPlayers(boolean allPlayers) {
        this.allPlayers = allPlayers;
        return this;
    }

    /**
     * Sets random amount of projectiles to fire everywhere.
     *
     * @param randomCount The amount of projectiles to randomly fire, o to disable.
     * @return This goal for chaining.
     */
    public FireProjectilesGoal setRandomCount(int randomCount) {
        this.randomCount = randomCount;
        return this;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (this.projectileName == null && this.projectileClass == null) {
            return false;
        }

        this.attackTarget = this.host.getTarget();
        if (!this.allPlayers && this.randomCount <= 0 && this.attackTarget == null) {
            return false;
        }

        return this.phase < 0 || this.phase == this.host.getBattlePhase();
    }

    @Override
    public void start() {
        this.cooldownTime = 1;
    }

    @Override
    public void stop() {
        this.attackTarget = null;
    }

    @Override
    public void tick() {
        if (--this.cooldownTime > 0) {
            return;
        }
        this.cooldownTime = Math.round((float) ((1.0D / this.host.getAttribute(BaseCreatureEntity.RANGED_SPEED).getValue()) * this.fireRate));

        // All Players Mode:
        if (this.allPlayers) {
            for (Player target : this.host.playerTargets) {
                if (target.getAbilities().invulnerable || target.isSpectator())
                    continue;
                this.fireProjectile(target);
            }
            return;
        }

        // Random Mode:
        if (this.randomCount > 0) {
            for (int i = 0; i < this.randomCount; i++) {
                this.host.fireProjectile(this.projectileName, null, this.host.getRandom().nextFloat() * 20, this.host.getRandom().nextFloat() * this.angle, this.offset, this.velocity, this.scale, this.inaccuracy);
            }
            return;
        }

        // Single Target Mode:
        this.fireProjectile(this.attackTarget);
    }

    /**
     * Fires a projectile towards a target.
     *
     * @param target The target to fire at.
     */
    public BaseProjectileEntity fireProjectile(Entity target) {
        BaseProjectileEntity projectile = this.createProjectile();
        if (this.overhead) {
            this.fireProjectileOverhead(projectile);
        } else {
            this.host.fireProjectile(projectile, target, (float) this.host.position().distanceTo(target.position()), this.angle, this.offset, this.velocity, this.scale, this.inaccuracy);
        }
        return projectile;
    }

    public BaseProjectileEntity createProjectile() {
        BaseProjectileEntity projectile = null;
        if (this.projectileName != null) {
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
            if (projectileInfo == null) {
                return projectile;
            }
            projectile = projectileInfo.createProjectile(this.host.getCommandSenderWorld(), this.host);
        } else if (this.projectileClass != null) {
            projectile = ProjectileManager.getInstance().createOldProjectile(this.projectileClass, this.host.getCommandSenderWorld(), this.host);
        }

        if (projectile != null) {
            projectile.setProjectileScale(this.scale);
        }
        return projectile;
    }

    /**
     * Moves the provided projectile so that it is above the target and will drop down on them.
     * If there is no target, the host entity will drop a projectile onto themselves instead.
     *
     * @param projectile The newly fired projectile.
     */
    public void fireProjectileOverhead(BaseProjectileEntity projectile) {
        Entity target = this.attackTarget != null ? this.attackTarget : this.host;
        projectile.setPos(target.position().x, target.position().y + this.offset.y + this.host.getRandom().nextDouble() * 3, target.position().z);
        projectile.shoot(0, -1, 0, 0.5F, this.inaccuracy);
        this.host.getCommandSenderWorld().addFreshEntity(projectile);
    }
}
