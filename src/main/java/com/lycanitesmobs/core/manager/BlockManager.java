package com.lycanitesmobs.core.manager;


import com.lycanitesmobs.core.block.base.BlockBase;
import com.lycanitesmobs.core.block.building.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.util.Lazy;

public class BlockManager {
    // ==================================================
    //                Add Dungeon Blocks
    // ==================================================

    /**
     * Creates a set of themed dungeon blocks such as tiles, bricks, pillars, etc as well as a crystal light source. The block name is added to a list that is used to automatically add each recipe at Post Init.
     *
     * @param stoneName The name of the stone block, such as "demon" or "shadow", etc. (stone and crystal are appended).
     **/
    public static void addDungeonBlocks(String stoneName) {
        Block.Properties properties = Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F);
        Block.Properties crystalProperties = Block.Properties.of().mapColor(MapColor.NONE).sound(SoundType.GLASS).strength(0.5F, 1200.0F).lightLevel((BlockState blockState) -> {
            return 15;
        });

        Lazy<? extends Block> stoneBlock = Lazy.of(() -> new BlockBase(properties, stoneName + "stone"));
        Lazy<? extends Block> crystalStair = Lazy.of(() -> new BlockStairsCustom(properties, (BlockBase) stoneBlock.get()));
        Lazy<? extends Block> crystalSlab = Lazy.of(() -> new BlockSlabCustom(properties, (BlockBase) stoneBlock.get()));
        Lazy<? extends Block> stoneBrickBlock = Lazy.of(() -> new BlockBase(properties, stoneName + "stonebrick"));
        Lazy<? extends Block> stoneBrickStairs = Lazy.of(() -> new BlockStairsCustom(properties, (BlockBase) stoneBrickBlock.get()));
        Lazy<? extends Block> stoneBrickSlab = Lazy.of(() -> new BlockSlabCustom(properties, (BlockBase) stoneBrickBlock.get()));
        Lazy<? extends Block> stoneBrickFence = Lazy.of(() -> new BlockFenceCustom(properties, (BlockBase) stoneBrickBlock.get()));
        Lazy<? extends Block> stoneBrickWall = Lazy.of(() -> new BlockWallCustom(properties, (BlockBase) stoneBrickBlock.get()));
        Lazy<? extends Block> stoneTileBlock = Lazy.of(() -> new BlockBase(properties, stoneName + "stonetile"));
        Lazy<? extends Block> stoneTilePolished = Lazy.of(() -> new BlockBase(properties, stoneName + "stonepolished"));
        Lazy<? extends Block> stoneTileChiseled = Lazy.of(() -> new BlockBase(properties, stoneName + "stonechiseled"));
        Lazy<? extends Block> stoneTileCrystal = Lazy.of(() -> new BlockBase(crystalProperties, stoneName + "crystal"));
        Lazy<? extends Block> stoneTileStairs = Lazy.of(() -> new BlockStairsCustom(properties, (BlockBase) stoneTileBlock.get()));
        Lazy<? extends Block> stoneTileSlab = Lazy.of(() -> new BlockSlabCustom(properties, (BlockBase) stoneTileBlock.get()));
        Lazy<? extends Block> stoneTilePillar = Lazy.of(() -> new BlockPillar(properties, stoneName + "stonepillar"));

        ObjectManager.addBlock(stoneName + "stone", stoneBlock, false);
        ObjectManager.addBlock(stoneName + "stone_stairs", crystalStair, false);
        ObjectManager.addBlock(stoneName + "stone_slab", crystalSlab, false);
        ObjectManager.addBlock(stoneName + "stonebrick", stoneBrickBlock, false);
        ObjectManager.addBlock(stoneName + "stonebrick_stairs", stoneBrickStairs, false);
        ObjectManager.addBlock(stoneName + "stonebrick_slab", stoneBrickSlab, false);
        ObjectManager.addBlock(stoneName + "stonebrick_fence", stoneBrickFence, false);
        ObjectManager.addBlock(stoneName + "stonebrick_wall", stoneBrickWall, false);
        ObjectManager.addBlock(stoneName + "stonetile", stoneTileBlock, false);
        ObjectManager.addBlock(stoneName + "stonetile_stairs", stoneTileStairs, false);
        ObjectManager.addBlock(stoneName + "stonetile_slab", stoneTileSlab, false);

        ObjectManager.addBlock(stoneName + "stonepolished", stoneTilePolished, false);
        ObjectManager.addBlock(stoneName + "stonechiseled", stoneTileChiseled, false);
        ObjectManager.addBlock(stoneName + "stonepillar", stoneTilePillar, false);

        ObjectManager.addBlock(stoneName + "crystal", stoneTileCrystal, false);
    }
}
