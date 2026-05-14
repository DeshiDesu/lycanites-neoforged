package com.lycanitesmobs.core.capabilities.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderEntity implements ICapabilitySerializable<CompoundTag> {
	public static Capability<ExtendedEntity> EXTENDED_ENTITY = CapabilityManager.get(new CapabilityToken<>() {
    });
	private ExtendedEntity instance = null;
	private final LazyOptional<ExtendedEntity> optional = LazyOptional.of(this::createInstance);

	public ExtendedEntity createInstance() {
			if (this.instance == null) {
				this.instance = new ExtendedEntity();
			}
			return this.instance;
	}
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		return capability == EXTENDED_ENTITY ? optional.cast() : LazyOptional.empty();
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
