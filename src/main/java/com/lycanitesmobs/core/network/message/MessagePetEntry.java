package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import com.lycanitesmobs.core.manager.PetManager;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessagePetEntry {
    public UUID petEntryID;
    public String petEntryType;
    public boolean spawningActive;
    public boolean teleportEntity;
    public String summonType;
    public int subspecies;
    public int variant;
    public byte behaviour;
    public int petEntryEntityID = -1;
    public String petEntryEntityName;
    public int respawnTime;
    public int respawnTimeMax;
    public int entityLevel;
    public int entityExperience;
    public boolean isRespawning;

    public MessagePetEntry() {
    }

    public MessagePetEntry(ExtendedPlayer playerExt, PetEntry petEntry) {
        this.petEntryID = petEntry.petEntryID;
        this.petEntryType = petEntry.getType();
        this.spawningActive = petEntry.spawningActive;
        this.teleportEntity = petEntry.teleportEntity;
        SummonSet summonSet = petEntry.summonSet;
        this.summonType = summonSet.summonType;
        this.subspecies = petEntry.subspeciesIndex;
        this.variant = petEntry.variantIndex;
        this.behaviour = summonSet.getBehaviourByte();
        this.petEntryEntityID = petEntry.entity != null ? petEntry.entity.getId() : -1;
        this.petEntryEntityName = petEntry.entityName;
        this.respawnTime = petEntry.respawnTime;
        this.respawnTimeMax = petEntry.respawnTimeMax;
        this.entityLevel = petEntry.entityLevel;
        this.entityExperience = petEntry.entityExperience;
        this.isRespawning = petEntry.isRespawning;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePetEntry message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        // Server Side:
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                Player player = ctx.get().getSender();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
                PetManager petManager = playerExt.petManager;
                PetEntry petEntry = petManager.getEntry(message.petEntryID);
                if (petEntry == null)
                    return;
                petEntry.setSpawningActive(message.spawningActive);
                petEntry.teleportEntity = message.teleportEntity;
                SummonSet summonSet = petEntry.summonSet;
                summonSet.readFromPacket(message.summonType, message.subspecies, message.variant, message.behaviour);
                petEntry.onBehaviourUpdate();
            });
            return;
        }

        // Client Side:
        Player player = ClientManager.getInstance().getClientPlayer();
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null)
            return;
        PetManager petManager = playerExt.petManager;
        PetEntry petEntry = petManager.getEntry(message.petEntryID);
        if (petEntry == null) {
            petEntry = new PetEntry(message.petEntryID, message.petEntryType, player, message.summonType);
            petManager.addEntry(petEntry);
        }
        petEntry.setSpawningActive(message.spawningActive);
        petEntry.teleportEntity = message.teleportEntity;
        petEntry.subspeciesIndex = message.subspecies;
        petEntry.variantIndex = message.variant;
        SummonSet summonSet = petEntry.summonSet;
        summonSet.readFromPacket(message.summonType, message.subspecies, message.variant, message.behaviour);
        Entity entity = null;
        if (message.petEntryEntityID != -1) {
            entity = player.getCommandSenderWorld().getEntity(message.petEntryEntityID);
        }
        petEntry.entity = entity;
        petEntry.entityName = message.petEntryEntityName;
        petEntry.respawnTime = message.respawnTime;
        petEntry.respawnTimeMax = message.respawnTimeMax;
        petEntry.entityLevel = message.entityLevel;
        petEntry.entityExperience = message.entityExperience;
        petEntry.isRespawning = message.isRespawning;
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePetEntry decode(FriendlyByteBuf packet) {
        MessagePetEntry message = new MessagePetEntry();
        message.petEntryID = packet.readUUID();
        message.petEntryType = packet.readUtf(512);
        message.spawningActive = packet.readBoolean();
        message.teleportEntity = packet.readBoolean();
        message.summonType = packet.readUtf(512);
        message.subspecies = packet.readInt();
        message.variant = packet.readInt();
        message.behaviour = packet.readByte();
        message.petEntryEntityID = packet.readInt();
        message.petEntryEntityName = packet.readUtf(1024);
        message.respawnTime = packet.readInt();
        message.respawnTimeMax = packet.readInt();
        message.entityLevel = packet.readInt();
        message.entityExperience = packet.readInt();
        message.isRespawning = packet.readBoolean();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePetEntry message, FriendlyByteBuf packet) {
        packet.writeUUID(message.petEntryID);
        packet.writeUtf(message.petEntryType);
        packet.writeBoolean(message.spawningActive);
        packet.writeBoolean(message.teleportEntity);
        packet.writeUtf(message.summonType);
        packet.writeInt(message.subspecies);
        packet.writeInt(message.variant);
        packet.writeByte(message.behaviour);
        packet.writeInt(message.petEntryEntityID);
        packet.writeUtf(message.petEntryEntityName);
        packet.writeInt(message.respawnTime);
        packet.writeInt(message.respawnTimeMax);
        packet.writeInt(message.entityLevel);
        packet.writeInt(message.entityExperience);
        packet.writeBoolean(message.isRespawning);
    }

}
