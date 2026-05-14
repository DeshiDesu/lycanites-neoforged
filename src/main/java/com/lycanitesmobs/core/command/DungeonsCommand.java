package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.data.config.ConfigDungeons;
import com.lycanitesmobs.core.event.StructureSpawnEvents;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DungeonsCommand {
    private static final SuggestionProvider<CommandSourceStack> DUNGEON_SUGGESTIONS = (context, builder) -> {
        String remaining = builder.getRemaining().toLowerCase();
        DungeonManager manager = DungeonManager.getInstance();
        for (DungeonSchematic schematic : manager.getSchematics()) {
            if (schematic == null || schematic.name == null)
                continue;
            String name = schematic.name;
            if (remaining.isEmpty() || name.toLowerCase().startsWith(remaining)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    };

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("dungeons")
                .then(Commands.literal("reload").executes(DungeonsCommand::reload))
                .then(Commands.literal("enable").executes(DungeonsCommand::enable))
                .then(Commands.literal("disable").executes(DungeonsCommand::disable))
                .then(Commands.literal("locate")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(DUNGEON_SUGGESTIONS)
                                .executes(DungeonsCommand::locateSpecific)));
    }

    public static int reload(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        DungeonManager.getInstance().reload();
        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.dungeons.reload"), true);
        return 0;
    }

    public static int enable(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        ConfigDungeons.INSTANCE.dungeonsEnabled.set(true);
        ConfigDungeons.INSTANCE.dungeonsEnabled.save();
        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.dungeons.enable"), true);
        return 0;
    }

    public static int disable(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        ConfigDungeons.INSTANCE.dungeonsEnabled.set(false);
        ConfigDungeons.INSTANCE.dungeonsEnabled.save();
        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.dungeons.disable"), true);
        return 0;
    }

    public static int locateSpecific(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }

        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(level);

        String targetName = StringArgumentType.getString(context, "name");
        BlockPos origin = BlockPos.containing(source.getPosition());
        ChunkPos centerChunk = new ChunkPos(origin);

        DungeonInstance closestInstance = null;
        double closestDistSq = Double.MAX_VALUE;

        if (extendedWorld != null) {
            int searchRadiusChunks = 512;
            List<DungeonInstance> nearby = extendedWorld.getNearbyDungeonInstances(centerChunk, searchRadiusChunks);

            if (nearby != null && !nearby.isEmpty()) {
                for (DungeonInstance instance : nearby) {
                    if (instance == null || instance.schematic == null || instance.schematic.name == null)
                        continue;
                    if (!instance.schematic.name.equalsIgnoreCase(targetName))
                        continue;
                    BlockPos dungeonOrigin = instance.getOrigin();
                    if (dungeonOrigin == null)
                        continue;
                    double distSq = dungeonOrigin.distSqr(origin);
                    if (distSq < closestDistSq) {
                        closestDistSq = distSq;
                        closestInstance = instance;
                    }
                }
            }
        }

        if (closestInstance != null) {
            BlockPos pos = closestInstance.getOrigin();
            double distance = Math.sqrt(closestDistSq);
            DungeonInstance finalClosest = closestInstance;
            source.sendSuccess(
                    () -> Component.literal(
                            "Nearest generated \"" + finalClosest.schematic.name + "\" at " +
                                    pos.getX() + ", " + pos.getY() + ", " + pos.getZ() +
                                    " (" + String.format("%.1f", distance) + " blocks away)"),
                    false);
            return 1;
        }

        DungeonSchematic schematic = DungeonManager.getInstance()
                .getSchematics()
                .stream()
                .filter(s -> s != null && s.name != null && s.name.equalsIgnoreCase(targetName))
                .findFirst()
                .orElse(null);

        if (schematic == null) {
            source.sendFailure(Component.literal("Unknown dungeon schematic: \"" + targetName + "\""));
            return 0;
        }

        BlockPos theoretical = StructureSpawnEvents.findNearestTheoreticalDungeon(level, schematic, origin, 128);
        if (theoretical == null) {
            source.sendFailure(Component
                    .literal("No valid theoretical location found for \"" + targetName + "\" within search radius."));
            return 0;
        }

        double distSqTheo = theoretical.distSqr(origin);
        double distanceTheo = Math.sqrt(distSqTheo);

        String tp = String.format("/tp %d %d %d", theoretical.getX(), theoretical.getY(), theoretical.getZ());

        Component coords = Component.literal(
                "[" + theoretical.getX() + ", " + theoretical.getY() + ", " + theoretical.getZ() + "]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tp))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click to teleport"))));

        Component message = Component.literal("Nearest " + schematic.name + " is at ")
                .append(coords)
                .append(Component.literal(" (" + String.format("%.0f", distanceTheo) + " blocks away)"));

        source.sendSuccess(() -> message, false);

        String nativeCmd = "/locate structure lycanitesmobs:" + schematic.name;
        source.sendSuccess(() -> Component.literal("Tip: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(nativeCmd).withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, nativeCmd))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click to use native locate"))))),
                false);

        return 1;
    }
}
