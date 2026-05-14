package com.lycanitesmobs.core.entity.base;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.goals.actions.PlayerControlGoal;
import com.lycanitesmobs.core.entity.goals.targeting.CopyRiderAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.RevengeRiderGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.ObjectLists;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.ForgeHooks;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.HashMap;

public abstract class RideableCreatureEntity extends TameableCreatureEntity {

    public Entity lastRiddenByEntity = null;

	// Jumping:
	public boolean mountJumping = false;
	public float jumpPower = 0.0F;

	public boolean abilityToggled = false;
	public boolean inventoryToggled = false;
	
	// ==================================================
  	//                    Constructor
  	// ==================================================
	protected RideableCreatureEntity(EntityType<? extends RideableCreatureEntity> entityType, Level world) {
		super(entityType, world);
		this.hasJumpSound = true;
	}


	// ========== Init AI ==========
	@Override
	protected void registerGoals() {
		// Greater Actions:
		this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new PlayerControlGoal(this));

		super.registerGoals();

		// Lesser Targeting:
		this.targetSelector.addGoal(this.nextReactTargetIndex++, new RevengeRiderGoal(this));
		this.targetSelector.addGoal(this.nextReactTargetIndex++, new CopyRiderAttackTargetGoal(this));
	}
    
    
    // ==================================================
    //                       Update
    // ==================================================
	@Override
	public void tick() {
		// Detect Dismount:
		if(this.lastRiddenByEntity != this.getControllingPassenger()) {
			if(this.lastRiddenByEntity != null)
				this.onDismounted(this.lastRiddenByEntity);
			this.lastRiddenByEntity = this.getControllingPassenger();
		}

		super.tick();
	}

	@Override
    public void aiStep() {
    	super.aiStep();

		if(this.hasRiderTarget()) {
			// Rider Buffs:
			if(this.getControllingPassenger() instanceof LivingEntity) {
				LivingEntity riderLiving = (LivingEntity)this.getControllingPassenger();

				// Run Mount Rider Effects:
				this.riderEffects(riderLiving);

				// Protect Rider from Effects:
				if(!this.canBurn()) {
					riderLiving.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (5 * 20) + 5, 1));
				}
				for(Object possibleEffect : riderLiving.getActiveEffects().toArray(new Object[0])) {
					if(possibleEffect instanceof MobEffectInstance) {
                        MobEffectInstance effectInstance = (MobEffectInstance)possibleEffect;
						if(!this.canBeAffected(effectInstance))
							riderLiving.removeEffect(effectInstance.getEffect());
					}
				}
			}

			// Mount Melee:
			if(!this.getCommandSenderWorld().isClientSide && this.hasAttackTarget() && this.updateTick % 20 == 0) {
				LivingEntity mountedAttackTarget = this.getTarget();
				if(mountedAttackTarget != null && this.canAttack(mountedAttackTarget) && this.distanceToSqr(mountedAttackTarget.position().x(), mountedAttackTarget.getBoundingBox().minY, mountedAttackTarget.position().z()) <= this.getMeleeAttackRange(mountedAttackTarget, 1)) {
					this.attackMelee(this.getTarget(), 1);
				}
			}

			// Dismounting:
//			if (!this.getCommandSenderWorld().isClientSide && this.getControllingPassenger() instanceof Player) {
//				Player player = (Player) this.getControllingPassenger();
//				ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
//				if(playerExt != null && playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT)) {
//					player.stopRiding();
//				}
//			}
		}
    	else {
    		this.abilityToggled = false;
			this.inventoryToggled = false;
    	}
    }
    
    public void riderEffects(LivingEntity rider) {
        if(!rider.canBreatheUnderwater() && this.canBreatheUnderwater() && rider.isInWater())
            rider.setAirSupply(300);
        for(MobEffectInstance effectInstance : rider.getActiveEffects().toArray(new MobEffectInstance[rider.getActiveEffects().size()])) {
        	if(!this.canBeAffected(effectInstance) && ObjectLists.inEffectList("debuffs", effectInstance.getEffect())) {
        		rider.removeEffect(effectInstance.getEffect());
			}
		}
    }

    
    // ==================================================
    //                   Mount Ability
    // ==================================================
    public void mountAbility(Entity rider) {}

    public void onDismounted(Entity entity) {
		this.setDeltaMovement(new Vec3(0,0,0));
		if(this.isSitting()) {
			int homeY = Mth.floor(this.position().y());
			if(!this.isFlying()) {
				homeY = this.getGroundY(this.blockPosition());
			}
            this.setHomePosition(Mth.floor(this.position().x()), homeY, Mth.floor(this.position().z()));
		}
	}
	
    
	// ==================================================
  	//                     Movement
  	// ==================================================
    @Override
    public boolean isPushable() {
        if(this.getControllingPassenger() != null)
            return false;
        return super.isPushable();
    }

    @Override
    public boolean canBeControlledByRider() {
	    if(this.getCommandSenderWorld().isClientSide)
	        return true;
        Entity entity = this.getControllingPassenger();
        return entity == this.getOwner();
    }
    
    @Override
    protected boolean isImmobile() {
    	// This will disable AI, we don't want this though!
    	return super.isImmobile();
    }
    
    @Override
    public void positionRider(Entity passenger,MoveFunction moveFunction) {
        if(this.hasPassenger(passenger)) {
        	double zOffset = this.getMountedZOffset();
        	if(zOffset == 0) {
        		zOffset = 0.00001D;
			}
			Vector3d mountOffset = this.getFacingPositionDouble(0, 0, 0, zOffset, this.yRotO);
            this.getControllingPassenger().setPos(this.position().x() + mountOffset.x, this.position().y() + this.getPassengersRidingOffset() + passenger.getMyRidingOffset(), this.position().z() + mountOffset.z);
        }
    }

    private void mount(Entity entity) {
    	entity.yRotO = this.yRotO;
    	entity.xRot = this.xRotO;
        if(!this.getCommandSenderWorld().isClientSide)
        	entity.startRiding(this);
    }

    public float getStrafeSpeed() {
		if (this.isSwimming() || this.isFlying()) {
			return 0.25F;
		}
		return 0.75F;
	}
    
    // ========== Move with Heading ==========
    @Override
    public void travel(Vec3 direction) {
        // Check if Mounted:
        if (!this.isTamed() || !this.hasSaddle() || !this.hasRiderTarget() || !(this.getControllingPassenger() instanceof LivingEntity) || !this.riderControl()) {
            super.travel(direction);
            return;
        }
        this.moveMountedWithHeading(direction.x(), direction.y(), direction.z());
    }

    public void moveMountedWithHeading(double strafe, double up, double forward) {
        // Apply Rider Movement:
        if(this.getControllingPassenger() instanceof LivingEntity) {
            LivingEntity rider = (LivingEntity) this.getControllingPassenger();
            this.yRotO = this.yRot = rider.getYRot();
            this.xRot = rider.xRot * 0.5F;
            this.setRot(this.yRot, this.xRot);
            this.yHeadRot = this.yBodyRot = this.yRot;
            strafe = rider.xxa * this.getStrafeSpeed();
            forward = rider.zza * this.getAISpeedModifier();
        }

        // Swimming / Flying Controls:
        double verticalMotion = 0;
        if(this.isInWater() || this.isInLava() || this.isFlying()) {
            if (this.getControllingPassenger() instanceof Player) {
                Player player = (Player) this.getControllingPassenger();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
                if(playerExt != null && playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.JUMP)) {
                    verticalMotion = this.creatureStats.getSpeed() * 20;
                }
				else if(playerExt != null && playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.DESCEND)) {
                    verticalMotion = -this.creatureStats.getSpeed() * 20;
                }
                else {
                    verticalMotion = 0;
                }
            }
        }

        else {
            // Jumping Controls:
            if (!this.isMountJumping()) {
                if (this.getControllingPassenger() instanceof Player) {
                    Player player = (Player) this.getControllingPassenger();
                    ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
                    if (playerExt != null && playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.JUMP)) {
                        this.startJumping();
                    }
                }
            }

            // Jumping Behaviour:
            if (this.getJumpPower() > 0.0F && !this.isMountJumping() && this.isControlledByLocalInstance()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, this.getMountJumpHeight() * (double) this.getJumpPower(), 0));
                if (this.hasEffect(MobEffects.JUMP))
					this.setDeltaMovement(this.getDeltaMovement().add(0, ((float) (this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F), 0));
                this.setMountJumping(true);
                this.hasImpulse = true;
                if (forward > 0.0F) {
                    float f2 = Mth.sin(this.yRot * (float) Math.PI / 180.0F);
                    float f3 = Mth.cos(this.yRot * (float) Math.PI / 180.0F);
					this.setDeltaMovement(this.getDeltaMovement().add(-0.4F * f2 * this.jumpPower, 0, 0.4F * f3 * this.jumpPower));
                }
                if (!this.getCommandSenderWorld().isClientSide)
                    this.playJumpSound();
                this.setJumpPower(0);
                ForgeHooks.onLivingJump(this);
            }
            this.setFlyingSpeed((float) (this.getSpeed() * this.getGlideScale()));
        }

		// Ability Controls:
		if(this.getControllingPassenger() instanceof Player) {
			Player player = (Player)this.getControllingPassenger();
			ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
			if(playerExt != null) {

				// Mount Ability:
				if (playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_ABILITY)) {
					this.mountAbility(player);
					this.abilityToggled = true;
				}
				else {
					this.abilityToggled = false;
				}

				// Player Inventory:
				if (playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_INVENTORY)) {
					if (!this.inventoryToggled)
						this.openGUI(player);
					this.inventoryToggled = true;
				}
				else {
					this.inventoryToggled = false;
				}
			}
		}

        // Apply Movement:
        if(this.isControlledByLocalInstance()) {
            this.setSpeed((float)this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
            if(!this.useDirectNavigator()) {
                if(this.isFlying() && !this.isInWater() && !this.isInLava()) {
                    this.moveRelative(0.1F, new Vec3(strafe, 0, forward));
                    this.move(MoverType.SELF, new Vec3(this.getDeltaMovement().x, verticalMotion / 16, this.getDeltaMovement().z));
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.8999999761581421D, 0.8999999761581421D, 0.8999999761581421D));
                }
				else if(this.isInWater()) {
					if(!this.canBreatheUnderwater()) {
						verticalMotion *= 0.25f;
						strafe *= 0.25f;
						forward *= 0.25f;
					}
					this.moveRelative(0.1F, new Vec3(strafe, 0, forward));
					this.move(MoverType.SELF, this.getDeltaMovement().add(0, verticalMotion / 16, 0));
					this.setDeltaMovement(this.getDeltaMovement().multiply(0.8999999761581421D, 0.8999999761581421D, 0.8999999761581421D));
				}
				else if(this.isInLava()) {
					if(!this.isStrongSwimmer()) {
						verticalMotion *= 0.25f;
						strafe *= 0.5f;
						forward *= 0.5f;
					}
					this.moveRelative(0.1F, new Vec3(strafe, 0, forward));
					this.move(MoverType.SELF, this.getDeltaMovement().add(0, verticalMotion / 16, 0));
					this.setDeltaMovement(this.getDeltaMovement().multiply(0.8999999761581421D, 0.8999999761581421D, 0.8999999761581421D));
				}
                else
                    super.travel(new Vec3(strafe, up, forward));
            }
            else
                this.directNavigator.flightMovement(strafe, forward);
        }

        // Clear Jumping:
        if(this.onGround() || this.isInWater() || this.isInLava()) {
            this.setJumpPower(0);
            this.setMountJumping(false);
        }

        // Animate Limbs:
        this.calculateEntityAnimation(true);
        /*this.animationSpeedOld = this.animationSpeed;
        double d0 = this.position().x() - this.xo;
        double d1 = this.position().z() - this.zo;
        float f4 = Mth.sqrt(d0 * d0 + d1 * d1) * 4.0F;
        if (f4 > 1.0F)
            f4 = 1.0F;
        this.animationSpeed += (f4 - this.animationSpeed) * 0.4F;
        this.animationPosition += this.animationSpeed;*/
    }

    // ========== Jumping Start ==========
    public void startJumping() {
    	this.setJumpPower();
    }
    
    // ========== Jumping ==========
    public double getMountJumpHeight() {
    	return 0.75D;
    }
    
    public boolean isMountJumping() {
    	return this.mountJumping;
    }
    
    public void setMountJumping(boolean set) {
    	this.mountJumping = set;
    }

    // ========== Jump Power ==========
    public void setJumpPower(int power) {
    	if(power < 0)
    		power = 0;
    	if(power > 99)
    		power = 99;
    	if(power < 90)
            this.jumpPower = 1.0F * ((float)power / 89.0F);
    	else
        	this.jumpPower = 1.0F + (1.0F * ((float)(power - 89) / 10.0F));
    }
    
    public void setJumpPower() {
    	this.setJumpPower(89);
    }
    
    public float getJumpPower() {
    	return this.jumpPower;
    }
    
    // ========== Gliding ==========
    public double getGlideScale() {
    	return 0.1F;
    }
    
    // ========== Rider Control ==========
    public boolean riderControl() {
    	return true;
    }
	
    
	// ==================================================
  	//                     Interact
  	// ==================================================
    // ========== Get Interact Commands ==========
    @Override
    public HashMap<Integer, String> getInteractCommands(Player player, @Nonnull ItemStack itemStack) {
    	HashMap<Integer, String> commands = new HashMap<Integer, String>();
    	commands.putAll(super.getInteractCommands(player, itemStack));
    	
    	// Mount:
        boolean mountingAllowed = CreatureManager.getInstance().config.mountingEnabled;
        if(mountingAllowed && this.isFlying())
            mountingAllowed = CreatureManager.getInstance().config.mountingFlightEnabled;
    	if(this.canBeMounted(player) && !player.isShiftKeyDown() && !this.getCommandSenderWorld().isClientSide && mountingAllowed)
    		commands.put(COMMAND_PIORITIES.MAIN.id, "Mount");
    	
    	return commands;
    }
    
    // ========== Perform Command ==========
    @Override
    public boolean performCommand(String command, Player player, ItemStack itemStack) {
    	
    	// Mount:
    	if(command.equals("Mount")) {
    		this.playMountSound();
            this.clearMovement();
            this.setTarget(null);
            this.mount(player);
            return true;
    	}
    	
    	return super.performCommand(command, player, itemStack);
    }
    
    
    // ==================================================
    //                       Targets
    // ==================================================
    // ========== Teams ==========
    @Override
    public Team getTeam() {
        if(this.hasRiderTarget()) {
            LivingEntity rider = this.getRider();
            if(rider != null)
                return rider.getTeam();
        }
        return super.getTeam();
    }
    
    @Override
    public boolean isAlliedTo(Entity target) {
        if(this.hasRiderTarget()) {
            LivingEntity rider = this.getRider();
            if(target == rider)
                return true;
            if(rider != null && target != null)
                return rider.isAlliedTo(target);
        }
        return super.isAlliedTo(target);
    }

	public boolean isEntityPassenger(Entity targetEntity, Entity nestedRider) {
		for(Entity entity : nestedRider.getPassengers()) {
			if (entity.equals(targetEntity)) {
				return true;
			}

			if (this.isEntityPassenger(targetEntity, entity)) {
				return true;
			}
		}

		return false;
	}
    
    
    // ==================================================
    //                     Abilities
    // ==================================================
    public boolean canBeMounted(Entity entity) {
    	if(this.getControllingPassenger() != null)
    		return false;
    	
    	// Can Be Mounted By A Player:
    	if(this.isTamed() && entity instanceof Player) {
    		Player player = (Player)entity;
    		if(player == this.getOwner())
    			return this.hasSaddle() && !this.isBaby();
    	}
    	
    	// Can Be Mounted By Mobs:
    	else if(!this.isTamed() && !(entity instanceof Player)) {
    		return !this.isBaby();
    	}
    	
    	return false;
    }
    
    
	// ==================================================
  	//                     Equipment
  	// ==================================================
    public boolean hasSaddle() {
    	ItemStack saddleStack = this.inventory.getEquipmentStack("saddle");
    	return !saddleStack.isEmpty() && saddleStack.getItem() == this.creatureInfo.creatureType.getSaddleItem();
    }

	
	// ==================================================
  	//                    Immunities
  	// ==================================================
    @Override
    public boolean hurt(DamageSource damageSource, float damageAmount) {
        Entity entity = damageSource.getEntity();
        return this.getControllingPassenger() != null && this.isEntityPassenger(entity, this) ? false : super.hurt(damageSource, damageAmount);
    }
    
    @Override
    public float getFallResistance() {
    	return 2;
    }
    
    
    // ==================================================
   	//                       Sounds
   	// ==================================================
    // ========== Mount ==========
    public void playMountSound() {
    	this.playSound(ObjectManager.getSound(this.creatureInfo.getName() + "_mount"), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}
