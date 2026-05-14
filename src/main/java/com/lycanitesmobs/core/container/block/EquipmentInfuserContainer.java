package com.lycanitesmobs.core.container.block;

import com.lycanitesmobs.core.container.base.BaseContainer;
import com.lycanitesmobs.core.container.slot.EquipmentInfuserChargeSlot;
import com.lycanitesmobs.core.container.slot.EquipmentInfuserPartSlot;
import com.lycanitesmobs.core.item.consumable.entity.ChargeItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.block.blockentity.EquipmentInfuserTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.extensions.IForgeMenuType;

public class EquipmentInfuserContainer extends BaseContainer {
    public static final MenuType<EquipmentInfuserContainer> TYPE = IForgeMenuType.create(EquipmentInfuserContainer::new);

    public EquipmentInfuserTileEntity equipmentInfuser;
    EquipmentInfuserPartSlot partSlot;
    EquipmentInfuserChargeSlot chargeSlot;

    /**
     * Client Constructor
     *
     * @param windowId        The window id for the gui screen to use.
     * @param playerInventory The accessing player's inventory.
     * @param extraData       A packet sent from the server to create the Container from.
     */
    public EquipmentInfuserContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, (EquipmentInfuserTileEntity) playerInventory.player.getCommandSenderWorld().getBlockEntity(BlockPos.of(extraData.readLong())));
    }

    /**
     * Main Constructor
     *
     * @param equipmentInfuser The Equipment Forge Tile Entity.
     * @param playerInventory  The Inventory of the accessing player.
     */
    public EquipmentInfuserContainer(int windowId, Inventory playerInventory, EquipmentInfuserTileEntity equipmentInfuser) {
        super(TYPE, windowId);
        this.equipmentInfuser = equipmentInfuser;

        // Player Inventory
        this.addPlayerSlots(playerInventory, 0, 0);

        // Forge Inventory
        this.inventoryStart = this.slots.size();
        int slots = 0;
        if (equipmentInfuser.getContainerSize() > 0) {
            int y = 28;

            this.chargeSlot = new EquipmentInfuserChargeSlot(this, slots++, 50, y);
            this.addSlot(this.chargeSlot);

            this.partSlot = new EquipmentInfuserPartSlot(this, slots++, 110, y);
            this.addSlot(this.partSlot);
        }
        this.inventoryFinish = this.inventoryStart + slots;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.equipmentInfuser == null || !this.equipmentInfuser.stillValid(player)) {
            return false;
        }
        return true;
    }

    /**
     * Called by either the Charge slot or the Part slot, performs infusion if possible.
     */
    public void attemptInfusion() {
        // Equipment Part:
        if (this.partSlot.getItem().getItem() instanceof ItemEquipmentPart) {
            ItemEquipmentPart equipmentPart = (ItemEquipmentPart) this.partSlot.getItem().getItem();

            // Charge Experience:
            if (this.chargeSlot.getItem().getItem() instanceof ChargeItem) {
                if (equipmentPart.getPartLevel(this.partSlot.getItem()) >= equipmentPart.levelMax) {
                    return;
                }
                ChargeItem chargeItem = (ChargeItem) this.chargeSlot.getItem().getItem();
                if (equipmentPart.isLevelingChargeItem(this.chargeSlot.getItem())) {
                    int experienceGained = equipmentPart.getExperienceFromChargeItem(this.chargeSlot.getItem());
                    equipmentPart.addExperience(this.partSlot.getItem(), experienceGained);
                    this.chargeSlot.remove(1);
                    this.attemptInfusion();
                }
                return;
            }

            // Dye Part:
            if (this.chargeSlot.getItem().getItem() instanceof DyeItem) {
                DyeItem dyeItem = (DyeItem) this.chargeSlot.getItem().getItem();
                DyeColor dyeColor = dyeItem.getDyeColor();
                equipmentPart.setColor(this.partSlot.getItem(), dyeColor.getTextureDiffuseColors()[0], dyeColor.getTextureDiffuseColors()[1], dyeColor.getTextureDiffuseColors()[2]);
                this.chargeSlot.remove(1);
                return;
            }

            // Remove Part Dye:
            if (this.chargeSlot.getItem().getItem() == Items.WATER_BUCKET) {
                equipmentPart.setColor(this.partSlot.getItem(), 1, 1, 1);
                this.chargeSlot.set(new ItemStack(Items.BUCKET));
                return;
            }
        }

        // Experience Bottle:
        if (this.partSlot.getItem().getItem() == Items.GLASS_BOTTLE) {
            if (this.chargeSlot.getItem().getItem() instanceof ChargeItem) {
                this.partSlot.set(new ItemStack(Items.EXPERIENCE_BOTTLE));
                this.chargeSlot.remove(1);
            }
        }
    }

    /**
     * Disabled until fixed later.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int slotID) {
        return ItemStack.EMPTY;
    }
}
