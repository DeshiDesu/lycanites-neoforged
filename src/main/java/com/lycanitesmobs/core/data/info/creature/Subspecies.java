package com.lycanitesmobs.core.data.info.creature;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.info.Variant;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.manager.ElementManager;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.entity.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.*;

public class Subspecies {
    /**
     * A map of creature variants available to this subspecies.
     **/
    public Map<Integer, Variant> variants = new HashMap<>();

    /**
     * The index of this subspecies in creature info. Set by creature info when added. Should never be 0 as that is used by the default and will result in this subspecies being ignored.
     **/
    public int index;

    /**
     * Higher priority Subspecies will always spawn in place of lower priority ones if they can spawn (conditions are met) regardless of weight. Only increase this above 0 if a subspecies has conditions otherwise they will stop standard/base Subspecies from showing up.
     **/
    public int priority = 0;

    /**
     * The name of this subspecies. Null for the default normal subspecies.
     **/
    public String name;

    /**
     * The class name of the model this subspecies should use, loaded client side only. If null, the default Creature model is used instead.
     **/
    @Nullable
    public String modelClassName;

    /**
     * The Elements of this subspecies, affects buffs and debuffs amongst other things.
     **/
    public List<ElementInfo> elements = new ArrayList<>();

    /**
     * A list of Spawn Conditions required for this subspecies to spawn.
     **/
    public List<SpawnCondition> spawnConditions = new ArrayList<>();

    /**
     * The offset relative to this subspecies width and height that riding entities should be offset by.
     **/
    public Vector3d mountOffset = new Vector3d(0.0D, 1.0D, 0.0D);


    public static Subspecies createFromJSON(CreatureInfo creatureInfo, JsonObject json) {
        // Name:
        String name = null;
        if (json.has("name")) {
            name = json.get("name").getAsString().toLowerCase();
        }

        // Create Subspecies:
        Subspecies subspecies = new Subspecies(name, json.get("index").getAsInt());

        // Create Variants:
        if (json.has("variants")) {
            for (JsonElement jsonElement : json.get("variants").getAsJsonArray()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Variant variant = Variant.createFromJSON(creatureInfo, jsonObject);
                if (variant != null) {
                    subspecies.variants.put(variant.index, variant);
                }
            }
        }

        // Model Class:
        if (json.has("modelClass")) {
            subspecies.modelClassName = json.get("modelClass").getAsString();
        }

        // Priority:
        if (json.has("priority")) {
            subspecies.priority = json.get("priority").getAsInt();
        }

        // Elements:
        List<String> elementNames = new ArrayList<>();
        if (json.has("element")) {
            elementNames.add(json.get("element").getAsString());
        }
        if (json.has("elements")) {
            elementNames = JSONHelper.getJsonStrings(json.get("elements").getAsJsonArray());
        }
        subspecies.elements.clear();
        for (String elementName : elementNames) {
            ElementInfo element = ElementManager.getInstance().getElement(elementName);
            if (element == null) {
                LMHelperClass.logWarningMessage("[Creature] Skipping unknown element '" + elementName + "' for subspecies '" + subspecies.name + "'. If you have updated the mod, clear your creature configs.");
                continue;
            }
            subspecies.elements.add(element);
        }

        // Conditions:
        if (json.has("conditions")) {
            JsonArray jsonArray = json.get("conditions").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
                SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
                if (spawnCondition != null) subspecies.spawnConditions.add(spawnCondition);
            }
        }

        // Mount Offset:
        subspecies.mountOffset = JSONHelper.getVector3d(json, "mountOffset", null);

        return subspecies;
    }


    /**
     * Constructor for creating a Subspecies.
     *
     * @param name  The skin of the Subspecies. Can be null for default subspecies.
     * @param index The index of the Subspecies. Should be 0 for the default subspecies.
     */
    public Subspecies(@Nullable String name, int index) {
        this.name = name;
        this.index = index;
    }


    /**
     * Loads this subspecies, used for adding new sounds, etc. Can only be done during startup.
     */
    public void load(CreatureInfo creatureInfo) {
        if (this.name != null && !creatureInfo.loadedSubspeciesSkins.contains(this.name)) {
            creatureInfo.addSounds("." + this.name);
            creatureInfo.loadedSubspeciesSkins.add(this.name);
        }
    }


    /**
     * Gets the display name of this Subspecies.
     *
     * @return The Subspecies title.
     */
    public Component getTitle() {
        if (this.name != null) {
            return Component.translatable("subspecies." + this.name);
        }
        return Component.literal("");
    }


    /**
     * Returns if this Subspecies is allowed to be used on the spawned entity.
     *
     * @return True if this Subspecies is allowed.
     */
    public boolean canSpawn(LivingEntity entityLiving) {
        if (entityLiving != null) {
            Level world = entityLiving.getCommandSenderWorld();

            // Check Conditions:
            for (SpawnCondition condition : this.spawnConditions) {
                if (!condition.isMet(world, null, new BlockPos(LMHelperClass.convertToVec3i(entityLiving.position())))) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Gets a random variant, normally used by a new mob when spawned.
     *
     * @param entity The entity that has this subspecies.
     * @param rare   If true, there will be much higher odds of a variant being picked.
     * @return A Variant or null if using the base variant.
     */
    public Variant getRandomVariant(LivingEntity entity, boolean rare) {
        LMHelperClass.logDebug("Subspecies", "~0===== Variant =====0~");
        LMHelperClass.logDebug("Subspecies", "Selecting random variant for: " + entity);
        if (rare) {
            LMHelperClass.logDebug("Subspecies", "The conditions have been set to rare increasing the chances of a variant being picked.");
        }
        if (this.variants.isEmpty()) {
            LMHelperClass.logDebug("Subspecies", "No variants available, will be base variant.");
            return null;
        }
        LMHelperClass.logDebug("Subspecies", "Variants Available: " + this.variants.size());

        // Get Weights:
        int baseSpeciesWeightScaled = Variant.BASE_WEIGHT;
        if (rare) {
            baseSpeciesWeightScaled = Math.round((float) baseSpeciesWeightScaled / 4);
        }
        int totalWeight = baseSpeciesWeightScaled;
        for (Variant variant : this.variants.values()) {
            totalWeight += variant.weight;
        }
        LMHelperClass.logDebug("Subspecies", "Total Weight: " + totalWeight);

        // Roll and Check Default:
        int roll = entity.getRandom().nextInt(totalWeight) + 1;
        LMHelperClass.logDebug("Subspecies", "Rolled: " + roll);
        if (roll <= baseSpeciesWeightScaled) {
            LMHelperClass.logDebug("Subspecies", "Base variant selected: " + baseSpeciesWeightScaled);
            return null;
        }

        // Get Random Subspecies:
        int checkWeight = baseSpeciesWeightScaled;
        for (Variant variant : this.variants.values()) {
            checkWeight += variant.weight;
            if (roll <= checkWeight) {
                LMHelperClass.logDebug("Subspecies", "Variant selected: " + variant.toString());
                return variant;
            }
        }

        LMHelperClass.logWarning("", "The roll was higher than the Total Weight, this shouldn't happen.");
        return null;
    }

    /**
     * Returns a variant for the provided index or null if invalid or index 0.
     *
     * @param index The index of the variant for this creature, 0 for default variant (null).
     * @return Creature variant.
     */
    @Nullable
    public Variant getVariant(int index) {
        if (!this.variants.containsKey(index)) {
            return null;
        }
        return this.variants.get(index);
    }

    /**
     * Used for when two mobs breed to randomly determine the variant of the child.
     *
     * @param entity         The entity that has this subspecies, currently only used to get RNG.
     * @param hostVariant    The variant of the host entity.
     * @param partnerVariant The variant of the partner entity.
     * @return The varaint the child should have, null for base variant.
     */
    public Variant getChildVariant(LivingEntity entity, @Nullable Variant hostVariant, @Nullable Variant partnerVariant) {
        int hostVariantIndex = (hostVariant != null ? hostVariant.index : 0);
        int partnerVariantIndex = (partnerVariant != null ? partnerVariant.index : 0);
        if (hostVariant == partnerVariant)
            return hostVariant;

        int hostWeight = (hostVariant != null ? hostVariant.weight : Variant.BASE_WEIGHT);
        int partnerWeight = (partnerVariant != null ? partnerVariant.weight : Variant.BASE_WEIGHT);
        int roll = entity.getRandom().nextInt(hostWeight + partnerWeight);
        if (roll > hostWeight)
            return partnerVariant;
        return hostVariant;
    }


    @Override
    public String toString() {
        return this.name != null ? this.name : "normal";
    }


    public static int getIndexFromOld(int oldIndex) {
        if (oldIndex > 3) {
            return 1;
        }
        return 0;
    }
}
