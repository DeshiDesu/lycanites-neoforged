package com.lycanitesmobs.core.item.consumable.utility;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.altar.AltarInfo;
import com.lycanitesmobs.core.item.base.BaseItem;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ItemSoulkey extends BaseItem {
    public int variant = 0; // 0 = Standard, 1 = Diamond, 2 = Emerald

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemSoulkey(Item.Properties properties, String itemName, int variant) {
        super(properties);
        this.itemName = itemName;
        this.variant = variant;
        this.setup();
    }


    // ==================================================
    //                       Use
    // ==================================================
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (!AltarInfo.checkAltarsEnabled() && !player.getCommandSenderWorld().isClientSide) {
            MutableComponent message = Component.translatable("message.soulkey.disabled");
            player.displayClientMessage(message, false);
            return InteractionResult.FAIL;
        }

        // Get Possible Altars:
        List<AltarInfo> possibleAltars = new ArrayList<>();
        if (AltarInfo.altars.isEmpty())
            LMHelperClass.logWarning("", "No altars have been registered, Soulkeys will not work at all.");
        for (AltarInfo altarInfo : AltarInfo.altars.values()) {
            if (altarInfo.checkBlockEvent(player, world, pos) && altarInfo.quickCheck(player, world, pos)) {
                possibleAltars.add(altarInfo);
            }
        }
        if (possibleAltars.isEmpty()) {
            MutableComponent message = Component.translatable("message.soulkey.none");
            player.displayClientMessage(message, false);
            return InteractionResult.FAIL;
        }

        // Activate First Valid Altar:
        for (AltarInfo altarInfo : possibleAltars) {
            if (altarInfo.fullCheck(player, world, pos)) {

                // Valid Altar:
                if (!player.getCommandSenderWorld().isClientSide) {
                    if (!altarInfo.activate(player, world, pos, this.variant)) {
                        MutableComponent message = Component.translatable("message.soulkey.badlocation");
                        player.displayClientMessage(message, false);
                        return InteractionResult.FAIL;
                    }
                    if (!player.getAbilities().instabuild)
                        itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
                    if (itemStack.getCount() <= 0)
                        player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                    MutableComponent message = Component.translatable("message.soulkey.active");
                    player.displayClientMessage(message, false);
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (!player.getCommandSenderWorld().isClientSide) {
            MutableComponent message = Component.translatable("message.soulkey.invalid");
            player.displayClientMessage(message, false);
        }

        return InteractionResult.FAIL;
    }
}
