package com.lycanitesmobs.core.event.mobevent.effects;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StructureMobEventEffect extends MobEventEffect {

	/** The Structure Builder to activate. TODO Replace with JSON Structures. **/
	StructureBuilder structureBuilder;


	@Override
	public void loadFromJSON(JsonObject json) {
		this.structureBuilder = StructureBuilder.getStructureBuilder(json.get("structureBuilderName").getAsString());

		super.loadFromJSON(json);
	}


	@Override
	public void onUpdate(Level world, Player player, BlockPos pos, int level, int ticks, int variant) {
		if(this.structureBuilder != null) {
			this.structureBuilder.build(world, player, pos, level, ticks, variant);
		}
	}
}
