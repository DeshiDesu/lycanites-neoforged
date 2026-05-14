package com.lycanitesmobs.core.event.mobevent;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.event.MobEventListener;
import com.lycanitesmobs.core.manager.MobEventManager;
import net.minecraft.world.level.Level;

public class MobEventSchedule {
	/** The Mob Event that this Schedule starts. **/
	public MobEvent mobEvent;

	/** The dimension ID of the world. **/
	public String dimensionId = "";

	/** The day of the world. **/
	public int worldDay = 0;

	/** The day time (ticks) of the world. **/
	public int dayTime = 100;


	/** Creates a Mob Event Schedule from the provided JSON data. **/
	public static MobEventSchedule createFromJSON(JsonObject json) {
		MobEvent mobEvent = null;
		if(json.has("eventName")) {
			mobEvent = MobEventManager.getInstance().getMobEvent(json.get("eventName").getAsString());
		}
		MobEventSchedule mobEventSchedule = new MobEventSchedule(mobEvent);
		mobEventSchedule.loadFromJSON(json);
		return mobEventSchedule;
	}


	/** Constructor **/
	public MobEventSchedule(MobEvent mobEvent) {
		this.mobEvent = mobEvent;
	}


	/** Loads this Mob Event Trigger from the provided JSON data. **/
	public void loadFromJSON(JsonObject json) {
		if(json.has("dimensionId"))
			this.dimensionId = json.get("dimensionId").getAsString();

		if(json.has("worldDay"))
			this.worldDay = json.get("worldDay").getAsInt();

		if(json.has("dayTime"))
			this.dayTime = json.get("dayTime").getAsInt();
	}


	/**
	 * Returns true if this scheduled event can start on the provided world.
	 * @return
	 */
	public boolean canStart(Level world) {
		if(world == null) {
			return false;
		}

		if(!world.dimension().location().toString().equals(this.dimensionId)) {
			return false;
		}

		int time = (int)Math.floor(world.getDayTime() % 24000D);
		int day = (int)(Math.floor(world.getGameTime()) / 23999D);

		if(day != this.worldDay) {
			return false;
		}

		if(time != this.dayTime) {
			return false;
		}

		return true;
	}


	/**
	 *
	 * @param worldExt
	 */
	public void start(ExtendedWorld worldExt) {
		if(this.mobEvent == null) {
			MobEventListener.getInstance().triggerRandomMobEvent(worldExt.world, worldExt, 1);
			return;
		}
		worldExt.startWorldEvent(this.mobEvent);
	}
}
