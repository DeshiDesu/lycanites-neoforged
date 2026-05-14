package com.lycanitesmobs.core.item.base;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class BaseItem extends Item {
    public static int DESCRIPTION_WIDTH = 200;
    public ResourceLocation registryName = null;
    public String itemName = "unamed_item";

    public BaseItem(Properties properties) {
        super(properties);
        setup();
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
    @Nonnull
    public String getDescriptionId() {
        return "item." + LycanitesMobs.MODID + "." + this.itemName;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack));
    }


    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        Component description = this.getDescription(stack, worldIn, tooltip, flag);
        if (!"".equalsIgnoreCase(description.getString())) {
            tooltip.add(description);
        }
        super.appendHoverText(stack, worldIn, tooltip, flag);
    }

    @Override
    public Component getDescription() {
        return super.getDescription();
    }

    public Component getDescription(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        return Component.translatable(this.getDescriptionId() + ".description").withStyle(ChatFormatting.GREEN);
    }

    public boolean hasContainerItem(ItemStack stack) {
        return this.hasCraftingRemainingItem();
    }

    public ItemStack getContainerItem(ItemStack itemStack) {
        return !this.hasContainerItem(itemStack) ? ItemStack.EMPTY : new ItemStack(this.getCraftingRemainingItem());
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return super.onEntityItemUpdate(stack, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return super.use(world, player, hand);
    }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        return super.interactLivingEntity(stack, player, entity, hand);
    }


    @Override
    public void onUseTick(Level p_41428_, LivingEntity p_41429_, ItemStack p_41430_, int p_41431_) {
        super.onUseTick(p_41428_, p_41429_, p_41430_, p_41431_);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        super.releaseUsing(stack, worldIn, entityLiving, timeLeft);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return super.getUseAnimation(itemStack);
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairStack) {
        return super.isValidRepairItem(itemStack, repairStack);
    }

    /**
     * Gets or creates an NBT Compound for the provided itemstack.
     **/
    public CompoundTag getTagCompound(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            return itemStack.getTag();
        }
        return new CompoundTag();
    }

    public void playSound(Level world, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(null, x, y, z, sound, category, volume, pitch);
    }

    public void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(null, pos, sound, category, volume, pitch);
    }
}
