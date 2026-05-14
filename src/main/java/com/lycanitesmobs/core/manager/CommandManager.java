package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.core.command.*;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandManager {
    public static CommandManager INSTANCE;

    public static CommandManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommandManager();
        }
        return INSTANCE;
    }


}
