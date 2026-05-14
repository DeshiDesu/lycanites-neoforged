package com.lycanitesmobs.core.data.info;

import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ToolActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectLists {
	// Maps:
	public static Map<String, List<ItemStack>> itemLists = new HashMap<>();
	public static Map<String, List<EntityType>> entityLists = new HashMap<>();
	public static Map<String, List<MobEffect>> effectLists = new HashMap<>();
	public static Map<String, MobEffect> allEffects = new HashMap<>();
	
	
    // ==================================================
    //                        Add
    // ==================================================
	public static void addItem(String list, Object object) {
		if(!(object instanceof Item || object instanceof Block || object instanceof ItemStack || object instanceof String))
			return;
		list = list.toLowerCase();
		if(!itemLists.containsKey(list))
			itemLists.put(list, new ArrayList<>());
		ItemStack itemStack = null;
		
		if(object instanceof Item)
			itemStack = new ItemStack((Item)object);
		else if(object instanceof Block)
			itemStack = new ItemStack((Block)object);
		else if(object instanceof ItemStack)
			itemStack = (ItemStack)object;
		else {
			if (ObjectManager.getItem((String) object) != null)
				itemStack = new ItemStack(ObjectManager.getItem((String) object));
			else if (ObjectManager.getBlock((String) object) != null)
				itemStack = new ItemStack(ObjectManager.getBlock((String) object));
		}
		
		if(itemStack != null)
			itemLists.get(list).add(itemStack);
	}
	
	public static void addEntity(String list, Object object) {
		if(!(object instanceof Entity || object instanceof String))
			return;
		list = list.toLowerCase();
		if(!entityLists.containsKey(list))
			entityLists.put(list, new ArrayList<>());

		EntityType entityType = null;
		if(object instanceof String) {
			CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature((String) object);
			if(creatureInfo != null) {
				entityType = creatureInfo.getEntityType();
			}
		}
		if(entityType != null) {
			entityLists.get(list).add(entityType);
		}
	}

	public static void addEffect(String list, MobEffect effect, String effectName) {
		if(effect == null)
			return;
		allEffects.put(effectName, effect);
		list = list.toLowerCase();
		if(!effectLists.containsKey(list))
			effectLists.put(list, new ArrayList<>());
		effectLists.get(list).add(effect);
	}
	

    // ==================================================
    //                        Get
    // ==================================================
	public static List<ItemStack> getItems(String list) {
		list = list.toLowerCase();
		if(!itemLists.containsKey(list))
			return new ArrayList<>();
		return itemLists.get(list);
	}

	public static List<EntityType> getEntites(String list) {
		list = list.toLowerCase();
		if(!entityLists.containsKey(list))
			return new ArrayList<>();
		return entityLists.get(list);
	}

	public static List<MobEffect> getEffects(String list) {
		list = list.toLowerCase();
		if(!effectLists.containsKey(list))
			return new ArrayList<>();
		return effectLists.get(list);
	}
	

    // ==================================================
    //                      Compare
    // ==================================================
	public static boolean inItemList(String list, ItemStack testStack) {
		list = list.toLowerCase();
        if(testStack == null || testStack.isEmpty())
            return false;
		if(!itemLists.containsKey(list))
			return false;
		for(ItemStack listStack : itemLists.get(list))
			if(testStack.getItem() == listStack.getItem()
			&& testStack.getDamageValue() == listStack.getDamageValue())
				return true;
		return false;
	}

	public static boolean inEntityList(String list, Class testClass) {
		list = list.toLowerCase();
		if(!entityLists.containsKey(list))
			return false;
		return false;
	}

	public static boolean inEffectList(String list, MobEffect effect) {
		list = list.toLowerCase();
		if(!effectLists.containsKey(list))
			return false;
		return effectLists.get(list).contains(effect);
	}
	
	
    // ==================================================
    //                   Create Lists
    // ==================================================
	public static void createVanillaLists() {
		// ========== Item Lists ==========
		// Raw Meat: (A bit cold...)
		ObjectLists.addItem("rawmeat", Items.BEEF);
		ObjectLists.addItem("rawmeat", Items.PORKCHOP);
		ObjectLists.addItem("rawmeat", Items.CHICKEN);
		ObjectLists.addItem("rawmeat", Items.MUTTON);
		ObjectLists.addItem("rawmeat", Items.RABBIT);

		// Cooked Meat: (Meaty goodness for carnivorous pets!)
		ObjectLists.addItem("cookedmeat", Items.COOKED_BEEF);
		ObjectLists.addItem("cookedmeat", Items.COOKED_PORKCHOP);
		ObjectLists.addItem("cookedmeat", Items.COOKED_CHICKEN);
		ObjectLists.addItem("cookedmeat", Items.COOKED_MUTTON);
		ObjectLists.addItem("cookedmeat", Items.COOKED_RABBIT);

		// Prepared Vegetables: (For most vegetarian pets.)
		ObjectLists.addItem("vegetables", Items.WHEAT);
		ObjectLists.addItem("vegetables", Items.CARROT);
		ObjectLists.addItem("vegetables", Items.POTATO);
		ObjectLists.addItem("vegetables", Items.BEETROOT);
		ObjectLists.addItem("vegetables", Items.DRIED_KELP);

		// Fruit: (For exotic pets!)
		ObjectLists.addItem("fruit", Items.APPLE);
		ObjectLists.addItem("fruit", Items.MELON_SLICE);
		ObjectLists.addItem("fruit", Blocks.PUMPKIN);
		ObjectLists.addItem("fruit", Items.PUMPKIN_PIE);
		ObjectLists.addItem("fruit", Items.SWEET_BERRIES);

		// Raw Fish: (Very smelly!)
		ObjectLists.addItem("rawfish", Items.COD);
		ObjectLists.addItem("rawfish", Items.SALMON);
		ObjectLists.addItem("rawfish", Items.TROPICAL_FISH);
		ObjectLists.addItem("rawfish", Items.PUFFERFISH);

		// Cooked Fish: (For those fish fiends!)
		ObjectLists.addItem("cookedfish", Items.COOKED_COD);
		ObjectLists.addItem("cookedfish", Items.COOKED_SALMON);

		// Cactus Food: (Jousts love these!)
		ObjectLists.addItem("cactusfood", Items.GREEN_DYE);

		// Mushrooms: (Fungi treats!)
        ObjectLists.addItem("mushrooms", Items.MUSHROOM_STEW);
        ObjectLists.addItem("mushrooms", Items.RED_MUSHROOM);
        ObjectLists.addItem("mushrooms", Items.BROWN_MUSHROOM);
        ObjectLists.addItem("mushrooms", Blocks.RED_MUSHROOM);
		ObjectLists.addItem("mushrooms", Blocks.BROWN_MUSHROOM);
		ObjectLists.addItem("mushrooms", Blocks.RED_MUSHROOM);
		ObjectLists.addItem("mushrooms", Blocks.BROWN_MUSHROOM_BLOCK);
		ObjectLists.addItem("mushrooms", Blocks.RED_MUSHROOM_BLOCK);

		// Sweets: (Sweet sugary goodness!)
		ObjectLists.addItem("sweets", Items.SUGAR);
		ObjectLists.addItem("sweets", Items.COCOA_BEANS);
		ObjectLists.addItem("sweets", Items.COOKIE);
		ObjectLists.addItem("sweets", Blocks.CAKE);
		ObjectLists.addItem("sweets", Items.PUMPKIN_PIE);

		// Fuel: (Fiery awesomeness!)
		ObjectLists.addItem("fuel", Items.COAL);

		// ========== Effects ==========
		// Buffs:
		ObjectLists.addEffect("buffs", MobEffects.DAMAGE_BOOST, "strength");
		ObjectLists.addEffect("buffs", MobEffects.DIG_SPEED, "haste");
		ObjectLists.addEffect("buffs", MobEffects.FIRE_RESISTANCE, "fire_resistance");
		ObjectLists.addEffect("buffs", MobEffects.HEAL, "instant_health");
		ObjectLists.addEffect("buffs", MobEffects.INVISIBILITY, "invisibility");
		ObjectLists.addEffect("buffs", MobEffects.JUMP, "jump_boost");
		ObjectLists.addEffect("buffs", MobEffects.MOVEMENT_SPEED, "speed");
		ObjectLists.addEffect("buffs", MobEffects.NIGHT_VISION, "night_vision");
		ObjectLists.addEffect("buffs", MobEffects.REGENERATION, "regeneration");
		ObjectLists.addEffect("buffs", MobEffects.DAMAGE_RESISTANCE, "resistance");
		ObjectLists.addEffect("buffs", MobEffects.WATER_BREATHING, "water_breathing");
		ObjectLists.addEffect("buffs", MobEffects.HEALTH_BOOST, "health_boost");
		ObjectLists.addEffect("buffs", MobEffects.ABSORPTION, "absorption");
		ObjectLists.addEffect("buffs", MobEffects.SATURATION, "saturation");
        ObjectLists.addEffect("buffs", MobEffects.GLOWING, "glowing");
        ObjectLists.addEffect("buffs", MobEffects.LEVITATION, "levitation");
        ObjectLists.addEffect("buffs", MobEffects.LUCK, "luck");
        ObjectLists.addEffect("buffs", MobEffects.DOLPHINS_GRACE, "dolphins_grace");

		// Debuffs:
        ObjectLists.addEffect("debuffs", MobEffects.BLINDNESS, "blindness");
        ObjectLists.addEffect("debuffs", MobEffects.CONFUSION, "nausea");
        ObjectLists.addEffect("debuffs", MobEffects.DIG_SLOWDOWN, "mining_fatigue");
        ObjectLists.addEffect("debuffs", MobEffects.HARM, "instant_damage");
        ObjectLists.addEffect("debuffs", MobEffects.HUNGER, "hunger");
        ObjectLists.addEffect("debuffs", MobEffects.MOVEMENT_SLOWDOWN, "slowness");
        ObjectLists.addEffect("debuffs", MobEffects.POISON, "poison");
        ObjectLists.addEffect("debuffs", MobEffects.WEAKNESS, "weakness");
        ObjectLists.addEffect("debuffs", MobEffects.WITHER, "wither");
        ObjectLists.addEffect("debuffs", MobEffects.UNLUCK, "unluck");
	}
	
	// ========== Add From Config Value ==========
	public static void addFromConfig(String listName) {
		/*String customDropsString = ConfigCreatures.INSTANCE.customDrops.get();
		LycanitesMobs.logDebug("Items", "~O========== Custom " + listName + " ==========O~");
		if(customDropsString != null && customDropsString.length() > 0) {
			for (String customDropEntryString : customDropsString.replace(" ", "").split(";")) {
				LycanitesMobs.logDebug("Items", "Adding: " + customDropEntryString);
				String[] customDropValues = customDropEntryString.split(",");
				String dropName = customDropValues[0];
				if (Item.getByNameOrId(dropName) != null) {
					Item customItem = Item.getByNameOrId(dropName);
					ObjectLists.addItem(listName, new ItemStack(customItem, 1));
					LycanitesMobs.logDebug("ItemSetup", "As Item: " + customItem);
				}
				else if (Block.getBlockFromName(dropName) != null) {
					Block customBlock = Block.getBlockFromName(dropName);
					ObjectLists.addItem(listName, new ItemStack(customBlock, 1));
					LycanitesMobs.logDebug("ItemSetup", "As Block: " + customBlock);
				}
			}
		}*/
	}
	
	
    // ==================================================
    //                   Check Tools
    // ==================================================
    // ========== Pickaxe ==========
	public static boolean isPickaxe(ItemStack itemStack) {
		if(itemStack.isEmpty()) {
			return false;
		}
		if(itemStack.getItem() instanceof PickaxeItem) {
			return true;
		}
		if(itemStack.canPerformAction(ToolActions.PICKAXE_DIG)) {
			return true;
		}
		return false;
	}

    // ========== Axe ==========
	public static boolean isAxe(ItemStack itemStack) {
        if(itemStack.isEmpty()) {
			return false;
		}
		if(itemStack.getItem() instanceof AxeItem) {
			return true;
		}
		if(itemStack.canPerformAction(ToolActions.AXE_DIG)) {
			return true;
		}
        return false;
	}

    // ========== Shovel ==========
	public static boolean isShovel(ItemStack itemStack) {
		if(itemStack.isEmpty()) {
			return false;
		}
		if(itemStack.getItem() instanceof ShovelItem) {
			return true;
		}
		if(itemStack.canPerformAction(ToolActions.SHOVEL_DIG)) {
			return true;
		}
		return false;
	}

	
    // ==================================================
    //                   Check Names
    // ==================================================
	public static boolean isName(Item item, String name) {
		if(item == null)
			return false;
		String itemName = item.getDescriptionId().toLowerCase();
		if(itemName.contains(name))
			return true;
		return false;
	}

	public static boolean isName(Block block, String name) {
		if(block == null)
			return false;
		name = name.toLowerCase();
		String blockName = block.getDescriptionId().toLowerCase();
		if(blockName.contains(name)) {
			return true;
		}
		return false;
	}
}
