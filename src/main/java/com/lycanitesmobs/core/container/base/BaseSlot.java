package com.lycanitesmobs.core.container.base;

import com.lycanitesmobs.core.container.creature.CreatureInventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BaseSlot extends Slot {

	// ==================================================
  	//                    Constructor
  	// ==================================================
	public BaseSlot(Container inventory, int slotIndex, int x, int y) {
		super(inventory, slotIndex, x, y);
	}
	
	
    // ==================================================
  	//                    Validation
  	// ==================================================
	public boolean mayPlace(ItemStack itemStack) {
		if(this.container == null) {
			return true;
		}
		return this.container.canPlaceItem(this.getSlotIndex(), itemStack);
    }
	
	public int getMaxStackSize() {
		if(this.container instanceof CreatureInventory)
			if(((CreatureInventory)this.container).getTypeFromSlot(this.getSlotIndex()) != null)
				return 1;
        return this.container.getMaxStackSize();
    }
}
