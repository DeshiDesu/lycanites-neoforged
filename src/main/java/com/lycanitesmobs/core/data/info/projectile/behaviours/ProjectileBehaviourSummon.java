package com.lycanitesmobs.core.data.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.network.packet.MessageScreenRequest;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class ProjectileBehaviourSummon extends ProjectileBehaviour {
    /**
     * The id of the mob to summon.
     **/
    public String summonMobId;

    /**
     * If true, the player selected minion is summoned instead of a direct entity from mobId.
     **/
    public boolean summonMinion = false;

    /**
     * The chance on summoning mobs.
     **/
    public double summonChance = 0.05;

    /**
     * How long in ticks the summoned creature lasts for.
     **/
    public int summonDuration = 60;

    /**
     * The minimum amount of mobs to summon.
     **/
    public int summonCountMin = 1;

    /**
     * The maximum amount of mobs to summon.
     **/
    public int summonCountMax = 1;

    /**
     * The size scale of summoned mobs.
     **/
    public double sizeScale = 1;

    @Override
    public void loadFromJSON(JsonObject json) {
        if (json.has("summonMobId"))
            this.summonMobId = json.get("summonMobId").getAsString();

        if (json.has("summonMinion"))
            this.summonMinion = json.get("summonMinion").getAsBoolean();

        if (json.has("summonChance"))
            this.summonChance = json.get("summonChance").getAsDouble();

        if (json.has("summonDuration"))
            this.summonDuration = json.get("summonDuration").getAsInt();

        if (json.has("summonCountMin"))
            this.summonCountMin = json.get("summonCountMin").getAsInt();

        if (json.has("summonCountMax"))
            this.summonCountMax = json.get("summonCountMax").getAsInt();

        if (json.has("sizeScale"))
            this.sizeScale = json.get("sizeScale").getAsDouble();
    }

    @Override
    public void onProjectileImpact(BaseProjectileEntity projectile, Level world, BlockPos pos) {
        if (projectile == null || projectile.getCommandSenderWorld().isClientSide) {
            return;
        }
        if (!CreatureManager.getInstance().config.isSummoningAllowed(world)) {
            return;
        }
        EntityType entityType = null;

        // Summon Minion:
        SummonSet summonSet = null;
        if (this.summonMinion) {
            if (!(projectile.getOwner() instanceof Player)) {
                return;
            }
            Player player = (Player) projectile.getOwner();
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
            if (extendedPlayer == null) {
                return;
            }
            summonSet = extendedPlayer.getSelectedSummonSet();
            if (summonSet == null || summonSet.getCreatureType() == null) {
                if (player instanceof ServerPlayer) {
                    MessageScreenRequest messageScreenRequest = new MessageScreenRequest(MessageScreenRequest.GuiRequest.SUMMONING);
                    LycanitesMobs.PACKET_MANAGER.sendToPlayer(messageScreenRequest, (ServerPlayer) player);
                }
                return;
            }
            entityType = summonSet.getCreatureType();
        }

        // Summon From ID:
        if (entityType == null && this.summonMobId != null) {
            CreatureInfo creatureInfo = CreatureManager.getInstance().getCreatureFromId(this.summonMobId);
            if (creatureInfo != null) {
                entityType = creatureInfo.getEntityType();
            } else {
                Object entityTypeObj = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(this.summonMobId));
                if (entityTypeObj instanceof EntityType) {
                    EntityType<?> fetchedType = (EntityType) entityTypeObj;
                    ResourceLocation entityKey = ForgeRegistries.ENTITY_TYPES.getKey(fetchedType);
                    if (entityKey != null && LycanitesMobs.MODID.equals(entityKey.getNamespace())) {
                        entityType = fetchedType;
                    } else {
                        LMHelperClass.logWarning("", "[ProjectileSummon] Entity type '" + this.summonMobId + "' is not from the lycanitesmobs mod namespace. Summon ignored.");
                    }
                }
            }
        }
        if (entityType == null) {
            return;
        }

        int summonCount = this.summonCountMin;
        if (this.summonCountMax > this.summonCountMin) {
            summonCount = this.summonCountMin + projectile.getCommandSenderWorld().random.nextInt(this.summonCountMax - this.summonCountMin);
        }

        for (int i = 0; i < summonCount; i++) {
            if (projectile.getCommandSenderWorld().random.nextDouble() <= this.summonChance) {
                try {
                    Entity entity = entityType.create(projectile.getCommandSenderWorld());
                    entity.moveTo(projectile.blockPosition().getX(), projectile.blockPosition().getY(), projectile.blockPosition().getZ(), projectile.yRotO, 0.0F);
                    if (entity instanceof BaseCreatureEntity) {
                        BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
                        entityCreature.setMinion(true);
                        entityCreature.setTemporary(this.summonDuration);
                        entityCreature.setSizeScale(this.sizeScale);

                        if (projectile.getOwner() instanceof Player && entityCreature instanceof TameableCreatureEntity) {
                            TameableCreatureEntity entityTameable = (TameableCreatureEntity) entityCreature;
                            entityTameable.setPlayerOwner((Player) projectile.getOwner());
                            entityTameable.setSitting(false);
                            entityTameable.setFollowing(true);
                            entityTameable.setPassive(false);
                            entityTameable.setAssist(true);
                            entityTameable.setAggressive(true);
                            if (summonSet != null) {
                                summonSet.applyBehaviour(entityTameable);
                                entityTameable.setSubspecies(summonSet.subspecies);
                                entityTameable.applyVariant(summonSet.variant);
                            }
                        }

                        float randomAngle = 45F + (45F * projectile.getCommandSenderWorld().random.nextFloat());
                        if (projectile.getCommandSenderWorld().random.nextBoolean()) {
                            randomAngle = -randomAngle;
                        }
                        BlockPos spawnPos = entityCreature.getFacingPosition(projectile, -1, randomAngle);
                        entity.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), projectile.yRotO, 0.0F);
                        entity.getCommandSenderWorld().addFreshEntity(entity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
