package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.entity.goals.targeting.CopyMasterAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindMasterGoal;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityJouste extends AgeableCreatureEntity {
    public EntityJouste(EntityType<? extends EntityJouste> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = true;
        this.babySpawnChance = 0.1D;
        this.canGrow = true;
        this.attackCooldownMax = 10;
        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));

        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindMasterGoal(this).setTargetClass(EntityJousteAlpha.class).setSightCheck(false));
        this.targetSelector.addGoal(this.nextFindTargetIndex++, new CopyMasterAttackTargetGoal(this));
    }

    @Override
    public void onFirstSpawn() {
        // Random Alpha:
        CreatureInfo alphaInfo = CreatureManager.getInstance().getCreature("joustealpha");
        if (alphaInfo != null) {
            float alphaChance = (float) alphaInfo.creatureSpawn.spawnWeight / Math.max(this.creatureInfo.creatureSpawn.spawnWeight, 1);
            if (this.getRandom().nextFloat() <= alphaChance) {
                EntityJousteAlpha alpha = (EntityJousteAlpha) CreatureManager.getInstance().getCreature("joustealpha").createEntity(this.getCommandSenderWorld());
                alpha.copyPosition(this);
                this.getCommandSenderWorld().addFreshEntity(alpha);
                this.remove(RemovalReason.DISCARDED);
            }
        }
        super.onFirstSpawn();
    }

    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
        if (blockState.getBlock() != Blocks.AIR) {
            if (Material.SAND.contains(blockState.getBlock()))
                return 10F;
            if (Material.CLAY.contains(blockState.getBlock()))
                return 7F;
            if (Material.STONE.contains(blockState.getBlock()))
                return 5F;
        }
        return super.getBlockPathWeight(x, y, z);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        if (!this.hasAttackTarget() && !this.hasMaster())
            return true;
        return super.canBeLeashed(player);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof EntityJousteAlpha)
            return false;
        return super.canAttack(target);
    }

    @Override
    public boolean isProtective(Entity entity) {
        if (entity instanceof EntityJouste) {
            return true;
        }
        return super.isProtective(entity);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.CACTUS)) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public void setGrowingAge(int age) {
        if (age == 0 && this.getAge() < 0) {
            CreatureInfo alphaInfo = CreatureManager.getInstance().getCreature("joustealpha");
            if (alphaInfo != null && this.getRandom().nextFloat() >= 0.9F) {
                EntityJousteAlpha alpha = (EntityJousteAlpha) CreatureManager.getInstance().getCreature("joustealpha").createEntity(this.getCommandSenderWorld());
                alpha.copyPosition(this);
                this.getCommandSenderWorld().addFreshEntity(alpha);
                this.remove(RemovalReason.DISCARDED);
            }
        }
        super.setGrowingAge(age);
    }
}
