package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageEntityGUICommand {
    int entityID;
    public int guiCommandID;

    public MessageEntityGUICommand() {
    }

    public MessageEntityGUICommand(int guiCommandID, Entity entity) {
        this.entityID = entity.getId();
        this.guiCommandID = guiCommandID;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageEntityGUICommand message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER)
            return;

        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            Level world = player.getCommandSenderWorld();
            Entity entity = world.getEntity(message.entityID);
            if (entity instanceof TameableCreatureEntity) {
                TameableCreatureEntity pet = (TameableCreatureEntity) entity;
                pet.performGUICommand(player, message.guiCommandID);
            }
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageEntityGUICommand decode(FriendlyByteBuf packet) {
        MessageEntityGUICommand message = new MessageEntityGUICommand();
        message.entityID = packet.readInt();
        message.guiCommandID = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageEntityGUICommand message, FriendlyByteBuf packet) {
        packet.writeInt(message.entityID);
        packet.writeInt(message.guiCommandID);
    }

}
