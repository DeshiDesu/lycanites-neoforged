package com.lycanitesmobs.core.capabilities.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderPlayer implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
	public static Capability<ExtendedPlayer> EXTENDED_PLAYER = CapabilityManager.get(new CapabilityToken<ExtendedPlayer>() {});

	private ExtendedPlayer instance = null;
	private final LazyOptional<ExtendedPlayer> optional= LazyOptional.of(this::createInstance);;


	public ExtendedPlayer createInstance() {
		if (instance == null) {
			this.instance = new ExtendedPlayer();
		}
		return this.instance;
	}
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		return capability == EXTENDED_PLAYER ? optional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		createInstance().writeNBT(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		createInstance().readNBT(nbt);
	}
}
