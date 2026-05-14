package com.lycanitesmobs.client.manager;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class KeyManager {
    public static KeyManager instance = new KeyManager();
    public Minecraft mc;

    public boolean inventoryOpen = false;

    public static KeyMapping dismount = new KeyMapping("key.mount.dismount", GLFW.GLFW_KEY_V, "Lycanites Mobs");
    public static KeyMapping descend = new KeyMapping("key.mount.descend", GLFW.GLFW_KEY_LEFT_ALT, "Lycanites Mobs");
    public static KeyMapping mountAbility = new KeyMapping("key.mount.ability", GLFW.GLFW_KEY_G, "Lycanites Mobs");
    public static KeyMapping mountInventory = new KeyMapping("key.mount.inventory", GLFW.GLFW_KEY_K, "Lycanites Mobs");

    public static KeyMapping beastiary = new KeyMapping("key.beastiary", GLFW.GLFW_KEY_UNKNOWN, "Lycanites Mobs");
    public static KeyMapping index = new KeyMapping("key.index", GLFW.GLFW_KEY_B, "Lycanites Mobs");
    public static KeyMapping pets = new KeyMapping("key.pets", GLFW.GLFW_KEY_UNKNOWN, "Lycanites Mobs");
    public static KeyMapping summoning = new KeyMapping("key.summoning", GLFW.GLFW_KEY_UNKNOWN, "Lycanites Mobs");
    public static KeyMapping minionSelection = new KeyMapping("key.minions", GLFW.GLFW_KEY_R, "Lycanites Mobs");

    public KeyManager() {
        this.mc = Minecraft.getInstance();
    }

    public static KeyManager getInstance() {
        return instance;
    }


}
