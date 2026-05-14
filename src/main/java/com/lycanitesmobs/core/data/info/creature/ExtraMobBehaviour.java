package com.lycanitesmobs.core.data.info.creature;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.RevengeGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;

public class ExtraMobBehaviour {
	// ========== Mob ==========
	/** The INSTANCE of the mob this extra behaviour belongs to. **/
	public BaseCreatureEntity host;
	
	// ========== Stats ==========
    public double multiplierHealth = 1.0D;
	public double multiplierDefense = 1.0D;
	public double multiplierArmor = 1.0D;
	public double multiplierSpeed = 1.0D;
	public double multiplierDamage = 1.0D;
	public double multiplierHaste = 1.0D;
	public double multiplierEffect = 1.0D;
	public double multiplierPierce = 1.0D;

    public int boostHealth = 0;
	public int boostDefense = 0;
	public int boostArmor = 0;
	public int boostSpeed = 0;
	public int boostDamage = 0;
	public int boostHaste = 0;
	public int boostEffect = 0;
	public int boostPierce = 0;
	
	// ========== Overrides ==========
	public boolean aggressiveOverride = false;
	public boolean flightOverride = false;
	public boolean swimmingOverride = false;
	public boolean waterBreathingOverride = false;
	public boolean fireImmunityOverride = false;
	public boolean stealthOverride = false;
	public boolean itemPickupOverride = false;
	public int inventorySizeOverride = 0;
	public double itemDropMultiplierOverride = 1;
	
	// ========== AI ==========
	public boolean aiAttackPlayers = false;
	public boolean aiDefendAnimals = false;
	
	
    // ==================================================
    //                     Constructor
    // ==================================================
	public ExtraMobBehaviour(BaseCreatureEntity host) {
		this.host = host;
	}
	
	
	// ==================================================
    //                        NBT
    // ==================================================
   	// ========== Read ===========
    /** Called from this host passing a compound storing all the extra behaviour options. **/
    public void read(CompoundTag nbtTagCompound) {
    	// Stat Multipliers:
        if(nbtTagCompound.contains("MultiplierHealth")) {
            this.multiplierHealth = nbtTagCompound.getDouble("MultiplierHealth");
        }
    	if(nbtTagCompound.contains("MultiplierDefense")) {
    		this.multiplierDefense = nbtTagCompound.getDouble("MultiplierDefense");
    	}
		if(nbtTagCompound.contains("MultiplierArmor")) {
			this.multiplierArmor = nbtTagCompound.getDouble("MultiplierArmor");
		}
    	if(nbtTagCompound.contains("MultiplierSpeed")) {
    		this.multiplierSpeed = nbtTagCompound.getDouble("MultiplierSpeed");
    	}
    	if(nbtTagCompound.contains("MultiplierDamage")) {
    		this.multiplierDamage = nbtTagCompound.getDouble("MultiplierDamage");
    	}
    	if(nbtTagCompound.contains("MultiplierHaste")) {
    		this.multiplierHaste = nbtTagCompound.getDouble("MultiplierHaste");
    	}
    	if(nbtTagCompound.contains("MultiplierEffect")) {
    		this.multiplierEffect = nbtTagCompound.getDouble("MultiplierEffect");
    	}
    	if(nbtTagCompound.contains("MultiplierPierce")) {
    		this.multiplierEffect = nbtTagCompound.getDouble("MultiplierPierce");
    	}

    	// Stat Boosts:
        if(nbtTagCompound.contains("BoostHealth")) {
            this.boostHealth = nbtTagCompound.getInt("BoostHealth");
        }
    	if(nbtTagCompound.contains("BoostDefense")) {
    		this.boostDefense = nbtTagCompound.getInt("BoostDefense");
    	}
		if(nbtTagCompound.contains("BoostArmor")) {
			this.boostArmor = nbtTagCompound.getInt("BoostArmor");
		}
    	if(nbtTagCompound.contains("BoostSpeed")) {
    		this.boostSpeed = nbtTagCompound.getInt("BoostSpeed");
    	}
    	if(nbtTagCompound.contains("BoostDamage")) {
    		this.boostDamage = nbtTagCompound.getInt("BoostDamage");
    	}
    	if(nbtTagCompound.contains("BoostHaste")) {
    		this.boostHaste = nbtTagCompound.getInt("BoostHaste");
    	}
    	if(nbtTagCompound.contains("BoostEffect")) {
    		this.boostEffect = nbtTagCompound.getInt("BoostEffect");
    	}
    	if(nbtTagCompound.contains("BoostPierce")) {
    		this.boostEffect = nbtTagCompound.getInt("BoostPierce");
    	}

    	// Overrides:
    	if(nbtTagCompound.contains("AggressiveOverride")) {
    		this.aggressiveOverride = nbtTagCompound.getBoolean("AggressiveOverride");
    	}
    	if(nbtTagCompound.contains("FlightOverride")) {
    		this.flightOverride = nbtTagCompound.getBoolean("FlightOverride");
    	}
    	if(nbtTagCompound.contains("SwimmingOverride")) {
    		this.swimmingOverride = nbtTagCompound.getBoolean("SwimmingOverride");
    	}
    	if(nbtTagCompound.contains("WaterBreathingOverride")) {
    		this.waterBreathingOverride = nbtTagCompound.getBoolean("WaterBreathingOverride");
    	}
    	if(nbtTagCompound.contains("FireImmunityOverride")) {
    		this.fireImmunityOverride = nbtTagCompound.getBoolean("FireImmunityOverride");
    	}
    	if(nbtTagCompound.contains("StealthOverride")) {
    		this.stealthOverride = nbtTagCompound.getBoolean("StealthOverride");
    	}
    	if(nbtTagCompound.contains("ItemPickupOverride")) {
    		this.itemPickupOverride = nbtTagCompound.getBoolean("ItemPickupOverride");
    	}
    	if(nbtTagCompound.contains("InventorySizeOverride")) {
    		this.inventorySizeOverride = nbtTagCompound.getInt("InventorySizeOverride");
    	}
    	if(nbtTagCompound.contains("ItemDropMultiplierOverride")) {
    		this.itemDropMultiplierOverride = nbtTagCompound.getDouble("ItemDropMultiplierOverride");
    	}
    	
    	// AI:
    	if(nbtTagCompound.contains("AIAttackPlayers")) {
    		this.aiAttackPlayers = nbtTagCompound.getBoolean("AIAttackPlayers");
    		this.host.targetSelector.removeGoal(this.host.aiTargetPlayer);
    		if(this.aiAttackPlayers) {
    			if(this.host.aiTargetPlayer == null) {
    				this.host.aiTargetPlayer = new FindAttackTargetGoal(this.host).addTargets(EntityType.PLAYER);
				}
    			this.host.targetSelector.addGoal(9, this.host.aiTargetPlayer);
    		}
    	}
    	if(nbtTagCompound.contains("AIDefendAnimals")) {
    		this.aiDefendAnimals = nbtTagCompound.getBoolean("AIDefendAnimals");
    		this.host.targetSelector.removeGoal(this.host.aiDefendAnimals);
    		if(this.aiDefendAnimals) {
				if(this.host.aiDefendAnimals == null) {
					this.host.aiDefendAnimals = new RevengeGoal(this.host).setHelpClasses(Animal.class);
				}
    			this.host.targetSelector.addGoal(10, this.host.aiDefendAnimals);
    		}
    	}
    }
    
    // ========== Write ==========
    /** Called from this host passing a compound writing all the extra behaviour options. **/
    public void write(CompoundTag nbtTagCompound) {
    	// Stat Multipliers:
        nbtTagCompound.putDouble("MultiplierHealth", this.multiplierHealth);
    	nbtTagCompound.putDouble("MultiplierDefense", this.multiplierDefense);
		nbtTagCompound.putDouble("MultiplierArmor", this.multiplierArmor);
    	nbtTagCompound.putDouble("MultiplierSpeed", this.multiplierSpeed);
    	nbtTagCompound.putDouble("MultiplierDamage", this.multiplierDamage);
    	nbtTagCompound.putDouble("MultiplierHaste", this.multiplierHaste);
    	nbtTagCompound.putDouble("MultiplierEffect", this.multiplierEffect);
    	nbtTagCompound.putDouble("MultiplierPierce", this.multiplierPierce);

    	// Stat Boosts:
        nbtTagCompound.putInt("BoostHealth", this.boostHealth);
    	nbtTagCompound.putInt("BoostDefense", this.boostDefense);
		nbtTagCompound.putInt("BoostArmor", this.boostArmor);
    	nbtTagCompound.putInt("BoostSpeed", this.boostSpeed);
    	nbtTagCompound.putInt("BoostDamage", this.boostDamage);
    	nbtTagCompound.putInt("BoostHaste", this.boostHaste);
    	nbtTagCompound.putInt("BoostEffect", this.boostEffect);
    	nbtTagCompound.putInt("BoostPierce", this.boostPierce);

    	// Overrides:
    	nbtTagCompound.putBoolean("AggressiveOverride", this.aggressiveOverride);
    	nbtTagCompound.putBoolean("FlightOverride", this.flightOverride);
    	nbtTagCompound.putBoolean("SwimmingOverride", this.swimmingOverride);
    	nbtTagCompound.putBoolean("WaterBreathingOverride", this.waterBreathingOverride);
    	nbtTagCompound.putBoolean("FireImmunityOverride", this.fireImmunityOverride);
    	nbtTagCompound.putBoolean("StealthOverride", this.stealthOverride);
    	nbtTagCompound.putBoolean("ItemPickupOverride", this.itemPickupOverride);
    	nbtTagCompound.putInt("InventorySizeOverride", this.inventorySizeOverride);
    	nbtTagCompound.putDouble("ItemDropMultiplierOverride", this.itemDropMultiplierOverride);
    	
    	// AI:
    	nbtTagCompound.putBoolean("AIAttackPlayers", this.aiAttackPlayers);
    	nbtTagCompound.putBoolean("AIDefendAnimals", this.aiDefendAnimals);
    }
}
