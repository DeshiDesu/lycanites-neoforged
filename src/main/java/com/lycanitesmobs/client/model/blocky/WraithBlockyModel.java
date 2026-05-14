package com.lycanitesmobs.client.model.blocky;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class WraithBlockyModel<T extends Entity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(LycanitesMobs.MODID, "wraith"), "main");
    private final ModelPart All;
    private final ModelPart Skull;
    private final ModelPart Skull2;
    private final ModelPart Skull3;
    private final ModelPart Skull4;
    private final ModelPart Skull5;
    private final ModelPart Skull6;
    private final ModelPart Skull7;
    private final ModelPart Skull8;
    private final ModelPart Skull9;
    private final ModelPart Skull10;
    private final ModelPart Skull11;
    private final ModelPart Skull12;
    private final ModelPart Skull13;
    private final ModelPart Mouth;
    private final ModelPart Skull14;
    private final ModelPart Skull15;
    private final ModelPart Skull18;
    private final ModelPart Skull16;
    private final ModelPart Skull17;
    private final ModelPart Skull19;
    private final ModelPart Flame;
    private final ModelPart RightFire;
    private final ModelPart bone;
    private final ModelPart LeftFire;
    private final ModelPart bone2;

    public WraithBlockyModel(ModelPart root) {
        this.All = root.getChild("All");
        this.Skull = this.All.getChild("Skull");
        this.Skull2 = this.Skull.getChild("Skull2");
        this.Skull3 = this.Skull2.getChild("Skull3");
        this.Skull4 = this.Skull3.getChild("Skull4");
        this.Skull5 = this.Skull.getChild("Skull5");
        this.Skull6 = this.Skull5.getChild("Skull6");
        this.Skull7 = this.Skull6.getChild("Skull7");
        this.Skull8 = this.Skull.getChild("Skull8");
        this.Skull9 = this.Skull8.getChild("Skull9");
        this.Skull10 = this.Skull9.getChild("Skull10");
        this.Skull11 = this.Skull.getChild("Skull11");
        this.Skull12 = this.Skull11.getChild("Skull12");
        this.Skull13 = this.Skull12.getChild("Skull13");
        this.Mouth = this.Skull.getChild("Mouth");
        this.Skull14 = this.Skull.getChild("Skull14");
        this.Skull15 = this.Skull14.getChild("Skull15");
        this.Skull18 = this.Skull15.getChild("Skull18");
        this.Skull16 = this.Skull.getChild("Skull16");
        this.Skull17 = this.Skull16.getChild("Skull17");
        this.Skull19 = this.Skull17.getChild("Skull19");
        this.Flame = this.All.getChild("Flame");
        this.RightFire = this.Flame.getChild("RightFire");
        this.bone = this.RightFire.getChild("bone");
        this.LeftFire = this.Flame.getChild("LeftFire");
        this.bone2 = this.LeftFire.getChild("bone2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 0.0F));

        PartDefinition Skull = All.addOrReplaceChild("Skull", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(20, 30).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition Skull2 = Skull.addOrReplaceChild("Skull2", CubeListBuilder.create().texOffs(35, 0).addBox(-5.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -5.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        PartDefinition Skull3 = Skull2.addOrReplaceChild("Skull3", CubeListBuilder.create().texOffs(35, 10).addBox(-5.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, -0.1745F, -0.2618F));

        PartDefinition Skull4 = Skull3.addOrReplaceChild("Skull4", CubeListBuilder.create().texOffs(35, 18).addBox(-5.0F, -1.0F, -2.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 1.0F, 0.0F, -0.48F, 0.0F));

        PartDefinition Skull5 = Skull.addOrReplaceChild("Skull5", CubeListBuilder.create().texOffs(55, 0).addBox(0.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -5.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

        PartDefinition Skull6 = Skull5.addOrReplaceChild("Skull6", CubeListBuilder.create().texOffs(55, 10).addBox(-1.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.1745F, 0.2618F));

        PartDefinition Skull7 = Skull6.addOrReplaceChild("Skull7", CubeListBuilder.create().texOffs(55, 18).addBox(0.0F, 0.0F, -2.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.0F, 1.0F, 0.0F, 0.48F, 0.0F));

        PartDefinition Skull8 = Skull.addOrReplaceChild("Skull8", CubeListBuilder.create().texOffs(73, 0).addBox(-5.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -5.0F, 0.0F, -0.6109F, 0.0F, 0.3491F));

        PartDefinition Skull9 = Skull8.addOrReplaceChild("Skull9", CubeListBuilder.create().texOffs(73, 10).addBox(-5.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 1.2217F, -0.2618F));

        PartDefinition Skull10 = Skull9.addOrReplaceChild("Skull10", CubeListBuilder.create().texOffs(73, 18).addBox(-5.0F, -1.0F, -2.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 1.0F, 0.0F, -0.48F, 0.0F));

        PartDefinition Skull11 = Skull.addOrReplaceChild("Skull11", CubeListBuilder.create().texOffs(93, 0).addBox(0.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -5.0F, 0.0F, -0.6109F, 0.0F, -0.3491F));

        PartDefinition Skull12 = Skull11.addOrReplaceChild("Skull12", CubeListBuilder.create().texOffs(93, 10).addBox(-1.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, -1.2217F, 0.2618F));

        PartDefinition Skull13 = Skull12.addOrReplaceChild("Skull13", CubeListBuilder.create().texOffs(93, 18).addBox(0.0F, 0.0F, -2.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.0F, 1.0F, 0.0F, 0.48F, 0.0F));

        PartDefinition Mouth = Skull.addOrReplaceChild("Mouth", CubeListBuilder.create().texOffs(0, 30).addBox(-3.0F, 0.0F, -5.5F, 6.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 39).addBox(-3.0F, -1.0F, -5.5F, 6.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition Skull14 = Skull.addOrReplaceChild("Skull14", CubeListBuilder.create().texOffs(73, 22).addBox(-5.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -5.0F, 0.0F, -0.2618F, 0.0F, 0.5672F));

        PartDefinition Skull15 = Skull14.addOrReplaceChild("Skull15", CubeListBuilder.create().texOffs(73, 30).addBox(-5.0F, -2.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(73, 41).addBox(-6.0F, 0.0F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.48F));

        PartDefinition Skull18 = Skull15.addOrReplaceChild("Skull18", CubeListBuilder.create().texOffs(74, 37).addBox(-4.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 0.0F, -1.0908F));

        PartDefinition Skull16 = Skull.addOrReplaceChild("Skull16", CubeListBuilder.create().texOffs(93, 22).addBox(-1.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -5.0F, 0.0F, -0.2618F, 0.0F, -0.5672F));

        PartDefinition Skull17 = Skull16.addOrReplaceChild("Skull17", CubeListBuilder.create().texOffs(93, 30).addBox(0.0F, -2.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(87, 41).addBox(-1.0F, 0.0F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 1.0F, 0.0F, 0.0F, 0.0F, -0.48F));

        PartDefinition Skull19 = Skull17.addOrReplaceChild("Skull19", CubeListBuilder.create().texOffs(94, 37).addBox(-1.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.0F, 0.0F, 0.0F, 0.0F, 1.0908F));

        PartDefinition Flame = All.addOrReplaceChild("Flame", CubeListBuilder.create().texOffs(0, 34).addBox(-3.5F, -7.5F, -3.5F, 7.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 51).mirror().addBox(-2.5F, -5.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.01F)).mirror(false)
                .texOffs(258, 8).addBox(-2.5F, -5.5F, -2.5F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 8).addBox(-1.5F, -5.5F, -2.5F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 8).addBox(0.5F, -5.5F, -2.5F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 8).addBox(1.5F, -5.5F, -2.5F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition RightFire = Flame.addOrReplaceChild("RightFire", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.5F, 1.0F, -2.5F, 0.0F, 0.6981F, 0.0F));

        PartDefinition bone = RightFire.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(258, 8).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 24).addBox(-2.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 40).addBox(-3.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 56).addBox(-4.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 72).addBox(-5.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 88).addBox(-6.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 104).addBox(-7.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 120).addBox(-8.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 136).addBox(-9.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 152).addBox(-10.0F, -5.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 0.0F));

        PartDefinition LeftFire = Flame.addOrReplaceChild("LeftFire", CubeListBuilder.create(), PartPose.offsetAndRotation(2.5F, 1.0F, -2.5F, 0.0F, -0.6981F, 0.0F));

        PartDefinition bone2 = LeftFire.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(258, 8).addBox(0.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 24).addBox(1.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 40).addBox(2.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 56).addBox(3.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 72).addBox(4.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 88).addBox(5.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 104).addBox(6.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 120).addBox(7.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 136).addBox(8.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(258, 152).addBox(9.0F, -6.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 260, 160);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float loop = ageInTicks;
        float vibration = loop * 2.0F;
        this.Skull.x += Mth.cos(vibration) * 0.01F;
        this.Skull.y += Mth.cos(vibration) * 0.01F;
        this.Skull.z += Mth.cos(vibration) * 0.01F;
        this.Mouth.xRot += -(Mth.cos(vibration) * 0.025F);
        if (entity instanceof BaseCreatureEntity creature && creature.hasAttackTarget()) {
            float base = (float) (Mth.PI / 9.0);
            this.Mouth.xRot += base + -(Mth.cos(loop) * 0.1F);
        }
    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        All.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return All;
    }
}