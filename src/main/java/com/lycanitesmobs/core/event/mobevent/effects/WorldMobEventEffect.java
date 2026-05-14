package com.lycanitesmobs.core.event.mobevent.effects;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;

public class WorldMobEventEffect extends MobEventEffect {

	/** Controls the rain. Can be: none, start or stop. **/
	protected String rain = "none";

	/** Controls the thundering. Can be: none, start or stop. **/
	protected String thunder = "none";

	/** If set, changes the time of day. 20000 is good for night time events. This only moves the day time forwards and not backwards. **/
	protected int dayTime = -1;


	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("rain"))
			this.rain = json.get("rain").getAsString();

		if(json.has("thunder"))
			this.thunder = json.get("thunder").getAsString();

		if(json.has("dayTime"))
			this.dayTime = json.get("dayTime").getAsInt();

		super.loadFromJSON(json);
	}


	@Override
	public void onUpdate(Level world, Player player, BlockPos pos, int level, int ticks, int variant) {
		if(ticks == 0) {
			ServerLevelData serverWorldInfo = null;
			if (world instanceof ServerLevelData) {
				serverWorldInfo = (ServerLevelData)world;
			}

			// Rain:
			if ("start".equalsIgnoreCase(this.rain)) {
				world.getLevelData().setRaining(true);
			} else if ("stop".equalsIgnoreCase(this.rain)) {
				world.getLevelData().setRaining(false);
			}

			// Lightning:
			if (serverWorldInfo != null) {
				if ("start".equalsIgnoreCase(this.thunder)) {
					serverWorldInfo.setThundering(true);
				} else if ("stop".equalsIgnoreCase(this.thunder)) {
					serverWorldInfo.setThundering(false);
				}
			}

			// Day Time:
			if (this.dayTime >= 0) {
				int dayTime = 23999;
				long currentTime = world.getDayTime();
				int targetTime = this.dayTime;
				long excessTime = currentTime % dayTime;
				if (excessTime > targetTime) {
					targetTime += dayTime;
				}
				for (ServerLevel serverWorld : world.getServer().getAllLevels()) {
					serverWorld.setDayTime(currentTime - excessTime + targetTime);
				}
			}
		}
	}
}
