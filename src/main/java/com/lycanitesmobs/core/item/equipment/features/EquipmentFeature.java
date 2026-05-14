package com.lycanitesmobs.core.item.equipment.features;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class EquipmentFeature {
	/** The type of feature that this is. **/
	public String featureType;

	/** The minimun level the part must be for this feature to be enabled. **/
	public int levelMin = -1;

	/** The maximun level the part must be for this feature to be enabled. **/
	public int levelMax = -1;


	/** Loads a Tool Feature from the provided JSON data. **/
	@Nullable
	public static EquipmentFeature createFromJSON(JsonObject json) {
		String type = json.get("featureType").getAsString();
		EquipmentFeature equipmentFeature = null;

		if("slot".equalsIgnoreCase(type)) {
			equipmentFeature = new SlotEquipmentFeature();
		}
		else if("harvest".equalsIgnoreCase(type)) {
			equipmentFeature = new HarvestEquipmentFeature();
		}
		else if("damage".equalsIgnoreCase(type)) {
			equipmentFeature = new DamageEquipmentFeature();
		}
		else if("effect".equalsIgnoreCase(type)) {
			equipmentFeature = new EffectEquipmentFeature();
		}
		else if("summon".equalsIgnoreCase(type)) {
			equipmentFeature = new SummonEquipmentFeature();
		}
		else if("projectile".equalsIgnoreCase(type)) {
			equipmentFeature = new ProjectileEquipmentFeature();
		}

		if(equipmentFeature == null) {
			return null;
		}

		equipmentFeature.featureType = type;
		equipmentFeature.loadFromJSON(json);
		return equipmentFeature;
	}


	/** Loads this feature from a JSON object. **/
	public void loadFromJSON(JsonObject json) {
		if(json.has("levelMin"))
			this.levelMin = json.get("levelMin").getAsInt();

		if(json.has("levelMax"))
			this.levelMax = json.get("levelMax").getAsInt();
	}


	/** Returns true if this feature is active for the provided stack and level. **/
	public boolean isActive(ItemStack itemStack, int level) {
		// Check Level:
		if(this.levelMin > -1 && level < this.levelMin) {
			return false;
		}
		if(this.levelMax > -1 && level > this.levelMax) {
			return false;
		}

		return true;
	}

	/** Returns a description of this feature. Returns null if the feature is not active. **/
	public Component getDescription(ItemStack itemStack, int level) {
		if(!this.isActive(itemStack, level)) {
			return null;
		}
		return Component.literal("");
	}

	/** Returns a description of this feature. Returns null if the feature is not active. **/
	public MutableComponent getSummary(ItemStack itemStack, int level) {
		if(!this.isActive(itemStack, level)) {
			return null;
		}
		return Component.literal(this.featureType);
	}
}
