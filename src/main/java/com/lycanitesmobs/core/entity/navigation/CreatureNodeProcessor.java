package com.lycanitesmobs.core.entity.navigation;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class CreatureNodeProcessor extends WalkNodeEvaluator implements ICreatureNodeProcessor {

    public BaseCreatureEntity entityCreature;

    public static double getGroundY(BlockGetter blockReader, BlockPos pos) {
        BlockPos blockpos = pos.below();
        VoxelShape voxelshape = blockReader.getBlockState(blockpos).getCollisionShape(blockReader, blockpos);
        return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.max(Direction.Axis.Y));
    }

    @Override
    public void prepare(PathNavigationRegion region, Mob mob) {
        this.updateEntitySize(mob);
        super.prepare(region, mob);
        if(mob instanceof BaseCreatureEntity) {
            this.entityCreature = (BaseCreatureEntity) mob;
        }
    }

    @Override
    public void updateEntitySize(Entity updateEntity) {
        this.entityWidth = Math.min(Mth.floor(this.getWidth(true, updateEntity) + 1.0F), 3);
        this.entityHeight = Math.min(Mth.floor(updateEntity.getDimensions(Pose.STANDING).height + 1.0F), 3);
        this.entityDepth = Math.min(Mth.floor(this.getWidth(true, updateEntity) + 1.0F), 3);
    }

    /** Returns the starting position to create a new path from. **/
    @Override
    public Node getStart() {

        // Flying/Strong Swimming:
        if(this.flying() || (this.entityCreature.isStrongSwimmer() && this.entityCreature.isUnderWater())) {
            return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5D), Mth.floor(this.mob.getBoundingBox().minZ));
        }

        return super.getStart();
    }

    /** Returns true if the entity is capable of pathing/moving in water at all. **/
    @Override
    public boolean canFloat() {
        if(this.entityCreature != null)
            return this.entityCreature.canWade() || this.entityCreature.isStrongSwimmer();
        return super.canFloat();
    }

    /** Returns a PathPoint to the given coordinates. **/
    @Override
    public Target getGoal(double x, double y, double z) {
        // Flying/Strong Swimming:
        if(this.flying() || this.swimming()) {
            return new Target(this.getNode(Mth.floor(x - this.getWidth(false)), Mth.floor(y + 0.5D), Mth.floor(z - this.getWidth(false))));
        }
        return super.getGoal(x, y, z);
    }

    /** Checks points around the provided fromPoint and adds it to path options if it is a valid point to travel to. **/
    @Override
    public int getNeighbors(Node[] pathOptions, Node fromPoint) {
        this.updateEntitySize(mob);

        // Flying/Strong Swimming/Diving:
        if(this.flying() || this.swimming()) {
            int i = 0;
            for (Direction direction : Direction.values()) {
                Node pathPoint = null;
                if(this.swimming()) {
                    pathPoint = this.getWaterNode(fromPoint.x + direction.getStepX(), fromPoint.y + direction.getStepY(), fromPoint.z + direction.getStepZ());
                }
                if(pathPoint == null) {
                    pathPoint = this.getFlightNode(fromPoint.x + direction.getStepX(), fromPoint.y + direction.getStepY(), fromPoint.z + direction.getStepZ());
                }
                if(pathPoint != null && !pathPoint.closed) {
                    pathOptions[i++] = pathPoint;
                }
            }
            return i;
        }

        return super.getNeighbors(pathOptions, fromPoint);
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int x, int y, int z, Mob entity) {
        if(this.swimming()) {
            return BlockPathTypes.WATER;
        }
        return super.getBlockPathType(blockGetter, x, y, z, entity);
    }
    /** Returns true if the entity should use swimming focused pathing. **/
    public boolean swimming() {
        if(this.entityCreature == null) {
            return false;
        }
        if(this.entityCreature.isInWater()) {
            return this.entityCreature.isStrongSwimmer() || (this.entityCreature.canWade() && this.entityCreature.shouldDive());
        }
        return false;
    }

    /** Returns true if the entity should use flight focused pathing. **/
    public boolean flying() {
        return this.entityCreature != null && this.entityCreature.isFlying() && !this.entityCreature.isUnderWater();
    }

    /**
     * Returns a width to path with.
     * @param blockChecks If true, this width is used for checking blocks, a reduced width can be returned for better performance here.
     * @return The entity width to path with.
     */
    public double getWidth(boolean blockChecks) {
		return this.getWidth(blockChecks, this.mob);
    }

    /**
     * Returns a width to path with.
     * @param blockChecks If true, this width is used for checking blocks, a reduced width can be returned for better performance here.
     * @param entity The entity to get the width of.
     * @return The entity width to path with.
     */
    public double getWidth(boolean blockChecks, Entity entity) {
		return Math.min(3, (double)entity.getDimensions(Pose.STANDING).width);
	}

    /** Flight Pathing **/
    protected Node getFlightNode(int x, int y, int z) {
        BlockPathTypes pathnodetype = this.isFlyablePathNode(x, y, z);
        if(this.entityCreature != null && this.entityCreature.isStrongSwimmer()) {
            if(pathnodetype == BlockPathTypes.WATER)
                return this.getNode(x, y, z);
        }
        return pathnodetype == BlockPathTypes.OPEN ? this.getNode(x, y, z) : null;
    }

    protected BlockPathTypes isFlyablePathNode(int x, int y, int z) {
        BlockPos centerPos = new BlockPos(x, y, z);
        for (int i = 0; i <= this.entityWidth; ++i) {
            for (int j = 0; j <= Math.min(this.entityHeight, 2); ++j) {
                for (int k = 0; k <= this.entityDepth; ++k) {
                    BlockState iblockstate = this.level.getBlockState(centerPos.offset(i, k, j));

                    // Non-Solid:
					if (!iblockstate.isSolid() && !(iblockstate.getBlock() instanceof LiquidBlock)) {
						return BlockPathTypes.OPEN;
					}

                    // Check For Open Air:
                    if (!iblockstate.isAir()) {
                        // If Can Swim Check For Swimmable Node:
                        if(this.entityCreature != null && this.entityCreature.isStrongSwimmer()) {
                            return this.isSwimmablePathNode(x, y, z);
                        }
                        return BlockPathTypes.BLOCKED;
                    }
                }
            }
        }

        return BlockPathTypes.OPEN;
    }

    /** Power Swim Pathing **/
    @Nullable
    protected Node getWaterNode(int x, int y, int z) {
        BlockPathTypes pathnodetype;
        if(this.entityCreature != null && this.entityCreature.isFlying()) {
            pathnodetype = this.isFlyablePathNode(x, y, z);
            if(pathnodetype == BlockPathTypes.OPEN)
                return this.getNode(x, y, z);
        }
        else {
            pathnodetype = this.isSwimmablePathNode(x, y, z);
        }
        return pathnodetype == BlockPathTypes.WATER ? this.getNode(x, y, z) : null;
    }

    protected BlockPathTypes isSwimmablePathNode(int x, int y, int z) {
        BlockPos centerPos = new BlockPos(x, y, z);
        for (int i = 0; i <= this.entityWidth; ++i) {
            for (int j = 0; j <= Math.min(this.entityHeight, 2); ++j) {
                for (int k = 0; k <= this.entityDepth; ++k) {
                    BlockPos blockPos = centerPos.offset(i, k, j);

                    // Block State Checks:
                    BlockState blockState = this.level.getBlockState(blockPos);

                    if(this.entityCreature == null || !blockState.isPathfindable(this.level, blockPos, PathComputationType.WATER)) {
                        if(j == y) { // Y must be water.
                            return BlockPathTypes.BLOCKED;
                        }
                        if(!blockState.isPathfindable(this.level, blockPos, PathComputationType.AIR) && !blockState.isPathfindable(this.level, blockPos, PathComputationType.WATER)) { // Blocked above water.
                            return BlockPathTypes.BLOCKED;
                        }
                    }

                    // Water Damages:
                    if (this.entityCreature.waterDamage() && blockState.getBlock() == Blocks.WATER) {
                        return BlockPathTypes.BLOCKED;
                    }

                    // Lava Damages:
                    if (this.entityCreature.canBurn() && blockState.getBlock() == Blocks.LAVA) {
                        return BlockPathTypes.BLOCKED;
                    }

                    // Ooze Swimming (With Water Damage):
                    if(!this.entityCreature.canFreeze() && ObjectManager.getBlock("ooze") != null && blockState.getBlock() == ObjectManager.getBlock("ooze")) {
                        return BlockPathTypes.WATER;
                    }

                    // Custom Fluid State Checks: - Added some direct checks to improve performance.
                    if (!blockState.getFluidState().isSource()) {
                        FluidState fluidState = this.level.getFluidState(blockPos);

                        // Water Damages:
                        if (this.entityCreature.waterDamage() && fluidState.is(FluidTags.WATER)) {
                            return BlockPathTypes.BLOCKED;
                        }

                        // Lava Damages:
                        if (this.entityCreature.canBurn() && fluidState.is(FluidTags.LAVA)) {
                            return BlockPathTypes.BLOCKED;
                        }
                    }
                }
            }
        }
        return BlockPathTypes.WATER;
    }
}
