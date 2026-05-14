package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.effect.EffectBase;
import com.lycanitesmobs.core.container.block.EquipmentForgeContainer;
import com.lycanitesmobs.core.container.block.EquipmentInfuserContainer;
import com.lycanitesmobs.core.container.block.EquipmentStationContainer;
import com.lycanitesmobs.core.container.block.SummoningPedestalContainer;
import com.lycanitesmobs.core.container.creature.CreatureContainer;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.lycanitesmobs.core.tabs.LMItemsGroup.itemNames;


public class ObjectManager {

    // ========== Items/Blocks ==========
    public static final Map<String, Supplier<? extends Item>> items = new HashMap<>();
    public static final Map<String, Lazy<? extends BlockItem>> blockItems = new HashMap<>();
    public static final Map<String, Lazy<? extends Block>> blocks = new HashMap<>();
    public static final Map<String, Lazy<? extends LiquidBlock>> liquidBlocks = new HashMap<>();
    // ========== Entities ==========
    public static final Map<String, Lazy<? extends EntityType<?>>> entityTypes = new HashMap<>();
    // ========== Containers ==========
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, LycanitesMobs.MODID);
    public static ObjectManager INSTANCE;
    public static ModInfo currentModInfo;
    public static Map<String, Class> tileEntities = new HashMap<>();
    public static Map<Class<? extends BlockEntity>, BlockEntityType<? extends BlockEntity>> tileEntityTypes = new HashMap<>();
    public static Map<String, ForgeFlowingFluid> fluids = new HashMap<>();
    public static Map<Block, Item> buckets = new HashMap<>();
    // ========== Creative Mode Tabs ==========
    public static Map<Item, ModInfo> itemGroups = new HashMap<>();
    // ========== Damage Sources/Effects ==========
    public static Map<String, EffectBase> effects = new HashMap<>();
    // ========== Sounds ==========
    public static Map<String, ResourceLocation> soundNames = new HashMap<>();
    public static Map<String, Lazy<? extends SoundEvent>> sounds = new HashMap<>();
    public static Map<String, Class<? extends Entity>> specialEntities = new HashMap<>();
    public static Map<Class<? extends Entity>, Constructor<? extends Entity>> specialEntityConstructors = new HashMap<>();
    public static Map<Class<? extends Entity>, EntityType<? extends Entity>> specialEntityTypes = new HashMap<>();

    public static Map<String, ResourceKey<DamageType>> damageTypeKeys = new HashMap<>();


    /**
     * The next available network id for special entities to register by.
     **/
    protected static int nextSpecialEntityNetworkId = 0;

    public static ObjectManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ObjectManager();
        return INSTANCE;
    }

    // ==================================================
    //                        Setup
    // ==================================================
    public static void setCurrentModInfo(ModInfo group) {
        currentModInfo = group;
    }

    /**
     * Generates the next available special entity network id to register with.
     *
     * @return The next special entity network id.
     */
    public static int getNextSpecialEntityNetworkId() {
        return nextSpecialEntityNetworkId++;
    }

    // ==================================================
    //                        Add
    // ==================================================
    // ========== Block ==========
    public static void addBlock(String name, Supplier<? extends Block> block, boolean isLiquid) {
        if (isLiquid) {
            liquidBlocks.put(name, Lazy.of(() -> (LiquidBlock) block.get()));
        } else {
            Item.Properties blockItemProperties = new Item.Properties();
            blocks.put(name, Lazy.of(block));
            blockItems.put(name, Lazy.of(() -> new BlockItem(blocks.get(name).get(), blockItemProperties)));
        }
    }

    // ========== Fluid ==========
    public static Fluid addFluid(String name, ForgeFlowingFluid fluid) {
        fluids.put(name, fluid);
        return fluid;
    }

    // ========== Item ==========
    public static void addItem(String name, Lazy<? extends Item> itemSupplier) {
        if (!items.containsKey(name)) {
            RegistryObject<Item> item = LycanitesMobs.ITEMS.register(name, itemSupplier);
            //LycanitesMobs.ITEMS.register(name,item);
            items.put(name, item);
            itemNames.add(name);
        }
    }

    // ========== Tile Entity ==========
    public static Class addTileEntity(String name, Class tileEntityClass) {
        name = name.toLowerCase();
        tileEntities.put(name, tileEntityClass);
        return tileEntityClass;
    }

    // ========== Potion Effect ==========
    public static EffectBase addPotionEffect(String name, boolean isBad, int color, boolean goodEffect) {
        EffectBase effect = new EffectBase(name, isBad, color);
        effects.put(name, effect);
        ObjectLists.addEffect(goodEffect ? "buffs" : "debuffs", effect, name);

        return effect;
    }

    // ========== Special Entity ==========
    public static void addSpecialEntity(String name, Class<? extends Entity> entityClass, Constructor<? extends Entity> specialEntityConstructor) {
        specialEntities.put(name, entityClass);
        specialEntityConstructors.put(entityClass, specialEntityConstructor);
    }

    // ========== Damage Source ==========
    public static void addDamageType(String name) {
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(LycanitesMobs.MODID, name.toLowerCase()));
        damageTypeKeys.put(name.toLowerCase(), key);
    }

    // ========== Sound ==========
    public static void addSound(String name, String path) {
        name = name.toLowerCase();
        ResourceLocation resourceLocation = new ResourceLocation(LycanitesMobs.MODID, path);
        soundNames.put(name, resourceLocation);
        sounds.put(name, Lazy.of(() -> SoundEvent.createVariableRangeEvent(resourceLocation)));
    }

    // ==================================================
    //                        Get
    // ==================================================
    // ========== Block ==========
    public static Block getBlock(String name) {
        name = name.toLowerCase();
        if (!blocks.containsKey(name))
            return null;
        return blocks.get(name).get();
    }

    public static LiquidBlock getFluidBlock(String name) {
        name = name.toLowerCase();
        if (!liquidBlocks.containsKey(name))
            return null;
        return liquidBlocks.get(name).get();
    }

    // ========== Item ==========
    public static Item getItem(String name) {
        name = name.toLowerCase();
        if (!items.containsKey(name))
            return null;
        return items.get(name).get();
    }

    // ========== Fluid ==========
    public static ForgeFlowingFluid getFluid(String name) {
        name = name.toLowerCase();
        if (!fluids.containsKey(name))
            return null;
        return fluids.get(name);
    }

    // ========== Tile Entity ==========
    public static Class getTileEntity(String name) {
        name = name.toLowerCase();
        if (!tileEntities.containsKey(name)) return null;
        return tileEntities.get(name);
    }

    // ========== Potion Effect ==========
    public static EffectBase getEffect(String name) {
        name = name.toLowerCase();
        if (!effects.containsKey(name)) return null;
        return effects.get(name);
    }

    // ========== Damage Source ==========
    public static DamageSource getDamageSource(Level level, String name) {
        ResourceKey<DamageType> key = damageTypeKeys.get(name.toLowerCase());
        if (key == null) {
            LMHelperClass.logWarning("ObjectManager", "Unknown damage type: " + name);
            return level.damageSources().generic();
        }
        Holder<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
        return new DamageSource(holder);
    }

    public static DamageSource getDamageSource(Level level, String name, @Nullable Entity direct, @Nullable Entity causing) {
        ResourceKey<DamageType> key = damageTypeKeys.get(name.toLowerCase());
        if (key == null) {
            LMHelperClass.logWarning("ObjectManager", "Unknown damage type: " + name);
            return level.damageSources().generic();
        }
        Holder<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
        return new DamageSource(holder, direct, causing);
    }

    // ========== Sound ==========
    public static SoundEvent getSound(String name) {
        name = name.toLowerCase();
        if (!sounds.containsKey(name))
            return null;
        return sounds.get(name).get();
    }


    @Nonnull
    public static EntityType<? extends LivingEntity> getEntityType(String name) {
        LMHelperClass.environmentRunnable(false, () -> {
            LMHelperClass.logInfo("", "EntityType: " + name);
        });
        return (EntityType<? extends LivingEntity>) entityTypes.get(name).get();
    }

    public static void addEntityType(String name, EntityType.Builder<? extends LivingEntity> builder) {
        entityTypes.put(name, () -> builder.build(name));

    }

    public static void registerContainers(IEventBus event) {

        MENUS.register(event);
        MENUS.register("creature", () -> CreatureContainer.TYPE);
        MENUS.register("summoning_pedestal", () -> SummoningPedestalContainer.TYPE);
        MENUS.register("equipment_forge", () -> EquipmentForgeContainer.TYPE);
        MENUS.register("equipment_infuser", () -> EquipmentInfuserContainer.TYPE);
        MENUS.register("equipment_station", () -> EquipmentStationContainer.TYPE);
    }
}
