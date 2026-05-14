package com.lycanitesmobs.client.renderer.entity.effect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.core.entity.creature.aberration.EntityFear;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FearRenderer extends EntityRenderer<EntityFear> {

    private static final ResourceLocation WHITE_TEX =
            new ResourceLocation(LycanitesMobs.MODID, "textures/effect/white.png");

    private static final Map<UUID, FearMesh> meshes = new HashMap<>();

    private static final float ALPHA = 0.3F;
    private static final float COLOR_R = 0.02F;
    private static final float COLOR_G = 0.02F;
    private static final float COLOR_B = 0.02F;

    private FearMeshProfile profile;

    public FearRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;
        this.profile = FearMeshProfile.dementor();
        activeInstance = this;
    }

    /**
     * Rebuilds the profile from FearMeshProfile.dementor() and clears all cached meshes
     * so they get recreated with the new values on the next render frame.
     */
    public static void refreshProfile() {
        meshes.values().forEach(FearMesh::dispose);
        meshes.clear();
        if (activeInstance != null) {
            activeInstance.profile = FearMeshProfile.dementor();
        }
    }

    private static FearRenderer activeInstance;

    @Override
    public ResourceLocation getTextureLocation(EntityFear entity) {
        return WHITE_TEX;
    }

    @Override
    public void render(EntityFear entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
       /* UUID id = entity.getUUID();
        FearMesh mesh = meshes.computeIfAbsent(id, k -> new FearMesh(profile));

        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        float velX = (float) (entity.getX() - entity.xOld);
        float velZ = (float) (entity.getZ() - entity.zOld);
        float hDist = (float) Math.sqrt(velX * velX + velZ * velZ);

        mesh.tick(yaw, pitch, hDist, partialTick);

        VertexBuffer vbo = mesh.getVbo();
        if (vbo == null || mesh.getVertexCount() == 0) return;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - mesh.getSmoothedYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(mesh.getSmoothedPitch()));

        Matrix4f modelMatrix = poseStack.last().pose();
        RenderType renderType = CustomRenderStates.FEAR_RENDER_TYPE;

        VBOBatcher.VBODrawCommand cmd = new VBOBatcher.VBODrawCommand(
                vbo, mesh.getVertexCount(), mesh.getVertexFormat(),
                modelMatrix, WHITE_TEX);
        cmd.setColor(COLOR_R, COLOR_G, COLOR_B, 0.03f);
        cmd.setLightOffset(packedLight);
        cmd.setOverlayOffset(0.0F, false);

        VBOBatcher.getInstance().queue(renderType, cmd);
        VBOBatcher.getInstance().endBatches();

        poseStack.popPose();*/
    }

    public static void cleanupEntity(UUID entityId) {
        FearMesh mesh = meshes.remove(entityId);
        if (mesh != null) {
            mesh.dispose();
        }
    }

    public static void cleanupAll() {
        meshes.values().forEach(FearMesh::dispose);
        meshes.clear();
    }
}
