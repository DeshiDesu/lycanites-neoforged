package com.lycanitesmobs.core.container.provider;

import com.lycanitesmobs.core.container.creature.CreatureContainer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CreatureContainerProvider implements Nameable, MenuProvider {
	public BaseCreatureEntity creature;

	public CreatureContainerProvider(@Nonnull BaseCreatureEntity creature) {
		this.creature = creature;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player Player) {
		return new CreatureContainer(windowId, playerInventory, this.creature);
	}

	@Override
	public Component getName() {
		return getDisplayName();
	}

	@Override
	public MutableComponent getDisplayName() {
		return this.creature.getDisplayName().copy();
	}
}
