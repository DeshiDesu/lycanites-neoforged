package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.core.block.liquid.BaseLiquidBlock;

import java.util.HashMap;
import java.util.Map;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.liquid.AcidLiquidBlock;
import com.lycanitesmobs.core.block.liquid.MoglavaLiquidBlock;
import com.lycanitesmobs.core.block.liquid.OozeLiquidBlock;
import com.lycanitesmobs.core.block.liquid.PoisonLiquidBlock;
import com.lycanitesmobs.core.block.liquid.VeshoneyLiquidBlock;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.block.fluid.CustomFluid;
import com.lycanitesmobs.core.block.fluid.type.BaseFluidType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.joml.Vector3f;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class FluidManager {

    public static FluidManager INSTANCE;

    public static FluidManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FluidManager();
        }
        return INSTANCE;
    }

    public Map<String, BaseLiquidBlock> worldgenFluidBlocks = new HashMap<>();

    public void defineFluids() {
        Block.Properties waterBlockProperties = Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable().replaceable();
        Block.Properties waterBrightBlockProperties = Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable().lightLevel(s -> 10).replaceable();
        Block.Properties lavaBlockProperties = Block.Properties.of().mapColor(MapColor.FIRE).noCollission().randomTicks().strength(100).noLootTable().lightLevel(s -> 15).replaceable();

        addFluid("ooze", 0x009F9F, 3000, 3000, 0, 10, false, OozeLiquidBlock.class, waterBrightBlockProperties, "frost", false, true);
        addFluid("rabbitooze", 0x00AFAF, 3000, 3000, 0, 10, true, OozeLiquidBlock.class, waterBrightBlockProperties, "frost", false, false);
        addFluid("moglava", 0xFF5722, 3000, 5000, 1100, 15, true, MoglavaLiquidBlock.class, lavaBlockProperties, "lava", false, true);
        addFluid("acid", 0x8BC34A, 1000, 10, 40, 10, false, AcidLiquidBlock.class, waterBrightBlockProperties, "acid", true, true);
        addFluid("sharacid", 0x8BB35A, 1000, 10, 40, 10, true, AcidLiquidBlock.class, waterBrightBlockProperties, "acid", false, false);
        addFluid("poison", 0x9C27B0, 1000, 8, 20, 0, false, PoisonLiquidBlock.class, waterBlockProperties, "poison", false, true);
        addFluid("vesspoison", 0xAC27A0, 1000, 8, 20, 0, true, PoisonLiquidBlock.class, waterBlockProperties, "poison", false, false);
        addFluid("veshoney", 0xCEBC39, 4000, 4000, 0, 0, false, VeshoneyLiquidBlock.class, waterBlockProperties, "fae", false, false);
    }

    public void addFluid(String fluidName, int fluidColor, int density, int viscosity, int temperature, int luminosity, boolean multiply, Class<? extends BaseLiquidBlock> blockClass, BlockBehaviour.Properties blockProperties, String elementName, boolean destroyItems, boolean worldgen) {
        ElementInfo element = ElementManager.getInstance().getElement(elementName);
        Vector3f fogColor = new Vector3f(((fluidColor >> 16) & 0xFF) / 255f, ((fluidColor >> 8) & 0xFF) / 255f, (fluidColor & 0xFF) / 255f);
        int tickRate = (viscosity >= 4000) ? 30 : 5;

        FluidBuilder builder = new FluidBuilder(fluidName, new ResourceLocation(LycanitesMobs.MODID, fluidName));
        FluidType fluidType = builder.createFluidType(
                new ResourceLocation(LycanitesMobs.MODID, "block/" + fluidName + "_still"),
                new ResourceLocation(LycanitesMobs.MODID, "block/" + fluidName + "_flowing"),
                null,
                fluidColor, fogColor, density, viscosity, temperature, luminosity, multiply
        );
        ForgeFlowingFluid.Properties fluidProps = builder.fluidProperties(fluidType, tickRate, () -> ObjectManager.getFluidBlock(fluidName));

        ObjectManager.addFluid(fluidName, new CustomFluid.Still(fluidProps, fluidName));
        ObjectManager.addFluid(fluidName + "_flowing", new CustomFluid.Flowing(fluidProps, fluidName + "_flowing"));
        ObjectManager.addSound(fluidName, "block." + fluidName);

        Item.Properties bucketProps = new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);
        ObjectManager.addItem(fluidName + "_bucket", () -> new BucketItem(builder::getStillFluid, bucketProps));

        ObjectManager.addBlock(fluidName, () -> {
            try {
                Supplier<ForgeFlowingFluid> still = builder::getStillFluid;
                Constructor<? extends BaseLiquidBlock> ctor = blockClass.getConstructor(Supplier.class, BlockBehaviour.Properties.class, String.class, ElementInfo.class, boolean.class);
                BaseLiquidBlock block = ctor.newInstance(still, blockProperties, fluidName, element, destroyItems);
                if (worldgen) worldgenFluidBlocks.put(fluidName, block);
                return block;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, true);
    }

    public static class FluidBuilder {
        private final String baseName;
        private final ResourceLocation registryName;
        private FluidType fluidType;
        private ForgeFlowingFluid.Properties fluidProperties;

        public FluidBuilder(String fluidName, ResourceLocation registryLocation) {
            this.baseName = fluidName;
            this.registryName = registryLocation;
        }

        public FluidType createFluidType(ResourceLocation stillTexture, ResourceLocation flowingTexture, ResourceLocation overlayTexture,
                                         int tintColor, Vector3f fogColor, int fluidDensity, int fluidViscosity, int fluidTemperature, int fluidLuminosity,
                                         boolean fluidMultiply) {
            FluidType.Properties props = FluidType.Properties.create();
            props.density(fluidDensity);
            props.viscosity(fluidViscosity);
            props.temperature(fluidTemperature);
            props.lightLevel(fluidLuminosity);
            if (fluidMultiply) props.canConvertToSource(true);
            fluidType = new BaseFluidType(registryName, stillTexture, flowingTexture, overlayTexture, tintColor, fogColor, props);
            return fluidType;
        }

        public ForgeFlowingFluid.Properties fluidProperties(FluidType fT, int fluidTickRate, Supplier<? extends LiquidBlock> blockSupplier) {
            ForgeFlowingFluid.Properties p = new ForgeFlowingFluid.Properties(() -> fT, this::getStillFluid, this::getFlowingFluid);
            p.tickRate(fluidTickRate);
            p.bucket(this::getBucketItem);
            p.block(blockSupplier);
            this.fluidProperties = p;
            return p;
        }

        public ForgeFlowingFluid getStillFluid() {
            return ObjectManager.getFluid(baseName);
        }

        public ForgeFlowingFluid getFlowingFluid() {
            return ObjectManager.getFluid(baseName + "_flowing");
        }

        public BucketItem getBucketItem() {
            return (BucketItem) ObjectManager.getItem(baseName + "_bucket");
        }

        public ForgeFlowingFluid.Properties getFluidProperties() {
            return fluidProperties;
        }

        public FluidType getFluidType() {
            return fluidType;
        }
    }
}
