package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessagePlayerControl {
    public byte controlStates;

    public MessagePlayerControl() {
    }

    public MessagePlayerControl(byte controlStates) {
        this.controlStates = controlStates;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePlayerControl message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER)
            return;

        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            playerExt.updateControlStates(message.controlStates);
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePlayerControl decode(FriendlyByteBuf packet) {
        MessagePlayerControl message = new MessagePlayerControl();
        message.controlStates = packet.readByte();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePlayerControl message, FriendlyByteBuf packet) {
        packet.writeByte(message.controlStates);
    }

}
