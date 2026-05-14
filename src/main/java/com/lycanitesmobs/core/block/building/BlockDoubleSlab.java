package com.lycanitesmobs.core.block.building;

import net.minecraft.world.level.block.Block;

public class BlockDoubleSlab extends BlockPillar {
    protected String slabName;

    // ==================================================
    //                   Constructor
    // ==================================================
    public BlockDoubleSlab(Block.Properties properties, String name, String slabName) {
        super(properties, name);
        this.slabName = slabName;
        setup();
    }


    // ==================================================
    //                      Break
    // ==================================================
    //========== Drops ==========
    // TODO Slab Drops
}
