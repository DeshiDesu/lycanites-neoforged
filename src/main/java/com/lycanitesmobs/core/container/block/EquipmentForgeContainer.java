package com.lycanitesmobs.core.container.block;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.container.slot.EquipmentForgeSlot;
import com.lycanitesmobs.core.container.base.BaseContainer;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.block.blockentity.TileEntityEquipmentForge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;

public class EquipmentForgeContainer extends BaseContainer {
    public static final MenuType<EquipmentForgeContainer> TYPE = IForgeMenuType.create(EquipmentForgeContainer::new);
    public TileEntityEquipmentForge equipmentForge;

    /**
     * Client Constructor
     *
     * @param windowId        The window id for the gui screen to use.
     * @param playerInventory The accessing player's inventory.
     * @param extraData       A packet sent from the server to create the Container from.
     */
    public EquipmentForgeContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, (TileEntityEquipmentForge) playerInventory.player.getCommandSenderWorld().getBlockEntity(BlockPos.of(extraData.readLong())));
    }

    /**
     * Main Constructor
     *
     * @param equipmentForge  The Equipment Forge Tile Entity.
     * @param playerInventory The Inventory of the accessing player.
     */
    public EquipmentForgeContainer(int windowId, Inventory playerInventory, TileEntityEquipmentForge equipmentForge) {
        super(TYPE, windowId);
        this.equipmentForge = equipmentForge;

        // Player Inventory
        this.addPlayerSlots(playerInventory, 0, 0);

        // Forge Inventory
        this.inventoryStart = this.slots.size();
        int slots = 0;
        if (equipmentForge.getContainerSize() > 0) {
            int slotSize = 18;
            int x = 8 + slotSize;
            int y = 38;

            // Crafted Piece:
            EquipmentForgeSlot equipmentSlotPiece = new EquipmentForgeSlot(this, slots++, x + (slotSize * 6), y, "piece");
            this.addSlot(equipmentSlotPiece);

            // Base:
            EquipmentForgeSlot equipmentSlotBase = new EquipmentForgeSlot(this, slots++, x + slotSize, y, "base");
            this.addSlot(equipmentSlotBase);

            // Head:
            EquipmentForgeSlot equipmentSlotHead = new EquipmentForgeSlot(this, slots++, x + (slotSize * 2), y, "none");
            this.addSlot(equipmentSlotHead);
            equipmentSlotBase.addChildSlot(equipmentSlotHead);

            // Tips:
            EquipmentForgeSlot equipmentSlotTipA = new EquipmentForgeSlot(this, slots++, x + (slotSize * 3), y, "none");
            this.addSlot(equipmentSlotTipA);
            equipmentSlotHead.addChildSlot(equipmentSlotTipA);

            EquipmentForgeSlot equipmentSlotTipB = new EquipmentForgeSlot(this, slots++, x + (slotSize * 2), y - slotSize, "none");
            this.addSlot(equipmentSlotTipB);
            equipmentSlotHead.addChildSlot(equipmentSlotTipB);

            EquipmentForgeSlot equipmentSlotTipC = new EquipmentForgeSlot(this, slots++, x + (slotSize * 2), y + slotSize, "none");
            this.addSlot(equipmentSlotTipC);
            equipmentSlotHead.addChildSlot(equipmentSlotTipC);

            // Pommel:
            EquipmentForgeSlot equipmentSlotPommel = new EquipmentForgeSlot(this, slots++, x, y, "none");
            this.addSlot(equipmentSlotPommel);
            equipmentSlotBase.addChildSlot(equipmentSlotPommel);
        }
        this.inventoryFinish = this.inventoryStart + slots;
    }


    @Override
    public boolean stillValid(Player player) {
        if (this.equipmentForge == null || !this.equipmentForge.stillValid(player)) {
            return false;
        }
        return true;
    }


    /**
     * Called when an equipment piece slot's contents is changed.
     *
     * @param equipmentSlot The equipment slot that changed. This must be a piece type slot.
     */
    public void onEquipmentPieceSlotChanged(EquipmentForgeSlot equipmentSlot) {
        if (this.equipmentForge == null) {
            return;
        }
        this.clearPartSlots();

        // Edit Equipment Piece:
        EquipmentForgeSlot slotBase = (EquipmentForgeSlot) this.getSlot(this.inventoryStart + 1);
        if (equipmentSlot.hasItem() && !slotBase.hasItem() && equipmentSlot.getItem().getItem() instanceof ItemEquipment) { // Only edit if not already creating a new piece.
            EquipmentForgeSlot slotHead = (EquipmentForgeSlot) this.getSlot(this.inventoryStart + 2);
            EquipmentForgeSlot slotTipA = (EquipmentForgeSlot) this.getSlot(this.inventoryStart + 3);
            EquipmentForgeSlot slotTipB = (EquipmentForgeSlot) this.getSlot(this.inventoryStart + 4);
            EquipmentForgeSlot slotTipC = (EquipmentForgeSlot) this.getSlot(this.inventoryStart + 5);
            EquipmentForgeSlot slotPommel = (EquipmentForgeSlot) this.getSlot(this.inventoryStart + 6);

            ItemEquipment itemEquipment = (ItemEquipment) equipmentSlot.getItem().getItem();
            int axeIndex = 0;
            for (ItemStack partStack : itemEquipment.getEquipmentPartStacks(equipmentSlot.getItem())) {
                ItemEquipmentPart itemEquipmentPart = itemEquipment.getEquipmentPart(partStack);
                if (itemEquipmentPart == null) {
                    continue;
                }
                if ("base".equals(itemEquipmentPart.slotType)) {
                    slotBase.putStackWithoutUpdate(partStack);
                } else if ("head".equals(itemEquipmentPart.slotType)) {
                    slotHead.putStackWithoutUpdate(partStack);
                } else if ("blade".equals(itemEquipmentPart.slotType) || "pike".equals(itemEquipmentPart.slotType) || "jewel".equals(itemEquipmentPart.slotType)) {
                    slotTipA.putStackWithoutUpdate(partStack);
                } else if ("axe".equals(itemEquipmentPart.slotType)) {
                    if (axeIndex++ == 0) {
                        slotTipB.putStackWithoutUpdate(partStack);
                    } else {
                        slotTipC.putStackWithoutUpdate(partStack);
                    }
                } else if ("pommel".equals(itemEquipmentPart.slotType)) {
                    slotPommel.putStackWithoutUpdate(partStack);
                }
            }
        }
    }


    /**
     * Called when an equipment part slot's contents is changed.
     *
     * @param equipmentSlot The equipment slot that changed. This must be any type of part slot.
     */
    public void onEquipmentPartSlotChanged(EquipmentForgeSlot equipmentSlot) {
        if (this.equipmentForge == null) {
            return;
        }

        // Slots:
        Slot slotPiece = this.getSlot(this.inventoryStart);
        Slot slotBase = this.getSlot(this.inventoryStart + 1);
        Slot slotHead = this.getSlot(this.inventoryStart + 2);
        Slot slotTipA = this.getSlot(this.inventoryStart + 3);
        Slot slotTipB = this.getSlot(this.inventoryStart + 4);
        Slot slotTipC = this.getSlot(this.inventoryStart + 5);
        Slot slotPommel = this.getSlot(this.inventoryStart + 6);

        // Create Equipment Piece:
        ItemEquipment itemEquipment = (ItemEquipment) ObjectManager.getItem("equipment");
        ItemStack pieceStack = new ItemStack(itemEquipment);

        // Add Parts:
        if (slotBase.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotBase.getItem(), 0);
        }
        if (slotHead.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotHead.getItem(), 1);
        }
        if (slotTipA.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotTipA.getItem(), 2);
        }
        if (slotTipB.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotTipB.getItem(), 3);
        }
        if (slotTipC.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotTipC.getItem(), 4);
        }
        if (slotPommel.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotPommel.getItem(), 5);
        }

        // Put Piece Stack:
        if (itemEquipment.getEquipmentPartCount(pieceStack) > 0) {
            slotPiece.set(pieceStack);
        } else {
            slotPiece.set(ItemStack.EMPTY);
        }
    }


    /**
     * Clears all slots except the piece slot.
     */
    public void clearPartSlots() {
        for (int i = 1; i <= 6; i++) {
            Slot slot = this.getSlot(this.inventoryStart + i);
            if (slot instanceof EquipmentForgeSlot) {
                EquipmentForgeSlot equipmentSlot = (EquipmentForgeSlot) slot;
                equipmentSlot.putStackWithoutUpdate(ItemStack.EMPTY);
            }
        }
    }


    /**
     * Returns true if all required parts have been added to make a valid piece of equipment.
     *
     * @return True if the equipment is valid.
     */
    public boolean isEquipmentValid() {
        Slot slotBase = this.getSlot(this.inventoryStart + 1);
        Slot slotHead = this.getSlot(this.inventoryStart + 2);
        Slot slotTipA = this.getSlot(this.inventoryStart + 3);
        Slot slotTipB = this.getSlot(this.inventoryStart + 4);
        Slot slotTipC = this.getSlot(this.inventoryStart + 5);

        if (!slotBase.hasItem() || !slotHead.hasItem()) {
            return false;
        }

        return slotTipA.hasItem() || slotTipB.hasItem() || slotTipC.hasItem();
    }


    @Override
    public ItemStack quickMoveStack(Player player, int slotID) {
        Slot slot = this.slots.get(slotID);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getItem();
        ItemStack original = slotStack.copy();

        if (this.isPlayerSlot(slotID)) {
            boolean moved = false;
            for (int i = this.inventoryStart; i < this.inventoryFinish; i++) {
                Slot targetSlot = this.slots.get(i);
                if (targetSlot.mayPlace(slotStack) && !targetSlot.hasItem()) {
                    targetSlot.set(slotStack.copy());
                    slot.set(ItemStack.EMPTY);
                    moved = true;
                    break;
                }
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!slot.mayPickup(player)) {
                return ItemStack.EMPTY;
            }
            if (this.playerInventoryFinish >= 0) {
                if (!this.moveItemStackTo(slotStack, this.playerInventoryStart, this.playerInventoryFinish + 1, true)) {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            slot.onTake(player, original);
        }

        return original;
    }
}
