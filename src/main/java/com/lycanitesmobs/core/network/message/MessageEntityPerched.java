package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageEntityPerched {
    public int perchedOnEntityID;
    public int perchedByEntityID;

    public MessageEntityPerched() {
    }

    public MessageEntityPerched(Entity perchedOnEntityID, Entity perchedByEntity) {
        this.perchedOnEntityID = perchedOnEntityID.getId();
        this.perchedByEntityID = perchedByEntity != null ? perchedByEntity.getId() : 0;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageEntityPerched message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        Player player = ClientManager.getInstance().getClientPlayer();
        Level world = player.getCommandSenderWorld();
        Entity perchedOnEntity = world.getEntity(message.perchedOnEntityID);
        Entity perchedByEntity = message.perchedByEntityID != 0 ? world.getEntity(message.perchedByEntityID) : null;

        if (!(perchedOnEntity instanceof LivingEntity))
            return;
        ExtendedEntity perchedOnEntityExt = ExtendedEntity.getForEntity((LivingEntity) perchedOnEntity);
        if (perchedOnEntityExt != null)
            perchedOnEntityExt.setPerchedByEntity(perchedByEntity);
        return;
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageEntityPerched decode(FriendlyByteBuf packet) {
        MessageEntityPerched message = new MessageEntityPerched();
        message.perchedOnEntityID = packet.readInt();
        message.perchedByEntityID = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageEntityPerched message, FriendlyByteBuf packet) {
        packet.writeInt(message.perchedOnEntityID);
        packet.writeInt(message.perchedByEntityID);
    }

}
