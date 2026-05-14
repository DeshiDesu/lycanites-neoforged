package com.lycanitesmobs.core.item.consumable.entity;

import com.lycanitesmobs.core.entity.dispenser.BaseProjectileDispenseBehaviour;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.item.base.BaseItem;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.lycanitesmobs.core.tabs.LMChargesGroup.chargeNames;

public class ChargeItem extends BaseItem {
    /**
     * How much experience a Charge Item grants per element matched.
     **/
    public static int CHARGE_EXPERIENCE = 50;

    /**
     * The projectile info that this projectile charge item belongs to.
     **/
    public ProjectileInfo projectileInfo;

    /**
     * Constructor
     *
     * @param projectileInfo The projectile info to base this charge off.
     */
    public ChargeItem(Item.Properties properties, ProjectileInfo projectileInfo) {
        super(properties);
        this.projectileInfo = projectileInfo;
        if (this.projectileInfo != null) {
            this.itemName = projectileInfo.chargeItemName;
            LMHelperClass.logDebug("Projectile", "Created Charge Item: " + projectileInfo.chargeItemName);
        }
        // Dispenser:
        var dispenserBehaviour = new BaseProjectileDispenseBehaviour(projectileInfo);
        DispenserBlock.registerBehavior(this, dispenserBehaviour);
        setup();
        chargeNames.add(this.itemName);
    }

    @Override
    public MutableComponent getName(ItemStack itemStack) {
        return this.getProjectileName().copy().append(" ").append(Component.translatable("item.lycanitesmobs.charge"));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        for (MutableComponent description : this.getAdditionalDescriptions(stack, worldIn, flag)) {
            tooltip.add(description);
        }
    }

    @Override
    public Component getDescription(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        return Component.translatable("item.lycanitesmobs.charge.description").withStyle(ChatFormatting.GREEN);

    }

    public List<MutableComponent> getAdditionalDescriptions(ItemStack itemStack, @Nullable Level world, TooltipFlag tooltipFlag) {
        List<MutableComponent> descriptions = new ArrayList<>();

        descriptions.add(Component.translatable("item.lycanitesmobs.charge.projectile").withStyle(ChatFormatting.GOLD)
                .append(" ").append(this.getProjectileName()));

        if (!this.getElements().isEmpty()) {
            descriptions.add(Component.translatable("item.lycanitesmobs.charge.elements").withStyle(ChatFormatting.DARK_AQUA)
                    .append(" ").append(this.getElementNames()));
        }

        return descriptions;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide && player.isShiftKeyDown()) { // isSneaking()
            BaseProjectileEntity projectile = this.createProjectile(itemStack, world, player);
            if (projectile == null) {
                LMHelperClass.logWarning("", "Failed to create projectile from Charge Item: " + this.itemName);
                return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
            }
            world.addFreshEntity(projectile);
            if (!player.getAbilities().instabuild) {
                itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
            }
            this.playSound(world, player.blockPosition(), projectile.getLaunchSound(), SoundSource.NEUTRAL, 0.5F, 0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof TameableCreatureEntity && ((TameableCreatureEntity) entity).getPlayerOwner() == player) {
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }

    /**
     * Creates a projectile instance from this charge item.
     *
     * @param itemStack    The charge itemstack.
     * @param world        The world to create the projectile in.
     * @param entityPlayer The player using the charge.
     * @return A projectile instance.
     */
    public BaseProjectileEntity createProjectile(ItemStack itemStack, Level world, Player entityPlayer) {
        if (this.projectileInfo != null) {
            return this.projectileInfo.createProjectile(world, entityPlayer);
        }
        return null;
    }

    /**
     * Gets the Elements of this Charge.
     *
     * @return A list of Elements that this Charge contains.
     */
    public List<ElementInfo> getElements() {
        if (this.projectileInfo == null) {
            return new ArrayList<>();
        }
        return this.projectileInfo.elements;
    }

    /**
     * Returns a comma separated list of Elements this Charge contains.
     *
     * @return The Elements this Charge contains.
     */
    public MutableComponent getElementNames() {
        MutableComponent elementNames = Component.literal("");
        boolean firstElement = true;
        for (ElementInfo element : this.getElements()) {
            if (!firstElement) {
                elementNames.append(", ");
            }
            firstElement = false;
            elementNames.append(element.getTitle());
        }
        return elementNames;
    }

    /**
     * Returns the display name of the projectile fired by this Charge.
     *
     * @return The Projectile this Charge fires.
     */
    public Component getProjectileName() {
        if (this.projectileInfo != null) {
            return this.projectileInfo.getTitle();
        }
        return Component.translatable("item.lycanitesmobs.charge");
    }
}
