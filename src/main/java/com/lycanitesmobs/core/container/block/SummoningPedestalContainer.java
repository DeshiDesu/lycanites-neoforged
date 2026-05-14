package com.lycanitesmobs.core.container.block;

import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import com.lycanitesmobs.core.container.base.BaseContainer;
import com.lycanitesmobs.core.container.base.BaseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;

public class SummoningPedestalContainer extends BaseContainer {
    public static final MenuType<SummoningPedestalContainer> TYPE = (MenuType<SummoningPedestalContainer>) IForgeMenuType.create(SummoningPedestalContainer::new);
    public TileEntitySummoningPedestal summoningPedestal;

    /**
     * Client Constructor
     *
     * @param windowId        The window id for the gui screen to use.
     * @param playerInventory The accessing player's inventory.
     * @param extraData       A packet sent from the server to create the Container from.
     */
    public SummoningPedestalContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, (TileEntitySummoningPedestal) playerInventory.player.getCommandSenderWorld().getBlockEntity(BlockPos.of(extraData.readLong())));
    }

    /**
     * Main Constructor
     *
     * @param windowId
     * @param playerInventory
     * @param summoningPedestal
     */
    public SummoningPedestalContainer(int windowId, Inventory playerInventory, TileEntitySummoningPedestal summoningPedestal) {
        super(TYPE, windowId);
        this.summoningPedestal = summoningPedestal;
        this.inventoryStart = this.slots.size();

        // Player Inventory
        this.playerInventoryStart = this.slots.size();
        this.addSlotGrid(playerInventory, 8, 171, 1, 0, 8);

        // Pedestal Inventory
        int slots = 0;
        if (summoningPedestal.getContainerSize() > 0) {
            this.addSlot(new BaseSlot(summoningPedestal, slots++, 93, 43));
        }
        this.inventoryFinish = this.inventoryStart + slots;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.summoningPedestal == null || !this.summoningPedestal.stillValid(player))
            return false;
        return player == this.summoningPedestal.getPlayer();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotID) {
        return ItemStack.EMPTY;
    }
}
