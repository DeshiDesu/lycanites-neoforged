package com.lycanitesmobs.client.renderer.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NoneRenderer<T extends Entity> extends EntityRenderer<T> {
    
    // ==================================================
    //                     Constructor
    // ==================================================
    public NoneRenderer(EntityRendererProvider.Context renderManager) {
    	super(renderManager);
    }



    // ==================================================
    //                     Do Render
    // ==================================================
    @Override
    public void render(T p_114485_, float p_114486_, float p_114487_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {
        super.render(p_114485_, p_114486_, p_114487_, p_114488_, p_114489_, p_114490_);
    }
    
    // ==================================================
    //                       Visuals
    // ==================================================
    // ========== Get Texture ==========
    @Override
	public ResourceLocation getTextureLocation(Entity entity) {
    	return null;
    }
}
