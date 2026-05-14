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

public class MessageEntityPickedUp {
    public int pickedUpEntityID;
    public int pickedUpByEntityID;

    public MessageEntityPickedUp() {
    }

    public MessageEntityPickedUp(Entity pickedUpEntity, Entity pickedUpByEntity) {
        this.pickedUpEntityID = pickedUpEntity.getId();
        this.pickedUpByEntityID = pickedUpByEntity != null ? pickedUpByEntity.getId() : 0;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageEntityPickedUp message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        Player player = ClientManager.getInstance().getClientPlayer();
        Level world = player.getCommandSenderWorld();
        Entity pickedUpEntity = world.getEntity(message.pickedUpEntityID);
        Entity pickedUpByEntity = message.pickedUpByEntityID != 0 ? world.getEntity(message.pickedUpByEntityID) : null;

        if (!(pickedUpEntity instanceof LivingEntity))
            return;
        ExtendedEntity pickedUpEntityExt = ExtendedEntity.getForEntity((LivingEntity) pickedUpEntity);
        if (pickedUpEntityExt != null)
            pickedUpEntityExt.setPickedUpByEntity(pickedUpByEntity);
        return;
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageEntityPickedUp decode(FriendlyByteBuf packet) {
        MessageEntityPickedUp message = new MessageEntityPickedUp();
        message.pickedUpEntityID = packet.readInt();
        message.pickedUpByEntityID = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageEntityPickedUp message, FriendlyByteBuf packet) {
        packet.writeInt(message.pickedUpEntityID);
        packet.writeInt(message.pickedUpByEntityID);
    }

}
