package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ChunkSpawnTrigger extends SpawnTrigger {
	/** Has a random chance of triggering after so many player ticks. **/


	/** Constructor **/
	public ChunkSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);
	}


	/** Called every time a new chunk is generated. **/
	public boolean onChunkPopulate(Level world, ChunkPos chunkPos) {
		// Chance:
		if(this.chance < 1 && world.random.nextDouble() > this.chance) {
			return false;
		}

		return this.trigger(world, null, new BlockPos(chunkPos.getMinBlockX() + 8, world.getSeaLevel(), chunkPos.getMinBlockZ() + 8), 0, 0);
	}
}
