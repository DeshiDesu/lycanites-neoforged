package com.lycanitesmobs.core.tabs;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.lycanitesmobs.core.manager.ObjectManager.blockItems;

public class LMBlocksGroup {
    public static final List<String> blockNames = new ArrayList<>();
    public CreativeModeTab.Builder builder = CreativeModeTab.builder();

    // ========== Constructor ==========
    public LMBlocksGroup() {
        init();
    }

    public void init() {
        builder.title(Component.translatable("itemGroup." + LycanitesMobs.MODID + ".blocks"))
                .icon(this::getIconItem)
                .displayItems((enabledFeatures, entries) -> {

                    List<String> remaining = new ArrayList<>();

                    for (Map.Entry<String, Lazy<? extends BlockItem>> entry : blockItems.entrySet()) {
                        String name = entry.getKey();
                        if (ObjectManager.getBlock(name) != null) {
                            remaining.add(name);
                        }
                    }

                    List<String> ordered = new ArrayList<>();

                    String[][] familyPatterns = new String[][]{
                            {"aberrantstone", "aberrantcrystal"},
                            {"ashenstone", "ashencrystal"},
                            {"desertstone", "desertcrystal"},
                            {"lushstone", "lushcrystal"},
                            {"streamstone", "streamcrystal"},
                            {"shadowstone", "shadowcrystal"},
                            {"demonstone", "demoncrystal"}
                    };

                    for (String[] patterns : familyPatterns) {
                        List<String> solid = new ArrayList<>();
                        List<String> shaped = new ArrayList<>();

                        Iterator<String> it = remaining.iterator();
                        while (it.hasNext()) {
                            String name = it.next();
                            boolean match = false;
                            for (String p : patterns) {
                                if (name.contains(p)) {
                                    match = true;
                                    break;
                                }
                            }
                            if (!match) continue;

                            it.remove();

                            boolean isShaped =
                                    name.contains("slab") ||
                                            name.contains("stairs") ||
                                            name.contains("fence") ||
                                            name.contains("wall");

                            if (isShaped) {
                                shaped.add(name);
                            } else {
                                solid.add(name);
                            }
                        }

                        Collections.sort(solid);
                        Collections.sort(shaped);

                        ordered.addAll(solid);
                        ordered.addAll(shaped);
                    }

                    List<String> webs = new ArrayList<>();
                    List<String> fires = new ArrayList<>();
                    List<String> clouds = new ArrayList<>();

                    Iterator<String> itFireWebCloud = remaining.iterator();
                    while (itFireWebCloud.hasNext()) {
                        String name = itFireWebCloud.next();
                        if (name.contains("web")) {
                            webs.add(name);
                            itFireWebCloud.remove();
                        } else if (name.contains("fire")) {
                            fires.add(name);
                            itFireWebCloud.remove();
                        } else if (name.contains("cloud")) {
                            clouds.add(name);
                            itFireWebCloud.remove();
                        }
                    }

                    Collections.sort(webs);
                    Collections.sort(fires);
                    Collections.sort(clouds);

                    ordered.addAll(webs);
                    ordered.addAll(fires);
                    ordered.addAll(clouds);

                    List<String> blockEntities = new ArrayList<>();
                    Iterator<String> it = remaining.iterator();
                    while (it.hasNext()) {
                        String name = it.next();
                        if (name.startsWith("equipment") ||
                                name.startsWith("summoningpedestal") ||
                                name.startsWith("soulcube")) {
                            blockEntities.add(name);
                            it.remove();
                        }
                    }

                    blockEntities.sort((a, b) -> {
                        int tierA = a.contains("lesser") ? 1 : a.contains("greater") ? 2 : a.contains("master") ? 3 : 0;
                        int tierB = b.contains("lesser") ? 1 : b.contains("greater") ? 2 : b.contains("master") ? 3 : 0;
                        if (tierA != tierB) return Integer.compare(tierA, tierB);
                        return a.compareTo(b);
                    });
                    ordered.addAll(blockEntities);

                    Collections.sort(remaining);
                    ordered.addAll(remaining);

                    for (String name : ordered) {
                        entries.accept(ObjectManager.getBlock(name));
                    }
                }).build();
    }


    public static CreativeModeTab.Builder getBuilder() {
        return new LMBlocksGroup().builder;
    }

    // ========== Tab Icon ==========
    public ItemStack getIconItem() {
        if (ObjectManager.getBlock("shadowcrystal") != null)
            return new ItemStack(Item.byBlock(ObjectManager.getBlock("shadowcrystal")));
        else if (ObjectManager.getBlock("summoningpedestal") != null)
            return new ItemStack(Item.byBlock(ObjectManager.getBlock("summoningpedestal")));
        else if (ObjectManager.getBlock("demoncrystal") != null)
            return new ItemStack(Item.byBlock(ObjectManager.getBlock("demoncrystal")));
        else
            return new ItemStack(Item.byBlock(Blocks.OBSIDIAN));
    }
}
