package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector3d;

public class RandomPositionGenerator {
	private static Vector3d staticVector = new Vector3d(0.0D, 0.0D, 0.0D);

    // ==================================================
 	//                 Find Random Target
 	// ==================================================
    public static Vector3d findRandomTarget(BaseCreatureEntity entity, int range, int height) {
        return findRandomTarget(entity, range, height, 0);
    }
    public static Vector3d findRandomTarget(BaseCreatureEntity entity, int range, int height, int heightLevel) {
        return getTargetBlock(entity, range, height, (Vector3d)null, heightLevel);
    }

    // ========== Find Random Waypoint to Target ==========
    public static Vector3d findRandomTargetTowards(BaseCreatureEntity entity, int range, int height, Vector3d par3Vector3d) {
        staticVector = new Vector3d(par3Vector3d.x - entity.position().x(), par3Vector3d.y - entity.position().y(), par3Vector3d.z - entity.position().z());
        return findRandomTargetTowards(entity, range, height, staticVector, 0);
    }
    public static Vector3d findRandomTargetTowards(BaseCreatureEntity entity, int range, int height, Vector3d par3Vector3d, int heightLevel) {
        staticVector = new Vector3d(par3Vector3d.x - entity.position().x(), par3Vector3d.y - entity.position().y(), par3Vector3d.z - entity.position().z());
        return getTargetBlock(entity, range, height, staticVector, heightLevel);
    }

    // ========== Find Random Waypoint from Target ==========
    public static Vector3d findRandomTargetAwayFrom(BaseCreatureEntity entity, int range, int height, Vector3d avoidTarget) {
        return findRandomTargetAwayFrom(entity, range, height, avoidTarget, 0);
    }
    public static Vector3d findRandomTargetAwayFrom(BaseCreatureEntity entity, int range, int height, Vector3d avoidTarget, int heightLevel) {
        staticVector = new Vector3d(entity.position().x(), entity.position().y(), entity.position().z()).sub(avoidTarget);
        return getTargetBlock(entity, range, height, staticVector, heightLevel);
    }

    // ========== Get Target Block ==========
    private static Vector3d getTargetBlock(BaseCreatureEntity entity, int range, int height, Vector3d target, int heightLevel) {
        PathNavigation pathNavigate = entity.getNavigation();
        RandomSource random = entity.getRandom();
        boolean validTarget = false;
        int targetX = 0;
        int targetY = 0;
        int targetZ = 0;
        float pathMin = -99999.0F;
        boolean pastHome;

        if(entity.hasHome()) {
            double homeDist = (entity.getRestrictCenter().distSqr(entity.blockPosition()) + 4.0F);
            double homeDistMax = (double)(entity.getHomeDistanceMax() + (float)range);
            pastHome = homeDist < homeDistMax * homeDistMax;
        }
        else
        	pastHome = false;

        for(int attempt = 0; attempt < 10; ++attempt) {
            int possibleX = random.nextInt(2 * range) - range;
            int possibleY = random.nextInt(2 * height) - height;
            int possibleZ = random.nextInt(2 * range) - range;

            // Random Height:
            if(entity.isFlying() || (entity.isStrongSwimmer() && entity.isInWater())) {
	            if(entity.position().y() > entity.getCommandSenderWorld().getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, entity.blockPosition()).getY() + (heightLevel * 1.25))
	        		possibleY = random.nextInt(2 * height) - height * 3 / 2;
	            else if(entity.position().y() < entity.getCommandSenderWorld().getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, entity.blockPosition()).getY() + heightLevel)
	            	possibleY = random.nextInt(2 * height) - height / 2;
            }

            if(target == null || (double)possibleX * target.x + (double)possibleZ * target.z >= 0.0D) {
            	possibleX += (int) Math.floor(entity.position().x());
            	possibleY += (int) Math.floor(entity.position().y());
            	possibleZ += (int) Math.floor(entity.position().z());
                BlockPos possiblePos = new BlockPos(possibleX, possibleY, possibleZ);

                if((!pastHome || entity.positionNearHome(possibleX, possibleY, possibleZ)) && (entity.useDirectNavigator() || pathNavigate.isStableDestination(possiblePos))) {
                    float pathWeight = entity.getBlockPathWeight(possibleX, possibleY, possibleZ);
                    if(pathWeight > pathMin) {
                    	pathMin = pathWeight;
                    	targetX = possibleX;
                    	targetY = possibleY;
                    	targetZ = possibleZ;
                        validTarget = true;
                    }
                }
            }
        }

        if(validTarget)
            return new Vector3d((double)targetX, (double)targetY, (double)targetZ);
        else
        	return null;
    }
}
