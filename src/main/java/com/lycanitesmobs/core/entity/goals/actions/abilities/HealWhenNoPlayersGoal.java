package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class HealWhenNoPlayersGoal extends Goal {
	BaseCreatureEntity host;

	// Targets:
	public List<Player> playerTargets = new ArrayList<>();
	public boolean firstPlayerTargetCheck = false;

    // Properties:
    private float healAmount = 50;

	/**
	 * Constrcutor
	 * @param setHost The creature using this goal.
	 */
	public HealWhenNoPlayersGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
		this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

	/**
	 * Sets how much this creature heals by.
	 * @param healAmount The amount to heal by.
	 * @return This goal for chaining.
	 */
	public HealWhenNoPlayersGoal setHealAmount(float healAmount) {
    	this.healAmount = healAmount;
    	return this;
    }

	@Override
    public boolean canUse() {
		return this.host.isAlive();
    }

	@Override
    public boolean canContinueToUse() {
        return this.host.isAlive();
    }

	@Override
    public void start() {}

	@Override
    public void stop() {
		this.firstPlayerTargetCheck = false;
	}

	@Override
    public void tick() {
		if(this.host.updateTick % 200 != 0 || !this.firstPlayerTargetCheck) {
			return;
		}
		this.firstPlayerTargetCheck = true;
		this.playerTargets = this.host.getNearbyEntities(Player.class, null, 64);
		if (this.host.updateTick % 20 == 0 && this.playerTargets.isEmpty()) {
			this.host.heal(this.healAmount);
		}
    }
}
