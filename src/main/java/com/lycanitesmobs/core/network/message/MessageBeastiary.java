package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.info.gui.Beastiary;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageBeastiary {
    public int entryAmount = 0;
    public String[] creatureNames;
    public int[] ranks;
    public int[] experience;

    public MessageBeastiary() {
    }

    public MessageBeastiary(Beastiary beastiary) {
        this.entryAmount = Math.min(201, beastiary.creatureKnowledgeList.size());
        if (this.entryAmount > 0) {
            this.creatureNames = new String[this.entryAmount];
            this.ranks = new int[this.entryAmount];
            this.experience = new int[this.entryAmount];
            int i = 0;
            for (CreatureKnowledge creatureKnowledge : beastiary.creatureKnowledgeList.values()) {
                this.creatureNames[i] = creatureKnowledge.creatureName;
                this.ranks[i] = creatureKnowledge.rank;
                this.experience[i] = creatureKnowledge.experience;
                i++;
            }
        }
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageBeastiary message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        Player player = ClientManager.getInstance().getClientPlayer();
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null)
            return;
        if (message.entryAmount < 0)
            return;

        playerExt.getBeastiary().creatureKnowledgeList.clear();
        for (int i = 0; i < message.entryAmount; i++) {
            String creatureName = message.creatureNames[i];
            int rank = message.ranks[i];
            int experience = message.experience[i];
            CreatureKnowledge creatureKnowledge = new CreatureKnowledge(playerExt.getBeastiary(), creatureName, rank, experience);
            playerExt.getBeastiary().creatureKnowledgeList.put(creatureKnowledge.creatureName, creatureKnowledge);
        }
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageBeastiary decode(FriendlyByteBuf packet) {
        MessageBeastiary message = new MessageBeastiary();
        message.entryAmount = Math.min(300, packet.readInt());
        if (message.entryAmount == 300) {
            LMHelperClass.logWarning("", "Received 300 or more creature entries, something went wrong with the Beastiary packet! Addition entries will be skipped to prevent OOM!");
        }
        if (message.entryAmount > 0) {
            message.creatureNames = new String[message.entryAmount];
            message.ranks = new int[message.entryAmount];
            message.experience = new int[message.entryAmount];
            for (int i = 0; i < message.entryAmount; i++) {
                message.creatureNames[i] = packet.readUtf(32767);
                message.ranks[i] = packet.readInt();
                message.experience[i] = packet.readInt();
            }
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageBeastiary message, FriendlyByteBuf packet) {
        packet.writeInt(message.entryAmount);
        if (message.entryAmount > 0) {
            for (int i = 0; i < message.entryAmount; i++) {
                packet.writeUtf(message.creatureNames[i]);
                packet.writeInt(message.ranks[i]);
                packet.writeInt(message.experience[i]);
            }
        }
    }
}
