package com.lycanitesmobs.core.worldgen.mobevents;

import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.block.base.BlockFireBase;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.hellfire.EntityHellfireWall;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.event.mobevent.effects.StructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class AsmodeusStructureBuilder extends StructureBuilder {

    public AsmodeusStructureBuilder() {
        this.name = "asmodeus";
    }

    @Override
    public void build(Level world, Player player, BlockPos pos, int level, int ticks, int variant) {
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        int originX = pos.getX();
        int originY = pos.getY();
        int originZ = pos.getZ();

        originX += 20;
        int height = 40;
        if (originY < 5)
            originY = 5;
        if (world.getMaxBuildHeight() <= height)
            originY = 5;
        else if (originY + height >= world.getMaxBuildHeight())
            originY = Math.max(5, world.getMaxBuildHeight() - height - 1);

        // Effects:
        if (ticks == 1) {
            for (int i = 0; i < 5; i++) {
                int effectIndex = i;
                BaseProjectileEntity baseProjectileEntity = new EntityHellfireWall(ProjectileManager.getInstance().oldProjectileTypes.get(EntityHellfireWall.class), world, originX, originY + (10 * i), originZ);
                baseProjectileEntity.projectileLife = 20 * 20;
                this.spawnDeferred(world, new BlockPos(originX, originY + (10 * i), originZ), "structure_effect:" + this.name + ":" + effectIndex + ":" + pos.asLong(), baseProjectileEntity, () -> {
                    if (worldExt != null) {
                        worldExt.bossUpdate(baseProjectileEntity);
                        worldExt.overrideBossRange(baseProjectileEntity, CreatureManager.getInstance().getCreature("asmodeus").bossNearbyRange);
                    }
                });
            }
        }

        // Build Floor:
        if (ticks == 3 * 20) {
            this.buildArenaFloor(world, originX, originY, originZ);
        }

        // Build Walls:
        if (ticks == 5 * 20) {
            this.buildArenaWalls(world, originX, originY, originZ);
        }

        // Build Obstacles:
        if (ticks == 7 * 20) {
            this.buildObstacles(world, originX, originY, originZ);
        }

        // Explosions:
        if (ticks >= 10 * 20 && ticks % 10 == 0) {
            world.explode(null, originX - 20 + world.random.nextInt(40), originY + 25 + world.random.nextInt(10), originZ - 20 + world.random.nextInt(40), 2, Level.ExplosionInteraction.NONE);
        }

        // Spawn Boss:
        if (ticks == 20 * 20) {
            BaseCreatureEntity baseCreatureEntity = (BaseCreatureEntity) CreatureManager.getInstance().getCreature("asmodeus").createEntity(world);
            baseCreatureEntity.moveTo(originX, originY + 1, originZ, 0, 0);
            BlockPos arenaCenter = new BlockPos(originX, originY + 1, originZ);
            this.spawnDeferred(world, arenaCenter, "structure_boss:" + this.name + ":" + pos.asLong(), baseCreatureEntity, () -> {
                baseCreatureEntity.setArenaCenter(arenaCenter);
                if (worldExt != null) {
                    MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.name);
                    if (mobEventPlayerServer != null) {
                        mobEventPlayerServer.mobEvent.onSpawn(baseCreatureEntity, world, player, pos, level, ticks, variant);
                    }
                }
            });
        }
    }


    // ==================================================
    //                     Arena Floor
    // ==================================================
    public void buildArenaFloor(Level world, int originX, int originY, int originZ) {
        int radius = 80;
        int height = 30;
        int minX = originX - radius;
        int maxX = originX + radius;
        int minY = originY;
        int maxY = originY + height;
        int minZ = originZ - radius;
        int maxZ = originZ + radius;
        BlockState floor = ObjectManager.getBlock("aberrantstonetile").defaultBlockState();
        BlockState light = ObjectManager.getBlock("aberrantcrystal").defaultBlockState();
        BlockState trimming = ObjectManager.getBlock("aberrantstonechiseled").defaultBlockState();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int topY = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z)).getY();
                for (int y = minY; y <= maxY; y++) {
                    BlockPos buildPos = new BlockPos(x, y, z);
                    if (y == minY) {
                        if (x == minX || x == maxX || z == minZ || z == maxZ)
                            world.setBlock(buildPos, trimming, 2);
                        else if (x % 6 == 0 && z % 6 == 0)
                            world.setBlock(buildPos, light, 2);
                        else
                            world.setBlock(buildPos, floor, 2);
                    } else {
                        if (y > minY + 5 && y >= topY)
                            break;
                        world.setBlock(buildPos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }


    // ==================================================
    //                     Arena Walls
    // ==================================================
    public void buildArenaWalls(Level world, int originX, int originY, int originZ) {
        int radius = 80;
        int thickness = 4;
        int height = 20;
        int minX = originX - (radius + thickness);
        int maxX = originX + (radius + thickness);
        int minY = originY;
        int maxY = originY + height;
        int minZ = originZ - (radius + thickness);
        int maxZ = originZ + (radius + thickness);
        BlockState base = ObjectManager.getBlock("aberrantstonebrick").defaultBlockState();
        BlockState light = ObjectManager.getBlock("aberrantcrystal").defaultBlockState();
        BlockState trimming = ObjectManager.getBlock("aberrantstonechiseled").defaultBlockState();
        BlockState top = ObjectManager.getBlock("aberrantstonepolished").defaultBlockState();
        BlockState fireBase = Blocks.OBSIDIAN.defaultBlockState();
        BlockState fire = ObjectManager.getBlock("hellfire").defaultBlockState().setValue(BlockFireBase.STATIC, true);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x > minX + thickness && x < maxX - thickness && z > minZ + thickness && z < maxZ - thickness)
                    continue;
                for (int y = minY; y <= maxY; y++) {
                    BlockPos buildPos = new BlockPos(x, y, z);
                    if (y < maxY - 2) {
                        if (y - 1 % 8 == 0)
                            world.setBlock(buildPos, light, 2);
                        else if (y % 8 == 0)
                            world.setBlock(buildPos, trimming, 2);
                        else
                            world.setBlock(buildPos, base, 2);
                    } else if (y == maxY - 2)
                        world.setBlock(buildPos, top, 2);
                    else if (y == maxY - 1)
                        world.setBlock(buildPos, fireBase, 2);
                    else
                        world.setBlock(buildPos, fire, 2);
                }
            }
        }
    }


    // ==================================================
    //                   Arena Obstacles
    // ==================================================
    public void buildObstacles(Level world, int originX, int originY, int originZ) {
        int gap = 20;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x >= -1 && x <= 1 && z >= -1 && z <= 1)
                    continue;
                this.buildPillar(world, originX + (x * gap), originY, originZ + (z * gap));
            }
        }
    }

    /**
     * Builds an actual pillar.
     **/
    public void buildPillar(Level world, int originX, int originY, int originZ) {
        int radius = 2;
        int height = 30;
        int minX = originX - radius;
        int maxX = originX + radius;
        int minY = originY;
        int maxY = originY + height;
        int minZ = originZ - radius;
        int maxZ = originZ + radius;
        BlockState base = ObjectManager.getBlock("aberrantstonepillar").defaultBlockState();
        BlockState light = ObjectManager.getBlock("aberrantcrystal").defaultBlockState();
        BlockState trimming = ObjectManager.getBlock("aberrantstonechiseled").defaultBlockState();
        BlockState top = ObjectManager.getBlock("aberrantstonepolished").defaultBlockState();
        BlockState fireBase = Blocks.OBSIDIAN.defaultBlockState();
        BlockState fire = ObjectManager.getBlock("hellfire").defaultBlockState().setValue(BlockFireBase.STATIC, true);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos buildPos = new BlockPos(x, y, z);
                    if (y < maxY - 2) {
                        if (y % 7 == 0)
                            world.setBlock(buildPos, light, 2);
                        else if (y % 8 == 0)
                            world.setBlock(buildPos, trimming, 2);
                        else
                            world.setBlock(buildPos, base, 2);
                    } else if (y == maxY - 2)
                        world.setBlock(buildPos, top, 2);
                    else if (y == maxY - 1)
                        world.setBlock(buildPos, fireBase, 2);
                    else
                        world.setBlock(buildPos, fire, 2);
                }
            }
        }
    }
}
