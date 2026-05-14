package com.lycanitesmobs.core.event.mobevent.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.info.altar.AltarInfo;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AltarMobEventTrigger extends MobEventTrigger {
	/** The Altar to trigger from. TODO Altars should be created via JSON here instead. **/
	public AltarInfo altar;


	/** Constructor **/
	public AltarMobEventTrigger(MobEvent mobEvent) {
		super(mobEvent);
	}


	/** Loads this Mob Event Trigger from the provided JSON data. **/
	public void loadFromJSON(JsonObject json) {
		this.altar = AltarInfo.getAltar(json.get("altarName").getAsString());
		if(this.altar != null)
			this.altar.mobEventTrigger = this;

		super.loadFromJSON(json);
	}


	/**
	 * Called when the associated altar has been activated for this trigger.
	 * @param entity
	 * @param world The world that the Altar was activated in.
	 * @param pos The central position of the Altar.
	 * @param variant The level of the activated the Altar.
	 * @return
	 */
	public boolean onActivate(Entity entity, Level world, BlockPos pos, int variant) {
		Player player = null;
		if(entity instanceof Player) {
			player = (Player)entity;
		}
		if(!this.canTrigger(world, player)) {
			return false;
		}
		return this.trigger(world, player, pos, 1, variant);
	}


	/**
	 * Called when this AltarMobEventTrigger is removed.
	 */
	public void onRemove() {
		if(this.altar != null)
			this.altar.mobEventTrigger = null;
	}
}
