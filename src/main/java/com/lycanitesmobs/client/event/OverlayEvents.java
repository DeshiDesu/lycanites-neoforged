package com.lycanitesmobs.client.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.event.mobevent.MobEventPlayerClient;
import com.lycanitesmobs.client.gui.screen.base.BaseOverlayScreen;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.client.manager.KeyManager;
import com.lycanitesmobs.client.manager.OverlayManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.data.config.ConfigDebug;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.item.summoningstaff.ItemStaffSummoning;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.lycanitesmobs.client.gui.screen.base.BaseOverlayScreen.GUI_ICONS_LOCATION;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OverlayEvents {
    // ==================================================
    //                  Draw Game Overlay
    // ==================================================
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderExperienceBar(RenderGuiOverlayEvent event) {
        if (ClientManager.getInstance().getClientPlayer() == null)
            return;
        Player player = ClientManager.getInstance().getClientPlayer();
        var minecraft = Minecraft.getInstance();
        var drawhelper = OverlayManager.getInstance().drawHelper;
        var baseOverlayScreen = BaseOverlayScreen.newInstance();
        if (event.isCancelable() || event.getOverlay() != VanillaGuiOverlay.EXPERIENCE_BAR.type()) {
            return;
        }
        PoseStack matrixStack = event.getGuiGraphics().pose();

        matrixStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int sWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth(); // getMainWindow()
        int sHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        // ========== Mob/World Events Title ==========
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(player.getCommandSenderWorld());
        if (worldExt != null) {
            for (MobEventPlayerClient mobEventPlayerClient : worldExt.clientMobEventPlayers.values()) {
                matrixStack.pushPose();
                mobEventPlayerClient.onGUIUpdate(event.getGuiGraphics(), baseOverlayScreen, sWidth, sHeight);
                matrixStack.popPose();
            }
            if (worldExt.clientWorldEventPlayer != null) {
                matrixStack.pushPose();
                worldExt.clientWorldEventPlayer.onGUIUpdate(event.getGuiGraphics(), baseOverlayScreen, sWidth, sHeight);
                matrixStack.popPose();
            }
        }

        // ========== Summoning Focus Bar ==========
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt != null && !minecraft.player.getAbilities().instabuild && (
                minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemStaffSummoning
                        || minecraft.player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ItemStaffSummoning
        )) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int barYSpace = 10;
            int barXSpace = -1;

            int summonBarWidth = 9;
            int summonBarHeight = 9;
            int summonBarX = (sWidth / 2) + 10;
            int summonBarY = sHeight - 30 - summonBarHeight;

            summonBarY -= barYSpace;
            if (minecraft.player.isEyeInFluid(FluidTags.WATER))
                summonBarY -= barYSpace;

            var emptyTex = TextureManager.getTexture("GUIPetSpiritEmpty");
            var fullTex = TextureManager.getTexture("GUIPetSpiritUsed");
            var fillTex = TextureManager.getTexture("GUIPetSpiritFilling");

            for (int n = 0; n < 10; n++) {
                int x = summonBarX + ((summonBarWidth + barXSpace) * (9 - n));

                minecraft.getTextureManager().bindForSetup(emptyTex);
                drawhelper.drawTexture(event.getGuiGraphics(), emptyTex, x, summonBarY, 0, 1, 1, summonBarWidth, summonBarHeight);

                int threshold = playerExt.summonFocusMax - (n * playerExt.summonFocusCharge);
                if (playerExt.summonFocus >= threshold) {
                    minecraft.getTextureManager().bindForSetup(fullTex);
                    drawhelper.drawTexture(event.getGuiGraphics(), fullTex, x, summonBarY, 0, 1, 1, summonBarWidth, summonBarHeight);
                } else if (playerExt.summonFocus + playerExt.summonFocusCharge > threshold) {
                    float scale = (float) (playerExt.summonFocus % playerExt.summonFocusCharge)
                            / (float) playerExt.summonFocusCharge;
                    int w = Math.round(summonBarWidth * scale);
                    if (w > 0) {
                        minecraft.getTextureManager().bindForSetup(fillTex);
                        drawhelper.drawTexture(event.getGuiGraphics(), fillTex,
                                x, summonBarY,
                                0, scale, 1,
                                w, summonBarHeight);
                    }
                }
            }
        }


        // ========== Mount Stamina Bar ==========
        if (minecraft.player.getVehicle() != null && minecraft.player.getVehicle() instanceof RideableCreatureEntity) {
            RideableCreatureEntity mount = (RideableCreatureEntity) minecraft.player.getVehicle();
            float mountStamina = mount.getStaminaPercent();

            // Mount Controls Message:
            if (baseOverlayScreen.mountMessageTime > 0) {
                MutableComponent mountMessage = Component.translatable("gui.mount.controls.prefix")
                        .append(" ").append(KeyManager.instance.mountAbility.getTranslatedKeyMessage())
                        .append(" ").append(Component.translatable("gui.mount.controls.ability"));
//						.append(" ").append(KeyHandler.instance.dismount.getTranslatedKeyMessage())
//						.append(" ").append(Component.translatable("gui.mount.controls.dismount"));
                minecraft.gui.setOverlayMessage(mountMessage, false);
            }

            // Mount Ability Stamina Bar:
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            minecraft.getTextureManager().bindForSetup(GUI_ICONS_LOCATION);
            int staminaBarWidth = 182;
            int staminaBarHeight = 5;
            int staminaEnergyWidth = (int) ((float) (staminaBarWidth + 1) * mountStamina);
            int staminaBarX = (sWidth / 2) - (staminaBarWidth / 2);
            int staminaBarY = sHeight - 32 + 3;
            int staminaTextureY = 84;
            if ("toggle".equals(mount.getStaminaType()))
                staminaTextureY -= staminaBarHeight * 2;
            int staminaEnergyY = staminaTextureY + staminaBarHeight;

            drawhelper.drawTexturedModalRect(event.getGuiGraphics(), staminaBarX, staminaBarY, 0, staminaTextureY, staminaBarWidth, staminaBarHeight);
            if (staminaEnergyWidth > 0)
                drawhelper.drawTexturedModalRect(event.getGuiGraphics(), staminaBarX, staminaBarY, 0, staminaEnergyY, staminaEnergyWidth, staminaBarHeight);

            if (baseOverlayScreen.mountMessageTime > 0)
                baseOverlayScreen.mountMessageTime--;
        } else
            baseOverlayScreen.mountMessageTime = baseOverlayScreen.mountMessageTimeMax;

        // ========== Taming Reputation Bar ==========
        HitResult mouseOver = Minecraft.getInstance().hitResult;
        if (mouseOver instanceof EntityHitResult) {
            Entity mouseOverEntity = ((EntityHitResult) mouseOver).getEntity();
            if (mouseOverEntity instanceof BaseCreatureEntity) {
                BaseCreatureEntity creatureEntity = (BaseCreatureEntity) mouseOverEntity;
                CreatureInfo creatureInfo = creatureEntity.creatureInfo;
                CreatureRelationshipEntry relationshipEntry = creatureEntity.relationships.getEntry(player);
                if (relationshipEntry != null && relationshipEntry.getReputation() > 0 && !creatureEntity.isTamed()) {
                    float barWidth = 100;
                    float barHeight = 11;
                    float barX = ((float) minecraft.getWindow().getGuiScaledWidth() / 2) - (barWidth / 2);
                    float barY = (float) minecraft.getWindow().getGuiScaledHeight() * 0.75F;
                    float barCenter = barX + (barWidth / 2);

                    drawhelper.drawTexture(event.getGuiGraphics(), TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
                    float reputationNormal = Math.min(1, (float) relationshipEntry.getReputation() / creatureInfo.getTamingReputation());
                    String barFillTexture = "GUIPetBarRespawn";
                    if (relationshipEntry.getReputation() >= creatureInfo.getFriendlyReputation()) {
                        barFillTexture = "GUIPetBarHealth";
                    }
                    drawhelper.drawTexture(event.getGuiGraphics(), TextureManager.getTexture(barFillTexture), barX, barY, 0, reputationNormal, 1, barWidth * reputationNormal, barHeight);
                    String reputationText = Component.translatable("entity.reputation").getString() + ": " + relationshipEntry.getReputation() + "/" + creatureInfo.getTamingReputation();
                    drawhelper.draw(event.getGuiGraphics(), reputationText, barCenter - ((float) drawhelper.getStringWidth(reputationText) / 2), barY + 2, 0xFFFFFF);
                }
            }
        }

        matrixStack.popPose();
        minecraft.getTextureManager().bindForSetup(GUI_ICONS_LOCATION);
    }

    // ==================================================
    //                 Debug Overlay
    // ==================================================
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onGameOverlay(OverlayManager.Text event) {
        if (!ConfigDebug.INSTANCE.creatureOverlay.get()) {
            return;
        }

        // Entity:
        HitResult mouseOver = Minecraft.getInstance().hitResult;
        if (mouseOver instanceof EntityHitResult) {
            Entity mouseOverEntity = ((EntityHitResult) mouseOver).getEntity();
            if (mouseOverEntity instanceof BaseCreatureEntity) {
                BaseCreatureEntity mouseOverCreature = (BaseCreatureEntity) mouseOverEntity;
                event.getLeft().add("");
                event.getLeft().add("Target Creature: " + mouseOverCreature.getName().getString());
                event.getLeft().add("Distance To player: " + mouseOverCreature.distanceTo(Minecraft.getInstance().player));
                event.getLeft().add("Elements: " + mouseOverCreature.creatureInfo.getElementNames(mouseOverCreature.getSubspecies()).getString());
                event.getLeft().add("Subspecies: " + mouseOverCreature.getSubspeciesIndex());
                event.getLeft().add("Variant: " + mouseOverCreature.getVariantIndex());
                event.getLeft().add("Level: " + mouseOverCreature.getMobLevel());
                event.getLeft().add("Experience: " + mouseOverCreature.getExperience() + "/" + mouseOverCreature.creatureStats.getExperienceForNextLevel());
                event.getLeft().add("Size: " + mouseOverCreature.sizeScale);
                event.getLeft().add("");
                event.getLeft().add("Health: " + mouseOverCreature.getHealth() + "/" + mouseOverCreature.getMaxHealth() + " Fresh: " + mouseOverCreature.creatureStats.getHealth());
                event.getLeft().add("Speed: " + mouseOverCreature.getAttribute(Attributes.MOVEMENT_SPEED).getValue() + "/" + mouseOverCreature.creatureStats.getSpeed());
                event.getLeft().add("");
                event.getLeft().add("Defense: " + mouseOverCreature.creatureStats.getDefense());
                event.getLeft().add("Armor: " + mouseOverCreature.getArmorValue());
                event.getLeft().add("");
                event.getLeft().add("Damage: " + mouseOverCreature.creatureStats.getDamage());
                event.getLeft().add("Melee Speed: " + mouseOverCreature.creatureStats.getAttackSpeed());
                event.getLeft().add("Melee Range: " + mouseOverCreature.getPhysicalRange());
                event.getLeft().add("Ranged Speed: " + mouseOverCreature.creatureStats.getRangedSpeed());
                event.getLeft().add("Pierce: " + mouseOverCreature.creatureStats.getPierce());
                event.getLeft().add("");
                event.getLeft().add("Effect Duration: " + mouseOverCreature.creatureStats.getEffect() + " Base Seconds");
                event.getLeft().add("Effect Amplifier: x" + mouseOverCreature.creatureStats.getAmplifier());
                event.getLeft().add("");
                event.getLeft().add("Has Attack Target: " + mouseOverCreature.hasAttackTarget());
                event.getLeft().add("Has Avoid Target: " + mouseOverCreature.hasAvoidTarget());
                event.getLeft().add("Has Master Target: " + mouseOverCreature.hasMaster());
                event.getLeft().add("Has Parent Target: " + mouseOverCreature.hasParent());

                event.getLeft().add("");
                CreatureRelationshipEntry relationshipEntry = mouseOverCreature.relationships.getEntry(Minecraft.getInstance().player);
                event.getLeft().add("Reputation with Player: " + (relationshipEntry != null ? relationshipEntry.getReputation() : 0) + "/" + mouseOverCreature.creatureInfo.getTamingReputation());

                if (mouseOverEntity instanceof TameableCreatureEntity) {
                    TameableCreatureEntity mouseOverTameable = (TameableCreatureEntity) mouseOverCreature;
                    event.getLeft().add("Owner ID: " + (mouseOverTameable.getOwnerId() != null ? mouseOverTameable.getOwnerId().toString() : "None"));
                    event.getLeft().add("Owner Name: " + mouseOverTameable.getOwnerName().getString());
                }
            }
        }
    }
}
