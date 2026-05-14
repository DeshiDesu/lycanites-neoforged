package com.lycanitesmobs.core.util.helpers;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.config.ConfigDebug;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.lang.reflect.Field;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.lycanitesmobs.LycanitesMobs.configReady;
import static com.lycanitesmobs.LycanitesMobs.earlyDebug;

public class LMHelperClass {
    public static final Set<String> errorMessagesLogged = new HashSet<>();
    public static final Set<String> warningMessagesLogged = new HashSet<>();
    public static final Set<String> infoMessagesLogged = new HashSet<>();
    public static Logger LOGGER = LycanitesMobs.LOGGER;

    /**
     * Prints an info message into the console.
     *
     * @param key     The debug config key to use, if empty, the message is always printed.
     * @param message The message to print.
     */
    public static void logInfo(String key, String message) {
        if ("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
            LOGGER.info("[LycanitesMobs] [Info] [" + key + "] " + message);
        }
    }

    /**
     * Prints an info debug into the console.
     *
     * @param key     The debug config key to use, if empty, the message is always printed.
     * @param message The message to print.
     */
    public static void logDebug(String key, String message) {
        if ("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
            LOGGER.debug("[LycanitesMobs] [Debug] [" + key + "] " + message);
        }
    }

    /**
     * Prints an info warning into the console.
     *
     * @param key     The debug config key to use, if empty, the message is always printed.
     * @param message The message to print.
     */
    public static void logWarning(String key, String message) {
        if ("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
            LOGGER.warn("[LycanitesMobs] [WARNING] [" + key + "] " + message);
        }
    }

    /**
     * Prints an error message into the console.
     *
     * @param message The error message to print.
     */
    public static void logError(String message) {
        LOGGER.error("[LycanitesMobs] " + message);
    }

    public static void logErrorMessageOnce(String errorMessage) {
        if (!errorMessagesLogged.contains(errorMessage)) {
            LOGGER.error("[Lycanites]: " + errorMessage);
            errorMessagesLogged.add(errorMessage);
        }
    }

    public static void logErrorMessage(String errorMessage) {
        LOGGER.error("[Lycanites]: " + errorMessage);
    }

    public static void logWarningMessageOnce(String errorMessage) {
        if (!warningMessagesLogged.contains(errorMessage)) {
            LOGGER.warn("[Lycanites]: " + errorMessage);
            warningMessagesLogged.add(errorMessage);
        }
    }

    public static void logWarningMessageOnceDev(String errorMessage) {
        if (!FMLEnvironment.production && !warningMessagesLogged.contains(errorMessage)) {
            LOGGER.warn("[Lycanites]: " + errorMessage);
            warningMessagesLogged.add(errorMessage);
        }
    }

    public static void logWarningMessage(String errorMessage) {
        LOGGER.warn("[Lycanites]: " + errorMessage);
    }

    public static void logErrorMessageCatchable(String errorMessage, Throwable e) {
        LOGGER.error("[Lycanites]: " + errorMessage, e);
    }

    public static void logErrorMessageOnceCatchable(String errorMessage, Throwable e) {
        if (!errorMessagesLogged.contains(errorMessage)) {
            LOGGER.error("[Lycanites]: " + errorMessage, e);
            errorMessagesLogged.add(errorMessage);
        }
    }

    public static void logInfoMessageOnce(String info) {
        if (!infoMessagesLogged.contains(info)) {
            LOGGER.info("[Lycanites]: " + info);
            infoMessagesLogged.add(info);
        }
    }

    public static void logInfoMessageOnceDev(String info) {
        if (!FMLEnvironment.production && !infoMessagesLogged.contains(info)) {
            LOGGER.info("[Lycanites]: " + info);
            infoMessagesLogged.add(info);
        }
    }

    public static void logInfoMessage(String info) {
        LOGGER.info("[Lycanites]: " + info);
    }

    public static void logInfoMessageDev(String info) {
        if (!FMLEnvironment.production) {
            LOGGER.info("[Lycanites]: " + info);
        }
    }

    public static Object convertObjectToDesired(Object input, String outputType) {
        return switch (outputType.toLowerCase()) {
            case "integer" -> convertToInteger(input);
            case "double" -> convertToDouble(input);
            case "float" -> convertToFloat(input);
            case "boolean" -> convertToBoolean(input);
            case "resourcelocation" -> convertToResourceLocation(input);
            case "vec3i" -> convertToVec3i(input);
            default -> input;
        };
    }

    public static Vector3d convertToVec3d(Object input) {
        if (input instanceof Vec3 v) {

            return new Vector3d(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        } else if (input instanceof Vector3f v) {
            return new Vector3d(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        } else if (input instanceof Vector3d v) {
            return new Vector3d(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        }
        return null;
    }

    public static Vec3 convertToVec3(Object input) {
        if (input instanceof Vector3i v) {
            return new Vec3(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        } else if (input instanceof Vector3f v) {
            return new Vec3(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        } else if (input instanceof Vector3d v) {
            return new Vec3(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        }
        return null;
    }

    public static Vec3i convertToVec3i(Object input) {
        if (input instanceof Vec3 v) {
            return new Vec3i(convertToInteger(v.x), convertToInteger(v.y), convertToInteger(v.z));
        } else if (input instanceof Vector3f v) {
            return new Vec3i(convertToInteger(v.x), convertToInteger(v.y), convertToInteger(v.z));
        } else if (input instanceof Vector3d v) {
            return new Vec3i(convertToInteger(v.x), convertToInteger(v.y), convertToInteger(v.z));
        }
        return null;
    }

    public static ResourceLocation convertToResourceLocation(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof ResourceLocation) {
            return (ResourceLocation) input;
        } else if (input instanceof String) {
            return new ResourceLocation((String) input);
        } else if (input instanceof Item item) {
            return ForgeRegistries.ITEMS.getKey(item);
        } else if (input instanceof Block block) {
            return ForgeRegistries.BLOCKS.getKey(block);
        } else if (input instanceof EntityType<?> entityType) {
            return ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        } else if (input instanceof Biome) {
            throw new IllegalArgumentException("Biome cannot be gotten from ForgeRegistries anymore - please use LMHelperClass.convertBiomeToResourceLocation(level, biome) to get from Level");
        } else if (input instanceof Fluid fluid) {
            return ForgeRegistries.FLUIDS.getKey(fluid);
        } else if (input instanceof MobEffect mobEffect) {
            return ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        } else if (input instanceof SoundEvent soundEvent) {
            return ForgeRegistries.SOUND_EVENTS.getKey(soundEvent);
        } else if (input instanceof Potion potion) {
            return ForgeRegistries.POTIONS.getKey(potion);
        } else if (input instanceof Enchantment enchantment) {
            return ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        } else if (input instanceof BlockEntityType<?> blockEntityType) {
            return ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntityType);
        } else if (input instanceof ParticleType<?> particleType) {
            return ForgeRegistries.PARTICLE_TYPES.getKey(particleType);
        } else if (input instanceof MenuType<?> menuType) {
            return ForgeRegistries.MENU_TYPES.getKey(menuType);
        } else if (input instanceof PaintingVariant paintingVariant) {
            return ForgeRegistries.PAINTING_VARIANTS.getKey(paintingVariant);
        } else if (input instanceof RecipeType<?> recipeType) {
            return ForgeRegistries.RECIPE_TYPES.getKey(recipeType);
        } else if (input instanceof RecipeSerializer<?> recipeSerializer) {
            return ForgeRegistries.RECIPE_SERIALIZERS.getKey(recipeSerializer);
        } else if (input instanceof Attribute attribute) {
            return ForgeRegistries.ATTRIBUTES.getKey(attribute);
        } else if (input instanceof StatType<?> statType) {
            return ForgeRegistries.STAT_TYPES.getKey(statType);
        } else if (input instanceof ArgumentTypeInfo<?, ?> argumentTypeInfo) {
            return ForgeRegistries.COMMAND_ARGUMENT_TYPES.getKey(argumentTypeInfo);
        } else if (input instanceof VillagerProfession villagerProfession) {
            return ForgeRegistries.VILLAGER_PROFESSIONS.getKey(villagerProfession);
        } else if (input instanceof PoiType poiType) {
            return ForgeRegistries.POI_TYPES.getKey(poiType);
        } else if (input instanceof MemoryModuleType<?> memoryModuleType) {
            return ForgeRegistries.MEMORY_MODULE_TYPES.getKey(memoryModuleType);
        } else if (input instanceof SensorType<?> sensorType) {
            return ForgeRegistries.SENSOR_TYPES.getKey(sensorType);
        } else if (input instanceof Schedule schedule) {
            return ForgeRegistries.SCHEDULES.getKey(schedule);
        } else if (input instanceof Activity activity) {
            return ForgeRegistries.ACTIVITIES.getKey(activity);
        } else if (input instanceof WorldCarver<?> worldCarver) {
            return ForgeRegistries.WORLD_CARVERS.getKey(worldCarver);
        } else if (input instanceof Feature<?> feature) {
            return ForgeRegistries.FEATURES.getKey(feature);
        } else if (input instanceof ChunkStatus chunkStatus) {
            return ForgeRegistries.CHUNK_STATUS.getKey(chunkStatus);
        } else if (input instanceof BlockStateProviderType<?> blockStateProviderType) {
            return ForgeRegistries.BLOCK_STATE_PROVIDER_TYPES.getKey(blockStateProviderType);
        } else if (input instanceof FoliagePlacerType<?> foliagePlacerType) {
            return ForgeRegistries.FOLIAGE_PLACER_TYPES.getKey(foliagePlacerType);
        } else if (input instanceof TreeDecoratorType<?> treeDecoratorType) {
            return ForgeRegistries.TREE_DECORATOR_TYPES.getKey(treeDecoratorType);
        }

        return null;
    }
    public static ResourceLocation convertToResourceLocation(Object input, RegistryAccess access) {
        if (input == null) {
            return null;
        }
        if (input instanceof ResourceLocation) {
            return (ResourceLocation) input;
        } else if (input instanceof String) {
            return new ResourceLocation((String) input);
        } else if (input instanceof Item item) {
            return access.registryOrThrow(Registries.ITEM).getKey(item);
        } else if (input instanceof Block block) {
            return access.registryOrThrow(Registries.BLOCK).getKey(block);
        } else if (input instanceof EntityType<?> entityType) {
            return access.registryOrThrow(Registries.ENTITY_TYPE).getKey(entityType);
        } else if (input instanceof Biome biome) {
            return access.registryOrThrow(Registries.BIOME).getKey(biome);
        } else if (input instanceof Fluid fluid) {
            return access.registryOrThrow(Registries.FLUID).getKey(fluid);
        } else if (input instanceof MobEffect mobEffect) {
            return access.registryOrThrow(Registries.MOB_EFFECT).getKey(mobEffect);
        } else if (input instanceof SoundEvent soundEvent) {
            return access.registryOrThrow(Registries.SOUND_EVENT).getKey(soundEvent);
        } else if (input instanceof Potion potion) {
            return access.registryOrThrow(Registries.POTION).getKey(potion);
        } else if (input instanceof Enchantment enchantment) {
            return access.registryOrThrow(Registries.ENCHANTMENT).getKey(enchantment);
        } else if (input instanceof BlockEntityType<?> blockEntityType) {
            return access.registryOrThrow(Registries.BLOCK_ENTITY_TYPE).getKey(blockEntityType);
        } else if (input instanceof ParticleType<?> particleType) {
            return access.registryOrThrow(Registries.PARTICLE_TYPE).getKey(particleType);
        } else if (input instanceof MenuType<?> menuType) {
            return access.registryOrThrow(Registries.MENU).getKey(menuType);
        } else if (input instanceof PaintingVariant paintingVariant) {
            return access.registryOrThrow(Registries.PAINTING_VARIANT).getKey(paintingVariant);
        } else if (input instanceof RecipeType<?> recipeType) {
            return access.registryOrThrow(Registries.RECIPE_TYPE).getKey(recipeType);
        } else if (input instanceof RecipeSerializer<?> recipeSerializer) {
            return access.registryOrThrow(Registries.RECIPE_SERIALIZER).getKey(recipeSerializer);
        } else if (input instanceof Attribute attribute) {
            return access.registryOrThrow(Registries.ATTRIBUTE).getKey(attribute);
        } else if (input instanceof StatType<?> statType) {
            return access.registryOrThrow(Registries.STAT_TYPE).getKey(statType);
        } else if (input instanceof ArgumentTypeInfo<?, ?> argumentTypeInfo) {
            return access.registryOrThrow(Registries.COMMAND_ARGUMENT_TYPE).getKey(argumentTypeInfo);
        } else if (input instanceof VillagerProfession villagerProfession) {
            return access.registryOrThrow(Registries.VILLAGER_PROFESSION).getKey(villagerProfession);
        } else if (input instanceof PoiType poiType) {
            return access.registryOrThrow(Registries.POINT_OF_INTEREST_TYPE).getKey(poiType);
        } else if (input instanceof MemoryModuleType<?> memoryModuleType) {
            return access.registryOrThrow(Registries.MEMORY_MODULE_TYPE).getKey(memoryModuleType);
        } else if (input instanceof SensorType<?> sensorType) {
            return access.registryOrThrow(Registries.SENSOR_TYPE).getKey(sensorType);
        } else if (input instanceof Schedule schedule) {
            return access.registryOrThrow(Registries.SCHEDULE).getKey(schedule);
        } else if (input instanceof Activity activity) {
            return access.registryOrThrow(Registries.ACTIVITY).getKey(activity);
        } else if (input instanceof WorldCarver<?> worldCarver) {
            return access.registryOrThrow(Registries.CARVER).getKey(worldCarver);
        } else if (input instanceof Feature<?> feature) {
            return access.registryOrThrow(Registries.FEATURE).getKey(feature);
        } else if (input instanceof ChunkStatus chunkStatus) {
            return access.registryOrThrow(Registries.CHUNK_STATUS).getKey(chunkStatus);
        } else if (input instanceof BlockStateProviderType<?> blockStateProviderType) {
            return access.registryOrThrow(Registries.BLOCK_STATE_PROVIDER_TYPE).getKey(blockStateProviderType);
        } else if (input instanceof FoliagePlacerType<?> foliagePlacerType) {
            return access.registryOrThrow(Registries.FOLIAGE_PLACER_TYPE).getKey(foliagePlacerType);
        } else if (input instanceof TreeDecoratorType<?> treeDecoratorType) {
            return access.registryOrThrow(Registries.TREE_DECORATOR_TYPE).getKey(treeDecoratorType);
        }

        return null;
    }

    public static Boolean convertToBoolean(Object input) {
        if (input instanceof Boolean) {
            return (Boolean) input;
        } else if (input instanceof String) {
            String stringValue = ((String) input).toLowerCase();
            if ("true".equals(stringValue)) {
                return true;
            } else if ("false".equals(stringValue)) {
                return false;
            }
        }
        return null;
    }


    public static Integer convertToInteger(Object input) {
        if (input instanceof Integer) {
            return (Integer) input;
        } else if (input instanceof Double || input instanceof Float) {
            return ((Number) input).intValue();
        } else {
            return null;
        }
    }

    public static Double convertToDouble(Object input) {
        if (input instanceof Double) {
            return (Double) input;
        } else if (input instanceof Integer || input instanceof Float) {
            return ((Number) input).doubleValue();
        } else {
            return null;
        }
    }

    public static Float convertToFloat(Object input) {
        if (input instanceof Float) {
            return (Float) input;
        } else if (input instanceof Integer || input instanceof Double) {
            return ((Number) input).floatValue();
        } else {
            return null;
        }
    }

    public static DamageSource damageSourceFromResourceKey(Level level, ResourceKey<DamageType> damageTypeResourceKey) {
        ResourceKey<Registry<DamageType>> damageTypeRegistryKey = ResourceKey.createRegistryKey(new ResourceLocation("damage_type"));
        Registry<DamageType> damageTypeRegistry = level.registryAccess().registryOrThrow(damageTypeRegistryKey);
        Holder<DamageType> holder = damageTypeRegistry.getHolderOrThrow(damageTypeResourceKey);
        return new DamageSource(holder);
    }

    public static double wrapDegrees(double p_76138_0_) {
        double lvt_2_1_ = p_76138_0_ % 360.0;
        if (lvt_2_1_ >= 180.0) {
            lvt_2_1_ -= 360.0;
        }
        if (lvt_2_1_ < -180.0) {
            lvt_2_1_ += 360.0;
        }
        return lvt_2_1_;
    }

    public static double clamp(double p_151237_0_, double p_151237_2_, double p_151237_4_) {
        if (p_151237_0_ < p_151237_2_) {
            return p_151237_2_;
        } else {
            return Math.min(p_151237_0_, p_151237_4_);
        }
    }

    public static boolean hasTag(Object obj, Object tag) {
        if (obj instanceof BlockState block) {
            if (tag instanceof TagKey<?> tagKey) {
                return block.getTags().anyMatch(blockTagKey -> blockTagKey == tagKey);
            } else if (tag instanceof String string) {
                return block.getTags().anyMatch(blockTagKey -> blockTagKey.location().equals(new ResourceLocation(string)));
            }
        }
        return false;
    }

    public static float getBrightness(Entity entity) {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(entity.getX(), 0.0, entity.getZ());
        if (entity.level().hasChunkAt(blockpos$mutable)) {
            blockpos$mutable.setY((int) Math.floor(entity.getEyeY()));
            return entity.level().getBrightness(LightLayer.BLOCK, blockpos$mutable);
        } else {
            return 0.0F;
        }
    }

    public static <T> T cast(Object o) {
        return (T) o;
    }

    /**
     * Runs the provided Runnable if the current environment matches the specified environment.
     *
     * @param prodEnvironment If true, run the code in a production environment. If false, run the code in a development environment.
     * @param runnable        The code to run in the specified environment.
     */
    public static void environmentRunnable(boolean prodEnvironment, Runnable runnable) {
        if (prodEnvironment && FMLEnvironment.production) {
            runnable.run();
        } else if (!FMLEnvironment.production) {
            runnable.run();
        }
    }

    public static class Materials {
        public static boolean isLeaves(Block block) {
            return hasTag(block, BlockTags.LEAVES);
        }

        public static boolean isDirt(Block block) {
            return hasTag(block, BlockTags.DIRT);
        }

        public static boolean isCoral(Block block) {
            return hasTag(block, BlockTags.CORAL_PLANTS);
        }

        public static boolean isCrop(Block block) {
            return hasTag(block, BlockTags.CROPS);
        }

        public static boolean isPlant(Block block) {
            return block instanceof IPlantable;
        }

        public static boolean isReplaceablePlant(Block block) {
            return hasTag(block, BlockTags.REPLACEABLE) && isPlant(block);
        }

        public static boolean isUnderwaterPlant(Block block) {
            return hasTag(block, BlockTags.UNDERWATER_BONEMEALS) && isPlant(block);
        }
    }

    /**
     * Fixes the max health value (why would it be set so low?!) by scanning for matching fields and correcting them.
     * This is a workaround for remapName not working.
     */
    public static void fixMaxHealth() {
        try {
            Field[] classFields = RangedAttribute.class.getDeclaredFields();
            for (Field field : classFields) {
                if (field.getType().equals(double.class)) {
                    field.setAccessible(true);
                    if ((double) field.get(Attributes.MAX_HEALTH) == 1024.0D) {
                        field.setDouble(Attributes.MAX_HEALTH, Double.MAX_VALUE);
                    }
                }
            }
        } catch (Exception e) {
            logError("Unable to fix the Maximum Mob health limit, all bosses will be capped at 1024 health, if you are using the latest version of Lycanites Mobs please report this as a bug with the following stack trace:");
            e.printStackTrace();
        }
    }

    // ==================================================
    //                      Raytrace
    // ==================================================
    // ========== Raytrace All ==========
    public static HitResult raytrace(Level world, double x, double y, double z, double tx, double ty, double tz, float borderSize, Entity entity, HashSet<Entity> excluded) {
        Vec3 startVec = new Vec3(x, y, z);
        Vec3 lookVec = new Vec3(tx - x, ty - y, tz - z);
        Vec3 endVec = new Vec3(tx, ty, tz);
        float minX = (float) (x < tx ? x : tx);
        float minY = (float) (y < ty ? y : ty);
        float minZ = (float) (z < tz ? z : tz);
        float maxX = (float) (x > tx ? x : tx);
        float maxY = (float) (y > ty ? y : ty);
        float maxZ = (float) (z > tz ? z : tz);

        // Get Block Collision:
        HitResult collision = world.clip(new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        startVec = new Vec3(x, y, z);

        // Get Entity Collision:
        if (excluded != null) {
            AABB bb = new AABB(minX, minY, minZ, maxX, maxY, maxZ).expandTowards(borderSize, borderSize, borderSize);
            List<Entity> allHitEntities = world.getEntities(null, bb);
            Entity closestHitEntity = null;
            double closestEntDistance = Float.POSITIVE_INFINITY;
            for (Entity hitEntity : allHitEntities) {
                if (hitEntity.isPickable() && !excluded.contains(hitEntity)) {
                    double entDistance = startVec.distanceTo(hitEntity.position());
                    if (entDistance < closestEntDistance) {
                        closestEntDistance = entDistance;
                        closestHitEntity = hitEntity;
                    }
                }
            }
            if (closestHitEntity != null) {
                collision = new EntityHitResult(closestHitEntity);
            }
        }

        return collision;
    }


    // ==================================================
    //                      Seasonal
    // ==================================================
    public static boolean isValentines() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.FEBRUARY && calendar.get(Calendar.DAY_OF_MONTH) >= 7 && calendar.get(Calendar.DAY_OF_MONTH) <= 14;
    }

    protected static Calendar easterCalendar;

    public static boolean isEaster() {
        Calendar calendar = Calendar.getInstance();

        if (easterCalendar == null) {
            int Y = calendar.get(Calendar.YEAR);
            int a = Y % 19;
            int b = Y / 100;
            int c = Y % 100;
            int d = b / 4;
            int e = b % 4;
            int f = (b + 8) / 25;
            int g = (b - f + 1) / 3;
            int h = (19 * a + b - d - g + 15) % 30;
            int i = c / 4;
            int k = c % 4;
            int L = (32 + 2 * e + 2 * i - h - k) % 7;
            int m = (a + 11 * h + 22 * L) / 451;
            int easterMonth = (h + L - 7 * m + 114) / 31;
            int easterDay = ((h + L - 7 * m + 114) % 31) + 1;
            easterCalendar = new GregorianCalendar(Y, easterMonth, easterDay);
        }

        long daysUntilEaster = ChronoUnit.DAYS.between(calendar.toInstant(), easterCalendar.toInstant());
        return daysUntilEaster <= 7 && daysUntilEaster >= 0;
    }

    public static boolean isMidsummer() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.JULY && calendar.get(Calendar.DAY_OF_MONTH) >= 10 && calendar.get(Calendar.DAY_OF_MONTH) <= 20;
    }

    public static boolean isHalloween() {
        Calendar calendar = Calendar.getInstance();
        if ((calendar.get(Calendar.DAY_OF_MONTH) >= 25 && calendar.get(Calendar.MONTH) == calendar.OCTOBER)
                || (calendar.get(Calendar.DAY_OF_MONTH) == 1 && calendar.get(Calendar.MONTH) == calendar.NOVEMBER)
        )
            return true;
        return false;
    }

    public static boolean isYuletide() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) >= 10 && calendar.get(Calendar.DAY_OF_MONTH) <= 25;
    }

    public static boolean isYuletidePeak() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) == 25;
    }

    public static boolean isNewYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.JANUARY && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public static int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }
}
