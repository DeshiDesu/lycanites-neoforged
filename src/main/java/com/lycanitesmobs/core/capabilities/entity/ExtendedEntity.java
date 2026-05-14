package com.lycanitesmobs.core.capabilities.entity;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.data.config.ConfigAdmin;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.network.message.MessageEntityPerched;
import com.lycanitesmobs.core.network.message.MessageEntityPickedUp;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedEntity {
    public static Map<Entity, ExtendedEntity> clientExtendedEntities = new HashMap<>();
    public static List<? extends String> FORCE_REMOVE_ENTITY_IDS;
    public static int FORCE_REMOVE_ENTITY_TICKS = 40;

    // Entity Instance:
    public LivingEntity entity;

    // Projectiles:
    protected Map<String, Integer> projectileCooldownsPrimary = new HashMap<>();
    protected Map<String, Integer> projectileCooldownsSecondary = new HashMap<>();

    // Safe Position:
    /**
     * The last coordinates the entity was at where it wasn't inside an opaque block. (Helps prevent suffocation).
     **/
    Vector3d lastSafePos;
    private boolean playerAllowFlyingSnapshot;
    private boolean playerIsFlyingSnapshot;

    // Last Attacked:
    public LivingEntity lastAttackedEntity;
    public int lastAttackedTime = 0;

    // Picked Up:
    public Entity pickedUpByEntity;

    // Percher
    public Entity perchedByEntity;
    public boolean perchedEntityNoclip = false;

    // Force Remove:
    boolean forceRemoveChecked = false;
    boolean forceRemove = false;
    int forceRemoveTicks = FORCE_REMOVE_ENTITY_TICKS;

    // ==================================================
    //                   Get for Entity
    // ==================================================
    public static ExtendedEntity getForEntity(LivingEntity entity) {
        if (entity == null) {
            //LycanitesMobs.logWarning("", "Tried to access an ExtendedEntity from a null Entity.");
            return null;
        }

        // Client Side:
        if (entity.getCommandSenderWorld().isClientSide) {
            if (clientExtendedEntities.containsKey(entity)) {
                ExtendedEntity extendedEntity = clientExtendedEntities.get(entity);
                extendedEntity.setEntity(entity);
                return extendedEntity;
            }
            ExtendedEntity extendedEntity = new ExtendedEntity();
            extendedEntity.setEntity(entity);
            clientExtendedEntities.put(entity, extendedEntity);
            return extendedEntity;
        }

        // Server Side:
        ExtendedEntity iExtendedEntity = entity.getCapability(CapabilityProviderEntity.EXTENDED_ENTITY).orElse(new ExtendedEntity());
        if (iExtendedEntity.getEntity() != entity)
            iExtendedEntity.setEntity(entity);
        return iExtendedEntity;
    }


    // ==================================================
    //                    Constructor
    // ==================================================
    public ExtendedEntity() {

    }


    // ==================================================
    //                      Entity
    // ==================================================

    /**
     * Initially sets the entity.
     **/
    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {

        return this.entity;
    }

    public void setLastAttackedEntity(LivingEntity target) {
        this.lastAttackedEntity = target;
        this.lastAttackedTime = this.entity.tickCount;
    }


    // ==================================================
    //                      Update
    // ==================================================
    public void onUpdate() {
        if (this.entity == null)
            return;

        // Projectiles:
        for (String cooldownName : this.projectileCooldownsPrimary.keySet()) {
            int cooldownValue = this.projectileCooldownsPrimary.get(cooldownName);
            if (cooldownValue > 0) {
                this.projectileCooldownsPrimary.put(cooldownName, cooldownValue - 1);
            }
        }
        for (String cooldownName : this.projectileCooldownsSecondary.keySet()) {
            int cooldownValue = this.projectileCooldownsSecondary.get(cooldownName);
            if (cooldownValue > 0) {
                this.projectileCooldownsSecondary.put(cooldownName, cooldownValue - 1);
            }
        }

        // Force Remove Entity:
        ExtendedEntity.FORCE_REMOVE_ENTITY_IDS = ConfigAdmin.INSTANCE.forceRemoveEntityIds.get();
        ExtendedEntity.FORCE_REMOVE_ENTITY_TICKS = 40;
        if (!this.entity.getCommandSenderWorld().isClientSide && FORCE_REMOVE_ENTITY_IDS != null && FORCE_REMOVE_ENTITY_IDS.size() > 0 && !this.forceRemoveChecked) {
            LMHelperClass.logDebug("ForceRemoveEntity", "Forced entity removal, checking: " + this.entity.getName());
            for (String forceRemoveID : FORCE_REMOVE_ENTITY_IDS) {
                if (forceRemoveID.equalsIgnoreCase(LMHelperClass.convertToResourceLocation(this.entity.getType(), this.entity.level().registryAccess()).toString())) {
                    this.forceRemove = true;
                    break;
                }
            }
            this.forceRemoveChecked = true;
        }
        if (this.forceRemove && this.forceRemoveTicks-- <= 0)
            this.entity.remove(Entity.RemovalReason.DISCARDED);

        // Safe Position:
        if (this.lastSafePos == null) {
            this.lastSafePos = new Vector3d(this.entity.position().x(), this.entity.position().y(), this.entity.position().z());
        }
        if (!this.entity.getCommandSenderWorld().getBlockState(this.entity.blockPosition()).isSolid()) {
            this.lastSafePos = new Vector3d(Math.floor(this.entity.position().x()) + 0.5D, this.entity.blockPosition().getY(), Math.floor(this.entity.position().z()) + 0.5D);
        }

        // Picked Up By Entity:
        try {
            this.updatePickedUpByEntity();
        } catch (Exception e) {
        }

        // Perched By Entity:
        try {
            this.updatedPerchedByEntity();
        } catch (Exception e) {
        }
    }


    // ==================================================
    //                       Death
    // ==================================================
    public void onDeath() {
        this.setPickedUpByEntity(null);
    }


    // ==================================================
    //                 Picked Up By Entity
    // ==================================================
    public void updatePickedUpByEntity() {
        if (this.pickedUpByEntity == null)
            return;

        // Check:
        if (!this.entity.getCommandSenderWorld().isClientSide) {
            if (!this.pickedUpByEntity.isAlive()) {
                this.setPickedUpByEntity(null);
                return;
            }
            if (this.pickedUpByEntity instanceof LivingEntity) {
                if (((LivingEntity) this.pickedUpByEntity).getHealth() <= 0) {
                    this.setPickedUpByEntity(null);
                    return;
                }
            }
            MobEffect weight = ObjectManager.getEffect("weight");
            if (weight != null) {
                if (this.entity.hasEffect(weight)) {
                    this.setPickedUpByEntity(null);
                    return;
                }
            }
            if (this.entity.distanceTo(this.pickedUpByEntity) > 32D) {
                this.setPickedUpByEntity(null);
                return;
            }
        }

        // Movement:
        if (this.pickedUpByEntity != null) {
            double[] pickupOffset = this.getPickedUpOffset();
            this.entity.setPos(this.pickedUpByEntity.position().x() + pickupOffset[0], this.pickedUpByEntity.position().y() + pickupOffset[1], this.pickedUpByEntity.position().z() + pickupOffset[2]);
            this.entity.setDeltaMovement(this.pickedUpByEntity.getDeltaMovement());
            this.entity.fallDistance = 0;
            if (!this.entity.getCommandSenderWorld().isClientSide && this.entity instanceof Player) {
                Player player = (Player) this.entity;
                player.getAbilities().mayfly = true;
                this.entity.noPhysics = true;
            }
        }
    }

    public void setPickedUpByEntity(Entity pickedUpByEntity) {
        if (this.pickedUpByEntity == pickedUpByEntity || this.entity == null) {
            return;
        }

        this.pickedUpByEntity = pickedUpByEntity;

        // Server Side:
        if (!this.entity.getCommandSenderWorld().isClientSide) {

            // Player Flying:
            if (this.entity instanceof Player) {
                if (pickedUpByEntity != null) {
                    this.playerAllowFlyingSnapshot = ((Player) this.entity).getAbilities().mayfly;
                    this.playerIsFlyingSnapshot = ((Player) this.entity).getAbilities().flying;
                } else {
                    ((Player) this.entity).getAbilities().mayfly = this.playerAllowFlyingSnapshot;
                    ((Player) this.entity).getAbilities().flying = this.playerIsFlyingSnapshot;
                    this.entity.noPhysics = false;
                }
            }

            // Teleport To Initial Pickup Position:
            if (this.pickedUpByEntity != null && !(this.entity instanceof Player)) {
                double[] pickupOffset = this.getPickedUpOffset();
                this.entity.teleportToWithTicket(this.pickedUpByEntity.position().x() + pickupOffset[0], this.pickedUpByEntity.position().y() + pickupOffset[1], this.pickedUpByEntity.position().z() + pickupOffset[2]);
            }

            MessageEntityPickedUp message = new MessageEntityPickedUp(this.entity, pickedUpByEntity);
            LycanitesMobs.PACKET_MANAGER.sendToWorld(message, this.entity.getCommandSenderWorld());
        }

        // Safe Drop Position:
        if (pickedUpByEntity == null) {
            if (this.lastSafePos != null) {
                this.entity.setPos(this.lastSafePos.x(), this.lastSafePos.y(), this.lastSafePos.z());
            }
            this.entity.setDeltaMovement(0, 0, 0);
            this.entity.fallDistance = 0;
        }
    }

    public double[] getPickedUpOffset() {
        double[] pickupOffset = new double[]{0, 0, 0};
        if (this.pickedUpByEntity instanceof BaseCreatureEntity) {
            pickupOffset = ((BaseCreatureEntity) this.pickedUpByEntity).getPickupOffset(this.entity);
        }
        if (CreatureManager.getInstance().config.disablePickupOffsets && this.entity instanceof Player) {
            return new double[]{0, 0, 0};
        }
        return pickupOffset;
    }

    public boolean isPickedUp() {
        return this.pickedUpByEntity != null;
    }

    public boolean isFeared() {
        if (this.entity == null) return false;
        MobEffect fear = ObjectManager.getEffect("fear");
        return fear != null && this.entity.hasEffect(fear);
    }


    // ==================================================
    //                     Perched
    // ==================================================

    /**
     * Sets the entity that is perching on this entity.
     *
     * @param perchedByEntity The entity to perch on this entity or null to clear.
     */
    public void setPerchedByEntity(Entity perchedByEntity) {
        if (this.perchedByEntity != null) {
            this.perchedByEntity.noPhysics = this.perchedEntityNoclip;
            if (this.perchedByEntity instanceof BaseCreatureEntity) {
                ((BaseCreatureEntity) this.perchedByEntity).setPerchTarget(null);
            }
        }

        this.perchedByEntity = perchedByEntity;
        if (perchedByEntity != null) {
            this.perchedEntityNoclip = perchedByEntity.noPhysics;
            perchedByEntity.noPhysics = true;
            if (perchedByEntity instanceof BaseCreatureEntity) {
                ((BaseCreatureEntity) perchedByEntity).setPerchTarget(this.entity);
            }
        }

        if (!this.entity.getCommandSenderWorld().isClientSide) {
            MessageEntityPerched message = new MessageEntityPerched(this.entity, this.perchedByEntity);
            LycanitesMobs.PACKET_MANAGER.sendToWorld(message, this.entity.getCommandSenderWorld());
        }
    }

    /**
     * Returns the entity currently perching on this entity if any.
     *
     * @return The entity perching on this entity or null.
     */
    public Entity getPerchedByEntity() {
        return this.perchedByEntity;
    }

    /**
     * Returns the xyz position that an entity should perch on this entity at.
     *
     * @return The perch position.
     */
    public Vector3d getPerchPosition() {
        double entityWidth = this.entity.getDimensions(this.entity.getPose()).width;
        double entityHeight = this.entity.getDimensions(this.entity.getPose()).height;

        double angle = Math.toRadians(this.entity.yRot) + 90;
        double xPerchPos = this.entity.position().x();
        double zPerchPos = this.entity.position().z();
        double distance = entityWidth * 0.7D;
        if (distance != 0) {
            xPerchPos += distance * -Math.sin(angle);
            zPerchPos += distance * Math.cos(angle);
        }

        return new Vector3d(
                xPerchPos,
                this.entity.position().y() + entityHeight * 0.78D,
                zPerchPos
        );
    }

    /**
     * Updates the position of an entity that is perching on this entity.
     */
    public void updatedPerchedByEntity() {
        Entity perchedByEntity = this.getPerchedByEntity();
        if (perchedByEntity != null) {
            Vector3d perchPosition = this.getPerchPosition();
            perchedByEntity.setPos(perchPosition.x(), perchPosition.y(), perchPosition.z());
            perchedByEntity.setDeltaMovement(this.entity.getDeltaMovement());
            perchedByEntity.yRot = this.entity.yRot;
            perchedByEntity.noPhysics = true;
        }
    }


    // ==================================================
    //                     Projectiles
    // ==================================================

    /**
     * Returns the projectile firing cooldown of the provided type and projectile name.
     *
     * @param type           The type of cooldown, should be 1 for primary and 2 for secondary.
     * @param projectileName The name of the projectile to get the firing cooldown of.
     * @return The current firing cooldown.
     */
    public int getProjectileCooldown(int type, String projectileName) {
        if (type == 1) {
            if (!this.projectileCooldownsPrimary.containsKey(projectileName)) {
                return 0;
            }
            return this.projectileCooldownsPrimary.get(projectileName);
        }

        if (!this.projectileCooldownsSecondary.containsKey(projectileName)) {
            return 0;
        }
        return this.projectileCooldownsSecondary.get(projectileName);
    }

    /**
     * Set the projectile firing cooldown of the provided type and projectile name.
     *
     * @param type           The type of cooldown, should be 1 for primary and 2 for secondary.
     * @param projectileName The name of the projectile to set the firing cooldown of.
     * @param cooldown       The cooldown (in ticks) to set.
     */
    public void setProjectileCooldown(int type, String projectileName, int cooldown) {
        if (type == 1) {
            this.projectileCooldownsPrimary.put(projectileName, cooldown);
        } else if (type == 2) {
            this.projectileCooldownsSecondary.put(projectileName, cooldown);
        }
    }


    // ==================================================
    //                       Remove
    // ==================================================
    public void onEntityRemoved() {
        clientExtendedEntities.remove(this.entity);
    }


    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========

    /**
     * Reads a list of Creature Knowledge from a player's NBTTag.
     **/
    public void readNBT(CompoundTag nbtTagCompound) {
        nbtTagCompound.get("extendedEntity");
    }

    // ========== Write ==========

    /**
     * Writes a list of Creature Knowledge to a player's NBTTag.
     **/
    public void writeNBT(CompoundTag nbtTagCompound) {
		/*ExtendedEntity extendedEntity = (ExtendedEntity)instance;
		CompoundNBT extTagCompound = new CompoundNBT();
		extendedEntity.writeNBT(extTagCompound);
		return extTagCompound;*/
    }
}