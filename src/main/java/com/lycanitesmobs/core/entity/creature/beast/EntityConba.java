package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FireProjectilesGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAvoidTargetGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import com.lycanitesmobs.core.entity.creature.insect.EntityVespid;
import com.lycanitesmobs.core.entity.creature.insect.EntityVespidQueen;

public class EntityConba extends TameableCreatureEntity implements Enemy {
	AttackMeleeGoal aiAttackMelee;
	public boolean vespidInfection = false;
	public int vespidInfectionTime = 0;
	
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityConba(EntityType<? extends EntityConba> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
		this.aiAttackMelee = new AttackMeleeGoal(this).setLongMemory(true).setEnabled(false);
		this.goalSelector.addGoal(this.nextPriorityGoalIndex++, this.aiAttackMelee); // Melee is a priority as it is used when infected.

        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new FireProjectilesGoal(this).setProjectile("poop").setFireRate(20).setVelocity(1.2F));

		super.registerGoals();

        this.targetSelector.addGoal(this.nextSpecialTargetIndex++, new FindAvoidTargetGoal(this).setTargetClass(Player.class).setTameTargetting(false));
        this.targetSelector.addGoal(this.nextSpecialTargetIndex++, new FindAvoidTargetGoal(this).setTargetClass(Villager.class));
        this.targetSelector.addGoal(this.nextSpecialTargetIndex++, new FindAvoidTargetGoal(this).setTargetClass(Pillager.class));
    }
    
    
    // ==================================================
    //                       Name
    // ==================================================
    /** Returns the species name of this entity. **/
	@Override
    public MutableComponent getSpeciesName() {
		MutableComponent infection = Component.literal("");
		if(this.vespidInfection) {
			String entityName = this.creatureInfo.getName();
	    	if(entityName != null) {
				infection = Component.translatable("entity." + this.creatureInfo.modInfo.modid + "." + entityName + ".infected");
				infection.append(" ");
			}
		}
    	return infection.append(super.getSpeciesName());
    }
    
    public String getTextureName() {
    	if(this.vespidInfection)
    		return super.getTextureName() + "_infected";
    	return super.getTextureName();
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();
        
        // Random Leaping:
        if(this.onGround() && !this.getCommandSenderWorld().isClientSide) {
        	if(this.hasAvoidTarget()) {
        		if(this.random.nextInt(10) == 0)
        			this.leap(1.0F, 0.6D, this.getTarget());
        	}
        	else {
        		if(this.random.nextInt(50) == 0 && this.isMoving())
        			this.leap(1.0D, 0.6D);
        	}
        }
        
        // Infected AI:
        if(!this.getCommandSenderWorld().isClientSide) {
			// The Swarm:
			if(!this.vespidInfection && "theswarm".equals(this.spawnEventType)) {
				this.vespidInfection = true;
			}

            if (this.vespidInfection && !this.getCommandSenderWorld().isClientSide) {
                this.aiAttackMelee.setEnabled(true);
                if (this.vespidInfectionTime++ >= 60 * 20) {
                    this.spawnVespidSwarm();
                    this.remove(RemovalReason.DISCARDED);
                }
            } else {
                this.aiAttackMelee.setEnabled(false);
            }
        }
        
        // Infected Visuals
        if(this.getCommandSenderWorld().isClientSide) {
        	this.vespidInfection = this.extraAnimation01();
        	if(this.vespidInfection) {
    	        for(int i = 0; i < 2; ++i) {
    	            this.getCommandSenderWorld().addParticle(ParticleTypes.WITCH, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
    	        }
        	}
        }
    }
	
	// ========== AI Update ==========
	@Override
	public boolean shouldCreatureGroupFlee(LivingEntity target) {
		if(this.isTamed())
			return false;
		return super.shouldCreatureGroupFlee(target);
	}
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
	@Override
	public boolean canAttack(LivingEntity target) {
		if(target instanceof EntityVespid || target instanceof EntityVespidQueen)
			return false;
		return super.canAttack(target);
	}
    
    // ========== Ranged Attack ==========
	@Override
	public void attackRanged(Entity target, float range) {
		this.fireProjectile("poop", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
		super.attackRanged(target, range);
	}
    
    
    // ==================================================
   	//                      Death
   	// ==================================================
    @Override
    public void die(DamageSource damageSource) {
		if(!this.getCommandSenderWorld().isClientSide && this.vespidInfection)
			this.spawnVespidSwarm();
        super.die(damageSource);
    }
    
    public void spawnVespidSwarm() {
    	int j = 2 + this.random.nextInt(5) + getCommandSenderWorld().getDifficulty().getId() - 1;
        for(int k = 0; k < j; ++k) {
            float f = ((float)(k % 2) - 0.5F) * this.getDimensions(Pose.STANDING).width / 4.0F;
            float f1 = ((float)(k / 2) - 0.5F) * this.getDimensions(Pose.STANDING).width / 4.0F;
            EntityVespid vespid = (EntityVespid)CreatureManager.getInstance().getCreature("vespid").createEntity(this.getCommandSenderWorld());
            vespid.moveTo(this.position().x() + (double)f, this.position().y() + 0.5D, this.position().z() + (double)f1, this.random.nextFloat() * 360.0F, 0.0F);
            vespid.applyVariant(this.getVariantIndex());
            vespid.setGrowingAge(vespid.growthTime);
            vespid.spawnEventType = this.spawnEventType;
            this.getCommandSenderWorld().addFreshEntity(vespid);
            if(this.getTarget() != null)
            	vespid.setLastHurtByMob(this.getTarget());
        }
    }
    
    
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
    //                      Abilities
    // ==================================================
    // ========== Extra Animations ==========
    /** An additional animation boolean that is passed to all clients through the animation mask. **/
    public boolean extraAnimation01() {
    	if(!this.getCommandSenderWorld().isClientSide)
    		return this.vespidInfection;
	    else
	    	return this.extraAnimation01;
    }
    
    
    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public float getFallResistance() {
    	return 100;
    }
    
    
    // ==================================================
    //                        NBT
    // ==================================================
   	// ========== Read ===========
    /** Used when loading this mob from a saved chunk. **/
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
    	super.readAdditionalSaveData(nbt);
        
        if(nbt.contains("VespidInfection")) {
        	this.vespidInfection = nbt.getBoolean("VespidInfection");
        }
        if(nbt.contains("VespidInfectionTime")) {
        	this.vespidInfectionTime = nbt.getInt("VespidInfectionTime");
        }
    }
    
    // ========== Write ==========
    /** Used when saving this mob to a chunk. **/
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
    	nbt.putBoolean("VespidInfection", this.vespidInfection);
    	if(this.vespidInfection)
        	nbt.putInt("VespidInfectionTime", this.vespidInfectionTime);
    }
}