package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class MessageCreatureKnowledge {
    public String creatureName;
    public int rank;
    public int experience;

    public MessageCreatureKnowledge() {
    }

    public MessageCreatureKnowledge(CreatureKnowledge creatureKnowledge) {
        this.creatureName = creatureKnowledge.creatureName;
        this.rank = creatureKnowledge.rank;
        this.experience = creatureKnowledge.experience;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageCreatureKnowledge message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        Player player = ClientManager.getInstance().getClientPlayer();
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null)
            return;

        playerExt.beastiary.addCreatureKnowledge(new CreatureKnowledge(playerExt.beastiary, message.creatureName, message.rank, message.experience), false);
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageCreatureKnowledge decode(FriendlyByteBuf packet) {
        MessageCreatureKnowledge message = new MessageCreatureKnowledge();
        try {
            message.creatureName = packet.readUtf(256);
            message.rank = packet.readInt();
            message.experience = packet.readInt();
        } catch (Exception e) {
            LMHelperClass.logWarning("", "There was a problem decoding the packet: " + packet + ".");
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageCreatureKnowledge message, FriendlyByteBuf packet) {
        packet.writeUtf(message.creatureName);
        packet.writeInt(message.rank);
        packet.writeInt(message.experience);
    }

}
