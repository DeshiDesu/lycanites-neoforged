package com.lycanitesmobs.core.entity.creature.imp;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class EntityPixen extends TameableCreatureEntity implements Enemy {

    protected boolean wantsToLand;
    protected boolean  isLanded;

    public int auraRate = 60;

    /** A list of beneficial potion effects that this element can grant. **/
    public List<String> auraEffects = new ArrayList<>();

    /** The duration (in ticks) of the random effect. **/
    public int auraDuration = 100;

    /** The random effect amplifier. **/
    public int auraAmplifier = 0;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityPixen(EntityType<? extends EntityPixen> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.hasAttackSound = false;
        this.flySoundSpeed = 5;
        this.setMaxUpStep(1.0F);
        this.setupMob();

        // Random Aura Effects:
        this.auraEffects.add("minecraft:speed");
        this.auraEffects.add("minecraft:haste");
        this.auraEffects.add("minecraft:jump_boost");
        this.auraEffects.add("lycanitesmobs:fallresist");
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setAlwaysTempted(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1D).setRange(14.0F).setMinChaseDistance(5.0F).setCheckSight(false));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

        // Land/Fly:
        if(!this.getCommandSenderWorld().isClientSide) {
            if(this.isLanded) {
                this.wantsToLand = false;
                if(!this.isSitting() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean()) {
                    this.leap(1.0D, 1.0D);
                    this.isLanded = false;
                }
            }
            else {
                if(this.wantsToLand) {
                    if(this.isSafeToLand()) {
                        this.isLanded = true;
                    }
                }
                else {
                    if (this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean()) {
                        this.wantsToLand = true;
                    }
                }
            }
        }

        // Mischief Aura:
        if(!this.getCommandSenderWorld().isClientSide && this.auraRate > 0 && !this.isPetType("familiar")) {
            if (this.updateTick % this.auraRate == 0) {
                List aoeTargets = this.getNearbyEntities(LivingEntity.class, null, 4);
                for (Object entityObj : aoeTargets) {
                    LivingEntity target = (LivingEntity) entityObj;
                    if (target != this && !(target instanceof EntityPixen) && target != this.getTarget() && target != this.getAvoidTarget()) {
                        int randomIndex = this.getRandom().nextInt(this.auraEffects.size());
                        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(this.auraEffects.get(randomIndex)));
                        if(effect != null) {
                            target.addEffect(new MobEffectInstance(effect, this.auraDuration, this.auraAmplifier));
                        }
                    }
                }
            }
        }
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Get Wander Position ==========
    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        if(this.wantsToLand || !this.isLanded) {
            BlockPos groundPos;
            for(groundPos = wanderPosition.below(); groundPos.getY() > 0 && this.getCommandSenderWorld().getBlockState(groundPos).getBlock() == Blocks.AIR; groundPos = groundPos.below()) {}
            if(this.getCommandSenderWorld().getBlockState(groundPos).isSolid()) {
                return groundPos.above();
            }
        }
        return super.getWanderPosition(wanderPosition);
    }

    // ========== Get Flight Offset ==========
    public double getFlightOffset() {
        if(!this.wantsToLand) {
            super.getFlightOffset();
        }
        return 0;
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("tricksterflare", target, range, 0, new Vector3d(0, 0, 0), 0.75f, 1f, 1F);
        super.attackRanged(target, range);
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean isFlying() { return !this.isLanded; }

    @Override
    public boolean isStrongSwimmer() { return false; }

    @Override
    public boolean canBreatheUnderwater() {
        return false;
    }

    @Override
    public boolean canBeTempted() {
        return !this.isInPack() && this.getLastHurtByMob() == null;
    }

    @Override
    public boolean isAggressive() {
        if(!this.isInPack() && this.getLastHurtByMob() == null) {
            return false;
        }
        return super.isAggressive();
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
   	//                     Immunities
   	// ==================================================
    @Override
    public float getFallResistance() {
        return 100;
    }
}
