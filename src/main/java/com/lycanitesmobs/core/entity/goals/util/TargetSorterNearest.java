package com.lycanitesmobs.core.entity.goals.util;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;

import java.util.Comparator;

public class TargetSorterNearest implements Comparator {
    private final Entity host;
    
    // ==================================================
  	//                    Constructor
  	// ==================================================

    public TargetSorterNearest(Entity setHost) {
        this.host = setHost;
    }
    
    
    // ==================================================
   	//                      Compare
   	// ==================================================
    public int compare(Object objectA, Object objectB) {
    	if(objectA instanceof Entity && objectB instanceof Entity)
    		return this.compareDistanceSq((Entity)objectA, (Entity)objectB);
    	if(objectA instanceof BlockPos && objectB instanceof BlockPos)
    		return this.compareDistanceSq((BlockPos)objectA, (BlockPos)objectB);
    	return 0;
    }
    
    
    // ==================================================
  	//                   Compare Distance
  	// ==================================================
    public int compareDistanceSq(Entity targetA, Entity targetB) {
        double distanceA = this.host.distanceTo(targetA);
        double distanceB = this.host.distanceTo(targetB);
        return Double.compare(distanceA, distanceB);
    }

    public int compareDistanceSq(BlockPos targetA, BlockPos targetB) {
        BlockPos hostCoords = new BlockPos((int)this.host.position().x(), (int)this.host.position().y(), (int)this.host.position().z());
        double distanceA = hostCoords.distSqr(new Vec3i(targetA.getX(), targetA.getY(), targetA.getZ()));
        double distanceB = hostCoords.distSqr(new Vec3i(targetB.getX(), targetB.getY(), targetB.getZ()));
        return Double.compare(distanceA, distanceB);
    }
}
