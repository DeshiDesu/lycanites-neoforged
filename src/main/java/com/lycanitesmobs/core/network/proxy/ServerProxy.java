package com.lycanitesmobs.core.network.proxy;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ServerProxy implements IProxy {
    @Override
    public void registerEvents() {

    }

    @Override
    public Level getWorld() {
        return null;
    }

    @Override
    public void addEntityToWorld(int entityId, Entity entity) {

    }

    @Override
    public Player getClientPlayer() {
        return null;
    }

    @Override
    public void openScreen(int screenId, Player player) {
    }
}
