package com.lycanitesmobs.core.entity.creature.aberration;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

public class EntityTrite extends TameableCreatureEntity implements Enemy {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityTrite(EntityType<? extends EntityTrite> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

        // Leap:
        if(this.hasAttackTarget() && this.onGround() && !this.getCommandSenderWorld().isClientSide && this.random.nextInt(10) == 0)
        	this.leap(6.0F, 0.6D, this.getTarget());
    }
    
    
    // ==================================================
   	//                      Attacks
   	// ==================================================
    @Override
    public boolean canAttack(LivingEntity target) {
        if(target instanceof EntityAstaroth ||  target instanceof EntityAsmodeus)
            return false;
        return super.canAttack(target);
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    // ========== Movement ==========
    @Override
    public boolean canClimb() { return true; }

    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }
    // ==================================================
  	//                     Immunities
  	// ==================================================
    @Override
    public boolean canBeAffected(MobEffectInstance potionEffect) {
		if(potionEffect.getEffect() == MobEffects.WITHER)
			return false;
		if(ObjectManager.getEffect("decay") != null)
			if(potionEffect.getEffect() == ObjectManager.getEffect("decay")) return false;
        super.canBeAffected(potionEffect);
        return true;
    }
    
    @Override
    public boolean canBurn() { return false; }

    @Override
    public float getFallResistance() {
        return 10;
    }
    
    // ========== Environmental ==========
    @Override
    public boolean webProof() { return true; }
}
