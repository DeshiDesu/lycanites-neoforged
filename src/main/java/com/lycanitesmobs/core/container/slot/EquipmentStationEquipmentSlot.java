package com.lycanitesmobs.core.container.slot;

import com.lycanitesmobs.core.container.block.EquipmentStationContainer;
import com.lycanitesmobs.core.container.base.BaseSlot;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EquipmentStationEquipmentSlot extends BaseSlot {
	public EquipmentStationContainer container;

	/**
	 * Constructor
	 * @param stationContainer The Equipment Station Container using this slot.
	 * @param slotIndex THe index of this slot.
	 * @param x The x display position.
	 * @param y The y display position.
	 */
	public EquipmentStationEquipmentSlot(EquipmentStationContainer stationContainer, int slotIndex, int x, int y) {
		super(stationContainer.equipmentStation, slotIndex, x, y);
		this.container = stationContainer;
	}


	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return itemStack.getItem() instanceof ItemEquipment;
	}


	/**
	 * Returns true if this slot has an item stack.
	 * @return True if this slot has a valid item stack.
	 */
	@Override
	public boolean hasItem() {
		return super.hasItem();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
    }


	/**
	 * Called when an ItemStack is inserted into this slot.
	 * @param itemStack The ItemStack being inserted.
	 */
	@Override
	public void set(ItemStack itemStack) {
		super.set(itemStack);
		this.container.attemptRepair();
	}


	@Override
	public void onTake(Player player, ItemStack itemStack) {
		super.onTake(player, itemStack);
	}


	@Override
	public boolean mayPickup(Player player) {
		return true;
	}
}
