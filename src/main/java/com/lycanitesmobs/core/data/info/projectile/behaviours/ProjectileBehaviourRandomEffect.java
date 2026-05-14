package com.lycanitesmobs.core.data.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class ProjectileBehaviourRandomEffect extends ProjectileBehaviour {
    /**
     * A list of beneficial potion effects that this element can grant.
     **/
    public List<String> effects = new ArrayList<>();

    /**
     * The duration (in ticks) of the random effect.
     **/
    public int duration = 100;

    /**
     * The random effect amplifier.
     **/
    public int amplifier = 0;

    @Override
    public void loadFromJSON(JsonObject json) {
        if (json.has("effects"))
            this.effects = JSONHelper.getJsonStrings(json.get("effects").getAsJsonArray());
    }

    @Override
    public void onProjectileDamage(BaseProjectileEntity projectile, Level world, LivingEntity target, float damage) {
        if (projectile.getOwner() == null || damage <= 0) {
            return;
        }

        if (this.duration <= 0 || this.amplifier < 0) {
            return;
        }

        int randomIndex = world.random.nextInt(this.effects.size());
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(this.effects.get(randomIndex)));
        if (effect != null) {
            target.addEffect(new MobEffectInstance(effect, this.duration, this.amplifier));
        }
    }
}
