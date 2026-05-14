package com.lycanitesmobs.core.worldgen.dungeon;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.spawner.MobSpawn;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeferredBossSpawner {
    private static final int MAX_SPAWNS_PER_TICK = 16;

    private static class BossSpawnRequest {
        final ResourceKey<Level> dimension;
        final BlockPos pos;
        final MobSpawn mobSpawn;
        final int roomRadius;

        BossSpawnRequest(ResourceKey<Level> dimension, BlockPos pos, MobSpawn mobSpawn, int roomRadius) {
            this.dimension = dimension;
            this.pos = pos;
            this.mobSpawn = mobSpawn;
            this.roomRadius = roomRadius;
        }
    }

    private static final Map<Long, List<BossSpawnRequest>> PENDING = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, List<BossSpawnRequest>> READY = new ConcurrentHashMap<>();
    private static final Set<Long> SPAWNED = ConcurrentHashMap.newKeySet();

    public static void enqueue(ResourceKey<Level> dimension, BlockPos pos, MobSpawn mobSpawn, int roomRadius,
                               @Nullable ServerLevel level) {
        long posKey = pos.asLong();
        if (SPAWNED.contains(posKey)) {
            return;
        }

        long chunkKey = new ChunkPos(pos).toLong();
        BossSpawnRequest request = new BossSpawnRequest(dimension, pos, mobSpawn, roomRadius);
        addPending(chunkKey, request);

        if (level != null && level.dimension().equals(dimension)) {
            ChunkPos cp = new ChunkPos(pos);
            if (level.getChunkSource().getChunkNow(cp.x, cp.z) != null) {
                moveChunkRequestsToReady(chunkKey, level.dimension());
            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (PENDING.isEmpty()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        long chunkKey = event.getChunk().getPos().toLong();
        moveChunkRequestsToReady(chunkKey, level.dimension());
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel level) || READY.isEmpty()) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        List<BossSpawnRequest> readyRequests = READY.computeIfAbsent(dimension,
                k -> Collections.synchronizedList(new ArrayList<>()));
        if (readyRequests.isEmpty()) {
            return;
        }

        List<BossSpawnRequest> snapshot;
        synchronized (readyRequests) {
            snapshot = new ArrayList<>(readyRequests);
            readyRequests.clear();
        }

        int processed = 0;
        for (BossSpawnRequest request : snapshot) {
            if (processed >= MAX_SPAWNS_PER_TICK) {
                addReady(request);
                continue;
            }

            ChunkPos chunkPos = new ChunkPos(request.pos);
            if (level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
                addPending(chunkPos.toLong(), request);
                continue;
            }

            try {
                spawnBoss(level, request);
                processed++;
            } catch (Exception e) {
                LMHelperClass.logWarning("", "[DeferredBoss] Error spawning boss: " + e.getMessage());
            }
        }
    }

    private static void spawnBoss(ServerLevel level, BossSpawnRequest request) {
        long posKey = request.pos.asLong();
        if (!SPAWNED.add(posKey)) {
            return;
        }

        request.mobSpawn.resolveEntityType();

        LivingEntity entity = request.mobSpawn.createEntity(level);
        if (entity == null) {
            SPAWNED.remove(posKey);
            return;
        }

        entity.setPos(request.pos.getX() + 0.5, request.pos.getY(), request.pos.getZ() + 0.5);

        if (entity instanceof BaseCreatureEntity creature) {
            creature.setHome(request.pos.getX(), request.pos.getY(), request.pos.getZ(), request.roomRadius);
            creature.spawnedAsBoss = true;
        }

        request.mobSpawn.onSpawned(entity, null);
        level.addFreshEntity(entity);
        LMHelperClass.logDebug("Dungeon", "[DeferredBoss] Spawned boss at " + request.pos);
    }

    private static void moveChunkRequestsToReady(long chunkKey, ResourceKey<Level> dimension) {
        List<BossSpawnRequest> requests = PENDING.remove(chunkKey);
        if (requests == null) {
            return;
        }

        for (BossSpawnRequest request : requests) {
            if (!request.dimension.equals(dimension)) {
                addPending(chunkKey, request);
                continue;
            }
            addReady(request);
        }
    }

    private static void addPending(long chunkKey, BossSpawnRequest request) {
        PENDING.computeIfAbsent(chunkKey, k -> Collections.synchronizedList(new ArrayList<>())).add(request);
    }

    private static void addReady(BossSpawnRequest request) {
        READY.computeIfAbsent(request.dimension, k -> Collections.synchronizedList(new ArrayList<>())).add(request);
    }
}
