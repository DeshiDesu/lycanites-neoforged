package com.lycanitesmobs.core.block.base;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.BlockTypeGetter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static com.lycanitesmobs.core.tabs.LMBlocksGroup.blockNames;

public class BlockBase extends Block implements BlockTypeGetter {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    // Properties:
    public String blockName = "block_base";
    public ResourceLocation registryName;
    // Stats:
    /**
     * If set to a value above 0, the block will update on the specified number of ticks.
     **/
    public int tickRate = 0;
    /**
     * If true, this block will be set to air on it's first tick, useful for blocks that despawn over time like fire.
     **/
    public boolean removeOnTick = false;
    /**
     * If true after performing a tick update, another tick update will be scheduled thus creating a loop.
     **/
    public boolean loopTicks = true;
    /**
     * Will falling blocks such as sand or gravel destroy this block if they land on it?
     */
    public boolean canBeCrushed = false;

    /**
     * If true, this block cannot be broken or even hit like a solid block.
     **/
    public boolean noBreakCollision = false;

    public BlockBase(Block.Properties properties, String name) {
        super(properties);
        this.blockName = name;
        this.setRegistryName(LycanitesMobs.MODID, this.blockName.toLowerCase());
        blockNames.add(name);
    }

    @Nullable
    /*@Override*/
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return null;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public void setRegistryName(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    public ResourceLocation setRegistryName(String modID, String blockName) {
        return registryName = new ResourceLocation(modID, blockName);
    }

    public void setup() {
        this.setRegistryName(LycanitesMobs.MODID, this.blockName);
    }

    @Override
    @Nonnull
    public String getDescriptionId() {
        return "block." + LycanitesMobs.modInfo.modid + "." + this.blockName;
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable(this.getDescriptionId());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(this.getDescription(stack, world));
    }

    public Component getDescription(ItemStack itemStack, @Nullable BlockGetter world) {
        return Component.translatable(this.getDescriptionId() + ".description").withStyle(ChatFormatting.GREEN);
    }

    // ==================================================
    //                      Place
    // ==================================================
    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (this.tickRate > 0 && world instanceof ServerLevel server) {
            server.scheduleTick(pos, this, this.tickRate(world), TickPriority.LOW);
        }
    }


    // ==================================================
    //                     Ticking
    // ==================================================
    // ========== Tick Rate ==========
    public int tickRate(LevelAccessor world) {
        return this.tickRate;
    }

    // ========== Tick Update ==========
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (this.removeOnTick && this.canRemove(world, pos, state, random)) {
            world.removeBlock(pos, true);
        } else if (this.tickRate > 0 && this.loopTicks) {
            world.scheduleTick(pos, this, this.tickRate(world), TickPriority.LOW);
        }
    }

    /**
     * Returns true if the block should be removed naturally (remove on tick).
     **/
    public boolean canRemove(Level world, BlockPos pos, BlockState state, RandomSource random) {
        return true;
    }

    // ========== Can Remove ==========

    // ==================================================
    //                    Collision
    // ==================================================
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext) {
        if (this.noBreakCollision) {
            return Shapes.empty();
        }
        return super.getShape(blockState, world, blockPos, selectionContext);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext) {
        return this.hasCollision ? blockState.getShape(world, blockPos) : Shapes.empty();
    }

    // ==================================================
    //                Collision Effects
    // ==================================================
    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(world, pos, state, entity);
    }

    @Override
    public void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity) {
        super.entityInside(blockState, world, pos, entity);
    }


    // Rendering:
    public static enum RENDER_TYPE {
        NONE(-1), NORMAL(0), CROSS(1), TORCH(2), FIRE(3), FLUID(4); // More found on RenderBlock, or use Client Proxies for custom renderers.
        public final int id;

        private RENDER_TYPE(int value) {
            this.id = value;
        }

        public int getValue() {
            return id;
        }
    }
}
