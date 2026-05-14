package com.lycanitesmobs.core.item.summoningstaff;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemStaffBlood extends ItemStaffSummoning {

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemStaffBlood(Item.Properties properties, String itemName, String textureName) {
        super(properties, itemName, textureName);
    }


    // ==================================================
    //                       Use
    // ==================================================
    // ========== Rapid Time ==========
    @Override
    public int getRapidTime(ItemStack itemStack) {
        return 20;
    }

    // ========== Summon Cost ==========
    @Override
    public int getSummonCostBoost() {
        return 0;
    }

    @Override
    public float getSummonCostMod() {
        return 0.5F;
    }

    // ========== Summon Duration ==========
    @Override
    public int getSummonDuration() {
        return 60 * 20;
    }

    // ========== Additional Costs ==========
    @Override
    public boolean getAdditionalCosts(Player player) {
        if (player.getHealth() <= 7)
            return false;
        player.hurt(player.level().damageSources().magic(), 6);
        return true;
    }


    // ==================================================
    //                     Repairs
    // ==================================================
    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairStack) {
        if (repairStack.getItem() == Items.GOLD_INGOT) return true;
        return super.isValidRepairItem(itemStack, repairStack);
    }
}
