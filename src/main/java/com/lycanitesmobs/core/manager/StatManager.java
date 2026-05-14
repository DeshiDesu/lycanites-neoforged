package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class StatManager {
    private static StatManager INSTANCE;

    public static StatManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StatManager();
        }
        return INSTANCE;
    }

    public Map<String, StatType<ResourceLocation>> statTypes = new HashMap<>();

    /**
     * Creates all base Stat Types.
     */
    public void createStatTypes() {
        StatManager.getInstance().addStatType("learn");
        StatManager.getInstance().addStatType("summon");
    }

    /**
     * Adds a new Stat Type. TODO Implement stats.
     *
     * @param name The unique name of the stat type.
     */
    public void addStatType(String name) {
//		Registry<ResourceLocation> statRegistry = new SimpleRegistry<>();
//		StatType<ResourceLocation> statType = new StatType<>(statRegistry);
//		statType.setRegistryName(LycanitesMobs.modInfo.modid, name);
//		this.statTypes.put(name, statType);
    }

    /**
     * Gets a from a stat type.
     *
     * @param typeName The stat type name.
     * @param name     The stat name.
     * @return The stat instance or null.
     */
    @Nullable
    public Stat getStat(String typeName, String name) {
        if (!this.statTypes.containsKey(typeName)) {
            return null;
        }
        return this.statTypes.get(typeName).get(new ResourceLocation(LycanitesMobs.modInfo.modid, name), StatFormatter.DEFAULT);
    }
}
