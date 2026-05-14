package com.lycanitesmobs.core.block.blockentity;

import com.lycanitesmobs.core.entity.item.CustomItemEntity;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityEquipmentForge extends TileEntityBase {
    /**
     * A list of item stacks in the forge.
     **/
    protected NonNullList<ItemStack> itemStacks = NonNullList.withSize(ItemEquipment.PART_LIMIT + 1, ItemStack.EMPTY);

    /**
     * The level of the forge.
     **/
    protected int forgeLevel = 1;

    /**
     * Constructor
     *
     * @param pos
     * @param state
     */
    public TileEntityEquipmentForge(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public void tick() {
        super.tick();
    }


    // ========================================
    //                Inventory
    // ========================================
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
        if (!(itemStack.getItem() instanceof ItemEquipment) && !(itemStack.getItem() instanceof ItemEquipmentPart)) {
            return false;
        }

        ItemStack existingStack = this.getItem(index);
        return existingStack.isEmpty();
    }

    @Override
    public void clearContent() {
        this.itemStacks.clear();
    }


    // ========================================
    //              Client Events
    // ========================================
    @Override
    public boolean triggerEvent(int eventID, int eventArg) {
        return false;
    }


    // ========================================
    //             Network Packets
    // ========================================


    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        CompoundTag syncData = new CompoundTag();

        // Server to Client:
        if (!this.getLevel().isClientSide) {
            syncData.putInt("ForgeLevel", this.forgeLevel);
        }

        return ClientboundBlockEntityDataPacket.create(this, be -> syncData);
    }


    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        if (!this.getLevel().isClientSide)
            return;

        CompoundTag syncData = packet.getTag();
        if (syncData.contains("ForgeLevel"))
            this.forgeLevel = syncData.getInt("ForgeLevel");
    }

    @Override
    public void onGuiButton(int buttonId) {
        LMHelperClass.logDebug("", "Received button packet id: " + buttonId);
    }


    // ========================================
    //                 NBT Data
    // ========================================

    @Override
    public void load(CompoundTag nbtTagCompound) {
        super.load(nbtTagCompound);
        if (nbtTagCompound.contains("ForgeLevel")) {
            this.forgeLevel = nbtTagCompound.getInt("ForgeLevel");
        }
        if (nbtTagCompound.contains("Items")) {
            ContainerHelper.loadAllItems(nbtTagCompound, this.itemStacks);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbtTagCompound) {
        nbtTagCompound.putInt("ForgeLevel", this.forgeLevel);
        ContainerHelper.saveAllItems(nbtTagCompound, this.itemStacks);
        super.saveAdditional(nbtTagCompound);
    }


    // ========================================
    //              Equipment Forge
    // ========================================

    /**
     * Returns the level of this Equipment Forge.
     */
    public int getForgeLevel() {
        return this.forgeLevel;
    }

    /**
     * Sets the level of this Equipment Forge.
     *
     * @param level The level to set the forge to. Higher levels allow for working with higher level Equipment Parts.
     */
    public void setLevel(int level) {
        this.forgeLevel = level;
    }

    /**
     * Gets the name of this Equipment Forge.
     */
    public MutableComponent getName() {
        String levelName = "lesser";
        if (this.forgeLevel == 2) {
            levelName = "greater";
        } else if (this.forgeLevel >= 3) {
            levelName = "master";
        }
        return Component.translatable("block.lycanitesmobs.equipmentforge_" + levelName);
    }
}
