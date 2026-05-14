package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.entity.IFusable;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendEntitiesGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendVillageGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

public class EntityAegis extends TameableCreatureEntity implements IFusable {

    public EntityAegis(EntityType<? extends EntityAegis> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;

        this.setupMob();
        this.setMaxUpStep(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));

        this.targetSelector.addGoal(this.nextSpecialTargetIndex++, new DefendVillageGoal(this));
        this.targetSelector.addGoal(this.nextSpecialTargetIndex++, new DefendEntitiesGoal(this, Villager.class));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.getCommandSenderWorld().isClientSide) {
            if (!this.hasAttackTarget() && this.currentBlockingTime < 2) {
                this.setBlocking();
            }
        }
    }

    @Override
    public boolean canBeTargetedBy(LivingEntity entity) {
        if (entity instanceof IronGolem || entity instanceof Villager) {
            return false;
        }
        return super.canBeTargetedBy(entity);
    }

    @Override
    public boolean shouldCreatureGroupHunt(LivingEntity target) {
        if (target instanceof TameableCreatureEntity && ((TameableCreatureEntity) target).isTamed()) {
            return false;
        }
        return super.shouldCreatureGroupHunt(target);
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public boolean canAttackWhileBlocking() {
        return false;
    }

    public boolean petControlsEnabled() {
        return true;
    }

    @Override
    public int getNoBagSize() {
        return 0;
    }

    @Override
    public int getBagSize() {
        return this.creatureInfo.bagSize;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.IN_WALL)) return true;
        return super.isInvulnerableTo(source);
    }

    /**
     * Called when this mob has received damage. Here a random blocking chance is applied.
     **/
    @Override
    public void onDamage(DamageSource damageSrc, float damage) {
        if (this.getRandom().nextDouble() > 0.75D && this.getHealth() / this.getMaxHealth() > 0.25F)
            this.setBlocking();
        if (damageSrc.getEntity() != null) {
            if (damageSrc.getEntity() instanceof Monster)
                damage *= 0.5F;
        }
        super.onDamage(damageSrc, damage);
    }

    @Override
    public void setBlocking() {
        this.currentBlockingTime = this.blockingTime + this.getRandom().nextInt(this.blockingTime / 2);
    }

    @Override
    public float getDamageModifier(DamageSource damageSrc) {
        if (!this.isBlocking()) {
            return 2.0F;
        }
        return super.getDamageModifier(damageSrc);
    }

    protected IFusable fusionTarget;

    @Override
    public IFusable getFusionTarget() {
        return this.fusionTarget;
    }

    @Override
    public void setFusionTarget(IFusable fusionTarget) {
        this.fusionTarget = fusionTarget;
    }

    @Override
    public EntityType<? extends LivingEntity> getFusionType(IFusable fusable) {
        if (fusable instanceof EntityCinder) {
            return CreatureManager.getInstance().getEntityType("wisp");
        }
        if (fusable instanceof EntityJengu) {
            return CreatureManager.getInstance().getEntityType("nymph");
        }
        if (fusable instanceof EntityGeonach) {
            return CreatureManager.getInstance().getEntityType("vapula");
        }
        if (fusable instanceof EntityZephyr) {
            return CreatureManager.getInstance().getEntityType("sylph");
        }
        if (fusable instanceof EntityArgus) {
            return CreatureManager.getInstance().getEntityType("spectre");
        }
        return null;
    }


}
