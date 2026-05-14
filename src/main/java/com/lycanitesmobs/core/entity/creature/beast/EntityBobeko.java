package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class EntityBobeko extends AgeableCreatureEntity {
    public EntityBobeko(EntityType<? extends EntityBobeko> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = false;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Trail:
        if (!this.getCommandSenderWorld().isClientSide && (this.tickCount % 10 == 0 || this.isMoving() && this.tickCount % 5 == 0)) {
            int trailHeight = 2;
            if (this.isBaby())
                trailHeight = 1;
            for (int y = 0; y < trailHeight; y++) {
                Block block = this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(0, y, 0)).getBlock();
                if (block == Blocks.AIR || block == Blocks.SNOW || block == ObjectManager.getBlock("frostcloud"))
                    this.getCommandSenderWorld().setBlockAndUpdate(this.blockPosition().offset(0, y, 0), ObjectManager.getBlock("frostcloud").defaultBlockState());
            }
        }

        // Particles:
        if (this.getCommandSenderWorld().isClientSide)
            for (int i = 0; i < 2; ++i) {
                this.getCommandSenderWorld().addParticle(ParticleTypes.ITEM_SNOWBALL, this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
            }
    }

    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if (block != Blocks.AIR) {
            if (Material.GRASS.contains(blockState.getBlock()) || Material.TOP_SNOW.contains(blockState.getBlock()))
                return 10F;
            if (Material.DIRT.contains(blockState.getBlock()) || Material.ICE.contains(blockState.getBlock()))
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return true;
    }

    @Override
    public int getNoBagSize() {
        return 0;
    }

    @Override
    public int getBagSize() {
        return this.creatureInfo.bagSize;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.type().equals(ObjectManager.getDamageSource(this.level(), "ooze").type())) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance potionEffect) {
        if (potionEffect.getEffect() == MobEffects.MOVEMENT_SLOWDOWN) return false;
        if (potionEffect.getEffect() == MobEffects.HUNGER) return false;
        return super.canBeAffected(potionEffect);
    }

    @Override
    public HashMap<Integer, String> getInteractCommands(Player player, ItemStack itemStack) {
        HashMap<Integer, String> commands = new HashMap<Integer, String>();
        commands.putAll(super.getInteractCommands(player, itemStack));

        if (itemStack != null) {
            // Milk:
            if (itemStack.getItem() == Items.BUCKET)
                commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Milk");
        }

        return commands;
    }

    @Override
    public boolean performCommand(String command, Player player, ItemStack itemStack) {

        // Milk:
        if (command.equals("Milk")) {
            this.replacePlayersItem(player, itemStack, new ItemStack(Items.MILK_BUCKET));
            return true;
        }

        return super.performCommand(command, player, itemStack);
    }
}
