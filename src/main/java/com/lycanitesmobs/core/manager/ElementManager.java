package com.lycanitesmobs.core.manager;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads and manages all Element definitions.
 **/
public class ElementManager extends JSONLoader {
    public static ElementManager INSTANCE;

    /**
     * A list of all elements.
     **/
    public Map<String, ElementInfo> elements = new HashMap<>();


    /**
     * Returns the main Element Manager INSTANCE or creates it and returns it.
     **/
    public static ElementManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ElementManager();
        }
        return INSTANCE;
    }


    /**
     * Loads all JSON Elements. Should only be done on pre-init and before Creature Info is loaded.
     *
     * @param modInfo A mod info object that the json is loaded for.
     */
    public void loadAllFromJson(ModInfo modInfo) {
        this.elements.clear();
        this.loadAllJson(modInfo, "Element", "elements", "name", false, null, FileLoader.COMMON, StreamLoader.COMMON);
        for (ElementInfo elementInfo : this.elements.values()) {
            elementInfo.init();
        }
        LMHelperClass.logDebug("Element", "Complete! " + this.elements.size() + " JSON Elements Loaded In Total.");
    }


    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        ElementInfo elementInfo = new ElementInfo();
        elementInfo.loadFromJSON(json);
        if (elementInfo.name == null) {
            LMHelperClass.logWarning("", "Unable to load " + loadGroup + " json due to missing name.");
            return;
        }
        this.elements.put(elementInfo.name, elementInfo);
    }


    /**
     * Gets an element by name.
     *
     * @param elementName The name of the element to get.
     * @return The Element Info.
     */
    public ElementInfo getElement(String elementName) {
        if (!this.elements.containsKey(elementName))
            return null;
        return this.elements.get(elementName);
    }
}
