package com.lycanitesmobs.core.item.block;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.item.base.BaseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemBlockPlacer extends BaseItem {
    public String placedBlockName;

    public ItemBlockPlacer(Item.Properties properties, String itemName, String placedBlockName) {
        super(properties);
        this.itemName = itemName;
        this.placedBlockName = placedBlockName;
        this.setup();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            pos = pos.relative(context.getClickedFace());
            if (player.mayUseItemAt(pos, context.getClickedFace(), itemStack)) {
                try {
                    BlockState blockState = world.getBlockState(pos);
                    if (blockState.isAir()) {
                        world.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, ObjectManager.getSound(this.placedBlockName), SoundSource.PLAYERS, 1.0F, player.getRandom().nextFloat() * 0.4F + 0.8F, false);
                        world.setBlockAndUpdate(pos, ObjectManager.getBlock(this.placedBlockName).defaultBlockState());
                    }
                    if (!player.getAbilities().instabuild)
                        itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
                    return InteractionResult.SUCCESS;
                } catch (Exception e) {
                }
            }
        }
        return InteractionResult.FAIL;
    }
}
