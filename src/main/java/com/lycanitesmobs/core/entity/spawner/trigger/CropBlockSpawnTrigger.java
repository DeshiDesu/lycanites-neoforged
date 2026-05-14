package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;

public class CropBlockSpawnTrigger extends BlockSpawnTrigger {

	/** Constructor **/
	public CropBlockSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);
	}


	@Override
	public boolean isTriggerBlock(BlockState blockState, Level world, BlockPos blockPos, int fortune, @Nullable LivingEntity entity) {
		Block block = blockState.getBlock();
		return block instanceof IPlantable || block instanceof VineBlock;
	}

	@Override
	public int getBlockLevel(BlockState blockState, Level world, BlockPos blockPos) {
		return 0;
	}
}
