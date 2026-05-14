package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.event.MobEventListener;
import com.lycanitesmobs.core.manager.MobEventManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MobEventCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("mobevent")
				.then(Commands.literal("start")
						.then(Commands.argument("mobevent", StringArgumentType.string())
								.then(Commands.argument("level", IntegerArgumentType.integer())
										.executes(MobEventCommand::start)
										.then(Commands.argument("subspecies", IntegerArgumentType.integer())
												.executes(MobEventCommand::startSubspecies))
													.then(Commands.argument("world", IntegerArgumentType.integer())
															.executes(MobEventCommand::startWorld))
				)))
				.then(Commands.literal("random")
							.then(Commands.argument("level", IntegerArgumentType.integer())
									.executes(MobEventCommand::random)
										.then(Commands.argument("subspecies", IntegerArgumentType.integer())
												.executes(MobEventCommand::startWorld))
													.then(Commands.argument("world", IntegerArgumentType.integer())
															.executes(MobEventCommand::randomWorld))
							))
				.then(Commands.literal("stop")
						.executes(MobEventCommand::stop)
							.then(Commands.argument("world", IntegerArgumentType.integer())
									.executes(MobEventCommand::stopWorld)));
	}

	public static int start(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		Level world = context.getSource().getLevel();
		String eventName = StringArgumentType.getString(context, "mobevent");
		int level = Math.max(1, IntegerArgumentType.getInteger(context, "level"));

		if(!MobEventManager.getInstance().mobEvents.containsKey(eventName)) {
			context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.start.unknown"), true);
			return 0;
		}

		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		if(extendedWorld == null) {
			return 0;
		}

		Player player = null;
		BlockPos pos = new BlockPos(0, 0, 0);

		if(context.getSource().getEntity() instanceof Player) {
			player = (Player)context.getSource().getEntity();
			pos = new BlockPos(player.blockPosition());

			// Check Conditions:
			MobEvent mobEvent = MobEventManager.getInstance().getMobEvent(eventName);
			if (!mobEvent.canStart(world, player)) {
				context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.start.conditions"), true);
				return 0;
			}

		}

		extendedWorld.startMobEvent(eventName, player, pos, level, -1);
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.start"), true);
		return 0;
	}

	public static int startSubspecies(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		Level world = context.getSource().getLevel();
		String eventName = StringArgumentType.getString(context, "mobevent");
		int level = Math.max(1, IntegerArgumentType.getInteger(context, "level"));
		int subspecies = Math.max(-1, IntegerArgumentType.getInteger(context, "subspecies"));

		if(!MobEventManager.getInstance().mobEvents.containsKey(eventName)) {
			context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.start.unknown"), true);
			return 0;
		}

		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		if(extendedWorld == null) {
			return 0;
		}

		Player player = null;
		BlockPos pos = new BlockPos(0, 0, 0);

		if(context.getSource().getEntity() instanceof Player) {
			player = (Player)context.getSource().getEntity();
			pos = new BlockPos(player.blockPosition());

			// Check Conditions:
			MobEvent mobEvent = MobEventManager.getInstance().getMobEvent(eventName);
			if (!mobEvent.canStart(world, player)) {
				context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.start.conditions"), true);
				return 0;
			}

		}

		extendedWorld.startMobEvent(eventName, player, pos, level, subspecies);
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.start"), true);
		return 0;
	}

	public static int startWorld(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		String eventName = StringArgumentType.getString(context, "mobevent");
		int level = Math.max(1, IntegerArgumentType.getInteger(context, "level"));
		int subspecies = Math.max(-1, IntegerArgumentType.getInteger(context, "subspecies"));
		int worldId = IntegerArgumentType.getInteger(context, "world");

		if(!MobEventManager.getInstance().mobEvents.containsKey(eventName)) {
			context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.start.unknown"), true);
			return 0;
		}

//		World world = DimensionManager.getWorld(context.getSource().getServer(), DimensionType.getById(worldId), false, false); TODO Get world from id instead!
		Level world = context.getSource().getLevel();
		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		if(extendedWorld == null) {
			return 0;
		}

		Player player = null;
		BlockPos pos = new BlockPos(0, 0, 0);

		if(context.getSource().getEntity() instanceof Player) {
			player = (Player)context.getSource().getEntity();
			pos = new BlockPos(player.blockPosition());

			// Check Conditions:
			MobEvent mobEvent = MobEventManager.getInstance().getMobEvent(eventName);
			if (!mobEvent.canStart(world, player)) {
				context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.start.conditions"), true);
				return 0;
			}

		}

		extendedWorld.startMobEvent(eventName, player, pos, level, subspecies);
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.start"), true);
		return 0;
	}

	public static int random(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		Level world = context.getSource().getLevel();
		int level = Math.max(1, IntegerArgumentType.getInteger(context, "level"));

		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		if(extendedWorld == null) {
			return 0;
		}
		extendedWorld.stopWorldEvent();
		MobEventListener.getInstance().triggerRandomMobEvent(world, extendedWorld, level);
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.random"), true);
		return 0;
	}

	public static int randomWorld(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		int level = Math.max(1, IntegerArgumentType.getInteger(context, "level"));
		int worldId = IntegerArgumentType.getInteger(context, "world");

//		World world = DimensionManager.getWorld(context.getSource().getServer(), DimensionType.getById(worldId), false, false); TODO Get world from id instead!
		Level world = context.getSource().getLevel();
		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		if(extendedWorld == null) {
			return 0;
		}
		extendedWorld.stopWorldEvent();
		MobEventListener.getInstance().triggerRandomMobEvent(world, extendedWorld, level);
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.random"), true);
		return 0;
	}

	public static int stop(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		Level world = context.getSource().getLevel();
		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		if(extendedWorld == null) {
			return 0;
		}
		extendedWorld.stopWorldEvent();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.stop"), true);
		return 0;
	}

	public static int stopWorld(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		int worldId = IntegerArgumentType.getInteger(context, "world");
//		World world = DimensionManager.getWorld(context.getSource().getServer(), DimensionType.getById(worldId), false, false); TODO Get world from id instead!
		Level world = context.getSource().getLevel();
		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		if(extendedWorld == null) {
			return 0;
		}
		extendedWorld.stopWorldEvent();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevent.stop"), true);
		return 0;
	}
}
