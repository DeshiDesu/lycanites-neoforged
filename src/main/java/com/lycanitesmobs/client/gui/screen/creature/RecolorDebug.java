package com.lycanitesmobs.client.gui.screen.creature;

import com.lycanitesmobs.client.model.creature.base.ModelObjState;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;

public class RecolorDebug {
    public static BaseCreatureEntity debugEntity;
    public static boolean enabled = false;

    public static float baseR = 1.0F;
    public static float baseG = 1.0F;
    public static float baseB = 1.0F;
    public static float baseRange = 0.35F;

    public static float variantR = 0.0F;
    public static float variantG = 0.0F;
    public static float variantB = 1.0F;
    public static float variantAmount = 1.0F;

    public static void attachTo(BaseCreatureEntity entity) {
        debugEntity = entity;
        enabled = true;
    }

    public static void detach() {
        debugEntity = null;
        enabled = false;
    }

    public static void applyToState(ModelObjState state, BaseCreatureEntity entity) {
        if (!enabled || debugEntity == null || entity == null) return;
        if (entity != debugEntity) return;
        if (state == null) return;

        state.baseR = baseR;
        state.baseG = baseG;
        state.baseB = baseB;
        state.baseRange = baseRange;

        state.variantR = variantR;
        state.variantG = variantG;
        state.variantB = variantB;
        state.variantAmount = variantAmount;

        state.variantInit = true;
    }
}
