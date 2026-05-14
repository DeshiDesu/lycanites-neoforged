package com.lycanitesmobs.client.gui.screen.beastiary.lists;

import com.lycanitesmobs.client.gui.screen.beastiary.BeastiaryScreen;
import com.lycanitesmobs.client.gui.widgets.BaseList;
import com.lycanitesmobs.client.gui.widgets.BaseListEntry;
import com.lycanitesmobs.core.util.VersionChecker;
import net.minecraft.client.gui.GuiGraphics;

public class BeastiaryIndexList extends BaseList {
	protected VersionChecker.VersionInfo versionInfo;

	/**
	 * Constructor
	 * @param width The width of the list.
	 * @param height The height of the list.
	 * @param top The y position that the list starts at.
	 * @param bottom The y position that the list stops at.
	 * @param x The x position of the list.
	 */
	public BeastiaryIndexList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x, VersionChecker.VersionInfo versionInfo) {
		super(parentGui, width, height, top, bottom, x, parentGui.drawHelper.getWordWrappedHeight(versionInfo.getUpdateNotes(), width - 20));
		this.versionInfo = versionInfo;
	}

	@Override
	public void createEntries() {
		this.addEntry(new Entry(this));
	}

	/**
	 * List Entry
	 */
	public static class Entry extends BaseListEntry {
		private BeastiaryIndexList parentList;
		
		public Entry(BeastiaryIndexList parentList) {
			this.parentList = parentList;
		}

		@Override
		public void render(GuiGraphics matrixStack, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean focus, float partialTicks) {
			if(index == 0 && this.parentList.versionInfo != null) {
				this.parentList.drawHelper.drawStringWrapped(matrixStack, this.parentList.versionInfo.getUpdateNotes(), left + 6, top, this.parentList.getWidth() - 20, 0xFFFFFF, true);
			}
		}

		@Override
		protected void onClicked() {}


	}
}
