package com.lycanitesmobs.core.entity.creature.aberration;

import com.google.common.collect.Maps;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.EatBlockGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.data.info.item.ItemDrop;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityYale extends AgeableCreatureEntity implements IForgeShearable {

	protected static final EntityDataAccessor<Byte> FUR = SynchedEntityData.defineId(EntityYale.class, EntityDataSerializers.BYTE);

	private static final Map<DyeColor, ItemLike> WOOL_BY_COLOR = Util.make(Maps.newEnumMap(DyeColor.class), (itemProviderMap) -> {
		itemProviderMap.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
		itemProviderMap.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
		itemProviderMap.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
		itemProviderMap.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
		itemProviderMap.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
		itemProviderMap.put(DyeColor.LIME, Blocks.LIME_WOOL);
		itemProviderMap.put(DyeColor.PINK, Blocks.PINK_WOOL);
		itemProviderMap.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
		itemProviderMap.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
		itemProviderMap.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
		itemProviderMap.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
		itemProviderMap.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
		itemProviderMap.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
		itemProviderMap.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
		itemProviderMap.put(DyeColor.RED, Blocks.RED_WOOL);
		itemProviderMap.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
	});
	private static final Map<DyeColor, float[]> DYE_TO_RGB = Maps.newEnumMap(Arrays.stream(DyeColor.values()).collect(Collectors.toMap((DyeColor p_200204_0_) -> {
		return p_200204_0_;
	}, EntityYale::createSheepColor)));

    protected ItemDrop woolDrop;


    public EntityYale(EntityType<? extends EntityYale> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = false;

        this.canGrow = true;
        this.babySpawnChance = 0.1D;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        this.setupMob();

		this.woolDrop = new ItemDrop(LMHelperClass.convertToResourceLocation(Blocks.WHITE_WOOL, world.registryAccess()).toString(), 1).setMinAmount(1).setMaxAmount(3);
    }

    @Override
    protected void registerGoals() {
		this.goalSelector.addGoal(this.nextIdleGoalIndex++, new EatBlockGoal(this).setBlocks(Blocks.GRASS_BLOCK).setReplaceBlock(Blocks.DIRT));
		super.registerGoals();
		this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FUR, (byte) 1);
    }

	@Override
	public void onFirstSpawn() {
		if(!this.isBaby())
			this.setColor(this.getRandomFurColor(this.getRandom()));
		super.onFirstSpawn();
	}

	@Override
	public boolean isShearable(@Nonnull ItemStack item, Level world, BlockPos pos) {
		return this.hasFur() && !this.isBaby();
	}

	@Override
	public ArrayList<ItemStack> onSheared(@Nullable Player player, @Nonnull ItemStack item, Level world, BlockPos pos, int fortune) {
		ArrayList<ItemStack> dropStacks = new ArrayList<>();
		if(this.woolDrop == null) {
			return dropStacks;
		}

		this.setFur(false);
		this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);

		int quantity = this.woolDrop.getQuantity(this.getRandom(), fortune, 1);
		ItemStack dropStack = this.woolDrop.getEntityDropItemStack(this, quantity);
		if(dropStack != null && dropStack.getItem() instanceof BlockItem && LMHelperClass.convertToResourceLocation(((BlockItem)dropStack.getItem()).getBlock(), world.registryAccess()).toString().contains("_wool")) {
			dropStack = new ItemStack(WOOL_BY_COLOR.get(this.getColor()), dropStack.getCount());
		}
		dropStacks.add(dropStack);
		
		return dropStacks;
	}

	public boolean hasFur() {
		if(this.entityData == null) return true;
		return this.entityData.get(FUR) > 0;
	}

	public void setFur(boolean fur) {
		if(!this.getCommandSenderWorld().isClientSide)
			this.entityData.set(FUR, (byte) (fur ? 1 : 0));
	}
	
	@Override
	public void onEat() {
		if(!this.getCommandSenderWorld().isClientSide)
			this.setFur(true);
	}
	
	@Override
	public boolean canBeColored(Player player) {
		return true;
	}
	
	@Override
	public void setColor(DyeColor color) {
		Item woolItem = WOOL_BY_COLOR.get(this.getColor()).asItem();
        if(this.woolDrop == null) {
			this.woolDrop = new ItemDrop(LMHelperClass.convertToResourceLocation(woolItem, this.level().registryAccess()).toString(), 1).setMinAmount(1).setMaxAmount(3);
		}
		else if(this.woolDrop.getItemStack().getItem() != woolItem) {
			this.woolDrop.setDrop(new ItemStack(woolItem, 1));
		}
		super.setColor(color);
	}

	public DyeColor getRandomFurColor(RandomSource random) {
		int i = random.nextInt(100);
		if (i < 5) {
			return DyeColor.BLACK;
		} else if (i < 10) {
			return DyeColor.GREEN;
		} else if (i < 15) {
			return DyeColor.RED;
		} else if (i < 18) {
			return DyeColor.BROWN;
		} else {
			return random.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
		}
	}
	
	/**
	 * Attempts to mix both parents to come up with a mixed dye color.
	 */
	private DyeColor getMixedFurColor(BaseCreatureEntity father, BaseCreatureEntity mother) {
		DyeColor dyeA = father.getColor();
		DyeColor dyeB = mother.getColor();
		TransientCraftingContainer craftinginventory = mixColors(dyeA, dyeB);
		return this.level().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftinginventory, this.level()).map((craftingRecipe) ->
				craftingRecipe.assemble(craftinginventory,level().registryAccess())).map(ItemStack::getItem).filter(DyeItem.class::isInstance).map(DyeItem.class::cast).map(DyeItem::getDyeColor).orElseGet(() ->
				this.level().random.nextBoolean() ? dyeA : dyeB);
	}

	private static TransientCraftingContainer mixColors(DyeColor dyeA, DyeColor dyeB) {
		TransientCraftingContainer craftinginventory = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
			@Override
			public ItemStack quickMoveStack(Player player, int i) {
				return ItemStack.EMPTY;
			}

			public boolean stillValid(Player playerIn) {
				return false;
			}
		}, 2, 1);
		craftinginventory.setItem(0, new ItemStack(DyeItem.byColor(dyeA)));
		craftinginventory.setItem(1, new ItemStack(DyeItem.byColor(dyeB)));
		return craftinginventory;
	}

	private static float[] createSheepColor(DyeColor p_192020_0_) {
		if (p_192020_0_ == DyeColor.WHITE) {
			return new float[]{0.9019608F, 0.9019608F, 0.9019608F};
		} else {
			float[] afloat = p_192020_0_.getTextureDiffuseColors();
			float f = 0.75F;
			return new float[]{afloat[0] * 0.75F, afloat[1] * 0.75F, afloat[2] * 0.75F};
		}
	}

    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if(block != Blocks.AIR) {
            if(Material.GRASS.contains(blockState.getBlock()))
                return 10F;
            if(Material.DIRT.contains(blockState.getBlock()))
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return true;
    }

	@Override
	public int getNoBagSize() { return 0; }

	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }

    @Override
    public float getFallResistance() {
    	return 50;
    }

	@Override
	public boolean canDropItem(ItemDrop itemDrop) {
		if(!super.canDropItem(itemDrop)) {
			return false;
		}
		if(itemDrop.getItemStack().getItem() instanceof BlockItem && LMHelperClass.convertToResourceLocation(((BlockItem)itemDrop.getItemStack().getItem()).getBlock(), this.level().registryAccess()).toString().contains("_wool")) {
			return this.hasFur();
		}
		return true;
	}

	@Override
	public void dropItem(ItemStack itemStack) {
		if(this.woolDrop != null && itemStack.getItem() instanceof BlockItem && LMHelperClass.convertToResourceLocation(((BlockItem)itemStack.getItem()).getBlock(), this.level().registryAccess()).toString().contains("_wool")) {
			itemStack = new ItemStack(WOOL_BY_COLOR.get(this.getColor()), itemStack.getCount());
		}
		super.dropItem(itemStack);
	}

	@Override
	public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
		AgeableCreatureEntity baby = super.createChild(partner);
		DyeColor color = this.getMixedFurColor(this, partner);
        baby.setColor(color);
		return baby;
	}

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
    	super.readAdditionalSaveData(nbt);
    	if(nbt.contains("HasFur")) {
    		this.setFur(nbt.getBoolean("HasFur"));
    	}
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
    	super.addAdditionalSaveData(nbt);
    	nbt.putBoolean("HasFur", this.hasFur());
    }
}
