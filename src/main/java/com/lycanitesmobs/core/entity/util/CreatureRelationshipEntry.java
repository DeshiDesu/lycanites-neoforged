package com.lycanitesmobs.core.entity.util;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class CreatureRelationshipEntry {
	protected CreatureRelationships creatureRelationships;
	protected UUID targetEntityUUID;
	protected int reputation = 0;

	/**
	 * Constructor
	 * @param creatureRelationships The Creature Relationships that this entry belongs to.
	 */
	public CreatureRelationshipEntry(CreatureRelationships creatureRelationships) {
		this.creatureRelationships = creatureRelationships;
	}

	/**
	 * Gets the reputation of this relationship.
	 * @return The reputation of this relationship.
	 */
	public int getReputation() {
		return this.reputation;
	}

	/**
	 * Sets the reputation of this relationship.
	 */
	public void setReputation(int reputation) {
		this.reputation = reputation;
		this.creatureRelationships.getCreatureEntity().queueSync();
	}

	/**
	 * Increases reputation by the provided amount.
	 * @param amount The amount of reputation to increase by.
	 * @return Returns true if the reputation has increased, false otherwise (if it is capped, etc).
	 */
	public boolean increaseReputation(int amount) {
		int max = this.creatureRelationships.getCreatureEntity().creatureInfo.getTamingReputation();
		int min = -this.creatureRelationships.getCreatureEntity().creatureInfo.getTamingReputation();
		if (this.reputation >= max) {
			return false;
		}
		this.reputation = Math.max(Math.min(this.reputation + amount, max), min);
		this.creatureRelationships.getCreatureEntity().queueSync();
		return true;
	}

	/**
	 * Decreases reputation by the provided amount.
	 * @param amount The amount of reputation to decrease by.
	 * @return Returns true if the reputation has decreased, false otherwise (if it is capped, etc).
	 */
	public boolean decreaseReputation(int amount) {
		int max = this.creatureRelationships.getCreatureEntity().creatureInfo.getTamingReputation();
		int min = -this.creatureRelationships.getCreatureEntity().creatureInfo.getTamingReputation();
		if (this.reputation <= min) {
			return false;
		}
		this.reputation = Math.max(Math.min(this.reputation - amount, max), min);
		this.creatureRelationships.getCreatureEntity().queueSync();
		return true;
	}

	/**
	 * Whether this reputation should cause the target of being capable of attacked or not.
	 * @return True if the target can be attacked at all, false if not.
	 */
	public boolean canAttack() {
		return this.reputation < this.creatureRelationships.getCreatureEntity().creatureInfo.getTamingReputation();
	}

	/**
	 * Whether this reputation can allow the target to be attacked on sight when aggressive.
	 * @return True if the target can be hunted, false if not.
	 */
	public boolean canHunt() {
		return this.reputation < this.creatureRelationships.getCreatureEntity().creatureInfo.getFriendlyReputation();
	}

	/**
	 * Whether this reputation should cause the target to be defended on sight when in the  defensive state or above.
	 * @return True if the target should be defended, false if not.
	 */
	public boolean shouldDefend() {
		return this.reputation >= this.creatureRelationships.getCreatureEntity().creatureInfo.getFriendlyReputation();
	}

	/**
	 * Gets the UUID of the Target Entity that this relationship is for.
	 * @return The target entity UUID.
	 */
	public UUID getTargetEntityUUID() {
		return this.targetEntityUUID;
	}

	/**
	 * Sets the target entity that this Relationship Entry is for.
	 */
	public void setTarget(Entity entity) {
		this.targetEntityUUID = entity.getUUID();
	}

	/**
	 * Loads relationships from nbt data.
	 * @param nbt The nbt data to load from.
	 */
	public void load(CompoundTag nbt) {
		if(nbt.hasUUID("TargetUUID")) {
			this.targetEntityUUID = nbt.getUUID("TargetUUID");
		}
		if(nbt.contains("Reputation")) {
			this.setReputation(nbt.getInt("Reputation"));
		}
	}

	/**
	 * Saves relationships to nbt data.
	 * @param nbt The nbt data to save to.
	 */
	public void save(CompoundTag nbt) {
		nbt.putUUID("TargetUUID", this.targetEntityUUID);
		nbt.putInt("Reputation", this.reputation);
	}
}
