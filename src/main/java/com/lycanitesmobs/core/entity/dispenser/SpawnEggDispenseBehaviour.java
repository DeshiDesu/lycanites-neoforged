package com.lycanitesmobs.core.entity.dispenser;

import com.lycanitesmobs.core.item.consumable.entity.ItemCustomSpawnEgg;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

public class SpawnEggDispenseBehaviour extends DefaultDispenseItemBehavior {
    @Override
    public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ItemCustomSpawnEgg))
            return itemStack;

        ItemCustomSpawnEgg itemCustomSpawnEgg = (ItemCustomSpawnEgg) itemStack.getItem();
        Position position = DispenserBlock.getDispensePosition(blockSource);
        Entity entity = itemCustomSpawnEgg.spawnCreature(blockSource.getLevel(), itemStack, position.x(), position.y(), position.z());
        if (itemStack.hasCustomHoverName())
            entity.setCustomName(itemStack.getHoverName());

        itemStack.split(1);
        return itemStack;
    }
}
