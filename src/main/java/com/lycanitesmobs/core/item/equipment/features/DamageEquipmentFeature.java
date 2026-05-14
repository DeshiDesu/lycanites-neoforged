package com.lycanitesmobs.core.item.equipment.features;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class DamageEquipmentFeature extends EquipmentFeature {
	/** How much damage this part adds to the weapon attack. **/
	public int damageAmount = 1;

	/** The attack cooldown modifier (based on vanilla sword cooldown). All modifiers are averaged together. **/
	public double damageCooldown = 1;

	/** How much knockback this part adds to the weapon attack. **/
	public double damageKnockback = 0;

	/** The range (in blocks) that this feature adds to the weapon attack. **/
	public double damageRange = 0;

	/** The angle (in degrees) that this feature applies to the weapon attack. (The feature with the largest range is used.) **/
	public double damageSweep = 45;


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);

		if(json.has("damageAmount"))
			this.damageAmount = json.get("damageAmount").getAsInt();

		if(json.has("damageCooldown"))
			this.damageCooldown = json.get("damageCooldown").getAsDouble();

		if(json.has("damageKnockback"))
			this.damageKnockback = json.get("damageKnockback").getAsDouble();

		if(json.has("damageRange"))
			this.damageRange = json.get("damageRange").getAsDouble();

		if(json.has("damageSweep"))
			this.damageSweep = json.get("damageSweep").getAsDouble();
	}

	@Override
	public MutableComponent getDescription(ItemStack itemStack, int level) {
		if(!this.isActive(itemStack, level)) {
			return null;
		}
		MutableComponent description = Component.translatable("equipment.feature." + this.featureType)
			.append(" " + this.damageAmount);
		if(this.damageCooldown > 0) {
			description.append("\n")
					.append(Component.translatable("equipment.feature.damage.cooldown"))
					.append(" " + String.format("%.1f", this.damageCooldown));
		}
		if(this.damageKnockback > 0) {
			description.append("\n")
					.append(Component.translatable("equipment.feature.damage.knockback"))
					.append(" " + String.format("%.0f", this.damageKnockback));
		}
		if(this.damageRange > 0) {
			description.append("\n")
					.append(Component.translatable("equipment.feature.damage.range"))
					.append(" " + String.format("%.1f", this.damageRange));
		}
		if(this.damageSweep > 0) {
			description.append("\n")
					.append(Component.translatable("equipment.feature.damage.sweep"))
					.append(" " + String.format("%.0f", this.damageSweep));
		}
		return description;
	}

	@Override
	public MutableComponent getSummary(ItemStack itemStack, int level) {
		return this.getDescription(itemStack, level);
	}
}
