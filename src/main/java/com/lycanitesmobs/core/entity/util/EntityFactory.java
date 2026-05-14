package com.lycanitesmobs.core.entity.util;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class EntityFactory implements EntityType.EntityFactory<Entity> {
    public static EntityFactory INSTANCE;
    public Map<EntityType, Constructor<? extends Entity>> entityTypeConstructorMap = new HashMap<>();
    public Map<Constructor<? extends Entity>, EntityType> entityConstructorTypeMap = new HashMap<>();
    public Map<String, EntityType> entityTypeNetworkMap = new HashMap<>();
    public BiFunction<PlayMessages.SpawnEntity, Level, Entity> createOnClientFunction = this::createOnClient;

    /**
     * Returns the main Entity Factory instance or creates it and returns it.
     **/
    public static EntityFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EntityFactory();
        }
        return INSTANCE;
    }

    /**
     * Adds a new Entity Type and Entity Class mapping for this Factory to create.
     *
     * @param entityType  The Entity Type to create from.
     * @param entityClass The Entity Class to instantiate for the type.
     */
    public void addEntityType(EntityType entityType, Constructor<? extends Entity> entityClass, String networkName) {
        LMHelperClass.logDebug("Entity", "Adding entity: " + entityClass + " Type: " + entityType.getDescription() + " Classification: " + entityType.getCategory());

        this.entityTypeConstructorMap.put(entityType, entityClass);
        this.entityConstructorTypeMap.put(entityClass, entityType);
        this.entityTypeNetworkMap.put(networkName, entityType);
    }

    /**
     * Creates an entity from an entity type in the provided world.
     *
     * @param entityType The entity type to create an entity from.
     * @param world      The world to create the entity in.
     * @return The created entity or null if no entity could be created.
     */
    @Override
    public Entity create(EntityType entityType, Level world) {
        try {
            if (!this.entityTypeConstructorMap.containsKey(entityType)) {
                LMHelperClass.logWarning("", "Unable to find constructor for Entity Type: " + entityType);
                for (EntityType<?> type : this.entityTypeConstructorMap.keySet()) {
                    LMHelperClass.logWarning("", "Values: " + type);
                }
                return null;
            }
            LMHelperClass.logDebug("Entity", "Spawning entity: " + this.entityTypeConstructorMap.get(entityType) + " - " + entityType.getCategory());
            Constructor<? extends Entity> constructor = this.entityTypeConstructorMap.get(entityType);
            return constructor.newInstance(entityType, world);
        } catch (Exception e) {
            LycanitesMobs.LOGGER.info("[LycanitesMobs]: Error in EntityFactory.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Spawns an entity on the client side from a server packet.
     *
     * @param spawnPacket The entity spawn packet.
     * @param world       The world to spawn in.
     */
    public Entity createOnClient(PlayMessages.SpawnEntity spawnPacket, Level world) {
        LMHelperClass.logDebug("", "Client factory called!");
        return null;
    }
}
