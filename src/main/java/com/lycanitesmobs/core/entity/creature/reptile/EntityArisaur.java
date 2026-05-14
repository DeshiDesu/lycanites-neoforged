package com.lycanitesmobs.core.entity.creature.reptile;

import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityArisaur extends AgeableCreatureEntity implements IGroupHeavy {

    public EntityArisaur(EntityType<? extends EntityArisaur> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = MobType.UNDEFINED;
        this.hasAttackSound = false;

        this.canGrow = true;
        this.babySpawnChance = 0.1D;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        //this.solidCollision = true;
        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }

    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        if (this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z)).getBlock() != Blocks.AIR) {
            BlockState blocState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
            if (Material.GRASS.contains(blocState.getBlock()))
                return 10F;
            if (Material.DIRT.contains(blocState.getBlock()))
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
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
    public boolean canBeLeashed(Player player) {
        return true;
    }

    /**
     * Returns this creature's main texture. Also checks for for subspecies.
     **/
    @Override
    public ResourceLocation getTexture() {
        if (!this.hasCustomName() || !"Flowersaur".equals(this.getCustomName().getString()))
            return super.getTexture();

        String textureName = this.getTextureName() + "_flowersaur";
        if (TextureManager.getTexture(textureName) == null)
            TextureManager.addTexture(textureName, "textures/entity/" + textureName.toLowerCase() + ".png");
        return TextureManager.getTexture(textureName);
    }
}
