package com.lycanitesmobs.core.event.mobevent.effects;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CommandMobEventEffect extends MobEventEffect {
	/** The command to run. **/
	public String command = "";


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);

		if(json.has("command"))
			this.command = json.get("command").getAsString();
	}

	@Override
	public void onUpdate(Level world, Player player, BlockPos pos, int level, int ticks, int variant) {
		super.onUpdate(world, player, pos, level, ticks, variant);
		if(!this.canActivate(world, player, pos, level, ticks)) {
			return;
		}

		MinecraftServer server = world.getServer();
		if(server != null) {
			server.getCommands().performCommand(null, this.command);
		}
	}
}
