package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.core.block.blockentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageTileEntityButton {
    public int buttonId;
    public BlockPos tileEntityPos;

    public MessageTileEntityButton() {
    }

    public MessageTileEntityButton(int buttonId, BlockPos tileEntityPos) {
        this.buttonId = buttonId;
        this.tileEntityPos = tileEntityPos;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageTileEntityButton message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER)
            return;

        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            BlockEntity tileEntity = player.getCommandSenderWorld().getBlockEntity(message.tileEntityPos);
            if (!(tileEntity instanceof TileEntityBase)) {
                return;
            }
            TileEntityBase tileEntityBase = (TileEntityBase) tileEntity;
            tileEntityBase.onGuiButton(message.buttonId);
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageTileEntityButton decode(FriendlyByteBuf packet) {
        MessageTileEntityButton message = new MessageTileEntityButton();
        message.buttonId = packet.readInt();
        message.tileEntityPos = new BlockPos(packet.readInt(), packet.readInt(), packet.readInt());
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageTileEntityButton message, FriendlyByteBuf packet) {
        packet.writeInt(message.buttonId);
        packet.writeInt(message.tileEntityPos.getX());
        packet.writeInt(message.tileEntityPos.getY());
        packet.writeInt(message.tileEntityPos.getZ());
    }

}
