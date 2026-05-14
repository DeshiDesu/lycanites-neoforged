package com.lycanitesmobs.core.item.consumable.entity;

import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.entity.dispenser.SpawnEggDispenseBehaviour;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.creature.CreatureType;
import com.lycanitesmobs.core.item.base.CreatureTypeItem;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCustomSpawnEgg extends CreatureTypeItem {

    public ItemCustomSpawnEgg(Item.Properties properties, CreatureType creatureType) {
        super(properties, creatureType.getSpawnEggName(), creatureType);
        //this.setRegistryName(this.modInfo.modid, this.itemName);
        DispenserBlock.registerBehavior(this, new SpawnEggDispenseBehaviour());
        LMHelperClass.logDebug("Creature Type", "Created Creature Type Spawn Egg: " + this.itemName);
    }

    @Override
    public MutableComponent getName(ItemStack itemStack) {
        MutableComponent displayName = Component.translatable("creaturetype.spawn")
                .append(" ")
                .append(this.creatureType.getTitle())
                .append(": ");
        CreatureInfo creatureInfo = this.getCreatureInfo(itemStack);
        if (creatureInfo != null)
            displayName.append(creatureInfo.getTitle());
        else
            displayName.append("Missing Creature NBT");
        return displayName;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        Component description = this.getDescription(stack, worldIn, tooltip, flag);
        if (!"".equalsIgnoreCase(description.getString()) && !("item." + this.itemName + ".description").equals(description.getContents())) {
            tooltip.add(description);
        }
    }

    @Override
    public Component getDescription(ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        CreatureInfo creatureInfo = this.getCreatureInfo(itemStack);
        if (creatureInfo == null) {
            String creatureName = this.getCreatureName(itemStack);
            LMHelperClass.logWarning("Mob Spawn Egg", "Unable to get Creature Info for id: " + creatureName);
            return Component.translatable("Unable to get Creature Info for id: '" + creatureName + "' this spawn egg may have been created by a give command without NBT data.");
        }
        return creatureInfo.getDescription().plainCopy().withStyle(ChatFormatting.GREEN);
    }

    // TODO: Create creative tabs
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        for (CreatureInfo creatureInfo : this.creatureType.creatures.values()) {
            ItemStack itemstack = new ItemStack(this, 1);
            this.applyCreatureInfoToItemStack(itemstack, creatureInfo);
            items.add(itemstack);
        }
    }

    /**
     * Applies creature info to a spawn egg item stack.
     *
     * @param itemStack    The spawn egg item stack top apply to.
     * @param creatureInfo The creature info to apply.
     */
    public void applyCreatureInfoToItemStack(ItemStack itemStack, CreatureInfo creatureInfo) {
        CompoundTag itemStackNBT = itemStack.hasTag() ? itemStack.getTag() : new CompoundTag();
        CompoundTag spawnEggNBT = new CompoundTag();
        spawnEggNBT.putString("creaturename", creatureInfo.getName());
        itemStackNBT.put("CreatureInfoSpawnEgg", spawnEggNBT);
        itemStack.setTag(itemStackNBT);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!player.mayUseItemAt(pos.relative(context.getClickedFace()), context.getClickedFace(), itemStack)) {
            return InteractionResult.FAIL;
        }

        // Edit Spawner:
        if (block == Blocks.SPAWNER) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            BaseSpawner mobspawnerbaselogic = ((SpawnerBlockEntity) tileEntity).getSpawner();
            RandomSource randomsource = world.getRandom();
            mobspawnerbaselogic.setEntityId(this.getCreatureInfo(itemStack).getEntityType(), world, randomsource, player.blockPosition());
            tileEntity.setChanged();
            world.sendBlockUpdated(pos, blockState, blockState, 3);
            if (!player.getAbilities().instabuild) {
                itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
            }

            return InteractionResult.SUCCESS;
        }

        // Spawn Mob:
        pos = pos.relative(context.getClickedFace());
        double d0 = 0.0D;
        if (context.getClickedFace() == Direction.UP && blockState.getBlock() instanceof FenceBlock) {
            d0 = 0.5D;
        }

        LivingEntity entity = this.spawnCreature(world, itemStack, (double) pos.getX() + 0.5D, (double) pos.getY() + d0, (double) pos.getZ() + 0.5D);
        if (entity != null) {
            if (itemStack.hasCustomHoverName()) {
                entity.setCustomName(itemStack.getHoverName());
            }
            if (!player.getAbilities().instabuild) {
                itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (world.isClientSide)
            return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
        else {
            HitResult rayTraceResult = this.getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);

            if (rayTraceResult.getType() != HitResult.Type.BLOCK)
                return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);

            BlockHitResult blockRayTraceResult = (BlockHitResult) rayTraceResult;
            BlockPos pos = blockRayTraceResult.getBlockPos();

            if (!world.mayInteract(player, pos)) {
                return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
            }

            if (!player.mayUseItemAt(pos, blockRayTraceResult.getDirection(), itemStack)) {
                return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
            }

            if (world.getBlockState(pos).getBlock() == Blocks.WATER) {
                LivingEntity entity = spawnCreature(world, itemStack, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
                if (entity != null)
                    if (itemStack.hasCustomHoverName()) {
                        entity.setCustomName(itemStack.getHoverName());
                    }
                if (!player.getAbilities().instabuild) {
                    itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
                }
            }

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
        }
    }

    /**
     * Get Creature Info
     *
     * @param itemStack The spawn egg item stack to get the creature from.
     * @return The Creature Info of the stack spawn egg or null if unknown.
     */
    public CreatureInfo getCreatureInfo(ItemStack itemStack) {
        String creatureName = this.getCreatureName(itemStack);
        return CreatureManager.getInstance().getCreature(creatureName);
    }

    /**
     * Get Creature Name
     *
     * @param itemStack The spawn egg item stack to get the creature name from.
     * @return The name of the creature that the spawn egg item stack should spawn.
     */
    public String getCreatureName(ItemStack itemStack) {
        CompoundTag itemStackNBT = itemStack.getTag();
        if (itemStackNBT == null || !itemStackNBT.contains("CreatureInfoSpawnEgg", 10)) {
            return null;
        }
        CompoundTag spawnEggNBT = itemStackNBT.getCompound("CreatureInfoSpawnEgg");
        return !spawnEggNBT.contains("creaturename", 8) ? null : spawnEggNBT.getString("creaturename");
    }

    /**
     * Spawn Creature
     *
     * @param world     The world to spawn in.
     * @param itemStack The spawn egg itemstack to spawn from.
     * @param x         X spawn coordinate.
     * @param y         Y spawn coordinate.
     * @param z         Z spawn coordinate.
     * @return The spawned entity instance.
     */
    public LivingEntity spawnCreature(Level world, ItemStack itemStack, double x, double y, double z) {
        LivingEntity entity = this.getCreatureInfo(itemStack).createEntity(world);
        if (entity != null && world instanceof ServerLevelAccessor) {
            entity.moveTo(x, y, z, LMHelperClass.convertToFloat(LMHelperClass.wrapDegrees(world.random.nextFloat() * 360.0F)), 0.0F);
            entity.yHeadRot = entity.yRotO;
            entity.yBodyRot = entity.yRotO;

            if (itemStack.hasCustomHoverName()) {
                entity.setCustomName(itemStack.getHoverName());
            }

            if (entity instanceof Mob) {
                Mob mobEntity = (Mob) entity;
                mobEntity.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(mobEntity.blockPosition()), MobSpawnType.SPAWN_EGG, null, null);
                mobEntity.playAmbientSound();
            }

            if (world instanceof ServerLevel serverLevel) {
                DeferredLevelActionManager.enqueue(serverLevel, BlockPos.containing(x, y, z), "spawn_egg:" + entity.getUUID(), level -> level.addFreshEntity(entity));
            } else {
                world.addFreshEntity(entity);
            }
        }
        return entity;
    }
}
