package com.lycanitesmobs.core.worldgen.mobevents;

import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.block.base.BlockFireBase;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.misc.EntityShadowfireBarrier;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.event.mobevent.effects.StructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class AmalgalichStructureBuilder extends StructureBuilder {

    public AmalgalichStructureBuilder() {
        this.name = "amalgalich";
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
                BaseProjectileEntity baseProjectileEntity = new EntityShadowfireBarrier(ProjectileManager.getInstance().oldProjectileTypes.get(EntityShadowfireBarrier.class), world, originX, originY + (10 * i), originZ);
                baseProjectileEntity.projectileLife = 20 * 20;
                this.spawnDeferred(world, new BlockPos(originX, originY + (10 * i), originZ), "structure_effect:" + this.name + ":" + effectIndex + ":" + pos.asLong(), baseProjectileEntity, () -> {
                    if (worldExt != null) {
                        worldExt.bossUpdate(baseProjectileEntity);
                    }
                });
            }
        }

        // Build Floor:
        if (ticks == 1 * 20) {
            this.buildArenaFloor(world, originX, originY, originZ);
        }

        // Explosions:
        if (ticks >= 3 * 20 && ticks % 10 == 0) {
            world.explode(null, originX - 20 + world.random.nextInt(40), originY + 25 + world.random.nextInt(10), originZ - 20 + world.random.nextInt(40), 2, Level.ExplosionInteraction.NONE);
        }

        // Build Obstacles:
        if (ticks == 3 * 20) {
            this.buildObstacles(world, originX, originY, originZ);
        }

        // Spawn Boss:
        if (ticks == 25 * 20) {
            BaseCreatureEntity baseCreatureEntity = (BaseCreatureEntity) CreatureManager.getInstance().getCreature("amalgalich").createEntity(world);
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
        int radius = 60;
        int height = 40;
        Block primaryBlock = ObjectManager.getBlock("shadowstonetile");
        Block secondaryBlock = ObjectManager.getBlock("shadowcrystal");
        double secondaryChance = 0.05D;

        int stripNumber = 1;
        for (int x = originX - radius; x < originX + radius; x++) {
            float stripNormal = (float) stripNumber / (float) radius;
            if (stripNumber > radius)
                stripNormal = (float) (radius - (stripNumber - radius)) / (float) radius;
            int stripRadius = Math.round(radius * (float) Math.sin(Math.toRadians(90 * stripNormal)));

            for (int z = originZ - stripRadius; z < originZ + stripRadius; z++) {
                int y = originY;
                // Build Floor:
                Block buildBlock = primaryBlock;
                if (world.random.nextDouble() <= secondaryChance)
                    buildBlock = secondaryBlock;
                world.setBlock(new BlockPos(x, y, z), buildBlock.defaultBlockState(), 2);
                world.setBlock(new BlockPos(x, y - 1, z), buildBlock.defaultBlockState(), 2);
                world.setBlock(new BlockPos(x, y - 2, z), buildBlock.defaultBlockState(), 2);
                y++;
                while (y <= originY + height && y < world.getMaxBuildHeight()) {
                    world.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                    y++;
                }
            }

            stripNumber++;
        }
    }


    // ==================================================
    //                   Arena Obstacles
    // ==================================================
    public void buildObstacles(Level world, int originX, int originY, int originZ) {
        double angle = 0;
        int radius = 50;
        List<int[]> decorationCoords = new ArrayList<>();

        while (angle < 360) {
            angle += 5 + (5 * world.random.nextDouble());
            double angleRadians = Math.toRadians(angle);
            double x = radius * Math.cos(angleRadians) - Math.sin(angleRadians);
            double z = radius * Math.sin(angleRadians) + Math.cos(angleRadians);
            decorationCoords.add(this.buildPillar(world, originX + (int) Math.ceil(x), originY, originZ + (int) Math.ceil(z)));
        }

        for (int[] decorationCoord : decorationCoords)
            this.buildDecoration(world, decorationCoord[0], decorationCoord[1], decorationCoord[2]);
    }

    /**
     * Builds an actual pillar.
     **/
    public int[] buildPillar(Level world, int originX, int originY, int originZ) {
        int radiusMax = 5;
        int height = 30;
        Block primaryBlock = ObjectManager.getBlock("shadowstonebrick");
        Block secondaryBlock = ObjectManager.getBlock("shadowstone");
        Block tetriaryBlock = ObjectManager.getBlock("shadowstonechiseled");
        Block capBlock = Blocks.OBSIDIAN;
        Block hazardBlock = ObjectManager.getBlock("shadowfire");
        double secondaryChance = 0.4D;
        double tetriaryChance = 0.05D;
        int[] decorationCoord = new int[]{originX, originY, originZ};

        int radius = radiusMax;
        int radiusHeight = height - 10;
        for (int y = originY; y <= originY + height; y++) {
            boolean lowerRadius = --radiusHeight <= 0;
            int stripNumber = 1;
            for (int x = originX - radius; x <= originX + radius; x++) {
                float stripNormal = (float) stripNumber / (float) radius;
                if (stripNumber > radius)
                    stripNormal = (float) (radius - (stripNumber - radius)) / (float) radius;
                int stripRadius = Math.round(radius * (float) Math.sin(Math.toRadians(90 * stripNormal)));

                for (int z = originZ - stripRadius; z <= originZ + stripRadius; z++) {
                    if (lowerRadius || y == originY + height)
                        world.setBlock(new BlockPos(x, y, z), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
                    else if (radiusHeight - 1 <= 0 || y == originY + height - 1)
                        world.setBlock(new BlockPos(x, y, z), capBlock.defaultBlockState(), 2);
                    else if (world.random.nextDouble() > secondaryChance)
                        world.setBlock(new BlockPos(x, y, z), primaryBlock.defaultBlockState(), 2);
                    else if (world.random.nextDouble() > tetriaryChance)
                        world.setBlock(new BlockPos(x, y, z), secondaryBlock.defaultBlockState(), 2);
                    else
                        world.setBlock(new BlockPos(x, y, z), tetriaryBlock.defaultBlockState(), 2);
                }

                stripNumber++;
            }
            if (lowerRadius) {
                radiusHeight = radiusMax;
                radius--;
            }
        }

        return decorationCoord;
    }

    /**
     * Adds decoration to a pillar.
     **/
    public void buildDecoration(Level world, int originX, int originY, int originZ) {
        Block primaryBlock = Blocks.OBSIDIAN;
        Block hazardBlock = ObjectManager.getBlock("shadowfire");
        world.setBlock(new BlockPos(originX, originY + 1, originZ), primaryBlock.defaultBlockState(), 2);
        world.setBlock(new BlockPos(originX, originY + 2, originZ), primaryBlock.defaultBlockState(), 2);
        world.setBlock(new BlockPos(originX, originY + 3, originZ), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
        world.setBlock(new BlockPos(originX + 1, originY + 1, originZ), primaryBlock.defaultBlockState(), 2);
        world.setBlock(new BlockPos(originX + 1, originY + 2, originZ), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
        world.setBlock(new BlockPos(originX - 1, originY + 1, originZ), primaryBlock.defaultBlockState(), 2);
        world.setBlock(new BlockPos(originX - 1, originY + 2, originZ), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
        world.setBlock(new BlockPos(originX, originY + 1, originZ + 1), primaryBlock.defaultBlockState(), 2);
        world.setBlock(new BlockPos(originX, originY + 2, originZ + 1), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
        world.setBlock(new BlockPos(originX, originY + 1, originZ - 1), primaryBlock.defaultBlockState(), 2);
        world.setBlock(new BlockPos(originX, originY + 2, originZ - 1), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
    }
}
