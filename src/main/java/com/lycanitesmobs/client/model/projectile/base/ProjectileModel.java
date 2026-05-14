package com.lycanitesmobs.client.model.projectile.base;

import com.lycanitesmobs.client.model.animation.IAnimationModel;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.entity.projectile.ProjectileModelRenderer;
import com.lycanitesmobs.client.renderer.layer.projectile.LayerProjectileBase;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ProjectileModel extends EntityModel<BaseProjectileEntity> implements IAnimationModel {
    public PoseStack matrixStack;

    public ProjectileModel() {
        this(1.0F);
    }

    public ProjectileModel(float shadowSize) {
    	/*this.texWidth = 128;
		this.texHeight = 128;*/
    }

    @Override
    public void setupAnim(BaseProjectileEntity entity, float time, float distance, float loop, float lookY, float lookX) {
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer iVertexBuilder, int i, int i1, float v, float v1, float v2, float v3) {
    }

    /**
     * Called by the renderer to add custom layers to it.
     *
     * @param renderer The renderer to add layers to.
     */
    public void addCustomLayers(ProjectileModelRenderer renderer) {
    }

    /**
     * Generates all animation frames for a render tick.
     *
     * @param entity     The entity to render.
     * @param time       The current movement time for walk cycles, etc.
     * @param distance   The current movement amount for walk cycles, etc.
     * @param loop       A constant tick for looping animations.
     * @param lookY      The entity's yaw looking position for head rotation, etc.
     * @param lookX      The entity's pitch looking position for head rotation, etc.
     * @param scale      The base scale to render the model at, usually just 0.0625F which scales 1m unit in Blender to a 1m block unit in Minecraft.
     * @param brightness The brightness of the mob based on block location, etc.
     */
    public void generateAnimationFrames(BaseProjectileEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness) {
    }

    /**
     * Clears all animation frames that were generated for a render tick.
     */
    public void clearAnimationFrames() {
    }

    /**
     * Renders this model based on an entity.
     *
     * @param entity        The entity to render.
     * @param matrixStack   The matrix stack for animation.
     * @param vertexBuilder The vertex builder for rendering the model.
     * @param layer         The layer to render, the base layer is null.
     * @param time          The current movement time for walk cycles, etc.
     * @param distance      The current movement amount for walk cycles, etc.
     * @param loop          A constant tick for looping animations.
     * @param lookY         The entity's yaw looking position for head rotation, etc.
     * @param lookX         The entity's pitch looking position for head rotation, etc.
     * @param scale         The base scale to render the model at, usually just 0.0625F which scales 1m unit in Blender to a 1m block unit in Minecraft.
     * @param brightness    The brightness of the mob based on block location, etc.
     */
    public void render(BaseProjectileEntity entity, PoseStack matrixStack, VertexConsumer vertexBuilder, LayerProjectileBase layer, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness) {
        this.matrixStack = matrixStack;
        float sizeScale = 1F;
        if (entity != null) {
            sizeScale *= entity.getProjectileScale();
        }
        this.doScale(sizeScale, sizeScale, sizeScale);
        this.doTranslate(0, 0.5f - sizeScale / 2, 0);
    }

    /**
     * Returns true if the part can be rendered, this can do various checks such as Yale wool only rendering in the YaleWoolLayer or hiding body parts in place of armor parts, etc.
     **/
    public boolean canRenderPart(String partName, BaseProjectileEntity entity, LayerProjectileBase layer) {
        if (layer == null)
            return this.canBaseRenderPart(partName, entity);
        if (entity != null)
            return layer.canRenderPart(partName, entity);
        return false;
    }

    /**
     * Returns true if the part can be rendered on the base layer.
     **/
    public boolean canBaseRenderPart(String partName, BaseProjectileEntity entity) {
        return true;
    }

    /**
     * Returns the coloring to be used for this part and layer.
     **/
    public Vector4f getPartColor(String partName, BaseProjectileEntity entity, LayerProjectileBase layer, float loop) {
        if (layer == null || entity == null)
            return this.getBasePartColor(partName, entity, loop);
        return layer.getPartColor(partName, entity);
    }

    /**
     * Returns the coloring to be used for this part on the base layer.
     **/
    public Vector4f getBasePartColor(String partName, BaseProjectileEntity entity, float loop) {
        return CustomRenderStates.WHITE;
    }

    /**
     * Returns the texture offset to be used for this part and layer.
     **/
    public Vector2f getPartTextureOffset(String partName, BaseProjectileEntity entity, LayerProjectileBase layer, float loop) {
        if (layer == null || !(entity instanceof BaseProjectileEntity))
            return this.getBaseTextureOffset(partName, entity, loop);
        return layer.getTextureOffset(partName, entity, loop);
    }

    /**
     * Returns the texture offset to be used for this part on the base layer (for scrolling, etc).
     **/
    public Vector2f getBaseTextureOffset(String partName, BaseProjectileEntity entity, float loop) {
        return new Vector2f(0, 0);
    }

    /**
     * Gets the brightness to render the given part at.
     *
     * @param partName   The name of the part to render.
     * @param layer      The layer to render, null for base layer.
     * @param entity     The entity to render.
     * @param brightness The base brightness of the entity based on location.
     * @return The brightness to render at.
     */
    public int getBrightness(String partName, LayerProjectileBase layer, BaseProjectileEntity entity, int brightness) {
        if (layer != null) {
            return layer.getBrightness(partName, entity, brightness);
        }
        return brightness;
    }

    /**
     * Gets the brightness to render the given part at.
     *
     * @param entity The entity to render.
     * @param layer  The layer to render, null for base layer.
     * @return The brightness to render at.
     */
    public int getBlending(BaseProjectileEntity entity, LayerProjectileBase layer) {
        if (layer != null) {
            return layer.getBlending(entity);
        }
        return CustomRenderStates.BLEND.NORMAL.getValue();
    }

    /**
     * Gets the brightness to render the given part at.
     *
     * @param entity The entity to render.
     * @param layer  The layer to render, null for base layer.
     * @return The brightness to render at.
     */
    public boolean getGlow(BaseProjectileEntity entity, LayerProjectileBase layer) {
        if (layer != null) {
            return layer.getGlow(entity);
        }
        return false;
    }

    @Override
    public void rotate(float rotX, float rotY, float rotZ) {
    }

    @Override
    public void angle(float rotation, float angleX, float angleY, float angleZ) {
    }

    @Override
    public void translate(float posX, float posY, float posZ) {
    }

    @Override
    public void scale(float scaleX, float scaleY, float scaleZ) {
    }

    @Override
    public void doRotate(float rotX, float rotY, float rotZ) {
        if (rotX != 0.0F) {
            this.matrixStack.mulPose(Axis.XP.rotationDegrees(rotX));
        }
        if (rotY != 0.0F) {
            this.matrixStack.mulPose(Axis.YP.rotationDegrees(rotY));
        }
        if (rotZ != 0.0F) {
            this.matrixStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
        }
    }

    @Override
    public void doAngle(float rotation, float angleX, float angleY, float angleZ) {
        if (rotation != 0.0F && (angleX != 0.0F || angleY != 0.0F || angleZ != 0.0F)) {
            this.matrixStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(rotation), angleX, angleY, angleZ)));
        }
    }

    @Override
    public void doTranslate(float posX, float posY, float posZ) {
        if (posX != 0.0F || posY != 0.0F || posZ != 0.0F) {
            this.matrixStack.translate(posX, posY, posZ); // TODO Translation?
        }
    }

    @Override
    public void doScale(float scaleX, float scaleY, float scaleZ) {
        if (scaleX != 1.0F || scaleY != 1.0F || scaleZ != 1.0F) {
            this.matrixStack.scale(scaleX, scaleY, scaleZ); // TODO Scaling?
        }
    }

    @Override
    public double rotateToPoint(double aTarget, double bTarget) {
        return rotateToPoint(0, 0, aTarget, bTarget);
    }

    @Override
    public double rotateToPoint(double aCenter, double bCenter, double aTarget, double bTarget) {
        if (aTarget - aCenter == 0)
            if (aTarget > aCenter) return 0;
            else if (aTarget < aCenter) return 180;
        if (bTarget - bCenter == 0)
            if (bTarget > bCenter) return 90;
            else if (bTarget < bCenter) return -90;
        if (aTarget - aCenter == 0 && bTarget - bCenter == 0)
            return 0;
        return Math.toDegrees(Math.atan2(aCenter - aTarget, bCenter - bTarget) - Math.PI / 2);
    }

    @Override
    public double[] rotateToPoint(double xCenter, double yCenter, double zCenter, double xTarget, double yTarget, double zTarget) {
        double[] rotations = new double[3];
        rotations[0] = this.rotateToPoint(yCenter, -zCenter, yTarget, -zTarget);
        rotations[1] = this.rotateToPoint(-zCenter, xCenter, -zTarget, xTarget);
        rotations[2] = this.rotateToPoint(yCenter, xCenter, yTarget, xTarget);
        return rotations;
    }
}
