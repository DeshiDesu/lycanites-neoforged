package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSummoningPedestalStats {
    public int capacity;
    public int progress;
    public int fuel;
    public int fuelMax;
    public int x;
    public int y;
    public int z;

    public MessageSummoningPedestalStats() {
    }

    public MessageSummoningPedestalStats(int capacity, int progress, int fuel, int fuelMax, int x, int y, int z) {
        this.capacity = capacity;
        this.progress = progress;
        this.fuel = fuel;
        this.fuelMax = fuelMax;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSummoningPedestalStats message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() -> {
            Player player = ClientManager.getInstance().getClientPlayer();
            BlockEntity tileEntity = player.getCommandSenderWorld().getBlockEntity(new BlockPos(message.x, message.y, message.z));
            TileEntitySummoningPedestal summoningPedestal = null;
            if (tileEntity instanceof TileEntitySummoningPedestal)
                summoningPedestal = (TileEntitySummoningPedestal) tileEntity;
            if (summoningPedestal == null)
                return;
            summoningPedestal.capacity = message.capacity;
            summoningPedestal.summonProgress = message.progress;
            summoningPedestal.summoningFuel = message.fuel;
            summoningPedestal.summoningFuelMax = message.fuelMax;
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSummoningPedestalStats decode(FriendlyByteBuf packet) {
        MessageSummoningPedestalStats message = new MessageSummoningPedestalStats();
        message.x = packet.readInt();
        message.y = packet.readInt();
        message.z = packet.readInt();
        message.capacity = packet.readInt();
        message.progress = packet.readInt();
        message.fuel = packet.readInt();
        message.fuelMax = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSummoningPedestalStats message, FriendlyByteBuf packet) {
        packet.writeInt(message.x);
        packet.writeInt(message.y);
        packet.writeInt(message.z);
        packet.writeInt(message.capacity);
        packet.writeInt(message.progress);
        packet.writeInt(message.fuel);
        packet.writeInt(message.fuelMax);
    }

}
