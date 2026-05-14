package com.lycanitesmobs.client.event.mobevent;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.client.manager.OverlayManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.client.gui.screen.base.BaseOverlayScreen;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

public class MobEventPlayerClient {

    // Properties:
    public MobEvent mobEvent;

    // Properties:
    public int ticks = 0;
    public Level world;
    public SimpleSoundInstance sound;

    /**
     * True if the started event was already running and show display as 'Event Extended' in chat.
     **/
    public boolean extended = false;


    // ==================================================
    //                     Constructor
    // ==================================================
    public MobEventPlayerClient(MobEvent mobEvent, Level world) {
        this.mobEvent = mobEvent;
        this.world = world;
        if (!world.isClientSide)
            LMHelperClass.logWarning("", "Created a MobEventClient with a server side world, this shouldn't happen, things are going to get weird!");
    }


    // ==================================================
    //                       Start
    // ==================================================
    public void onStart(Player player) {
        if (!this.extended) {
            this.ticks = 0;
        }
        MutableComponent eventMessage = Component.translatable("event." + (extended ? "extended" : "started") + ".prefix")
                .append(" ")
                .append(this.mobEvent.getTitle())
                .append(" ")
                .append(Component.translatable("event." + (extended ? "extended" : "started") + ".suffix"));
        player.displayClientMessage(eventMessage, false);

        if (player.getAbilities().instabuild && !MobEventPlayerServer.testOnCreative && "world".equalsIgnoreCase(this.mobEvent.channel)) {
            return;
        }

        this.playSound();
    }

    public void playSound() {
        if (ObjectManager.getSound("mobevent_" + this.mobEvent.title.toLowerCase()) == null) {
            LMHelperClass.logWarning("MobEvent", "Sound missing for: " + this.mobEvent.getTitle());
            return;
        }
        this.sound = new MobEventSound(ObjectManager.getSound("mobevent_" + this.mobEvent.title.toLowerCase()), SoundSource.RECORDS, ClientManager.getInstance().getClientPlayer(), this.world.random, 1.0F, 1.0F);
        Minecraft.getInstance().getSoundManager().play(this.sound);
    }


    // ==================================================
    //                      Finish
    // ==================================================
    public void onFinish(Player player) {
        MutableComponent eventMessage = Component.translatable("event.finished.prefix")
                .append(" ")
                .append(this.mobEvent.getTitle())
                .append(" ")
                .append(Component.translatable("event.finished.suffix"));
        player.displayClientMessage(eventMessage, false);
    }


    // ==================================================
    //                      Update
    // ==================================================
    public void onUpdate() {
        this.ticks++;
    }


    // ==================================================
    //                       GUI
    // ==================================================
    @OnlyIn(Dist.CLIENT)
    public void onGUIUpdate(GuiGraphics matrixStack, BaseOverlayScreen gui, int sWidth, int sHeight) {
        Player player = ClientManager.getInstance().getClientPlayer();
        if (player.getAbilities().instabuild && !MobEventPlayerServer.testOnCreative && "world".equalsIgnoreCase(this.mobEvent.channel)) {
            return;
        }
        if (this.world == null || this.world != player.getCommandSenderWorld()) return;
        if (!this.world.isClientSide) return;

        int introTime = 12 * 20;
        if (this.ticks > introTime) return;
        int startTime = 2 * 20;
        int stopTime = 4 * 20;
        float animation = 1.0F;

        if (this.ticks < startTime)
            animation = (float) this.ticks / (float) startTime;
        else if (this.ticks > introTime - stopTime)
            animation = ((float) (introTime - this.ticks) / (float) stopTime);

        if (animation <= 0F) {
            return;
        }

        int width = 256;
        int height = 256;
        int x = (sWidth / 2) - (width / 2);
        int y = (sHeight / 2) - (height / 2);
        int u = width;
        int v = height;
        x += 3 - (this.ticks % 6);
        y += 2 - (this.ticks % 4);

        gui.getMinecraft().getTextureManager().bindForSetup(this.getTexture());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, animation);

        OverlayManager.getInstance().drawHelper.drawTexturedModalRect(matrixStack, x, y, u, v, width, height);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }


    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTexture() {
        if (TextureManager.getTexture("guimobevent" + this.mobEvent.title) == null)
            TextureManager.addTexture("guimobevent" + this.mobEvent.title, "textures/mobevents/" + this.mobEvent.title.toLowerCase() + ".png");
        return TextureManager.getTexture("guimobevent" + this.mobEvent.title);
    }
}
