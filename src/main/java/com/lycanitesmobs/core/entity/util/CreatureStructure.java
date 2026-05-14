package com.lycanitesmobs.core.entity.util;

import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonTheme;
import com.lycanitesmobs.core.worldgen.dungeon.definition.ThemeBlock;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.ContextUtils;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatureStructure {
	protected BaseCreatureEntity owner;
	protected DungeonTheme dungeonTheme;
	protected Map<Integer, List<ContextUtils.CreatureBuildTask>> buildTasks = new HashMap<>();
	protected BlockPos origin;

	protected BlockPos startPos;

	public CreatureStructure(BaseCreatureEntity creatureEntity, DungeonTheme dungeonTheme) {
		this.owner = creatureEntity;
		this.dungeonTheme = dungeonTheme;
		this.origin = creatureEntity.blockPosition();
	}

	/**
	 * Gets the origin position of this structure.
	 * @return blockPos The position that acts as the structure origin.
	 */
	public BlockPos getOrigin() {
		return this.origin;
	}

	/**
	 * Sets the origin position of this structure.
	 * @param blockPos The position to act as the structure origin.
	 */
	public void setOrigin(BlockPos blockPos) {
		this.origin = blockPos;
		this.startPos = blockPos.offset(0, 8, 0);
	}

	/**
	 * Updates structure maintenance, checking for any parts that need to be build, etc.
	 */
	public void refreshBuildTasks() {
		this.buildTasks.clear();
		int radius = 8;

		// Check if started:
		if (!this.isPhaseComplete(0)) {
			this.createBuildTask(
				this.dungeonTheme.getCeiling(null, '1', this.owner.getRandom()),
				this.startPos,
				0
			);
			return;
		}

		// A simple box for now. TODO Try to use Dungeon Sector?
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {

					// Skip Corners:
					if (Math.abs(x) == radius && Math.abs(z) == radius) {
						continue;
					}

					BlockPos blockPos = this.origin.offset(x, y, z);

					// Check Placement:
					if (!this.shouldBuildAt(blockPos)) {
						continue;
					}

					BlockState blockState = null;

					// Entrance Spaces:
					if (Math.abs(y) <= 1 && (Math.abs(x) <= 1 || Math.abs(z) <= 1)) {
						blockState = Blocks.AIR.defaultBlockState();
					}

					// Inside Air:
					else if (Math.abs(x) < radius && Math.abs(y) < radius && Math.abs(z) < radius) {
						if (Math.abs(x) < (radius / 4) + 1 && y < -(radius / 2) + 1 && Math.abs(z) < (radius / 4) + 1) {
							continue;
						}
						BlockState targetState = this.owner.getCommandSenderWorld().getBlockState(blockPos);
						if (targetState.getBlock() != Blocks.AIR && !(targetState.getBlock() instanceof IFluidBlock)) {
							blockState = Blocks.AIR.defaultBlockState();
						}
					}

					// Ceiling:
					else if (y == radius) {
						blockState = this.dungeonTheme.getCeiling(null, '1', this.owner.getRandom());
					}

					// Floor:
					else if (y == -radius) {
						blockState = this.dungeonTheme.getFloor(null, '1', this.owner.getRandom());
					}

					// Wall:
					else {
						blockState = this.dungeonTheme.getWall(null, '1', this.owner.getRandom());
					}

					// Create Build Task If Different:
					if (blockState != null && this.owner.getCommandSenderWorld().getBlockState(blockPos).getBlock() != blockState.getBlock()) {
						this.createBuildTask(blockState, blockPos, 1);
					}
				}
			}
		}

		// First Pit Blocks:
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					if (Math.abs(x) < (radius / 4) + 1 && Math.abs(z) < (radius / 4) + 1) {
						continue;
					}

					BlockPos blockPos = this.origin.offset(x, y, z);

					// Check Placement:
					if (!this.shouldBuildAt(blockPos)) {
						continue;
					}

					for (int pitLayer = 1; pitLayer <= (radius / 2); pitLayer++) {
						if (y == radius - pitLayer && Math.abs(x) < (radius - pitLayer) && Math.abs(z) < (radius - pitLayer)) {
							BlockState blockState = this.dungeonTheme.getPit('1', this.owner.getRandom());
							if (this.owner.getCommandSenderWorld().getBlockState(blockPos).getBlock() != blockState.getBlock()) {
								this.createBuildTask(blockState, blockPos, 2);
							}
						}
					}
					for (int pitLayer = 1; pitLayer <= (radius / 2); pitLayer++) {
						if (y == -radius + pitLayer && Math.abs(x) < (radius - pitLayer) && Math.abs(z) < (radius - pitLayer)) {
							BlockState blockState = this.dungeonTheme.getPit('1', this.owner.getRandom());
							if (this.owner.getCommandSenderWorld().getBlockState(blockPos).getBlock() != blockState.getBlock()) {
								this.createBuildTask(blockState, blockPos, 2);
							}
						}
					}
				}
			}
		}

		// Second Pit Blocks:
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					if (Math.abs(x) < (radius / 4) + 1 && y <= -(radius / 2) && y > -radius && Math.abs(z) < (radius / 4) + 1) {
						BlockPos blockPos = this.origin.offset(x, y, z);

						// Check Placement:
						if (!this.shouldBuildAt(blockPos)) {
							continue;
						}

						BlockState blockState = this.dungeonTheme.getPit('2', this.owner.getRandom());
						if (this.owner.getCommandSenderWorld().getBlockState(blockPos).getBlock() != blockState.getBlock()) {
							this.createBuildTask(blockState, blockPos, 3);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates and adds a new ContextUtils.CreatureBuildTask to this CreatureStructure.
	 * Note that build tasks are constantly emptied when the structure is rescanned.
	 * Existing tasks using the same position will also be removed.
	 * @param blockState The block state to place.
	 * @param blockPos The position to place at.
	 */
	public void createBuildTask(BlockState blockState, BlockPos blockPos, int phase) {
		if (!this.buildTasks.containsKey(phase)) {
			this.buildTasks.put(phase, new ArrayList<>());
		}

		// Remove Existing Tasks Using Same Block Position:
		for (int checkPhase = 0; checkPhase < this.buildTasks.size(); checkPhase++) {
			if (!this.buildTasks.containsKey(checkPhase)) {
				continue;
			}
			List<ContextUtils.CreatureBuildTask> removeTasks = new ArrayList<>();
			for (ContextUtils.CreatureBuildTask creatureBuildTask : this.buildTasks.get(checkPhase)) {
				if (creatureBuildTask.pos.equals(blockPos)) {
					removeTasks.add(creatureBuildTask);
				}
			}
			for (ContextUtils.CreatureBuildTask creatureBuildTask : removeTasks) {
				this.buildTasks.get(checkPhase).remove(creatureBuildTask);
			}
		}

		this.buildTasks.get(phase).add(new ContextUtils.CreatureBuildTask(blockState, blockPos, phase));
	}

	/**
	 * Checks if the provided position is valid for building, checks for existing structure blocks.
	 * @param pos The position to check.
	 * @return True if a Build Task should be created for the position, false if not.
	 */
	protected boolean shouldBuildAt(BlockPos pos) {
		BlockState targetState = this.owner.getCommandSenderWorld().getBlockState(pos);
		Block targetBlock = targetState.getBlock();
		if (targetBlock == Blocks.AIR) {
			return true;
		}
		if (this.owner.getCommandSenderWorld().getBlockEntity(pos) != null) {
			return false;
		}
		if (Material.WATER.contains(targetBlock) || Material.LAVA.contains(targetBlock)) {
			return true;
		}
		if (LMHelperClass.Materials.isPlant(targetBlock)
				|| Material.DIRT.contains(targetBlock)
				|| Material.GRASS.contains(targetBlock)
				|| Material.LEAVES.contains(targetBlock)
				|| LMHelperClass.Materials.isCrop(targetBlock)
				|| LMHelperClass.Materials.isReplaceablePlant(targetBlock)
				|| Material.WOOD.contains(targetBlock)
		) {
			return true;
		}
		return false;
	}

	@Nullable
	public ContextUtils.CreatureBuildTask getBuildTask(LivingEntity builder) {
		if (this.buildTasks.isEmpty()) {
			return null;
		}
		for (int phase = 0; phase < this.buildTasks.size(); phase++) {
			if (!this.buildTasks.containsKey(phase) || this.buildTasks.get(phase).isEmpty()) {
				continue;
			}
			ContextUtils.CreatureBuildTask buildTask = null;
			if (buildTasks.get(phase).size() == 1) {
				buildTask = buildTasks.get(phase).get(0);
			}
			else {
				buildTask = this.buildTasks.get(phase).get(builder.getRandom().nextInt(buildTasks.get(phase).size()));
			}
			return buildTask;
		}
		return null;
	}

	/**
	 * Completes the provided build task, removing it from the build tasks list.
	 * @param buildTask The build task to complete.
	 */
	public void completeBuildTask(ContextUtils.CreatureBuildTask buildTask) {
		for (int phase = 0; phase < this.buildTasks.size(); phase++) {
			if (!this.buildTasks.containsKey(phase) || this.buildTasks.get(phase).isEmpty()) {
				continue;
			}
			this.buildTasks.get(phase).remove(buildTask);
		}
	}

	/**
	 * Performs a check to see if the provided structure build phase is complete.
	 * @param phase The phase to check.
	 * @return True if in a hive, false if not.
	 */
	public boolean isPhaseComplete(int phase) {
		return this.getBuildTaskSize(phase) == 0;
	}

	/**
	 * Returns how many Build Tasks that are left to finish building the structure.
	 * @param phase The build task phase.
	 * @return How many tasks are left.
	 */
	public int getBuildTaskSize(int phase) {
		if (!this.buildTasks.containsKey(phase)) {
			return 0;
		}
		return this.buildTasks.get(phase).size();
	}

	/**
	 * Returns how many Build Tasks that are left to finish building the structure for the final phase.
	 * @return How many tasks are left.
	 */
	public int getFinalPhaseBuildTaskSize() {
		return this.getBuildTaskSize(this.buildTasks.size() -1);
	}

	/**
	 * Checks if the block at the provided blockpos is a block belonging to this structure.
	 * @param blockPos The position to check.
	 * @return True if the block is part of this structure's theme.
	 */
	protected boolean isStructureBlock(BlockPos blockPos) {
		BlockState targetState = this.owner.getCommandSenderWorld().getBlockState(blockPos);
		Block targetBlock = targetState.getBlock();
		if (targetBlock == Blocks.AIR) {
			return false;
		}
		for (ThemeBlock themeBlock : this.dungeonTheme.ceilingBlocks) {
			if (targetBlock == themeBlock.getBlock()) {
				return true;
			}
		}
		for (ThemeBlock themeBlock : this.dungeonTheme.floorBlocks) {
			if (targetBlock == themeBlock.getBlock()) {
				return true;
			}
		}
		for (ThemeBlock themeBlock : this.dungeonTheme.wallBlocks) {
			if (targetBlock == themeBlock.getBlock()) {
				return true;
			}
		}
		return false;
	}
}
