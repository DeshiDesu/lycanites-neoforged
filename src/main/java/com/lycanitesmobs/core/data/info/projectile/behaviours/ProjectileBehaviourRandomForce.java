package com.lycanitesmobs.core.data.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;

public class ProjectileBehaviourRandomForce extends ProjectileBehaviour {
	/** How much force to apply. **/
	public double force = 0.5D;

	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("force"))
			this.force = json.get("force").getAsDouble();
	}

	@Override
	public void onProjectileUpdate(BaseProjectileEntity projectile) {
		if(projectile.getCommandSenderWorld().isClientSide) {
			return;
		}

		if(projectile.updateTick % 5 == 0) {
			projectile.push(
					(0.5D - projectile.getCommandSenderWorld().getRandom().nextDouble()) * this.force,
					(0.5D - projectile.getCommandSenderWorld().getRandom().nextDouble()) * this.force,
					(0.5D - projectile.getCommandSenderWorld().getRandom().nextDouble()) * this.force
			);
		}
	}
}
