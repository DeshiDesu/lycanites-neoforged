package com.lycanitesmobs.core.event.mobevent.effects;


import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public abstract class StructureBuilder {
	public static Map<String, StructureBuilder> STRUCTURE_BUILDERS = new HashMap<>();

	public String name;

	/** Gets a Structure Builder by name. **/
	public static StructureBuilder getStructureBuilder(String name) {
		if(STRUCTURE_BUILDERS.containsKey(name)) {
			return STRUCTURE_BUILDERS.get(name);
		}
		return null;
	}


	/** Adds a new Structure Builder. **/
	public static void addStructureBuilder(StructureBuilder structureBuilder) {
		STRUCTURE_BUILDERS.put(structureBuilder.name, structureBuilder );
	}


	public abstract void build(Level world, Player player, BlockPos pos, int level, int ticks, int variant);

	protected boolean spawnDeferred(Level world, BlockPos pos, String key, Entity entity, Runnable afterSpawn) {
		if (world instanceof ServerLevel serverLevel) {
			return DeferredLevelActionManager.enqueue(serverLevel, pos, key, level -> {
				level.addFreshEntity(entity);
				if (afterSpawn != null) {
					afterSpawn.run();
				}
			});
		}

		world.addFreshEntity(entity);
		if (afterSpawn != null) {
			afterSpawn.run();
		}
		return true;
	}
}
