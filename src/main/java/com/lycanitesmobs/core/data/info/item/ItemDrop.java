package com.lycanitesmobs.core.data.info.item;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ItemDrop {
    // ========== Item ==========
    protected String itemId;
    protected String burningItemId;
    protected Map<Integer, String> effectItemIds = new HashMap<>();

    public int minAmount = 1;
    public int maxAmount = 1;
    public boolean bonusAmount = true;

    /* Set to false to prevent this drop from using drop multipliers. */
    public boolean amountMultiplier = true;

    public float chance = 0;

    /**
     * The ID of the subspecies that this drop is restricted to. An ID below 0 will have this drop ignore the subspecies.
     **/
    public int subspeciesIndex = -1;

    /**
     * The ID of the variant that this drop is restricted to. An ID below 0 will have this drop ignore the variant.
     **/
    public int variantIndex = -1;

    /**
     * If true, items will only drop for adults and not babies. True by default.
     **/
    public boolean adultOnly = true;


    // ==================================================
    //                       JSON
    // ==================================================

    /**
     * Creates a MobDrop from the provided JSON data.
     **/
    public static ItemDrop createFromJSON(JsonObject json) {
        ItemDrop itemDrop = null;
        if (json.has("item")) {
            String itemId = json.get("item").getAsString();
            itemDrop = new ItemDrop(itemId, 1);
            itemDrop.loadFromJSON(json);
        } else {
            LMHelperClass.logWarning("", "[JSON] Unable to load item drop from json as it has no item id!");
        }

        return itemDrop;
    }


    // ==================================================
    //                      Config
    // ==================================================

    /**
     * Creates a MobDrop from the provided Config String.
     **/
    public static ItemDrop createFromConfigString(String itemDropString) {
        if (itemDropString != null && itemDropString.length() > 0) {
            String[] customDropValues = itemDropString.split(",");
            String itemId = customDropValues[0];
            int itemMetadata = 0;
            if (customDropValues.length > 1) {
                itemMetadata = Integer.parseInt(customDropValues[1]);
            }
            int amountMin = 1;
            if (customDropValues.length > 2) {
                amountMin = Integer.parseInt(customDropValues[2]);
            }
            int amountMax = 1;
            if (customDropValues.length > 3) {
                amountMax = Integer.parseInt(customDropValues[3]);
            }
            float chance = 1;
            if (customDropValues.length > 4) {
                chance = Float.parseFloat(customDropValues[4]);
            }

            ItemDrop itemDrop = new ItemDrop(itemId, chance);
            itemDrop.setMinAmount(amountMin);
            itemDrop.setMaxAmount(amountMax);

            return itemDrop;
        }
        return null;
    }


    // ==================================================
    //                     Constructor
    // ==================================================
    public ItemDrop(String itemId, float chance) {
        this.itemId = itemId;
        this.chance = chance;
    }

    public ItemDrop(CompoundTag nbtTagCompound) {
        this.read(nbtTagCompound);
    }

    public ItemDrop(ItemDrop copyDrop) {
        this.itemId = copyDrop.itemId;
        this.minAmount = copyDrop.minAmount;
        this.maxAmount = copyDrop.maxAmount;
        this.bonusAmount = copyDrop.bonusAmount;
        this.amountMultiplier = copyDrop.amountMultiplier;
        this.chance = copyDrop.chance;
        this.subspeciesIndex = copyDrop.subspeciesIndex;
        this.variantIndex = copyDrop.variantIndex;
        this.adultOnly = copyDrop.adultOnly;
        this.burningItemId = copyDrop.burningItemId;
        this.effectItemIds = copyDrop.effectItemIds;
    }

    public void loadFromJSON(JsonObject json) {
        if (json.has("minAmount"))
            this.minAmount = json.get("minAmount").getAsInt();
        if (json.has("maxAmount"))
            this.maxAmount = json.get("maxAmount").getAsInt();
        if (json.has("bonusAmount"))
            this.bonusAmount = json.get("bonusAmount").getAsBoolean();
        if (json.has("amountMultiplier"))
            this.amountMultiplier = json.get("amountMultiplier").getAsBoolean();
        if (json.has("chance"))
            this.chance = json.get("chance").getAsFloat();
        if (json.has("subspecies"))
            this.subspeciesIndex = json.get("subspecies").getAsInt();
        if (json.has("variant"))
            this.variantIndex = json.get("variant").getAsInt();
        if (json.has("adultOnly"))
            this.adultOnly = json.get("adultOnly").getAsBoolean();

        if (json.has("burningItem")) {
            this.burningItemId = json.get("burningItem").getAsString();
        }
    }


    // ==================================================
    //                     Properties
    // ==================================================
    public ItemDrop setDrop(ItemStack itemStack) {
        this.itemId = itemStack.getItem().getDescriptionId();
        return this;
    }

    public ItemDrop setBurningDrop(ItemStack itemStack) {
        this.burningItemId = itemStack.getItem().getDescriptionId();
        return this;
    }

    public ItemDrop setEffectDrop(int effectID, ItemStack itemStack) {
        this.effectItemIds.put(effectID, itemStack.getItem().getDescriptionId());
        return this;
    }

    public ItemDrop setMinAmount(int amount) {
        this.minAmount = amount;
        return this;
    }

    public ItemDrop setMaxAmount(int amount) {
        this.maxAmount = amount;
        return this;
    }

    public ItemDrop setChance(float chance) {
        this.chance = chance;
        return this;
    }

    public ItemDrop setSubspecies(int subspeciesIndex) {
        this.subspeciesIndex = subspeciesIndex;
        return this;
    }

    public ItemDrop setVariant(int variantIndex) {
        this.variantIndex = variantIndex;
        return this;
    }


    /**
     * Returns a quantity to drop.
     *
     * @param random     The instance of random to use.
     * @param bonus      A bonus multiplier.
     * @param multiplier The value to multiply the quantity by.
     * @return The randomised amount to drop.
     */
    public int getQuantity(RandomSource random, int bonus, int multiplier) {
        // Will It Drop?
        float roll = random.nextFloat();
        roll = Math.max(roll, 0);
        if (roll > this.chance)
            return 0;

        // How Many?
        if (!this.amountMultiplier) {
            multiplier = 1;
        }
        int min = this.minAmount;
        int max = this.maxAmount + (this.bonusAmount ? bonus : 0);
        if (max <= min) {
            return min * multiplier;
        }
        roll = roll / this.chance;
        float dropRange = (max - min) * roll;
        int dropAmount = min + Math.round(dropRange);
        return Math.min(dropAmount * multiplier, this.getItemStack().getMaxStackSize());
    }

    /**
     * Gets the base itemstack for this item drop.
     *
     * @return The base itemstack to drop.
     */
    @Nonnull
    public ItemStack getItemStack() {
        if (this.itemId == null) {
            return ItemStack.EMPTY;
        }

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.itemId));
        if (item != null) {
            return new ItemStack(item, 1);
        }

        return ItemStack.EMPTY;
    }

    /**
     * Gets the itemstack that burning entities should drop.
     *
     * @return The burning itemstack or the base itemstack if not set.
     */
    @Nonnull
    public ItemStack getBurningItemStack() {
        if (this.burningItemId == null) {
            return this.getItemStack();
        }

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.burningItemId));
        if (item != null) {
            return new ItemStack(item, 1);
        }

        return this.getItemStack();
    }

    /**
     * Gets the itemstack that entities with the provided effect should drop.
     *
     * @return The effect itemstack or the base itemstack if not set.
     */
    @Nonnull
    public ItemStack getEffectItemStack(int effectId) {
        if (!this.effectItemIds.containsKey(effectId)) {
            return ItemStack.EMPTY;
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.effectItemIds.get(effectId)));
        if (item != null) {
            return new ItemStack(item, 1);
        }

        return ItemStack.EMPTY;
    }

    public ItemStack getEntityDropItemStack(LivingEntity entity, int quantity) {
        ItemStack itemStack = this.getItemStack();

        if (entity != null) {
            if (entity.isOnFire()) {
                itemStack = this.getBurningItemStack();
            }

            for (Object potionEffect : entity.getActiveEffects()) {
                if (potionEffect instanceof MobEffectInstance) {
                    int effectId = MobEffect.getId(((MobEffectInstance) potionEffect).getEffect());
                    ItemStack effectStack = this.getEffectItemStack(effectId);
                    if (!effectStack.isEmpty())
                        itemStack = effectStack;
                }
            }
        }

        itemStack.setCount(quantity);
        return itemStack;
    }


    /**
     * Reads this Item Drop from NBT.
     *
     * @param nbtTagCompound The NBT to load values from.
     */
    public void read(CompoundTag nbtTagCompound) {
        if (nbtTagCompound.contains("ItemId"))
            this.itemId = nbtTagCompound.getString("ItemId");
        this.minAmount = nbtTagCompound.getInt("MinAmount");
        this.maxAmount = nbtTagCompound.getInt("MaxAmount");
        if (nbtTagCompound.contains("BonusAmount"))
            this.bonusAmount = nbtTagCompound.getBoolean("BonusAmount");
        this.chance = nbtTagCompound.getFloat("Chance");
        if (nbtTagCompound.contains("AmountMultiplier"))
            this.amountMultiplier = nbtTagCompound.getBoolean("AmountMultiplier");
        if (nbtTagCompound.contains("Subspecies"))
            this.subspeciesIndex = nbtTagCompound.getInt("Subspecies");
        if (nbtTagCompound.contains("Variant"))
            this.variantIndex = nbtTagCompound.getInt("Variant");
        if (nbtTagCompound.contains("AdultOnly"))
            this.adultOnly = nbtTagCompound.getBoolean("AdultOnly");
    }


    /**
     * Writes this Item Drop to NBT.
     *
     * @param nbtTagCompound The NBT to write to.
     * @return True on success or false on fail (this happens if this drop is missing an item id, etc).
     */
    public boolean writeToNBT(CompoundTag nbtTagCompound) {
        if (this.itemId == null) {
            return false;
        }

        nbtTagCompound.putString("ItemId", this.itemId);
        nbtTagCompound.putInt("MinAmount", this.minAmount);
        nbtTagCompound.putInt("MaxAmount", this.maxAmount);
        nbtTagCompound.putBoolean("BonusAmount", this.bonusAmount);
        nbtTagCompound.putFloat("Chance", this.chance);
        nbtTagCompound.putBoolean("AmountMultiplier", this.amountMultiplier);
        nbtTagCompound.putInt("Subspecies", this.subspeciesIndex);
        nbtTagCompound.putInt("Variant", this.variantIndex);
        nbtTagCompound.putBoolean("AdultOnly", this.adultOnly);

        return true;
    }


    /**
     * Returns this Item Drop as a string value for using in configs.
     *
     * @return The Item Drop config string.
     */
    public String toConfigString() {
        return this.itemId + "," + this.minAmount + "," + this.maxAmount + "," + this.chance;
    }
}
