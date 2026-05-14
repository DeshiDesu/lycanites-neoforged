package com.lycanitesmobs.core.item.summoningstaff;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.special.PortalEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.item.base.BaseItem;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ItemStaffSummoning extends BaseItem {
    protected float damageScale = 1.0F;
    protected int weaponFlash = 0;

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemStaffSummoning(Item.Properties properties, String itemName, String textureName) {
        super(properties);
        this.itemName = itemName;
        this.setup();
    }


    // ==================================================
    //                       Use
    // ==================================================
    public void damageItemCharged(ItemStack itemStack, LivingEntity entity, float power) {
        ExtendedPlayer playerExt = null;
        if (entity instanceof Player) {
            playerExt = ExtendedPlayer.getForPlayer((Player) entity);
        }
        if (playerExt != null && playerExt.staffPortal != null) {
            this.damageStaff(itemStack, playerExt.staffPortal.summonAmount, (ServerPlayer) entity);
        }
    }

    // ========== Prevent Swing ==========
    @Override
    public boolean onEntitySwing(ItemStack itemStack, LivingEntity entity) {
        if (entity instanceof Player) {
            entity.startUsingItem(InteractionHand.MAIN_HAND);
            return true;
        }
        return super.onEntitySwing(itemStack, entity);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity entity) {
        return true;
    }

    // ========== Start ==========
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!CreatureManager.getInstance().config.isSummoningAllowed(world)) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt != null) {
            // Summon Selected Mob:
            SummonSet summonSet = playerExt.getSelectedSummonSet();
            if (summonSet.isUseable()) {
                if (!player.getCommandSenderWorld().isClientSide) {
                    playerExt.staffPortal = new PortalEntity((EntityType<? extends PortalEntity>) ProjectileManager.getInstance().oldProjectileTypes.get(PortalEntity.class), world, player, summonSet, this);
                    playerExt.staffPortal.moveTo(player.position().x(), player.position().y(), player.position().z(), world.random.nextFloat() * 360.0F, 0.0F);
                    world.addFreshEntity(playerExt.staffPortal);
                }
            }
            // Open Minion GUI If None Selected:
            else {
                playerExt.staffPortal = null;
                if (!player.getCommandSenderWorld().isClientSide())
                    playerExt.sendAllSummonSetsToPlayer();
                else
                    ClientManager.getInstance().displayGuiScreen("beastiary", player);
            }
        }
        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }


    // ========== Using ==========
    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack itemStack, int useRemaining) {
        if (!user.isUsingItem()) {
            return;
        }
        int useTime = this.getUseDuration(itemStack) - useRemaining;
        if (useTime >= this.getRapidTime(itemStack)) {
            int rapidRemainder = useTime % this.getRapidTime(itemStack);
            if (rapidRemainder == 0) {
                if (this.rapidAttack(itemStack, user.getCommandSenderWorld(), user)) {
                    this.weaponFlash = Math.max(20, this.getRapidTime(itemStack));
                }
            }
        }
        if (useTime >= this.getChargeTime(itemStack))
            this.weaponFlash = Math.max(20, this.getChargeTime(itemStack));

        super.onUseTick(level, user, itemStack, useRemaining);
    }

    // ========== Stop ==========
    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        int useTime = this.getUseDuration(stack) - timeLeft;
        float power = (float) useTime / (float) this.getChargeTime(stack);

        this.weaponFlash = 0;

        if ((double) power < 0.1D)
            return;
        if (power > 1.0F)
            power = 1.0F;

        if (this.chargedAttack(stack, worldIn, entityLiving, power)) {
            this.damageItemCharged(stack, entityLiving, power);
            this.weaponFlash = Math.min(20, this.getChargeTime(stack));
        }

        ExtendedPlayer playerExt = null;
        if (entityLiving instanceof Player) {
            playerExt = ExtendedPlayer.getForPlayer((Player) entityLiving);
        }
        if (playerExt != null) {
            playerExt.staffPortal = null;
        }
    }

    // ========== Animation ==========
    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BOW;
    }

    // ========== Max Use Duration ==========
    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }

    // ========== Charge Time ==========
    public int getChargeTime(ItemStack itemStack) {
        return 1;
    }

    // ========== Rapid Time ==========
    public int getRapidTime(ItemStack itemStack) {
        return 20;
    }

    // ========== Summon Cost ==========
    public int getSummonCostBoost() {
        return 0;
    }

    public float getSummonCostMod() {
        return 1.0F;
    }

    // ========== Summon Duration ==========
    public int getSummonDuration() {
        return 60 * 20;
    }

    // ========== Summon Amount ==========
    public int getSummonAmount() {
        return 1;
    }

    // ========== Additional Costs ==========
    public boolean getAdditionalCosts(Player player) {
        return true;
    }

    // ========== Minion Behaviour ==========
    public void applyMinionBehaviour(TameableCreatureEntity minion, Player player) {
        SummonSet summonSet = ExtendedPlayer.getForPlayer(player).getSelectedSummonSet();
        summonSet.applyBehaviour(minion);
        minion.setSubspecies(summonSet.subspecies);
        minion.applyVariant(summonSet.variant);
    }

    // ========== Minion Effects ==========
    public void applyMinionEffects(BaseCreatureEntity minion) {
    }


    // ==================================================
    //                      Attack
    // ==================================================
    // ========== Rapid ==========
    public boolean rapidAttack(ItemStack itemStack, Level world, LivingEntity entity) {
        return false;
    }

    protected void damageStaff(ItemStack itemStack, int amountToDamage, ServerPlayer entity) {
        itemStack.hurt(amountToDamage, entity.getRandom(), entity);
        if (itemStack.getCount() == 0) {
            if (entity.getItemInHand(InteractionHand.MAIN_HAND).equals(itemStack)) {
                entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            } else if (entity.getItemInHand(InteractionHand.OFF_HAND).equals(itemStack)) {
                entity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }
    }

    // ========== Charged ==========
    public boolean chargedAttack(ItemStack itemStack, Level world, LivingEntity entity, float power) {
        ExtendedPlayer playerExt = null;
        if (entity instanceof Player) {
            playerExt = ExtendedPlayer.getForPlayer((Player) entity);
        }
        if (playerExt != null && playerExt.staffPortal != null) {
            int successCount = playerExt.staffPortal.summonCreatures();
            this.damageStaff(itemStack, successCount, (ServerPlayer) entity);
            return successCount > 0;
        }
        return false;
    }


    // ==================================================
    //                     Repairs
    // ==================================================
    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairStack) {
        if (repairStack.getItem() == Items.GOLD_INGOT) return true;
        return super.isValidRepairItem(itemStack, repairStack);
    }
}
