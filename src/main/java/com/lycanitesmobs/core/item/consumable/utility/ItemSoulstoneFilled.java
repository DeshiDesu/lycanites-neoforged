package com.lycanitesmobs.core.item.consumable.utility;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.data.info.creature.CreatureType;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemSoulstoneFilled extends ItemSoulstone {
    public ItemSoulstoneFilled(Item.Properties properties, CreatureType creatureType) {
        super(properties, creatureType);
        this.itemName = creatureType.getSoulstoneName();
        this.setRegistryName(LycanitesMobs.MODID, this.itemName);
    }

    @Override
    public void setup() {
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
        }
        if (this.creatureType.tameableCreatures.isEmpty()) {
            LMHelperClass.logInfo("", "Tried to use a " + this.creatureType.getSoulstoneName() + " but there are no tameable creatures for this type yet.");
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
        }

        if (!player.getCommandSenderWorld().isClientSide) {
            int creatureIndex = 0;
            if (this.creatureType.tameableCreatures.size() > 1) {
                creatureIndex = player.getRandom().nextInt(this.creatureType.tameableCreatures.size());
            }
            TameableCreatureEntity entity = (TameableCreatureEntity) this.creatureType.tameableCreatures.get(creatureIndex).createEntity(world);
            entity.moveTo(player.getX(), player.getY(), player.getZ(), player.yRotO, player.xRotO);
            world.addFreshEntity(entity);
            entity.setPlayerOwner(player);
            super.interactLivingEntity(itemStack, player, entity, hand);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }


    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (this.creatureType.tameableCreatures.isEmpty()) {
            return super.interactLivingEntity(stack, player, entity, hand);
        }
        return InteractionResult.FAIL;
    }
}
