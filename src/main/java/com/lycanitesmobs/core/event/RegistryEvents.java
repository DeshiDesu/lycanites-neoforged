package com.lycanitesmobs.core.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.BlockTypeGetter;
import com.lycanitesmobs.core.block.blockentity.EquipmentInfuserTileEntity;
import com.lycanitesmobs.core.block.blockentity.EquipmentStationTileEntity;
import com.lycanitesmobs.core.block.blockentity.TileEntityEquipmentForge;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import com.lycanitesmobs.core.command.*;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.item.ItemInfo;
import com.lycanitesmobs.core.entity.effect.EffectBase;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.block.fluid.CustomFluid;
import com.lycanitesmobs.core.block.fluid.type.BaseFluidType;
import com.lycanitesmobs.core.manager.*;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.lycanitesmobs.core.manager.ObjectManager.*;
import static com.lycanitesmobs.core.util.helpers.LMHelperClass.cast;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegistryEvents {
    public static RegistryEvents INSTANCE;

    public static RegistryEvents getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RegistryEvents();
        return INSTANCE;
    }

    // ==================================================
    //                  Registry Events
    // ==================================================
    // ========== Entities ==========
    @SubscribeEvent
    public void registerEntities(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ENTITY_TYPES,
                helper -> {
                    for (Map.Entry<String, Lazy<? extends EntityType<?>>> entityType : entityTypes.entrySet()) {
                        helper.register(new ResourceLocation(LycanitesMobs.MODID, entityType.getKey()), entityType.getValue().get());
                    }
                }
        );
    }

    // ========== Blocks ==========

    /**
     * Registers blocks and their corresponding block items.
     * <p>
     * Block items must be registered in the same Registry Event after blocks
     * to ensure that a non-null block is passed to the BlockItem constructor.
     * This sequence guarantees that the block references used in block items
     * are fully initialized and registered, avoiding any potential issues
     * with null references or unregistered blocks.
     * </p>
     *
     * @param event The register event for blocks and items.
     */
    @SubscribeEvent
    public void registerBlocks(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            for (Supplier<? extends Block> block : blocks.values()) {
                BlockTypeGetter getter = (BlockTypeGetter) block.get();
                helper.register(getter.getRegistryName(), block.get());
            }
            for (Lazy<? extends LiquidBlock> lazy : liquidBlocks.values()) {
                LiquidBlock lb = lazy.get();
                BlockTypeGetter getter = (BlockTypeGetter) lb;
                helper.register(getter.getRegistryName(), lb);
            }
        });
        event.register(ForgeRegistries.Keys.ITEMS,
                helper -> {
                    for (Map.Entry<String, Lazy<? extends BlockItem>> entry : ObjectManager.blockItems.entrySet()) {
                        String name = entry.getKey();
                        Supplier<? extends BlockItem> blockItem = entry.getValue();
                        LMHelperClass.logDebug("Item", "Registering item block: " + name);
                        if (name == null) {
                            LMHelperClass.logWarning("", "Block Item: " + name + " has no Registry Name!");
                            continue;
                        }
                        helper.register(new ResourceLocation(LycanitesMobs.MODID, name), blockItem.get());
                    }
                }
        );
    }

    // ========== Items ==========
    @SubscribeEvent
    public void registerItems(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS,
                helper -> {
                    //General Items
                    for (ItemInfo itemInfo : ItemManager.items.values()) {
                        if (ObjectManager.items.containsKey(itemInfo.getName())) {
                            LycanitesMobs.LOGGER.info("Tried registering item in Object Manager's item list, skipping: " + itemInfo.getName());
                            return;
                        }

                        LMHelperClass.logDebug("Item", "Registering general item: " + itemInfo.getName());
                        helper.register(new ResourceLocation(LycanitesMobs.MODID, itemInfo.getItem().itemName), itemInfo.getItem());
                    }
                    //Special Items
                    for (Supplier<? extends Item> item : items.values()) {
                        for (String name : items.keySet()) {
                            if (ItemManager.items.containsKey(name)) {
                                LycanitesMobs.LOGGER.info("Tried registering item in Item Manager's item list, skipping: " + name);
                                return;
                            }
                            LMHelperClass.logDebug("Item", "Registering special item: " + name);
                            helper.register(new ResourceLocation(LycanitesMobs.MODID, name), item.get());
                        }
                    }
                }
        );

    }

    // ========== Sounds ==========

    /**
     * Sounds are for some reason being registered elsewhere(assuming during the process of
     * adding them to the sounds map with SoundEvent.createVariableRangeEvent(resourceLocation)
     * so this is not needed. Kept for future organization.
     *
     * @param event
     */
    /*@SubscribeEvent
    public void registerSounds(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.SOUND_EVENTS,
                helper -> {
                    for (Lazy<? extends SoundEvent> soundEvent : sounds.values()) {
                        for (ResourceLocation registryName : soundNames.values()) {
                            if (registryName == null) {
                                LycanitesMobs.logWarning("", "Sound: " + soundEvent + " has no Registry Name!");
                            }
                            helper.register(registryName, soundEvent.get());
                        }
                    }
                }
        );
    }*/

    // ========== Potions ==========
    @SubscribeEvent
    public void registerEffects(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.MOB_EFFECTS,
                helper -> {
                    for (EffectBase effect : effects.values()) {
                        helper.register(effect.getRegistryName(), effect);
                    }
                }
        );
    }

    // ========== Fluids ==========
    @SubscribeEvent
    public void registerFluids(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.FLUIDS, helper -> {
            FluidManager.getInstance().defineFluids();
            for (Fluid fluid : fluids.values()) {
                if (fluid instanceof CustomFluid customFluid) {
                    helper.register(customFluid.getRegistryName(), fluid);
                }
            }
        });
        event.register(ForgeRegistries.Keys.FLUID_TYPES, helper -> {
            Set<ResourceLocation> seen = new HashSet<>();
            for (Fluid fluid : fluids.values()) {
                FluidType ft = fluid.getFluidType();
                if (ft instanceof BaseFluidType bft) {
                    ResourceLocation id = bft.getRegistryName();
                    if (seen.add(id)) {
                        helper.register(id, bft);
                    }
                }
            }
        });
    }

    // ========== Tile Entities ==========
    @SubscribeEvent
    public void registerTileEntities(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES,
                helper -> {
                    BlockEntityType<TileEntitySummoningPedestal> summoningPedestalType = BlockEntityType.Builder.of(TileEntitySummoningPedestal::new,
                            getBlock("summoningpedestal")
                    ).build(null);
                    ResourceLocation summoningpedestalLocation = new ResourceLocation(LycanitesMobs.MODID, "summoningpedestal");
                    helper.register(summoningpedestalLocation, summoningPedestalType);
                    tileEntityTypes.put(TileEntitySummoningPedestal.class, summoningPedestalType);

                    BlockEntityType<TileEntityEquipmentForge> equipmentForgeType = BlockEntityType.Builder.of(TileEntityEquipmentForge::new,
                            getBlock("equipmentforge_lesser"),
                            getBlock("equipmentforge_greater"),
                            getBlock("equipmentforge_master")
                    ).build(null);
                    ResourceLocation equipmentforgeLocation = new ResourceLocation(LycanitesMobs.MODID, "equipmentforge");
                    helper.register(equipmentforgeLocation, equipmentForgeType);
                    tileEntityTypes.put(TileEntityEquipmentForge.class, equipmentForgeType);

                    BlockEntityType<EquipmentInfuserTileEntity> equipmentInfuserType = BlockEntityType.Builder.of(EquipmentInfuserTileEntity::new,
                            getBlock("equipment_infuser")
                    ).build(null);
                    ResourceLocation equipment_infuserLocation = new ResourceLocation(LycanitesMobs.MODID, "equipment_infuser");
                    helper.register(equipment_infuserLocation, equipmentInfuserType);
                    tileEntityTypes.put(EquipmentInfuserTileEntity.class, equipmentInfuserType);

                    BlockEntityType<EquipmentStationTileEntity> equipmentStationType = BlockEntityType.Builder.of(EquipmentStationTileEntity::new,
                            getBlock("equipment_station")
                    ).build(null);
                    ResourceLocation equipment_stationLocation = new ResourceLocation(LycanitesMobs.MODID, "equipment_station");
                    helper.register(equipment_stationLocation, equipmentStationType);
                    tileEntityTypes.put(EquipmentStationTileEntity.class, equipmentStationType);
                });
    }


    @SubscribeEvent
    public void registerAttributes(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ATTRIBUTES,
                helper -> {
                    helper.register(new ResourceLocation(LycanitesMobs.MODID, "defense"), BaseCreatureEntity.DEFENSE);
                    helper.register(new ResourceLocation(LycanitesMobs.MODID, "ranged_speed"), BaseCreatureEntity.RANGED_SPEED);
                }
        );
    }

    // ========== Stats ==========
    @SubscribeEvent
    public void registerStats(RegisterEvent event) {
        StatManager.getInstance().createStatTypes();
        for (var entry : StatManager.getInstance().statTypes.entrySet()) {
            event.register(ForgeRegistries.Keys.STAT_TYPES,
                    helper -> {
                        helper.register(new ResourceLocation(LycanitesMobs.MODID, entry.getKey()), entry.getValue());
                    }
            );
        }
    }


    // ========== Commands ==========
    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("lm")
                        .then(CreaturesCommand.register())
                        .then(BeastiaryCommand.register())
                        .then(SpawnersCommand.register())
                        .then(SpawnerCommand.register())
                        .then(MobEventsCommand.register())
                        .then(MobEventCommand.register())
                        .then(EquipmentCommand.register())
                        .then(DungeonsCommand.register())
                        .then(DebugCommand.register())
        );
    }


    /*   @SubscribeEvent
       public void registerFeatures(RegisterEvent event) {
           event.register(ForgeRegistries.Keys.FEATURES,
                   helper -> {
                       helper.register(new ResourceLocation(LycanitesMobs.modInfo.modid, "chunkspawn"), WorldGenManager.getInstance().chunkSpawnFeature);
                       helper.register(new ResourceLocation(LycanitesMobs.modInfo.modid, "dungeon"), WorldGenManager.getInstance().dungeonFeature);
                   }
           );
       }*/
    // ==================================================
    //              Entity Attribute Registration
    // ==================================================
    @SubscribeEvent
    public void registerEntityAttributes(EntityAttributeCreationEvent event) {
        CreatureManager cm = CreatureManager.getInstance();
        for (var entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
            ResourceLocation loc = entry.getKey().location();
            if (!loc.getNamespace().equals(LycanitesMobs.MODID)) continue;
            CreatureInfo creatureInfo = cm.getCreature(loc.getPath());
            if (creatureInfo == null) continue;
            event.put(cast(entry.getValue()), BaseCreatureEntity.registerCustomAttributes().build());
        }
    }

    @SubscribeEvent
    public void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        CreatureManager cm = CreatureManager.getInstance();
        int count = 0;
        for (var entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
            ResourceLocation loc = entry.getKey().location();
            if (!loc.getNamespace().equals(LycanitesMobs.MODID)) continue;
            CreatureInfo creatureInfo = cm.getCreature(loc.getPath());
            if (creatureInfo == null) continue;

            event.register(
                    cast(entry.getValue()),
                    SpawnPlacements.Type.NO_RESTRICTIONS,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, level, spawnType, pos, random) -> true,
                    SpawnPlacementRegisterEvent.Operation.OR
            );
            count++;
        }
        LMHelperClass.logDebug("MobSpawns", "Registered SpawnPlacements for " + count + " creatures.");
    }

    // ==================================================
    //           Debug: Scute Summon All Entities
    // ==================================================

    /**
     * Right-clicking with a Turtle Scute spawns one of every registered lycanitesmobs entity
     * in a grid pattern centred two blocks in front of the player. Server-side only.
     */
    @SubscribeEvent
    public static void onRightClickTurtleScute(PlayerInteractEvent.RightClickItem event) {
        if (FMLEnvironment.production) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (event.getItemStack().getItem() != Items.SCUTE) return;

        // entities per row
        final int COLUMNS = 10;

        // blocks between each spawn
        final double SPACING = 8.0;

        var player = event.getEntity();
        Vec3 origin = player.position().add(player.getLookAngle().multiply(3, 0, 3));

        int index = 0;
        for (var entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
            if (!entry.getKey().location().getNamespace().equals(LycanitesMobs.MODID)) continue;

            EntityType<?> type = entry.getValue();
            Entity entity = type.create(serverLevel);
            if (entity == null) continue;

            double col = (index % COLUMNS) * SPACING;
            double row = (index / COLUMNS) * SPACING;
            entity.setPos(origin.x + col, origin.y, origin.z + row);
            serverLevel.addFreshEntity(entity);
            index++;
        }

        LMHelperClass.logInfoMessageDev("[Debug] Spawned " + index + " lycanitesmobs entities.");
        event.setCanceled(true);
    }
}
