package com.lycanitesmobs.core.container.provider;

import com.lycanitesmobs.core.block.blockentity.EquipmentInfuserTileEntity;
import com.lycanitesmobs.core.container.block.EquipmentInfuserContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EquipmentInfuserContainerProvider implements Nameable, MenuProvider {
    public EquipmentInfuserTileEntity equipmentInfuser;

    public EquipmentInfuserContainerProvider(@Nonnull EquipmentInfuserTileEntity equipmentInfuser) {
        this.equipmentInfuser = equipmentInfuser;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player Player) {
        return new EquipmentInfuserContainer(windowId, playerInventory, this.equipmentInfuser);
    }

    @Override
    public Component getName() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return this.equipmentInfuser.getName();
    }
}
