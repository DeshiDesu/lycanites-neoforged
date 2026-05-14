package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.effect.EffectBase;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityOstimien extends TameableCreatureEntity {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityOstimien(EntityType<? extends EntityOstimien> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.ARTHROPOD;
        this.hasAttackSound = true;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new StealthGoal(this).setStealthTime(20).setStealthAttack(true).setStealthMove(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();
        
        // Lurker Blind Stalking:
        if(this.getTarget() != null) {
        	EffectBase stalkingEffect = ObjectManager.getEffect("plague");
        	if(stalkingEffect != null && this.getTarget().hasEffect(stalkingEffect))
        		this.setAvoidTarget(this.getTarget());
        	else
        		this.setAvoidTarget(null);
        }
        else
        	this.setAvoidTarget(null);
        
        // Leap:
        if(this.onGround() && !this.getCommandSenderWorld().isClientSide && this.random.nextInt(10) == 0) {
        	if(this.hasAttackTarget())
        		this.leap(6.0F, 0.4D, this.getTarget());
        	else if(this.hasAvoidTarget())
        		this.leap(4.0F, 0.4D);
        }
    }
    
    
    // ==================================================
   	//                     Stealth
   	// ==================================================
    @Override
    public boolean canStealth() {
    	if(this.getCommandSenderWorld().isClientSide) return false;
    	else {
	    	if(this.hasAttackTarget()) {
	    		if(this.getTarget() instanceof Player) {
	    			Player playerTarget = (Player)this.getTarget();
	    			ItemStack itemstack = playerTarget.getInventory().getSelected();
	    			if(this.isTamingItem(itemstack))
	    				return false;
	    		}
				EffectBase stalkingEffect = ObjectManager.getEffect("instability");
	    		if(stalkingEffect != null) {
					if(!this.getTarget().hasEffect(stalkingEffect))
						return false;
				}
	    		if(this.distanceTo(this.getTarget()) < (5.0D * 5.0D))
	    			return false;
	    	}
	    	else {
	    		if(this.isMoving())
	    			return false;
	    	}
	        return true;
        }
    }
    
    @Override
    public void startStealth() {
    	if(this.getCommandSenderWorld().isClientSide) {
            ParticleOptions particle = ParticleTypes.SMOKE;
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            for(int i = 0; i < 100; i++)
            	this.getCommandSenderWorld().addParticle(particle, this.position().x() + (double)(this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double)this.getDimensions(Pose.STANDING).width, this.position().y() + 0.5D + (double)(this.random.nextFloat() * this.getDimensions(Pose.STANDING).height), this.position().z() + (double)(this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double)this.getDimensions(Pose.STANDING).width, d0, d1, d2);
        }
    	super.startStealth();
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean canClimb() { return true; }
    
    
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }


    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }
    
    
    // ==================================================
    //                       Visuals
    // ==================================================
    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isInvisibleTo(Player player) {
    	if(this.isTamed() && this.getOwner() == player)
    		return false;
        return this.isInvisible();
    }
}
