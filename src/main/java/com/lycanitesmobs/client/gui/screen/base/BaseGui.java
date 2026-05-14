package com.lycanitesmobs.client.gui.screen.base;

import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

public abstract class BaseGui extends Screen {
    public BaseGui(Component screenName) {
        super(screenName);
    }

    public static void renderLivingEntityRotated(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int scale,
            float yawDegrees,
            float pitchDegrees,
            LivingEntity entity
    ) {
        var mc = Minecraft.getInstance();
        var dispatcher = mc.getEntityRenderDispatcher();
        var pose = guiGraphics.pose();

        float oldBodyRot = entity.yBodyRot;
        float oldBodyRotO = entity.yBodyRotO;
        float oldYRot = entity.getYRot();
        float oldYRotO = entity.yRotO;
        float oldXRot = entity.getXRot();
        float oldXRotO = entity.xRotO;
        float oldHeadO = entity.yHeadRotO;
        float oldHead = entity.yHeadRot;

        pose.pushPose();
        pose.translate(x, y, 50.0D);
        pose.scale(scale, scale, scale);

        var qBase = Axis.ZP.rotationDegrees(180.0F);

        float yawRad = (float) (yawDegrees * Math.PI / 180.0);
        float pitchRad = (float) (pitchDegrees * Math.PI / 180.0);
        Quaternionf qView = new Quaternionf().rotateYXZ(yawRad, pitchRad, 0.0F);

        pose.mulPose(qBase);
        pose.mulPose(qView);

        entity.yBodyRot = 0.0F;
        entity.yBodyRotO = 0.0F;
        entity.setYRot(0.0F);
        entity.yRotO = 0.0F;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;
        entity.yHeadRotO = 0.0F;
        entity.yHeadRot = 0.0F;

        Lighting.setupForEntityInInventory();
        dispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() ->
                dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, pose, buffer, 15728880)
        );
        // GUI creature renders do not get the world AFTER_ENTITIES hook, so drain any
        // deferred cutout batches here while the preview pose is still active.
        VBOBatcher.getInstance().endDeferredBatches();
        buffer.endBatch();
        dispatcher.setRenderShadow(true);
        pose.popPose();
        Lighting.setupFor3DItems();

        entity.yBodyRot = oldBodyRot;
        entity.yBodyRotO = oldBodyRotO;
        entity.setYRot(oldYRot);
        entity.yRotO = oldYRotO;
        entity.setXRot(oldXRot);
        entity.xRotO = oldXRotO;
        entity.yHeadRotO = oldHeadO;
        entity.yHeadRot = oldHead;
    }

    public static void renderLivingEntity(GuiGraphics guiGraphics, int x, int y, float scale, float lookX, float lookY, LivingEntity entity) {
        PoseStack matrixStack = guiGraphics.pose();
        float lookXRot = (float) Math.atan(lookX / 40.0F);
        float lookYRot = (float) Math.atan(lookY / 40.0F);

        matrixStack.pushPose();
        matrixStack.translate(x, y, 1500.0F);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        matrixStack.translate(0.0D, 0.0D, 1000.0D);
        matrixStack.scale(scale, scale, scale);

        Quaternionf modelRotationRoll = Axis.ZP.rotationDegrees(180.0F);
        Quaternionf modelRotationPitch = Axis.XP.rotationDegrees(lookYRot * 20.0F);
        modelRotationRoll.mul(modelRotationPitch);
        matrixStack.mulPose(modelRotationRoll);
        matrixStack.mulPose(Axis.YN.rotationDegrees(180.0F));

        float renderYawOffset = entity.yBodyRot;
        float prevRenderYawOffset = entity.yBodyRotO;
        float rotationYaw = entity.getYRot();
        float prevRotationYaw = entity.yRotO;
        float rotationPitch = entity.getXRot();
        float prevRotationPitch = entity.xRotO;
        float prevRotationYawHead = entity.yHeadRotO;
        float rotationYawHead = entity.yHeadRot;

        entity.yBodyRot = lookXRot * 20.0F;
        entity.yBodyRotO = entity.yBodyRot;
        entity.setYRot(lookXRot * 40.0F);
        entity.yRotO = entity.getYRot();
        entity.setXRot(-lookYRot * 20.0F);
        entity.xRotO = entity.getXRot();
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        entity.setOnGround(true);

        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        modelRotationPitch.conjugate();
        renderManager.overrideCameraOrientation(modelRotationPitch);
        renderManager.setRenderShadow(false);
        MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderManager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, renderTypeBuffer, 15728880);
        // GUI previews bypass the world render stages, so deferred creature batches
        // need an explicit flush here or cutout parts render late with the wrong pose.
        VBOBatcher.getInstance().endDeferredBatches();
        renderTypeBuffer.endBatch();
        renderManager.setRenderShadow(true);

        entity.yBodyRot = renderYawOffset;
        entity.yBodyRotO = prevRenderYawOffset;
        entity.setYRot(rotationYaw);
        entity.yRotO = prevRotationYaw;
        entity.setXRot(rotationPitch);
        entity.xRotO = prevRotationPitch;
        entity.yHeadRotO = prevRotationYawHead;
        entity.yHeadRot = rotationYawHead;

        matrixStack.popPose();
    }

}
