package com.lycanitesmobs.core.entity.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CustomItemEntity extends ItemEntity {
    protected boolean canBurn = true;

    // ==================================================
    //                     Constructor
    // ==================================================
    public CustomItemEntity(Level world) {
        super(EntityType.ITEM, world);
    }

    public CustomItemEntity(Level world, double x, double y, double z, ItemStack itemStack) {
        super(world, x, y, z, itemStack);
    }


    // ==================================================
    //                   Taking Damage
    // ==================================================
    public boolean hurt(DamageSource damageSource, float damageAmount) {
        if (this.isInvulnerableTo(this.level().damageSources().inFire()) || !this.canBurn) {
            if (this.level().damageSources().onFire() == damageSource ||
                    this.level().damageSources().inFire() == damageSource) {
                return false;
            }
        }
        return super.hurt(damageSource, damageAmount);
    }

    public void setCanBurn(boolean canBurn) {
        this.canBurn = canBurn;
    }


    // ==================================================
    //                    Immunities
    // ==================================================
    // TODO Fire Immunity handled by EntityType


    // ==================================================
    //                  Network Flags
    // ==================================================
    protected void setSharedFlag(int flagID, boolean value) {
        if (flagID == 0 && this.isInvulnerableTo(this.level().damageSources().inFire()))
            value = false;
        super.setSharedFlag(flagID, value);
    }
}
