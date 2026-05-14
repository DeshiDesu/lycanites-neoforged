package com.lycanitesmobs.core.entity.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ArenaNode {
    /** A list of all nodes directly connected to this node. **/
    public List<ArenaNode> adjacentNodes = new ArrayList<ArenaNode>();

    public Level world;
    public BlockPos pos;

    // ==================================================
    //                    Constructor
    // ==================================================
    public ArenaNode(Level world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }


    // ==================================================
    //                    Connections
    // ==================================================
    public void addAdjacentNode(ArenaNode node) {
        if(node == null || this.adjacentNodes.contains(node))
            return;
        this.adjacentNodes.add(node);
    }

    public ArenaNode getRandomAdjacentNode() {
        int adjacentNodesSize = this.adjacentNodes.size();
        if(adjacentNodesSize <= 0)
            return null;
        if(adjacentNodesSize == 1)
            return this.adjacentNodes.get(0);
        return this.adjacentNodes.get(this.world.random.nextInt(adjacentNodesSize));
    }

    public ArenaNode getClosestAdjacentNode(BlockPos targetPos) {
        int adjacentNodesSize = this.adjacentNodes.size();
        if(adjacentNodesSize <= 0)
            return null;
        if(adjacentNodesSize == 1)
            return this.adjacentNodes.get(0);
        double smallestDistance = this.pos.distSqr(targetPos);
        ArenaNode closestNode = this;
        for(ArenaNode adjacentNode : this.adjacentNodes) {
            double distance = adjacentNode.pos.distSqr(targetPos);
            if(distance < smallestDistance) {
                smallestDistance = distance;
                closestNode = adjacentNode;
            }
        }
        return closestNode;
    }
}
