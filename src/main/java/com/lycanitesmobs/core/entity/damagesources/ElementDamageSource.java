package com.lycanitesmobs.core.entity.damagesources;

import com.lycanitesmobs.core.data.info.element.ElementInfo;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class ElementDamageSource extends DamageSource {
    private ElementInfo element;

    public ElementDamageSource(Holder<DamageType> damageTypeHolder, @Nullable Entity entity,ElementInfo element) {
        super(damageTypeHolder, entity);
        this.element = element;
    }

    @Override
    public String getMsgId() {
        return super.getMsgId();
    }

    /*public static ElementDamageSource causeElementDamage(Entity entity, ElementInfo element) {
        return new ElementDamageSource(entity, element);
    }*/

    /*public ElementDamageSource(Entity entity, ElementInfo element) {
		super("mob", entity);
        this.element = element;
	}*/

    public ElementInfo getElement() {
        return this.element;
    }
}
