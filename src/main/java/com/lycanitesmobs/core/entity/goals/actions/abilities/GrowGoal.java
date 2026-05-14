package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class GrowGoal extends Goal {
	BaseCreatureEntity host;

    // Properties:
	protected int tickRate = 1 * 20;
	protected float growthAmount = 0.1F;
	protected int phase = -1;

	public int abilityTime = 0;

	/**
	 * Constrcutor
	 * @param setHost The creature using this goal.
	 */
	public GrowGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
    }

	/**
	 * Sets the tick rate of this goal.
	 * @param tickRate The tick rate.
	 * @return This goal for chaining.
	 */
	public GrowGoal setTickRate(int tickRate) {
    	this.tickRate = tickRate;
    	return this;
    }

	/**
	 * Sets how much to grow by each interval.
	 * @param growthAmount The growth amount.
	 * @return This goal for chaining.
	 */
	public GrowGoal setGrowthAmount(float growthAmount) {
		this.growthAmount = growthAmount;
		return this;
	}

	/**
	 * Sets the battle phase to restrict this goal to.
	 * @param phase The phase to restrict to, if below 0 phases are ignored.
	 * @return This goal for chaining.
	 */
	public GrowGoal setPhase(int phase) {
		this.phase = phase;
		return this;
	}

	@Override
    public boolean canUse() {
		if(!this.host.isAlive() ) {
			return false;
		}
		return this.phase < 0 || this.phase == this.host.getBattlePhase();
    }

    @Override
	public void start() {
		this.abilityTime = 0;
	}

	@Override
    public void tick() {
		if(this.abilityTime++ % this.tickRate != 0) {
			return;
		}

		this.host.setSizeScale(this.host.sizeScale + this.growthAmount);
    }
}
