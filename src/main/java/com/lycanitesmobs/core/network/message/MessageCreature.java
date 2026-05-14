package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageCreature {
    int entityID;
    int playerReputation = 0;

    public MessageCreature() {
    }

    public MessageCreature(BaseCreatureEntity creatureEntity, int playerReputation) {
        this.entityID = creatureEntity.getId();
        this.playerReputation = playerReputation;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageCreature message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        Player player = ClientManager.getInstance().getClientPlayer();
        Level world = player.getCommandSenderWorld();
        Entity entity = world.getEntity(message.entityID);
        if (!(entity instanceof BaseCreatureEntity)) {
            return;
        }
        BaseCreatureEntity creatureEntity = (BaseCreatureEntity) entity;

        CreatureRelationshipEntry relationshipEntry = creatureEntity.relationships.getOrCreateEntry(player);
        relationshipEntry.setReputation(message.playerReputation);
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageCreature decode(FriendlyByteBuf packet) {
        MessageCreature message = new MessageCreature();
        try {
            message.entityID = packet.readInt();
            message.playerReputation = packet.readInt();
        } catch (Exception e) {
            LMHelperClass.logWarning("", "There was a problem decoding the packet: " + packet + ".");
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageCreature message, FriendlyByteBuf packet) {
        packet.writeInt(message.entityID);
        packet.writeInt(message.playerReputation);
    }

}
