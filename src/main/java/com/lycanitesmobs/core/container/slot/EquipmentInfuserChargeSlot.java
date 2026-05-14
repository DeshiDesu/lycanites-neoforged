package com.lycanitesmobs.core.container.slot;

import com.lycanitesmobs.core.container.block.EquipmentInfuserContainer;
import com.lycanitesmobs.core.container.base.BaseSlot;
import com.lycanitesmobs.core.item.consumable.entity.ChargeItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EquipmentInfuserChargeSlot extends BaseSlot {
    public EquipmentInfuserContainer container;

    /**
     * Constructor
     *
     * @param infuserContainer The Equipment Infuser Container using this slot.
     * @param slotIndex        THe index of this slot.
     * @param x                The x display position.
     * @param y                The y display position.
     */
    public EquipmentInfuserChargeSlot(EquipmentInfuserContainer infuserContainer, int slotIndex, int x, int y) {
        super(infuserContainer.equipmentInfuser, slotIndex, x, y);
        this.container = infuserContainer;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return itemStack.getItem() instanceof ChargeItem || itemStack.getItem() instanceof DyeItem || itemStack.getItem() == Items.WATER_BUCKET || itemStack.getItem() == Items.BUCKET;
    }

    /**
     * Returns true if this slot has an item stack.
     *
     * @return True if this slot has a valid item stack.
     */
    @Override
    public boolean hasItem() {
        return super.hasItem();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    /**
     * Called when an ItemStack is inserted into this slot.
     *
     * @param itemStack The ItemStack being inserted.
     */
    @Override
    public void set(ItemStack itemStack) {
        super.set(itemStack);
        this.container.attemptInfusion();
    }

    /**
     * Called when an ItemStack is removed into this slot.
     *
     * @param itemStack The ItemStack being inserted.
     */
    @Override
    public void onTake(Player player, ItemStack itemStack) {
        super.onTake(player, itemStack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }
}
