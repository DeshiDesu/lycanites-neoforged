package com.lycanitesmobs.core.mixin;

import com.lycanitesmobs.LycanitesMobs;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = CreativeModeTabRegistry.class, remap = false)
public abstract class CreativeModeTabRegistryMixin {

    @Final
    @Shadow
    private static List<CreativeModeTab> SORTED_TABS;

    @Inject(method = "setCreativeModeTabOrder", at = @At("RETURN"))
    private static void rewriteTabs(List<CreativeModeTab> tierList, CallbackInfo ci) {
        List<CreativeModeTab> list = SORTED_TABS;

        List<CreativeModeTab> vanilla = new ArrayList<>();
        List<CreativeModeTab> others = new ArrayList<>();

        CreativeModeTab lyItems = null;
        CreativeModeTab lyBlocks = null;
        CreativeModeTab lyCreatures = null;
        CreativeModeTab lyEquipment = null;
        CreativeModeTab lyProjectile = null;
        List<CreativeModeTab> extraLycanites = new ArrayList<>();

        for (CreativeModeTab tab : list) {
            ResourceLocation id = CreativeModeTabRegistry.getName(tab);
            if (id == null) continue;

            String ns = id.getNamespace();
            String path = id.getPath();

            if ("minecraft".equals(ns)) {
                vanilla.add(tab);
                continue;
            }

            if ("lycanitesmobs".equals(ns)) {
                if (path.contains("items") && lyItems == null) {
                    lyItems = tab;
                } else if (path.contains("blocks") && lyBlocks == null) {
                    lyBlocks = tab;
                } else if (path.contains("creatures") && lyCreatures == null) {
                    lyCreatures = tab;
                } else if ((path.contains("equipment") || path.contains("equipmentparts")) && lyEquipment == null) {
                    lyEquipment = tab;
                } else if ((path.contains("charge") || path.contains("projectile")) && lyProjectile == null) {
                    lyProjectile = tab;
                } else {
                    extraLycanites.add(tab);
                }
                continue;
            }

            others.add(tab);
        }

        List<CreativeModeTab> reordered = new ArrayList<>(list.size());
        reordered.addAll(vanilla);

        if (lyItems != null) reordered.add(lyItems);
        if (lyBlocks != null) reordered.add(lyBlocks);
        if (lyCreatures != null) reordered.add(lyCreatures);
        if (lyProjectile != null) reordered.add(lyProjectile);
        if (lyEquipment != null) reordered.add(lyEquipment);
        reordered.addAll(extraLycanites);

        reordered.addAll(others);

        list.clear();
        list.addAll(reordered);
    }

}
