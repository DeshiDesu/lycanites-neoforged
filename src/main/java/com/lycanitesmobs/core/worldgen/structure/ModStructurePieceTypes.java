package com.lycanitesmobs.core.worldgen.structure;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class ModStructurePieceTypes {

    public static StructurePieceType LM_DUNGEON_PIECE;

    public static void init() {
        LM_DUNGEON_PIECE = register("lm_dungeon_piece", LMDungeonPiece::new);
    }

    private static StructurePieceType register(String name, StructurePieceType.ContextlessType type) {
        return Registry.register(
                BuiltInRegistries.STRUCTURE_PIECE,
                new ResourceLocation(LycanitesMobs.MODID, name),
                type);
    }
}
