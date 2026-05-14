package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class WorldSpawnTrigger extends SpawnTrigger {
	/** Has a random chance of triggering after so many world ticks. **/

	/** How many ticks between trigger attempts. **/
	public double tickRate = 400;

	/** The minimum distance the player must be away from the last tick position. **/
	public double lastTickDistanceMin = -1;

	/** The maximum distance the player must be away from the last tick position. **/
	public double lastTickDistanceMax = -1;

	/** If true, the world time is used instead of a generic world tick count. **/
	public boolean useWorldTime = false;

	/** Tracks the first tick to prevent instant firing on world load. **/
	private long firstTick = -1;


	/** Constructor **/
	public WorldSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("tickRate"))
			this.tickRate = json.get("tickRate").getAsDouble();

		if(json.has("lastTickDistanceMin"))
			this.lastTickDistanceMin = json.get("lastTickDistanceMin").getAsDouble();

		if(json.has("lastTickDistanceMax"))
			this.lastTickDistanceMax = json.get("lastTickDistanceMax").getAsDouble();

		if(json.has("useWorldTime"))
			this.useWorldTime = json.get("useWorldTime").getAsBoolean();

		super.loadFromJSON(json);
	}


	/** Called every world tick from where players are active. **/
	public void onTick(Level world, BlockPos position, long ticks) {
		long tickRateLong = (long) this.tickRate;

		if (this.firstTick < 0) {
			this.firstTick = ticks;
			return;
		}

		// World Time:
		if(this.useWorldTime && world.getDayTime() % 24000 != tickRateLong) {
			return;
		}

		// Tick Rate:
		else if(ticks == 0 || ticks % tickRateLong != 0) {
			return;
		}

		// Chance:
		if(this.chance < 1 && world.random.nextDouble() > this.chance) {
			return;
		}

		this.trigger(world, null, position, 0, 0);
	}
}
