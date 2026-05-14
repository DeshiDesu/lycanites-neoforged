package com.lycanitesmobs.core.item.consumable.utility;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.item.base.BaseItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemImmunizer extends BaseItem {

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemImmunizer(Item.Properties properties) {
        super(properties);
        this.itemName = "immunizer";
        this.setup();
    }


    // ==================================================
    //                    Item Use
    // ==================================================
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
        }

        if (!world.isClientSide && ObjectManager.getEffect("immunization") != null) {
            player.addEffect(new MobEffectInstance(ObjectManager.getEffect("immunization"), 10 * 20));
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }
}
