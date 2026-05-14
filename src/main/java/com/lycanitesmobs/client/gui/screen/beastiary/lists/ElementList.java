package com.lycanitesmobs.client.gui.screen.beastiary.lists;

import com.lycanitesmobs.client.gui.screen.beastiary.ElementsBeastiaryScreen;
import com.lycanitesmobs.client.gui.widgets.BaseList;
import com.lycanitesmobs.client.gui.widgets.BaseListEntry;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.manager.ElementManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ElementList extends BaseList<ElementsBeastiaryScreen> {

    /**
     * Constructor
     *
     * @param parentGui The Beastiary GUI using this list.
     * @param width     The width of the list.
     * @param height    The height of the list.
     * @param top       The y position that the list starts at.
     * @param bottom    The y position that the list stops at.
     * @param x         The x position of the list.
     */

    public ElementList(ElementsBeastiaryScreen parentGui, int width, int height, int top, int bottom, int x) {
        super(parentGui, width, height, top, bottom, x, 24);
    }

    @Override
    public void createEntries() {
        int index = 0;
        List<ElementInfo> elements = new ArrayList<>();
        elements.addAll(ElementManager.getInstance().elements.values());
        elements.sort(Comparator.comparing(ElementInfo::getName));
        for (ElementInfo elementInfo : elements) {
            this.addEntry(new Entry(this, index++, elementInfo.name));
        }
    }

    @Override
    public void setSelected(@Nullable BaseListEntry entry) {
        super.setSelected(entry);
        if (entry instanceof Entry)
            this.screen.elementInfo = ElementManager.getInstance().getElement(((Entry) entry).elementName);
    }

    /**
     * List Entry
     */
    public static class Entry extends BaseListEntry {
        private ElementList parentList;
        public String elementName;

        public Entry(ElementList parentList, int index, String elementName) {
            this.parentList = parentList;
            this.index = index;
            this.elementName = elementName;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int bottom, int right,
                           int mouseX, int mouseY, boolean focus, float partialTicks) {
            try {
                ElementInfo elementInfo = ElementManager.getInstance().getElement(this.elementName);
                if (elementInfo == null) {
                    return;
                }

                this.parentList.screen.drawHelper.drawString(
                        guiGraphics,
                        elementInfo.getTitle().getString(),
                        left + 4,
                        top + 4,
                        0xFFFFFF
                );
            } catch (Exception e) {
                LMHelperClass.logErrorMessageCatchable("Error rendering ElementList entry: " + this.elementName, e);
            }
        }


        @Override
        protected void onClicked() {
            this.parentList.setSelected(this);
        }


    }
}
