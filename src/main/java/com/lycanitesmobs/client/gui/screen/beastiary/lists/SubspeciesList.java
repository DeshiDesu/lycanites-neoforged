package com.lycanitesmobs.client.gui.screen.beastiary.lists;

import com.lycanitesmobs.client.gui.screen.beastiary.BeastiaryScreen;
import com.lycanitesmobs.client.gui.widgets.BaseList;
import com.lycanitesmobs.client.gui.widgets.BaseListEntry;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.creature.Subspecies;
import com.lycanitesmobs.core.data.info.Variant;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class SubspeciesList extends BaseList<BeastiaryScreen> {
	private CreatureInfo creature;
	private boolean summoning;

	/**
	 * Constructor
	 * @param screen The Beastiary GUI using this list.
	 * @param width The width of the list.
	 * @param height The height of the list.
	 * @param top The y position that the list starts at.
	 * @param bottom The y position that the list stops at.
	 * @param x The x position of the list.
	 */
	public SubspeciesList(BeastiaryScreen screen, boolean summoning, int width, int height, int top, int bottom, int x) {
		super(screen, width, height, top, bottom, x, 24);
		this.summoning = summoning;
		this.refreshList();
	}

	/**
	 * Reloads all items in this list.
	 */
	public void refreshList() {
		this.replaceEntries(new ArrayList<>());

		if(!this.summoning) {
			this.creature = this.screen.playerExt.selectedCreature;
		}
		else {
			this.creature = this.screen.playerExt.getSelectedSummonSet().getCreatureInfo();
		}
		if(this.creature == null) {
			return;
		}

		int index = 0;
		for(Subspecies subspecies : this.creature.subspecies.values()) {
			this.addEntry(new Entry(this, index++, subspecies.index, 0));
			for (int variantIndex : subspecies.variants.keySet()) {
				if(!this.screen.playerExt.getBeastiary().hasKnowledgeRank(this.creature.getName(), 2)) {
					continue;
				}
				Variant variant = subspecies.getVariant(variantIndex);
				if(variant == null) {
					continue;
				}
				if (this.summoning && "rare".equals(variant.rarity)) {
					continue;
				}
				this.addEntry(new Entry(this, index++, subspecies.index, variant.index));
			}
		}
	}

	@Override
	public void setSelected(@Nullable BaseListEntry entry) {
		super.setSelected(entry);
		if(!(entry instanceof Entry)) {
			return;
		}
		if(!this.summoning) {
			this.screen.playerExt.selectedSubspecies = ((Entry)entry).subspeciesIndex;
			this.screen.playerExt.selectedVariant = ((Entry)entry).variantIndex;
		}
		else {
			this.screen.playerExt.getSelectedSummonSet().setSubspecies(((Entry)entry).subspeciesIndex);
			this.screen.playerExt.getSelectedSummonSet().setVariant(((Entry)entry).variantIndex);
			this.screen.playerExt.sendSummonSetToServer((byte)this.screen.playerExt.selectedSummonSet);
		}
	}

	@Override
	protected boolean isSelectedItem(int index) {
		if(!(this.getEntry(index) instanceof Entry))
			return false;
		if(!this.summoning) {
			return this.screen.playerExt.selectedSubspecies == ((Entry)this.getEntry(index)).subspeciesIndex &&
					this.screen.playerExt.selectedVariant == ((Entry)this.getEntry(index)).variantIndex;
		}
		else {
			return this.screen.playerExt.getSelectedSummonSet().getSubspecies() == ((Entry)this.getEntry(index)).subspeciesIndex &&
					this.screen.playerExt.getSelectedSummonSet().getVariant() == ((Entry)this.getEntry(index)).variantIndex;
		}
	}

	@Override
	protected void renderBackground(GuiGraphics stack) {
		if(!this.summoning) {
			if(this.creature != this.screen.playerExt.selectedCreature) {
				this.refreshList();
			}
		}
		else {
			if(this.creature != this.screen.playerExt.getSelectedSummonSet().getCreatureInfo()) {
				this.refreshList();
			}
		}

	}

	/**
	 * List Entry
	 */
	public static class Entry extends BaseListEntry {
		private SubspeciesList parentList;
		public int subspeciesIndex;
		public int variantIndex;

		public Entry(SubspeciesList parentList, int index, int subspeciesIndex, int variantIndex) {
			this.parentList = parentList;
			this.index = index;
			this.subspeciesIndex = subspeciesIndex;
			this.variantIndex = variantIndex;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean focus, float partialTicks) {
			Subspecies subspecies = this.parentList.creature.getSubspecies(this.subspeciesIndex);
			MutableComponent subspeciesName = Component.literal("");
			if(subspecies.name != null) {
				subspeciesName = Component.literal(" ").append(subspecies.getTitle());
			}

			Variant variant = subspecies.getVariant(this.variantIndex);
			MutableComponent variantName = Component.literal("Normal");
			if(variant != null) {
				variantName = variant.getTitle();
			}

			int nameY = top + 6;
			this.parentList.screen.drawHelper.drawString(guiGraphics, variantName.append(subspeciesName).getString(), left + 10, nameY, 0xFFFFFF);
		}

		@Override
		protected void onClicked() {
			this.parentList.setSelected(this);
		}

		
	}
}
