package com.lycanitesmobs.core.item.special;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.item.base.BaseItem;
import net.minecraft.world.item.Item;


public class ItemMobToken extends BaseItem {

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemMobToken(Item.Properties properties) {
        super(properties);
        this.itemName = "mobtoken";
        this.setup();
    }

    @Override
    public void setup() {
        this.setRegistryName(LycanitesMobs.MODID, this.itemName);
    }
}
