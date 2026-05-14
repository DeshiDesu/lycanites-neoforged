package com.lycanitesmobs.core.container.creature;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatureInventory implements Container {

    // Basic Armor Values: (Copied values from EntityHorse)
    public static final Map<String, Integer> armorValues = new HashMap<>();
    static {
		armorValues.put("Leather", 5);
		armorValues.put("Iron", 10);
		armorValues.put("Gold", 15);
		armorValues.put("Chain", 17);
		armorValues.put("Diamond", 20);
    }

    // Equipment Types:
    public static final List<String> equipmentTypes = new ArrayList<>();
    static {
        equipmentTypes.add("head");
        equipmentTypes.add("chest");
        equipmentTypes.add("legs");
        equipmentTypes.add("feet");
        equipmentTypes.add("saddle");
        equipmentTypes.add("bag");
    }
	
	// Properties:
	public BaseCreatureEntity creature;
	public String inventoryName;
	protected NonNullList<ItemStack> inventoryContents;
    protected boolean basicArmor = true;
    protected int nextEquipmentSlot = 0;

    // Equipment Slots:
    public Map<String, Integer> equipmentTypeToSlot = new HashMap<>();
    public Map<Integer, String> equipmentSlotToType = new HashMap<>();

    // ==================================================
    //                    Data Parameters
    // ==================================================
    public static EntityDataAccessor<ItemStack> getEquipmentDataParameter(String type) {
        if(type.equals("head"))
            return BaseCreatureEntity.EQUIPMENT_HEAD;
        if(type.equals("chest"))
            return BaseCreatureEntity.EQUIPMENT_CHEST;
        if(type.equals("legs"))
            return BaseCreatureEntity.EQUIPMENT_LEGS;
        if(type.equals("feet"))
            return BaseCreatureEntity.EQUIPMENT_FEET;
        if(type.equals("saddle"))
            return BaseCreatureEntity.EQUIPMENT_SADDLE;
        return BaseCreatureEntity.EQUIPMENT_BAG;
    }

    /** Registers parameters to the provided datamanager. **/
    public static void registerData(SynchedEntityData dataManager) {
        for(String equipmentType : equipmentTypes)
            dataManager.define(getEquipmentDataParameter(equipmentType), ItemStack.EMPTY);
    }

	
	// ==================================================
  	//                    Constructor
  	// ==================================================
	public CreatureInventory(String inventoryName, BaseCreatureEntity creature) {
		this.inventoryName = inventoryName;
		this.creature = creature;
		this.addEquipmentSlot("chest");
		this.addEquipmentSlot("saddle");
		this.addEquipmentSlot("bag");
		this.inventoryContents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
	}

    protected void addEquipmentSlot(String type) {
        this.equipmentTypeToSlot.put(type, this.nextEquipmentSlot);
        this.equipmentSlotToType.put(this.nextEquipmentSlot, type);
        this.nextEquipmentSlot++;
    }
	
	
	// ==================================================
  	//                     Details
  	// ==================================================
    public String getName() {
        return this.inventoryName + Component.translatable("entity.level").getString() + " " + this.creature.getMobLevel();
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(this.getName());
    }

	public boolean hasCustomName() {
		return true;
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}
	
	@Override
	public int getContainerSize() {
		return this.getItemSlotsSize() + this.getSpecialSlotsSize();
	}

	public int getActiveItemSlotsSize() {
		if(!this.getEquipmentStack("bag").isEmpty())
			return this.creature.getBagSize();
		else
			return this.creature.getNoBagSize();
	}

	public int getItemSlotsSize() {
		return this.creature.getInventorySizeMax();
	}
	
	public int getSpecialSlotsSize() {
		return this.equipmentTypeToSlot.size();
	}
	
	/** Returns true if this mob has any items in it's bag slots. **/
	public boolean hasBagItems() {
		for(ItemStack itemStack : this.inventoryContents) {
			if(itemStack != null)
				return true;
		}
		return false;
	}

    /** Returns true if this inventory is empty. **/
    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventoryContents) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
	
	
	// ==================================================
  	//                      Actions
  	// ==================================================
	public void onInventoryChanged(boolean refresh) {
		if(this.creature.getCommandSenderWorld().isClientSide)
			return;
		
		// Empty Bag if Removed:
		if(this.getEquipmentStack("bag") == null) {
			int bagSizeDiff = this.creature.getBagSize() - this.creature.getNoBagSize();
			if(bagSizeDiff > 0)
				for(int i = this.creature.getNoBagSize(); i <= this.getItemSlotsSize(); i++)
					if(this.getItem(i - 1) != null) {
						this.creature.dropItem(this.getItem(i - 1));
						this.setInventorySlotContentsNoUpdate(i - 1, null);
					}
		}
		
		// Update Datawatcher:
        for(String type : this.equipmentSlotToType.values()) {
			ItemStack itemStack = this.getEquipmentStack(type);
			EntityDataAccessor<ItemStack> dataParameter = getEquipmentDataParameter(type);
            if(dataParameter == null)
                continue;
			if(itemStack == null)
                this.creature.getEntityData().set(dataParameter, ItemStack.EMPTY);
            else
			    this.creature.getEntityData().set(dataParameter, itemStack);
		}

		if(refresh)
			this.creature.scheduleGUIRefresh();
	}
	
	@Override
	public boolean stillValid(Player entityplayer) {
		return true;
	}
	
	@Override
	public void startOpen(Player player) {}
	
	@Override
	public void stopOpen(Player player) {}

    @Override
    public void clearContent() {

    }
	
	
	// ==================================================
  	//                      Items
  	// ==================================================
	@Override
	public ItemStack getItem(int slotID) {
		if(slotID >= this.getContainerSize() || slotID < 0)
			return ItemStack.EMPTY;
		else
			return this.inventoryContents.get(slotID);
	}

	@Override
	public void setItem(int slotID, ItemStack itemStack) {
		this.setInventorySlotContentsNoUpdate(slotID, itemStack);
		this.onInventoryChanged("bag".equals(this.getTypeFromSlot(slotID)));
	}
	
	public void setInventorySlotContentsNoUpdate(int slotID, ItemStack itemStack) {
		if(slotID >= this.getContainerSize() || slotID < 0)
			return;
        if(itemStack != null) {
	        if(itemStack.getCount() > this.getMaxStackSize())
	        	itemStack.setCount(this.getMaxStackSize());
	        if(itemStack.getCount() < 1)
	        	itemStack = null;
        }
        if(itemStack == null)
        	itemStack = ItemStack.EMPTY;
		this.inventoryContents.set(slotID, itemStack);
	}
	
	// ========== Decrease Stack Size ==========
	@Override
	public ItemStack removeItem(int slot, int amount) {
    	if (this.creature.isDeadOrDying()) {
    		return ItemStack.EMPTY;
		}
		ItemStack[] splitStacks = this.decrStackSize(this.getItem(slot), amount);
		this.setItem(slot, splitStacks[0]);
        this.onInventoryChanged(false);
		return splitStacks[1];
	}

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY;
    }

    public ItemStack[] decrStackSize(ItemStack itemStack, int amount) {
		ItemStack[] splitStacks = {null, null};
		if(itemStack == null)
			return splitStacks;
		
        if(itemStack.getCount() <= amount) {
            splitStacks[0] = ItemStack.EMPTY;
            splitStacks[1] = itemStack;
        }
        else {
        	splitStacks[1] = itemStack.split(amount);
            if(itemStack.getCount() == 0)
            	itemStack = ItemStack.EMPTY;
            splitStacks[0] = itemStack;
        }
        return splitStacks;
	}

	@Override
	public boolean canPlaceItem(int slotID, ItemStack itemStack) {
		if (this.creature.isDeadOrDying()) {
			return false;
		}
		String type = this.getTypeFromSlot(slotID);
		if(type != null) {
			if(!this.isEquipmentValidForSlot(type, itemStack))
				return false;
			ItemStack equipedStack = this.getItem(slotID);
			if(equipedStack != null && !equipedStack.isEmpty())
				return false;
				
		}
		return true;
	}

    // ========== Check Space ==========
	public int getSpaceForStack(ItemStack itemStack) {
		if(itemStack == null)
			return 0;
		if(itemStack.getItem() == null || itemStack.getCount() < 1)
			return 0;
		
		int space = 0;
		for(int slotID = 0; slotID < this.inventoryContents.size(); slotID++) {
			if(this.canPlaceItem(slotID, itemStack)) {
				ItemStack slotStack = this.getItem(slotID);
				if(slotStack != null && slotStack != ItemStack.EMPTY && !slotStack.isEmpty()) {
					if(slotStack.getCount() < slotStack.getMaxStackSize())
						if(slotStack.getItem() == itemStack.getItem() && slotStack.getDamageValue() == itemStack.getDamageValue())
							space += slotStack.getMaxStackSize() - slotStack.getCount();
				}
				else
					space += itemStack.getMaxStackSize();
			}
			if(space >= itemStack.getCount())
				break;
		}

		return Math.min(space, itemStack.getCount());
	}
	
	// ========== Auto Insert Item Stack ==========
	public ItemStack autoInsertStack(ItemStack itemStack) {
		if(itemStack == null || itemStack.getCount() < 1 || this.creature.isDeadOrDying()) {
			return ItemStack.EMPTY;
		}

		for(int slotID = 0; slotID < this.inventoryContents.size(); slotID++) {
			ItemStack slotStack = this.inventoryContents.get(slotID);
			
			// If there is a stack in the slot:
			if(slotStack != null && slotStack != ItemStack.EMPTY && !slotStack.isEmpty()) {
				if(slotStack.getCount() < slotStack.getMaxStackSize())
					if(slotStack.getItem() == itemStack.getItem() && slotStack.getDamageValue() == itemStack.getDamageValue()) {
						int space = Math.max(slotStack.getMaxStackSize() - slotStack.getCount(), 0);
						
						// If there is more than or just enough room:
						if(space >= itemStack.getCount()) {
							this.getItem(slotID).setCount(this.getItem(slotID).getCount() + itemStack.getCount());
									itemStack = null;
						}
						else {
                            this.getItem(slotID).setCount(this.getItem(slotID).getCount() + space);
                            itemStack.setCount(itemStack.getCount() - space);
						}
					}
			}
			
			// If the slot is empty:
			else {
				this.setItem(slotID, itemStack);
				itemStack = null;
			}
			
			if(itemStack == null)
				break;
		}
		return itemStack;
	}
	
	
	// ==================================================
  	//                      Equipment
  	// ==================================================
	public void setAdvancedArmor(boolean advanced) {
		this.basicArmor = !advanced;
		if(advanced) {
			this.addEquipmentSlot("head");
			this.addEquipmentSlot("legs");
			this.addEquipmentSlot("feet");
		}
	}
	
	public boolean useAdvancedArmor() {
		return !this.basicArmor;
	}
	
	// ========== Type to Slot Mapping ==========
	public int getSlotFromType(String type) {
		if(this.equipmentTypeToSlot.containsKey(type))
			return this.equipmentTypeToSlot.get(type) + this.getItemSlotsSize();
		else
			return -1;
	}

	public String getTypeFromSlot(int slotID) {
		if(this.equipmentSlotToType.containsKey(slotID - this.getItemSlotsSize()))
			return this.equipmentSlotToType.get(slotID - this.getItemSlotsSize());
		else
			return null;
	}
	
	// ========== Get Equipment ==========
	public ItemStack getEquipmentStack(String type) {
		if(getEquipmentDataParameter(type) == null)
			return ItemStack.EMPTY;
		if(this.creature.getCommandSenderWorld().isClientSide) {
			try {
				return this.creature.getEntityData().get(getEquipmentDataParameter(type));
			}
			catch(Exception e) {
				return ItemStack.EMPTY;
			}
		}
		else
			return this.getItem(this.getSlotFromType(type));
	}
	
	// ========== Set Equipment ==========
	public void setEquipmentStack(String type, ItemStack itemStack) {
		if(!this.creature.getCommandSenderWorld().isClientSide && this.equipmentTypeToSlot.containsKey(type) && this.isEquipmentValidForSlot(type, itemStack))
			this.setItem(this.getSlotFromType(type), itemStack);
	}
	public void setEquipmentStack(ItemStack itemStack) {
		String type = this.getSlotForEquipment(itemStack);
		if(type != null)
			this.setEquipmentStack(type, itemStack);
	}
	
	// ========== Get Slot for Equipment ==========
	public String getSlotForEquipment(ItemStack itemStack) {
		if(itemStack == null)
			return null;
		
		// Basic Armor:
		if(this.basicArmor) {
			if(itemStack.getItem() == Items.LEATHER_HORSE_ARMOR)
				return "chest";
			if(itemStack.getItem() == Items.IRON_HORSE_ARMOR)
				return "chest";
	    	if(itemStack.getItem() == Items.GOLDEN_HORSE_ARMOR)
	    		return "chest";
	    	if(itemStack.getItem() == Items.DIAMOND_HORSE_ARMOR)
	    		return "chest";
		}
		
		// Advanced Armor:
		if(!this.basicArmor && itemStack.getItem() instanceof ArmorItem) {
			ArmorItem armorstack = (ArmorItem)(itemStack.getItem());
			if(armorstack.getEquipmentSlot() == EquipmentSlot.HEAD)
				return "head";
			if(armorstack.getEquipmentSlot() == EquipmentSlot.CHEST)
				return "chest";
			if(armorstack.getEquipmentSlot() == EquipmentSlot.LEGS)
				return "legs";
			if(armorstack.getEquipmentSlot() == EquipmentSlot.FEET)
				return "feet";
		}
		
		// Saddle:
		if(this.creature instanceof RideableCreatureEntity && itemStack.getItem() == this.creature.creatureInfo.creatureType.getSaddleItem()) {
			return "saddle";
		}
		
		// Bag:
		if(itemStack.getItem() == Item.byBlock(Blocks.CHEST))
			return "bag";
		
		return null;
	}
	
	// ========== Equipment Valid for Slot ==========
	public boolean isEquipmentValidForSlot(String type, ItemStack itemStack) {
		if(itemStack == null)
			return true;
		return type.equals(getSlotForEquipment(itemStack));
	}
	
	// ========== Get Equipment Grade ==========
	public String getEquipmentGrade(String type) {
    	ItemStack equipmentStack = this.getEquipmentStack(type);
    	if(equipmentStack == null)
    		return null;
    	if(equipmentStack.getItem() instanceof ArmorItem) {
    		ArmorItem armor = (ArmorItem) equipmentStack.getItem();
    		if(armor.getMaterial() == ArmorMaterials.LEATHER)
    			return "Leather";
    		else if(armor.getMaterial() == ArmorMaterials.IRON)
    			return "Iron";
    		else if(armor.getMaterial() == ArmorMaterials.CHAIN)
    			return "Chain";
    		else if(armor.getMaterial() == ArmorMaterials.GOLD)
    			return "Gold";
    		else if(armor.getMaterial() == ArmorMaterials.DIAMOND)
    			return "Diamond";
    	}
    	if(equipmentStack.getItem() == Items.LEATHER_HORSE_ARMOR)
    		return "Leather";
    	if(equipmentStack.getItem() == Items.IRON_HORSE_ARMOR)
    		return "Iron";
    	if(equipmentStack.getItem() == Items.GOLDEN_HORSE_ARMOR)
    		return "Gold";
    	if(equipmentStack.getItem() == Items.DIAMOND_HORSE_ARMOR)
    		return "Diamond";
    	return null;
    }
	
	// ========== Get Armor Value ==========
	public int getArmorValue() {
		String[] armorSlots = {"head", "chest", "legs", "feet"};
		int totalArmor = 0;
        for(String armorSlot : armorSlots) {
        	ItemStack armorStack = this.getEquipmentStack(armorSlot);
        	if(armorStack != null) {
            	if(armorStack.getItem() instanceof ArmorItem)
	                totalArmor += ((ArmorItem)armorStack.getItem()).getDefense();
            	else if(this.getEquipmentGrade(armorSlot) != null && this.armorValues.containsKey(this.getEquipmentGrade(armorSlot)))
            		totalArmor += this.armorValues.get(this.getEquipmentGrade(armorSlot));
        	}
        }
        return totalArmor;
	}
	
	
	// ==================================================
  	//                   Drop Inventory
  	// ==================================================
	public void dropInventory() {
		for(int slotID = 0; slotID < this.inventoryContents.size(); slotID++) {
			ItemStack itemStack = this.inventoryContents.get(slotID);
			this.creature.dropItem(itemStack);
			this.setInventorySlotContentsNoUpdate(slotID, ItemStack.EMPTY);
		}
	}
    
    
    // ==================================================
    //                        NBT
    // ==================================================
	/**
	 * Loads inventory from nbt data.
	 * @param nbt The nbt data to load from.
	 */
	public void load(CompoundTag nbt) {
    	// Read Items:
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(nbt, itemStacks); // Reads ItemStack into a List from "Items" tag.

    	for(int i = 0; i < itemStacks.size(); ++i) {
    		if(i < this.getContainerSize()) {
                ItemStack itemStack = itemStacks.get(i);
                if(itemStack.isEmpty())
                    this.setInventorySlotContentsNoUpdate(i, ItemStack.EMPTY);
                else {
					this.setInventorySlotContentsNoUpdate(i, itemStack);
				}
            }
    	}
    	this.onInventoryChanged(false);
    }

	/**
	 * Saves inventory to nbt data.
	 * @param nbt The nbt data to save to.
	 */
	public void save(CompoundTag nbt) {
    	// Write Items:
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        for(int i = 0; i < this.getContainerSize(); i++) {
            if(this.getItem(i) != null) {
				itemStacks.set(i, this.getItem(i));
			}
        }
		ContainerHelper.saveAllItems(nbt, itemStacks); // Adds ItemStack NBT into the NBT Tag Compound.
    	
    }

	@Override
	public void setChanged() {}
}
