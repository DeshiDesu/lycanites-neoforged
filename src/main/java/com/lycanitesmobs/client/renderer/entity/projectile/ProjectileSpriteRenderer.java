package com.lycanitesmobs.client.renderer.entity.projectile;

import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.misc.LaserEndProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.LaserProjectileEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class ProjectileSpriteRenderer extends EntityRenderer<BaseProjectileEntity> {
    private Class projectileClass;

    public ProjectileSpriteRenderer(EntityRendererProvider.Context renderManager, Class projectileClass) {
        super(renderManager);
        this.projectileClass = projectileClass;
    }

    @Override
    public void render(BaseProjectileEntity entity, float partialTicks, float yaw, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int brightness) {
        if (entity instanceof CustomProjectileEntity && ((CustomProjectileEntity) entity).projectileInfo == null)
            return;
        if (entity.getClass() == LaserEndProjectileEntity.class) return;

        float loop = (float) entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : Math.min(1, partialTicks));
        float scale = entity.getProjectileScale();

        if (entity instanceof CustomProjectileEntity && ((CustomProjectileEntity) entity).getLaserEnd() != null) {
            matrixStack.pushPose();
            this.renderLaser((CustomProjectileEntity) entity, matrixStack, renderTypeBuffer, ((CustomProjectileEntity) entity).laserWidth / 4, loop);
            matrixStack.popPose();
            return;
        }

        matrixStack.pushPose();
        matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        matrixStack.translate(0, entity.getTextureOffsetY(), 0);
        matrixStack.scale(scale, scale, scale);
        ResourceLocation texture = this.getTextureLocation(entity);
        RenderType rendertype = CustomRenderStates.getSpriteRenderType(texture);
        this.renderSprite(entity, matrixStack, renderTypeBuffer, rendertype, entity.textureScale);
        matrixStack.popPose();
    }


    public void renderSprite(BaseProjectileEntity entity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, RenderType rendertype, float scale) {
        float textureWidth = 0.25F;
        float textureHeight = 0.25F;
        float minU = 0;
        float maxU = 1;
        float minV = 0;
        float maxV = 1;
        if (entity.animationFrameMax > 0) {
            minV = (float) entity.animationFrame / (float) entity.animationFrameMax;
            maxV = minV + (1F / (float) entity.animationFrameMax);
            textureWidth *= scale;
            textureHeight *= scale;
        }

        Matrix4f matrix4f = matrixStack.last().pose();
        VertexConsumer vertexBuilder = renderTypeBuffer.getBuffer(rendertype);

        vertexBuilder
                .vertex(matrix4f, -textureWidth, -textureHeight + (textureHeight / 2), 0.0F) // pos
                .color(255, 255, 255, 255) // color
                .uv(minU, maxV) // texture
                .normal(0.0F, 1.0F, 0.0F) // normal
                .endVertex();
        vertexBuilder
                .vertex(matrix4f, textureWidth, -textureHeight + (textureHeight / 2), 0.0F)
                .color(255, 255, 255, 255) // color
                .uv(maxU, maxV)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexBuilder
                .vertex(matrix4f, textureWidth, textureHeight + (textureHeight / 2), 0.0F)
                .color(255, 255, 255, 255) // color
                .uv(maxU, minV)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexBuilder
                .vertex(matrix4f, -textureWidth, textureHeight + (textureHeight / 2), 0.0F)
                .color(255, 255, 255, 255) // color
                .uv(minU, minV)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    public void renderLaser(CustomProjectileEntity entity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float scale, float loop) {
        double laserSize = entity.position().distanceTo(entity.getLaserEnd());
        float spacing = 1;
        double factor = spacing / laserSize;
        if (laserSize <= 0) return;

        ResourceLocation texture = this.getTextureLocation(entity);
        RenderType rendertype = CustomRenderStates.getSpriteRenderType(texture);
        Vec3 direction = entity.getLaserEnd().subtract(entity.position()).normalize();

        for (float segment = 0; segment <= laserSize; segment += factor) {
            matrixStack.pushPose();
            matrixStack.translate(segment * direction.x() * spacing, segment * direction.y() * spacing, segment * direction.z() * spacing);
            matrixStack.translate(0, entity.getTextureOffsetY(), 0);
            matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            matrixStack.scale(scale, scale, scale);
            this.renderSprite(entity, matrixStack, renderTypeBuffer, rendertype, scale);
            matrixStack.popPose();
        }
    }


    @Override
    public ResourceLocation getTextureLocation(BaseProjectileEntity entity) {
        ResourceLocation tex = entity.getTexture();
        LMHelperClass.logErrorMessageOnce("Missing texture from entity: " + entity.getType());
        return tex != null ? tex : MissingTextureAtlasSprite.getLocation();
    }

    protected ResourceLocation getLaserTexture(LaserProjectileEntity entity) {
        return entity.getBeamTexture();
    }

    public void bindTexture(ResourceLocation texture) {
        // TODO Remove
    }
}
