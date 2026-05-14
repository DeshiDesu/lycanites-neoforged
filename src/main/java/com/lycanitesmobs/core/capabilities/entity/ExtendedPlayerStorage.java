package com.lycanitesmobs.core.capabilities.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;

public class ExtendedPlayerStorage {

    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========
    /** Reads a list of Creature Knowledge from a player's NBTTag. **/
    public void readNBT(Capability<ExtendedPlayer> capability, ExtendedPlayer instance, Direction facing, CompoundTag nbt) {
        instance.readNBT(nbt);
    }

    // ========== Write ==========
    /** Writes a list of Creature Knowledge to a player's NBTTag. **/
    public CompoundTag writeNBT(Capability<ExtendedPlayer> capability, ExtendedPlayer instance, Direction facing) {
        CompoundTag extTagCompound = new CompoundTag();
        instance.writeNBT(extTagCompound);
        return extTagCompound;
    }
}
