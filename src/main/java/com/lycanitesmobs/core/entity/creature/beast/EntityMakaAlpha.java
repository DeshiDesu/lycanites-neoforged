package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendEntitiesGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityMakaAlpha extends AgeableCreatureEntity {
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityMakaAlpha(EntityType<? extends EntityMakaAlpha> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;
        this.attackCooldownMax = 10;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(this.getClass()));
		this.targetSelector.addGoal(this.nextSpecialTargetIndex++, new DefendEntitiesGoal(this, EntityMaka.class));

		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
    }
	
	
	// ==================================================
  	//                      Update
  	// ==================================================
	@Override
	public void aiStep() {
		super.aiStep();
		
		// Alpha Sparring Cooldown:
		if(this.hasAttackTarget() && this.getTarget() instanceof EntityMakaAlpha) {
			if(this.getHealth() / this.getMaxHealth() <= 0.25F || this.getTarget().getHealth() / this.getTarget().getMaxHealth() <= 0.25F) {
				this.setTarget(null);
			}
		}
	}

	@Override
	public boolean isProtective(Entity entity) {
		if(entity instanceof EntityMaka) {
			return true;
		}
		return super.isProtective(entity);
	}
	
	
	// ==================================================
   	//                      Movement
   	// ==================================================
    // ========== Pathing Weight ==========
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if(block != Blocks.AIR) {
            if(Material.GRASS.contains(blockState.getBlock()))
                return 10F;
            if(Material.DIRT.contains(blockState.getBlock()))
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }

    // ========== Can leash ==========
    @Override
    public boolean canBeLeashed(Player player) {
        return true;
    }

	// ==================================================
	//                     Equipment
	// ==================================================
	@Override
	public int getNoBagSize() { return 0; }
	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }
	// ==================================================
   	//                      Attacks
   	// ==================================================
	@Override
    public boolean canAttack(LivingEntity target) {
		if(target instanceof EntityMaka)
			return false;
    	if(target instanceof EntityMakaAlpha && (this.getHealth() / this.getMaxHealth() <= 0.25F || target.getHealth() / target.getMaxHealth() <= 0.25F))
    		return false;
    	return super.canAttack(target);
    }

	@Override
	public boolean canAttackOwnSpecies() {
		return true;
	}

    @Override
    public void setTarget(LivingEntity entity) {
    	if(entity == null && this.getTarget() instanceof EntityMakaAlpha) {
    		this.heal((this.getMaxHealth() - this.getHealth()) / 2);
    		this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 2, false, false));
			this.getTarget().heal((this.getMaxHealth() - this.getHealth()) / 2);
			this.getTarget().addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 2, false, false));
    	}
    	super.setTarget(entity);
    }

	@Override
	public boolean rollAttackTargetChance(LivingEntity target) {
    	if(target instanceof Player || target.getType() == this.getType())
    		return this.getRandom().nextDouble() <= 0.01D;
		return true;
	}
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
        float damageMod = super.getDamageModifier(damageSrc);
        if(damageSrc.getEntity() instanceof EntityMakaAlpha)
            damageMod *= 2;
        return damageMod;
    }
    
    
    // ==================================================
    //                     Breeding
    // ==================================================
    // ========== Create Child ==========
	@Override
	public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
		return (AgeableCreatureEntity) CreatureManager.getInstance().getCreature("maka").createEntity(this.getCommandSenderWorld());
	}
}
