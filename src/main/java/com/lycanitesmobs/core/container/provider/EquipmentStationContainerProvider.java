package com.lycanitesmobs.core.container.provider;

import com.lycanitesmobs.core.block.blockentity.EquipmentStationTileEntity;
import com.lycanitesmobs.core.container.block.EquipmentStationContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EquipmentStationContainerProvider implements Nameable, MenuProvider {
    public EquipmentStationTileEntity equipmentStation;

    public EquipmentStationContainerProvider(@Nonnull EquipmentStationTileEntity equipmentStation) {
        this.equipmentStation = equipmentStation;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player Player) {
        return new EquipmentStationContainer(windowId, playerInventory, this.equipmentStation);
    }

    @Override
    public Component getName() {
        return getDisplayName();
    }

    @Override
    public MutableComponent getDisplayName() {
        return this.equipmentStation.getName();
    }
}
