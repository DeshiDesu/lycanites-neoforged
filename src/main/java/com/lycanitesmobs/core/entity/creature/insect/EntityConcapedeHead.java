package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityConcapedeHead extends AgeableCreatureEntity {
	
	public static int CONCAPEDE_SIZE_MAX = 10;
	public BaseCreatureEntity backSegment;
	public boolean isHungry = true;
	
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityConcapedeHead(EntityType<? extends EntityConcapedeHead> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.ARTHROPOD;
        this.hasAttackSound = true;

        this.canGrow = true;
        this.babySpawnChance = 0D;
        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }

	@Override
	public void loadCreatureFlags() {
		CONCAPEDE_SIZE_MAX = this.creatureInfo.getFlag("sizeMax", CONCAPEDE_SIZE_MAX);
	}
	
	
    // ==================================================
    //                      Spawn
    // ==================================================
	// ========== On Spawn ==========
	@Override
	public void onFirstSpawn() {
		// Create Starting Segments:
        if(!this.getCommandSenderWorld().isClientSide && this.backSegment == null) {
        	this.setGrowingAge(-this.growthTime / 4);
        	int segmentCount = this.getRandom().nextInt(CONCAPEDE_SIZE_MAX);
    		AgeableCreatureEntity parentSegment = this;
        	for(int segment = 0; segment < segmentCount; segment++) {
        		EntityConcapedeSegment segmentEntity = (EntityConcapedeSegment)CreatureManager.getInstance().getCreature("concapedesegment").createEntity(parentSegment.getCommandSenderWorld());
        		segmentEntity.moveTo(parentSegment.position().x(), parentSegment.position().y(), parentSegment.position().z(), 0.0F, 0.0F);
				segmentEntity.setParentTarget(parentSegment);
				segmentEntity.applyVariant(this.getVariantIndex());
				segmentEntity.setSizeScale(this.sizeScale);
				segmentEntity.spawnEventType = this.spawnEventType;
				segmentEntity.firstSpawn = false;
        		parentSegment.getCommandSenderWorld().addFreshEntity(segmentEntity);
				parentSegment = segmentEntity;
        	}
        }
        super.onFirstSpawn();
	}
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	@Override
	public boolean shouldFollowParent() {
		return false; // Never follow parents.
	}

	@Override
	public boolean rollLookChance() {
		return false;
	}

	@Override
	public boolean rollWanderChance() {
		return false;
	}
	
	
	// ==================================================
  	//                        Age
  	// ==================================================
	@Override
	public void setGrowingAge(int age) {
		// Spawn Additional Segments:
		if(!this.getCommandSenderWorld().isClientSide && !this.firstSpawn && age == 0 && !this.isHungry && CreatureManager.getInstance().getCreature("concapedesegment") != null) {
			age = -(this.growthTime / 4);
			this.isHungry = true;

			int size = 0;
			BaseCreatureEntity lastSegment = this;
			while(size <= CONCAPEDE_SIZE_MAX) {
				size++;

				BaseCreatureEntity trailingSegment = null;
				if(lastSegment instanceof EntityConcapedeHead)
					trailingSegment = ((EntityConcapedeHead)lastSegment).backSegment;
				else if(lastSegment instanceof EntityConcapedeSegment)
					trailingSegment = ((EntityConcapedeSegment)lastSegment).backSegment;

				if(trailingSegment == null || trailingSegment == lastSegment) {
					break;
				}
				lastSegment = trailingSegment;
			}

			if(size < CONCAPEDE_SIZE_MAX) {
				EntityConcapedeSegment segmentEntity = (EntityConcapedeSegment)CreatureManager.getInstance().getCreature("concapedesegment").createEntity(lastSegment.getCommandSenderWorld());
	    		segmentEntity.moveTo(lastSegment.position().x(), lastSegment.position().y(), lastSegment.position().z(), 0.0F, 0.0F);
				segmentEntity.setParentTarget(lastSegment);
				segmentEntity.applyVariant(this.getVariantIndex());
				segmentEntity.setSizeScale(this.sizeScale);
				segmentEntity.spawnEventType = this.spawnEventType;
				segmentEntity.firstSpawn = false;
				lastSegment.getCommandSenderWorld().addFreshEntity(segmentEntity);
			}
		}
        super.setGrowingAge(age);
    }
	
	
	// ==================================================
   	//                      Movement
   	// ==================================================
	// ========== Pathing Weight ==========
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

    // ========== Can leash ==========
    @Override
    public boolean canBeLeashed(Player player) {
        return true;
    }

	@Override
	public boolean isPushable() {
		return false;
	}
	
	
	// ==================================================
   	//                      Attacks
   	// ==================================================
	@Override
	public boolean canAttack(LivingEntity target) {
		if(target instanceof EntityConcapedeSegment)
			return false;
		return super.canAttack(target);
	}
    
    
    // ==================================================
  	//                      Targets
  	// ==================================================
    @Override
    public boolean isAggressive() {
    	if(this.isInLove())
    		return false;
		this.getCommandSenderWorld();
		if(this.isDaytime())
    		return this.testLightLevel() < 2;
    	else
    		return super.isAggressive();
    }
    
    @Override
    public boolean isProtective(Entity entity) {
    	if(this.isInLove())
    		return false;
		if(entity instanceof EntityConcapedeSegment) {
			BaseCreatureEntity checkCreature = (BaseCreatureEntity)entity;
			while(true) {
				if(!checkCreature.hasParent()) {
					break;
				}
				if(checkCreature.getParentTarget() == this) {
					return true;
				}
				if(checkCreature.getParentTarget() instanceof BaseCreatureEntity) {
					checkCreature = (BaseCreatureEntity)checkCreature.getParentTarget();
					continue;
				}
				break;
			}
		}
    	return false;
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean canClimb() { return true; }
    
    
    // ==================================================
    //                     Breeding
    // ==================================================
    // ========== Create Child ==========
    @Override
	public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
		return null;
	}
	
	@Override
	public boolean canBreed() {
        return this.getGrowingAge() >= 0;
    }

    @Override
	public boolean breed() {
		if(super.breed()) {
			this.isHungry = false;
			if(this.getAge() == 0) {
				this.setGrowingAge(0);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canMate() {
		return false;
	}

	// ==================================================
	//                     Equipment
	// ==================================================
	@Override
	public int getNoBagSize() { return 0; }
	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public float getFallResistance() {
    	return 100;
    }


	// ==================================================
	//                        NBT
	// ==================================================
	// ========== Read ===========
	/** Used when loading this mob from a saved chunk. **/
	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		if(nbt.contains("IsHungry")) {
			this.isHungry = nbt.getBoolean("IsHungry");
		}
		super.readAdditionalSaveData(nbt);
	}

	// ========== Write ==========
	/** Used when saving this mob to a chunk. **/
	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putBoolean("IsHungry", this.isHungry);
	}
}
