package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.network.message.*;
import com.lycanitesmobs.core.network.packet.MessageScreenRequest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketManager {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LycanitesMobs.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );


    // ==================================================
    //                    Initialize
    // ==================================================

    /**
     * Initializes the Packet Handler where Messages are registered.
     */
    public void register() {
        int messageID = 0;

        HANDLER.registerMessage(messageID++, MessageBeastiary.class, MessageBeastiary::encode, MessageBeastiary::decode, MessageBeastiary::handle);
        HANDLER.registerMessage(messageID++, MessageCreature.class, MessageCreature::encode, MessageCreature::decode, MessageCreature::handle);
        HANDLER.registerMessage(messageID++, MessageCreatureKnowledge.class, MessageCreatureKnowledge::encode, MessageCreatureKnowledge::decode, MessageCreatureKnowledge::handle);
        HANDLER.registerMessage(messageID++, MessagePlayerStats.class, MessagePlayerStats::encode, MessagePlayerStats::decode, MessagePlayerStats::handle);
        HANDLER.registerMessage(messageID++, MessagePetEntry.class, MessagePetEntry::encode, MessagePetEntry::decode, MessagePetEntry::handle);
        HANDLER.registerMessage(messageID++, MessagePetEntryRemove.class, MessagePetEntryRemove::encode, MessagePetEntryRemove::decode, MessagePetEntryRemove::handle);
        HANDLER.registerMessage(messageID++, MessageSummonSet.class, MessageSummonSet::encode, MessageSummonSet::decode, MessageSummonSet::handle);
        HANDLER.registerMessage(messageID++, MessageSummonSetSelection.class, MessageSummonSetSelection::encode, MessageSummonSetSelection::decode, MessageSummonSetSelection::handle);
        HANDLER.registerMessage(messageID++, MessageEntityPickedUp.class, MessageEntityPickedUp::encode, MessageEntityPickedUp::decode, MessageEntityPickedUp::handle);
        HANDLER.registerMessage(messageID++, MessageEntityPerched.class, MessageEntityPerched::encode, MessageEntityPerched::decode, MessageEntityPerched::handle);
        HANDLER.registerMessage(messageID++, MessageWorldEvent.class, MessageWorldEvent::encode, MessageWorldEvent::decode, MessageWorldEvent::handle);
        HANDLER.registerMessage(messageID++, MessageMobEvent.class, MessageMobEvent::encode, MessageMobEvent::decode, MessageMobEvent::handle);
        HANDLER.registerMessage(messageID++, MessageSummoningPedestalStats.class, MessageSummoningPedestalStats::encode, MessageSummoningPedestalStats::decode, MessageSummoningPedestalStats::handle);
        HANDLER.registerMessage(messageID++, MessageEntityVelocity.class, MessageEntityVelocity::encode, MessageEntityVelocity::decode, MessageEntityVelocity::handle);
        HANDLER.registerMessage(messageID++, MessageEntityGUICommand.class, MessageEntityGUICommand::encode, MessageEntityGUICommand::decode, MessageEntityGUICommand::handle);
        HANDLER.registerMessage(messageID++, MessageScreenRequest.class, MessageScreenRequest::encode, MessageScreenRequest::decode, MessageScreenRequest::handle);
        HANDLER.registerMessage(messageID++, MessagePlayerControl.class, MessagePlayerControl::encode, MessagePlayerControl::decode, MessagePlayerControl::handle);
        HANDLER.registerMessage(messageID++, MessagePlayerLeftClick.class, MessagePlayerLeftClick::encode, MessagePlayerLeftClick::decode, MessagePlayerLeftClick::handle);
        HANDLER.registerMessage(messageID++, MessageSummoningPedestalSummonSet.class, MessageSummoningPedestalSummonSet::encode, MessageSummoningPedestalSummonSet::decode, MessageSummoningPedestalSummonSet::handle);
        HANDLER.registerMessage(messageID++, MessageTileEntityButton.class, MessageTileEntityButton::encode, MessageTileEntityButton::decode, MessageTileEntityButton::handle);
        HANDLER.registerMessage(messageID++, MessageSpawnEntity.class, MessageSpawnEntity::encode, MessageSpawnEntity::decode, MessageSpawnEntity::handle);
        HANDLER.registerMessage(messageID++, MessageOverlayMessage.class, MessageOverlayMessage::encode, MessageOverlayMessage::decode, MessageOverlayMessage::handle);
    }


    // ==================================================
    //                   Send To Player
    // ==================================================

    /**
     * Sends a packet from the server to the specified player.
     *
     * @param message The network packet message to send.
     * @param player  The player to send to.
     */
    public <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        HANDLER.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }


    // ==================================================
    //                 Send To All Around
    // ==================================================

    /**
     * Sends a packet from the server to all players near the specified block position within range.
     *
     * @param message The network packet message to send.
     * @param pos     The position to find players from.
     * @param range   The range to find players within.
     */
    public <MSG> void sendToAllAround(MSG message, Level world, Vec3 pos, double range) {
        for (Player player : world.players()) {
            if (player instanceof ServerPlayer && player.distanceToSqr(pos) <= (range * range)) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                this.sendToPlayer(message, serverPlayer);
            }
        }
    }


    // ==================================================
    //                 Send To Dimension
    // ==================================================

    /**
     * Sends a packet to all players within the specified world.
     *
     * @param message The network packet message to send.
     * @param world   The world to find players to send packets to.
     */
    public <MSG> void sendToWorld(MSG message, Level world) {
        for (Player player : world.players()) {
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                HANDLER.sendTo(message, serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }


    // ==================================================
    //                   Send To Server
    // ==================================================

    /**
     * Sends a packet from the client player to the server.
     *
     * @param message The network packet message to send.
     */
    public <MSG> void sendToServer(MSG message) {
        HANDLER.sendToServer(message);
    }
}
