package com.lycanitesmobs.core.item.tool;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum CustomTiers implements Tier {
    HARVEST_0(0, 59, 2.0F, 0.0F, 15, Ingredient.EMPTY),
    HARVEST_1(1, 131, 4.0F, 1.0F, 5, Ingredient.EMPTY),
    HARVEST_2(2, 250, 6.0F, 2.0F, 14, Ingredient.EMPTY),
    HARVEST_3(3, 1561, 8.0F, 3.0F, 10, Ingredient.EMPTY),
    HARVEST_4(4, 3122, 10.0F, 4.0F, 18, Ingredient.EMPTY);

    private final int level;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final Ingredient repairIngredient;

    CustomTiers(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue, Ingredient repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.attackDamageBonus;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient;
    }
}
