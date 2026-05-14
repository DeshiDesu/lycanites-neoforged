package com.lycanitesmobs.core.item.equipment.features;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.item.tool.ToolType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.IForgeShearable;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class HarvestEquipmentFeature extends EquipmentFeature {
    /**
     * List of blocks by harvest type. These are checked after Materials are checked.
     **/
    private static final Set<Block> SPADE_HARVEST = Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS_BLOCK, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SOUL_SAND, Blocks.DIRT_PATH, Blocks.GRAY_CONCRETE_POWDER);
    private static final Set<Block> PICKAXE_HARVEST = Sets.newHashSet(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.POWERED_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE);
    private static final Set<Block> AXE_HARVEST = Sets.newHashSet(Blocks.OAK_PLANKS, Blocks.BOOKSHELF, Blocks.CHEST, Blocks.PUMPKIN, Blocks.MELON, Blocks.LADDER, Blocks.OAK_BUTTON, Blocks.OAK_PRESSURE_PLATE);
    private static final Map<Block, BlockState> HOE_LOOKUP = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT_PATH, Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT, Blocks.FARMLAND.defaultBlockState(), Blocks.COARSE_DIRT, Blocks.DIRT.defaultBlockState()));

    /**
     * The type of tool to harvest as. Can be: pickaxe, axe, shovel, hoe, sword or shears.
     **/
    public String harvestType;

    /**
     * The shape of the harvest. Can be block, cross or random.
     **/
    public String harvestShape = "block";

    /**
     * How much harvest speed to add when harvesting compatible blocks.
     **/
    public float harvestSpeed = 1;

    /**
     * The level of harvesting. -1 = Unable, 0 = Wood, 1 = Stone, 2 = Iron, 3 = Diamond.
     **/
    public int harvestLevel = 3;

    /**
     * The additional block range of the harvest shape, relative to the harvesting direction, the central block is not affected by this. X = number of blocks both sides laterally (sideways). Y = Number of blocks vertically. Z = Number of blocks forwards.
     **/
    public Vec3i harvestRange = new Vec3i(0, 0, 0);


    // ==================================================
    //                        JSON
    // ==================================================
    @Override
    public void loadFromJSON(JsonObject json) {
        super.loadFromJSON(json);

        this.harvestType = json.get("harvestType").getAsString();

        if (json.has("harvestType"))
            this.harvestType = json.get("harvestType").getAsString();

        if (json.has("harvestSpeed"))
            this.harvestSpeed = json.get("harvestSpeed").getAsFloat();

        if (json.has("harvestLevel"))
            this.harvestLevel = json.get("harvestLevel").getAsInt();

        if (json.has("harvestShape"))
            this.harvestShape = json.get("harvestShape").getAsString();

        this.harvestRange = JSONHelper.getVector3i(json, "harvestRange");
    }

    @Override
    public MutableComponent getDescription(ItemStack itemStack, int level) {
        if (!this.isActive(itemStack, level)) {
            return null;
        }
        return Component.translatable("equipment.feature." + this.featureType).append(" ")
                .append(this.getSummary(itemStack, level));
    }

    @Override
    public MutableComponent getSummary(ItemStack itemStack, int level) {
        if (!this.isActive(itemStack, level)) {
            return null;
        }
        MutableComponent summary = Component.translatable("equipment.harvest.type." + this.harvestType);
        if (this.harvestRange.distSqr(new Vec3i(0, 0, 0)) > 0) {
            summary.append(" (").append(Component.translatable("equipment.harvest.shape." + this.harvestShape));
            summary.append(" " + this.getHarvestRangeString(level)).append(")");
        }
        return summary;
    }

    public String getHarvestRangeString(int level) {
        String harvestRangeString = "" + ((this.harvestRange.getX()) * 2 + 1);
        harvestRangeString += "x" + (this.harvestRange.getY() + 1);
        harvestRangeString += "x" + (this.harvestRange.getZ() + 1);
        return harvestRangeString;
    }


    // ==================================================
    //                     Harvesting
    // ==================================================

    /**
     * Gets the Tool Type provided by this Harvest Feature.
     *
     * @return The Tool Type of this feature, will return null if it's not a Pickaxe, Axe or Shovel.
     */
    @Nullable
    public ToolType getToolType() {
        return ToolType.get(this.harvestType);
    }

    /**
     * Returns if this feature can harvest the provided block or not.
     *
     * @param blockState The blockstate to check.
     * @return True if the block can be destroyed by this feature.
     */
    public boolean canHarvestBlock(BlockState blockState) {
        Block block = blockState.getBlock();
        MapColor material = blockState.getBlock().defaultMapColor();
        // Stone:
        if (material == MapColor.METAL || material == MapColor.STONE || PICKAXE_HARVEST.contains(block)) {
            return this.harvestType.equalsIgnoreCase("pickaxe");
        }

        // Wood:
        if (material == MapColor.WOOD || AXE_HARVEST.contains(block)) {
            return this.harvestType.equalsIgnoreCase("axe");
        }

        // Plants:
        if (LMHelperClass.Materials.isPlant(block)) {
            return this.harvestType.equalsIgnoreCase("axe") || this.harvestType.equalsIgnoreCase("sword") || this.harvestType.equalsIgnoreCase("shears");
        }

        // Web and Leaves:
        if (block == Blocks.COBWEB || LMHelperClass.Materials.isLeaves(block) || LMHelperClass.Materials.isUnderwaterPlant(block)) {
            return this.harvestType.equalsIgnoreCase("sword") || this.harvestType.equalsIgnoreCase("shears");
        }

        // Dirt:
        if (LMHelperClass.Materials.isDirt(block) || block == Blocks.CLAY || block == Blocks.SAND || block == Blocks.GRASS || block == Blocks.SNOW_BLOCK || block == Blocks.SNOW || SPADE_HARVEST.contains(block)) {
            return this.harvestType.equalsIgnoreCase("shovel");
        }

        // Growth:
        if (LMHelperClass.Materials.isCoral(block) || LMHelperClass.Materials.isCrop(block)) {
            return this.harvestType.equalsIgnoreCase("sword");
        }

        // Wire:
        if (block == Blocks.TRIPWIRE) {
            return this.harvestType.equalsIgnoreCase("shears");
        }

        return false;
    }


    /**
     * Returns the speed that this feature adds to harvesting the provided block.
     *
     * @param blockState The block to harvest.
     * @return The harvest speed to add (all harvest features have their speed added together).
     */
    public float getHarvestSpeed(BlockState blockState) {
        if (!this.canHarvestBlock(blockState)) {
            return 0;
        }

        return this.harvestSpeed * this.getHarvestMultiplier(blockState);
    }


    /**
     * Returns a harvest speed multiplier for the provided block.
     *
     * @param blockState The block to check.
     * @return A harvest speed multiplier.
     */
    public float getHarvestMultiplier(BlockState blockState) {
        Block material = blockState.getBlock();

        // Web:
        if (material == Blocks.COBWEB) {
            return 10;
        }

        // Shears:
        if ((LMHelperClass.Materials.isLeaves(material) || LMHelperClass.Materials.isReplaceablePlant(material)) && this.harvestType.equalsIgnoreCase("shears")) {
            return 10;
        }

        return 1;
    }


    /**
     * Called when a block is destroyed by Equipment with this Feature.
     *
     * @param world               The world where the block was destroyed.
     * @param harvestedBlockState The block state that was destroyed.
     * @param harvestedPos        The position of the destroyed block.
     * @param livingEntity        The entity that destroyed the block.
     */
    public void onBlockDestroyed(Level world, BlockState harvestedBlockState, BlockPos harvestedPos, LivingEntity livingEntity) {
        if (livingEntity == null || livingEntity.isShiftKeyDown()) {
            return;
        }

        // Get Facing:
        Direction facingH = livingEntity.getDirection();
        Direction facingLat = facingH.getClockWise();
        Direction facing = facingH;
        if (livingEntity.xRotO > 45) {
            facing = Direction.DOWN;
        } else if (livingEntity.xRotO < -45) {
            facing = Direction.UP;
        }
        Vec3i[][] selectionRanges = new Vec3i[3][2];
        int lon = 0;
        int lat = 1;
        int vert = 2;
        int min = 0;
        int max = 1;

        // Get Longitudinal (Z):
        selectionRanges[lon][min] = new Vec3i(
                Math.min(0, this.harvestRange.getZ() * facing.getStepX()),
                Math.min(0, this.harvestRange.getZ() * facing.getStepY()),
                Math.min(0, this.harvestRange.getZ() * facing.getStepZ())
        );
        selectionRanges[lon][max] = new Vec3i(
                Math.max(0, this.harvestRange.getZ() * facing.getStepX()),
                Math.max(0, this.harvestRange.getZ() * facing.getStepY()),
                Math.max(0, this.harvestRange.getZ() * facing.getStepZ())
        );

        // Get Lateral (X):
        selectionRanges[lat][min] = new Vec3i(
                this.harvestRange.getX() * -Math.abs(facingLat.getStepX()),
                this.harvestRange.getX() * -Math.abs(facingLat.getStepY()),
                this.harvestRange.getX() * -Math.abs(facingLat.getStepZ())
        );
        selectionRanges[lat][max] = new Vec3i(
                this.harvestRange.getX() * Math.abs(facingLat.getStepX()),
                this.harvestRange.getX() * Math.abs(facingLat.getStepY()),
                this.harvestRange.getX() * Math.abs(facingLat.getStepZ())
        );

        // Get Vertical (Y):
        if (facing != Direction.DOWN && facing != Direction.UP) {
            int vertOffset = this.harvestRange.getY() != 0 ? -1 : 0;
            selectionRanges[vert][min] = new Vec3i(0, vertOffset, 0);
            selectionRanges[vert][max] = new Vec3i(0, this.harvestRange.getY() + vertOffset, 0);
        } else {
            selectionRanges[vert][min] = LMHelperClass.convertToVec3i(new Vector3f(
                    this.harvestRange.getY() * -Math.abs(facingH.getStepX()) * 0.5F,
                    this.harvestRange.getY() * -Math.abs(facingH.getStepY()) * 0.5F,
                    this.harvestRange.getY() * -Math.abs(facingH.getStepZ()) * 0.5F
            ));
            selectionRanges[vert][max] = LMHelperClass.convertToVec3i(new Vector3f(
                    this.harvestRange.getY() * Math.abs(facingH.getStepX()) * 0.5F,
                    this.harvestRange.getY() * Math.abs(facingH.getStepY()) * 0.5F,
                    this.harvestRange.getY() * Math.abs(facingH.getStepZ()) * 0.5F
            ));
        }

        // Block and Random Area Harvesting:
        if (this.harvestShape.equalsIgnoreCase("block") || this.harvestShape.equalsIgnoreCase("random")) {
            boolean random = this.harvestShape.equalsIgnoreCase("random");

            // Longitude:
            for (int longX = selectionRanges[lon][min].getX(); longX <= selectionRanges[lon][max].getX(); longX++) {
                for (int longY = selectionRanges[lon][min].getY(); longY <= selectionRanges[lon][max].getY(); longY++) {
                    for (int longZ = selectionRanges[lon][min].getZ(); longZ <= selectionRanges[lon][max].getZ(); longZ++) {

                        // Latitude:
                        for (int latX = selectionRanges[lat][min].getX(); latX <= selectionRanges[lat][max].getX(); latX++) {
                            for (int latY = selectionRanges[lat][min].getY(); latY <= selectionRanges[lat][max].getY(); latY++) {
                                for (int latZ = selectionRanges[lat][min].getZ(); latZ <= selectionRanges[lat][max].getZ(); latZ++) {

                                    // Vertical:
                                    for (int vertX = selectionRanges[vert][min].getX(); vertX <= selectionRanges[vert][max].getX(); vertX++) {
                                        for (int vertY = selectionRanges[vert][min].getY(); vertY <= selectionRanges[vert][max].getY(); vertY++) {
                                            for (int vertZ = selectionRanges[vert][min].getZ(); vertZ <= selectionRanges[vert][max].getZ(); vertZ++) {

                                                BlockPos destroyPos = harvestedPos.offset(longX, longY, longZ).offset(latX, latY, latZ).offset(vertX, vertY, vertZ);
                                                if (this.shouldHarvestBlock(world, harvestedBlockState, harvestedPos, destroyPos) && (!random || world.random.nextBoolean())) {
                                                    world.destroyBlock(destroyPos, true);
                                                }

                                            }
                                        }
                                    }

                                }
                            }
                        }

                    }
                }
            }

            return;
        }

        // Cross Area Harvesting:
        if (this.harvestShape.equalsIgnoreCase("cross")) {

            // Longitude:
            for (int longX = selectionRanges[lon][min].getX(); longX <= selectionRanges[lon][max].getX(); longX++) {
                for (int longY = selectionRanges[lon][min].getY(); longY <= selectionRanges[lon][max].getY(); longY++) {
                    for (int longZ = selectionRanges[lon][min].getZ(); longZ <= selectionRanges[lon][max].getZ(); longZ++) {

                        // Latitude:
                        for (int latX = selectionRanges[lat][min].getX(); latX <= selectionRanges[lat][max].getX(); latX++) {
                            for (int latY = selectionRanges[lat][min].getY(); latY <= selectionRanges[lat][max].getY(); latY++) {
                                for (int latZ = selectionRanges[lat][min].getZ(); latZ <= selectionRanges[lat][max].getZ(); latZ++) {
                                    BlockPos destroyPos = harvestedPos.offset(longX, longY, longZ).offset(latX, latY, latZ);
                                    if (this.shouldHarvestBlock(world, harvestedBlockState, harvestedPos, destroyPos)) {
                                        world.destroyBlock(destroyPos, true);
                                    }
                                }
                            }
                        }

                        // Vertical:
                        for (int vertX = selectionRanges[vert][min].getX(); vertX <= selectionRanges[vert][max].getX(); vertX++) {
                            for (int vertY = selectionRanges[vert][min].getY(); vertY <= selectionRanges[vert][max].getY(); vertY++) {
                                for (int vertZ = selectionRanges[vert][min].getZ(); vertZ <= selectionRanges[vert][max].getZ(); vertZ++) {
                                    BlockPos destroyPos = harvestedPos.offset(longX, longY, longZ).offset(vertX, vertY, vertZ);
                                    if (this.shouldHarvestBlock(world, harvestedBlockState, harvestedPos, destroyPos)) {
                                        world.destroyBlock(destroyPos, true);
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * Returns if the target block position should be area harvested.
     *
     * @param world               The world to check in.
     * @param harvestedBlockState The initial block harvested to compare to.
     * @param harvestedPos        The initial block position that was harvested at.
     * @param targetPos           The target area harvesting position to check.
     * @return True if the block should be area harvested.
     */
    public boolean shouldHarvestBlock(Level world, BlockState harvestedBlockState, BlockPos harvestedPos, BlockPos targetPos) {
        if (harvestedPos.equals(targetPos)) {
            return false;
        }
        BlockState targetBlockState = world.getBlockState(targetPos);
        if (targetBlockState.getBlock() != harvestedBlockState.getBlock()) {
            return false;
        }

        // Don't area harvest Block Entities.
        if (world.getBlockEntity(targetPos) != null) {
            return false;
        }

        return this.canHarvestBlock(world.getBlockState(targetPos));
    }

    /**
     * Called when a player right clicks on a block.
     *
     * @param context The item use context.
     */
    public boolean onBlockUsed(UseOnContext context) {
        if (!"hoe".equals(this.harvestType)) {
            return false;
        }

		/*int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(context);
		if (hook != 0) return false;*/
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        if (context.getClickedFace() != Direction.DOWN && world.isEmptyBlock(blockPos.above())) {
            BlockState blockstate = HOE_LOOKUP.get(world.getBlockState(blockPos).getBlock());
            if (blockstate != null) {
                Player playerentity = context.getPlayer();
                world.playSound(playerentity, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!world.isClientSide) {
                    world.setBlock(blockPos, blockstate, 11);
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Called when a player right clicks on an entity.
     *
     * @param player    The player using the equipment.
     * @param entity    The entity the player is using the equipment on.
     * @param itemStack The equipment itemstack.
     */
    public boolean onEntityInteraction(Player player, LivingEntity entity, ItemStack itemStack) {
        if (!"shears".equals(this.harvestType) || player.getCommandSenderWorld().isClientSide) {
            return false;
        }

        if (entity instanceof IForgeShearable) {
            IForgeShearable target = (IForgeShearable) entity;
            BlockPos pos = new BlockPos((int) Math.floor(entity.getX()), (int) Math.floor(entity.getY()), (int) Math.floor(entity.getZ()));
            if (target.isShearable(itemStack, entity.level(), pos)) {
                List<ItemStack> drops = target.onSheared(player, itemStack, entity.level(), pos,
                        EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, itemStack));
                Random rand = new Random();
                drops.forEach(d -> {
                    ItemEntity ent = entity.spawnAtLocation(d, 1.0F);
                    ent.setDeltaMovement(ent.getDeltaMovement().add((rand.nextFloat() - rand.nextFloat()) * 0.1F, rand.nextFloat() * 0.05F, (rand.nextFloat() - rand.nextFloat()) * 0.1F));
                });
            }
            return true;
        }

        return false;
    }
}
