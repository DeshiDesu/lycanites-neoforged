package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class BegGoal extends Goal {
	// Targets:
    private TameableCreatureEntity host;
    private Player player;
    
    // Properties:
    private float range = 8.0F * 8.0F;
    private int begTime;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public BegGoal(TameableCreatureEntity setHost) {
        this.host = setHost;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public BegGoal setRange(float setRange) {
    	this.range = setRange * setRange;
    	return this;
    }
	
    
	// ==================================================
 	//                   Should Execute
 	// ==================================================
	@Override
    public boolean canUse() {
        this.player = this.host.getCommandSenderWorld().getNearestPlayer(this.host.position().x(), this.host.position().y(), this.host.position().z(), (double) this.range, entity -> true);
        return this.player != null && this.gotBegItem(this.player);
    }
	
    
	// ==================================================
 	//                 Continue Executing
 	// ==================================================
	@Override
    public boolean canContinueToUse() {
        return this.player.isAlive() && (!(this.host.distanceTo(this.player) > (double) (this.range * this.range)) && (this.begTime > 0 && this.gotBegItem(this.player)));
    }
	
    
	// ==================================================
 	//                      Start
 	// ==================================================
	@Override
    public void start() {
        this.host.clearMovement();
        this.begTime = 40 + this.host.getRandom().nextInt(40);
    }
	
    
	// ==================================================
 	//                       Reset
 	// ==================================================
	@Override
    public void stop() {
        this.player = null;
    }
	
    
	// ==================================================
 	//                      Update
 	// ==================================================
	@Override
    public void tick() {
        this.host.getLookControl().setLookAt(this.player.position().x(), this.player.position().y() + (double)this.player.getEyeHeight(), this.player.position().z(), 10.0F, (float)this.host.getMaxHeadXRot());
        --this.begTime;
    }
	
    
	// ==================================================
 	//                    Got Beg Item
 	// ==================================================
    private boolean gotBegItem(Player player) {
        ItemStack itemstack = player.getInventory().getSelected();
        if(itemstack.isEmpty())
        	return false;
        
        if(!this.host.isTamed())
        	return this.host.isTamingItem(itemstack);
        
        return this.host.isBreedingItem(itemstack);
    }
}
