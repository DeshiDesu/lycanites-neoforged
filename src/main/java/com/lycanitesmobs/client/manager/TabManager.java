package com.lycanitesmobs.client.manager;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.buttons.Tab;
import com.lycanitesmobs.client.gui.screen.InventorySnooperScreen;
import com.lycanitesmobs.core.data.config.ConfigClient;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TabManager {
    public static ArrayList<Tab> tabList = new ArrayList<Tab>();

    public static void registerTab(Tab tab) {
        tabList.add(tab);
    }

    public static ArrayList<Tab> getTabList() {
        return tabList;
    }

    public static void addTabsToInventory(Screen gui) {
        if (ConfigClient.INSTANCE.inventoryTab.get()) {
            InventorySnooperScreen inventorySnooperScreen = new InventorySnooperScreen(mc.player);
            try {
                Field field = Screen.class.getDeclaredField(inventorySnooperScreen.getButtonListFieldName());
                field.setAccessible(true);
                List buttonList = (List) field.get(gui);
                addTabsToList(buttonList);
                field.set(gui, buttonList);
            } catch (Exception e) {
                LMHelperClass.logWarning("", "A problem occured when adding custom inventory tabs:");
                e.printStackTrace();
            }
        }
    }

    private static Minecraft mc = Minecraft.getInstance();

    public static void openInventoryGui() {
        InventoryScreen inventory = new InventoryScreen(mc.player);
        mc.setScreen(inventory);
        TabManager.addTabsToInventory(inventory);
    }

    public static void updateTabValues(int cornerX, int cornerY, Class<?> selectedButton) {
        int count = 2;
        for (int i = 0; i < tabList.size(); i++) {
            Tab t = tabList.get(i);

            if (t.shouldAddToList()) {
                t.buttonId = (byte) count;
                t.setX(cornerX + (count - 2) * 28);
                t.setY(cornerY - 28);
                t.active = !t.getClass().equals(selectedButton);
                count++;
            }
        }
    }

    public static void addTabsToList(List buttonList) {
        for (Tab tab : tabList) {
            if (tab.shouldAddToList()) {
                buttonList.add(tab);
            }
        }
    }
}
