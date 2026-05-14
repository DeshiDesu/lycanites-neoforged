package com.lycanitesmobs.core.block.special;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.base.BlockBase;
import com.lycanitesmobs.core.container.provider.EquipmentForgeContainerProvider;
import com.lycanitesmobs.core.block.blockentity.TileEntityEquipmentForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import static com.lycanitesmobs.core.tabs.LMBlocksGroup.blockNames;

public class BlockEquipmentForge extends BlockBase implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public int level;

    // ==================================================
    //                   Constructor
    // ==================================================
    public BlockEquipmentForge(Block.Properties properties, int level) {
        super(properties, "");

        // Properties:
        this.level = level;
        if (level <= 1) {
            this.blockName = "equipmentforge_lesser";
        } else if (level == 2) {
            this.blockName = "equipmentforge_greater";
        } else {
            this.blockName = "equipmentforge_master";
        }
        blockNames.add(this.blockName);
        this.setRegistryName(LycanitesMobs.MODID, this.blockName.toLowerCase());
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityEquipmentForge forge) {
                for (int i = 1; i < forge.getContainerSize(); i++) {
                    ItemStack stack = forge.getItem(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation direction) {
        return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, entity, itemStack);
    }

/*	@Override
	public boolean hasTileEntity(BlockState blockState) {
		return true;
	}*/

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        TileEntityEquipmentForge tileEntityEquipmentForge = new TileEntityEquipmentForge(blockPos, state);
        tileEntityEquipmentForge.setLevel(this.level);
        return tileEntityEquipmentForge;
    }

    @Override //onBlockActivated()
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof TileEntityEquipmentForge) {
                NetworkHooks.openScreen((ServerPlayer) player, new EquipmentForgeContainerProvider((TileEntityEquipmentForge) tileEntity), buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int eventID, int eventParam) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity != null && tileEntity.triggerEvent(eventID, eventParam);
    }
}
