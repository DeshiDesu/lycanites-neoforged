package com.lycanitesmobs.core.container.provider;

import com.lycanitesmobs.core.block.blockentity.TileEntityEquipmentForge;
import com.lycanitesmobs.core.container.block.EquipmentForgeContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EquipmentForgeContainerProvider implements Nameable, MenuProvider {
    public TileEntityEquipmentForge equipmentForge;

    public EquipmentForgeContainerProvider(@Nonnull TileEntityEquipmentForge equipmentForge) {
        this.equipmentForge = equipmentForge;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new EquipmentForgeContainer(windowId, playerInventory, this.equipmentForge);
    }

    @Override
    public Component getName() {
        return getDisplayName();
    }

    @Override
    public MutableComponent getDisplayName() {
        return this.equipmentForge.getName();
    }
}
