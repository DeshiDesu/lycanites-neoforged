package com.lycanitesmobs.core.data.info.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.item.base.GenericItem;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.Lazy;

import java.util.Iterator;
import java.util.function.Supplier;

import static com.lycanitesmobs.core.manager.ObjectManager.items;

public class ItemInfo {
    /** The Item based on this Item Info. **/
    //public GenericItem item;

    // Core Info:
    /**
     * The name of this item. Lowercase, no space, used for language entries and for generating the projectile id, etc. Required.
     **/
    protected String name;

    /**
     * The group that this item belongs to.
     **/
    public ModInfo group;

    /**
     * Constructor
     *
     * @param group The group that this item definition will belong to.
     */
    public ItemInfo(ModInfo group) {
        this.group = group;
    }

    /**
     * Loads this item from a JSON object.
     **/
    public void loadFromJSON(JsonObject json) {
        this.name = json.get("name").getAsString();

        // Model (Optional):
        String modelName = null;
        if (json.has("model")) {
            modelName = json.get("model").getAsString();
        }

        // Group:
		/*CreativeModeTab.Builder group = ItemManager.getInstance().itemsGroup;
		if(json.has("group")) {
			String groupName = json.get("group").getAsString();
			if("blocks".equalsIgnoreCase(groupName))
				group = ItemManager.getInstance().blocksGroup;
			if("creatures".equalsIgnoreCase(groupName))
				group = ItemManager.getInstance().creaturesGroups;
		}*/

        // Stack Size:
        int maxStackSize = 64;
        if (json.has("maxStackSize"))
            maxStackSize = json.get("maxStackSize").getAsInt();

        // Food:
        FoodProperties food = null;
        if (json.has("food")) {
            JsonObject foodJson = json.get("food").getAsJsonObject();
            FoodProperties.Builder foodBuilder = new FoodProperties.Builder();
            foodBuilder.nutrition(foodJson.get("hunger").getAsInt());
            foodBuilder.saturationMod(foodJson.get("saturation").getAsFloat());

            if (!foodJson.has("alwaysEdible") || foodJson.get("alwaysEdible").getAsBoolean())
                foodBuilder.alwaysEat();

            if (foodJson.has("fast") && foodJson.get("fast").getAsBoolean())
                foodBuilder.fast();

            if (foodJson.has("meat") && foodJson.get("meat").getAsBoolean())
                foodBuilder.meat();

            if (foodJson.has("effects")) {
                JsonArray effectsJson = foodJson.getAsJsonArray("effects");
                Iterator<JsonElement> jsonIterator = effectsJson.iterator();
                while (jsonIterator.hasNext()) {
                    JsonObject foodEffectJson = jsonIterator.next().getAsJsonObject();
                    String effectId = foodEffectJson.get("effectId").getAsString();
                    String[] effectIds = effectId.split(":"); // Can't get effects from registry yet, this means no effects from other mods. :(
                    MobEffect effect;
                    if ("minecraft".equals(effectIds[0]))
                        effect = ObjectLists.allEffects.get(effectIds[1]);
                    else
                        effect = ObjectManager.getEffect(effectIds[1]);
                    if (effect == null) {
                        LMHelperClass.logWarning("", "Unable to add food effect: " + effectId);
                        continue;
                    }
                    MobEffectInstance effectInstance = new MobEffectInstance(effect, foodEffectJson.get("duration").getAsInt() * 20, foodEffectJson.get("amplifier").getAsInt());

                    float chance = 1F;
                    if (foodEffectJson.has("chance"))
                        chance = foodEffectJson.get("chance").getAsFloat();

                    foodBuilder.effect(effectInstance, chance);
                }
            }

            food = foodBuilder.build();
        }

        // Create Item Properties:
        Item.Properties properties = new Item.Properties();
		/*if(modelName != null) TODO Generic Item Model Renderer
			properties.setTEISR(() -> com.lycanitesmobs.core.renderer.EquipmentRenderer::new);*/

        properties.stacksTo(maxStackSize);
        if (food != null)
            properties.food(food);

        // Create Item:
        String finalModelName = modelName;
        Lazy<Item> item = Lazy.of(() -> new GenericItem(properties, this.name, finalModelName));
        ObjectManager.addItem(name, item);
        //this.getItem().modelName = modelName;
        /*
         * Creative Mode Tab
         */
        /*group.displayItems((enabledFeatures, entries) -> {
            entries.accept(this.getItem());
        });*/
    }

    /**
     * Gets the Item based on this ItemInfo/
     *
     * @return The item.
     */
    public GenericItem getItem() {
        return (GenericItem) items.get(name).get();
    }

    /**
     * Returns the name of this item info, this is the unformatted lowercase name. Ex: cleansingcrystal
     *
     * @return Item name.
     */
    public String getName() {
        return this.name;
    }
}
