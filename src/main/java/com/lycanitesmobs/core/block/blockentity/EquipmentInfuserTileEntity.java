package com.lycanitesmobs.core.block.blockentity;

import com.lycanitesmobs.core.entity.item.CustomItemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class EquipmentInfuserTileEntity extends TileEntityBase {
    /**
     * A list of item stacks in the infuser.
     **/
    protected NonNullList<ItemStack> itemStacks = NonNullList.withSize(2, ItemStack.EMPTY);

    /**
     * Constructor
     *
     * @param pos
     * @param state
     */
    public EquipmentInfuserTileEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public Component getName() {
        return Component.translatable("block.lycanitesmobs.equipment_infuser");
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

	/*@Override
	public void tick() {
		super.tick();
	}*/

	/*@Override
	public BlockPos getPos() {
		return this.getBlockPos();
	}*/

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.itemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getItem(int index) {
        return this.itemStacks.get(index);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(this.itemStacks, index, count);
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.itemStacks, index);
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setItem(int index, ItemStack stack) {
        this.itemStacks.set(index, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return super.stillValid(p_18946_);
    }

    @Override
    public void startOpen(Player player) {

    }

    @Override
    public void stopOpen(Player player) {

    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
     * guis use Slot.isItemValid
     */
    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        ItemStack existingStack = this.getItem(index);
        return existingStack.isEmpty();
    }

    @Override
    public void clearContent() {
        this.itemStacks.clear();
    }

    @Override
    public boolean triggerEvent(int eventID, int eventArg) {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        CompoundTag syncData = new CompoundTag();
        return ClientboundBlockEntityDataPacket.create(this, be -> syncData);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        super.onDataPacket(net, packet);
    }

    @Override
    public void onGuiButton(int buttonId) {

    }

    @Override
    public void load(CompoundTag nbtTagCompound) {
        super.load(nbtTagCompound);
        if (nbtTagCompound.contains("Items")) {
            ContainerHelper.loadAllItems(nbtTagCompound, this.itemStacks);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbtTagCompound) {
        ContainerHelper.saveAllItems(nbtTagCompound, this.itemStacks);
        super.saveAdditional(nbtTagCompound);
    }
}
