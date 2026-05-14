package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.BaseGoal;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class SummonMinionsGoal extends BaseGoal {
    // Properties:
	protected int summonTime = 0;
	protected int summonRate = 60;
	protected int summonCap = 5;
	protected CreatureInfo minionInfo;
	protected boolean perPlayer = false;
	protected boolean antiFlight = false;
	protected double sizeScale = 1;


	/**
	 * Constrcutor
	 * @param setHost The creature using this goal.
	 */
	public SummonMinionsGoal(BaseCreatureEntity setHost) {
		super(setHost);
		this.setFlags(EnumSet.noneOf(Flag.class));
    }

	/**
	 * Sets the rate of summoning (in ticks).
	 * @param summonRate The summoning tick rate.
	 * @return This goal for chaining.
	 */
	public SummonMinionsGoal setSummonRate(int summonRate) {
    	this.summonRate = summonRate;
    	return this;
    }

	/**
	 * Sets the minion count cap for summoning.
	 * @param summonCap The summoning cap.
	 * @return This goal for chaining.
	 */
	public SummonMinionsGoal setSummonCap(int summonCap) {
		this.summonCap = summonCap;
		return this;
	}

	/**
	 * If true, the cap is scaled per players detected.
	 * @param perPlayer True to enable.
	 * @return This goal for chaining.
	 */
	public SummonMinionsGoal setPerPlayer(boolean perPlayer) {
		this.perPlayer = perPlayer;
		return this;
	}

	/**
	 * Sets anti flight summoning where minions are summoned at any player targets that are flying.
	 * @param antiFlight True to enable.
	 * @return This goal for chaining.
	 */
	public SummonMinionsGoal setAntiFlight(boolean antiFlight) {
		this.antiFlight = antiFlight;
		return this;
	}

	/**
	 * Sets the creature to summon.
	 * @param creatureName The creature name to summon.
	 * @return This goal for chaining.
	 */
	public SummonMinionsGoal setMinionInfo(String creatureName) {
    	this.minionInfo = CreatureManager.getInstance().getCreature(creatureName);
    	return this;
    }

	/**
	 * Sets the scale to multiple the minion's size by.
	 * @param sizeScale The scale to multiple the creature's size by.
	 * @return This goal for chaining.
	 */
	public SummonMinionsGoal setSizeScale(double sizeScale) {
		this.sizeScale = sizeScale;
		return this;
	}

	@Override
    public boolean canUse() {
		if(this.host.isPetType("familiar")) {
			return false;
		}
		return super.canUse() && this.minionInfo != null;
	}

	@Override
    public void start() {
		this.summonTime = 1;
	}

	@Override
    public void tick() {
		if(this.summonTime++ % this.summonRate != 0) {
			return;
		}

		if(this.host.getMinions(this.minionInfo.getEntityType()).size() >= this.summonCap) {
			return;
		}

		// Anti Flight Mode:
		if(this.antiFlight) {
			for (Player target : this.host.playerTargets) {
				if(target.isCreative() || target.isSpectator())
					continue;
				if (CreatureManager.getInstance().config.bossAntiFlight > 0 && target.position().y() > this.host.position().y() + CreatureManager.getInstance().config.bossAntiFlight + 1) {
					this.summonMinion(target);
				}
			}
			return;
		}

		this.summonMinion(this.host.getTarget());
    }

	protected void summonMinion(LivingEntity target) {
		LivingEntity minion = this.minionInfo.createEntity(this.host.getCommandSenderWorld());
		this.host.summonMinion(minion, this.host.getRandom().nextDouble() * 360, this.host.getDimensions(this.host.getPose()).width + 1);
		if(minion instanceof BaseCreatureEntity) {
			BaseCreatureEntity minionCreature = (BaseCreatureEntity)minion;
			minionCreature.setTarget(target);
			minionCreature.setSizeScale(minionCreature.sizeScale * this.sizeScale);
		}
	}
}
