package com.lycanitesmobs.core.data.info.block;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class BlockReference {
	protected final Level world;
	protected final BlockPos pos;

	public BlockReference(Level world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}

	public Level getWorld() {
		return this.world;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public BlockState getState() {
		return this.getWorld().getBlockState(this.getPos());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlockReference)) return false;
		BlockReference that = (BlockReference) o;
		return world.equals(that.world) && pos.equals(that.pos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(world, pos);
	}
}
