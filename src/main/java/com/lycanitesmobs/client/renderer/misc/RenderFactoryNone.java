package com.lycanitesmobs.client.renderer.misc;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderFactoryNone<T extends Entity> {
    /**
     * Render factory is no longer needed as the render provider directly accesses the renderer through
     * the render registry event
     */
    protected Class entityClass;

    public RenderFactoryNone(Class entityClass) {
        this.entityClass = entityClass;
    }

    public EntityRenderer<? super T> createRenderFor(EntityRendererProvider.Context manager) {
        return new NoneRenderer(manager);
    }

}
