package com.lycanitesmobs.core.entity.damagesources;


import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class MinionEntityDamageSource extends DamageSource {
    protected DamageSource minionDamageSource;
    protected final Entity minionOwner;

    public MinionEntityDamageSource(Holder<DamageType> damageTypeHolder, @Nullable Entity owner) {
        super(damageTypeHolder, owner);
        this.minionDamageSource = minionDamageSource;
        this.minionOwner = owner;
    }

	/*public MinionEntityDamageSource(DamageSource minionDamageSource, Entity owner) {
		super(minionDamageSource.getMsgId(), minionDamageSource.getEntity());
        this.minionDamageSource = minionDamageSource;
        this.minionOwner = owner;
	}*/

    // This Entity Caused The Damage:
    @Override
    public Entity getDirectEntity() {
        return this.getEntity();
    }

    // This Entity Gets Credit for The Kill:
    @Override
    public Entity getEntity() {
        return this.minionOwner;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity slainEntity) {
        return this.minionDamageSource.getLocalizedDeathMessage(slainEntity);
    }
}
