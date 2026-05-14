package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class OreBlockSpawnTrigger extends BlockSpawnTrigger {

	/** If true, ores (ore blocks that drop as blocks as well as coal, nether gold ore and monster egg) will trigger. **/
	public boolean ores = true;

	/** If true, gems (ore blocks that drop as items excluding coal, nether gold ore and monster egg) will trigger. **/
	public boolean gems = false;

	/** Constructor **/
	public OreBlockSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);

		if(json.has("ores"))
			this.ores = json.get("ores").getAsBoolean();

		if(json.has("gems"))
			this.gems = json.get("gems").getAsBoolean();
	}


	@Override
	public boolean isTriggerBlock(BlockState blockState, Level world, BlockPos blockPos, int fortune, @Nullable LivingEntity entity) {
		Block block = blockState.getBlock();

		if(block instanceof InfestedBlock) {
			return this.ores;
		}
		if(block == Blocks.COAL_ORE || block == Blocks.NETHER_GOLD_ORE) {
			return this.ores;
		}

		if(LMHelperClass.convertToResourceLocation(block, world.registryAccess()) == null) {
			return false;
		}
		String blockName = LMHelperClass.convertToResourceLocation(block, world.registryAccess()).getPath();
		String[] blockNameParts = blockName.split("\\.");
		for(String blockNamePart : blockNameParts) {
			int blockNamePartLength = blockNamePart.length();

			// Check if start or end of block name part is "ore" or "crystal".
			boolean nameMatch = false;
			if (blockNamePartLength >= 3) {
				if (blockNamePart.substring(0, 3).equalsIgnoreCase("ore") || blockNamePart.substring(blockNamePartLength - 3, blockNamePartLength).equalsIgnoreCase("ore")) {
					nameMatch = true;
				}
			}
			if (!nameMatch && blockNamePartLength >= 7) {
				if ( blockNamePart.substring(0, 7).equalsIgnoreCase("crystal") || blockNamePart.substring(blockNamePartLength - 7, blockNamePartLength).equalsIgnoreCase("crystal")) {
					nameMatch = true;
				}
			}

			if(nameMatch) {
				if(this.ores && this.gems) {
					return true;
				}

				if (blockName.contains("coal")) {
					return this.ores;
				}

				if(world instanceof ServerLevel) {
					List<ItemStack> drops;
					if(entity == null) {
						drops = block.getDrops(blockState, (ServerLevel)world, blockPos, null);
					}
					else {
						drops = block.getDrops(blockState, (ServerLevel)world, blockPos, null, entity, entity.getUseItem());
					}
					for(ItemStack dropStack : drops) {
						if(dropStack.getItem() instanceof BlockItem) {
							return this.ores;
						}
						else {
							return this.gems;
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public int getBlockLevel(BlockState blockState, Level world, BlockPos blockPos) {
		Block block = blockState.getBlock();
		if(block == Blocks.DIAMOND_ORE)
			return 3;
		if(block == Blocks.EMERALD_ORE)
			return 3;
		if(block == Blocks.LAPIS_ORE)
			return 2;
		if(block == Blocks.GOLD_ORE)
			return 2;
		if(block == Blocks.IRON_ORE)
			return 1;
		return 0;
	}
}
