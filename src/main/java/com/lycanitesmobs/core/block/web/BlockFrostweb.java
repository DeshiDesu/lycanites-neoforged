package com.lycanitesmobs.core.block.web;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.base.BlockBase;
import com.lycanitesmobs.core.manager.ItemManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockFrostweb extends BlockBase {

    // ==================================================
    //                   Constructor
    // ==================================================
    public BlockFrostweb(Block.Properties properties) {
        super(properties, "frostweb");

        this.blockName = "frostweb";

        // Stats:
        this.tickRate = 200;
        this.removeOnTick = true;
        this.loopTicks = false;
        this.canBeCrushed = false;

        this.noBreakCollision = false;

        this.setRegistryName(LycanitesMobs.MODID, this.blockName.toLowerCase());
        this.registerDefaultState(this.getStateDefinition().any().setValue(AGE, 0));

        ItemManager.getInstance().cutoutBlocks.add(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }


    // ==================================================
    //                     Break
    // ==================================================
    /*@Override
    public Item getItemDropped(BlockState blockState, Random random, int fortune) {
        return ObjectManager.getItem("frostwebcharge");
    }

    @Override
    public int damageDropped(BlockState blockState) {
        return 0;
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }*/


    // ==================================================
    //                Collision Effects
    // ==================================================
    @Override
    public void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity) {
        super.entityInside(blockState, world, pos, entity);
        entity.makeStuckInBlock(blockState, new Vec3(0.25D, (double) 0.05F, 0.25D));
    }


    // ==================================================
    //                      Particles
    // ==================================================
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        if (random.nextInt(100) == 0) {
            x += random.nextFloat();
            z += random.nextFloat();
            world.addParticle(ParticleTypes.ITEM_SNOWBALL, x, y, z, 0.0D, 0.0D, 0.0D);
        }
        super.animateTick(state, world, pos, random);
    }


    // ==================================================
    //                      Rendering
    // ==================================================
    /*@Override Redundant?
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }*/
}
