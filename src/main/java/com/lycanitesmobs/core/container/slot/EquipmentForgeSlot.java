package com.lycanitesmobs.core.container.slot;

import com.lycanitesmobs.core.container.base.BaseSlot;
import com.lycanitesmobs.core.container.block.EquipmentForgeContainer;
import com.lycanitesmobs.core.container.creature.CreatureInventory;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.item.equipment.features.EquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.SlotEquipmentFeature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EquipmentForgeSlot extends BaseSlot {
	public String type;
	public EquipmentForgeContainer containerForge;
	public List<EquipmentForgeSlot> childSlots = new ArrayList<>();

	/**
	 * Constructor
	 * @param containerForge The Equipment Forge Container using this slot.
	 * @param slotIndex THe index of this slot.
	 * @param x The x display position.
	 * @param y The y display position.
	 * @param type The Equipment item type for this slot. Can be any part slot type or "piece" for an assembled piece of equipment or "none" when unavailable.
	 */
	public EquipmentForgeSlot(EquipmentForgeContainer containerForge, int slotIndex, int x, int y, String type) {
		super(containerForge.equipmentForge, slotIndex, x, y);
		this.containerForge = containerForge;
		this.type = type;
	}


	@Override
	public boolean mayPlace(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if(item instanceof ItemEquipmentPart) {
			ItemEquipmentPart equipmentPart = (ItemEquipmentPart)item;
			return this.type.equals(equipmentPart.slotType) && equipmentPart.getPartLevel(itemStack) <= this.containerForge.equipmentForge.getForgeLevel();
		}
		else if(item instanceof ItemEquipment) {
			if(this.containerForge.equipmentForge.getForgeLevel() < 3) {
				ItemEquipment equipment = (ItemEquipment)item;
				int equipmentLevel = equipment.getHighestLevel(itemStack);
				if(equipmentLevel > this.containerForge.equipmentForge.getForgeLevel()) {
					return false;
				}
			}
			return this.type.equals("piece") && !this.hasItem();
		}
        return false;
    }


	/**
	 * Returns true if this slot has an item stack, unless it is an Equipment Piece with no parts.
	 * @return True if this slot has a valid item stack.
	 */
	@Override
	public boolean hasItem() {
		if(!super.hasItem()) {
			return false;
		}
		if(this.getItem().getItem() instanceof ItemEquipment) {
			ItemEquipment itemEquipment = (ItemEquipment) this.getItem().getItem();
			if(itemEquipment.getEquipmentPartCount(this.getItem()) == 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getMaxStackSize() {
		if(this.container instanceof CreatureInventory)
			if(((CreatureInventory)this.container).getTypeFromSlot(this.getSlotIndex()) != null)
				return 1;
        return this.container.getMaxStackSize();
    }


	/**
	 * Sets the Equipment Type that this slot can hold. If an item is present and doesn't match the new type it is destroyed.
	 * @param type The type to set the slot to.
	 */
	public void setType(String type) {
		this.type = type;
	}


	/**
	 * Adds a child equipment slot to this slot. Child slots have their type updated by their parents based on what is made available by the parent.
	 * @param slot The child slot to add. The first added should be the center, then left and then right (for all slots provided by Head pieces).
	 */
	public void addChildSlot(EquipmentForgeSlot slot) {
		if(this.childSlots.contains(slot)) {
			return;
		}
		this.childSlots.add(slot);
		this.updateChildSlots();
	}


	/**
	 * Updates a child equipment slot to the provided type and from the provided index if available.
	 * @param index The index of the child slot to update. If out of bounds then false is returned.
	 * @param type The type to update the slot to.
	 * @return True if a slot was updated.
	 */
	public boolean updateChildSlot(int index, String type) {
		if(index >= this.childSlots.size()) {
			return false;
		}
		this.childSlots.get(index).setType(type);
		return true;
	}


	/**
	 * Called when an ItemStack is inserted into this slot and updates any child slots.
	 * @param itemStack The ItemStack being inserted.
	 */
	@Override
	public void set(ItemStack itemStack) {
		this.putStackWithoutUpdate(itemStack);

		// Update Container:
		if("piece".equals(this.type)) {
			this.containerForge.onEquipmentPieceSlotChanged(this);
		}
		else {
			this.containerForge.onEquipmentPartSlotChanged(this);
		}
	}


	/**
	 * Puts an ItemStack into this slot but does not slot changes in the container. Use this for when the slot types don't need to change.
	 * @param itemStack The ItemStack being inserted.
	 */
	public void putStackWithoutUpdate(ItemStack itemStack) {
		super.set(itemStack);

		// Update Child Slots:
		if(!this.childSlots.isEmpty()) {
			this.updateChildSlots();
		}
	}


	@Override
	public void onTake(Player player, ItemStack itemStack) {
		Item item = itemStack.getItem();

		// Equipment Part:
		if(item instanceof ItemEquipmentPart) {
			this.updateChildSlots();
		}

		// Equipment Piece:
		if(item instanceof ItemEquipment) {
			int forgeLevel = this.containerForge.equipmentForge.getForgeLevel();
			if(forgeLevel < 3) {
				((ItemEquipment)item).applyLevelCap(itemStack, this.containerForge.equipmentForge.getForgeLevel());
			}
		}

		if("piece".equals(this.type)) {
			this.containerForge.onEquipmentPieceSlotChanged(this);
		}
		else {
			this.containerForge.onEquipmentPartSlotChanged(this);
		}

		super.onTake(player, itemStack);
	}


	@Override
	public boolean mayPickup(Player player) {
		if(!this.childSlots.isEmpty()) {
			for(EquipmentForgeSlot childSlot : this.childSlots) {
				if(!childSlot.getItem().isEmpty()) {
					return false;
				}
			}
		}
		else if("piece".equals(this.type)) {
			if(this.hasItem() && !this.containerForge.isEquipmentValid()) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Updates the type of each child slot that is connected to this slot.
	 */
	public void updateChildSlots() {
		Item item = this.getItem().getItem();

		// Update Child Slots:
		List<Integer> updatedChildSlots = new ArrayList<>();
		if(item instanceof ItemEquipmentPart) {
			ItemEquipmentPart itemEquipmentPart = (ItemEquipmentPart) item;

			int axeSlots = 0;
			for (EquipmentFeature feature : itemEquipmentPart.features) {
				if (feature instanceof SlotEquipmentFeature) {
					SlotEquipmentFeature slotFeature = (SlotEquipmentFeature) feature;
					int level = itemEquipmentPart.getPartLevel(this.getItem());
					if (slotFeature.isActive(this.getItem(), level)) {
						int index = 0;
						if (slotFeature.slotType.equals("axe")) {
							index = ++axeSlots;
						}
						else if (slotFeature.slotType.equals("pommel")) {
							index = 1;
						}
						if (!updatedChildSlots.contains(index) && this.updateChildSlot(index, slotFeature.slotType)) {
							updatedChildSlots.add(index);
						}
					}
				}
			}
		}

		// Set Unavailable Slots To None:
		for(int index = 0; index < this.childSlots.size(); index++) {
			if(!updatedChildSlots.contains(index)) {
				this.updateChildSlot(index, "none");
			}
		}
	}
}
