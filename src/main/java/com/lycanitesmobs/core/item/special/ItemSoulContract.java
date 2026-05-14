package com.lycanitesmobs.core.item.special;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.item.base.BaseItem;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemSoulContract extends BaseItem {

    public ItemSoulContract(Properties properties) {
        super(properties);
        this.itemName = "soul_contract";
        this.setup();
    }

    /**
     * Returns the owner uuid.
     **/
    public UUID getOwnerUUID(ItemStack itemStack) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        UUID uuid = null;
        if (nbt.contains("ownerUUID")) {
            uuid = nbt.getUUID("ownerUUID");
        }
        return uuid;
    }

    /**
     * Returns the pet entry uuid.
     **/
    public UUID getPetEntryUUID(ItemStack itemStack) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        UUID uuid = null;
        if (nbt.contains("petEntryUUID")) {
            uuid = nbt.getUUID("petEntryUUID");
        }
        return uuid;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return super.use(world, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, world, tooltip, tooltipFlag);

        UUID ownerUUID = this.getOwnerUUID(itemStack);
        UUID petEntryUUID = this.getPetEntryUUID(itemStack);
        if (ownerUUID != null && petEntryUUID != null) {
            Player owner = world.getPlayerByUUID(ownerUUID);
            ExtendedPlayer extendedOwner = ExtendedPlayer.getForPlayer(owner);
            if (owner == null || extendedOwner == null) {
                tooltip.add(Component.translatable("item.lycanitesmobs.soul_contract.offline"));
                return;
            }
            PetEntry petEntry = extendedOwner.petManager.getEntry(petEntryUUID);
            if (petEntry == null) {
                tooltip.add(Component.translatable("item.lycanitesmobs.soul_contract.released").withStyle(ChatFormatting.DARK_RED));
                tooltip.add(Component.translatable("item.lycanitesmobs.soul_contract.owner").append(" ").append(owner.getDisplayName()).withStyle(ChatFormatting.DARK_PURPLE));
                return;
            }
            tooltip.add(Component.translatable("item.lycanitesmobs.soul_contract.bound").append(" ").append(petEntry.getDisplayName()).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.lycanitesmobs.soul_contract.owner").append(" ").append(owner.getDisplayName()).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity entity, InteractionHand hand) {
        // Invalid Entity:
        if (!(entity instanceof BaseCreatureEntity) || !((BaseCreatureEntity) entity).isBoundPet()) {
            if (!player.getCommandSenderWorld().isClientSide) {
                player.displayClientMessage(Component.translatable("message.soul_contract.invalid").withStyle(ChatFormatting.RED), false);
            }
            return InteractionResult.PASS;
        }

        BaseCreatureEntity creatureEntity = (BaseCreatureEntity) entity;
        UUID ownerUUID = this.getOwnerUUID(itemStack);
        UUID petEntryUUID = this.getPetEntryUUID(itemStack);

        if (ownerUUID == null || petEntryUUID == null) {
            return this.bindSoul(itemStack, player, creatureEntity);
        }

        return this.transferSoul(itemStack, player, creatureEntity, ownerUUID, petEntryUUID);
    }

    /**
     * Binds the provided pet uuid to this soul contract.
     *
     * @param itemStack      The Soul Contract itemstack.
     * @param player         The player using the Soul Contract to transfer from.
     * @param creatureEntity The entity targeted by the Soul Contract.
     * @return Pass on failure otherwise success.
     */
    public InteractionResult bindSoul(ItemStack itemStack, Player player, BaseCreatureEntity creatureEntity) {

        // Check Ownership:
        if (creatureEntity.getOwner() != player || "familiar".equalsIgnoreCase(creatureEntity.getPetEntry().getType())) {
            if (!player.getCommandSenderWorld().isClientSide) {
                player.displayClientMessage(Component.translatable("message.soul_contract.not_owner").withStyle(ChatFormatting.RED), false);
            }
            return InteractionResult.PASS;
        }

        // Bind Soul:
        if (!player.getCommandSenderWorld().isClientSide) {
            CompoundTag nbt = this.getTagCompound(itemStack);
            nbt.putUUID("ownerUUID", player.getUUID());
            nbt.putUUID("petEntryUUID", creatureEntity.getPetEntry().petEntryID);
            itemStack.setTag(nbt);
            player.getInventory().setItem(player.getInventory().selected, itemStack);
            player.displayClientMessage(Component.translatable("message.soul_contract.bound").withStyle(ChatFormatting.GREEN), false);
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Transfers the bound pet to a new owner.
     *
     * @param itemStack      The Soul Contract itemstack.
     * @param player         The player using the Soul Contract to transfer to.
     * @param creatureEntity The entity targeted by the Soul Contract to check for a match.
     * @param ownerUUID      The unique id of the pet owner.
     * @param petEntryUUID   The unique id of the pet entry to transfer.
     * @return Pass on failure otherwise success.
     */
    public InteractionResult transferSoul(ItemStack itemStack, Player player, BaseCreatureEntity creatureEntity, UUID ownerUUID, UUID petEntryUUID) {
        // Transferring to Self:
        if (player.getUUID().equals(ownerUUID)) {
            // Unbind from Contract:
            if (petEntryUUID.equals(creatureEntity.getPetEntry().petEntryID)) {
                if (!player.getCommandSenderWorld().isClientSide) {
                    CompoundTag nbt = this.getTagCompound(itemStack);
                    nbt.remove("ownerUUID");
                    nbt.remove("petEntryUUID");
                    itemStack.setTag(nbt);
                    player.getInventory().setItem(player.getInventory().selected, itemStack);
                    player.displayClientMessage(Component.translatable("message.soul_contract.unbound").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                }
                return InteractionResult.SUCCESS;
            }

            // Bind a different Creature to the Contract instead:
            return this.bindSoul(itemStack, player, creatureEntity);
        }

        // Transfer on Wrong Pet:
        if (!petEntryUUID.equals(creatureEntity.getPetEntry().petEntryID)) {
            if (!player.getCommandSenderWorld().isClientSide) {
                player.displayClientMessage(Component.translatable("message.soul_contract.wrong_target").withStyle(ChatFormatting.RED), false);
            }
            return InteractionResult.PASS;
        }

        // Get Owner:
        Player owner = player.getCommandSenderWorld().getPlayerByUUID(ownerUUID);
        ExtendedPlayer extendedOwner = ExtendedPlayer.getForPlayer(owner);
        if (owner == null || extendedOwner == null) {
            if (!player.getCommandSenderWorld().isClientSide) {
                player.displayClientMessage(Component.translatable("message.soul_contract.owner_missing").withStyle(ChatFormatting.RED), false);
            }
            return InteractionResult.PASS;
        }

        // Get Receiver:
        ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
        if (extendedPlayer == null) {
            return InteractionResult.PASS;
        }

        // Transfer to New Owner:
        if (!player.getCommandSenderWorld().isClientSide) {
            extendedOwner.petManager.removeEntry(creatureEntity.getPetEntry());
            extendedPlayer.petManager.addEntry(creatureEntity.getPetEntry());
            extendedOwner.sendPetEntriesToPlayer(creatureEntity.getPetEntry().getType());
            extendedOwner.sendPetEntryRemoveToPlayer(creatureEntity.getPetEntry());
            extendedPlayer.sendPetEntriesToPlayer(creatureEntity.getPetEntry().getType());
            owner.displayClientMessage(Component.translatable("message.soul_contract.transferred").withStyle(ChatFormatting.GREEN), false);
            player.displayClientMessage(Component.translatable("message.soul_contract.transferred").withStyle(ChatFormatting.GREEN), false);
        }
        if (creatureEntity instanceof TameableCreatureEntity) {
            ((TameableCreatureEntity) creatureEntity).setPlayerOwner(player);
        }
        player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return this.getOwnerUUID(itemStack) != null && this.getPetEntryUUID(itemStack) != null;
    }
}
