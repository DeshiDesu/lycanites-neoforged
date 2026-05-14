package com.lycanitesmobs.core.tabs;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.item.consumable.entity.ChargeItem;
import com.lycanitesmobs.core.item.consumable.entity.ItemCustomSpawnEgg;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

public class LMItemsGroup {
    public CreativeModeTab.Builder builder = CreativeModeTab.builder();
    public static final List<String> itemNames = new ArrayList<>();

    // ========== Constructor ==========
    public LMItemsGroup() {
        init();
    }

    public static CreativeModeTab.Builder getBuilder() {
        return new LMItemsGroup().builder;
    }

    public void init() {
        builder = builder
                .title(Component.translatable("itemGroup." + LycanitesMobs.MODID + ".items"))
                .icon(this::getIconItem)
                .displayItems((enabledFeatures, entries) -> {
                    List<String> filtered = new ArrayList<>();
                    for (String name : itemNames) {
                        Item item = ObjectManager.getItem(name);
                        if (item instanceof ItemEquipmentPart) continue;
                        if (item instanceof ItemCustomSpawnEgg) continue;
                        if (item instanceof ChargeItem) continue;
                        if (name.equals("equipment")) continue;
                        filtered.add(name);
                    }

                    String[] patterns = new String[]{
                            "saddle_", "treat_", "soulstone_",
                            "summoningstaff", "staff",
                            "_bucket"
                    };

                    String FOOD_KEY = "food";

                    Map<String, List<String>> groups = new HashMap<>();
                    List<String> leftover = new ArrayList<>();

                    for (String name : filtered) {
                        Item item = ObjectManager.getItem(name);

                        boolean isFood = item != null && item.getFoodProperties() != null;

                        if (isFood) {
                            List<String> list = groups.get(FOOD_KEY);
                            if (list == null) {
                                list = new ArrayList<>();
                                groups.put(FOOD_KEY, list);
                            }
                            list.add(name);
                            continue;
                        }

                        String matched = null;
                        for (String p : patterns) {
                            if (name.contains(p)) {
                                matched = p;
                                break;
                            }
                        }

                        if (matched != null) {
                            List<String> list = groups.get(matched);
                            if (list == null) {
                                list = new ArrayList<>();
                                groups.put(matched, list);
                            }
                            list.add(name);
                        } else {
                            leftover.add(name);
                        }
                    }

                    List<String> usedPatterns = new ArrayList<>();

                    if (groups.containsKey(FOOD_KEY)) {
                        usedPatterns.add(FOOD_KEY);
                    }

                    for (String p : patterns) {
                        if (groups.containsKey(p)) {
                            usedPatterns.add(p);
                        }
                    }

                    Collections.sort(usedPatterns, (a, b) -> {
                        int ca = groups.get(a).size();
                        int cb = groups.get(b).size();
                        if (ca != cb) return Integer.compare(cb, ca);
                        int ia = -1;
                        int ib = -1;
                        for (int i = 0; i < patterns.length; i++) {
                            if (patterns[i].equals(a)) ia = i;
                            if (patterns[i].equals(b)) ib = i;
                        }
                        return Integer.compare(ia, ib);
                    });

                    for (String p : usedPatterns) {
                        List<String> names = groups.get(p);
                        if (p.equals(FOOD_KEY)) {
                            List<String> baked = new ArrayList<>();
                            List<String> cookedMeat = new ArrayList<>();
                            List<String> rawMeat = new ArrayList<>();

                            for (String name : names) {
                                boolean cooked = name.startsWith("cooked_") && name.endsWith("_meat");
                                boolean raw = name.startsWith("raw_") && name.endsWith("_meat");
                                if (cooked) cookedMeat.add(name);
                                else if (raw) rawMeat.add(name);
                                else baked.add(name);
                            }

                            Collections.sort(baked);
                            Collections.sort(cookedMeat);
                            Collections.sort(rawMeat);

                            for (String name : baked) {
                                Item item = ObjectManager.getItem(name);
                                if (item != null) {
                                    entries.accept(item);
                                }
                            }
                            for (String name : cookedMeat) {
                                Item item = ObjectManager.getItem(name);
                                if (item != null) {
                                    entries.accept(item);
                                }
                            }
                            for (String name : rawMeat) {
                                Item item = ObjectManager.getItem(name);
                                if (item != null) {
                                    entries.accept(item);
                                }
                            }
                        } else {
                            Collections.sort(names);
                            for (String name : names) {
                                Item item = ObjectManager.getItem(name);
                                if (item != null) {
                                    entries.accept(item);
                                }
                            }
                        }
                    }

                    Collections.sort(leftover);
                    for (String name : leftover) {
                        Item item = ObjectManager.getItem(name);
                        if (item != null) {
                            entries.accept(item);
                        }
                    }
                });
    }


    // ========== Tab Icon ==========
    public ItemStack getIconItem() {
        if (ObjectManager.getItem("soulgazer") != null)
            return new ItemStack(ObjectManager.getItem("soulgazer"));
        else if (ObjectManager.getItem("poisongland") != null)
            return new ItemStack(ObjectManager.getItem("poisongland"));
        else
            return new ItemStack(Items.EMERALD);
    }
}