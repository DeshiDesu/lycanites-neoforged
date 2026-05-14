package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Iterator;
import java.util.List;

public class DefendVillageGoal extends FindAttackTargetGoal {
	private final TargetingConditions villageSearchPredicate = TargetingConditions.forCombat().range(64.0D);

	public DefendVillageGoal(BaseCreatureEntity setHost) {
		super(setHost);
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	@Override
	public boolean canUse() {
		if(this.host.getOwner() != null) {
			return false;
		}

		AABB villageSearchArea = this.host.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
		List<Villager> villagers = this.host.level().getNearbyEntities(Villager.class, this.villageSearchPredicate, this.host, villageSearchArea);
		List<Player> players = this.host.level().getNearbyPlayers(this.villageSearchPredicate, this.host, villageSearchArea);
		Iterator villagerIter = villagers.iterator();

		while(villagerIter.hasNext()) {
			LivingEntity livingEntity = (LivingEntity)villagerIter.next();
			Villager villagerEntity = (Villager)livingEntity;
			Iterator playerIter = players.iterator();

			while(playerIter.hasNext()) {
				Player playerEntity = (Player)playerIter.next();
				int reputation = villagerEntity.getPlayerReputation(playerEntity);
				if (reputation <= -100) {
					this.target = playerEntity;
				}
			}
		}

		return this.isEntityTargetable(this.target, false);
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void start() {
		super.start();
	}

}
