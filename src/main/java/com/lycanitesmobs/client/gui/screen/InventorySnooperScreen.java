package com.lycanitesmobs.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A sneaky snooping class that taps into GuiInventory to try and grab stuff from those stupid protected fields!
 *
 */
public class InventorySnooperScreen extends InventoryScreen {

	public InventorySnooperScreen(Player entityPlayer) {
		super(entityPlayer);
	}
	
	/**
	 * This agent's missing is to infiltrate the buttonList field by comparing it to it's own.
	 * @return The name of the buttonList field before or after obfuscation.
	 */
	public String getButtonListFieldName() {
		try {
			for(Field field : Screen.class.getDeclaredFields()) {
				field.setAccessible(true);
				Object object = field.get(this);
				if(object instanceof List)
					if(object == this.renderables)
						return field.getName();
			}
		} catch(Exception e) {}
		return "";
	}
	
	public int getGUIXSize() {
		return this.imageWidth;
	}
	
	public int getGUIYSize() {
		return this.imageHeight;
	}

}
