package com.lycanitesmobs.core.entity.pets;



import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class PetEntryFamiliar extends PetEntry {

    // ==================================================
    //                     Constructor
    // ==================================================
	public PetEntryFamiliar(UUID petEntryID, LivingEntity host, String summonType) {
        super(petEntryID, "familiar", host, summonType);
	}
}
