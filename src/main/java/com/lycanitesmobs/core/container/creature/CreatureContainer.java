package com.lycanitesmobs.core.container.creature;

import com.lycanitesmobs.core.container.base.BaseContainer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;

public class CreatureContainer extends BaseContainer {
	public static final MenuType<CreatureContainer> TYPE = (MenuType<CreatureContainer>) IForgeMenuType.create(CreatureContainer::new);
	public BaseCreatureEntity creature;

	/**
	 * Client Constructor
	 * @param windowId The window id for the gui screen to use.
	 * @param playerInventory The accessing player's inventory.
	 * @param extraData A packet sent from the server to create the Container from.
	 */
	public CreatureContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
		this(windowId, playerInventory, (BaseCreatureEntity)playerInventory.player.getCommandSenderWorld().getEntity(extraData.readInt()));
	}

	/**
	 * Main Constructor
	 * @param windowId The window id for the gui screen to use.
	 * @param playerInventory The accessing player's inventory.
	 * @param creature The creature to access.
	 */
	public CreatureContainer(int windowId, Inventory playerInventory, BaseCreatureEntity creature) {
		super(TYPE, windowId);
		this.creature = creature;

		// Player Inventory:
		this.addPlayerSlots(playerInventory, 0, 0);

		// Creature Equipment:
		this.specialStart = this.slots.size();
		this.drawCreatureEquipment(creature, 8, 18);
		this.specialFinish = this.slots.size() - 1;

		// Creature Inventory
		this.inventoryStart = this.slots.size();
		if(creature.inventory.getItemSlotsSize() > 0)
			this.addSlotsByColumn(creature.inventory, 8 + (18 * 4), 18, 5, 0, creature.inventory.getActiveItemSlotsSize() - 1);
		this.inventoryFinish = this.slots.size() - 1;
	}


	
	
	// ==================================================
  	//                    Draw Slots
  	// ==================================================
	public void drawCreatureEquipment(BaseCreatureEntity creature, int equipX, int equipY) {
		// Creature Accessories:
		if(creature instanceof RideableCreatureEntity) {
			this.addSlot(creature.inventory, creature.inventory.getSlotFromType("saddle"), equipX, equipY);
			equipY += 18;
		}
		if(creature.getBagSize() > 0) {
			this.addSlot(creature.inventory, creature.inventory.getSlotFromType("bag"), equipX, equipY);
			equipY += 18;
		}

		// Weapon and Dye slots will go here.
		
		// Creature Armor:
		equipX += 18;
		equipY = 18;
		if(creature.inventory.useAdvancedArmor()) {
			this.addSlot(creature.inventory, creature.inventory.getSlotFromType("head"), equipX, equipY);
			equipY += 18;
		}
		this.addSlot(creature.inventory, creature.inventory.getSlotFromType("chest"), equipX, equipY);
		equipY += 18;
		if(creature.inventory.useAdvancedArmor()) {
			this.addSlot(creature.inventory, creature.inventory.getSlotFromType("legs"), equipX, equipY);
			equipY += 18;
			this.addSlot(creature.inventory, creature.inventory.getSlotFromType("feet"), equipX, equipY);
			equipY += 18;
		}
	}
	
	
	// ==================================================
  	//                  Container Closed
  	// ==================================================
	@Override
	public void removed(Player player) {
		super.removed(player);
	}
}
