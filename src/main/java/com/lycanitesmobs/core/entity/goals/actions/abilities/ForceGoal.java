package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;


public class ForceGoal extends Goal {
	BaseCreatureEntity host;

    // Properties:
	protected int duration = 10 * 20;
	protected int cooldownDuration = 15 * 20;
	protected int windUp = 3 * 20;
	protected float range = 15 * 20;
	protected float force = 1F;
	protected int phase = -1;

	public int abilityTime = 0;
	public int cooldownTime = this.cooldownDuration;
	public boolean windUpForce = false;
	public boolean dismountTargets = false;


	/**
	 * Constrcutor
	 * @param setHost The creature using this goal.
	 */
	public ForceGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
    }

	/**
	 * Sets the battle phase to restrict this goal to.
	 * @param phase The phase to restrict to, if below 0 phases are ignored.
	 * @return This goal for chaining.
	 */
	public ForceGoal setPhase(int phase) {
		this.phase = phase;
		return this;
	}

	/**
	 * Sets the duration of firing (in ticks).
	 * @param duration The firing duration.
	 * @return This goal for chaining.
	 */
	public ForceGoal setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	/**
	 * Sets the cooldown after firing (in ticks).
	 * @param cooldown The cooldown.
	 * @return This goal for chaining.
	 */
	public ForceGoal setCooldown(int cooldown) {
		this.cooldownDuration = cooldown;
		this.cooldownTime = cooldown;
		return this;
	}

	/**
	 * Sets how long it takes for the pull/push to wind up (in ticks).
	 * @param windUp The wind up duration.
	 * @return This goal for chaining.
	 */
	public ForceGoal setWindUp(int windUp) {
		this.windUp = windUp;
		return this;
	}

	/**
	 * Sets the range of force.
	 * @param range The range.
	 * @return This goal for chaining.
	 */
	public ForceGoal setRange(float range) {
		this.range = range;
		return this;
	}

	/**
	 * Sets the pull/push force.
	 * @param force The force, positive pushes, negative pulls.
	 * @return This goal for chaining.
	 */
	public ForceGoal setForce(float force) {
		this.force = force;
		return this;
	}

	/**
	 * Sets if a weaker force should be applied as this goal winds up.
	 * @param windUpForce Whether affected targets should be dismounted.
	 * @return This goal for chaining.
	 */
	public ForceGoal setWindUpForce(boolean windUpForce) {
		this.windUpForce = windUpForce;
		return this;
	}

	/**
	 * Sets if this force goal should dismount any targets affected that are riding another entity.
	 * @param dismountTargets Whether affected targets should be dismounted.
	 * @return This goal for chaining.
	 */
	public ForceGoal setDismount(boolean dismountTargets) {
		this.dismountTargets = dismountTargets;
		return this;
	}

	@Override
    public boolean canUse() {
		if(!this.host.isAlive()) {
			return false;
		}

		if(this.phase >= 0 && this.phase != this.host.getBattlePhase()) {
			return false;
		}

		return true;
    }

	@Override
	public void start() {
		this.cooldownTime = this.cooldownDuration;
	}

	@Override
    public void tick() {
		if(this.cooldownTime-- > 0) {
			this.abilityTime = 0;
			return;
		}

		if(this.abilityTime == this.windUp) {
			this.host.playAttackSound();
		}

		if(this.abilityTime++ >= this.duration && this.cooldownDuration > 0) {
			this.cooldownTime = this.cooldownDuration;
			return;
		}

		double motionCap = -this.force;
		double factor = -this.force * 0.1D;
		if(this.abilityTime < this.windUp) {
			if(!this.windUpForce) {
				return;
			}
			factor *= (double)this.abilityTime / this.windUp;
		}
		for(Entity entity : this.host.getNearbyEntities(Entity.class, this::isValidTarget, this.range)) {
			if(!(entity instanceof LivingEntity)) {
				continue;
			}
			double xDist = this.host.position().x() - entity.position().x();
			double yDist = this.host.position().y() - entity.position().y();
			double zDist = this.host.position().z() - entity.position().z();
			double xzDist = Math.max(Mth.sqrt(LMHelperClass.convertToFloat(xDist * xDist + zDist * zDist)), 0.01D);
			ServerPlayer player = null;
			if (entity instanceof ServerPlayer) {
				player = (ServerPlayer) entity;
			}
			if (entity.getDeltaMovement().x() < motionCap && entity.getDeltaMovement().x() > -motionCap && entity.getDeltaMovement().z() < motionCap && entity.getDeltaMovement().z() > -motionCap) {
				entity.push(
						((xDist / xzDist) * factor) + (entity.getDeltaMovement().x() * factor),
						(yDist * factor * 0.25D) + (entity.getDeltaMovement().y() * factor * 0.25D),
						((zDist / xzDist) * factor) + (entity.getDeltaMovement().z() * factor)
				);
			}
			if(this.dismountTargets && entity.getVehicle() != null) {
				entity.stopRiding();
			}
			if (player != null) {
				player.connection.send(new ClientboundSetEntityMotionPacket(entity));
			}
		}
    }

	public boolean isValidTarget(Entity entity) {
		if(entity == this.host) {
			return false;
		}
		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (player.isCreative() || player.isSpectator()) {
				return false;
			}
		}
		return true;
	}
}
