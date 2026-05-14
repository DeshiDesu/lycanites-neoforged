package com.lycanitesmobs.client.manager;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.ModInfo;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {
	private static TextureManager INSTANCE;
	public static TextureManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new TextureManager();
		}
		return INSTANCE;
	}
	
	// Maps:
	public static Map<String, ResourceLocation> textures = new HashMap<>();
	public static Map<String, ResourceLocation[]> textureGroups = new HashMap<>();

	/**
	 * Registers GUI and misc textures.
	 */
	public void createTextures(ModInfo modInfo) {
		// Beastiary:
		addTexture("GUIBeastiaryBackground", "textures/guis/beastiary/background.png");
		addTexture("GUIPetLevel", "textures/guis/beastiary/level.png");
		addTexture("GUIPetSpirit", "textures/guis/beastiary/spirit.png");
		addTexture("GUIPetSpiritEmpty", "textures/guis/beastiary/spirit_empty.png");
		addTexture("GUIPetSpiritUsed", "textures/guis/beastiary/spirit_used.png");
		addTexture("GUIPetSpiritFilling", "textures/guis/beastiary/spirit_filling.png");
		addTexture("GUIPetBarHealth", "textures/guis/beastiary/bar_health.png");
		addTexture("GUIPetBarRespawn", "textures/guis/beastiary/bar_respawn.png");
		addTexture("GUIBarExperience", "textures/guis/beastiary/bar_experience.png");
		addTexture("GUIPetBarEmpty", "textures/guis/beastiary/bar_empty.png");

		// Containers:
		addTexture("GUIInventoryCreature", "textures/guis/inventory_creature.png");
		addTexture("GUISummoningPedestal", "textures/guis/minion_lg.png");
		addTexture("GUIEquipmentForge", "textures/guis/equipmentforge.png");
	}

	public static void addTexture(String name, String path) {
		name = name.toLowerCase();
		textures.put(name, new ResourceLocation(LycanitesMobs.MODID, path));
	}

	public static ResourceLocation getTexture(String name) {
		name = name.toLowerCase();
		if(!textures.containsKey(name))
			return null;
		return textures.get(name);
	}
}
