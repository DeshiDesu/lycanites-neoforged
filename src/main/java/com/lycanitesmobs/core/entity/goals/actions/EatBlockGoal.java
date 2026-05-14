package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class EatBlockGoal extends Goal {
	// Targets:
    private BaseCreatureEntity host;
    
    // Properties:
    private Block[] blocks = new Block[0];
    private Block[] materials = new Block[0];
    private Block replaceBlock = Blocks.AIR;
    private int eatTime = 40;
    private int eatTimeMax = 40;


    public EatBlockGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

     public EatBlockGoal setBlocks(Block... setBlocks) {
    	this.blocks = setBlocks;
     	return this;
     }

     public EatBlockGoal setMaterials(Block... setBlocks) {
    	this.materials = setBlocks;
     	return this;
     }

     public EatBlockGoal setReplaceBlock(Block block) {
    	this.replaceBlock = block;
     	return this;
     }

     public EatBlockGoal setEatTime(int setTime) {
    	this.eatTimeMax = setTime;
     	return this;
     }

 	@Override
    public boolean canUse() {
    	 if(this.host.getRandom().nextInt(this.host.isBaby() ? 50 : 1000) != 0)
             return false;
    	 
    	 int i = Mth.floor(this.host.position().x());
         int j = Mth.floor(this.host.position().y());
         int k = Mth.floor(this.host.position().z());

         BlockState blockState = this.host.getCommandSenderWorld().getBlockState(new BlockPos(i, j - 1, k));
         return this.isValidBlock(blockState);
     }

     public boolean isValidBlock(BlockState blockState) {
         for(Block edibleBlock : this.blocks) {
        	 if(edibleBlock == blockState.getBlock())
        		 return true;
         }
         
         Block material = blockState.getBlock();
         for(Block edibleMaterial : this.materials) {
        	 if(edibleMaterial == material)
        		 return true;
         }
         
         return false;
     }

 	@Override
    public void start() {
    	 this.eatTime = this.eatTimeMax;
         this.host.clearMovement();
     }

 	@Override
    public void stop() {
    	 this.eatTime = this.eatTimeMax;
     }

  	@Override
    public boolean canContinueToUse() {
    	  return this.eatTime > 0;
      }

 	@Override
    public void tick() {
         if(--this.eatTime != 0) return;
         
         int i = Mth.floor(this.host.position().x());
         int j = Mth.floor(this.host.position().y());
         int k = Mth.floor(this.host.position().z());
         BlockState blockState = this.host.getCommandSenderWorld().getBlockState(new BlockPos(i, j - 1, k));
         
         if(this.isValidBlock(blockState)) {
             //if(this.host.getEntityWorld().getGameRules().getGameRuleBooleanValue("mobGriefing"))
        	 this.host.getCommandSenderWorld().removeBlock(new BlockPos(i, j - 1, k), true); // Might be something else was x, y, z, false
         }

         this.host.getCommandSenderWorld().levelEvent(2001, new BlockPos(i, j - 1, k), Block.getId(blockState));
         this.host.getCommandSenderWorld().setBlock(new BlockPos(i, j - 1, k), this.replaceBlock.defaultBlockState(), 2);
         this.host.onEat();
     }
}
