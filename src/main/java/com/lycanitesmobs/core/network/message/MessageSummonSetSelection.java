package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSummonSetSelection {
    public byte summonSetID;

    public MessageSummonSetSelection() {
    }

    public MessageSummonSetSelection(ExtendedPlayer playerExt) {
        this.summonSetID = (byte) playerExt.selectedSummonSet;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSummonSetSelection message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        // Server Side:
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                Player player = ctx.get().getSender();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
                playerExt.setSelectedSummonSet(message.summonSetID);
            });
            return;
        }

        // Client Side:
        Player player = ClientManager.getInstance().getClientPlayer();
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null)
            return;
        playerExt.setSelectedSummonSet(message.summonSetID);
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSummonSetSelection decode(FriendlyByteBuf packet) {
        MessageSummonSetSelection message = new MessageSummonSetSelection();
        message.summonSetID = packet.readByte();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSummonSetSelection message, FriendlyByteBuf packet) {
        packet.writeByte(message.summonSetID);
    }

}
