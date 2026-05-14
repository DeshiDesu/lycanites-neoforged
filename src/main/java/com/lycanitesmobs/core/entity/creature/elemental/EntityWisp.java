package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;

import java.util.List;

public class EntityWisp extends TameableCreatureEntity {
	EntityWisp playPartner = null;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityWisp(EntityType<? extends EntityWisp> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = false;

        this.setupMob();

        this.setMaxUpStep(1.0F);
		this.setAttackCooldownMax(this.getRangedCooldown());
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setRange(14.0F).setMinChaseDistance(10.0F));
    }


    // ==================================================
    //                      Updates
    // ==================================================
	@Override
	public void tick() {
    	super.tick();
    	if(this.playPartner != null && !this.hasAttackTarget()) {
			this.lookAt(this.playPartner, 360, 360);
		}
	}

    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

		if(!this.getCommandSenderWorld().isClientSide) {

			// Light Aura:
			if(!this.isPetType("familiar")) {
				if (this.updateTick % 10 == 0) {
					List aoeTargets = this.getNearbyEntities(LivingEntity.class, null, 2);
					for (Object entityObj : aoeTargets) {
						LivingEntity target = (LivingEntity) entityObj;
						if(target == this) {
							continue;
						}
						if(target instanceof EntityWisp) {
							if (this.playPartner == null) {
								this.playPartner = (EntityWisp) target;
								if (((EntityWisp) target).playPartner == null) {
									((EntityWisp) target).playPartner = this;
								}
							}
							continue;
						}
						this.applyBuffs(target, 1, 1);
						this.applyDebuffs(target, 1, 1);
						if(target instanceof Zombie || target instanceof Skeleton) {
							target.setSecondsOnFire(1);
						}
						if(target instanceof BaseCreatureEntity) {
							BaseCreatureEntity targetCreature = (BaseCreatureEntity)target;
							if(targetCreature.daylightBurns()) {
								targetCreature.setSecondsOnFire(1);
							}
						}
					}
				}
			}

			// Playing:
			if(this.playPartner == null) {
				this.setAttackCooldownMax(this.getRangedCooldown());
				if (this.updateTick % 40 == 0) {
					List aoeTargets = this.getNearbyEntities(EntityWisp.class, null, 30);
					for (Object entityObj : aoeTargets) {
						EntityWisp target = (EntityWisp)entityObj;
						if(target != this && this.getTarget() != target) {
							this.playPartner = target;
							if(target.playPartner == null) {
								target.playPartner = this;
							}
							break;
						}
					}
				}
			}
			else {
				if(!this.playPartner.isAlive() || this.getTarget() == this.playPartner || this.distanceTo(this.playPartner) >= 100) {
					this.playPartner = null;
				}
				else if(this.hasAttackTarget()) {
					this.setAttackCooldownMax(this.getRangedCooldown());
					if(this.getPlayerOwner() == this.playPartner.getPlayerOwner()) {
						this.playPartner.setTarget(this.getTarget());
					}
				}
				else {
					if(!this.isAttackOnCooldown() && !this.hasAttackTarget()) {
						if(this.updateTick % this.getRangedCooldown() == 0) {
							this.setAttackCooldownMax(160);
							this.playPartner.setAttackCooldownMax(160);
							this.playPartner.triggerAttackCooldown();
							this.attackRanged(this.playPartner, this.distanceTo(this.playPartner));
						}
					}
				}
			}
		}
    }

    @Override
	public boolean rollWanderChance() {
		if(this.playPartner == null) {
			return super.rollWanderChance();
		}
		if(this.isAttackOnCooldown()) {
			return false;
		}
		return this.getRandom().nextDouble() <= 0.002D;
	}


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
		this.fireProjectile("lightball", target, range, 0, new Vector3d(0, 0, 0), 0.5f, 1f, 1F);
		super.attackRanged(target, range);
    }

    @Override
	public boolean canAttack(LivingEntity targetEntity) {
    	if(targetEntity == this.playPartner) {
    		return false;
		}
		if(targetEntity instanceof EntityWisp && this.getPlayerOwner() == ((EntityWisp)targetEntity).getPlayerOwner()) {
    		return false;
		}
    	return super.canAttack(targetEntity);
	}


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() { return true; }


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
	//                   Brightness
	// ==================================================
	@Override
	public float getBrightness() {
		return 1.0F;
	}

	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender() {
		return 15728880;
	}
}
