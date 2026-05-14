package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class KillSpawnTrigger extends EntitySpawnTrigger {


	/** Constructor **/
	public KillSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);
	}


	/** Called every time a player kills an entity. **/
	public void onKill(Player player, LivingEntity killedEntity) {

		// Check Entity:
		if(!this.isMatchingEntity(killedEntity)) {
			return;
		}

		// Chance:
		if(this.chance < 1 && player.getRandom().nextDouble() > this.chance) {
			return;
		}

		this.trigger(player.getCommandSenderWorld(), player, killedEntity.blockPosition(), 0, 0);
	}
}
