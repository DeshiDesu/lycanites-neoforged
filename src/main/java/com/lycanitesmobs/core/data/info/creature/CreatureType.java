package com.lycanitesmobs.core.data.info.creature;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.item.consumable.entity.ItemCustomSpawnEgg;
import com.lycanitesmobs.core.item.equipment.CreatureSaddleItem;
import com.lycanitesmobs.core.item.consumable.entity.CreatureTreatItem;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulstone;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulstoneFilled;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lycanitesmobs.core.tabs.LMCreaturesGroup.creatureNames;

public class CreatureType {

    // Core Info:
    /**
     * The name of this creature type. Lowercase, no space, used for language entries and for generating the entity id, etc. Required.
     **/
    protected String name;

    /**
     * The mod info of the mod this creature type belongs to.
     **/
    public ModInfo modInfo;

    /**
     * A map of all creatures of this type by name.
     **/
    public Map<String, CreatureInfo> creatures = new HashMap<>();

    /**
     * A list of all creatures of this type that can be tamed.
     **/
    public List<CreatureInfo> tameableCreatures = new ArrayList<>();

    /**
     * The treat item this type uses.
     **/
    public Lazy<Item> treat;

    /**
     * The saddle item this type uses.
     **/
    public Lazy<Item> saddle;

    /**
     * The soulstone item this type uses.
     **/
    public Lazy<ItemSoulstone> soulstone;

    /**
     * The spawn egg item this type uses.
     **/
    public Lazy<Item> spawnEgg;

    /**
     * The diet type this Creature Type provides as food for predators. "none" by default.
     **/
    public String dietProvided = "none";


    /**
     * Constructor
     *
     * @param group The group that this creature definition will belong to.
     */
    public CreatureType(ModInfo group) {
        this.modInfo = group;
    }

    /**
     * Loads this creature type from a JSON object.
     **/
    public void loadFromJson(JsonObject json) {
        this.name = json.get("name").getAsString();

        if (json.has("dietProvided"))
            this.dietProvided = json.get("dietProvided").getAsString();
    }

    /**
     * Loads this creature type (should only be called during startup), generates spawn egg, etc.
     */
    public void init() {
        this.createItems();
        LMHelperClass.logDebug("Creature Type", "Loaded Creature Type: " + this.getName());
    }

    /**
     * Returns the name of this creature type. Ex: beast
     *
     * @return The name of this creature type.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a translated title for this creature type. Ex: Beast
     *
     * @return The display name of this creature type.
     */
    public MutableComponent getTitle() {
        return Component.translatable("creaturetype." + this.getName());
    }

    /**
     * Generates a treat item name from this type. Ex: treat_beast
     *
     * @return The treat item name for this creature type.
     */
    public String getTreatName() {
        return "treat_" + this.getName();
    }

    /**
     * Gets this creature type's treat item.
     *
     * @return The treat item for this creature type.
     */
    public Item getTreatItem() {
        return this.treat.get();
    }

    /**
     * Generates a saddle item name from this type. Ex: saddle_avian
     *
     * @return The saddle item name for this creature type.
     */
    public String getSaddleName() {
        return "saddle_" + this.getName();
    }

    /**
     * Gets this creature type's saddle item.
     *
     * @return The saddle item for this creature type.
     */
    public Item getSaddleItem() {
        return this.saddle.get();
    }

    /**
     * Generates a soulstone item name from this type. Ex: soulstone_imp
     *
     * @return The soulstone item name for this creature type.
     */
    public String getSoulstoneName() {
        return "soulstone_" + this.getName();
    }

    /**
     * Gets this creature type's soulstone item.
     *
     * @return The soulstone item for this creature type.
     */
    public ItemSoulstone getSoulstoneItem() {
        return this.soulstone.get();
    }

    /**
     * Generates a spawn egg item name from this type. Ex: spawn_insect
     *
     * @return The spawn egg item name for this creature type.
     */
    public String getSpawnEggName() {
        return "spawn_" + this.getName();
    }

    /**
     * Gets this creature type's spawn egg item.
     * Note: The Spawn Egg item requires NBT data to determine which specific Creature it spawns.
     *
     * @return The spawn egg item for this creature type.
     */
    public Item getSpawnEggItem() {
        return this.spawnEgg.get();
    }

    /**
     * Adds a creature to this Creature Type.
     *
     * @param creatureInfo The creature to add.
     * @return
     */
    public void addCreature(CreatureInfo creatureInfo) {
        if (this.creatures.containsKey(creatureInfo.getName())) {
            return;
        }
        this.creatures.put(creatureInfo.getName(), creatureInfo);
        if (creatureInfo.isTameable()) {
            this.tameableCreatures.add(creatureInfo);
        }
    }

    /**
     * Creates items for this creature type such as the spawn egg item or treat item, must be called after creatures are loaded so that an egg for each creature can be added.
     */
    public void createItems() {
        Item.Properties standardItemProperties = new Item.Properties();
        // TODO: create creative tabs
        //.tab(ItemManager.getInstance().itemsGroup)

        Item.Properties smallStackItemProperties = new Item.Properties();
        smallStackItemProperties.stacksTo(16);

        Item.Properties spawnEggProperties = new Item.Properties();

        // Treat:
        this.treat = Lazy.of(() -> new CreatureTreatItem(smallStackItemProperties, this));
        ObjectManager.addItem(this.getTreatName(), this.treat);

        // Saddle:
        this.saddle = Lazy.of(() -> new CreatureSaddleItem(smallStackItemProperties, this));
        //this.saddleItems.put(this.getName(),this.saddle);
        ObjectManager.addItem(this.getSaddleName(), this.saddle);


        // Spawn Eggs:
        this.spawnEgg = Lazy.of(() -> new ItemCustomSpawnEgg(spawnEggProperties, this));
        ObjectManager.addItem(this.getSpawnEggName(), this.spawnEgg);
        creatureNames.add(this.getSpawnEggName());

        // Soulstone:
        this.soulstone = Lazy.of(() -> new ItemSoulstoneFilled(standardItemProperties, this));
        ObjectManager.addItem(this.getSoulstoneName(), this.soulstone);
    }
}
