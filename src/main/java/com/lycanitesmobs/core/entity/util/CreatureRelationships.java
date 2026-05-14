package com.lycanitesmobs.core.entity.util;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

public class CreatureRelationships {
	protected BaseCreatureEntity creatureEntity;
	protected Map<UUID, CreatureRelationshipEntry> relationships = new HashMap<>();

	/**
	 * Constructor
	 * @param creatureEntity The Creature Entity these relationships are for.
	 */
	public CreatureRelationships(BaseCreatureEntity creatureEntity) {
		this.creatureEntity = creatureEntity;
	}

	/**
	 * Gets a Relationship Entry for the provided entity.
	 * @param entity The target entity to get a relationship for.
	 * @return The relationship for the target entity.
	 */
	@Nullable
	public CreatureRelationshipEntry getEntry(Entity entity) {
		if (this.relationships.containsKey(entity.getUUID())) {
			return this.relationships.get(entity.getUUID());
		}
		return null;
	}

	/**
	 * Returns all player entities that have a relationship and their reputation.
	 * @return A list of all player entities with a relationship entry.
	 */
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<>();

		if (this.getCreatureEntity().getCommandSenderWorld().isClientSide()) {
			return players;
		}

		for (CreatureRelationshipEntry relationshipEntry : this.relationships.values()) {
			Player player = this.getCreatureEntity().getCommandSenderWorld().getPlayerByUUID(relationshipEntry.getTargetEntityUUID());
			if (player != null) {
				players.add(player);
			}
		}
		return players;
	}

	/**
	 * Gets or Creates if not present, a Relationship Entry for the provided entity.
	 * @param entity The target entity to get or create a relationship for.
	 * @return The relationship for the target entity.
	 */
	public CreatureRelationshipEntry getOrCreateEntry(Entity entity) {
		CreatureRelationshipEntry relationshipEntry = this.getEntry(entity);
		if (relationshipEntry == null) {
			relationshipEntry = new CreatureRelationshipEntry(this);
			relationshipEntry.setTarget(entity);
			this.relationships.put(relationshipEntry.getTargetEntityUUID(), relationshipEntry);
		}
		return relationshipEntry;
	}

	/**
	 * Gets the Creature Entity that these relationships belong to.
	 * @return The Creature Entity.
	 */
	public BaseCreatureEntity getCreatureEntity() {
		return this.creatureEntity;
	}

	/**
	 * Loads relationships from nbt data.
	 * @param nbt The nbt data to load from.
	 */
	public void load(CompoundTag nbt) {
		if(nbt.contains("CreatureRelationships")) {
			this.relationships.clear();
			ListTag relationshipNbtList = nbt.getList("CreatureRelationships", 10);
			for(int i = 0; i < relationshipNbtList.size(); i++) {
				CompoundTag relationshipNbt = relationshipNbtList.getCompound(i);
				CreatureRelationshipEntry creatureRelationshipEntry = new CreatureRelationshipEntry(this);
				creatureRelationshipEntry.load(relationshipNbt);
				this.relationships.put(creatureRelationshipEntry.getTargetEntityUUID(), creatureRelationshipEntry);
			}
		}
	}

	/**
	 * Saves relationships to nbt data.
	 * @param nbt The nbt data to save to.
	 */
	public void save(CompoundTag nbt) {
		ListTag relationshipNbtList = new ListTag();
		for (CreatureRelationshipEntry creatureRelationshipEntry : this.relationships.values()) {
			CompoundTag relationshipNbt = new CompoundTag();
			creatureRelationshipEntry.save(relationshipNbt);
			relationshipNbtList.add(relationshipNbt);
		}
		nbt.put("CreatureRelationships", relationshipNbtList);
	}
}
