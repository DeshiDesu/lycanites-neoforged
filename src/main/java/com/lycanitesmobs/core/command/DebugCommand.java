package com.lycanitesmobs.core.command;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.config.ConfigDebug;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class DebugCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("debug")
                .then(Commands.literal("log").then(Commands.argument("category", StringArgumentType.string()).executes(DebugCommand::log)))
                .then(Commands.literal("list").executes(DebugCommand::list))
                .then(Commands.literal("biomesfromtag").then(Commands.argument("biometag", StringArgumentType.string()).executes(DebugCommand::biomesfromtag)))
                .then(Commands.literal("listbiometags").executes(DebugCommand::listbiometags).then(Commands.argument("biome", StringArgumentType.string()).executes(DebugCommand::listbiometagsforbiome)))
                .then(Commands.literal("overlay").executes(DebugCommand::overlay));
    }

    public static int log(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        String category = StringArgumentType.getString(context, "category").toLowerCase();
        List<String> enabledLogs = new ArrayList<>();
        enabledLogs.addAll(ConfigDebug.INSTANCE.enabled.get());

        if (enabledLogs.contains(category)) {
            enabledLogs.remove(category);
            LMHelperClass.logDebug("", category + " Debug Logging Disabled");
        } else {
            enabledLogs.add(category);
            LMHelperClass.logDebug("", category + " Debug Logging Enabled");
        }
        ConfigDebug.INSTANCE.enabled.set(enabledLogs);
        ConfigDebug.INSTANCE.enabled.save();

        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.debug.log").append(" " + category), true);
        return 0;
    }

    public static int list(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.debug.list"), true);
        String[] debugCategories = new String[]{"jsonspawner", "mobspawns", "entity", "subspecies", "creature", "mobevents", "dungeon", "items", "equipment"};
        for (String debugCategory : debugCategories) {
            context.getSource().sendSuccess(() -> Component.literal(debugCategory), true);
        }
        return 0;
    }

    public static int biomesfromtag(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        String biomeTag = StringArgumentType.getString(context, "biometag").toLowerCase();
        BiomeManager.BiomeType biomeType = null;
        try {
            biomeType = BiomeManager.BiomeType.valueOf(biomeTag);
        } catch (Exception e) {
            LMHelperClass.logWarning("", "Unknown biome tag: " + biomeTag + ".");
        }
        if (biomeType == null) {
            return 0;
        }
        for (BiomeManager.BiomeEntry biomeKey : BiomeManager.getBiomes(biomeType)) {
            context.getSource().sendSuccess(() -> Component.literal(biomeKey.getKey().location().toString()), true); // TODO Figure out how the hell to get a biome display name now...
        }
        return 0;
    }

    public static int listbiometags(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        for (BiomeManager.BiomeType biomeType : BiomeManager.BiomeType.values()) {
            context.getSource().sendSuccess(() -> Component.literal(biomeType.name()), true);
        }
        return 0;
    }

    public static int listbiometagsforbiome(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        String biomeId = StringArgumentType.getString(context, "biome");
        ResourceLocation biomeResourceLocation = new ResourceLocation(biomeId);
        Biome biome = ForgeRegistries.BIOMES.getValue(biomeResourceLocation);
        if (biome == null) {
            context.getSource().sendSuccess(() -> Component.literal("Cannot find a biome with that id."), true);
            return 0;
        }
        for (BiomeManager.BiomeType biomeType : BiomeManager.BiomeType.values()) {
            for (BiomeManager.BiomeEntry biomeKey : BiomeManager.getBiomes(biomeType)) {
                if (biomeKey.getKey().location().toString().equals(biomeId)) {
                    context.getSource().sendSuccess(() -> Component.literal("Tags for: " + biomeId), true);
                    return 0;
                }
            }
        }
        return 0;
    }

    public static int overlay(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        ConfigDebug.INSTANCE.creatureOverlay.set(!ConfigDebug.INSTANCE.creatureOverlay.get());
        ConfigDebug.INSTANCE.creatureOverlay.save();
        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.debug.overlay"), true);
        return 0;
    }
}
