package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessagePlayerStats {
    public int spirit;
    public int summonFocus;
    public int creatureStudyCooldown;

    public MessagePlayerStats() {
    }

    public MessagePlayerStats(ExtendedPlayer playerExt) {
        this.spirit = playerExt.spirit;
        this.summonFocus = playerExt.summonFocus;
        this.creatureStudyCooldown = playerExt.creatureStudyCooldown;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePlayerStats message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        Player player = ClientManager.getInstance().getClientPlayer();
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null)
            return;

        playerExt.spirit = message.spirit;
        playerExt.summonFocus = message.summonFocus;
        playerExt.creatureStudyCooldown = message.creatureStudyCooldown;
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePlayerStats decode(FriendlyByteBuf packet) {
        MessagePlayerStats message = new MessagePlayerStats();
        message.spirit = packet.readInt();
        message.summonFocus = packet.readInt();
        message.creatureStudyCooldown = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePlayerStats message, FriendlyByteBuf packet) {
        packet.writeInt(message.spirit);
        packet.writeInt(message.summonFocus);
        packet.writeInt(message.creatureStudyCooldown);
    }

}
