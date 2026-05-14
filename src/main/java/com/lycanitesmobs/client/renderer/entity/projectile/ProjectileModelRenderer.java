package com.lycanitesmobs.client.renderer.entity.projectile;

import com.google.common.collect.Lists;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.model.projectile.base.ProjectileModel;
import com.lycanitesmobs.client.model.projectile.base.ProjectileObjModel;
import com.lycanitesmobs.client.obj.model.VBOObjModel;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.client.renderer.layer.projectile.LayerProjectileBase;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ProjectileModelRenderer extends EntityRenderer<BaseProjectileEntity> implements RenderLayerParent<BaseProjectileEntity, ProjectileModel> {
    protected final List<RenderLayer<BaseProjectileEntity, ProjectileModel>> renderLayers = Lists.newArrayList(); // TODO Layers for projectiles.
    protected ProjectileModel renderModel;
    protected ProjectileModel defaultModel;


    public ProjectileModelRenderer(EntityRendererProvider.Context renderManager, ProjectileInfo projectileInfo) {
        super(renderManager);
        this.renderModel = ModelManager.getInstance().getProjectileModel(projectileInfo);
        this.defaultModel = this.renderModel;
        this.renderModel.addCustomLayers(this);
    }

    public ProjectileModelRenderer(EntityRendererProvider.Context renderManager, String projectileName) {
        super(renderManager);
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(projectileName);
        if (projectileInfo != null) {
            this.renderModel = ModelManager.getInstance().getProjectileModel(projectileInfo);
        } else {
            this.renderModel = ModelManager.getInstance().getOldProjectileModel(projectileName);
        }
        if (renderModel == null) {
            return;
        }
        this.defaultModel = this.renderModel;
        this.renderModel.addCustomLayers(this);
    }

    @Override
    public void render(BaseProjectileEntity entity, float partialTicks, float yaw, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int brightness) {
        // Model States:
        float time = 0;
        float distance = 0;
        float loop = (float) entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : (partialTicks % 1.0F));
        float lookYaw = 0;
        float lookPitch = 0;
        float scale = 1;
        boolean invisible = false;
        boolean allyInvisible = false;

        // Render Model and Layers:
        try {
            matrixStack.translate(0, -0.25F, 0); // translate
            matrixStack.scale(0.25F, 0.25F, 0.25F); // scale
            matrixStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(entity.yRotO), 0.0F, 1.0F, 0.0F))); // rotate

            if (this.getModel() == null) {
                LMHelperClass.logWarning("", "Missing Projectile Model: " + entity);
            } else if (!(this.getModel() instanceof ProjectileObjModel)) {
                ResourceLocation texture = this.getTextureLocation(entity);
                if (texture == null) {
                    return;
                }
                RenderType renderType = CustomRenderStates.getObjRenderType(texture, this.renderModel.getBlending(entity, null), this.renderModel.getGlow(entity, null));
                this.getModel().render(entity, matrixStack, renderTypeBuffer.getBuffer(renderType), null, 0, 0, loop, 0, 0, scale, brightness);
            } else {

                this.getModel().generateAnimationFrames(entity, time, distance, loop, lookYaw, lookPitch, 1, brightness);
                this.renderModel(entity, matrixStack, renderTypeBuffer, null, time, distance, loop, lookYaw, lookPitch, 1, brightness, invisible, allyInvisible);
                for (RenderLayer<BaseProjectileEntity, ProjectileModel> layer : this.renderLayers) {
                    if (!(layer instanceof LayerProjectileBase)) {
                        continue;
                    }
                    LayerProjectileBase layerCreatureBase = (LayerProjectileBase) layer;
                    if (!layerCreatureBase.canRenderLayer(entity, scale)) {
                        continue;
                    }
                    this.renderModel(entity, matrixStack, renderTypeBuffer, layerCreatureBase, time, distance, loop, lookYaw, lookPitch, scale, brightness, invisible, allyInvisible);
                }
                this.getModel().clearAnimationFrames();

                VBOBatcher.getInstance().endBatches();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Renders the main model.
     *
     * @param entity           The entity to render.
     * @param matrixStack      The matrix stack for animation.
     * @param renderTypeBuffer The render type buffer for rendering with.
     * @param layer            The layer to render, the base layer is null.
     * @param time             The current movement time for walk cycles, etc.
     * @param distance         The current movement amount for walk cycles, etc.
     * @param loop             A constant tick for looping animations.
     * @param lookY            The entity's yaw looking position for head rotation, etc.
     * @param lookX            The entity's pitch looking position for head rotation, etc.
     * @param scale            The base scale to render the model at, usually just 1 which scales 1m unit in Blender to a 1m block unit in Minecraft.
     * @param brightness       The brightness of the mob based on block location, etc.
     * @param invisible        If true, the entity has invisibility or some form of stealth.
     * @param allyInvisible    If true, the entity has invisibility or some form of stealth but is allied to the player so should be translucent, etc.
     */
    protected void renderModel(BaseProjectileEntity entity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, LayerProjectileBase layer, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness, boolean invisible, boolean allyInvisible) {
        ResourceLocation texture = this.getEntityTexture(entity, layer);
        if (texture == null) {
            return;
        }

        // Render Model
        // TODO allyInvisible lower color alpha
        if (!invisible || allyInvisible) {
            VBOObjModel.renderType = CustomRenderStates.getObjVBORenderType(this.getModel().getBlending(entity, layer), this.getModel().getGlow(entity, layer));
            VBOObjModel.renderNormal = true;
        }
        if (entity.isCurrentlyGlowing()) {
            VBOObjModel.renderOutline = true;
        }
        VBOObjModel.tex = texture;
        this.getModel().render(entity, matrixStack, null, layer, time, distance, loop, lookY, lookX, 1, brightness);
        VBOObjModel.tex = null;
        VBOObjModel.renderOutline = false;
        VBOObjModel.renderNormal = false;
        VBOObjModel.renderType = null;
    }


    //@Override
    public ProjectileModel getModel() {
        return this.renderModel;
    }

    public final boolean addLayer(RenderLayer<BaseProjectileEntity, ProjectileModel> layer) {
        return this.renderLayers.add(layer);
    }

    /**
     * Gets the texture to use.
     *
     * @param entity The entity to get the texture from.
     * @param layer  The layer to get the texture for.
     * @return The texture to bind.
     */
    public ResourceLocation getEntityTexture(BaseProjectileEntity entity, LayerProjectileBase layer) {
        if (layer == null) {
            return this.getTextureLocation(entity);
        }
        ResourceLocation layerTexture = layer.getLayerTexture(entity);
        return layerTexture != null ? layerTexture : this.getTextureLocation(entity);
    }

    @Override
    public ResourceLocation getTextureLocation(BaseProjectileEntity entity) {
        return entity.getTexture();
    }

}
