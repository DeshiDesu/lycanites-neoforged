package com.lycanitesmobs;

import com.lycanitesmobs.client.gui.screen.block.EquipmentForgeScreen;
import com.lycanitesmobs.client.gui.screen.block.EquipmentInfuserScreen;
import com.lycanitesmobs.client.gui.screen.block.EquipmentStationScreen;
import com.lycanitesmobs.client.gui.screen.block.SummoningPedestalScreen;
import com.lycanitesmobs.client.gui.screen.creature.CreatureInventoryScreen;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.manager.EffectManager;
import com.lycanitesmobs.core.event.GameEventListener;
import com.lycanitesmobs.core.data.info.altar.AltarInfo;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.event.RegistryEvents;
import com.lycanitesmobs.core.manager.*;
import com.lycanitesmobs.core.network.proxy.ClientProxy;
import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.manager.CommandManager;
import com.lycanitesmobs.core.compatibility.OculusCompat;
import com.lycanitesmobs.core.data.config.CoreConfig;
import com.lycanitesmobs.core.container.block.EquipmentForgeContainer;
import com.lycanitesmobs.core.container.block.EquipmentInfuserContainer;
import com.lycanitesmobs.core.container.block.EquipmentStationContainer;
import com.lycanitesmobs.core.container.block.SummoningPedestalContainer;
import com.lycanitesmobs.core.container.creature.CreatureContainer;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.entity.util.EntityFactory;
import com.lycanitesmobs.core.block.Material;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.item.consumable.holiday.ItemHalloweenTreat;
import com.lycanitesmobs.core.item.consumable.holiday.ItemWinterGift;
import com.lycanitesmobs.core.manager.EquipmentPartManager;
import com.lycanitesmobs.core.event.MobEventListener;
import com.lycanitesmobs.core.network.proxy.IProxy;
import com.lycanitesmobs.core.network.proxy.ServerProxy;
import com.lycanitesmobs.core.entity.spawner.StructureSpawnInjector;
import com.lycanitesmobs.core.event.SpawnerEventListener;
import com.lycanitesmobs.core.worldgen.data.WorldgenJsonDumper;
import com.lycanitesmobs.core.worldgen.structure.DungeonVirtualPack;
import com.lycanitesmobs.core.worldgen.structure.ModStructureTypes;
import com.lycanitesmobs.core.worldgen.structure.ModStructurePieceTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.lycanitesmobs.core.util.helpers.LMHelperClass.fixMaxHealth;

@Mod(LycanitesMobs.MODID)
@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID)
public class LycanitesMobs {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "lycanitesmobs";
    public static final String name = "Lycanites Mobs";
    public static final String versionNumber = "2.3.3.4";
    public static final String versionMC = "1.20.1";
    public static final String version = versionNumber + " - MC " + versionMC;
    public static final String website = "https://lycanitesmobs.com";
    public static final String serviceAPI = "https://service.lycanitesmobs.com/api/v1";
    public static final String twitter = "https://twitter.com/Lycanite05";
    public static final String patreon = "https://www.patreon.com/lycanite";
    public static final String guilded = "https://www.guilded.gg/i/jpLvd6J2";
    public static final String discord = "https://discord.gg/bFpV3z4";

    public static final PacketManager PACKET_MANAGER = new PacketManager();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.Keys.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.Keys.BLOCKS, MODID);
    // TODO: move projectile registries to RegistryEvents
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
            .create(ForgeRegistries.Keys.ENTITY_TYPES, MODID);

    public static EffectManager effectManager;

    public static ModInfo modInfo;
    public static boolean configReady = false;
    public static boolean earlyDebug = false;
    // Proxy:
    public static IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    /**
     * Constructor
     **/

    public LycanitesMobs() {
        modInfo = new ModInfo(this, name, 1000);

        CoreConfig.buildSpec();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CoreConfig.SPEC);

        FileLoader.initAll(modInfo.modid);
        StreamLoader.initAll(modInfo.modid);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        WorldGenManager.register(modEventBus);
        ObjectManager.registerContainers(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::addPackFinders);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);
        modEventBus.addListener(GameEventListener::onRegisterCapabilities);
        modEventBus.register(RegistryEvents.getInstance());

        PROXY.registerEvents();
        ItemManager.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(SpawnerEventListener.getInstance());
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(CommandManager.getInstance());
        MinecraftForge.EVENT_BUS.register(new GameEventListener());
        MinecraftForge.EVENT_BUS.register(MobEventManager.getInstance());
        MinecraftForge.EVENT_BUS.register(MobEventListener.getInstance());
        MinecraftForge.EVENT_BUS.register(StructureSpawnInjector.getInstance());

        PACKET_MANAGER.register();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            try {
                Class<?> lib = Class.forName("net.liopyu.dyntopolib.DyntopoLib");
                lib.getMethod("register", IEventBus.class).invoke(null, modEventBus);
                Class<?> shapes = Class.forName("net.liopyu.dyntopolib.LycanitesShapes");
                shapes.getMethod("register").invoke(null);
            } catch (ClassNotFoundException ignored) {
                // dyntopolib not present — editor unavailable, mod runs fine
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("DyntopoLib found but failed to initialize", e);
            }
        });

        this.loadContent();
    }

    public static void registryObjects() {
        // Blocks and Items:
        ItemManager.getInstance().startup(modInfo);
        // Equipment Parts:
        EquipmentPartManager.getInstance().loadAllFromJson(modInfo);
        // Creatures:
        CreatureManager.getInstance().startup(modInfo);
        // Projectiles:
        ProjectileManager.getInstance().startup(modInfo);
    }

    /**
     * Some initialization must be done after registries have been initialized so we
     * do them on common setup
     */
    public static void loadValues() {
        var creatureManager = CreatureManager.getInstance();
        for (CreatureInfo creatureInfo : creatureManager.creatures.values()) {
            ResourceLocation id = new ResourceLocation(LycanitesMobs.MODID, creatureInfo.getName());
            EntityType<?> t = ForgeRegistries.ENTITY_TYPES.getValue(id);
            if (t == null || !id.equals(ForgeRegistries.ENTITY_TYPES.getKey(t)))
                continue;
            creatureInfo.entityType = (EntityType<? extends LivingEntity>) t;
            EntityFactory.getInstance().addEntityType(creatureInfo.entityType, creatureInfo.entityConstructor,
                    creatureInfo.getName());
            creatureInfo.init();
        }
        ProjectileManager.getInstance().bindRegisteredTypes();
    }

    // Content Loading:
    public void loadContent() {
        // Elements:
        ElementManager.getInstance().loadAllFromJson(modInfo);

        // Vanilla Item Lists:
        ObjectLists.createVanillaLists();

        // Mob Effects:
        effectManager = EffectManager.getInstance();

        // Registry Objects (creatures must load before spawners so MobSpawn can resolve creature IDs):
        LycanitesMobs.registryObjects();

        // Spawners:
        SpawnerManager.getInstance().loadAllFromJson(modInfo);

        // Structure Spawn Injection (replaces tick-based StructureSpawnLocation):
        StructureSpawnInjector.getInstance().loadAllFromJson(modInfo);

        // Altars:
        AltarInfo.createAltars();

        // Mob Events:
        MobEventManager.getInstance().loadAllFromJson(modInfo);

        // Dungeons:
        DungeonManager.getInstance().loadAllFromJson(modInfo);

        // Treat Lists:
        ItemHalloweenTreat.createObjectLists();
        ItemWinterGift.createObjectLists();

        // World Gen:
        WorldGenManager.getInstance().setupFluidFeatures();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        configReady = true;
        ObjectManager.setCurrentModInfo(modInfo);
        this.loadConfigs();
        // Initialize Material block lists
        Material.init();
        // Fix Health Limit:
        fixMaxHealth();
        // Mod Support:
        OculusCompat.init();
        loadValues();

        event.enqueueWork(() -> {
            ModStructureTypes.init();
            ModStructurePieceTypes.init();

            WorldGenManager.getInstance().setupFluidFeatures();
            WorldgenJsonDumper.dumpIfDev();
        });
    }

    /**
     * Registers a virtual datapack that dynamically generates worldgen structure,
     * structure_set, and biome tag JSONs from dungeon schematic configs.
     * This replaces the hard-coded JSON files and allows full customization
     * of dungeon biome placement through the schematic condition configs.
     */
    private void addPackFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(consumer -> {
                Pack.ResourcesSupplier supplier = id -> new DungeonVirtualPack(id);

                Pack pack = Pack.readMetaAndCreate(
                        "lycanitesmobs_dynamic_dungeons",
                        Component.literal("Lycanites Mobs Dynamic Dungeons"),
                        true,
                        supplier,
                        PackType.SERVER_DATA,
                        Pack.Position.TOP,
                        PackSource.BUILT_IN
                );

                if (pack != null) {
                    consumer.accept(pack);
                }
            });
        }
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        ClientManager.getInstance().initLanguageManager();
        ClientManager.getInstance().registerEvents();
        TextureManager.getInstance().createTextures(modInfo);
        ModelManager.getInstance().createModels();
        ClientManager.getInstance().initBlockRenderTypes();
        // New menu screen registration
        event.enqueueWork(
                () -> {
                    MenuScreens.register(CreatureContainer.TYPE, CreatureInventoryScreen::new);
                    MenuScreens.register(SummoningPedestalContainer.TYPE, SummoningPedestalScreen::new);
                    MenuScreens.register(EquipmentForgeContainer.TYPE, EquipmentForgeScreen::new);
                    MenuScreens.register(EquipmentInfuserContainer.TYPE, EquipmentInfuserScreen::new);
                    MenuScreens.register(EquipmentStationContainer.TYPE, EquipmentStationScreen::new);
                });

    }

    public void loadConfigs() {
        ItemManager.getInstance().loadConfig();
        CreatureManager.getInstance().loadConfig();
        MobEventManager.getInstance().loadConfig();
        AltarInfo.loadGlobalSettings();
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Example code to dispatch IMC to another mod
        // InterModComms.sendTo("modif", "methodname", () -> { LOGGER.info("Hello world
        // from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event) {
        // Example code to receive and process InterModComms from other mods
        // LOGGER.info("Got IMC {}",
        // event.getIMCStream().map(m->m.getMessageSupplier().get()).collect(Collectors.toList()));
    }
}
