package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import net.minecraft.world.entity.LivingEntity;

public class EntitySpawnedSpawnTrigger extends EntitySpawnTrigger {

	/** Constructor **/
	public EntitySpawnedSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);
	}


	/** Called every time an entity is spawned. **/
	public void onEntitySpawned(LivingEntity spawnedEntity) {

		// Check Entity:
		if(!this.isMatchingEntity(spawnedEntity)) {
			return;
		}

		// Chance:
		if(this.chance < 1 && spawnedEntity.getRandom().nextDouble() > this.chance) {
			return;
		}

		this.trigger(spawnedEntity.getCommandSenderWorld(), null, spawnedEntity.blockPosition(), 0, 0);
	}
}
