package com.lycanitesmobs.core.data.info.creature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.Variant;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.data.info.item.ItemDrop;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.ElementManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Contains various information about a creature from default spawn information to stats, etc.
 **/
public class CreatureInfo {
    /**
     * The creature type this creature belongs to.
     **/
    public CreatureType creatureType;
    /**
     * A list of groups that this creature is in.
     **/
    public List<CreatureGroup> groups = new ArrayList<>();
    /**
     * The entity class used by this creature.
     **/
    public Class<? extends BaseCreatureEntity> entityClass;
    /**
     * The constructor used by this creature to create entity instances.
     **/
    public Constructor<? extends BaseCreatureEntity> entityConstructor;
    /**
     * The class of the model this creature should use, loaded client side only.
     **/
    public String modelClassName;
    /**
     * The mod info of the mod this creature belongs to.
     **/
    public ModInfo modInfo;
    /**
     * The entity type used to store base attributes of this creature.
     **/
    public EntityType<? extends LivingEntity> entityType;
    /**
     * If false, this mob will be removed from the world if present and wont be allowed by any spawners.
     **/
    public boolean enabled = true;
    /**
     * If true, this is not a true mob, for example the fear entity. It will also not be register1ed to spawners and will not load assets, etc.
     **/
    public boolean dummy = false;
    /**
     * The Spawn Information for this creature.
     **/
    public CreatureSpawn creatureSpawn;
    // Stats:
    public double width = 0.8D;
    public double height = 1.8D;
    public int experience = 5;
    public double health = 20.0D;
    public double defense = 0.0D;
    public double armor = 0.0D;
    public double speed = 24.0D; // Divided by 100 when applied.
    public double damage = 2.0D;
    public double attackSpeed = 1.0D; // Seconds per melee.
    public double rangedSpeed = 0.5D; // Seconds per ranged.
    public double effectDuration = 1.0D; // Seconds of effect.
    public double effectAmplifier = -1.0D; // No effect when less than 0.
    public double pierce = 1.0D;
    public double sight = 16.0D;
    public double knockbackResistance = 0.0D;
    public int bagSize = 5;
    public int packSize = 3;


    // Spawn Egg:
    /**
     * The background color of this mob's egg. Required.
     **/
    public int eggBackColor;

    /**
     * The foreground color of this mob's egg. Required.
     **/
    public int eggForeColor;


    // Creature Type:
    /**
     * If true, this creature is a boss creature and should use special boss features such as boss health bars and dps taken limits, etc.
     **/
    public boolean boss = false;

    /**
     * The Subspecies that this creature can use.
     **/
    public Map<Integer, Subspecies> subspecies = new HashMap<>();

    /**
     * A list of subspecies skins that have been loaded, used to prevent them loading assets multiple times per color variation.
     **/
    public List<String> loadedSubspeciesSkins = new ArrayList<>();

    /**
     * The Elements of this creature, affects buffs and debuffs amongst other things.
     **/
    public List<ElementInfo> elements = new ArrayList<>();

    /**
     * The diets that this creature has, this controls what food it can eat for being healed, breeding, etc. Diets will search for a tag group lycanitesmobs:diet_dietname.json for items. Creature Types will also provide what diet they are suited for.
     **/
    public List<String> diets = new ArrayList<>();


    // Creature Difficulty:
    /**
     * If true, this mob is allowed on Peaceful Difficulty.
     **/
    public boolean peaceful = false;

    /**
     * If true, this mob can be farmed (bred and lured using food items). The entity must have age AI for this.
     **/
    public boolean farmable = false;

    /**
     * If true, this mob can be summoned as a minion. The entity must have pet AI for this.
     **/
    public boolean summonable = false;

    /**
     * If true, this mob can be tamed as a pet. The entity must have pet AI and a treat item set for this.
     **/
    public boolean tameable = false;

    /**
     * If true, this mob can be used as a mount. The entity must have mount AI for this.
     **/
    public boolean mountable = false;

    /**
     * If true, this mob can perch on it's owner's shoulder. The entity must have pet AI for this.
     **/
    public boolean perchable = false;

    /**
     * How many charges this creature normally costs to summon.
     **/
    public int summonCost = 1;

    /**
     * The Dungeon Level of this mob, for Lycanites Dungeons this affects what floor the mob appears on, but this is also used by other mods such as Doomlike Dungeons to assess difficulty. Default: -1 (All levels).
     **/
    public int dungeonLevel = -1;

    /**
     * The range (in blocks) that players must be to be considered within range of this entity as a boss, used for block place/break boss protection.
     **/
    public int bossNearbyRange = 60;


    // Items:
    /**
     * A list of all the item drops available to this creature.
     **/
    public List<ItemDrop> drops = new ArrayList<>();
    /**
     * A custom scale to apply to the mob's size.
     **/
    public double sizeScale = 1;


    // Scale:
    /**
     * A custom scale to apply to the mob's hitbox.
     **/
    public double hitboxScale = 1;
    /**
     * The offset relative to this creatures width and height that riding entities should be offset by.
     **/
    public Vector3d mountOffset = new Vector3d(0.0D, 1.0D, 0.0D);
    /**
     * The name of this mob. Lowercase, no space, used for language entries and for generating the entity id, etc. Required.
     **/
    protected String name;


    // Flags:
    protected int tamingReputation = 500;
    /**
     * A json array containing a list of drops to be loaded during init.
     **/
    protected JsonArray dropsJson;
    /**
     * A list of boolean flags set for this creature.
     **/
    protected Map<String, Boolean> boolFlags = new HashMap<>();
    /**
     * A list of double flags set for this creature.
     **/
    protected Map<String, Double> doubleFlags = new HashMap<>();
    /**
     * A list of string flags set for this creature.
     **/
    protected Map<String, String> stringFlags = new HashMap<>();

    /**
     * Constructor
     *
     * @param modInfo The mod that this creature definition will belong to.
     */
    public CreatureInfo(ModInfo modInfo) {
        this.modInfo = modInfo;
        this.creatureSpawn = new CreatureSpawn();
    }

    /**
     * Loads this creature from a JSON object.
     * @return true if loaded successfully, false if this entry should be skipped.
     **/
    public boolean loadFromJson(JsonObject json) {
        this.name = json.get("name").getAsString();

        // Entity Class:
        try {
            this.entityClass = (Class<? extends BaseCreatureEntity>) Class.forName(json.get("entityClass").getAsString());
            this.entityConstructor = this.entityClass.getConstructor(EntityType.class, Level.class);
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("[Creature] Skipping creature '" + this.getName() + "': unable to find entity class '" + json.get("entityClass").getAsString() + "'. If you have updated the mod, clear your creature configs at: config/lycanitesmobs/creatures/" + this.getName() + ".json");
            return false;
        }

        // Enabled:
        if (json.has("enabled"))
            this.enabled = json.get("enabled").getAsBoolean();
        if (json.has("dummy"))
            this.dummy = json.get("dummy").getAsBoolean();
        if (this.dummy)
            return true;

        // Model Class:
        this.modelClassName = json.get("modelClass").getAsString();

        // Get Creature Type:
        if (json.has("creatureType")) {
            this.creatureType = CreatureManager.getInstance().getCreatureType(json.get("creatureType").getAsString());
            if (this.creatureType == null) {
                LMHelperClass.logWarning("", "Unable to find the creature type: " + json.get("creatureType").getAsString() + " for Creature: " + this.name);
            }
        }
        if (this.creatureType == null) {
            this.creatureType = CreatureManager.getInstance().getCreatureType("beast");
        }

        // Creature Group:
        if (json.has("groups")) {
            for (String groupName : JSONHelper.getJsonStrings(json.getAsJsonArray("groups"))) {
                CreatureGroup group = CreatureManager.getInstance().getCreatureGroup(groupName);
                if (group != null) {
                    this.groups.add(group);
                    group.addCreature(this);
                } else {
                    LMHelperClass.logWarning("", "[Creature] Unable to find the Creature Group: " + groupName + " for Creature: " + this.name);
                }
            }
        }

        // Spawning:
        this.creatureSpawn.loadFromJSON(this, json.get("spawning").getAsJsonObject());

        // Size:
        if (json.has("width"))
            this.width = json.get("width").getAsDouble();
        if (json.has("height"))
            this.height = json.get("height").getAsDouble();
        if (json.has("sizeScale"))
            this.sizeScale = json.get("sizeScale").getAsDouble();
        if (json.has("hitboxScale"))
            this.hitboxScale = json.get("hitboxScale").getAsDouble();
        this.mountOffset = JSONHelper.getVector3d(json, "mountOffset", this.mountOffset);

        // Stats:
        if (json.has("experience"))
            this.experience = json.get("experience").getAsInt();
        if (json.has("health"))
            this.health = json.get("health").getAsDouble();
        if (json.has("defense"))
            this.defense = json.get("defense").getAsDouble();
        if (json.has("armor"))
            this.armor = json.get("armor").getAsDouble();
        if (json.has("speed"))
            this.speed = json.get("speed").getAsDouble();
        if (json.has("damage"))
            this.damage = json.get("damage").getAsDouble();
        if (json.has("attackSpeed"))
            this.attackSpeed = json.get("attackSpeed").getAsDouble();
        if (json.has("rangedSpeed"))
            this.rangedSpeed = json.get("rangedSpeed").getAsDouble();
        if (json.has("effectDuration"))
            this.effectDuration = json.get("effectDuration").getAsDouble();
        if (json.has("effectAmplifier"))
            this.effectAmplifier = json.get("effectAmplifier").getAsDouble();
        if (json.has("pierce"))
            this.pierce = json.get("pierce").getAsDouble();

        if (json.has("knockbackResistance"))
            this.knockbackResistance = json.get("knockbackResistance").getAsDouble();
        if (json.has("sight"))
            this.sight = json.get("sight").getAsDouble();

        if (json.has("packSize"))
            this.packSize = json.get("packSize").getAsInt();
        if (json.has("tamingReputation"))
            this.tamingReputation = json.get("tamingReputation").getAsInt();

        if (json.has("bagSize"))
            this.bagSize = json.get("bagSize").getAsInt();

        // Spawn Egg:
        this.eggBackColor = Color.decode(json.get("eggBackColor").getAsString()).getRGB();
        this.eggForeColor = Color.decode(json.get("eggForeColor").getAsString()).getRGB();

        // Subspecies:
        for (JsonElement jsonElement : json.get("subspecies").getAsJsonArray()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Subspecies subspecies = Subspecies.createFromJSON(this, jsonObject);
            this.subspecies.put(subspecies.index, subspecies);
        }
        if (this.subspecies.isEmpty()) {
            LMHelperClass.logWarningMessage("[Creature] Skipping creature '" + this.getName() + "': no subspecies found (there should always be at least 1 default subspecies). Clear your creature configs at: config/lycanitesmobs/creatures/" + this.getName() + ".json");
            return false;
        }

        // Elements:
        List<String> elementNames = new ArrayList<>();
        if (json.has("element")) {
            elementNames.add(json.get("element").getAsString());
        }
        if (json.has("elements")) {
            elementNames = JSONHelper.getJsonStrings(json.get("elements").getAsJsonArray());
        }
        this.elements.clear();
        for (String elementName : elementNames) {
            ElementInfo element = ElementManager.getInstance().getElement(elementName);
            if (element == null) {
                LMHelperClass.logWarningMessage("[Creature] Skipping unknown element '" + elementName + "' for creature '" + this.getName() + "'. Clear your creature configs at: config/lycanitesmobs/creatures/" + this.getName() + ".json");
                continue;
            }
            this.elements.add(element);
        }

        // Diets:
        if (json.has("diets"))
            this.diets = JSONHelper.getJsonStrings(json.get("diets").getAsJsonArray());

        // Flags:
        if (json.has("boss"))
            this.boss = json.get("boss").getAsBoolean();
        if (json.has("peaceful"))
            this.peaceful = json.get("peaceful").getAsBoolean();

        // Features:
        if (json.has("farmable"))
            this.farmable = json.get("farmable").getAsBoolean();
        if (json.has("summonable"))
            this.summonable = json.get("summonable").getAsBoolean();
        if (json.has("tameable"))
            this.tameable = json.get("tameable").getAsBoolean();
        if (json.has("mountable"))
            this.mountable = json.get("mountable").getAsBoolean();
        if (json.has("perchable"))
            this.perchable = json.get("perchable").getAsBoolean();
        if (json.has("summonCost"))
            this.summonCost = json.get("summonCost").getAsInt();
        if (json.has("dungeonLevel"))
            this.dungeonLevel = json.get("dungeonLevel").getAsInt();
        if (json.has("bossNearbyRange"))
            this.bossNearbyRange = json.get("bossNearbyRange").getAsInt();

        // Item Drops:
        if (json.has("drops")) {
            for (JsonElement mobDropJson : json.getAsJsonArray("drops")) {
                ItemDrop itemDrop = ItemDrop.createFromJSON(mobDropJson.getAsJsonObject());
                if (itemDrop != null) {
                    this.drops.add(itemDrop);
                } else {
                    LMHelperClass.logWarning("", "[Creature] Unable to add item drop to creature: " + getName() + ".");
                }
            }
        }

        // Flags:
        if (json.has("flags")) {
            for (JsonElement flagJson : json.getAsJsonArray("flags")) {
                JsonObject flagJsonObject = flagJson.getAsJsonObject();
                if (!flagJsonObject.has("name") || !flagJsonObject.has("value")) {
                    LMHelperClass.logWarning("", "Invalid creature json flag, make sure that the flag has both a name and value, skipping this flag.");
                    continue;
                }
                try {
                    this.addFlag(flagJsonObject.get("name").getAsString(), flagJsonObject.get("value").getAsBoolean());
                } catch (Exception ignored) {
                }
                try {
                    this.addFlag(flagJsonObject.get("name").getAsString(), flagJsonObject.get("value").getAsDouble());
                } catch (Exception ignored) {
                }
                try {
                    this.addFlag(flagJsonObject.get("name").getAsString(), flagJsonObject.get("value").getAsString());
                } catch (Exception ignored) {
                }
            }
        }

        // Add To Creature Type:
        if (this.creatureType != null) {
            this.creatureType.addCreature(this);
        }
        return true;
    }

    /**
     * Loads this creature (should only be called during startup), generates sounds, achievement stats, etc.
     */
    public void init() {
        // Skip Dummies:
        if (this.dummy) {
            return;
        }

		/*/ Add Trophy: TODO Creature Trophies
		ItemStack advancementStack = new ItemStack(ObjectManager.getItem("mobtoken"));
		achievementStack.setTagInfo("Mob", new StringNBT(this.getName()));*/

        // Add Sounds:
        this.addSounds("");

        // Init Subspecies:
        for (Subspecies subspecies : this.subspecies.values()) {
            subspecies.load(this);
        }

        // Vanilla Spawning:
        this.creatureSpawn.registerVanillaSpawns(this);

        LMHelperClass.logDebug("Creature", "Creature Loaded: " + this.getName() + " - " + this.entityClass + " (" + this.modInfo.name + ")");
    }

    /**
     * Adds sounds that this creature uses.
     */
    public void addSounds(String suffix) {
        ObjectManager.addSound(this.getName() + suffix + "_say", "entity." + getName() + suffix + ".say");
        ObjectManager.addSound(this.name + suffix + "_hurt", "entity." + getName() + suffix + ".hurt");
        ObjectManager.addSound(this.name + suffix + "_death", "entity." + getName() + suffix + ".death");
        ObjectManager.addSound(this.name + suffix + "_step", "entity." + getName() + suffix + ".step");
        ObjectManager.addSound(this.name + suffix + "_attack", "entity." + getName() + suffix + ".attack");
        ObjectManager.addSound(this.name + suffix + "_jump", "entity." + getName() + suffix + ".jump");
        ObjectManager.addSound(this.name + suffix + "_fly", "entity." + getName() + suffix + ".fly");
        if (this.isSummonable() || this.isTameable() || TameableCreatureEntity.class.isAssignableFrom(this.entityClass)) {
            ObjectManager.addSound(this.name + suffix + "_tame", "entity." + getName() + suffix + ".tame");
            ObjectManager.addSound(this.name + suffix + "_beg", "entity." + getName() + suffix + ".beg");
        }
        if (this.isTameable())
            ObjectManager.addSound(this.name + suffix + "_eat", "entity." + getName() + suffix + ".eat");
        if (this.isMountable())
            ObjectManager.addSound(this.name + suffix + "_mount", "entity." + getName() + suffix + ".mount");
        if (this.isBoss())
            ObjectManager.addSound(this.name + suffix + "_phase", "entity." + getName() + suffix + ".phase");
    }

    /**
     * Returns the name of this creature, this is the unformatted lowercase name. Ex: lurker
     *
     * @return Creature name.
     */
    public String getName() {
        return this.name.toLowerCase();
    }

    /**
     * Returns the registry id of this creature. Ex: lycanitesmobs:lurker
     *
     * @return Creature registry entity id.
     */
    public String getEntityId() {
        return this.modInfo.modid + ":" + this.getName();
    }

    /**
     * Returns the groups that this creature is in.
     *
     * @return Creature name.
     */
    public List<CreatureGroup> getGroups() {
        return this.groups;
    }

    /**
     * Returns the entity type of this creature. Prefers the cached entityType field (set during loadValues)
     * over a fresh registry lookup to avoid returning EntityType.PIG (the registry default) when the
     * entity type hasn't been registered yet or the registry lookup fails.
     *
     * @return Creature's entity type.
     */
    @Nullable
    public EntityType<? extends LivingEntity> getEntityType() {
        if (this.entityType != null) {
            return this.entityType;
        }
        ResourceLocation id = new ResourceLocation(LycanitesMobs.MODID, this.getName());
        EntityType<?> t = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (t != null && id.equals(ForgeRegistries.ENTITY_TYPES.getKey(t))) {
            return (EntityType<? extends LivingEntity>) t;
        }
        LMHelperClass.logWarning("", "[Creature] getEntityType() registry lookup failed for: " + id + " (returned " + t + "). Entity type may not be registered yet.");
        return null;
    }


    /**
     * Returns the resource location for this creature.
     *
     * @return Creature resource location.
     */
    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(this.modInfo.modid, this.getName());
    }


    /**
     * Returns the language key for this creature. Ex: swampmobs.lurker
     *
     * @return Creature language key.
     */
    public String getLocalisationKey() {
        return this.modInfo.modid + "." + this.getName();
    }


    /**
     * Returns a translated title for this creature. Ex: Lurker
     *
     * @return The display name of this creature.
     */
    public Component getTitle() {
        return Component.translatable("entity." + this.getLocalisationKey());
    }


    /**
     * Returns a translated description of this creature.
     *
     * @return The creature description.
     */
    public MutableComponent getDescription() {
        return Component.translatable("entity." + this.getLocalisationKey() + ".description");
    }


    /**
     * Returns a translated description of this creature.
     *
     * @return The creature description.
     */
    public MutableComponent getHabitatDescription() {
        return Component.translatable("entity." + this.getLocalisationKey() + ".habitat");
    }


    /**
     * Returns a translated description of this creature.
     *
     * @return The creature description.
     */
    public MutableComponent getCombatDescription() {
        return Component.translatable("entity." + this.getLocalisationKey() + ".combat");
    }


    /**
     * Returns a comma separated list of Elements used by this Creature.
     *
     * @return The Elements used by this Creature.
     */
    public MutableComponent getElementNames(Subspecies subspecies) {
        List<ElementInfo> elements = this.elements;
        if (subspecies != null && !subspecies.elements.isEmpty()) {
            elements = subspecies.elements;
        }
        if (elements.isEmpty()) {
            return Component.translatable("common.none");
        }
        MutableComponent elementNames = Component.literal("");
        boolean firstElement = true;
        for (ElementInfo element : elements) {
            if (!firstElement) {
                elementNames.append(", ");
            }
            firstElement = false;
            elementNames.append(element.getTitle());
        }
        return elementNames;
    }


    /**
     * Returns a comma separated list of Diets used by this Creature.
     *
     * @return The Diets used by this Creature.
     */
    public MutableComponent getDietNames() {
        if (this.diets.isEmpty()) {
            return Component.translatable("common.none");
        }
        MutableComponent dietNames = Component.literal("");
        boolean firstDiet = true;
        for (String diet : this.diets) {
            if (!firstDiet) {
                dietNames.append(", ");
            }
            firstDiet = false;
            dietNames.append(Component.translatable("diet." + diet));
        }
        return dietNames;
    }


    /**
     * Returns a comma separated list of Biomes native for this Creature.
     *
     * @return The Biomes native for this Creature.
     */
    public MutableComponent getBiomeNames() {
        List<String> biomeIds = new ArrayList<>();
        if (this.creatureSpawn.biomesFromTags != null)
            biomeIds.addAll(this.creatureSpawn.biomesFromTags);
        if (this.creatureSpawn.biomeIds != null)
            biomeIds.addAll(this.creatureSpawn.biomeIds);
        if (biomeIds.isEmpty()) {
            return Component.translatable("gui.beastiary.biomes.none");
        }
        MutableComponent biomeNames = Component.literal("");
        boolean firstBiome = true;
        for (String biomeId : biomeIds) {
            if (!firstBiome) {
                biomeNames.append(", ");
            }
            firstBiome = false;
            biomeNames.append(Component.literal(biomeId)); // TODO Figure out how to get biome display names now.
        }
        return biomeNames;
    }


    /**
     * Returns a comma separated list of items dropped by this Creature.
     *
     * @return The items dropped by this Creature.
     */
    public MutableComponent getDropNames() {
        if (this.drops.isEmpty()) {
            return Component.literal("");
        }
        MutableComponent dropNames = Component.literal("");
        boolean firstDrop = true;
        for (ItemDrop drop : this.drops) {
            if (!firstDrop) {
                dropNames.append("\n");
            }
            firstDrop = false;
            dropNames.append(drop.getItemStack().getHoverName());
            dropNames.append(" (");

            if (drop.maxAmount > drop.minAmount)
                dropNames.append(drop.minAmount + "-" + drop.maxAmount + "X");
            else
                dropNames.append(drop.minAmount + "X");

            dropNames.append(" " + (drop.chance * 100) + "%");

            Subspecies subspecies = this.getSubspecies(0);
            if (drop.subspeciesIndex > 0) {
                subspecies = this.getSubspecies(drop.subspeciesIndex);
                if (subspecies.name != null) {
                    dropNames.append(" ");
                    dropNames.append(subspecies.getTitle());
                }
            }

            if (drop.variantIndex >= 0) {
                dropNames.append(" ");
                Variant variant = subspecies.getVariant(drop.variantIndex);
                if (variant == null) {
                    dropNames.append(Component.translatable("subspecies.normal"));
                } else {
                    dropNames.append(variant.getTitle());
                }
            }

            dropNames.append(")");
        }
        return dropNames;
    }


    /**
     * Returns the resource location for the GUI icon of this creature.
     *
     * @return Creature icon resource location.
     */
    public ResourceLocation getIcon() {
        ResourceLocation texture = TextureManager.getTexture(this.getName() + "_icon");
        if (texture == null) {
            TextureManager.addTexture(this.getName() + "_icon", "textures/guis/creatures/" + this.getName() + "_icon.png");
            texture = TextureManager.getTexture(this.getName() + "_icon");
        }
        return texture;
    }


    /**
     * Returns if this creature is farmable.
     *
     * @return True if creature is farmable.
     */
    public boolean isFarmable() {
        return this.farmable && AgeableCreatureEntity.class.isAssignableFrom(this.entityClass);
    }


    /**
     * Returns if this creature is summonable.
     *
     * @return True if creature is summonable.
     */
    public boolean isSummonable() {
        return this.summonable && TameableCreatureEntity.class.isAssignableFrom(this.entityClass);
    }


    /**
     * Returns if this creature is tameable.
     *
     * @return True if creature is tameable.
     */
    public boolean isTameable() {
        return this.tameable && TameableCreatureEntity.class.isAssignableFrom(this.entityClass);
    }


    /**
     * Returns if this creature is mountable.
     *
     * @return True if creature is mountable.
     */
    public boolean isMountable() {
        return this.mountable && RideableCreatureEntity.class.isAssignableFrom(this.entityClass);
    }


    /**
     * Returns if this creature is perchable.
     *
     * @return True if creature is perchable.
     */
    public boolean isPerchable() {
        return this.perchable;
    }


    /**
     * Returns if this creature is a boss.
     *
     * @return True if creature is a boss.
     */
    public boolean isBoss() {
        return this.boss;
    }


    /**
     * Returns a subspecies for the provided index or the first subspecies if invalid.
     *
     * @param index The index of the subspecies for this creature.
     * @return Creature subspecies.
     */
    public Subspecies getSubspecies(int index) {
        if (!this.subspecies.containsKey(index)) {
            return this.subspecies.get(0);
        }
        return this.subspecies.get(index);
    }


    /**
     * Gets a random subspecies, normally used by a new mob when spawned.
     *
     * @param entity The entity that has this subspecies.
     * @return The Subspecies to use.
     */
    public Subspecies getRandomSubspecies(LivingEntity entity) {
        LMHelperClass.logDebug("Subspecies", "~0===== Subspecies =====0~");
        LMHelperClass.logDebug("Subspecies", "Selecting subspecies for: " + entity);
        LMHelperClass.logDebug("Subspecies", "Subspecies Available: " + this.subspecies.size());

        // Get Viable Subspecies:
        List<Subspecies> possibleSubspecies = new ArrayList<>();
        int highestPriority = 0;
        for (Subspecies subspeciesEntry : this.subspecies.values()) {
            if (subspeciesEntry.canSpawn(entity)) {
                possibleSubspecies.add(subspeciesEntry);
                if (subspeciesEntry.priority > highestPriority) {
                    highestPriority = subspeciesEntry.priority;
                }
            }
        }
        if (possibleSubspecies.isEmpty()) {
            LMHelperClass.logWarning("", "[Subspecies] No subspecies allowed for " + this.getName() + ", there should always be a default subspecies, returning the first subspecies found for now.");
            return this.subspecies.get(0);
        }

        // Filter Priorities:
        if (highestPriority > 0) {
            for (Subspecies subspeciesEntry : possibleSubspecies.toArray(new Subspecies[possibleSubspecies.size()])) {
                if (subspeciesEntry.priority < highestPriority) {
                    possibleSubspecies.remove(subspeciesEntry);
                }
            }
        }
        LMHelperClass.logDebug("Subspecies", "Subspecies Allowed: " + possibleSubspecies.size() + " Highest Priority: " + highestPriority);

        // Only One Viable:
        if (possibleSubspecies.size() == 1) {
            return possibleSubspecies.get(0);
        }

        // Get Random Subspecies:
        int randomIndex = entity.getRandom().nextInt(possibleSubspecies.size());
        return possibleSubspecies.get(randomIndex);
    }

    /**
     * Returns the amount of Relationship Reputation required to become friendly.
     *
     * @return The reputation required to be friendly.
     */
    public int getFriendlyReputation() {
        return Math.round((float) this.tamingReputation / 2);
    }

    /**
     * Returns the amount of Relationship Reputation required to trigger a tame.
     *
     * @return The reputation required to tame.
     */
    public int getTamingReputation() {
        return this.tamingReputation;
    }

    /**
     * Returns if this creature can eat the provided item as food for healing, breeding, etc.
     *
     * @param itemStack The item to eat.
     * @return True if the item can be eaten.
     */
    public boolean canEat(ItemStack itemStack) {
        if (this.diets.isEmpty()) {
            return false;
        }
        for (String diet : this.diets) {
            ResourceLocation dietTagId = new ResourceLocation(LycanitesMobs.MODID, "diet_" + diet);
            TagKey<Item> dietTag = TagKey.create(Registries.ITEM, dietTagId);
            if (dietTag == null) {
                LMHelperClass.logWarning("", "[Creature] Cannot find diet: " + dietTagId);
                return false;
            }
            if (itemStack.is(dietTag)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Creates a new Entity instance from this creature info. Returns null on failure.
     *
     * @param world The world to create the entity in.
     * @return The created entity.
     */
    public LivingEntity createEntity(Level world) {
        try {
            if (this.entityClass == null)
                return null;
            EntityType<? extends LivingEntity> type = this.getEntityType();
            if (this.entityType == null) {
                LMHelperClass.logWarning("", "[Creature] createEntity() called for " + this.getName()
                        + " but entityType field is null (loadValues may not have run or entity registration failed). "
                        + "Registry lookup returned: " + type);
            }
            return this.entityConstructor.newInstance(type, world);
        } catch (Exception e) {
            LMHelperClass.logWarning("", "[Creature] Failed to create entity for " + this.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a new boolean flag to this creature.
     *
     * @param flagName  The name of the flag.
     * @param flagValue The value of the flag.
     */
    public void addFlag(String flagName, boolean flagValue) {
        this.boolFlags.put(flagName, flagValue);
    }

    /**
     * Adds a new double flag to this creature.
     *
     * @param flagName  The name of the flag.
     * @param flagValue The value of the flag.
     */
    public void addFlag(String flagName, double flagValue) {
        this.doubleFlags.put(flagName, flagValue);
    }

    /**
     * Adds a new string flag to this creature.
     *
     * @param flagName  The name of the flag.
     * @param flagValue The value of the flag.
     */
    public void addFlag(String flagName, String flagValue) {
        this.stringFlags.put(flagName, flagValue);
    }

    /**
     * Returns a boolean flag that this creature has.
     *
     * @param flagName     The name of the flag to get.
     * @param defaultValue The value to return if the flag is missing.
     * @return The flag value.
     */
    public boolean getFlag(String flagName, boolean defaultValue) {
        if (!this.boolFlags.containsKey(flagName)) {
            return defaultValue;
        }
        return this.boolFlags.get(flagName);
    }

    /**
     * Returns an int flag that this creature has rounded into an integer.
     *
     * @param flagName     The name of the flag to get.
     * @param defaultValue The value to return if the flag is missing.
     * @return The flag value.
     */
    public int getFlag(String flagName, int defaultValue) {
        if (!this.doubleFlags.containsKey(flagName)) {
            return defaultValue;
        }
        return Math.round(this.doubleFlags.get(flagName).floatValue());
    }

    /**
     * Returns a double flag that this creature has.
     *
     * @param flagName     The name of the flag to get.
     * @param defaultValue The value to return if the flag is missing.
     * @return The flag value.
     */
    public double getFlag(String flagName, double defaultValue) {
        if (!this.doubleFlags.containsKey(flagName)) {
            return defaultValue;
        }
        return this.doubleFlags.get(flagName);
    }

    /**
     * Returns a string flag that this creature has.
     *
     * @param flagName     The name of the flag to get.
     * @param defaultValue The value to return if the flag is missing.
     * @return The flag value.
     */
    public String getFlag(String flagName, String defaultValue) {
        if (!this.stringFlags.containsKey(flagName)) {
            return defaultValue;
        }
        return this.stringFlags.get(flagName);
    }
}
