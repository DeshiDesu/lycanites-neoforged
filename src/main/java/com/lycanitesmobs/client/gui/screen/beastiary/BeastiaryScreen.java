package com.lycanitesmobs.client.gui.screen.beastiary;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.client.gui.screen.base.BaseGui;
import com.lycanitesmobs.client.gui.screen.base.BaseScreen;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class BeastiaryScreen extends BaseScreen {
    public enum Page {
        INDEX((byte) 0), CREATURES((byte) 1), PETS((byte) 2), SUMMONING((byte) 3), ELEMENTS((byte) 4);
        public byte id;

        Page(byte i) {
            id = i;
        }
    }

    /**
     * A snapshot of the users GUI Scale setting so it can be restored on closing the Beastiary.
     **/
    static int OPENED_GUI_SCALE;
    /**
     * Set to true when any Beastiary GUI is active in order to prevent the GUI Scaling going out of sync.
     **/
    static boolean GUI_ACTIVE;

    public Minecraft mc;

    public Player player;
    public ExtendedPlayer playerExt;
    public LivingEntity creaturePreviewEntity;
    public float creaturePreviewTicks = 0;

    public int centerX;
    public int centerY;
    public int windowWidth;
    public int windowHeight;
    public int halfX;
    public int halfY;
    public int windowX;
    public int windowY;


    public int colLeftX;
    public int colLeftY;
    public int colLeftWidth;
    public int colLeftHeight;
    public int colLeftCenterX;
    public int colLeftCenterY;

    public int colRightX;
    public int colRightY;
    public int colRightWidth;
    public int colRightHeight;
    public int colRightCenterX;
    public int colRightCenterY;


    /**
     * Constructor
     *
     * @param player The player to create the GUI instance for.
     */
    public BeastiaryScreen(Player player) {
        super(Component.translatable("gui.beastiary.name"));
        this.player = player;
        this.playerExt = ExtendedPlayer.getForPlayer(player);
        this.mc = Minecraft.getInstance();
		/*if(this.mc.gameSettings.guiScale != 2 || GUI_ACTIVE) {
			OPENED_GUI_SCALE = this.mc.gameSettings.guiScale;
			this.mc.mainWindow.calculateScale(this.mc.gameSettings.guiScale, this.mc.getForceUnicodeFont());
		}
		else {
			GUI_ACTIVE = true;
		}*/
    }

    @Override
    public void removed() {
		/*if(this.mc.gameSettings.guiScale == 2 && !GUI_ACTIVE) {
			this.mc.gameSettings.guiScale = OPENED_GUI_SCALE;
			this.mc.mainWindow.calculateScale(this.mc.gameSettings.guiScale, this.mc.getForceUnicodeFont());
		}
		GUI_ACTIVE = false;*/
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        // 1) Make sure scaledResolution is set
        if (this.scaledResolution == null) {
            this.scaledResolution = this.mc.getWindow();
        }

        this.zLevel = -1000F;

        // 2) Compute geometry first
        this.windowWidth = this.getScaledX(0.95F);
        this.windowHeight = this.getScaledY(0.95F);
        this.halfX = this.windowWidth / 2;
        this.halfY = this.windowHeight / 2;
        this.windowX = (this.width / 2) - (this.windowWidth / 2);
        this.windowY = (this.height / 2) - (this.windowHeight / 2);
        this.centerX = this.windowX + (this.windowWidth / 2);
        this.centerY = this.windowY + (this.windowHeight / 2);

        // columns...
        this.colLeftX = this.windowX + this.getScaledX(80F / 1920F);
        this.colLeftY = this.windowY + this.getScaledY(460F / 1080F);
        this.colLeftWidth = this.getScaledX(320F / 1920F);
        this.colLeftHeight = this.getScaledX(380F / 1080F);
        this.colLeftCenterX = this.colLeftX + Math.round(this.colLeftWidth / 2);
        this.colLeftCenterY = this.colLeftY + Math.round(this.colLeftHeight / 2);

        this.colRightX = this.windowX + this.getScaledX(480F / 1920F);
        this.colRightY = this.windowY + this.getScaledY(420F / 1080F);
        this.colRightWidth = this.getScaledX(1260F / 1920F);
        this.colRightHeight = this.getScaledX(400F / 1080F);
        this.colRightCenterX = this.colRightX + Math.round(this.colRightWidth / 2);
        this.colRightCenterY = this.colRightY + Math.round(this.colRightHeight / 2);

        // 3) Now let BaseScreen set drawHelper and call initWidgets()
        super.init();
    }


    @Override
    protected void initWidgets() {
        int menuPadding = 6;
        int menuX = this.centerX - Math.round((float) this.windowWidth / 2) + menuPadding;
        int menuY = this.windowY + menuPadding;
        int menuWidth = this.windowWidth - (menuPadding * 2);

        int buttonCount = 5;
        int buttonPadding = 2;
        int buttonX = menuX + buttonPadding;
        int buttonWidth = Math.round((float) (menuWidth / buttonCount)) - (buttonPadding * 2);
        int buttonWidthPadded = buttonWidth + (buttonPadding * 2);
        int buttonHeight = 20;
        ButtonBase button;

        // Top Menu:
        button = new ButtonBase(Page.INDEX.id, buttonX + (buttonWidthPadded * this.renderables.size()), menuY, buttonWidth, buttonHeight, Component.translatable("gui.beastiary.index.title"), this);
        this.addRenderableWidget(button);
        button = new ButtonBase(Page.CREATURES.id, buttonX + (buttonWidthPadded * this.renderables.size()), menuY, buttonWidth, buttonHeight, Component.translatable("gui.beastiary.creatures"), this);
        this.addRenderableWidget(button);
        button = new ButtonBase(Page.PETS.id, buttonX + (buttonWidthPadded * this.renderables.size()), menuY, buttonWidth, buttonHeight, Component.translatable("gui.beastiary.pets"), this);
        this.addRenderableWidget(button);
        button = new ButtonBase(Page.SUMMONING.id, buttonX + (buttonWidthPadded * this.renderables.size()), menuY, buttonWidth, buttonHeight, Component.translatable("gui.beastiary.summoning"), this);
        this.addRenderableWidget(button);
        button = new ButtonBase(Page.ELEMENTS.id, buttonX + (buttonWidthPadded * this.renderables.size()), menuY, buttonWidth, buttonHeight, Component.translatable("gui.beastiary.elements"), this);
        this.addRenderableWidget(button);
    }

    @Override
    public void renderBackground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIBeastiaryBackground"), this.windowX, this.windowY, this.zLevel, 1, 1, this.windowWidth, this.windowHeight);
    }

    @Override
    public void renderForeground(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        MutableComponent title = Component.literal("\u00A7l\u00A7n").append(this.getTitle());
        float width = this.drawHelper.getStringWidth(title.getString());
        this.drawHelper.drawString(matrixStack, title.getString(), this.colRightCenterX - Math.round(width / 2), this.colRightY, 0xFFFFFF, true);
    }

    /**
     * Called when a GUI button is interacted with.
     *
     * @param buttonId The button that was interacted with.
     */
    @Override
    public void actionPerformed(int buttonId) {
        if (buttonId == Page.INDEX.id) {
            this.mc.setScreen(new IndexBeastiaryScreen(Minecraft.getInstance().player));
        }
        if (buttonId == Page.CREATURES.id) {
            this.mc.setScreen(new CreaturesBeastiaryScreen(Minecraft.getInstance().player));
        }
        if (buttonId == Page.PETS.id) {
            this.mc.setScreen(new PetsBeastiaryScreen(Minecraft.getInstance().player));
        }
        if (buttonId == Page.SUMMONING.id) {
            this.mc.setScreen(new SummoningBeastiaryScreen(Minecraft.getInstance().player));
        }
        if (buttonId == Page.ELEMENTS.id) {
            this.mc.setScreen(new ElementsBeastiaryScreen(Minecraft.getInstance().player));
        }
    }

    /**
     * Called when a key is pressed.
     */
    @Override
    public boolean keyReleased(int keyCode, int keyCodeB, int keyCodeC) {
        if (keyCode == 1 || keyCode == this.mc.options.keyInventory.getKey().getValue()) {
            this.mc.player.closeContainer();
        }
        return super.keyReleased(keyCode, keyCodeB, keyCodeC);
    }

    /**
     * Returns the title of this Beastiary Page.
     *
     * @return The title text string to display.
     */
    public MutableComponent getTitle() {
        return Component.translatable("gui.beastiary.name");
    }

    /**
     * Draws a level bar for the provided creature info.
     *
     * @param matrixStack  The matrix stack to draw with.
     * @param creatureInfo The creature info to get stats from.
     * @param texture      The texture to use as a level dot/star.
     * @param x            The x position to draw from.
     * @param y            The y position to draw from.
     */
    public void drawLevel(GuiGraphics matrixStack, CreatureInfo creatureInfo, ResourceLocation texture, int x, int y) {
        int level = creatureInfo.summonCost;
        if (level <= 10) {
            this.drawHelper.drawBar(matrixStack, texture, x, y, 0, 9, 9, level, 10);
        }
    }

    public void renderCreature(GuiGraphics matrixStack, CreatureInfo creatureInfo, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (creatureInfo == null) {
            this.creaturePreviewEntity = null;
            return;
        }

        try {
            boolean subspeciesMatch = true;
            if (this.creaturePreviewEntity instanceof BaseCreatureEntity) {
                subspeciesMatch = ((BaseCreatureEntity) this.creaturePreviewEntity).getSubspeciesIndex() == this.getDisplaySubspecies(creatureInfo);
                if (subspeciesMatch) {
                    subspeciesMatch = ((BaseCreatureEntity) this.creaturePreviewEntity).getVariantIndex() == this.getDisplayVariant(creatureInfo);
                }
            }

            if (this.creaturePreviewEntity == null || this.creaturePreviewEntity.getClass() != creatureInfo.entityClass || !subspeciesMatch) {
                this.creaturePreviewEntity = creatureInfo.createEntity(this.player.getCommandSenderWorld());
                this.creaturePreviewEntity.setOnGround(true);
                if (this.creaturePreviewEntity instanceof BaseCreatureEntity) {
                    ((BaseCreatureEntity) this.creaturePreviewEntity).setSubspecies(this.getDisplaySubspecies(creatureInfo));
                    ((BaseCreatureEntity) this.creaturePreviewEntity).setVariant(this.getDisplayVariant(creatureInfo));
                }
                if (this.creaturePreviewEntity instanceof AgeableCreatureEntity) {
                    ((AgeableCreatureEntity) this.creaturePreviewEntity).setGrowingAge(0);
                }
                this.onCreateDisplayEntity(creatureInfo, this.creaturePreviewEntity);
                this.playCreatureSelectSound(creatureInfo);
            }

            if (this.creaturePreviewEntity != null) {
                int creatureSize = Math.round((float) this.windowHeight / 6);
                double creatureWidth = creatureInfo.width;
                double creatureHeight = creatureInfo.height;
                int scale = (int) Math.round((2.5F / Math.max(creatureWidth, creatureHeight)) * creatureSize);
                int posX = x;
                int posY = y + 80;
                float lookX = (float) posX - mouseX;
                float lookY = (float) posY - mouseY;
                this.creaturePreviewTicks += partialTicks;
                if (this.creaturePreviewEntity instanceof BaseCreatureEntity) {
                    BaseCreatureEntity previewCreatureBase = (BaseCreatureEntity) this.creaturePreviewEntity;
                    previewCreatureBase.onlyRenderTicks = this.creaturePreviewTicks;
                }

                matrixStack.pose().pushPose();
                matrixStack.pose().translate(0, 0, -1000);
                BaseGui.renderLivingEntity(matrixStack, posX, posY, scale, lookX, lookY, this.creaturePreviewEntity);
                matrixStack.pose().popPose();
            }
        } catch (Exception e) {
            LMHelperClass.logWarning("", "An exception occurred when trying to preview a creature in the Beastiary.");
            e.printStackTrace();
        }
    }


    /**
     * Gets the Subspecies to use for the display creature.
     *
     * @param creatureInfo The Creature Info being displayed.
     */
    public int getDisplaySubspecies(CreatureInfo creatureInfo) {
        return this.playerExt.selectedSubspecies;
    }


    /**
     * Gets the Variant to use for the display creature.
     *
     * @param creatureInfo The Creature Info being displayed.
     */
    public int getDisplayVariant(CreatureInfo creatureInfo) {
        return this.playerExt.selectedVariant;
    }

    /**
     * Plays an idle or tame sound of the provided creature when it is selected in the GUI.
     *
     * @param creatureInfo The creature to play the sound from.
     */
    public void playCreatureSelectSound(CreatureInfo creatureInfo) {
        String soundSuffix = "";
        if (creatureInfo.getSubspecies(this.playerExt.selectedSubspecies).name != null) {
            soundSuffix += "." + creatureInfo.getSubspecies(this.playerExt.selectedSubspecies).name;
        }
        SoundEvent soundEvent = ObjectManager.getSound(creatureInfo.getName() + soundSuffix + "_say");
        this.player.getCommandSenderWorld().playSound(this.player, this.player.position().x(), this.player.position().y(), this.player.position().z(), soundEvent, SoundSource.NEUTRAL, 1, 1);
    }

    /**
     * Called when a display entity is created.
     *
     * @param creatureInfo The Creature Info used to create the entity.
     * @param entity       The display entity instance.
     */
    public void onCreateDisplayEntity(CreatureInfo creatureInfo, LivingEntity entity) {
    }
}
