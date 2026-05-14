package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.manager.CreatureManager;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CreaturesCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("creatures")
				.then(Commands.literal("reload").executes(CreaturesCommand::reload));
	}

	public static int reload(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		CreatureManager.getInstance().reload();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.creatures.reload"), true);
		return 0;
	}
}
