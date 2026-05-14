package com.lycanitesmobs.client.model.creature.base;


import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class ModelObjState {
    public Entity entity;

    public float baseR = 1.0F;
    public float baseG = 1.0F;
    public float baseB = 1.0F;
    public float baseRange = 0.1F;

    public float variantR = 1.0F;
    public float variantG = 1.0F;
    public float variantB = 1.0F;
    public float variantAmount = 0.0F;
    public boolean variantInit = false;

    public float attackAnimationProgress = 0.0F;
    public boolean attackAnimationIncreasing = false;
    public boolean attackAnimationPlaying = false;
    public float attackAnimationSpeed = 1F / 10;

    protected Map<String, Boolean> additionalBooleans = new HashMap<>();
    protected Map<String, Float> additionalFloats = new HashMap<>();

    public ModelObjState(Entity entity) {
        this.entity = entity;
    }

    public void setBoolean(String key, boolean value) {
        this.additionalBooleans.put(key, value);
    }

    public boolean getBoolean(String key) {
        if (this.additionalBooleans.containsKey(key))
            return this.additionalBooleans.get(key);
        return false;
    }

    public void setFloat(String key, float value) {
        this.additionalFloats.put(key, value);
    }

    public float getFloat(String key) {
        if (this.additionalFloats.containsKey(key))
            return this.additionalFloats.get(key);
        return 0;
    }
}
