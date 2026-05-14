package com.lycanitesmobs.core.manager;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.item.ItemConfig;
import com.lycanitesmobs.core.data.info.item.ItemInfo;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.block.base.BlockBase;
import com.lycanitesmobs.core.block.building.HiveBlock;
import com.lycanitesmobs.core.block.cloud.*;
import com.lycanitesmobs.core.block.fire.*;
import com.lycanitesmobs.core.block.special.BlockEquipmentForge;
import com.lycanitesmobs.core.block.special.BlockSummoningPedestal;
import com.lycanitesmobs.core.block.special.EquipmentInfuserBlock;
import com.lycanitesmobs.core.block.special.EquipmentStationBlock;
import com.lycanitesmobs.core.block.web.BlockFrostweb;
import com.lycanitesmobs.core.block.web.BlockQuickWeb;
import com.lycanitesmobs.core.item.consumable.entity.ChargeItem;
import com.lycanitesmobs.core.item.block.ItemBlockPlacer;
import com.lycanitesmobs.core.item.consumable.holiday.ItemHalloweenTreat;
import com.lycanitesmobs.core.item.consumable.holiday.ItemWinterGift;
import com.lycanitesmobs.core.item.consumable.holiday.ItemWinterGiftLarge;
import com.lycanitesmobs.core.item.consumable.utility.ItemCleansingCrystal;
import com.lycanitesmobs.core.item.consumable.utility.ItemImmunizer;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulkey;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulstone;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.special.*;
import com.lycanitesmobs.core.item.summoningstaff.*;
import com.lycanitesmobs.core.tabs.*;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

public class ItemManager extends JSONLoader {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LycanitesMobs.MODID);
    // Creative Tabs:
    public static final CreativeModeTab.Builder itemsGroup =
            LMItemsGroup.getBuilder();

    public static final CreativeModeTab.Builder blocksGroup =
            LMBlocksGroup.getBuilder();

    public static final CreativeModeTab.Builder creaturesGroups =
            LMCreaturesGroup.getBuilder();

    public static final CreativeModeTab.Builder equipmentPartsGroup =
            LMEquipmentPartsGroup.getBuilder();

    public static final CreativeModeTab.Builder chargesGroup =
            LMChargesGroup.getBuilder();

    public static final CreativeModeTab.Builder bestEquipmentGroup =
            LMBestEquipmentGroup.getBuilder();

    public static final RegistryObject<CreativeModeTab> itemTab = TABS.register(LycanitesMobs.MODID + ".items", itemsGroup::build);
    public static final RegistryObject<CreativeModeTab> blockTab = TABS.register(LycanitesMobs.MODID + ".blocks", blocksGroup::build);
    public static final RegistryObject<CreativeModeTab> creaturesTab = TABS.register(LycanitesMobs.MODID + ".creatures", creaturesGroups::build);
    public static final RegistryObject<CreativeModeTab> chargesTab = TABS.register(LycanitesMobs.MODID + ".charges", chargesGroup::build);
    public static final RegistryObject<CreativeModeTab> equipmentPartsTab = TABS.register(LycanitesMobs.MODID + ".equipmentparts", equipmentPartsGroup::build);
    public static final RegistryObject<CreativeModeTab> bestEquipmentTab = TABS.register(LycanitesMobs.MODID + ".bestequipment", bestEquipmentGroup::build);
    public static final Map<String, Item.Properties> registryItems = new HashMap<>();
    public static ItemManager INSTANCE;
    public static Map<String, ItemInfo> items = new HashMap<>();

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }

    /**
     * A list of blocks that need to use the cutout renderer.
     **/
    public List<Block> cutoutBlocks = new ArrayList<>();
    /**
     * A list of mod groups that have loaded with this manager.
     **/
    public List<ModInfo> loadedGroups = new ArrayList<>();
    /**
     * Handles all global item general config settings.
     **/
    public ItemConfig config;

    /**
     * Returns the main Item Manager instance or creates it and returns it.
     **/
    public static ItemManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemManager();
        }
        return INSTANCE;
    }

    /**
     * Called during startup and initially loads everything in this manager.
     *
     * @param modInfo The mod loading this manager.
     */
    public void startup(ModInfo modInfo) {
        loadItems();
        loadAllFromJson(modInfo);
    }

    /**
     * Loads all JSON Items.
     **/
    public void loadAllFromJson(ModInfo modInfo) {
        if (!this.loadedGroups.contains(modInfo)) {
            this.loadedGroups.add(modInfo);
        }
        this.loadAllJson(modInfo, "Items", "items", "name", true, null, FileLoader.COMMON, StreamLoader.COMMON);
        LMHelperClass.logDebug("Items", "Complete! " + this.items.size() + " JSON Items Loaded In Total.");
    }

    /*private static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, LycanitesMobs.MODID);


    public static void registerItem(IEventBus event) {
        ITEM_DEFERRED_REGISTER.register(event);
        ITEM_DEFERRED_REGISTER.register("soulgazer", () -> new ItemSoulgazer(new Item.Properties()));
    }*/


    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        ItemInfo itemInfo = new ItemInfo(modInfo);
        itemInfo.loadFromJSON(json);
        if (Objects.equals(itemInfo.getName(), "unamed_item")) return;
        this.items.put(itemInfo.getName(), itemInfo);
    }

    /**
     * Called during early start up, loads all global configs into this manager.
     **/
    public void loadConfig() {
        ItemConfig.loadGlobalSettings();
    }


    /**
     * Called during early start up, loads all non-json items.
     **/
    public void loadItems() {
        Item.Properties itemProperties = new Item.Properties();
        Item.Properties itemPropertiesNoStack = new Item.Properties().stacksTo(1);

        ObjectManager.addItem("soulgazer", () -> new ItemSoulgazer(itemPropertiesNoStack));
        ObjectManager.addItem("soul_contract", () -> new ItemSoulContract(itemPropertiesNoStack));
        ObjectManager.addItem("mobtoken", () -> new ItemMobToken(new Item.Properties()));
        ObjectManager.addItem("soulstone", () -> new ItemSoulstone(itemProperties, null));
        // Equipment Pieces:
        Item.Properties equipmentProperties = new Item.Properties().stacksTo(1).setNoRepair();
        ObjectManager.addItem("equipment", () -> new ItemEquipment(equipmentProperties));

        // Keys:
        ObjectManager.addItem("soulkey", () -> new ItemSoulkey(itemProperties, "soulkey", 0));
        ObjectManager.addItem("soulkeydiamond", () -> new ItemSoulkey(itemProperties, "soulkeydiamond", 1));
        ObjectManager.addItem("soulkeyemerald", () -> new ItemSoulkey(itemProperties, "soulkeyemerald", 2));


        // Buff Items:
        ObjectManager.addItem("immunizer", () -> new ItemImmunizer(itemProperties));
        ObjectManager.addItem("cleansingcrystal", () -> new ItemCleansingCrystal(itemProperties));


        // Seasonal Items:
        ObjectManager.addItem("halloweentreat", () -> new ItemHalloweenTreat(itemProperties));
        ObjectManager.addItem("wintergift", () -> new ItemWinterGift(itemProperties));
        ObjectManager.addItem("wintergiftlarge", () -> new ItemWinterGiftLarge(itemProperties));


        // Special:
        ObjectManager.addItem("frostyfur", () -> new ItemBlockPlacer(itemProperties, "frostyfur", "frostcloud"));
        ObjectManager.addItem("poisongland", () -> new ItemBlockPlacer(itemProperties, "poisongland", "poisoncloud"));
        ObjectManager.addItem("geistliver", () -> new ItemBlockPlacer(itemProperties, "geistliver", "shadowfire"));

        BlockManager.addDungeonBlocks("lush");
        //if (true) return;
        //.tab(this.itemsGroup)
        // Summoning Staves:
        Item.Properties summoningStaffProperties = new Item.Properties().stacksTo(1).durability(500);
        ObjectManager.addItem("summoningstaff", () -> new ItemStaffSummoning(summoningStaffProperties, "summoningstaff", "summoningstaff"));
        ObjectManager.addItem("stablesummoningstaff", () -> new ItemStaffStable(summoningStaffProperties, "stablesummoningstaff", "staffstable"));
        ObjectManager.addItem("bloodsummoningstaff", () -> new ItemStaffBlood(summoningStaffProperties, "bloodsummoningstaff", "staffblood"));
        ObjectManager.addItem("sturdysummoningstaff", () -> new ItemStaffSturdy(summoningStaffProperties, "sturdysummoningstaff", "staffsturdy"));
        ObjectManager.addItem("savagesummoningstaff", () -> new ItemStaffSavage(summoningStaffProperties, "savagesummoningstaff", "staffsavage"));
        // Utilities:
        ObjectManager.addBlock("summoningpedestal", () -> new BlockSummoningPedestal(Block.Properties.of().sound(SoundType.METAL).strength(5, 10)), false);
        ObjectManager.addBlock("equipmentforge_lesser", () -> new BlockEquipmentForge(Block.Properties.of().sound(SoundType.WOOD).strength(5, 10), 1), false);
        ObjectManager.addBlock("equipmentforge_greater", () -> new BlockEquipmentForge(Block.Properties.of().sound(SoundType.STONE).strength(5, 20), 2), false);
        ObjectManager.addBlock("equipmentforge_master", () -> new BlockEquipmentForge(Block.Properties.of().sound(SoundType.METAL).strength(5, 1000), 3), false);
        ObjectManager.addBlock("equipment_infuser", () -> new EquipmentInfuserBlock(Block.Properties.of().sound(SoundType.METAL).strength(5, 1000)), false);
        ObjectManager.addBlock("equipment_station", () -> new EquipmentStationBlock(Block.Properties.of().sound(SoundType.METAL).strength(5, 1000)), false);

        // Building Blocks:

        BlockManager.addDungeonBlocks("desert");
        BlockManager.addDungeonBlocks("shadow");
        BlockManager.addDungeonBlocks("demon");
        BlockManager.addDungeonBlocks("aberrant");
        BlockManager.addDungeonBlocks("ashen");
        BlockManager.addDungeonBlocks("stream");
        ObjectManager.addBlock("soulcubedemonic", () -> new BlockBase(Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), "soulcubedemonic"), false);
        ObjectManager.addBlock("soulcubeundead", () -> new BlockBase(Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), "soulcubeundead"), false);
        ObjectManager.addBlock("soulcubeaberrant", () -> new BlockBase(Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), "soulcubeaberrant"), false);
        ObjectManager.addBlock("propolis", () -> new HiveBlock(Block.Properties.of().mapColor(MapColor.CLAY).sound(SoundType.WET_GRASS).strength(0.6F).randomTicks(), "propolis"), false);
        ObjectManager.addBlock("veswax", () -> new HiveBlock(Block.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(0.6F).randomTicks(), "veswax"), false);


        // Effect Blocks:
        Block.Properties fireProperties = Block.Properties.of().mapColor(MapColor.FIRE).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion();
        Block.Properties brightFireProperties = Block.Properties.of().mapColor(MapColor.FIRE).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion().lightLevel((BlockState blockState) -> 15);
        ObjectManager.addSound("frostfire", "block.frostfire");
        ObjectManager.addBlock("frostfire", () -> new BlockFrostfire(fireProperties), false);
        ObjectManager.addSound("icefire", "block.icefire");
        ObjectManager.addBlock("icefire", () -> new BlockIcefire(fireProperties), false);
        ObjectManager.addSound("hellfire", "block.hellfire");
        ObjectManager.addBlock("hellfire", () -> new BlockHellfire(brightFireProperties), false);
        ObjectManager.addSound("doomfire", "block.doomfire");
        ObjectManager.addBlock("doomfire", () -> new BlockDoomfire(brightFireProperties), false);
        ObjectManager.addSound("primefire", "block.primefire");
        ObjectManager.addBlock("primefire", () -> new BlockPrimefire(brightFireProperties), false);
        ObjectManager.addSound("scorchfire", "block.scorchfire");
        ObjectManager.addBlock("scorchfire", () -> new BlockScorchfire(brightFireProperties), false);
        ObjectManager.addSound("shadowfire", "block.shadowfire");
        ObjectManager.addBlock("shadowfire", () -> new BlockShadowfire(fireProperties), false);
        ObjectManager.addSound("smitefire", "block.smitefire");
        ObjectManager.addBlock("smitefire", () -> new BlockSmitefire(brightFireProperties), false);

        Block.Properties cloudProperties = Block.Properties.of().mapColor(MapColor.NONE).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion();
        ObjectManager.addSound("frostcloud", "block.frostcloud");
        ObjectManager.addBlock("frostcloud", () -> new BlockFrostCloud(cloudProperties), false);
        ObjectManager.addSound("poisoncloud", "block.poisoncloud");
        ObjectManager.addBlock("poisoncloud", () -> new BlockPoisonCloud(cloudProperties), false);
        ObjectManager.addSound("poopcloud", "block.poopcloud");
        ObjectManager.addBlock("poopcloud", () -> new BlockPoopCloud(cloudProperties), false);

        Block.Properties webProperties = Block.Properties.of().mapColor(MapColor.WOOL).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion();
        ObjectManager.addBlock("quickweb", () -> new BlockQuickWeb(webProperties), false);
        ObjectManager.addBlock("frostweb", () -> new BlockFrostweb(webProperties), false);

        ObjectManager.addDamageType("ooze");
        ObjectManager.addDamageType("acid");
    }

    /**
     * Determines the Equipment Sharpness repair amount of the provided itemstack. Stack size is not taken into account.
     *
     * @param itemStack The itemstack to check the item and nbt data of.
     * @return The amount of Sharpness the provided itemstack restores.
     */
    public int getEquipmentSharpnessRepair(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }

        Item item = itemStack.getItem();
        Object locationObj = LMHelperClass.convertObjectToDesired(item, "resourcelocation");
        if (locationObj instanceof ResourceLocation location) {
            if (ItemConfig.lowEquipmentSharpnessItems != null) {
                for (String itemId : ItemConfig.lowEquipmentSharpnessItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.lowEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.mediumEquipmentSharpnessItems != null) {
                for (String itemId : ItemConfig.mediumEquipmentSharpnessItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.mediumEquipmentRepairAmount;
                    }
                }
            }

            if (ItemConfig.highEquipmentSharpnessItems != null) {
                for (String itemId : ItemConfig.highEquipmentSharpnessItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.highEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.maxEquipmentSharpnessItems != null) {
                for (String itemId : ItemConfig.maxEquipmentSharpnessItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemEquipment.SHARPNESS_MAX;
                    }
                }
            }

        }
        return 0;
    }

    /**
     * Determines the Equipment Mana repair amount of the provided itemstack. Stack size is not taken into account.
     *
     * @param itemStack The itemstack to check the item and nbt data of.
     * @return The amount of Mana the provided itemstack restores.
     */
    public int getEquipmentManaRepair(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }

        Item item = itemStack.getItem();
        Object locationObj = LMHelperClass.convertObjectToDesired(item, "resourcelocation");
        if (locationObj instanceof ResourceLocation location) {
            if (item instanceof ChargeItem) {
                return ItemConfig.highEquipmentRepairAmount;
            }
            if (ItemConfig.lowEquipmentManaItems != null) {
                for (String itemId : ItemConfig.lowEquipmentManaItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.lowEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.mediumEquipmentManaItems != null) {
                for (String itemId : ItemConfig.mediumEquipmentManaItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.mediumEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.highEquipmentManaItems != null) {
                for (String itemId : ItemConfig.highEquipmentManaItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.highEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.maxEquipmentManaItems != null) {
                for (String itemId : ItemConfig.maxEquipmentManaItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemEquipment.MANA_MAX;
                    }
                }
            }

        }
        return 0;
    }

}
