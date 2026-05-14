package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeferredLevelActionManager {
    private static final int MAX_ACTIONS_PER_TICK = 64;
    private static final int MAX_RETRIES = 200;

    private static class QueuedAction {
        final ResourceKey<Level> dimension;
        @Nullable
        final ChunkPos chunkPos;
        @Nullable
        final String key;
        final Consumer<ServerLevel> action;
        int retries = 0;

        QueuedAction(ResourceKey<Level> dimension, @Nullable ChunkPos chunkPos, @Nullable String key, Consumer<ServerLevel> action) {
            this.dimension = dimension;
            this.chunkPos = chunkPos;
            this.key = key;
            this.action = action;
        }
    }

    private static final ConcurrentHashMap<ResourceKey<Level>, Deque<QueuedAction>> READY = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ResourceKey<Level>, Set<String>> PENDING_KEYS = new ConcurrentHashMap<>();

    public static boolean enqueue(ServerLevel level, BlockPos pos, @Nullable String key, Consumer<ServerLevel> action) {
        return enqueue(level, new ChunkPos(pos), key, action);
    }

    public static boolean enqueue(ServerLevel level, @Nullable ChunkPos chunkPos, @Nullable String key, Consumer<ServerLevel> action) {
        ResourceKey<Level> dimension = level.dimension();
        if (key != null) {
            Set<String> pending = PENDING_KEYS.computeIfAbsent(dimension, dim -> ConcurrentHashMap.newKeySet());
            if (!pending.add(key)) {
                return false;
            }
        }

        READY.computeIfAbsent(dimension, dim -> new ConcurrentLinkedDeque<>())
                .offer(new QueuedAction(dimension, chunkPos, key, action));
        return true;
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }

        Deque<QueuedAction> queue = READY.get(level.dimension());
        if (queue == null || queue.isEmpty()) {
            return;
        }

        int inspected = 0;
        while (inspected++ < MAX_ACTIONS_PER_TICK) {
            QueuedAction queuedAction = queue.poll();
            if (queuedAction == null) {
                return;
            }

            if (queuedAction.chunkPos != null && level.getChunkSource().getChunkNow(queuedAction.chunkPos.x, queuedAction.chunkPos.z) == null) {
                if (++queuedAction.retries > MAX_RETRIES) {
                    releaseKey(queuedAction.dimension, queuedAction.key);
                    LMHelperClass.logWarning("DeferredAction", "Discarding deferred action after too many retries in " + queuedAction.dimension.location());
                    continue;
                }
                queue.offer(queuedAction);
                continue;
            }

            try {
                queuedAction.action.accept(level);
            } catch (Exception e) {
                LMHelperClass.logWarning("DeferredAction", "Error while running deferred action: " + e.getMessage());
            } finally {
                releaseKey(queuedAction.dimension, queuedAction.key);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        READY.remove(dimension);
        PENDING_KEYS.remove(dimension);
    }

    private static void releaseKey(ResourceKey<Level> dimension, @Nullable String key) {
        if (key == null) {
            return;
        }

        Set<String> pending = PENDING_KEYS.get(dimension);
        if (pending == null) {
            return;
        }
        pending.remove(key);
        if (pending.isEmpty()) {
            PENDING_KEYS.remove(dimension, pending);
        }
    }
}
