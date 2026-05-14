package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import com.lycanitesmobs.core.entity.pets.PlayerFamiliars;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class PetManager {
    public LivingEntity host;
    /**
     * A list of all pet entries, useful for looking up everything summoned by an entity as well as ensuring that no entries are added as multiple types.
     **/
    public Map<UUID, PetEntry> entries = new HashMap<>();
    /**
     * Newly added PetEntries that need to be synced to the client player.
     **/
    public List<PetEntry> newEntries = new ArrayList<>();
    /**
     * PetEntries that need to be removed.
     **/
    public List<PetEntry> removedEntries = new ArrayList<>();
    /**
     * A map containing NBT Tag Compounds mapped to Pet Entry UUIDs.
     **/
    public Map<UUID, CompoundTag> entryNBTs = new HashMap<>();


    public PetManager(LivingEntity host) {
        this.host = host;
    }

    /**
     * Returns true if the provided entry is in this manager.
     **/
    public boolean hasEntry(PetEntry petEntry) {
        return this.entries.containsKey(petEntry.petEntryID);
    }

    /**
     * Adds a new PetEntry and executes onAdd() methods. The provided entry should have set whether it's a pet, mount, minion, etc.
     **/
    public void addEntry(PetEntry petEntry) {
        if (this.entries.containsKey(petEntry.petEntryID)) {
            LMHelperClass.logWarning("", "[Pet Manager] Tried to add a Pet Entry that is already added!");
            return;
        }

        // Load From NBT:
        if (this.entryNBTs.containsKey(petEntry.petEntryID)) {
            petEntry.readFromNBT(this.entryNBTs.get(petEntry.petEntryID));
        }

        this.entries.put(petEntry.petEntryID, petEntry);
        petEntry.onAdd(this);
        this.newEntries.add(petEntry);
    }

    /**
     * Removes an entry from this manager. This is called automatically if the entry itself is no longer active.
     * This will not cause the entry itself to become inactive if it is still active.
     * If an entry is finished, it is best to call onRemove() on the entry itself, this method will then be called automatically.
     **/
    public void removeEntry(PetEntry petEntry) {
        if (!this.entries.containsValue(petEntry)) {
            LMHelperClass.logWarning("", "[Pet Manager] Tried to remove a pet entry that isn't added!");
            return;
        }

        this.entries.remove(petEntry.petEntryID);
    }

    /**
     * Returns the requested pet entry from its specific id.
     **/
    public PetEntry getEntry(UUID id) {
        return this.entries.get(id);
    }

    /**
     * Returns the requested entry list.
     **/
    public List<PetEntry> createEntryListByType(String type) {
        List<PetEntry> filteredEntries = new ArrayList<>();
        for (PetEntry petEntry : this.entries.values()) {
            if (type.equalsIgnoreCase(petEntry.getType())) {
                filteredEntries.add(petEntry);
            }
        }
        return filteredEntries;
    }

    /**
     * Called by the host's entity update, runs any logic to manage pet entries.
     **/
    public void onUpdate(Level world) {
        if (this.host == null)
            return;

        if (!this.entryNBTs.isEmpty()) {

            for (PetEntry petEntry : this.entries.values()) {
                if (this.entryNBTs.containsKey(petEntry.petEntryID)) {
                    petEntry.readFromNBT(this.entryNBTs.get(petEntry.petEntryID));
                    this.entryNBTs.remove(petEntry.petEntryID);
                }
            }

            for (CompoundTag nbtEntry : this.entryNBTs.values()) {
                if (this.host instanceof Player && nbtEntry.getString("Type").equalsIgnoreCase("familiar")) {
                    if (!PlayerFamiliars.INSTANCE.playerFamiliars.containsKey(this.host.getUUID()) ||
                            !PlayerFamiliars.INSTANCE.playerFamiliars.get(this.host.getUUID()).containsKey(nbtEntry.getUUID("UUID"))) {
                        continue;
                    }
                }
                PetEntry petEntry = new PetEntry(nbtEntry.getUUID("UUID"), nbtEntry.getString("Type"), this.host, nbtEntry.getString("SummonType"));
                petEntry.readFromNBT(nbtEntry);
                if (petEntry.active)
                    this.addEntry(petEntry);
            }

            this.entryNBTs.clear();
        }

        // New Entries:
        if (this.newEntries.size() > 0) {
            if (!world.isClientSide && this.host instanceof Player) {
                for (PetEntry petEntry : this.newEntries) {
                    ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer((Player) this.host);
                    if (playerExt != null)
                        playerExt.sendPetEntryToPlayer(petEntry);
                }
            }
            this.newEntries = new ArrayList<>();
        }

        // Entry Updates:
        int newSpiritReserved = 0;
        for (PetEntry petEntry : new ArrayList<>(this.entries.values())) {

            // Owner Check:
            if (petEntry.host != this.host)
                petEntry.setOwner(this.host);

            // Pet and Mount Spirit Check:
            if (this.host instanceof Player) {
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer((Player) this.host);
                if (playerExt != null && petEntry.usesSpirit()) {
                    int spiritCost = petEntry.getSpiritCost();
                    if (petEntry.spawningActive && petEntry.active) {
                        newSpiritReserved += spiritCost;
                        if ((playerExt.spirit + playerExt.spiritReserved < newSpiritReserved) || newSpiritReserved > playerExt.spiritMax) {
                            petEntry.spawningActive = false;
                            newSpiritReserved -= spiritCost;
                        }
                    }
                }
            }

            if (petEntry.active)
                petEntry.onUpdate(world);
            else
                this.removedEntries.add(petEntry);
        }

        // Remove Inactive Entries:
        if (this.removedEntries.size() > 0) {
            for (PetEntry petEntry : this.removedEntries) {
                this.removeEntry(petEntry);
            }
            this.removedEntries = new ArrayList<>();
        }

        // Spirit Reserved:
        if (this.host instanceof Player) {
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer((Player) this.host);
            if (playerExt != null)
                playerExt.spiritReserved = newSpiritReserved;
        }
    }


    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========

    /**
     * Reads a list of Pet Entries from a player's NBTTag.
     **/
    public void readFromNBT(CompoundTag nbtTagCompound) {
        if (!nbtTagCompound.contains("PetManager"))
            return;

        ListTag entryList = nbtTagCompound.getList("PetManager", 10);
        for (int i = 0; i < entryList.size(); ++i) {
            CompoundTag nbtEntry = (CompoundTag) entryList.get(i);
            if (!nbtEntry.hasUUID("UUID") && nbtEntry.contains("EntryName")) { // Convert Pet Entries from older mod versions.
                LMHelperClass.logInfo("", "[Pets] Converting Pet Entry from older mod version: " + nbtEntry.getString("EntryName") + "...");
                nbtEntry.putUUID("UUID", UUID.randomUUID());
            }
            if (nbtEntry.hasUUID("UUID")) {
                this.entryNBTs.put(nbtEntry.getUUID("UUID"), nbtEntry);
            } else {
                LMHelperClass.logWarning("", "[Pets] A Pet Entry was missing a UUID and EntryName, this is either a bug or NBT data has been tampered with, please report this!");
            }
        }
    }

    // ========== Write ==========

    /**
     * Writes a list of Creature Knowledge to a player's NBTTag.
     **/
    public void writeToNBT(CompoundTag nbtTagCompound) {
        ListTag entryList = new ListTag();
        for (PetEntry petEntry : this.entries.values()) {
            CompoundTag nbtEntry = new CompoundTag();
            petEntry.writeToNBT(nbtEntry);
            entryList.add(nbtEntry);
        }
        nbtTagCompound.put("PetManager", entryList);
    }
}
