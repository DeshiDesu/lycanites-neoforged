package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;


public class FaceTargetGoal extends Goal {
	private BaseCreatureEntity host;

	private LivingEntity target;

	/**
	 * Constrcutor
	 * @param setHost The creature using this goal.
	 */
	public FaceTargetGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
    }

	@Override
    public boolean canUse() {
		if(!this.host.isAlive()) {
			return false;
		}
		this.target = this.host.getTarget();
		return this.target != null;
    }

	@Override
    public void tick() {
		this.host.getLookControl().setLookAt(this.target, 10.0F, this.host.getMaxHeadXRot());
    }
}
