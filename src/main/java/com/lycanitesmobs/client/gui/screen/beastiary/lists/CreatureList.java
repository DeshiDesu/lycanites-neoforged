package com.lycanitesmobs.client.gui.screen.beastiary.lists;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.client.gui.screen.beastiary.BeastiaryScreen;
import com.lycanitesmobs.client.gui.widgets.BaseList;
import com.lycanitesmobs.client.gui.widgets.BaseListEntry;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.creature.Subspecies;
import com.lycanitesmobs.core.data.info.Variant;
import com.lycanitesmobs.core.network.message.MessageSummonSetSelection;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CreatureList extends BaseList<BeastiaryScreen> {
    public enum Type {
        KNOWLEDGE((byte) 0), SUMMONABLE((byte) 1), PET((byte) 2), MOUNT((byte) 3), FAMILIAR((byte) 4);
        public byte id;

        Type(byte i) {
            id = i;
        }
    }

    private Type listType;
    private CreatureFilterList filterList;
    private boolean releaseRefresh;

    /**
     * Constructor
     *
     * @param listType   The type of contents to show in this list.
     * @param screen     The Beastiary GUI using this list.
     * @param filterList A creature filter list to restrict this list by, if null every creature is listed.
     * @param width      The width of the list.
     * @param height     The height of the list.
     * @param top        The y position that the list starts at.
     * @param bottom     The y position that the list stops at.
     * @param x          The x position of the list.
     */
    public CreatureList(Type listType, BeastiaryScreen screen, CreatureFilterList filterList, int width, int height, int top, int bottom, int x) {
        super(screen, width, height, top, bottom, x, 24);
        this.listType = listType;
        this.filterList = filterList;
        if (this.filterList != null) {
            this.filterList.addFilteredList(this);
        }
        this.refreshList();
    }

    /**
     * Reloads all items in this list.
     */
    public void refreshList() {
        // Clear:
        this.replaceEntries(new ArrayList<>());
        int creatureIndex = 0;

        // Creature Knowledge List:
        if (this.listType == Type.KNOWLEDGE || this.listType == Type.SUMMONABLE) {
            List<String> creatures = new ArrayList<>(this.screen.playerExt.getBeastiary().creatureKnowledgeList.keySet());
            creatures.sort(Collator.getInstance(new Locale("US")));
            for (String creatureName : creatures) {
                CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(creatureName.toLowerCase());
                if (this.listType == Type.SUMMONABLE) {
                    if (!creatureInfo.isSummonable()) {
                        continue;
                    }
                    if (this.screen.playerExt.getBeastiary().getCreatureKnowledge(creatureName) == null || this.screen.playerExt.getBeastiary().getCreatureKnowledge(creatureName).rank < 2) {
                        continue;
                    }
                }
                if (creatureInfo != null && (this.filterList == null || this.filterList.canListCreature(creatureInfo, this.listType))) {
                    this.addEntry(new Entry(this, creatureIndex++, creatureInfo));
                }
            }
            return;
        }

        // Pet List:
        if (this.listType == Type.PET || this.listType == Type.MOUNT || this.listType == Type.FAMILIAR) {
            String petType = "pet";
            if (this.listType == Type.MOUNT) {
                petType = "mount";
            } else if (this.listType == Type.FAMILIAR) {
                petType = "familiar";
            }
            List<PetEntry> pets = this.screen.playerExt.petManager.createEntryListByType(petType);
            pets.sort(Comparator.comparing(PetEntry::getDisplayNameString));
            for (PetEntry petEntry : pets) {
                CreatureInfo creatureInfo = petEntry.getCreatureInfo();
                if (creatureInfo != null && (this.filterList == null || this.filterList.canListCreature(creatureInfo, this.listType))) {
                    this.addEntry(new Entry(this, creatureIndex++, petEntry));
                }
            }
        }
    }

    /**
     * Changes the type of creatures that this list should display. Also refreshes this list.
     *
     * @param listType The new list type to use.
     */
    public void changeType(Type listType) {
        this.listType = listType;
        this.refreshList();
    }

    @Override
    public void setSelected(@Nullable BaseListEntry entry) {
        super.setSelected(entry);
        if (!(entry instanceof Entry)) {
            return;
        }

        Entry creatureEntry = (Entry) entry;
        if (this.listType == Type.KNOWLEDGE) {
            this.screen.playerExt.selectedCreature = creatureEntry.creatureInfo;
            this.screen.playerExt.selectedSubspecies = 0;
            this.screen.playerExt.selectedVariant = 0;
        } else if (this.listType == Type.SUMMONABLE) {
            this.screen.playerExt.getSelectedSummonSet().setSummonType(creatureEntry.creatureInfo.getName());

            Subspecies subspecies = creatureEntry.creatureInfo.getSubspecies(this.screen.playerExt.getSelectedSummonSet().getSubspecies());
            this.screen.playerExt.getSelectedSummonSet().setSubspecies(subspecies.index);

            Variant variant = subspecies.getVariant(this.screen.playerExt.getSelectedSummonSet().getVariant());
            int variantId = variant != null ? variant.index : 0;
            if (!this.screen.playerExt.getBeastiary().hasKnowledgeRank(creatureEntry.creatureInfo.getName(), 2)) {
                variantId = 0;
            }
            this.screen.playerExt.getSelectedSummonSet().setVariant(variantId);

            this.screen.playerExt.sendSummonSetToServer((byte) this.screen.playerExt.selectedSummonSet);
            MessageSummonSetSelection message = new MessageSummonSetSelection(this.screen.playerExt);
            LycanitesMobs.PACKET_MANAGER.sendToServer(message);
        } else if (this.listType == Type.PET || this.listType == Type.MOUNT || this.listType == Type.FAMILIAR) {
            this.screen.playerExt.selectedPet = creatureEntry.petEntry;
            this.screen.playerExt.sendPetEntryToPlayer(this.screen.playerExt.selectedPet);
        }
    }

    @Override
    protected boolean isSelectedItem(int index) {
        if (!(this.getEntry(index) instanceof Entry)) {
            return false;
        }

        Entry creatureEntry = (Entry) this.getEntry(index);
        if (this.listType == Type.KNOWLEDGE) {
            return this.screen.playerExt.selectedCreature != null && this.screen.playerExt.selectedCreature.equals(creatureEntry.creatureInfo);
        } else if (this.listType == Type.SUMMONABLE) {
            return this.screen.playerExt.getSelectedSummonSet().getCreatureInfo() != null && this.screen.playerExt.getSelectedSummonSet().getCreatureInfo().equals(creatureEntry.creatureInfo);
        } else if (this.listType == Type.PET || this.listType == Type.MOUNT || this.listType == Type.FAMILIAR) {
            return this.screen.playerExt.selectedPet != null && this.screen.playerExt.selectedPet.equals(creatureEntry.petEntry);
        }
        return false;
    }

    @Override
    protected void renderBackground(GuiGraphics stack) {
        // Automatically Refresh:
        if (this.listType == Type.PET || this.listType == Type.MOUNT || this.listType == Type.FAMILIAR) {
            if (this.screen.playerExt.selectedPet != null && this.releaseRefresh != this.screen.playerExt.selectedPet.releaseEntity) {
                this.releaseRefresh = this.screen.playerExt.selectedPet.releaseEntity;
                this.refreshList();
            }
        }
    }

    /**
     * List Entry
     */
    public static class Entry extends BaseListEntry {
        private CreatureList parentList;
        public CreatureInfo creatureInfo;
        public PetEntry petEntry;

        public Entry(CreatureList parentList, int index, CreatureInfo creatureInfo) {
            this.parentList = parentList;
            this.index = index;
            this.creatureInfo = creatureInfo;
        }

        public Entry(CreatureList parentList, int index, PetEntry petEntry) {
            this.parentList = parentList;
            this.index = index;
            this.petEntry = petEntry;
        }

        @Override
        public void render(GuiGraphics matrixStack, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean focus, float partialTicks) {
            // Knowledge Slot:
            if (this.parentList.listType == Type.KNOWLEDGE || this.parentList.listType == Type.SUMMONABLE) {
                if (this.creatureInfo == null) {
                    return;
                }

                // Name:
                int nameY = top + 6;
                if (this.parentList.listType == Type.SUMMONABLE) {
                    nameY = top + 2;
                }
                this.parentList.screen.drawHelper.drawString(matrixStack, this.creatureInfo.getTitle().getString(), left + 20, nameY, 0xFFFFFF);

                // Level:
                if (this.parentList.listType == Type.SUMMONABLE) {
                    this.parentList.screen.drawLevel(matrixStack, this.creatureInfo, TextureManager.getTexture("GUIPetLevel"), left + 18, top + 10);
                }

                // Icon:
                if (this.creatureInfo.getIcon() != null) {
                    this.parentList.screen.drawHelper.drawTexture(matrixStack, this.creatureInfo.getIcon(), left + 2, top + 2, 0, 1, 1, 16, 16);
                }
            }

            // Pet Slot:
            else {
                if (this.petEntry == null) {
                    return;
                }

                // Name:
                int nameY = top + 6;
                if (this.parentList.listType == Type.PET || this.parentList.listType == Type.MOUNT) {
                    nameY = top + 2;
                }
                this.parentList.screen.drawHelper.drawString(matrixStack, this.petEntry.getDisplayName().getString(), left + 20, nameY, 0xFFFFFF);

                // Level:
                if (this.parentList.listType == Type.PET || this.parentList.listType == Type.MOUNT) {
                    this.parentList.screen.drawLevel(matrixStack, this.petEntry.getCreatureInfo(), TextureManager.getTexture("GUIPetLevel"), left + 18, top + 10);
                }

                // Icon:
                if (this.petEntry.getCreatureInfo().getIcon() != null) {
                    this.parentList.screen.drawHelper.drawTexture(matrixStack, this.petEntry.getCreatureInfo().getIcon(), left + 2, top + 2, 0, 1, 1, 16, 16);
                }
            }
        }

        private String trimText(String text, int maxWidth) {
            if (text == null) {
                return "";
            }
            if (maxWidth <= 0) {
                return "";
            }
            if (this.parentList.screen.drawHelper.getStringWidth(text) <= maxWidth) {
                return text;
            }

            String ellipsis = "...";
            int ellipsisWidth = this.parentList.screen.drawHelper.getStringWidth(ellipsis);
            StringBuilder builder = new StringBuilder(text);

            while (builder.length() > 0 && this.parentList.screen.drawHelper.getStringWidth(builder.toString()) + ellipsisWidth > maxWidth) {
                builder.setLength(builder.length() - 1);
            }

            builder.append(ellipsis);
            return builder.toString();
        }

        @Override
        protected void onClicked() {
            this.parentList.setSelected(this);
        }


    }
}
