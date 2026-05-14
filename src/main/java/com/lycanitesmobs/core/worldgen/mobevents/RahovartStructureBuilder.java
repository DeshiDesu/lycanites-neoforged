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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class RahovartStructureBuilder extends StructureBuilder {

    public RahovartStructureBuilder() {
        this.name = "rahovart";
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
                    }
                });
            }
        }

        // Build Floor:
        if (ticks == 3 * 20) {
            this.buildArenaFloor(world, originX, originY, originZ);
        }

        // Build Obstacles:
        if (ticks == 5 * 20) {
            this.buildObstacles(world, originX, originY, originZ);
        }

        // Explosions:
        if (ticks >= 10 * 20 && ticks % 10 == 0) {
            world.explode(null, originX - 20 + world.random.nextInt(40), originY + 25 + world.random.nextInt(10), originZ - 20 + world.random.nextInt(40), 2, Level.ExplosionInteraction.NONE);
        }

        // Spawn Boss:
        if (ticks == 20 * 20) {
            BaseCreatureEntity baseCreatureEntity = (BaseCreatureEntity) CreatureManager.getInstance().getCreature("rahovart").createEntity(world);
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
        double rubbleChance = 0.01D;
        int radius = 60;
        int height = 40;
        Block primaryBlock = ObjectManager.getBlock("demonstonetile");
        Block secondaryBlock = ObjectManager.getBlock("demoncrystal");
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
        int height = 20 + Math.round(20 * world.random.nextFloat());
        Block primaryBlock = ObjectManager.getBlock("demonstonebrick");
        Block secondaryBlock = ObjectManager.getBlock("demonstone");
        Block tetriaryBlock = ObjectManager.getBlock("demonstonechiseled");
        Block pillarBlock = ObjectManager.getBlock("demonstonepillar");
        double secondaryChance = 0.4D;
        double tetriaryChance = 0.05D;
        int[] decorationCoord = new int[]{originX, originY, originZ};

        int radius = radiusMax;
        int radiusHeight = radiusMax;
        for (int y = originY; y <= originY + height; y++) {
            if (y <= originY + (radiusMax * radiusMax)) {
                int stripNumber = 1;
                for (int x = originX - radius; x <= originX + radius; x++) {
                    float stripNormal = (float) stripNumber / (float) radius;
                    if (stripNumber > radius)
                        stripNormal = (float) (radius - (stripNumber - radius)) / (float) radius;
                    int stripRadius = Math.round(radius * (float) Math.sin(Math.toRadians(90 * stripNormal)));

                    for (int z = originZ - stripRadius; z <= originZ + stripRadius; z++) {
                        if (x == originX && z == originZ) {
                            world.setBlock(new BlockPos(x, y, z), pillarBlock.defaultBlockState(), 2);
                        } else {
                            if (world.random.nextDouble() > secondaryChance)
                                world.setBlock(new BlockPos(x, y, z), primaryBlock.defaultBlockState(), 2);
                            else if (world.random.nextDouble() > tetriaryChance)
                                world.setBlock(new BlockPos(x, y, z), secondaryBlock.defaultBlockState(), 2);
                            else
                                world.setBlock(new BlockPos(x, y, z), tetriaryBlock.defaultBlockState(), 2);
                        }
                    }

                    stripNumber++;
                }
            } else {
                world.setBlock(new BlockPos(originX, y, originZ), pillarBlock.defaultBlockState(), 2);
                decorationCoord = new int[]{originX, y, originZ};
            }
            if (--radiusHeight <= 0) {
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
        Block hazardBlock = ObjectManager.getBlock("hellfire");
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
