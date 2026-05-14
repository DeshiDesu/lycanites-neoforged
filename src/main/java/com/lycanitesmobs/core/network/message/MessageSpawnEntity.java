package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.util.EntityFactory;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;


import java.util.UUID;
import java.util.function.Supplier;

public class MessageSpawnEntity {
    public String entityTypeName = "";
    public int entityId = 0;
    public UUID uuid;
    public float pitch;
    public float yaw;
    public double x;
    public double y;
    public double z;

    public MessageSpawnEntity() {
    }

    public MessageSpawnEntity(Entity serverEntity) {
        if (serverEntity != null) {
            if (serverEntity instanceof BaseProjectileEntity) {
                this.entityTypeName = ((BaseProjectileEntity) serverEntity).entityName;
            }
            this.entityId = serverEntity.getId();
            this.uuid = serverEntity.getUUID();
            this.pitch = serverEntity.xRotO;
            this.yaw = serverEntity.yRotO;
            this.x = serverEntity.position().x();
            this.y = serverEntity.position().y();
            this.z = serverEntity.position().z();
        }
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSpawnEntity message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() -> {
            if (!EntityFactory.getInstance().entityTypeNetworkMap.containsKey(message.entityTypeName)) {
                LMHelperClass.logWarning("", "Unable to find entity type from packet: " + message.entityTypeName);
                return;
            }
            EntityType entityType = EntityFactory.getInstance().entityTypeNetworkMap.get(message.entityTypeName);
            Entity entity = EntityFactory.getInstance().create(entityType, LycanitesMobs.PROXY.getWorld());
            if (entity == null) {
                LMHelperClass.logWarning("", "Unable to create client entity from packet: " + message.entityTypeName);
                return;
            }
            entity.setPos(message.x, message.y, message.z);
            entity.xRotO = message.pitch;
            entity.yRotO = message.yaw;
            entity.setId(message.entityId);
            entity.setUUID(message.uuid);

            // Projectiles:
            if (entity instanceof BaseProjectileEntity) {
                BaseProjectileEntity projectileEntity = (BaseProjectileEntity) entity;
                projectileEntity.entityName = message.entityTypeName;
                if (entity instanceof CustomProjectileEntity) {
                    ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(message.entityTypeName);
                    if (projectileInfo != null) {
                        ((CustomProjectileEntity) entity).setProjectileInfo(projectileInfo);
                    }
                }
            }

            LycanitesMobs.PROXY.addEntityToWorld(message.entityId, entity);
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSpawnEntity decode(FriendlyByteBuf packet) {
        MessageSpawnEntity message = new MessageSpawnEntity();
        message.entityTypeName = packet.readUtf();
        message.entityId = packet.readInt();
        message.uuid = packet.readUUID();
        message.pitch = packet.readFloat();
        message.yaw = packet.readFloat();
        message.x = packet.readDouble();
        message.y = packet.readDouble();
        message.z = packet.readDouble();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSpawnEntity message, FriendlyByteBuf packet) {
        packet.writeUtf(message.entityTypeName);
        packet.writeInt(message.entityId);
        packet.writeUUID(message.uuid);
        packet.writeFloat(message.pitch);
        packet.writeFloat(message.yaw);
        packet.writeDouble(message.x);
        packet.writeDouble(message.y);
        packet.writeDouble(message.z);
    }

}
