package com.lycanitesmobs.core.entity.creature.demon;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

public class EntityVorach extends TameableCreatureEntity implements Enemy {

	public EntityVorach(EntityType<? extends EntityVorach> entityType, Level world) {
		super(entityType, world);
		this.attribute = MobType.UNDEAD;
		this.hasAttackSound = true;
		this.spreadFire = true;
		this.setupMob();

		this.setMaxUpStep(1.0F);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setSpeed(1.5D));
	}

	@Override
	public boolean attackMelee(Entity target, double damageScale) {
		if(!super.attackMelee(target, damageScale))
			return false;

		// Leech:
		float leeching = Math.max(1, this.getAttackDamage(damageScale) / 2);
		this.heal(leeching);

		return true;
	}

	@Override
	public float getAISpeedModifier() {
		if (this.hasAttackTarget() && this.distanceToSqr(this.getTarget()) >= 60) {
			if (this.isLookingAtMe(this.getTarget())) {
				return 0;
			}
			return super.getAISpeedModifier() * 5;
		}
		return super.getAISpeedModifier();
	}

	@Override
	public boolean canClimb() { return true; }

	@Override
	public boolean canBurn() { return false; }

	@Override
	public int getNoBagSize() { return 0; }
	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }

	@Override
	public float getFallResistance() {
		return 10;
	}
}
