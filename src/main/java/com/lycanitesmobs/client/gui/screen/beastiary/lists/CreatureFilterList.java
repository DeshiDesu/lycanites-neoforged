package com.lycanitesmobs.client.gui.screen.beastiary.lists;

import com.lycanitesmobs.client.gui.screen.beastiary.BeastiaryScreen;
import com.lycanitesmobs.client.gui.widgets.BaseList;
import com.lycanitesmobs.client.gui.widgets.BaseListEntry;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CreatureFilterList extends BaseList<BeastiaryScreen> {
	protected List<CreatureList> filteredLists = new ArrayList<>();

	/**
	 * Constructor
	 * @param width The width of the list.
	 * @param height The height of the list.
	 * @param top The y position that the list starts at.
	 * @param bottom The y position that the list stops at.
	 * @param x The x position of the list.
	 */
	public CreatureFilterList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x, int slotHeight) {
		super(parentGui, width, height, top, bottom, x, slotHeight);
	}

	/**
	 * Reloads all items in this list. Used in place of createEntries
	 */
	public abstract void refreshList();

	@Override
	public void setSelected(@Nullable BaseListEntry entry) {
		super.setSelected(entry);
		for(CreatureList creatureList : this.filteredLists) {
			if(creatureList != null) {
				creatureList.refreshList();
			}
		}
	}

	/**
	 * Adds a Creature List as a list that should be filtered by this filter list.
	 * @param creatureList The Creature List to add and refresh as this filter list changes.
	 */
	public void addFilteredList(CreatureList creatureList) {
		if(!this.filteredLists.contains(creatureList)) {
			this.filteredLists.add(creatureList);
		}
	}

	/**
	 * Returns if this filter list allows the provided Creature Info to be added to the display list.
	 * @param creatureInfo The Creature info to display.
	 * @param listType The type of Creature List.
	 * @return True if the Creature Info should be included.
	 */
	public boolean canListCreature(CreatureInfo creatureInfo, CreatureList.Type listType) {
		return true;
	}

	/**
	 * Renders a slot based on the provided entry.
	 * @param index The entry index.
	 */
	public void renderSlot(GuiGraphics guiGraphics, int index, int top, int left, int bottom, int right) {}

	/**
	 * List Entry
	 */
	public abstract static class Entry extends BaseListEntry {
		protected CreatureFilterList parentList;

		public Entry(CreatureFilterList parentList, int index) {
			this.parentList = parentList;
			this.index = index;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
			this.parentList.renderSlot(guiGraphics, index, top, left, bottom, right);
		}

		@Override
		protected void onClicked() {
			this.parentList.setSelected(this);
		}
	}
}
