package com.lycanitesmobs.core.entity.creature.demon;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.util.GoalConditions;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.ChaseGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.EffectAuraGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FaceTargetGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.SummonMinionsGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class EntityApollyon extends TameableCreatureEntity implements Enemy {

    public EntityApollyon(EntityType<? extends EntityApollyon> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.UNDEAD;
        this.hasAttackSound = false;
        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(this.nextIdleGoalIndex, new FaceTargetGoal(this));
        this.goalSelector.addGoal(this.nextCombatGoalIndex, new ChaseGoal(this).setMinDistance(16F).setMaxDistance(64F).setSpeed(1));
//        this.goalSelector.addGoal(this.nextCombatGoalIndex, new BuildAroundTargetGoal(this).setBlock(ObjectManager.getBlock("doomfire")).setTickRate(40).setRange(3).setEnclose(true).setTargetBit(TARGET_BITS.ATTACK));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0D).setRange(32.0F).setMinChaseDistance(16.0F).setChaseTime(-1));
        this.goalSelector.addGoal(this.nextCombatGoalIndex, new EffectAuraGoal(this).setEffect(MobEffects.DAMAGE_BOOST).setAmplifier(2).setEffectSeconds(2).setRange(32).setCheckSight(false)
                .setTargetTypes(TARGET_TYPES.ALLY.id).setTargetCreatureType("demon"));
        this.goalSelector.addGoal(this.nextCombatGoalIndex, new EffectAuraGoal(this).setEffect(MobEffects.MOVEMENT_SPEED).setAmplifier(2).setEffectSeconds(2).setRange(32).setCheckSight(false)
                .setTargetTypes(TARGET_TYPES.ALLY.id).setTargetCreatureType("demon"));
        this.goalSelector.addGoal(this.nextCombatGoalIndex, new EffectAuraGoal(this).setEffect(MobEffects.DAMAGE_RESISTANCE).setAmplifier(2).setEffectSeconds(2).setRange(32).setCheckSight(false)
                .setTargetTypes(TARGET_TYPES.ALLY.id).setTargetCreatureType("demon"));
        this.goalSelector.addGoal(this.nextCombatGoalIndex, new SummonMinionsGoal(this).setMinionInfo("belphegor").setSummonCap(2)
                .setConditions(new GoalConditions().setRareVariantOnly(true)));
    }

	@Override
    public void tick() {
        super.tick();

        // Particles:
        if(this.getCommandSenderWorld().isClientSide) {
            for (int i = 0; i < 2; ++i) {
                this.getCommandSenderWorld().addParticle(ParticleTypes.SMOKE, this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
                this.getCommandSenderWorld().addParticle(ParticleTypes.FLAME, this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void attackRanged(Entity target, float range) {
        BaseProjectileEntity projectile = this.fireProjectile("doomfireball", target, range, 0, new Vector3d(0,0,0), 0.6f, 3f, 4F);
        if (projectile != null) {
            projectile.setPos(target.position().x, target.position().y + 4 + this.getRandom().nextDouble() * 3, target.position().z);
            projectile.shoot(0, -1, 0, 0.5F, 4);
        }
        super.attackRanged(target, range);
    }

    @Override
    public boolean canBurn() { return false; }

    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }

    public boolean petControlsEnabled() { return true; }
}
