package com.lycanitesmobs.client.model.creature.base;

import com.lycanitesmobs.client.model.animation.IAnimationModel;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureEquipment;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureSaddle;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public abstract class CreatureModel<T extends BaseCreatureEntity> extends EntityModel<T> implements IAnimationModel {

    // Matrix:
    public PoseStack matrixStack;

    public CreatureModel() {
        this(1.0F);
    }

    public CreatureModel(float shadowSize) {
        // Texture:
    	/*this.texWidth = 128;
		this.texHeight = 128;*/
    }

    @Override
    public void setupAnim(BaseCreatureEntity entity, float time, float distance, float loop, float lookY, float lookX) {
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexBuilder, int someIntA, int someIntB, float someFloatA, float someFloatB, float someFloatC, float someFloatD) {
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
    public void generateAnimationFrames(BaseCreatureEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness) {
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
     * @param fade          The damage fade to render (red flash when damaged).
     */
    public abstract void render(T entity, PoseStack matrixStack, VertexConsumer vertexBuilder, LayerCreatureBase layer, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness, int fade);


    /**
     * Called by the renderer to add custom layers to it.
     *
     * @param renderer The renderer to add layers to.
     */
    public void addCustomLayers(CreatureRenderer renderer) {
        renderer.addLayer(new LayerCreatureEquipment(renderer, "chest"));
        renderer.addLayer(new LayerCreatureSaddle(renderer));
    }

    /**
     * Returns true if the part can be rendered, this can do various checks such as Yale wool only rendering in the YaleWoolLayer or hiding body parts in place of armor parts, etc.
     *
     * @param partName The model part name to render.
     * @param entity   The entity to render.
     * @param layer    The layer to render. Null for base layer
     * @param trophy   If true, the model is being a rendered as a trophy block, etc.
     * @return True if the part can be rendered.
     */
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if (layer == null)
            return this.canBaseRenderPart(partName, entity, trophy);
        if (entity instanceof BaseCreatureEntity)
            return layer.canRenderPart(partName, (BaseCreatureEntity) entity, trophy);
        return false;
    }

    /**
     * Returns true if the part can be rendered on the base layer.
     *
     * @param partName The model part name to render.
     * @param entity   The entity to render.
     * @param trophy   If true, the model is being a rendered as a trophy block, etc.
     * @return True if the part can be rendered.
     */
    public boolean canBaseRenderPart(String partName, Entity entity, boolean trophy) {
        return true;
    }

    /**
     * Returns the coloring to be used for this part and layer.
     *
     * @param partName The model part name to render.
     * @param entity   The entity to render.
     * @param layer    The layer to render. Null for base layer
     * @param trophy   If true, the model is being a rendered as a trophy block, etc.
     * @param loop     The animation tick for looping effects.
     * @return The color to render the part at.
     */
    public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
        if (layer == null || !(entity instanceof BaseCreatureEntity))
            return this.getBasePartColor(partName, entity, trophy, loop);
        return layer.getPartColor(partName, (BaseCreatureEntity) entity, trophy);
    }

    /**
     * Returns the coloring to be used for this part on the base layer.
     *
     * @param partName The model part name to render.
     * @param entity   The entity to render.
     * @param trophy   If true, the model is being a rendered as a trophy block, etc.
     * @param loop     The animation tick for looping effects.
     * @return The color to render the part at.
     */
    public Vector4f getBasePartColor(String partName, Entity entity, boolean trophy, float loop) {
        return CustomRenderStates.WHITE;
    }

    /**
     * Returns the texture offset to be used for this part and layer.
     *
     * @param partName The model part name to render.
     * @param entity   The entity to render.
     * @param layer    The layer to render. Null for base layer
     * @param trophy   If true, the model is being a rendered as a trophy block, etc.
     * @param loop     The animation tick for looping effects.
     * @return The texture offset to render the part at.
     */
    public Vector2f getPartTextureOffset(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
        if (layer == null || !(entity instanceof BaseCreatureEntity))
            return this.getBaseTextureOffset(partName, entity, trophy, loop);
        return layer.getTextureOffset(partName, (BaseCreatureEntity) entity, trophy, loop);
    }

    /**
     * Returns the texture offset to be used for this part on the base layer (for scrolling, etc).
     *
     * @param partName The model part name to render.
     * @param entity   The entity to render.
     * @param trophy   If true, the model is being a rendered as a trophy block, etc.
     * @param loop     The animation tick for looping effects.
     * @return The texture offset to render the part at.
     */
    public Vector2f getBaseTextureOffset(String partName, Entity entity, boolean trophy, float loop) {
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
    public int getBrightness(String partName, LayerCreatureBase layer, BaseCreatureEntity entity, int brightness) {
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
    public int getBlending(BaseCreatureEntity entity, LayerCreatureBase layer) {
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
    public boolean getGlow(BaseCreatureEntity entity, LayerCreatureBase layer) {
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
            this.matrixStack.translate(posX, posY, posZ);
        }
    }

    @Override
    public void doScale(float scaleX, float scaleY, float scaleZ) {
        if (scaleX != 1.0F || scaleY != 1.0F || scaleZ != 1.0F) {
            this.matrixStack.scale(scaleX, scaleY, scaleZ);
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
