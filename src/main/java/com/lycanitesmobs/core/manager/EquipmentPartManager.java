package com.lycanitesmobs.core.manager;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lycanitesmobs.core.tabs.LMEquipmentPartsGroup.equipmentNames;

public class EquipmentPartManager extends JSONLoader {

    public static EquipmentPartManager INSTANCE;

    public Map<String, Lazy<ItemEquipmentPart>> equipmentParts = new HashMap<>();

    /**
     * A list of mod groups that have loaded with this Equipment Part Manager.
     **/
    public List<ModInfo> loadedGroups = new ArrayList<>();

    /**
     * Returns the main EquipmentPartManager INSTANCE or creates it and returns it.
     **/
    public static EquipmentPartManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EquipmentPartManager();
        }
        return INSTANCE;
    }

    /**
     * Loads all JSON Equipment Parts.
     **/
    public void loadAllFromJson(ModInfo modInfo) {
        if (!loadedGroups.contains(modInfo)) {
            loadedGroups.add(modInfo);
        }
        loadAllJson(modInfo, "Equipment", "equipment", "itemName", false, null, FileLoader.COMMON, StreamLoader.COMMON);
        LMHelperClass.logDebug("Equipment", "Complete! " + equipmentParts.size() + " JSON Equipment Parts Loaded In Total.");
    }

    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        Item.Properties properties = new Item.Properties().stacksTo(1).setNoRepair();
        Lazy<ItemEquipmentPart> equipmentPart = Lazy.of(() -> new ItemEquipmentPart(properties, json));
        String name = ItemEquipmentPart.getPartName(json);
        if (this.equipmentParts.containsKey(name)) {
            LMHelperClass.logWarning("", "[Equipment] Tried to add a Equipment Part with a name that is already in use: " + name);
            throw new RuntimeException("[Equipment] Tried to add a Equipment Part with a name that is already in use: " + name);
        }
        this.equipmentParts.put(name, equipmentPart);
        equipmentNames.add(name);
        ObjectManager.addItem(name, equipmentPart);
    }

    /**
     * Reloads all Equipment part JSON.
     */
    public void reload() {
        this.equipmentParts.clear();
        for (ModInfo group : this.loadedGroups) {
            this.loadAllFromJson(group);
        }
    }
}
