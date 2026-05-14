package com.lycanitesmobs.core.container.slot;

import com.lycanitesmobs.core.container.block.EquipmentStationContainer;
import com.lycanitesmobs.core.container.base.BaseSlot;
import com.lycanitesmobs.core.manager.ItemManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EquipmentStationRepairSlot extends BaseSlot {
	public EquipmentStationContainer container;

	/**
	 * Constructor
	 * @param container The Equipment Infuser Container using this slot.
	 * @param slotIndex THe index of this slot.
	 * @param x The x display position.
	 * @param y The y display position.
	 */
	public EquipmentStationRepairSlot(EquipmentStationContainer container, int slotIndex, int x, int y) {
		super(container.equipmentStation, slotIndex, x, y);
		this.container = container;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return ItemManager.getInstance().getEquipmentSharpnessRepair(itemStack) > 0 || ItemManager.getInstance().getEquipmentManaRepair(itemStack) > 0;
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
		return 64;
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

	/**
	 * Called when an ItemStack is removed into this slot.
	 * @param itemStack The ItemStack being inserted.
	 */
	@Override
	public void onTake(Player player, ItemStack itemStack) {
		super.onTake(player, itemStack);
	}

	@Override
	public boolean mayPickup(Player player) {
		return true;
	}
}
