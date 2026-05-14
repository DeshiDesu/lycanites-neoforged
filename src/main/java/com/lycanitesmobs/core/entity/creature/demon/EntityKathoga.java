package com.lycanitesmobs.core.entity.creature.demon;

import com.google.common.base.Predicate;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.PlayerControlGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.List;

public class EntityKathoga extends RideableCreatureEntity {
	
	PlayerControlGoal playerControlAI;

    public EntityKathoga(EntityType<? extends EntityKathoga> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.UNDEAD;
        this.hasAttackSound = true;
        this.spreadFire = true;
        this.setupMob();

        this.setMaxUpStep(1.0F);;
    }

    @Override
	public boolean canBurn() {
		return false;
	}

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(ZombifiedPiglin.class).setSpeed(1.5D).setDamageScale(8.0D).setRange(2.5D));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(Hoglin.class).setSpeed(1.5D).setDamageScale(8.0D).setRange(2.5D));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(EnderMan.class).setSpeed(1.5D).setDamageScale(8.0D).setRange(2.5D));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setSpeed(1.5D));

        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.ZOMBIFIED_PIGLIN));
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.PIGLIN));
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.PIGLIN_BRUTE));
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.HOGLIN));
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.ENDERMAN));
    }

	@Override
	public boolean shouldCreatureGroupFlee(LivingEntity target) {
		return false;
	}

	@Override
    public void aiStep() {
        super.aiStep();
        
        // Become a farmed animal if removed from the Nether to another dimension, prevents natural despawning.
        if(this.getCommandSenderWorld().dimension() != Level.NETHER)
        	this.setFarmed();
    }
    
    public void riderEffects(LivingEntity rider) {
    	if(rider.hasEffect(MobEffects.WITHER))
    		rider.removeEffect(MobEffects.WITHER);
        if(rider.isOnFire())
            rider.setSecondsOnFire(0);
    }

    @Override
    public void mountAbility(Entity rider) {
    	if(this.getCommandSenderWorld().isClientSide)
    		return;

    	if(this.abilityToggled)
    		return;
    	if(this.getStamina() < this.getStaminaCost())
    		return;
        this.specialAttack();
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

    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if(!super.attackMelee(target, damageScale))
        	return false;
        
    	// Breed:
        if((target instanceof Animal || (target instanceof BaseCreatureEntity && ((BaseCreatureEntity)target).creatureInfo.isFarmable())) && target.getDimensions(Pose.STANDING).height >= 1F)
    		this.breed();
    	
        return true;
    }

    public void specialAttack() {
        // Withering Roar:
        double distance = 5.0D;
        List<LivingEntity> possibleTargets = this.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(distance, distance, distance), (Predicate<LivingEntity>) possibleTarget -> {
            if(!possibleTarget.isAlive()
                    || possibleTarget == EntityKathoga.this
                    || EntityKathoga.this.isEntityPassenger(possibleTarget, EntityKathoga.this)
                    || EntityKathoga.this.isAlliedTo(possibleTarget)
                    || !EntityKathoga.this.canAttackType(possibleTarget.getType())
                    || !EntityKathoga.this.canAttack(possibleTarget))
                return false;
            return true;
        });
        if(!possibleTargets.isEmpty()) {
            for(LivingEntity possibleTarget : possibleTargets) {
                boolean doDamage = true;
                if(this.getRider() instanceof Player) {
                    if(MinecraftForge.EVENT_BUS.post(new AttackEntityEvent((Player)this.getRider(), possibleTarget))) {
                        doDamage = false;
                    }
                }
                if(doDamage) {
                    possibleTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 10 * 20, 0));
                }
            }
        }
        this.playAttackSound();
        this.triggerAttackCooldown();
    }

    public boolean canBeTempted() {
    	return this.isBaby();
    }

    public boolean petControlsEnabled() { return true; }

    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }

    @Override
    public float getFallResistance() {
    	return 10;
    }

	@Override
	public boolean isBreedingItem(ItemStack itemStack) {
        return false; // Breeding is triggered by attacking specific mobs instead!
    }
}
