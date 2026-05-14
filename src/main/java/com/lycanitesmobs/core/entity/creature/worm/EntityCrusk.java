package com.lycanitesmobs.core.entity.creature.worm;

import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityCrusk extends TameableCreatureEntity implements IGroupHeavy {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityCrusk(EntityType<? extends EntityCrusk> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.ARTHROPOD;
        this.hasAttackSound = true;
        this.babySpawnChance = 0.25D;
        this.growthTime = -120000;
        this.setupMob();
        this.hitAreaWidthScale = 1.5F;

        // Stats:
        this.setMaxUpStep(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new StealthGoal(this).setStealthTime(60).setStealthMove(true).setStealthAttack(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
    }

    @Override
    public boolean rollWanderChance() {
        return this.getRandom().nextDouble() <= 0.001D;
    }


    // ==================================================
    //                      Stealth
    // ==================================================
    @Override
    public boolean canStealth() {
        if (this.isTamed() && this.isSitting())
            return false;
        BlockState blockState = this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(0, -1, 0));
        if (blockState.getBlock() != Blocks.AIR) {
            if (Material.DIRT.contains(blockState.getBlock())) return true;
            if (Material.GRASS.contains(blockState.getBlock())) return true;
            if (Material.LEAVES.contains(blockState.getBlock())) return true;
            if (Material.SAND.contains(blockState.getBlock())) return true;
            if (Material.CLAY.contains(blockState.getBlock())) return true;
            if (Material.TOP_SNOW.contains(blockState.getBlock())) return true;
            if (Material.SNOW.contains(blockState.getBlock())) return true;
        }
        if (blockState.getBlock() == Blocks.NETHERRACK)
            return true;
        return false;
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    public boolean canBeTempted() {
        return this.isBaby();
    }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
    }


    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() {
        return 0;
    }

    @Override
    public int getBagSize() {
        return this.creatureInfo.bagSize;
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS)) return true;
        return super.isInvulnerableTo(source);
    }
}
