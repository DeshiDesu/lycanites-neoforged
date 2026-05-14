package com.lycanitesmobs.core.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.entity.spawner.condition.WorldSpawnCondition;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StructureSpawnEvents {
    private static final boolean DEBUG_DUNGEON_PLACEMENT = false;

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!DEBUG_DUNGEON_PLACEMENT) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!serverPlayer.isCreative()) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos clickPos = event.getPos();

        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(level);
        if (extendedWorld == null) {
            return;
        }

        boolean spawned = false;
        for (DungeonSchematic schematic : DungeonManager.getInstance().getSchematics()) {
            if (schematic == null || schematic.name == null) {
                continue;
            }

            WorldSpawnCondition worldCondition = schematic.getWorldSpawnCondition();
            if (worldCondition == null) {
                continue;
            }

            int minY = worldCondition.getMinY();
            int maxY = worldCondition.getMaxY();
            if (maxY < minY) {
                int t = minY;
                minY = maxY;
                maxY = t;
            }

            int clampedY = Mth.clamp(clickPos.getY(), minY, maxY);
            BlockPos origin = new BlockPos(clickPos.getX(), clampedY, clickPos.getZ());

            if (!worldCondition.isMet(level, serverPlayer, origin)) {
                continue;
            }
            if (!schematic.canBuild(level, origin)) {
                continue;
            }

            debugSpawnDungeonInstance(level, extendedWorld, schematic, origin);

            player.sendSystemMessage(
                    Component.literal(
                            "Debug spawned \"" + schematic.name + "\" at " +
                                    origin.getX() + ", " + origin.getY() + ", " + origin.getZ()));
            spawned = true;
            break;
        }

        if (spawned) {
            event.setCanceled(true);
        }
    }

    public static BlockPos findNearestTheoreticalDungeon(ServerLevel level, DungeonSchematic schematic,
            BlockPos origin, int maxRegionRadius) {
        WorldSpawnCondition condition = schematic.getWorldSpawnCondition();
        if (condition == null) {
            return null;
        }

        long worldSeed = level.getSeed();
        int spacing = Math.max(1, condition.getSpacing());
        long salt = condition.getSalt();

        ChunkPos originChunk = new ChunkPos(origin);
        int originRegionX = originChunk.x < 0 ? (originChunk.x - spacing + 1) / spacing : originChunk.x / spacing;
        int originRegionZ = originChunk.z < 0 ? (originChunk.z - spacing + 1) / spacing : originChunk.z / spacing;

        BlockPos bestPos = null;
        double bestDistSq = Double.MAX_VALUE;

        for (int radius = 0; radius <= maxRegionRadius; radius++) {
            boolean foundInThisRadius = false;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && dx != -radius && dx != radius && dz != -radius && dz != radius) {
                        continue;
                    }

                    int regionX = originRegionX + dx;
                    int regionZ = originRegionZ + dz;
                    long structureSeed = computeStructureSeed(worldSeed, salt, regionX, regionZ);
                    RandomSource random = RandomSource.create(structureSeed);

                    int originChunkX = regionX * spacing + random.nextInt(spacing);
                    int originChunkZ = regionZ * spacing + random.nextInt(spacing);

                    int centerX = originChunkX * 16 + 8;
                    int centerZ = originChunkZ * 16 + 8;

                    int minY = condition.getMinY();
                    int maxY = condition.getMaxY();
                    if (maxY < minY) {
                        int t = minY;
                        minY = maxY;
                        maxY = t;
                    }
                    int yRange = Math.max(1, maxY - minY + 1);
                    RandomSource yRandom = RandomSource.create(
                            computeStructureSeed(worldSeed, salt, originChunkX, originChunkZ));
                    int y = minY + yRandom.nextInt(yRange);

                    BlockPos candidate = new BlockPos(centerX, y, centerZ);
                    if (!condition.isMet(level, null, candidate)) {
                        continue;
                    }
                    if (!schematic.canBuild(level, candidate)) {
                        continue;
                    }

                    double distSq = candidate.distSqr(origin);
                    if (distSq < bestDistSq) {
                        bestDistSq = distSq;
                        bestPos = candidate;
                        foundInThisRadius = true;
                    }
                }
            }
            if (foundInThisRadius && bestPos != null) {
                break;
            }
        }
        return bestPos;
    }

    private static long computeStructureSeed(long worldSeed, long salt, int chunkX, int chunkZ) {
        return worldSeed + salt + chunkX * 2345803L + chunkZ * 9236449L + (long) chunkX * chunkZ * 223L;
    }

    private static void debugSpawnDungeonInstance(ServerLevel level, ExtendedWorld extendedWorld,
            DungeonSchematic schematic, BlockPos origin) {
        DungeonInstance instance = new DungeonInstance();
        instance.schematic = schematic;
        instance.setOrigin(origin);
        instance.init(level);

        long xorSeed = level.getSeed() ^ origin.asLong();
        RandomSource idRandom = RandomSource.create(xorSeed);
        UUID id = new UUID(idRandom.nextLong(), idRandom.nextLong());
        instance.uuid = id;

        extendedWorld.addDungeonInstance(instance, id);
        extendedWorld.buildDungeonInLoadedChunks(instance);

        LMHelperClass.logDebug("Dungeon",
                "Debug dungeon " + schematic.name + " spawned at " + origin);
    }
}
