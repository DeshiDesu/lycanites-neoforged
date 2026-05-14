package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.navigation.CreaturePathNavigator;
import com.lycanitesmobs.core.data.info.ObjectLists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class TemptGoal extends Goal {
    // Targets:
    private BaseCreatureEntity host;
    private Player player;
    
    // Properties:
    private double speed = 1.0D;
    private ItemStack temptItemStack = null;
    private String temptList = null;
    private int retemptTime;
    private int retemptTimeMax = 0; // Lowered from 100 because it's just annoying!
    private double temptDistanceMin = 1.0D;
    private double temptDistanceMax = 16.0D;
    private boolean scaredByPlayerMovement = false;
    private boolean stopAttack = false;
    private boolean includeTreats = true;
    private boolean includeDiet = false;
    private boolean alwaysTempted = false;

    private double targetX;
    private double targetY;
    private double targetZ;
    private double targetPitch;
    private double targetYaw;
    private boolean canSwim;
    private boolean isRunning;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public TemptGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public TemptGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    public TemptGoal setItemStack(ItemStack item) {
    	this.temptItemStack = item;
    	return this;
    }
    public TemptGoal setItemList(String list) {
    	this.temptList = list;
    	return this;
    }
    public TemptGoal setRetemptTime(int time) {
    	this.retemptTimeMax = time;
    	return this;
    }
    public TemptGoal setTemptDistanceMin(double dist) {
    	this.temptDistanceMin = dist;
    	return this;
    }
    public TemptGoal setTemptDistanceMax(double dist) {
    	this.temptDistanceMax = dist;
    	return this;
    }
    public TemptGoal setScaredByMovement(boolean scared) {
    	this.scaredByPlayerMovement = scared;
    	return this;
    }
    public TemptGoal setStopAttack(boolean setStopAttack) {
    	this.stopAttack = setStopAttack;
    	return this;
    }
    public TemptGoal setIncludeTreats(boolean includeTreats) {
        this.includeTreats = includeTreats;
        return this;
    }
    public TemptGoal setIncludeDiet(boolean includeDiet) {
        this.includeDiet = includeDiet;
        return this;
    }
    public TemptGoal setAlwaysTempted(boolean alwaysTempted) {
        this.alwaysTempted = alwaysTempted;
        return this;
    }
    
    
    // ==================================================
  	//                  Should Execute
  	// ==================================================
	@Override
    public boolean canUse() {
        if(this.retemptTime > 0) {
            --this.retemptTime;
            return false;
        }
        
        if(!this.host.canBeTempted()) {
            return false;
        }
        
        if(this.host.isTamed()) {
            return false;
        }

        if(this.player == null) {
            // Find New Player:
            this.player = this.host.getCommandSenderWorld().getNearestPlayer(this.host.position().x(), this.host.position().y(), this.host.position().z(), this.temptDistanceMax, entity -> true);
            if(this.player == null) {
                return false;
            }
        }
        else {
            // Check Current Player:
            if(this.host.distanceToSqr(this.player) > this.temptDistanceMax * this.temptDistanceMax) {
                this.player = null;
                return false;
            }
        }

        if(!this.alwaysTempted && !this.isTemptStack(this.player.getMainHandItem()) && !this.isTemptStack(this.player.getOffhandItem())) {
            this.player = null;
            return false;
        }

        return true;
    }

    public boolean isTemptStack(ItemStack itemStack) {
        if(itemStack.isEmpty()) {
            return false;
        }

        // Creature Type Treats for Tameables:
        if(this.includeTreats && this.host.canBeTempted() && this.host instanceof TameableCreatureEntity && ((TameableCreatureEntity) this.host).isTamingItem(itemStack)) {
            return true;
        }

        // Creature Diet:
        if(this.includeDiet && this.host.creatureInfo.canEat(itemStack)) {
            return true;
        }

        // Tempt List:
        if(this.temptList != null) {
            if(ObjectLists.inItemList(this.temptList, itemStack)) {
                return true;
            }
        }

        // Single Tempt Item:
        else if(this.temptItemStack != null) {
            if(itemStack.getItem() != this.temptItemStack.getItem()) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    
    // ==================================================
  	//                 Continue Executing
  	// ==================================================
	@Override
    public boolean canContinueToUse() {
        if(this.scaredByPlayerMovement) {
            if(this.host.distanceTo(this.player) < 36.0D) {
                if(this.player.distanceToSqr(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D)
                    return false;
                if(Math.abs((double)this.player.xRotO - this.targetPitch) > 5.0D || Math.abs((double)this.player.yRotO - this.targetYaw) > 5.0D)
                    return false;
            }
            else {
                this.targetX = this.player.position().x();
                this.targetY = this.player.position().y();
                this.targetZ = this.player.position().z();
            }

            this.targetPitch = (double)this.player.xRotO;
            this.targetYaw = (double)this.player.yRotO;
        }
        return this.canUse();
    }
    
    
    // ==================================================
  	//                      Start
  	// ==================================================
	@Override
    public void start() {
        this.host.setStealth(0.0F);
        this.targetX = this.player.position().x();
        this.targetY = this.player.position().y();
        this.targetZ = this.player.position().z();
        this.isRunning = true;
        if (this.host.getNavigation() instanceof GroundPathNavigation || this.host.getNavigation() instanceof CreaturePathNavigator) {
            PathNavigation navigateGround = this.host.getNavigation();
            this.canSwim = !navigateGround.canFloat();
            navigateGround.setCanFloat(true);
        }
        if(this.stopAttack) {
            this.host.setTarget(null);
        }
    }
    
    
    // ==================================================
  	//                      Reset
  	// ==================================================
	@Override
    public void stop() {
        this.player = null;
        this.host.getNavigation().stop();
        this.retemptTime = this.retemptTimeMax;
        if(this.host instanceof AgeableCreatureEntity) {
            AgeableCreatureEntity ageable = (AgeableCreatureEntity)this.host;
            if(!ageable.isBaby() && !ageable.canBreed()) {
                Math.max(this.retemptTime *= 10, 100);
            }
        }
        this.isRunning = false;
        if (this.host.getNavigation() instanceof GroundPathNavigation || this.host.getNavigation() instanceof CreaturePathNavigator) {
            PathNavigation navigateGround = this.host.getNavigation();
            navigateGround.setCanFloat(this.canSwim);
        }
    }
    
    
    // ==================================================
  	//                      Update
  	// ==================================================
	@Override
    public void tick() {
        if(this.stopAttack) {
            this.host.setTarget(null);
        }
        this.host.getLookControl().setLookAt(this.player, 30.0F, (float)this.host.getMaxHeadXRot());
        if(this.host.distanceToSqr(this.player) < this.temptDistanceMin * this.temptDistanceMin) {
            this.host.clearMovement();
        }
        else {
        	if(!this.host.useDirectNavigator()) {
                this.host.getNavigation().moveTo(this.player, this.speed);
            }
        	else {
                this.host.directNavigator.setTargetPosition(new BlockPos((int) this.player.position().x(), (int) this.player.position().y(), (int) this.player.position().z()), speed);
            }
        }
    }
    
    /**
     * @see #isRunning ???
     */
    public boolean isRunning() {
        return this.isRunning;
    }
}
