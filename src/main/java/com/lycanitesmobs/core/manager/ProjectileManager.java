package com.lycanitesmobs.core.manager;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.entity.projectile.generic.ModelProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.RapidFireProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.hellfire.*;
import com.lycanitesmobs.core.entity.projectile.misc.EntityDevilGatling;
import com.lycanitesmobs.core.entity.projectile.misc.EntityShadowfireBarrier;
import com.lycanitesmobs.core.entity.projectile.misc.LaserEndProjectileEntity;
import com.lycanitesmobs.core.entity.special.PortalEntity;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.util.EntityFactory;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ProjectileManager extends JSONLoader {
    public static ProjectileManager INSTANCE;

    /**
     * A map of all projectiles by name.
     **/
    public Map<String, ProjectileInfo> projectiles = new HashMap<>();

    /**
     * A map of old projectile classes that are hardcoded instead of using json definitions that use the default item sprite renderer.
     **/
    public Map<String, Class<? extends Entity>> oldSpriteProjectiles = new HashMap<>();

    /**
     * A map of old projectiles that use the obj model renderer. Newer json based projectiles provide their model class in their ProjectileInfo definition instead.
     **/
    public Map<String, Class<? extends Entity>> oldModelProjectiles = new HashMap<>();

    /**
     * A map of old projectile classes to types for creating new instances.
     **/
    public Map<Class<? extends Entity>, EntityType<? extends BaseProjectileEntity>> oldProjectileTypes = new HashMap<>();

    /**
     * A map of old projectile classes to simple names for translation, etc.
     **/
    public Map<Class<? extends Entity>, String> oldProjectileNames = new HashMap<>();

    /**
     * Returns the main Projectile Manager instance or creates it and returns it.
     **/
    public static ProjectileManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProjectileManager();
        }
        return INSTANCE;
    }

    /**
     * Called during startup and initially loads everything in this manager.
     *
     * @param modInfo The mod loading this manager.
     */
    public void startup(ModInfo modInfo) {
        this.loadAllFromJSON(modInfo);
        for (ProjectileInfo projectileInfo : this.projectiles.values()) {
            projectileInfo.load();
        }
        this.loadOldProjectiles();
        for (ProjectileInfo projectileInfo : this.projectiles.values()) {
            EntityType.Builder<?> b = EntityType.Builder.of(EntityFactory.getInstance(), MobCategory.MISC)
                    .setTrackingRange(40).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true)
                    .sized(projectileInfo.width, projectileInfo.height);
            LycanitesMobs.ENTITY_TYPES.register(projectileInfo.getName(), () -> (EntityType<?>) b.build(projectileInfo.getName()));
        }
        for (String projectileInfo : this.oldSpriteProjectiles.keySet()) {
            LycanitesMobs.ENTITY_TYPES.register(projectileInfo, () -> this.createEntityType(projectileInfo, this.oldSpriteProjectiles.get(projectileInfo)));
        }
        for (String projectileInfo : this.oldModelProjectiles.keySet()) {
            LycanitesMobs.ENTITY_TYPES.register(projectileInfo, () -> this.createEntityType(projectileInfo, this.oldModelProjectiles.get(projectileInfo)));
        }
    }


    /**
     * Loads all JSON Creature Types. Should be done before creatures are loaded so that they can find their type on load.
     **/
    public void loadAllFromJSON(ModInfo groupInfo) {
        this.loadAllJson(groupInfo, "Projectile", "projectiles", "name", true, null, FileLoader.COMMON, StreamLoader.COMMON);
        LMHelperClass.logDebug("Projectile", "Complete! " + this.projectiles.size() + " JSON Projectile Info Loaded In Total.");
    }

    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        ProjectileInfo projectileInfo = new ProjectileInfo(modInfo);
        if (!projectileInfo.loadFromJSON(json)) {
            return;
        }
        if (projectileInfo.getName() == null) {
            LMHelperClass.logWarning("", "[Projectile] Unable to load " + loadGroup + " json due to missing name.");
            return;
        }

        // Already Exists:
        if (this.projectiles.containsKey(projectileInfo.getName())) {
            projectileInfo = this.projectiles.get(projectileInfo.getName());
            if (!projectileInfo.loadFromJSON(json)) {
                return;
            }
        }

        this.projectiles.put(projectileInfo.getName(), projectileInfo);
        return;
    }

    public void bindRegisteredTypes() {
        for (ProjectileInfo projectileInfo : this.projectiles.values()) {
            ResourceLocation id = new ResourceLocation(LycanitesMobs.MODID, projectileInfo.getName());
            EntityType<?> t = ForgeRegistries.ENTITY_TYPES.getValue(id);
            if (t == null || !id.equals(ForgeRegistries.ENTITY_TYPES.getKey(t))) continue;
            projectileInfo.entityType = (EntityType<? extends BaseProjectileEntity>) t;
            EntityFactory.getInstance().addEntityType(projectileInfo.entityType, projectileInfo.entityConstructor, projectileInfo.getName());
            projectileInfo.initAfterRegistry();
        }
    }


    /**
     * Creates an Entity Type for older projectiles.
     *
     * @param entityName  The projectile name to register with.
     * @param entityClass The projectile entity class to register.
     * @return The projectile's Entity Type.
     */
    public EntityType createEntityType(String entityName, Class<? extends Entity> entityClass) {
        // LMHelperClass.logInfo("", "EntityClass: " + entityClass + ", EntityName: " + entityName);
        EntityType.Builder entityTypeBuilder = EntityType.Builder.of(EntityFactory.getInstance(), MobCategory.MISC);
        entityTypeBuilder.setTrackingRange(40);
        entityTypeBuilder.setUpdateInterval(3);
        entityTypeBuilder.setShouldReceiveVelocityUpdates(true);
        entityTypeBuilder.sized(0.25F, 0.25F);
        EntityType entityType = entityTypeBuilder.build(entityName);
        try {
            EntityFactory.getInstance().addEntityType(entityType, entityClass.getConstructor(EntityType.class, Level.class), entityName.toLowerCase());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        LMHelperClass.logDebug("Projectile", "Added (Projectile) Entity Type: " + entityName + " Type: " + entityType);
        this.oldProjectileTypes.put(entityClass, entityType);
        return entityType;
    }

    /**
     * Gets a projectile by name.
     *
     * @param projectileName The name of the projectile to get.
     * @return The Projectile Info.
     */
    @Nullable
    public ProjectileInfo getProjectile(String projectileName) {
        if (!this.projectiles.containsKey(projectileName)) {
            return null;
        }
        return this.projectiles.get(projectileName);
    }

    /**
     * Gets a Projectile Entity Type by name.
     *
     * @param projectileName The name of the projectile to get.
     * @return The Entity Type or null.
     */
    @Nullable
    public EntityType<? extends BaseProjectileEntity> getEntityType(String projectileName) {
        ProjectileInfo projectileInfo = this.getProjectile(projectileName);
        if (projectileInfo == null)
            return null;
        return projectileInfo.getEntityType();
    }

    /**
     * Called during early start up, loads all items.
     **/
    public void loadOldProjectiles() {
        this.addOldProjectile("summoningportal", PortalEntity.class);
        this.addOldProjectile("rapidfire", RapidFireProjectileEntity.class);
        this.addOldProjectile("laserend", LaserEndProjectileEntity.class);

        this.addOldProjectile("shadowfirebarrier", EntityShadowfireBarrier.class, false);
        this.addOldProjectile("hellfirewall", EntityHellfireWall.class, false);
        this.addOldProjectile("hellfireorb", EntityHellfireOrb.class, false);
        this.addOldProjectile("hellfirewave", EntityHellfireWave.class, false);
        this.addOldProjectile("hellfirewavepart", EntityHellfireWavePart.class, false);
        this.addOldProjectile("hellfirebarrier", EntityHellfireBarrier.class, false);
        this.addOldProjectile("hellfirebarrierpart", EntityHellfireBarrierPart.class, false);
        this.addOldProjectile("devilgatling", EntityDevilGatling.class, false);
        this.addOldProjectile("hellshield", EntityHellShield.class, false);
        this.addOldProjectile("helllaser", EntityHellLaser.class, false);
        this.addOldProjectile("helllaserend", EntityHellLaserEnd.class, false);
    }

    public void addOldProjectile(String name, Class<? extends BaseProjectileEntity> entityClass) {
        this.oldProjectileNames.put(entityClass, name);
        if (ModelProjectileEntity.class.isAssignableFrom(entityClass)) {
            this.oldModelProjectiles.put(name, entityClass);
            return;
        }
        this.oldSpriteProjectiles.put(name, entityClass);
    }

    public void addOldProjectile(String name, Class<? extends BaseProjectileEntity> entityClass, boolean impactSound) {
        ModInfo modInfo = LycanitesMobs.modInfo;
        ObjectManager.addSound(name, "projectile." + name);
        if (impactSound) {
            ObjectManager.addSound(name + "_impact", "projectile." + name + ".impact");
        }
        this.addOldProjectile(name, entityClass);
    }

    public BaseProjectileEntity createOldProjectile(Class<? extends BaseProjectileEntity> projectileClass, Level world, LivingEntity entity) {
        try {
            return projectileClass.getConstructor(EntityType.class, Level.class, LivingEntity.class).newInstance(this.oldProjectileTypes.get(projectileClass), world, entity);
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
            return null;
        }
    }

    public BaseProjectileEntity createOldProjectile(Class<? extends BaseProjectileEntity> projectileClass, Level world, double x, double y, double z) {
        try {
            return projectileClass.getConstructor(EntityType.class, Level.class, double.class, double.class, double.class).newInstance(this.oldProjectileTypes.get(projectileClass), world, x, y, z);
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
            return null;
        }
    }
}
