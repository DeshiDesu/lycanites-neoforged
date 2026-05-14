package com.lycanitesmobs.core.util;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ContextUtils {
    public static class AnimationContext {
        public final BaseCreatureEntity entity;
        public final float time;
        public final float distance;
        public final float loop;
        public final float lookY;
        public final float lookX;
        public final float scale;
        public final int brightness;
        public final ResourceLocation texture;

        public AnimationContext(BaseCreatureEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness, ResourceLocation texture) {
            this.entity = entity;
            this.time = time;
            this.distance = distance;
            this.loop = loop;
            this.lookY = lookY;
            this.lookX = lookX;
            this.scale = scale;
            this.brightness = brightness;
            this.texture = texture;
        }
    }
    public static class CreatureBuildTask {
        public BlockState blockState;
        public BlockPos pos;
        public int phase;

        public CreatureBuildTask(BlockState blockState, BlockPos pos, int phase) {
            this.blockState = blockState;
            this.pos = pos;
            this.phase = phase;
        }
    }
}
