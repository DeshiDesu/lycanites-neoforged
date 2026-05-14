package com.lycanitesmobs.core.item.consumable.holiday;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.item.CustomItemEntity;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.item.base.BaseItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class ItemWinterGift extends BaseItem {

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemWinterGift(Item.Properties properties) {
        super(properties);
        this.itemName = "wintergift";
        this.setup();
        ObjectManager.addSound(this.itemName + "_good", "item." + this.itemName + ".good");
        ObjectManager.addSound(this.itemName + "_bad", "item." + this.itemName + ".bad");
    }


    // ==================================================
    //                    Item Use
    // ==================================================
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
        }

        if (!world.isClientSide) {
            if (player.getRandom().nextBoolean())
                this.openGood(itemStack, world, player);
            else
                this.openBad(itemStack, world, player);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }


    // ==================================================
    //                       Good
    // ==================================================
    public void openGood(ItemStack itemStack, Level world, Player player) {
        MutableComponent message = Component.translatable("item.lycanitesmobs." + this.itemName + ".good");
        player.displayClientMessage(message, false);
        this.playSound(world, player.blockPosition(), ObjectManager.getSound(this.itemName + "_good"), SoundSource.AMBIENT, 5.0F, 1.0F);

        // Three Random Gifts:
        List<ItemStack> dropStacks = ObjectLists.getItems("winter_gifts");
        if (dropStacks == null || dropStacks.isEmpty())
            return;
        ItemStack dropStack = dropStacks.get(player.getRandom().nextInt(dropStacks.size()));
        dropStack.setCount(1 + player.getRandom().nextInt(4));
        CustomItemEntity entityItem = new CustomItemEntity(world, player.position().x(), player.position().y(), player.position().z(), dropStack);
        entityItem.setPickUpDelay(10);
        world.addFreshEntity(entityItem);
    }


    // ==================================================
    //                       Bad
    // ==================================================
    public void openBad(ItemStack itemStack, Level world, Player player) {
        MutableComponent message = Component.translatable("item.lycanitesmobs." + this.itemName + ".bad");
        player.displayClientMessage(message, false);
        this.playSound(world, player.blockPosition(), ObjectManager.getSound(this.itemName + "_bad"), SoundSource.AMBIENT, 5.0F, 1.0F);

        // One Random Trick:
        List<EntityType> entityTypes = ObjectLists.getEntites("winter_tricks");
        if (entityTypes.isEmpty())
            return;
        EntityType entityType = entityTypes.get(player.getRandom().nextInt(entityTypes.size()));
        if (entityType != null) {
            Entity entity = entityType.create(world);
            if (entity != null) {
                entity.moveTo(player.position().x(), player.position().y(), player.position().z(), player.yRotO, player.xRotO);

                // Themed Names:
                if (entity instanceof BaseCreatureEntity) {
                    BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
                    entityCreature.addLevel(world.random.nextInt(10));
                    if (entityCreature.creatureInfo.getName().equals("wildkin"))
                        entityCreature.setCustomName(Component.literal("Gooderness"));
                    else if (entityCreature.creatureInfo.getName().equals("jabberwock"))
                        entityCreature.setCustomName(Component.literal("Rudolph"));
                    else if (entityCreature.creatureInfo.getName().equals("ent"))
                        entityCreature.setCustomName(Component.literal("Salty Tree"));
                    else if (entityCreature.creatureInfo.getName().equals("treant"))
                        entityCreature.setCustomName(Component.literal("Salty Tree"));
                    else if (entityCreature.creatureInfo.getName().equals("reaper"))
                        entityCreature.setCustomName(Component.literal("Satan Claws"));
                    else if (entityCreature.creatureInfo.getName().equals("behemoth"))
                        entityCreature.setCustomName(Component.literal("Krampus"));
                }

                world.addFreshEntity(entity);
            }
        }
    }


    // ==================================================
    //                       Lists
    // ==================================================
    public static void createObjectLists() {
        // Halloween Treats:
        ObjectLists.addItem("winter_gifts", Items.DIAMOND);
        ObjectLists.addItem("winter_gifts", Items.GOLD_INGOT);
        ObjectLists.addItem("winter_gifts", Items.EMERALD);
        ObjectLists.addItem("winter_gifts", Blocks.IRON_BLOCK);
        ObjectLists.addItem("winter_gifts", Items.ENDER_PEARL);
        ObjectLists.addItem("winter_gifts", Items.BLAZE_ROD);
        ObjectLists.addItem("winter_gifts", Items.GLOWSTONE_DUST);
        ObjectLists.addItem("winter_gifts", Items.COAL);
        ObjectLists.addItem("winter_gifts", ObjectManager.getItem("mosspie"));
        ObjectLists.addItem("winter_gifts", ObjectManager.getItem("ambercake"));
        ObjectLists.addItem("winter_gifts", ObjectManager.getItem("peakskebab"));
        ObjectLists.addItem("winter_gifts", ObjectManager.getItem("bulwarkburger"));
        ObjectLists.addItem("winter_gifts", ObjectManager.getItem("palesoup"));
        ObjectLists.addFromConfig("winter_gifts");

        // Halloween Mobs:
        ObjectLists.addEntity("winter_tricks", "wildkin");
        ObjectLists.addEntity("winter_tricks", "jabberwock");
        ObjectLists.addEntity("winter_tricks", "ent");
        ObjectLists.addEntity("winter_tricks", "treant");
        ObjectLists.addEntity("winter_tricks", "reaper");
        ObjectLists.addEntity("winter_tricks", "behemoth");
    }
}
