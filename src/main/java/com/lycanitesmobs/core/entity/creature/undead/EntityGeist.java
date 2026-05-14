package com.lycanitesmobs.core.entity.creature.undead;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.block.base.BlockFireBase;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import com.lycanitesmobs.core.entity.goals.actions.MoveVillageGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class EntityGeist extends AgeableCreatureEntity implements Enemy {

    public boolean shadowfireDeath = true;

    public EntityGeist(EntityType<? extends EntityGeist> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEAD;
        this.hasAttackSound = true;
        this.spreadFire = true;

        this.canGrow = false;
        this.babySpawnChance = 0.01D;

        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(this.nextTravelGoalIndex++, new MoveVillageGoal(this));

        super.registerGoals();

        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));

        if (this.getNavigation() instanceof GroundPathNavigation) {
            GroundPathNavigation pathNavigateGround = (GroundPathNavigation) this.getNavigation();
            pathNavigateGround.setCanOpenDoors(true);
            pathNavigateGround.setAvoidSun(true);
        }
    }

    @Override
    public void loadCreatureFlags() {
        this.shadowfireDeath = this.creatureInfo.getFlag("shadowfireDeath", this.shadowfireDeath);
    }

    @Override
    public void onKillEntity(LivingEntity entityLivingBase) {
        super.onKillEntity(entityLivingBase);

        if (this.getCommandSenderWorld().getDifficulty().getId() >= 2 && entityLivingBase instanceof Villager) {
            if (this.getCommandSenderWorld().getDifficulty().getId() == 2 && this.random.nextBoolean()) return;

            Villager villagerentity = (Villager) entityLivingBase;
            ZombieVillager zombievillagerentity = EntityType.ZOMBIE_VILLAGER.create(this.level());
            zombievillagerentity.copyPosition(villagerentity);
            villagerentity.remove(RemovalReason.DISCARDED);
            zombievillagerentity.finalizeSpawn((ServerLevelAccessor) this.getCommandSenderWorld(), this.getCommandSenderWorld().getCurrentDifficultyAt(zombievillagerentity.blockPosition()), MobSpawnType.CONVERSION, null, null);
            zombievillagerentity.setVillagerData(villagerentity.getVillagerData());
            zombievillagerentity.setTradeOffers(villagerentity.getOffers().createTag());
            zombievillagerentity.setVillagerXp(villagerentity.getVillagerXp());
            zombievillagerentity.setBaby(villagerentity.isBaby());
            zombievillagerentity.setNoAi(villagerentity.isNoAi());

            if (villagerentity.hasCustomName()) {
                zombievillagerentity.setCustomName(villagerentity.getCustomName());
                zombievillagerentity.setCustomNameVisible(villagerentity.isCustomNameVisible());
            }

            this.getCommandSenderWorld().addFreshEntity(zombievillagerentity);
            this.getCommandSenderWorld().levelEvent(null, 1016, zombievillagerentity.blockPosition(), 0);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        try {
            int shadowfireWidth = (int) Math.floor(this.getDimensions(this.getPose()).width) + 1;
            int shadowfireHeight = (int) Math.floor(this.getDimensions(this.getPose()).height) + 1;
            boolean permanent = false;
            if (damageSource.getEntity() == this) {
                permanent = true;
                shadowfireWidth *= 5;
            }

            if (!this.getCommandSenderWorld().isClientSide && (permanent || (this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.shadowfireDeath))) {
                for (int x = (int) this.position().x() - shadowfireWidth; x <= (int) this.position().x() + shadowfireWidth; x++) {
                    for (int y = (int) this.position().y() - shadowfireHeight; y <= (int) this.position().y() + shadowfireHeight; y++) {
                        for (int z = (int) this.position().z() - shadowfireWidth; z <= (int) this.position().z() + shadowfireWidth; z++) {
                            Block block = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y, z)).getBlock();
                            if (block != Blocks.AIR && block != ObjectManager.getBlock("shadowfire")) {
                                BlockPos placePos = new BlockPos(x, y + 1, z);
                                Block upperBlock = this.getCommandSenderWorld().getBlockState(placePos).getBlock();
                                if (upperBlock == Blocks.AIR) {
                                    this.getCommandSenderWorld().setBlockAndUpdate(placePos, ObjectManager.getBlock("shadowfire").defaultBlockState().setValue(BlockFireBase.PERMANENT, permanent));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        super.die(damageSource);
    }

    @Override
    public boolean daylightBurns() {
        return !this.isBaby() && !this.isMinion();
    }
}
