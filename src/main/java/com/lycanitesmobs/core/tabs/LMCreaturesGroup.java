package com.lycanitesmobs.core.tabs;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.item.consumable.entity.ItemCustomSpawnEgg;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LMCreaturesGroup {
    public static final List<String> creatureNames = new ArrayList<>();
    public CreativeModeTab.Builder builder = CreativeModeTab.builder();
    private ItemStack iconStack = ItemStack.EMPTY;
    private boolean fallbackIcon = false;

    public LMCreaturesGroup() {
        init();
    }

    public void init() {
        builder = builder.title(Component.translatable("itemGroup." + LycanitesMobs.MODID + ".creatures"))
                .icon(this::getIconItem)
                .displayItems((enabledFeatures, entries) -> {
                    // Sort the creatureNames list alphabetically
                    Collections.sort(creatureNames);
                    for (String item : creatureNames) {
                        var i = ObjectManager.getItem(item);
                        if (i instanceof ItemCustomSpawnEgg spawnEgg) {
                            for (CreatureInfo creatureInfo : spawnEgg.getCreatureType().creatures.values()) {
                                ItemStack itemstack = new ItemStack(i, 1);
                                spawnEgg.applyCreatureInfoToItemStack(itemstack, creatureInfo);
                                entries.accept(itemstack);
                            }
                        }
                    }
                });
    }

    public static CreativeModeTab.Builder getBuilder() {
        return new LMCreaturesGroup().builder;
    }

    public ItemStack getIconItem() {
        this.fallbackIcon = false;
        if (ObjectManager.getItem("beastspawn") != null)
            return new ItemStack(ObjectManager.getItem("beastspawn"));
        if (ObjectManager.getItem("demonspawn") != null)
            return new ItemStack(ObjectManager.getItem("demonspawn"));
        if (ObjectManager.getItem("avianspawn") != null)
            return new ItemStack(ObjectManager.getItem("avianspawn"));
        if (ObjectManager.getItem("arthropodspawn") != null)
            return new ItemStack(ObjectManager.getItem("arthropodspawn"));

        this.fallbackIcon = true;
        return new ItemStack(Items.CREEPER_SPAWN_EGG);
    }

}