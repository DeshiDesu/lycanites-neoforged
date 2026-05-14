package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageWorldEvent {
    public String mobEventName;
    public BlockPos pos;
    public int level = 1;
    public int subspecies = 1;

    public MessageWorldEvent() {
    }

    public MessageWorldEvent(String mobEventName, BlockPos pos, int level, int subspecies) {
        this.mobEventName = mobEventName;
        this.pos = pos;
        this.level = level;
        this.subspecies = subspecies;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageWorldEvent message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        Player player = ClientManager.getInstance().getClientPlayer();
        Level world = player.getCommandSenderWorld();
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);

        if ("".equals(message.mobEventName))
            worldExt.stopWorldEvent();
        else {
            worldExt.startMobEvent(message.mobEventName, null, message.pos, message.level, message.subspecies);
        }
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageWorldEvent decode(FriendlyByteBuf packet) {
        MessageWorldEvent message = new MessageWorldEvent();
        message.mobEventName = packet.readUtf(256);
        message.pos = packet.readBlockPos();
        message.level = packet.readInt();
        message.subspecies = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageWorldEvent message, FriendlyByteBuf packet) {
        packet.writeUtf(message.mobEventName);
        packet.writeBlockPos(message.pos);
        packet.writeInt(message.level);
        packet.writeInt(message.subspecies);
    }

}
