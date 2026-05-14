package com.lycanitesmobs.core.block.base;


import com.google.common.collect.ImmutableMap;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.manager.ItemManager;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockFireBase extends BlockBase {
    public static final BooleanProperty PERMANENT = BooleanProperty.create("permanent");
    public static final BooleanProperty STATIC = BooleanProperty.create("static");
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    private static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_199776_0_) -> p_199776_0_.getKey() != Direction.DOWN).collect(Util.toMap());
    private final Map<BlockState, VoxelShape> shapesCache;
    public boolean dieInRain = true;
    public boolean triggerTNT = true;
    public int agingRate = 3;
    public float spreadChance = 1;
    public boolean removeOnNoFireTick;
    private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public BlockFireBase(Block.Properties properties, String name) {
        super(properties, name);

        this.removeOnTick = false;
        this.removeOnNoFireTick = false;
        this.loopTicks = true;
        this.canBeCrushed = true;

        this.noBreakCollision = false;

        this.tickRate = 30; // Default tick rate, configs can set this to 1 to remove this fire block from worlds.

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(AGE, 0)
                .setValue(PERMANENT, false)
                .setValue(STATIC, false)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false));
        this.shapesCache = ImmutableMap.copyOf(this.stateDefinition.getPossibleStates().stream().filter((p_53497_) -> {
            return p_53497_.getValue(AGE) == 0;
        }).collect(Collectors.toMap(Function.identity(), BlockFireBase::calculateShape)));
        ItemManager.getInstance().cutoutBlocks.add(this);
    }

    private static VoxelShape calculateShape(BlockState p_53491_) {
        VoxelShape voxelshape = Shapes.empty();
        if (p_53491_.getValue(UP)) {
            voxelshape = UP_AABB;
        }

        if (p_53491_.getValue(NORTH)) {
            voxelshape = Shapes.or(voxelshape, NORTH_AABB);
        }

        if (p_53491_.getValue(SOUTH)) {
            voxelshape = Shapes.or(voxelshape, SOUTH_AABB);
        }

        if (p_53491_.getValue(EAST)) {
            voxelshape = Shapes.or(voxelshape, EAST_AABB);
        }

        if (p_53491_.getValue(WEST)) {
            voxelshape = Shapes.or(voxelshape, WEST_AABB);
        }

        return voxelshape.isEmpty() ? DOWN_AABB : voxelshape;
    }


    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return this.shapesCache.get(pState.setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, PERMANENT, STATIC, NORTH, EAST, SOUTH, WEST, UP);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
    }

    public BlockState getStateForPlacement(BlockGetter world, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = world.getBlockState(belowPos);

        boolean hasSupportBelow = belowState.isFaceSturdy(world, belowPos, Direction.UP);
        BlockState state = this.defaultBlockState();

        if (hasSupportBelow) {
            state = state.setValue(UP, false)
                    .setValue(NORTH, false)
                    .setValue(EAST, false)
                    .setValue(SOUTH, false)
                    .setValue(WEST, false)
                    .setValue(PERMANENT, false);
            return state;
        }

        boolean anySide = false;

        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) continue;
            BooleanProperty prop = FACING_TO_PROPERTY_MAP.get(dir);
            if (prop != null) {
                boolean flammable = this.canCatchFire(world, pos.relative(dir), dir.getOpposite());
                if (flammable) anySide = true;
                state = state.setValue(prop, flammable);
            }
        }

        if (!anySide) {
            state = state.setValue(UP, true);
        }

        return state.setValue(PERMANENT, false);
    }


    /**
     * Returns true if this block can place another block at the specified location.
     **/
    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        return worldIn.getBlockState(blockpos).isFaceSturdy(worldIn, blockpos, Direction.UP) || this.areNeighborsFlammable(worldIn, pos);
    }


    protected boolean areNeighborsFlammable(BlockGetter worldIn, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (this.canCatchFire(worldIn, pos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        //return this.isValidPosition(state, worldIn, currentPos) ? this.getStateForPlacement(worldIn, currentPos).with(AGE, state.get(AGE)) : Blocks.AIR.getDefaultState();
        return this.getStateForPlacement(worldIn, currentPos).setValue(AGE, state.getValue(AGE));
    }

    @Override
    public int tickRate(LevelAccessor world) {
        return this.tickRate;
    }


    @Override
    public void tick(BlockState blockState, ServerLevel world, BlockPos pos, RandomSource rand) {
        if (blockState.getValue(STATIC) || !world.isAreaLoaded(pos, 2)) return;

        boolean permanent = blockState.getValue(PERMANENT);

        if (!world.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (this.removeOnNoFireTick && !permanent) world.removeBlock(pos, false);
            return;
        }

        if (this.removeOnTick) {
            world.removeBlock(pos, false);
            return;
        }

        BlockState below = world.getBlockState(pos.below());
        if (below.getBlock() == Blocks.AIR) {
            world.removeBlock(pos, false);
            return;
        }

        boolean onSource = permanent || this.isBlockFireSource(below, world, pos.below(), Direction.UP);
        int age = blockState.getValue(AGE);

        if (!onSource && this.canDie(world, pos) && rand.nextFloat() < 0.2F + age * 0.03F) {
            world.removeBlock(pos, false);
            return;
        }

        if (age < 15) {
            int inc = rand.nextInt(this.agingRate + 1);
            blockState = blockState.setValue(AGE, Math.min(15, age + inc));
            world.setBlock(pos, blockState, 4);
            age = blockState.getValue(AGE);
        }

        if (this.loopTicks) {
            int delay = Math.max(1, this.tickRate(world) + rand.nextInt(10));
            world.scheduleTick(pos, this, delay, TickPriority.LOW);
        }

        if (!onSource) {
            if (!this.canNeighborCatchFire(world, pos)) {
                if (age > 3) world.removeBlock(pos, false);
                return;
            }
            if (!this.canCatchFire(world, pos.below(), Direction.UP) && age == 15 && rand.nextInt(4) == 0) {
                world.removeBlock(pos, false);
                return;
            }
        }

        if (this.spreadChance <= 0 || permanent) return;

        boolean highHumidity = world.getBiome(pos).get().getModifiedClimateSettings().downfall() > 0.85F;
        int humidity = highHumidity ? -50 : 0;
        this.tryCatchFire(world, pos.east(), 300 + humidity, rand, age, Direction.WEST);
        this.tryCatchFire(world, pos.west(), 300 + humidity, rand, age, Direction.EAST);
        this.tryCatchFire(world, pos.below(), 250 + humidity, rand, age, Direction.UP);
        this.tryCatchFire(world, pos.above(), 250 + humidity, rand, age, Direction.DOWN);
        this.tryCatchFire(world, pos.north(), 300 + humidity, rand, age, Direction.SOUTH);
        this.tryCatchFire(world, pos.south(), 300 + humidity, rand, age, Direction.NORTH);

        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                for (int dy = -1; dy <= 4; ++dy) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    int chance = 100 + Math.max(0, dy - 1) * 100;
                    BlockPos p = pos.offset(dx, dy, dz);
                    int enc = this.getNeighborEncouragement(world, p);
                    if (enc > 0) {
                        int flam = (enc + 40 + world.getDifficulty().getId() * 7) / (age + 30);
                        if (highHumidity) flam /= 2;
                        if (flam > 0 && rand.nextInt(chance) <= flam && (!world.isRaining() || !this.canDie(world, p))) {
                            int newAge = Math.min(15, age + rand.nextInt(5) / 4);
                            this.burnBlockReplace(world, p, newAge);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns true if any adjacent blocks can catch fire.
     **/
    protected boolean canNeighborCatchFire(Level worldIn, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (this.canCatchFire(worldIn, pos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the flammability of nearby blocks, highly flammable blocks that are near each other will help spread fire faster.
     **/
    protected int getNeighborEncouragement(Level worldIn, BlockPos pos) {
        if (!worldIn.isEmptyBlock(pos))
            return 0;
        else {
            int i = 0;
            for (Direction direction : Direction.values()) {
                i = Math.max(worldIn.getBlockState(pos.relative(direction)).getFlammability(worldIn, pos.relative(direction), direction.getOpposite()), i);
            }
            return i;
        }
    }

    /**
     * Attempts to ignite the position.
     **/
    private void tryCatchFire(Level world, BlockPos pos, int chance, RandomSource random, int age, Direction face) {
        int flammability = this.getBlockFlammability(world, pos, face);
        if (Math.round(random.nextInt(chance) / this.spreadChance) < flammability) {
            BlockState blockState = world.getBlockState(pos);

            if (random.nextInt(age + 10) < 5 && !world.isRainingAt(pos)) {
                int newFireAge = age + random.nextInt(5) / 4;
                if (newFireAge > 15)
                    newFireAge = 15;
                this.burnBlockReplace(world, pos, newFireAge);
            } else {
                this.burnBlockDestroy(world, pos);
            }

            if (this.triggerTNT && blockState.getBlock() instanceof TntBlock) {
                TntBlock.explode(world, pos);
            }
        }
    }

    /**
     * Burns away a block, typically replacing it with this fire block, but can change it to other things depending on the type of fire block.
     **/

    public void burnBlockReplace(Level world, BlockPos pos, int newFireAge) {
        BlockState placement = this.getStateForPlacement(world, pos)
                .setValue(AGE, newFireAge)
                .setValue(PERMANENT, false);

        world.setBlock(pos, placement, 3);
    }

    /**
     * Burns away a block, typically setting it to air but can change it to other things depending on the type of fire block.
     **/
    public void burnBlockDestroy(Level world, BlockPos pos) {
        world.removeBlock(pos, false);
    }

    /**
     * Returns true if the block at the provided position and face can catch fire.
     **/
    public boolean canCatchFire(BlockGetter world, BlockPos pos, Direction face) {
        return world.getBlockState(pos).isFlammable(world, pos, face);
    }

    /**
     * Checks if the provided block is a fire source, can be overridden for custom sources.
     **/
    public boolean isBlockFireSource(BlockState state, LevelAccessor world, BlockPos pos, Direction side) {
        return state.isFireSource(world, pos, side);
    }

    /**
     * Returns how flammable the target block is.
     **/
    public int getBlockFlammability(BlockGetter world, BlockPos pos, Direction face) {
        return world.getBlockState(pos).getFlammability(world, pos, face);
    }

    /**
     * Returns true if this fire block should be extinguished, can check for rain and position, etc.
     **/
    protected boolean canDie(Level world, BlockPos pos) {
        return world.isRainingAt(pos) || world.isRainingAt(pos.west()) || world.isRainingAt(pos.east()) || world.isRainingAt(pos.north()) || world.isRainingAt(pos.south());
    }

    /**
     * Client side animation and sounds.
     **/
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (random.nextInt(24) == 0) {
            world.playLocalSound(x + 0.5D, y + 0.5D, z + 0.5D, ObjectManager.getSound(this.blockName), SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
        }
        super.animateTick(state, world, pos, random);
    }
}
