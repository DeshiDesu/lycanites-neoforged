package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageOverlayMessage {
    public Component message;

    public MessageOverlayMessage() {
    }

    public MessageOverlayMessage(MutableComponent message) {
        this.message = message;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageOverlayMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        Minecraft.getInstance().gui.setOverlayMessage(message.message, false);
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageOverlayMessage decode(FriendlyByteBuf packet) {
        MessageOverlayMessage message = new MessageOverlayMessage();
        message.message = packet.readComponent();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageOverlayMessage message, FriendlyByteBuf packet) {
        packet.writeComponent(message.message);
    }

}
