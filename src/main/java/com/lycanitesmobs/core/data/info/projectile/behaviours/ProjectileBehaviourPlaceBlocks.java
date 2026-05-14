package com.lycanitesmobs.core.data.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.registries.ForgeRegistries;

public class ProjectileBehaviourPlaceBlocks extends ProjectileBehaviour {
    /**
     * The name of the block to place.
     **/
    public String blockName;

    /**
     * The chance of placing a block at each location.
     **/
    public double chance = 1;

    /**
     * The radius of blocks placed.
     **/
    public int radius = 1;

    /**
     * The height of blocks placed.
     **/
    public int height = 1;

    /**
     * Whether the block must be placed on top of a solid block (use for fires, etc)
     **/
    public boolean solidSurface = true;

    @Override
    public void loadFromJSON(JsonObject json) {
        this.blockName = json.get("block").getAsString();

        if (json.has("chance"))
            this.chance = json.get("chance").getAsDouble();

        if (json.has("radius"))
            this.radius = json.get("radius").getAsInt();

        if (json.has("height"))
            this.height = json.get("height").getAsInt();

        if (json.has("solidSurface"))
            this.solidSurface = json.get("solidSurface").getAsBoolean();
    }

    @Override
    public void onProjectileImpact(BaseProjectileEntity projectile, Level world, BlockPos pos) {
        if (world.isClientSide) return;

        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(this.blockName));
        if (block == null) return;

        BlockState base = block.defaultBlockState();
        BlockState blockState = applyLevelIfPresent(base, 8);

        BlockPos start = pos.above();
        for (int x = -this.radius + 1; x < this.radius; x++) {
            for (int y = this.height - 1; y < this.height; y++) {
                for (int z = -this.radius + 1; z < this.radius; z++) {
                    BlockPos placePos = start.offset(x, y, z);
                    if (this.solidSurface && !world.getBlockState(placePos.below()).isFaceSturdy(world, placePos.below(), Direction.UP)) {
                        continue;
                    }
                    if (!blockState.canSurvive(world, placePos)) continue;
                    if (!projectile.canDestroyBlock(placePos)) continue;
                    if (this.chance < 1.0 && world.random.nextDouble() > this.chance) continue;

                    world.setBlockAndUpdate(placePos, blockState);
                }
            }
        }
    }

    private static BlockState applyLevelIfPresent(BlockState state, int desired) {
        for (var prop : state.getProperties()) {
            if (prop instanceof IntegerProperty ip) {
                String n = ip.getName();
                if ("level".equals(n) || "layers".equals(n)) {
                    int min = ip.getPossibleValues().stream().mapToInt(Integer::intValue).min().orElse(desired);
                    int max = ip.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(desired);
                    int v = Mth.clamp(desired, min, max);
                    return state.setValue(ip, v);
                }
            }
        }
        return state;
    }

}
