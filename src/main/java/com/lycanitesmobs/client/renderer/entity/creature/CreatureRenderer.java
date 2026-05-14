package com.lycanitesmobs.client.renderer.entity.creature;

import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.model.creature.base.CreatureModel;
import com.lycanitesmobs.client.obj.model.VBOObjModel;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.core.compatibility.OculusCompat;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

@OnlyIn(Dist.CLIENT)
public class CreatureRenderer<T extends BaseCreatureEntity> extends MobRenderer<T, CreatureModel<T>> {
    public CreatureRenderer(String entityID, EntityRendererProvider.Context renderManager, float shadowSize) {
        super(renderManager, ModelManager.getInstance().getCreatureModel(CreatureManager.getInstance().getCreature(entityID), null), shadowSize);
        if (this.model == null)
            return;

    }

    /**
     * Called by the main entity renderer to perform rendering.
     *
     * @param entity           The entity to render.
     * @param partialTicks     The partial fraction of the animation tick.
     * @param yaw              The yaw rotation of the entity.
     * @param matrixStack      The entity matrix stack for animating with, etc.
     * @param renderTypeBuffer The render type buff for rendering with.
     * @param brightness       The brightness of the mob based on block location, etc.
     */
    @Override
    public void render(T entity, float partialTicks, float yaw, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int brightness) {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(entity, this, partialTicks, matrixStack, renderTypeBuffer, brightness))) {
            return;
        }

        // Get Model and Layers:
        try {
            this.layers.clear();
            this.model = ModelManager.getInstance().getCreatureModel(entity.creatureInfo, entity.subspecies);
            if (this.model == null) {
                return;
            }
            this.model.addCustomLayers(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get Entity States:
        float scale = 1;
        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        this.model.riding = shouldSit;
        this.model.young = entity.isBaby();
        float renderYaw = Mth.clamp(yaw, entity.yBodyRotO, entity.yBodyRot);
        float renderYawHead = Mth.clamp(yaw, entity.yHeadRotO, entity.yHeadRot);

        // Looking Yaw:
        float lookYaw = renderYawHead - renderYaw;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) entity.getVehicle();
            renderYaw = Mth.clamp(yaw, livingentity.yBodyRotO, livingentity.yBodyRot);
            lookYaw = renderYawHead - renderYaw;
            float renderYawMountOffset = Mth.wrapDegrees(lookYaw);
            if (renderYawMountOffset < -85.0F) {
                renderYawMountOffset = -85.0F;
            }

            if (renderYawMountOffset >= 85.0F) {
                renderYawMountOffset = 85.0F;
            }

            renderYaw = renderYawHead - renderYawMountOffset;
            if (renderYawMountOffset * renderYawMountOffset > 2500.0F) {
                renderYaw += renderYawMountOffset * 0.2F;
            }

            lookYaw = renderYawHead - renderYaw;
        }

        float time = 0.0F;
        int fade = 0;
        boolean invisible = false;

        // Looking Pitch:
        matrixStack.pushPose();
        VBOObjModel.deferFlush = true;
        try {
            float lookPitch = Mth.lerp(yaw, entity.xRotO, entity.xRot);

            // Animation Ticks:
            float loop = this.getBob(entity, partialTicks % 1.0F);
            this.setupRotations(entity, matrixStack, loop, renderYaw, yaw);
            matrixStack.scale(-1.0F, -1.0F, 1.0F);
            this.scale(entity, matrixStack, yaw);
            matrixStack.translate(0.0D, (double) -1.501F, 0.0D);
            float distance = 0.0F;
            if (!shouldSit && entity.isAlive()) {
                distance = entity.walkAnimation.speed(yaw);
                time = entity.walkAnimation.position() - entity.walkAnimation.speed() * (1.0F - yaw);
                if (entity.isBaby()) {
                    time *= 3.0F;
                }

                if (distance > 1.0F) {
                    distance = 1.0F;
                }
            }

            fade = Math.max(entity.hurtTime, 0);
            invisible = !this.isBodyVisible(entity);
            boolean allyInvisible = invisible && !entity.isInvisibleTo(Minecraft.getInstance().player);

            // Render Model Layers:
            this.getMainModel().generateAnimationFrames(entity, time, distance, loop, lookYaw, lookPitch, 1, brightness);
            this.renderModel(entity, matrixStack, renderTypeBuffer, null, time, distance, loop, lookYaw, lookPitch, 1, brightness, fade, invisible, allyInvisible);
            for (RenderLayer<T, CreatureModel<T>> layer : this.layers) {
                if (!(layer instanceof LayerCreatureBase)) {
                    continue;
                }
                LayerCreatureBase layerCreatureBase = (LayerCreatureBase) layer;
                if (!layerCreatureBase.canRenderLayer(entity, scale)) {
                    continue;
                }
                this.renderModel(entity, matrixStack, renderTypeBuffer, layerCreatureBase, time, distance, loop, lookYaw, lookPitch, scale, brightness, fade, invisible, allyInvisible);
            }
            this.getMainModel().clearAnimationFrames();
            matrixStack.popPose();
            VBOBatcher.getInstance().endBatches();
        } finally {
            VBOObjModel.deferFlush = false;
        }

        // Render Name Tag
        if (!invisible) {
            RenderNameTagEvent renderNameplateEvent = new RenderNameTagEvent(entity, entity.getDisplayName(), this, matrixStack, renderTypeBuffer, fade, time);
            MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
            if (renderNameplateEvent.getResult() != Event.Result.DENY && (renderNameplateEvent.getResult() == Event.Result.ALLOW || this.shouldShowName(entity))) {
                MutableComponent entityName = entity.getName().copy();
                if (entityName != null) {
                    this.renderNameTag(entity, entityName, matrixStack, renderTypeBuffer, brightness);
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entity, this, partialTicks, matrixStack, renderTypeBuffer, brightness));
    }


    public ResourceLocation texture;

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
     * @param fade             The damage fade to render (red flash when damaged).
     * @param invisible        If true, the entity has invisibility or some form of stealth.
     * @param allyInvisible    If true, the entity has invisibility or some form of stealth but is allied to the player so should be translucent, etc.
     */
    protected void renderModel(BaseCreatureEntity entity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, LayerCreatureBase layer, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness, int fade, boolean invisible, boolean allyInvisible) {

        texture = this.getEntityTexture(entity, layer);

        try {
            if (OculusCompat.isShaderPackActive()) {
                if (!invisible || allyInvisible) {
                    RenderType renderType = OculusCompat.getIrisEntityRenderType(texture, this.getMainModel().getBlending(entity, layer), this.getMainModel().getGlow(entity, layer));
                    VBOObjModel.tex = texture;
                    this.getMainModel().render(entity, matrixStack, renderTypeBuffer.getBuffer(renderType), layer, time, distance, loop, lookY, lookX, 1, brightness, fade);
                }
                return;
            }

            if (!invisible || allyInvisible) {
                VBOObjModel.renderType = CustomRenderStates.getObjVBORenderType(this.getMainModel().getBlending(entity, layer), this.getMainModel().getGlow(entity, layer));
                VBOObjModel.renderNormal = true;
            }
            if (entity.isCurrentlyGlowing()) {
                VBOObjModel.renderOutline = true;
            }
            VBOObjModel.tex = texture;
            this.getMainModel().render(entity, matrixStack, null, layer, time, distance, loop, lookY, lookX, 1, brightness, fade);
        } finally {
            VBOObjModel.tex = null;
            VBOObjModel.renderOutline = false;
            VBOObjModel.renderNormal = false;
            VBOObjModel.renderType = null;
        }
    }

    /**
     * Returns the main model used by this renderer.
     *
     * @return The main model to render.
     */
    public CreatureModel getMainModel() {
        return this.model;
    }

    /**
     * Gets the texture to use.
     *
     * @param entity The entity to get the texture from.
     * @param layer  The layer to get the texture for.
     * @return The texture to bind.
     */
    public ResourceLocation getEntityTexture(BaseCreatureEntity entity, LayerCreatureBase layer) {
        if (layer == null) {
            return this.getTextureLocation(entity);
        }
        ResourceLocation layerTexture = layer.getLayerTexture(entity);
        return layerTexture != null ? layerTexture : this.getTextureLocation(entity);
    }

    @Override
    public ResourceLocation getTextureLocation(BaseCreatureEntity entity) {
        return entity.getTexture();
    }

    /**
     * If true, display the name of the entity above it.
     **/
    @Override
    protected boolean shouldShowName(BaseCreatureEntity entity) {
        if (!Minecraft.renderNames()) {
            return false;
        }
        if (entity.isInvisibleTo(Minecraft.getInstance().player)) {
            return false;
        }
        if (entity.getControllingPassenger() != null || entity.hasPerchTarget()) {
            return false;
        }

        if (entity.hasCustomName()) {
            return true;
        }
        if (entity.shouldShowName() && !entity.isTamed()) {
            return true;
        }

        return false; //entity == this.entityRenderDispatcher.crosshairPickEntity;
    }

    /**
     * Returns a rotation angle that is inbetween two other rotation angles. par1 and par2 are the angles between which
     * to interpolate, par3 is probably a float between 0.0 and 1.0 that tells us where "between" the two angles we are.
     * Example: par1 = 30, par2 = 50, par3 = 0.5, then return = 40
     */
    public float interpolateRotation(float par1, float par2, float par3) {
        float f3;

        for (f3 = par2 - par1; f3 < -180.0F; f3 += 360.0F) {
        }

        while (f3 >= 180.0F) {
            f3 -= 360.0F;
        }

        return par1 + par3 * f3;
    }

    /**
     * Overidden for smoother animations based on the new Schism renderers.
     *
     * @param creatureEntity The creature entity to get ticks for.
     * @param particalTicks  The tick delta per render frame.
     * @return An incremental render tick for animation loops, should only be called once per render frame.
     */
    @Override
    protected float getBob(BaseCreatureEntity creatureEntity, float particalTicks) {
        if (!Minecraft.getInstance().isPaused()) {
            creatureEntity.renderTick += Minecraft.getInstance().getDeltaFrameTime();
        }
        return creatureEntity.renderTick;
    }
}
