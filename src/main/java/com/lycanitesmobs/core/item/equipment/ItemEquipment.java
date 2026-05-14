package com.lycanitesmobs.core.item.equipment;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.client.renderer.item.EquipmentRenderer;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.item.base.BaseItem;
import com.lycanitesmobs.core.data.info.item.ItemConfig;
import com.lycanitesmobs.core.item.equipment.features.*;
import com.lycanitesmobs.core.item.tool.CustomTiers;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.item.tool.ToolType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class ItemEquipment extends BaseItem {
    /**
     * The maximum amount of parts that can be added to an Equipment Piece.
     **/
    public static int PART_LIMIT = 20;

    public static int SHARPNESS_MAX = 1500;
    public static int MANA_MAX = 1500;
    public ResourceLocation registryName = null;

    /**
     * Constructor
     */
    public ItemEquipment(Properties properties) {
        super(properties);
        this.itemName = "equipment";
        setup();
        properties.stacksTo(1);
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public ResourceLocation setRegistryName(String modID, String itemName) {
        return registryName = new ResourceLocation(modID, itemName);
    }

    public void setup() {
        this.setRegistryName(LycanitesMobs.MODID, this.itemName);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        if (itemStack.hasCustomHoverName()) {
            return itemStack.getHoverName();
        }
        if (ItemConfig.equipmentSpecialNames) {
            Component generatedName = EquipmentNameGenerator.generateName(itemStack);
            if (generatedName != null) {
                return generatedName;
            }
        }
        return super.getName(itemStack);
    }

    // ==================================================
    //                 Custom Renderer
    // ==================================================
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new EquipmentRenderer();
            }
        });
    }

    // ==================================================
    //                      Info
    // ==================================================
    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        for (MutableComponent description : this.getAdditionalDescriptions(stack, worldIn, flag)) {
            tooltip.add(description);
        }
    }

    @Override
    public Component getDescription(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        return Component.literal("");
    }

    public List<MutableComponent> getAdditionalDescriptions(ItemStack itemStack, @Nullable Level world, TooltipFlag tooltipFlag) {
        List<MutableComponent> descriptions = new ArrayList<>();

        // Condition:
        descriptions.add(Component.translatable("equipment.sharpness").append(" " + this.getSharpness(itemStack) + "/" + SHARPNESS_MAX).withStyle(ChatFormatting.BLUE));
        descriptions.add(Component.translatable("equipment.mana").append(" " + this.getMana(itemStack) + "/" + MANA_MAX).withStyle(ChatFormatting.BLUE));
        descriptions.add(Component.literal("-------------------"));

        // Part Names:
        for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(itemStack)) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null)
                continue;
            descriptions.add(equipmentPart.getName(itemStack).plainCopy().withStyle(ChatFormatting.DARK_GREEN));
        }
        descriptions.add(Component.literal("-------------------"));

        // Damage:
        descriptions.add(Component.translatable("equipment.feature.damage")
                .append(" " + String.format("%.0f", this.getDamageAmount(itemStack) + 1)).withStyle(ChatFormatting.GOLD));
        descriptions.add(Component.translatable("equipment.feature.damage.cooldown")
                .append(" " + String.format("%.1f", this.getDamageCooldown(itemStack))).withStyle(ChatFormatting.GOLD));
        descriptions.add(Component.translatable("equipment.feature.damage.knockback")
                .append(" " + String.format("%.0f", this.getDamageKnockback(itemStack))).withStyle(ChatFormatting.GOLD));
        descriptions.add(Component.translatable("equipment.feature.damage.range")
                .append(" " + String.format("%.1f", this.getDamageRange(itemStack))).withStyle(ChatFormatting.GOLD));
        descriptions.add(Component.translatable("equipment.feature.damage.sweep")
                .append(" " + String.format("%.0f", Math.min(this.getDamageSweep(itemStack), 360))).withStyle(ChatFormatting.GOLD));

        // Summaries:
        MutableComponent harvestSummaries = this.getFeatureSummaries(itemStack, "harvest");
        MutableComponent effectSummaries = this.getFeatureSummaries(itemStack, "effect");
        MutableComponent projectileSummaries = this.getFeatureSummaries(itemStack, "projectile");
        MutableComponent summonSummaries = this.getFeatureSummaries(itemStack, "summon");
        if (!"".equals(harvestSummaries.getString()) || !"".equals(effectSummaries.getString()) || !"".equals(projectileSummaries.getString()) || !"".equals(summonSummaries.getString())) {
            descriptions.add(Component.literal("-------------------"));

            // Harvest:
            if (!"".equals(harvestSummaries.getString())) {
                descriptions.add(Component.translatable("equipment.feature.harvest")
                        .append(" ").append(harvestSummaries).withStyle(ChatFormatting.DARK_PURPLE));
            }

            // Effect:
            if (!"".equals(effectSummaries.getString())) {
                descriptions.add(Component.translatable("equipment.feature.effect")
                        .append(" ").append(effectSummaries).withStyle(ChatFormatting.DARK_PURPLE));
            }

            // Projectile:
            if (!"".equals(projectileSummaries.getString())) {
                descriptions.add(Component.translatable("equipment.feature.projectile")
                        .append(" ").append(projectileSummaries).withStyle(ChatFormatting.DARK_PURPLE));
            }

            // Summon:
            if (!"".equals(summonSummaries.getString())) {
                descriptions.add(Component.translatable("equipment.feature.summon")
                        .append(" ").append(summonSummaries).withStyle(ChatFormatting.DARK_PURPLE));
            }
        }

        return descriptions;
    }


    /**
     * Gets comma separated text of the description summary of the provided feature type.
     *
     * @param itemStack   The Equipment Piece itemstack to get parts and features from.
     * @param featureType The feature type to get the summaries of.
     * @return A string of summaries, empty if none are of the type are found.
     */
    public MutableComponent getFeatureSummaries(ItemStack itemStack, String featureType) {
        Map<EquipmentFeature, ItemStack> effectFeatures = this.getFeaturesByTypeWithPartStack(itemStack, featureType);
        MutableComponent featureSummaries = Component.literal("");
        boolean first = true;
        for (EquipmentFeature equipmentFeature : effectFeatures.keySet()) {
            if (!first) {
                featureSummaries.append(", ");
            }
            first = false;
            MutableComponent featureSummary = equipmentFeature.getSummary(effectFeatures.get(equipmentFeature), this.getPartLevel(effectFeatures.get(equipmentFeature)));
            if (featureSummary != null) {
                featureSummaries.append(featureSummary);
            }
        }
        return featureSummaries;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public Font getFontRenderer(ItemStack stack) {
        return ClientManager.getInstance().getFontRenderer();
    }

    @Override
    public boolean isBarVisible(ItemStack itemStack) {
        return false;
    }


    // ==================================================
    //                  Equipment Piece
    // ==================================================

    /**
     * Returns a list of all Equipment Part ItemStacks for each slot.
     *
     * @param itemStack The Equipment ItemStack to get the Equipment Part ItemStacks from.
     * @return The map of all Equipment Part ItemStacks for each slot.
     */
    public NonNullList<ItemStack> getEquipmentPartStacks(ItemStack itemStack) {
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(PART_LIMIT, ItemStack.EMPTY);
        CompoundTag nbt = this.getTagCompound(itemStack);
        if (nbt.contains("Items")) {
            ContainerHelper.loadAllItems(nbt, itemStacks); // Reads ItemStacks into a List from "Items" tag.
        }
        return itemStacks;
    }

    /**
     * Counts how many Equipment Parts this Piece is made out of.
     *
     * @param itemStack The Equipment ItemStack to count the Equipment Part ItemStacks from.
     * @return How many Equipment Parts make this Equipment Piece.
     */
    public int getEquipmentPartCount(ItemStack itemStack) {
        int partCount = 0;
        for (ItemStack partStack : this.getEquipmentPartStacks(itemStack)) {
            if (!partStack.isEmpty()) {
                partCount++;
            }
        }
        return partCount;
    }

    /**
     * Returns the Equipment Part for the provided ItemStack or null in empty or a different item.
     *
     * @param itemStack The Equipment Part ItemStack to get the Equipment Part from.
     * @return The Equipment Part or null if the stack is empty.
     */
    public ItemEquipmentPart getEquipmentPart(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return null;
        }
        if (!(itemStack.getItem() instanceof ItemEquipmentPart)) {
            return null;
        }
        return (ItemEquipmentPart) itemStack.getItem();
    }

    /**
     * Adds an Equipment Part ItemStack to the provided Equipment ItemStack.
     *
     * @param equipmentStack     The Equipment ItemStack to add a part to.
     * @param equipmentPartStack The Equipment Part ItemStack to add.
     * @param slotIndex          The slot index to connect the part to.
     */
    public void addEquipmentPart(ItemStack equipmentStack, ItemStack equipmentPartStack, int slotIndex) {
        if (slotIndex >= PART_LIMIT) {
            return;
        }
        NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);
        itemStacks.set(slotIndex, equipmentPartStack);
        CompoundTag nbt = this.getTagCompound(equipmentStack);
        ContainerHelper.saveAllItems(nbt, itemStacks);
        equipmentStack.setTag(nbt);
    }

    /**
     * Searches for the provided active features by type and returns a list of them.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @param featureType    The type of feature to search for.
     * @return A list of features.
     */
    public List<EquipmentFeature> getFeaturesByType(ItemStack equipmentStack, String featureType) {
        List<EquipmentFeature> features = new ArrayList<>();
        for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            for (EquipmentFeature feature : equipmentPart.features) {
                if (feature.isActive(equipmentPartStack, equipmentPart.getPartLevel(equipmentPartStack)) && feature.featureType.equalsIgnoreCase(featureType)) {
                    features.add(feature);
                }
            }
        }
        return features;
    }

    /**
     * Searches for the provided active features by type and returns a map of them with the feature as the key and the stack they are from as the value.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @param featureType    The type of feature to search for.
     * @return A list of features.
     */
    public Map<EquipmentFeature, ItemStack> getFeaturesByTypeWithPartStack(ItemStack equipmentStack, String featureType) {
        Map<EquipmentFeature, ItemStack> features = new HashMap<>();
        for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            for (EquipmentFeature feature : equipmentPart.features) {
                if (feature.isActive(equipmentPartStack, equipmentPart.getPartLevel(equipmentPartStack)) && feature.featureType.equalsIgnoreCase(featureType)) {
                    features.put(feature, equipmentPartStack);
                }
            }
        }
        return features;
    }

    /**
     * Cycles through each Equipment Part and lowers their level to the provided level cap, used by lower level Forges.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @param levelCap       The level cap to lower parts to.
     */
    public void applyLevelCap(ItemStack equipmentStack, int levelCap) {
        for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            equipmentPart.setLevel(equipmentPartStack, Math.min(levelCap, equipmentPart.getPartLevel(equipmentPartStack)));
        }
    }

    /**
     * Cycles through each Equipment Part returns the highest part level found.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @return The highest part level found.
     */
    public int getHighestLevel(ItemStack equipmentStack) {
        int highestLevel = 0;
        for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            int partLevel = equipmentPart.getPartLevel(equipmentPartStack);
            if (partLevel > highestLevel) {
                highestLevel = partLevel;
            }
        }
        return highestLevel;
    }

    /**
     * Gets the Equipment Part level from the provided itemstack.
     *
     * @param partStack The itemstack to get a part level from.
     * @return The part level or 1 if the itemstack is invalid.
     */
    public int getPartLevel(ItemStack partStack) {
        Item featureItem = partStack.getItem();
        if (!(featureItem instanceof ItemEquipmentPart)) {
            return 1;
        }
        ItemEquipmentPart featurePart = (ItemEquipmentPart) featureItem;
        return featurePart.getPartLevel(partStack);
    }

    /**
     * Determines the Sharpness of this Equipment using the part with the least amount of Sharpness.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @return The lowest part sharpness found.
     */
    public int getSharpness(ItemStack equipmentStack) {
        int lowestSharpness = SHARPNESS_MAX;
        for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            int partSharpness = equipmentPart.getSharpness(equipmentPartStack);
            if (partSharpness < lowestSharpness) {
                lowestSharpness = partSharpness;
            }
        }
        return lowestSharpness;
    }

    /**
     * Increases the Sharpness of this Equipment by increasing the Sharpness of all parts.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @return True if Sharpness was increased at all.
     */
    public boolean addSharpness(ItemStack equipmentStack, int sharpness) {
        boolean sharpnessIncreased = false;
        NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);
        for (ItemStack equipmentPartStack : itemStacks) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            sharpnessIncreased = equipmentPart.addSharpness(equipmentPartStack, sharpness) || sharpnessIncreased;
        }

        CompoundTag nbt = this.getTagCompound(equipmentStack);
        ContainerHelper.saveAllItems(nbt, itemStacks);
        equipmentStack.setTag(nbt);
        return sharpnessIncreased;
    }

    /**
     * Decreases the Sharpness of this Equipment by decreasing the Sharpness of all parts.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @return True if Sharpness was decreased at all.
     */
    public boolean removeSharpness(ItemStack equipmentStack, int sharpness) {
        boolean sharpnessDecreased = false;
        NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);
        for (ItemStack equipmentPartStack : itemStacks) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            sharpnessDecreased = equipmentPart.removeSharpness(equipmentPartStack, sharpness) || sharpnessDecreased;
        }

        CompoundTag nbt = this.getTagCompound(equipmentStack);
        ContainerHelper.saveAllItems(nbt, itemStacks);
        equipmentStack.setTag(nbt);
        return sharpnessDecreased;
    }

    /**
     * Determines the Mana of this Equipment using the part with the least amount of Mana.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @return The lowest part mana found.
     */
    public int getMana(ItemStack equipmentStack) {
        int lowestMana = MANA_MAX;
        for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            int partMana = equipmentPart.getMana(equipmentPartStack);
            if (partMana < lowestMana) {
                lowestMana = partMana;
            }
        }
        return lowestMana;
    }

    /**
     * Increases the Mana of this Equipment by increasing the Mana of all parts.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @return True if Mana was increased at all.
     */
    public boolean addMana(ItemStack equipmentStack, int mana) {
        boolean manaIncreased = false;
        NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);
        for (ItemStack equipmentPartStack : itemStacks) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            manaIncreased = equipmentPart.addMana(equipmentPartStack, mana) || manaIncreased;
        }

        CompoundTag nbt = this.getTagCompound(equipmentStack);
        ContainerHelper.saveAllItems(nbt, itemStacks);
        equipmentStack.setTag(nbt);
        return manaIncreased;
    }

    /**
     * Decreases the Mana of this Equipment by decreasing the Mana of all parts.
     *
     * @param equipmentStack The itemStack of the Equipment.
     * @return True if Mana was decreased at all.
     */
    public boolean removeMana(ItemStack equipmentStack, int mana) {
        boolean manaDecreased = false;
        NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);
        for (ItemStack equipmentPartStack : itemStacks) {
            ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
            if (equipmentPart == null) {
                continue;
            }
            manaDecreased = equipmentPart.removeMana(equipmentPartStack, mana) || manaDecreased;
        }

        CompoundTag nbt = this.getTagCompound(equipmentStack);
        ContainerHelper.saveAllItems(nbt, itemStacks);
        equipmentStack.setTag(nbt);
        return manaDecreased;
    }


    // ==================================================
    //                       Using
    // ==================================================
    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 1800;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.getMana(itemStack) <= 0) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
        }
        boolean active = false;

        // Projectiles:
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "projectile")) {
            ProjectileEquipmentFeature projectileFeature = (ProjectileEquipmentFeature) equipmentFeature;
            if (projectileFeature.onUseSecondary(world, player, hand)) {
                active = true;
            }
        }

        if (active) {
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
    }

    @Override
    public void onUseTick(Level p_41428_, LivingEntity user, ItemStack itemStack, int count) {
        if (!user.isUsingItem() || this.getMana(itemStack) <= 0) {
            return;
        }
        boolean usedMana = false;

        // Projectiles:
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "projectile")) {
            ProjectileEquipmentFeature projectileFeature = (ProjectileEquipmentFeature) equipmentFeature;
            usedMana = projectileFeature.onHoldSecondary(user, count) || usedMana;
        }

        if (usedMana) {
            this.removeMana(itemStack, 1);
        }
    }

    /**
     * Called when the player left clicks with this equipment. This is called via the left click empty or block events (a network packet is called for left click empty).
     *
     * @param world  The world the player is in.
     * @param player The player using the equipment.
     * @param hand   The active hand.
     */
    public void onItemLeftClick(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.getMana(itemStack) <= 0) {
            return;
        }
        boolean usedMana = false;

        // Projectiles:
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "projectile")) {
            ProjectileEquipmentFeature projectileFeature = (ProjectileEquipmentFeature) equipmentFeature;
            usedMana = projectileFeature.onUsePrimary(world, player, hand) || usedMana;
        }

        if (usedMana) {
            this.removeMana(itemStack, 1);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        boolean active = false;
        ItemStack itemStack = context.getItemInHand();
        if (this.getMana(itemStack) <= 0) {
            return super.useOn(context);
        }

        // Harvesting:
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature) equipmentFeature;
            if (harvestFeature.onBlockUsed(context)) {
                active = true;
            }
        }

        if (active) {
            context.getPlayer().startUsingItem(context.getHand());
            this.removeSharpness(itemStack, 1);
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        boolean entityInteraction = false;

        // Harvesting:
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature) equipmentFeature;
            if (harvestFeature.onEntityInteraction(player, target, itemStack)) {
                entityInteraction = true;
            }
        }

        if (entityInteraction) {
            this.removeSharpness(itemStack, 1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }


    // ==================================================
    //                     Harvesting
    // ==================================================


    @Nonnull
    public Set<ToolType> getToolTypes(ItemStack itemStack) {
        Map<ToolType, Boolean> toolTypes = new HashMap<>();
        if (this.getSharpness(itemStack) <= 0) {
            return toolTypes.keySet();
        }
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature) equipmentFeature;
            ToolType toolType = harvestFeature.getToolType();
            if (toolType != null) {
                toolTypes.put(toolType, true);
            }
        }
        return toolTypes.keySet();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (this.getSharpness(stack) <= 0) {
            return false;
        }
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(stack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature) equipmentFeature;
            String type = harvestFeature.harvestType;
            if ("pickaxe".equalsIgnoreCase(type) && ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction)) {
                return true;
            }
            if ("axe".equalsIgnoreCase(type) && ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction)) {
                return true;
            }
            if ("shovel".equalsIgnoreCase(type) && ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction)) {
                return true;
            }
            if ("hoe".equalsIgnoreCase(type) && ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction)) {
                return true;
            }
            if ("sword".equalsIgnoreCase(type) && ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction)) {
                return true;
            }
            if ("shears".equalsIgnoreCase(type) && ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction)) {
                return true;
            }
        }
        return false;
    }

    public int getHarvestLevel(ItemStack itemStack, ToolType tool, @Nullable Player player, @Nullable BlockState blockState) {
        if (this.getSharpness(itemStack) <= 0) {
            return -1;
        }

        int harvestLevel = -1;
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature) equipmentFeature;
            if (harvestLevel < harvestFeature.harvestLevel && tool == harvestFeature.getToolType()) {
                harvestLevel = harvestFeature.harvestLevel;
            }
        }
        return harvestLevel;
    }
    /**
     * Returns the highest CustomTier across all harvest features on this equipment.
     */
    @Nullable
    public Tier getTier(ItemStack itemStack) {
        if (this.getSharpness(itemStack) <= 0) {
            return null;
        }

        int harvestLevel = -1;
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature) equipmentFeature;
            if (harvestLevel < harvestFeature.harvestLevel) {
                harvestLevel = harvestFeature.harvestLevel;
            }
        }

        return switch (harvestLevel) {
            case 0 -> CustomTiers.HARVEST_0;
            case 1 -> CustomTiers.HARVEST_1;
            case 2 -> CustomTiers.HARVEST_2;
            case 3 -> CustomTiers.HARVEST_3;
            case 4 -> CustomTiers.HARVEST_4;
            default -> null;
        };
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack itemStack, BlockState blockState) {
        if (this.getSharpness(itemStack) <= 0) {
            return false;
        }

        boolean canHarvest = false;
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature) equipmentFeature;
            if (harvestFeature.canHarvestBlock(blockState)) {
                canHarvest = true;
                break;
            }
        }

        if (!canHarvest) {
            return false;
        }

        Tier tier = this.getTier(itemStack);
        if (tier == null) {
            return false;
        }

        return TierSortingRegistry.isCorrectTierForDrops(tier, blockState);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState blockState) {
        return this.isCorrectToolForDrops(this.getDefaultInstance(), blockState);
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        if (this.getSharpness(itemStack) <= 0) {
            return 1;
        }

        float speed = 1;
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature) equipmentFeature;
            speed += harvestFeature.getHarvestSpeed(blockState);
        }
        return speed;
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level world, BlockState blockState, BlockPos pos, LivingEntity entityLiving) {
        if (this.getSharpness(itemStack) <= 0) {
            return super.mineBlock(itemStack, world, blockState, pos, entityLiving);
        }
        if (!world.isClientSide) {
            for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
                HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature) equipmentFeature;
                harvestFeature.onBlockDestroyed(world, blockState, pos, entityLiving);
            }
        }
        this.removeSharpness(itemStack, 1);
        return super.mineBlock(itemStack, world, blockState, pos, entityLiving);
    }


    // ==================================================
    //                      Damaging
    // ==================================================

    /**
     * Called when an entity is hit with this item.
     *
     * @param itemStack     The ItemStack being hit with.
     * @param primaryTarget The target entity being hit.
     * @param attacker      The entity using this item to hit.
     * @return True on successful hit.
     */
    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity primaryTarget, LivingEntity attacker) {
        if (this.getSharpness(itemStack) <= 0) {
            return true;
        }

        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(attacker);
        boolean attackOnCooldown = false;
        if (extendedEntity != null) {
            int currentCooldown = extendedEntity.getProjectileCooldown(1, "equipment_melee");
            int weaponCooldown = 9 * (int) this.getDamageCooldown(itemStack);
            attackOnCooldown = currentCooldown > 0;
            if (currentCooldown < weaponCooldown) {
                extendedEntity.setProjectileCooldown(1, "equipment_melee", weaponCooldown);
            }
        }

        List<LivingEntity> targets = new ArrayList<>();
        targets.add(primaryTarget);

        // Sweeping:
        if (!attacker.getCommandSenderWorld().isClientSide && !attacker.isShiftKeyDown() && !attackOnCooldown) {
            double sweepAngle = this.getDamageSweep(itemStack);
            if (sweepAngle > 0) {
                float sweepDamage = (float) this.getDamageAmount(itemStack);
                double sweepRange = 1 + this.getDamageRange(itemStack);
                List<LivingEntity> possibleTargets = attacker.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(sweepRange, sweepRange, sweepRange));
                for (LivingEntity possibleTarget : possibleTargets) {
                    // Valid Sweep Target:
                    if (possibleTarget == attacker || possibleTarget == primaryTarget) {
                        continue;
                    }
                    if (!possibleTarget.isAlive()) {
                        continue;
                    }
                    if (possibleTarget instanceof Player) {
                        continue;
                    }
                    if (possibleTarget instanceof OwnableEntity) {
                        OwnableEntity possibleTameableTarget = (OwnableEntity) possibleTarget;
                        if (possibleTameableTarget.getOwner() != null && !attacker.getCommandSenderWorld().getServer().isPvpAllowed()) {
                            continue;
                        }
                        if (possibleTameableTarget.getOwner() == attacker) {
                            continue;
                        }
                    }
                    if (possibleTarget instanceof TameableCreatureEntity) {
                        TameableCreatureEntity possibleTameableTarget = (TameableCreatureEntity) possibleTarget;
                        if (possibleTameableTarget.getPlayerOwner() != null && !attacker.getCommandSenderWorld().getServer().isPvpAllowed()) {
                            continue;
                        }
                        if (possibleTameableTarget.getPlayerOwner() == attacker) {
                            continue;
                        }
                    }

                    // Check Angle:
                    double targetXDist = possibleTarget.position().x() - attacker.position().x();
                    double targetZDist = attacker.position().z() - possibleTarget.position().z();
                    double targetAngleAbsolute = 180 + Math.toDegrees(Math.atan2(targetXDist, targetZDist));
                    double targetAngle = Math.abs(targetAngleAbsolute - attacker.yRotO);
                    if (targetAngle > 180) {
                        targetAngle = 180 - (targetAngle - 180);
                    }
                    if (targetAngle > sweepAngle) {
                        continue;
                    }

                    targets.add(possibleTarget);
                    DamageSource sweepSource = primaryTarget.level().damageSources().generic();
                    if (attacker instanceof Player) {
                        sweepSource = attacker.level().damageSources().playerAttack((Player) attacker);
                    }
                    possibleTarget.hurt(sweepSource, sweepDamage);
                }
            }
        }

        // Sweep Particles:
        if (attacker instanceof Player && targets.size() > 1) {
            Player playerAttacker = (Player) attacker;
            playerAttacker.sweepAttack();
            attacker.getCommandSenderWorld().playSound(null, attacker.position().x(), attacker.position().y(), attacker.position().z(), SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1.0F, 1.0F);
        }

        boolean usedMana = false;
        for (LivingEntity target : targets) {
            // Knockback:
            double knockback = this.getDamageKnockback(itemStack);
            if (knockback != 0 && attacker != null && target != null) {
                double xDist = attacker.position().x() - target.position().x();
                double zDist = attacker.position().z() - target.position().z();
                double xzDist = Math.max(Mth.sqrt(LMHelperClass.convertToFloat(xDist * xDist + zDist * zDist)), 0.01D);
                double motionCap = 10;
                if (target.getDeltaMovement().x() < motionCap && target.getDeltaMovement().x() > -motionCap && target.getDeltaMovement().z() < motionCap && target.getDeltaMovement().z() > -motionCap) {
                    target.push(
                            -(xDist / xzDist * knockback + target.getDeltaMovement().x() * knockback),
                            0,
                            -(zDist / xzDist * knockback + target.getDeltaMovement().z() * knockback)
                    );
                }
            }

            // Effects:
            for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "effect")) {
                EffectEquipmentFeature effectFeature = (EffectEquipmentFeature) equipmentFeature;
                effectFeature.onHitEntity(itemStack, target, attacker);
            }

            // Summons:
            for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "summon")) {
                SummonEquipmentFeature summonFeature = (SummonEquipmentFeature) equipmentFeature;
                usedMana = summonFeature.onHitEntity(itemStack, target, attacker) || usedMana;
            }

            // Projectiles:
            for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "projectile")) {
                ProjectileEquipmentFeature projectileFeature = (ProjectileEquipmentFeature) equipmentFeature;
                usedMana = projectileFeature.onHitEntity(itemStack, target, attacker) || usedMana;
            }
        }

        if (usedMana) {
            this.removeMana(itemStack, 1);
        }
        this.removeSharpness(itemStack, 1);

        return true;
    }

    /**
     * Adds attribute modifiers provided by this equipment.
     *
     * @param slot The slot that this equipment is in.
     * @return The Attribute Modifier multimap with changes applied to it.
     */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> multimap = MultimapBuilder.hashKeys().arrayListValues().build(super.getAttributeModifiers(slot, itemStack));
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", this.getDamageAmount(itemStack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -this.getDamageCooldown(itemStack), AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }

    /**
     * Returns how much damage this equipment will do.
     *
     * @param itemStack The equipment ItemStack.
     * @return The amount of base damage.
     */
    public double getDamageAmount(ItemStack itemStack) {
        if (this.getSharpness(itemStack) <= 0) {
            return 0;
        }

        double damage = 0;
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
            DamageEquipmentFeature damageFeature = (DamageEquipmentFeature) equipmentFeature;
            damage += damageFeature.damageAmount;
        }
        return damage;
    }

    /**
     * Returns the attack cooldown of this equipment.
     *
     * @param itemStack The equipment ItemStack.
     * @return The amount of cooldown in ticks.
     */
    public double getDamageCooldown(ItemStack itemStack) {
        double cooldown = 0;
        double i = 0;
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
            DamageEquipmentFeature damageFeature = (DamageEquipmentFeature) equipmentFeature;
            cooldown += damageFeature.damageCooldown;
            i++;
        }

        if (i == 0) {
            return 2.4D;
        }

        cooldown = 2.4D * (cooldown / i);
        return Math.min(3.5D, cooldown);
    }

    /**
     * Returns the attack range of this equipment.
     *
     * @param itemStack The equipment ItemStack.
     * @return The amount of range.
     */
    public double getDamageRange(ItemStack itemStack) {
        double range = 0;
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
            DamageEquipmentFeature damageFeature = (DamageEquipmentFeature) equipmentFeature;
            range += damageFeature.damageRange;
        }
        return range;
    }

    /**
     * Returns the attack knockback of this equipment.
     *
     * @param itemStack The equipment ItemStack.
     * @return The amount of knockback.
     */
    public double getDamageKnockback(ItemStack itemStack) {
        if (this.getSharpness(itemStack) <= 0) {
            return 0;
        }

        double knockback = 0;
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
            DamageEquipmentFeature damageFeature = (DamageEquipmentFeature) equipmentFeature;
            knockback += damageFeature.damageKnockback;
        }
        return knockback;
    }

    /**
     * Returns the attack sweep angle of this equipment.
     *
     * @param itemStack The equipment ItemStack.
     * @return The amount of knockback.
     */
    public double getDamageSweep(ItemStack itemStack) {
        if (this.getSharpness(itemStack) <= 0) {
            return 0;
        }

        double sweep = 0;
        for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
            DamageEquipmentFeature damageFeature = (DamageEquipmentFeature) equipmentFeature;
            sweep += damageFeature.damageSweep;
        }
        return sweep;
    }


    // ==================================================
    //                      Visuals
    // ==================================================

    /**
     * Returns the texture to use for the provided ItemStack.
     **/
    public ResourceLocation getTexture(ItemStack itemStack) {
        return TextureManager.getTexture(this.itemName);
    }
}
