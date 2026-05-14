package com.lycanitesmobs.core.entity.creature.avian;

import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3d;

public class EntityRoc extends RideableCreatureEntity implements Enemy {
    public AttackMeleeGoal attackAI;

    public boolean creeperDropping = true;

    private int creeperDropCooldown = 0;
	
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityRoc(EntityType<? extends EntityRoc> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;
        this.flySoundSpeed = 20;

        this.setupMob();

        this.setMaxUpStep(1.0F);
    }

    @Override
    protected void registerGoals() {
        if (this.creeperDropping) {
            this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.CREEPER));
        }

        super.registerGoals();

        this.attackAI = new AttackMeleeGoal(this).setLongMemory(false);
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, this.attackAI);
    }

    @Override
    public void loadCreatureFlags() {
        this.creeperDropping = this.creatureInfo.getFlag("creeperDropping", this.creeperDropping);
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

        // Entity Pickup Update:
        if(!this.getCommandSenderWorld().isClientSide && this.getControllingPassenger() == null) {
            // Attack AI and Creeper Carrying:
	    	this.attackAI.setEnabled(this.hasPickupEntity() ? this.getPickupEntity() instanceof Creeper : this.creeperDropCooldown <= 0);
            if(this.creeperDropCooldown > 0) {
                this.creeperDropCooldown--;
            }

            // Pickup Update:
	    	if(this.hasPickupEntity()) {
	    		ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPickupEntity());
	    		if(extendedEntity != null)
	    			extendedEntity.setPickedUpByEntity(this);

                // Drop Creeper On Target:
                if(this.getPickupEntity() instanceof Creeper && this.hasAttackTarget() && !(this.getTarget() instanceof Creeper)) {
                    double distance = new Vector3d(this.position().x(), 0, this.position().z()).distance(new Vector3d(this.getTarget().position().x(), 0, this.getTarget().position().z()));
                    if(distance <= 2D && this.position().y() > this.getTarget().position().y()) {
                        this.getPickupEntity().setLastHurtByMob(this.getTarget());
                        this.dropPickupEntity();
                        this.creeperDropCooldown = 6 * 20;
                    }
                }

                // Random Dropping:
                if(this.hasPickupEntity()) {
                    if (this.tickCount % 100 == 0 && this.getRandom().nextBoolean()) {
                        if (this.getPickupEntity() instanceof Player) {
                            for (int distToGround = 0; distToGround < 8; distToGround++) {
                                Block searchBlock = this.getCommandSenderWorld().getBlockState(new BlockPos((int) this.position().x(), (int) this.position().y() - distToGround, (int) this.position().z())).getBlock();
                                if (searchBlock != null && searchBlock != Blocks.AIR) {
                                    this.dropPickupEntity();
                                    this.leap(1.0F, 2.0D);
                                    break;
                                }
                            }
                        } else if (!(this.getPickupEntity() instanceof Creeper))
                            this.dropPickupEntity();
                    }
                }
	    	}
	    	
	    	/*/ Random Swooping:
	    	else if(this.hasAttackTarget() && !this.hasPickupEntity() && this.getDistance(this.getAttackTarget()) > 2 && this.getRNG().nextInt(20) == 0) {
	    		if(this.getPositionVec().getY() - 1 > this.getAttackTarget().getPositionVec().getY())
	    			this.leap(6.0F, -1.0D, this.getAttackTarget());
	    		else if(this.getPositionVec().getY() + 1 < this.getAttackTarget().getPositionVec().getY())
	    			this.leap(6.0F, 1.0D, this.getAttackTarget());
	    		else
	    			this.leap(6.0F, 0D, this.getAttackTarget());
	    	}*/
        }

        // Mounted Creeper Carrying:
        if(!this.getCommandSenderWorld().isClientSide && this.getControllingPassenger() == null && this.getPickupEntity() instanceof Creeper) {
            ((Creeper) this.getPickupEntity()).setTarget(null); // Prevent the carried Creeper from exploding on the riding player.
        }
    }

    @Override
    public void riderEffects(LivingEntity rider) {
        if(rider.hasEffect(MobEffects.WEAKNESS))
            rider.removeEffect(MobEffects.WEAKNESS);
        if(rider.hasEffect(MobEffects.DIG_SLOWDOWN))
            rider.removeEffect(MobEffects.DIG_SLOWDOWN);
    }


    // ==================================================
    //                      Movement
    // ==================================================
    /** Returns how high above attack targets this mob should fly when chasing. **/
    @Override
    public double getFlightOffset() {
        if(this.hasPickupEntity()) {
			return 5D;
		}
        return super.getFlightOffset();
    }

    @Override
    public boolean rollWanderChance() {
        if(this.isFlying())
            return this.getRandom().nextDouble() <= 0.25D;
        return this.getRandom().nextDouble() <= 0.008D;
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
    	if(!super.attackMelee(target, damageScale))
    		return false;

        if(target instanceof LivingEntity && this.getControllingPassenger() == null) {
            LivingEntity entityLivingBase = (LivingEntity)target;
            // Pickup:
            if (this.canPickupEntity(entityLivingBase)) {
                this.pickupEntity(entityLivingBase);
            }
            if(entityLivingBase instanceof Creeper) {
                entityLivingBase.setLastHurtByMob(null);
                ((Creeper) entityLivingBase).setTarget(null);
                this.setTarget(null);
            }
        }
        
        return true;
    }

    @Override
    public boolean canAttack(LivingEntity targetEntity) {
        if(this.isTamed()) {
            return super.canAttack(targetEntity);
        }

        // Ignore Targets Picked Up By Another Mob:
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(targetEntity);
        if(extendedEntity != null && extendedEntity.pickedUpByEntity != null) {
            return false;
        }

        // Creeper Bombing:
        if(!this.creeperDropping && targetEntity instanceof Creeper) {
            return false;
        }
        if(this.hasPickupEntity()) {
            if (targetEntity instanceof Creeper)
                return false;
        }
        if(this.creeperDropCooldown > 0) {
            return false;
        }

        return super.canAttack(targetEntity);
    }

    @Override
    public boolean isAggressive() {
        if(this.isTamed()) {
            return super.isAggressive();
        }
        if ("".equals(this.spawnEventType) && this.getCommandSenderWorld().isDay() && this.testLightLevel() >= 2) {
            return false;
        }
        return super.isAggressive();
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean isFlying() { return true; }

    // ========== Pickup ==========
    @Override
    public void pickupEntity(LivingEntity entity) {
        super.pickupEntity(entity);
        if(this.getCommandSenderWorld().getBlockState(this.blockPosition()) != null && this.getCommandSenderWorld().canSeeSkyFromBelowWater(this.blockPosition()))
            this.leap(0.5F, 4.0D);
    }
    
    @Override
    public double[] getPickupOffset(Entity entity) {
        if(entity != null) {
            return new double[]{0, 1 - entity.getDimensions(Pose.STANDING).height, 0};
        }
    	return new double[]{0, -1, 0};
    }

    @Override
    public boolean canPickupEntity(LivingEntity entity) {
        if(this.creeperDropCooldown > 0 || this.hasPickupEntity())
            return false;
        return super.canPickupEntity(entity);
    }

    @Override
    public boolean canAttackWithPickup() {
        if(this.getPickupEntity() instanceof Creeper) {
            return true;
        }
        return super.canAttackWithPickup();
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
    //                     Positions
    // ==================================================
    // ========== Get Wander Position ==========
    /** Takes an initial chunk coordinate for a random wander position and ten allows the entity to make changes to the position or react to it. **/
    @Override
    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        if(this.hasPickupEntity() && this.getPickupEntity() instanceof Player)
            return new BlockPos(wanderPosition.getX(), this.restrictYHeightFromGround(wanderPosition, 6, 14), wanderPosition.getZ());
        return super.getWanderPosition(wanderPosition);
    }


    // ==================================================
    //                   Mount Ability
    // ==================================================
    @Override
    public void mountAbility(Entity rider) {
        if(this.getCommandSenderWorld().isClientSide)
            return;

        if(this.abilityToggled)
            return;

        if(this.hasPickupEntity()) {
            if(this.getPickupEntity() instanceof Creeper) {
                ((Creeper)this.getPickupEntity()).ignite();
            }
            this.dropPickupEntity();
            return;
        }

        if(this.getStamina() < this.getStaminaCost())
            return;

        LivingEntity nearestTarget = this.getNearestEntity(LivingEntity.class, null, 4, false);
        if(this.canPickupEntity(nearestTarget)) {
            this.pickupEntity(nearestTarget);
        }

        this.applyStaminaCost();
    }

    public float getStaminaCost() {
        return 20;
    }

    public int getStaminaRecoveryWarmup() {
        return 5 * 20;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }
}
