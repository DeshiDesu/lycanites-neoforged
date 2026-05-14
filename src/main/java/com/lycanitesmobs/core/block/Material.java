package com.lycanitesmobs.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class Material {
    public static final List<Block> AIR = new ArrayList<>();
    public static final List<Block> PLANT = new ArrayList<>();
    public static final List<Block> REPLACEABLE_PLANT = new ArrayList<>();
    public static final List<Block> WATER = new ArrayList<>();
    public static final List<Block> LAVA = new ArrayList<>();
    public static final List<Block> TOP_SNOW = new ArrayList<>();
    public static final List<Block> FIRE = new ArrayList<>();
    public static final List<Block> WEB = new ArrayList<>();
    public static final List<Block> CLAY = new ArrayList<>();
    public static final List<Block> DIRT = new ArrayList<>();
    public static final List<Block> GRASS = new ArrayList<>();
    public static final List<Block> SAND = new ArrayList<>();
    public static final List<Block> WOOD = new ArrayList<>();
    public static final List<Block> LEAVES = new ArrayList<>();
    public static final List<Block> ICE = new ArrayList<>();
    public static final List<Block> CACTUS = new ArrayList<>();
    public static final List<Block> STONE = new ArrayList<>();
    public static final List<Block> METAL = new ArrayList<>();
    public static final List<Block> SNOW = new ArrayList<>();

    private static volatile boolean INITIALIZED = false;

    public static void init() {
        if (INITIALIZED) return;
        synchronized (Material.class) {
            if (INITIALIZED) return;

            for (Block block : ForgeRegistries.BLOCKS.getValues()) {
                BlockState defaultState = block.defaultBlockState();
                MapColor color = safeMapColor(defaultState);

                if (defaultState.canBeReplaced()) {
                    if (color == MapColor.PLANT) {
                        REPLACEABLE_PLANT.add(block);
                    } else if (color == MapColor.SNOW) {
                        TOP_SNOW.add(block);
                    }
                }

                if (!defaultState.getFluidState().isEmpty()) {
                    if (color == MapColor.WATER) {
                        WATER.add(block);
                    } else if (color == MapColor.FIRE) {
                        LAVA.add(block);
                    }
                }

                if (defaultState.blocksMotion()) {
                    if (color == MapColor.CLAY) {
                        CLAY.add(block);
                    } else if (color == MapColor.DIRT) {
                        DIRT.add(block);
                    } else if (color == MapColor.GRASS) {
                        GRASS.add(block);
                    } else if (color == MapColor.SAND) {
                        SAND.add(block);
                    } else if (color == MapColor.WOOD) {
                        WOOD.add(block);
                    } else if (color == MapColor.PLANT) {
                        PLANT.add(block);
                    } else if (color == MapColor.ICE) {
                        ICE.add(block);
                    } else if (color == MapColor.STONE) {
                        STONE.add(block);
                    } else if (color == MapColor.METAL) {
                        METAL.add(block);
                    } else if (color == MapColor.SNOW) {
                        SNOW.add(block);
                    }
                }

                if (!defaultState.blocksMotion()) {
                    if (color == MapColor.NONE && defaultState.getPistonPushReaction() == PushReaction.DESTROY) {
                        if (defaultState.getFluidState().isEmpty()) {
                            AIR.add(block);
                        } else if (defaultState.getPistonPushReaction() == PushReaction.DESTROY) {
                            FIRE.add(block);
                        } else if (color == MapColor.WOOL) {
                            WEB.add(block);
                        }
                    } else if (color == MapColor.PLANT) {
                        CACTUS.add(block);
                    }
                }
            }

            INITIALIZED = true;
        }
    }

    private static MapColor safeMapColor(BlockState state) {
        try {
            return state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        } catch (Throwable t) {
            return MapColor.NONE;
        }
    }
}
