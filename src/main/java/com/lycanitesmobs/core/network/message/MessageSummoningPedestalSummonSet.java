package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.core.entity.pets.SummonSet;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSummoningPedestalSummonSet {
    public String summonType;
    public int subpsecies;
    public int variant;
    public byte behaviour;
    public int x;
    public int y;
    public int z;

    public MessageSummoningPedestalSummonSet() {
    }

    public MessageSummoningPedestalSummonSet(SummonSet summonSet, int x, int y, int z) {
        this.summonType = summonSet.summonType;
        this.subpsecies = summonSet.subspecies;
        this.variant = summonSet.variant;
        this.behaviour = summonSet.getBehaviourByte();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSummoningPedestalSummonSet message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER)
            return;

        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            BlockEntity tileEntity = player.getCommandSenderWorld().getBlockEntity(new BlockPos(message.x, message.y, message.z));
            TileEntitySummoningPedestal summoningPedestal = null;
            if (tileEntity instanceof TileEntitySummoningPedestal)
                summoningPedestal = (TileEntitySummoningPedestal) tileEntity;
            if (summoningPedestal == null)
                return;
            if (summoningPedestal.summonSet == null)
                summoningPedestal.summonSet = new SummonSet(null);
            summoningPedestal.summonSet.readFromPacket(message.summonType, message.subpsecies, message.variant, message.behaviour);
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSummoningPedestalSummonSet decode(FriendlyByteBuf packet) {
        MessageSummoningPedestalSummonSet message = new MessageSummoningPedestalSummonSet();
        message.x = packet.readInt();
        message.y = packet.readInt();
        message.z = packet.readInt();
        message.summonType = packet.readUtf(256);
        message.subpsecies = packet.readInt();
        message.variant = packet.readInt();
        message.behaviour = packet.readByte();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSummoningPedestalSummonSet message, FriendlyByteBuf packet) {
        packet.writeInt(message.x);
        packet.writeInt(message.y);
        packet.writeInt(message.z);
        packet.writeUtf(message.summonType);
        packet.writeInt(message.subpsecies);
        packet.writeInt(message.variant);
        packet.writeByte(message.behaviour);
    }

}
