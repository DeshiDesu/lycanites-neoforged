package com.lycanitesmobs.core.network.proxy;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IProxy {
    void registerEvents();

    Level getWorld();

    void addEntityToWorld(int entityId, Entity entity);

    public Player getClientPlayer();

    public void openScreen(int screenId, Player player);
}
