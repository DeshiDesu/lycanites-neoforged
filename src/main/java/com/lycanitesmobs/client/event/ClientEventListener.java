package com.lycanitesmobs.client.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.effect.FearAudioHandler;
import com.lycanitesmobs.client.effect.FearHeartbeatSound;
import com.lycanitesmobs.client.effect.FearVisualHandler;
import com.lycanitesmobs.client.effect.MuffledSoundInstance;
import com.lycanitesmobs.client.gui.screen.creature.MinionSelectionScreen;
import com.lycanitesmobs.client.gui.screen.beastiary.CreaturesBeastiaryScreen;
import com.lycanitesmobs.client.gui.screen.beastiary.IndexBeastiaryScreen;
import com.lycanitesmobs.client.gui.screen.beastiary.PetsBeastiaryScreen;
import com.lycanitesmobs.client.gui.screen.beastiary.SummoningBeastiaryScreen;
import com.lycanitesmobs.client.gui.screen.creature.CreatureRecolorScreen;
import com.lycanitesmobs.client.loader.LanguageLoader;
import com.lycanitesmobs.client.loader.ModelReloadListener;
import com.lycanitesmobs.client.manager.KeyManager;
import com.lycanitesmobs.client.manager.TabManager;
import com.lycanitesmobs.client.model.blocky.*;
import com.lycanitesmobs.client.renderer.entity.creature.BlockyRenderer;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.entity.projectile.ProjectileRenderFactory;
import com.lycanitesmobs.client.renderer.entity.effect.FearRenderer;
import com.lycanitesmobs.client.renderer.misc.NoneRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.creature.CreatureType;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.item.consumable.entity.ItemColorCustomSpawnEgg;
import com.lycanitesmobs.core.item.consumable.entity.ItemCustomSpawnEgg;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.ItemManager;
import com.lycanitesmobs.core.item.special.ItemSoulgazer;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.network.message.MessagePlayerControl;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.Objects;

@Mod.EventBusSubscriber(
        modid = LycanitesMobs.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ClientEventListener {
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        var colors = event.getItemColors();

        for (CreatureType creatureType : CreatureManager.getInstance().creatureTypes.values()) {
            Item item = ObjectManager.getItem(creatureType.getSpawnEggName());
            if (item == null) {
                continue;
            }

            ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
            if (key == null) {
                continue;
            }

            if (!(item instanceof ItemCustomSpawnEgg)) {
                continue;
            }

            colors.register(new ItemColorCustomSpawnEgg(), item);
        }
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(LanguageLoader.getInstance());
        event.registerReloadListener(ModelReloadListener.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        var rp = event.getResourceProvider();
        var shader = new ShaderInstance(rp, new ResourceLocation(LycanitesMobs.MODID, "pos_tex_normal"), CustomRenderStates.POS_TEX_NORMAL);
        event.registerShader(shader, s -> CustomRenderStates.POS_TEX_NORMAL_SHADER = s);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BalayangBlockyModel.LAYER_LOCATION, BalayangBlockyModel::createBodyLayer);
        event.registerLayerDefinition(NymphBlockyModel.LAYER_LOCATION, NymphBlockyModel::createBodyLayer);
        event.registerLayerDefinition(GrueBlockyModel.LAYER_LOCATION, GrueBlockyModel::createBodyLayer);
        event.registerLayerDefinition(WraithBlockyModel.LAYER_LOCATION, WraithBlockyModel::createBodyLayer);
        event.registerLayerDefinition(HermaBlockyModel.LAYER_LOCATION, HermaBlockyModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        LycanitesMobs.LOGGER.info("LycanitesMobs: Registering Renderers!" + CreatureManager.getInstance().creatures.size());
        // Creatures:
        for (CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
            //LMHelperClass.logInfoMessage("CI Name: " + creatureInfo.getName());
            var registerBlocky = false;
            if (creatureInfo.dummy) {
                if ("fear".equals(creatureInfo.getName())) {
                    event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), FearRenderer::new);
                } else {
                    event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), (dispatcher) -> new NoneRenderer<BaseProjectileEntity>(dispatcher));
                }
                continue;
            } else if (registerBlocky) {
                if (Objects.equals(creatureInfo.getName(), "nymph")) {
                    LogUtils.getLogger().info("Registering renderer for: " + creatureInfo.getName());
                    event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/nymph.png"), new NymphBlockyModel<>(context.bakeLayer(NymphBlockyModel.LAYER_LOCATION))));
                    continue;
                } else if (Objects.equals(creatureInfo.getName(), "grue")) {
                    LogUtils.getLogger().info("Registering renderer for: " + creatureInfo.getName());
                    event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/grue.png"), new GrueBlockyModel<>(context.bakeLayer(GrueBlockyModel.LAYER_LOCATION))));
                    continue;
                } else if (Objects.equals(creatureInfo.getName(), "wraith")) {
                    LogUtils.getLogger().info("Registering renderer for: " + creatureInfo.getName());
                    event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/wraith.png"), new WraithBlockyModel<>(context.bakeLayer(WraithBlockyModel.LAYER_LOCATION))));
                    continue;
                } else if (Objects.equals(creatureInfo.getName(), "herma")) {
                    LogUtils.getLogger().info("Registering renderer for: " + creatureInfo.getName());
                    event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/herma_white.png"), new HermaBlockyModel<>(context.bakeLayer(HermaBlockyModel.LAYER_LOCATION))));
                    continue;
                }
            }
            event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), (dispatcher) -> new CreatureRenderer<>(creatureInfo.getName(), dispatcher, (float) creatureInfo.width / 2));
        }

        // Projectiles:
        for (ProjectileInfo projectileInfo : ProjectileManager.getInstance().projectiles.values()) {
            if (projectileInfo.modelClassName != null) {
                //Here we retain the factory since we are choosing the renderer based off the projectileInfo. Later on we can get rid of this and do individual renderers.
                event.registerEntityRenderer(LMHelperClass.cast(projectileInfo.getEntityType()), (dispatcher) -> new ProjectileRenderFactory<CustomProjectileEntity>(projectileInfo).createRenderFor(dispatcher));
            } else {
                event.registerEntityRenderer(LMHelperClass.cast(projectileInfo.getEntityType()), (dispatcher) -> new ProjectileRenderFactory<CustomProjectileEntity>(projectileInfo).createRenderFor(dispatcher));
            }
        }

        // Old Sprite Projectiles:
        for (String projectileName : ProjectileManager.getInstance().oldSpriteProjectiles.keySet()) {
            Class projectileClass = ProjectileManager.getInstance().oldSpriteProjectiles.get(projectileName);
            event.registerEntityRenderer(LMHelperClass.cast(ProjectileManager.getInstance().oldProjectileTypes.get(projectileClass)), (dispatcher) -> new ProjectileRenderFactory<BaseProjectileEntity>(projectileName, projectileClass, false).createRenderFor(dispatcher));
        }

        // Old Model Projectiles:
        for (String projectileName : ProjectileManager.getInstance().oldModelProjectiles.keySet()) {
            Class projectileClass = ProjectileManager.getInstance().oldModelProjectiles.get(projectileName);
            event.registerEntityRenderer(ProjectileManager.getInstance().oldProjectileTypes.get(projectileClass), (dispatcher) -> new ProjectileRenderFactory<BaseProjectileEntity>(projectileName, projectileClass, true).createRenderFor(dispatcher));
        }

        // Special Entities:
        for (Class specialClass : ObjectManager.specialEntities.values()) {
            event.registerEntityRenderer(LMHelperClass.cast(ObjectManager.specialEntityTypes.get(specialClass)), (dispatcher) -> new NoneRenderer<BaseProjectileEntity>(dispatcher));
        }

    }


    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyManager.dismount);
        event.register(KeyManager.descend);
        event.register(KeyManager.mountAbility);
        event.register(KeyManager.mountInventory);
        event.register(KeyManager.index);
        event.register(KeyManager.beastiary);
        event.register(KeyManager.pets);
        event.register(KeyManager.summoning);
        event.register(KeyManager.minionSelection);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event) {
        int sharpness = ItemManager.getInstance().getEquipmentSharpnessRepair(event.getItemStack());
        int mana = ItemManager.getInstance().getEquipmentManaRepair(event.getItemStack());
        if (sharpness > 0 || mana > 0) {
            event.getToolTip().add(Component.translatable("equipment.item.repair").withStyle(ChatFormatting.BLUE));
            if (sharpness > 0) {
                event.getToolTip().add(Component.translatable("equipment.sharpness").append(" " + sharpness).withStyle(ChatFormatting.BLUE));
            }
            if (mana > 0) {
                event.getToolTip().add(Component.translatable("equipment.mana").append(" " + mana).withStyle(ChatFormatting.BLUE));
            }
        }

        if (event.getItemStack().getItem() instanceof ItemSoulgazer) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getEntity());
            if (extendedPlayer != null && extendedPlayer.creatureStudyCooldown > 0) {
                event.getToolTip().add(Component.translatable("message.beastiary.study.cooldown").append(" " + String.format("%.0f", (float) extendedPlayer.creatureStudyCooldown / 20) + "s").withStyle(ChatFormatting.BLUE));
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onFogDensity(ViewportEvent.RenderFog event) {
        GameRenderer fogRenderer = event.getRenderer();
        LivingEntity entityLiving = Minecraft.getInstance().player;
        if (entityLiving == null) {
            return;
        }
        if (entityLiving.isInLava() && (!entityLiving.isOnFire() || entityLiving.hasEffect(MobEffects.FIRE_RESISTANCE))) {
            event.scaleNearPlaneDistance(0.5F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onBlockOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getBlockState().getBlock() == Blocks.FIRE && (!event.getPlayer().isOnFire() || event.getPlayer().hasEffect(MobEffects.FIRE_RESISTANCE))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            FearVisualHandler.uploadDimmedForWorld();
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            VBOBatcher.getInstance().endDeferredBatches();
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            FearVisualHandler.restoreCleanAfterWorld();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onPlaySound(PlaySoundEvent event) {
        if (!FearAudioHandler.isActive()) return;
        SoundInstance sound = event.getSound();
        if (sound == null) return;

        if (sound instanceof FearHeartbeatSound) return;

        float volumeScale = FearAudioHandler.computeVolumeScale(sound);
        float pitchScale = FearAudioHandler.computePitchScale();

        if (volumeScale >= 1.0F && pitchScale >= 1.0F) return;

        event.setSound(MuffledSoundInstance.wrap(sound, volumeScale, pitchScale));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        FearVisualHandler.tick();
        FearAudioHandler.tick();
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(KeyManager.getInstance().mc.player);
        if (playerExt == null)
            return;
        byte controlStates = 0;
        var mc = KeyManager.getInstance().mc;
        // ========== GUI Keys ==========
        // Player Inventory: Adds extra buttons to the GUI.
        if (!KeyManager.getInstance().inventoryOpen && mc.screen != null && mc.screen.getClass() == Screen.class) {
            TabManager.addTabsToInventory(mc.screen);
            KeyManager.getInstance().inventoryOpen = true;
        }
        if (KeyManager.getInstance().inventoryOpen && (mc.screen == null || mc.screen.getClass() != Screen.class)) {
            KeyManager.getInstance().inventoryOpen = false;
        }

        // Mount Inventory: Adds to control states.
        if (KeyManager.mountInventory.consumeClick()) {
            controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_INVENTORY.id;
        }

        // Beastiary Index:
        if (KeyManager.index.consumeClick()) {
            mc.setScreen(new IndexBeastiaryScreen(mc.player));
        }

        // Beastiary Creatures:
        if (KeyManager.beastiary.consumeClick()) {
            mc.setScreen(new CreaturesBeastiaryScreen(mc.player));
        }

        // Beastiary Pets:
        if (KeyManager.pets.consumeClick()) {
            mc.setScreen(new PetsBeastiaryScreen(mc.player));
        }

        // Beastiary Summoning:
        if (KeyManager.summoning.consumeClick()) {
            mc.setScreen(new SummoningBeastiaryScreen(mc.player));
        }

        if (mc.isWindowActive()) {
            // ========== HUD Controls ==========
            // Minion Selection:
            if (KeyManager.minionSelection.consumeClick()) {
                mc.setScreen(new MinionSelectionScreen(mc.player));
            }

            // ========== Action Controls ==========
            // Vanilla Jump: Adds to control states.
            if (mc.options.keyJump.isDown())
                controlStates += ExtendedPlayer.CONTROL_ID.JUMP.id;

            // Descend: Adds to control states.
            if (KeyManager.descend.isDown())
                controlStates += ExtendedPlayer.CONTROL_ID.DESCEND.id;

            // Mount Ability: Adds to control states.
            if (KeyManager.dismount.isDown())
                controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT.id;

            // Mount Ability: Adds to control states.
            if (KeyManager.mountAbility.isDown())
                controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_ABILITY.id;

            // Attack Key Pressed:
            if (Minecraft.getInstance().options.keyAttack.isDown()) {
                controlStates += ExtendedPlayer.CONTROL_ID.ATTACK.id;
            }
        }


        // ========== Sync Controls To Server ==========
        if (controlStates == playerExt.controlStates)
            return;
        MessagePlayerControl message = new MessagePlayerControl(controlStates);
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
        playerExt.controlStates = controlStates;
    }


    /**
     * Player keyboard events.
     **/
    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.Key event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        // Minion Selection - Closes If Not Holding:
        if (event.getKey() == KeyManager.minionSelection.getKey().getValue() && mc.screen instanceof MinionSelectionScreen) {
            if (event.getAction() == GLFW.GLFW_RELEASE) {
                mc.player.closeContainer();
            }
        }
    }


    /**
     * Player 'mouse' events, these are actually events based on attack or item use actions and are still triggered if the key binding is no longer a mouse click.
     **/
    @SubscribeEvent
    public void onMouseEvent(InputEvent.MouseButton event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.hitResult == null)
            return;

        // Large-entity reach is now handled server-side via a transient ForgeMod.ENTITY_REACH modifier
        // in ExtendedPlayer.updateBigEntityReach(), so no custom left-click handling is needed here.
        if (event.getButton() == 1 && event.getAction() == GLFW.GLFW_PRESS) {
            if (mc.hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entityHit = ((EntityHitResult) mc.hitResult).getEntity();
                if (entityHit instanceof BaseCreatureEntity creature) {
                    var held = mc.player.getMainHandItem();
                    if (held.getItem() == Items.STICK) {
                        mc.setScreen(new CreatureRecolorScreen(creature));
                    }
                }
            }
        }
    }
}
