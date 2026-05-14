package com.lycanitesmobs.client.gui.screen.beastiary.lists;

import com.lycanitesmobs.client.gui.screen.beastiary.BeastiaryScreen;
import com.lycanitesmobs.client.gui.widgets.BaseListEntry;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class PetTypeList extends CreatureFilterList {

	/**
	 * Constructor
	 * @param width The width of the list.
	 * @param height The height of the list.
	 * @param top The y position that the list starts at.
	 * @param bottom The y position that the list stops at.
	 * @param x The x position of the list.
	 */
	public PetTypeList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x) {
		super(parentGui, width, height, top, bottom, x, 24);
		this.refreshList();
	}

	@Override
	public void refreshList() {
		this.replaceEntries(new ArrayList<>());

		int index = 0;
		this.addEntry(new Entry(this, index++, "gui.beastiary.pets"));
		this.addEntry(new Entry(this, index++, "gui.beastiary.mounts"));
		this.addEntry(new Entry(this, index++, "gui.beastiary.familiars"));
	}

	@Override
	public void setSelected(@Nullable BaseListEntry entry) {
		super.setSelected(entry);
		this.screen.playerExt.selectedPetType = this.getSelectedIndex();
		for(CreatureList creatureList : this.filteredLists) {
			if(creatureList != null) {
				this.updateCreatureListType(creatureList);
			}
		}
	}

	@Override
	protected boolean isSelectedItem(int index) {
		return this.screen.playerExt.selectedPetType == index;
	}

	/**
	 * Updates the list type of the provided Creature List to match this filter list.
	 * @param creatureList The Creature List to update.
	 */
	public void updateCreatureListType(CreatureList creatureList) {
		int index = this.getSelectedIndex();
		CreatureList.Type listType = null;
		if(index == 0) {
			listType = CreatureList.Type.PET;
		}
		else if(index == 1) {
			listType = CreatureList.Type.MOUNT;
		}
		else if(index == 2) {
			listType = CreatureList.Type.FAMILIAR;
		}
		creatureList.changeType(listType);
	}

	@Override
	public void addFilteredList(CreatureList creatureList) {
		super.addFilteredList(creatureList);
		this.updateCreatureListType(creatureList);
	}

	@Override
	public boolean canListCreature(CreatureInfo creatureInfo, CreatureList.Type listType) {
		int index = this.getSelectedIndex();
		if(creatureInfo == null || listType == null) {
			return false;
		}
		if(index == 0 && listType == CreatureList.Type.PET) {
			return true;
		}
		if(index == 1 && listType == CreatureList.Type.MOUNT) {
			return true;
		}
		if(index == 2 && listType == CreatureList.Type.FAMILIAR) {
			return true;
		}
		return false;
	}

	@Override
	public void renderSlot(GuiGraphics matrixStack, int index, int top, int left, int bottom, int right) {
	}

	/**
	 * List Entry
	 */
	public static class Entry extends CreatureFilterList.Entry {
		public String petTypeKey;

		public Entry(CreatureFilterList parentList, int index, String petTypeKey) {
			super(parentList, index);
			this.petTypeKey = petTypeKey;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean focus, float partialTicks) {
			this.parentList.screen.drawHelper.drawString(guiGraphics, Component.translatable(this.petTypeKey).getString(), left + 2 , top + 4, 0xFFFFFF);
		}

		
	}
}
