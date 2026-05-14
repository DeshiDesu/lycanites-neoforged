package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import com.lycanitesmobs.core.event.SpawnerEventListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerSpawnTrigger extends SpawnTrigger {
	/** Has a random chance of triggering after so many player ticks. **/

	/** If true, creative players will also activate this trigger. **/
	public boolean creative = false;

	/** How many ticks between trigger attempts. **/
	public double tickRate = 400;

	/** The minimum distance the player must be away from the last tick position. **/
	public double lastTickDistanceMin = -1;

	/** The maximum distance the player must be away from the last tick position. **/
	public double lastTickDistanceMax = -1;

	/** If true, the world time that the player is in is used instead of the player's ticks. **/
	public boolean useWorldTime = false;

	/** Stores the position of a player from the last tick for calculating distance. **/
	protected Map<Player, BlockPos> lastTickPositions = new HashMap<>();


	/** Constructor **/
	public PlayerSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("creative"))
			this.creative = json.get("creative").getAsBoolean();

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


	/** Called every player tick. **/
	public void onTick(Player player, long ticks) {
		// Creative:
		if(!SpawnerEventListener.testOnCreative && (!this.creative && (player.isCreative() || player.isSpectator()))) {
			return;
		}

		// World Time:
		if(this.useWorldTime && player.getCommandSenderWorld().getDayTime() % 24000 != this.tickRate) {
			return;
		}

		// Tick Rate:
		else if(ticks == 0 || ticks % this.tickRate != 0) {
			return;
		}

		// Chance:
		if(this.chance < 1 && player.getRandom().nextDouble() > this.chance) {
			return;
		}

		// Last Tick Distance:
		if(this.lastTickDistanceMin > -1 || this.lastTickDistanceMax > -1) {
			BlockPos playerPos = player.blockPosition();
			if (!this.lastTickPositions.containsKey(player)) {
				this.lastTickPositions.put(player, playerPos);
			}
			else {
				playerPos = this.lastTickPositions.get(player);
			}
			double lastTickDistance = player.blockPosition().distSqr(playerPos);
			if(this.lastTickDistanceMin > -1 && lastTickDistance < this.lastTickDistanceMin) {
				return;
			}
			if(this.lastTickDistanceMax > -1 && lastTickDistance > this.lastTickDistanceMax) {
				return;
			}
		}

		this.trigger(player.getCommandSenderWorld(), player, player.blockPosition(), 0, 0);
	}
}
