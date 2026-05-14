package com.lycanitesmobs.client.model.blocky;// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class NymphBlockyModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("lycanites", "nymph_layer"), "main");
    private final ModelPart All;
    private final ModelPart Body;
    private final ModelPart ArmRight;
    private final ModelPart ArmRight2;
    private final ModelPart ArmLeft;
    private final ModelPart ArmLeft2;
    private final ModelPart Head;
    private final ModelPart bone5;
    private final ModelPart Head3;
    private final ModelPart Head2;
    private final ModelPart bone6;
    private final ModelPart WingLeftTop;
    private final ModelPart bone4;
    private final ModelPart WingLeftBottom;
    private final ModelPart bone3;
    private final ModelPart WingRightTop;
    private final ModelPart bone2;
    private final ModelPart WingRightBottom;
    private final ModelPart bone;

    public NymphBlockyModel(ModelPart root) {
        this.All = root.getChild("All");
        this.Body = this.All.getChild("Body");
        this.ArmRight = this.All.getChild("ArmRight");
        this.ArmRight2 = this.ArmRight.getChild("ArmRight2");
        this.ArmLeft = this.All.getChild("ArmLeft");
        this.ArmLeft2 = this.ArmLeft.getChild("ArmLeft2");
        this.Head = this.All.getChild("Head");
        this.bone5 = this.Head.getChild("bone5");
        this.Head3 = this.Head.getChild("Head3");
        this.Head2 = this.Head.getChild("Head2");
        this.bone6 = this.Head.getChild("bone6");
        this.WingLeftTop = this.All.getChild("WingLeftTop");
        this.bone4 = this.WingLeftTop.getChild("bone4");
        this.WingLeftBottom = this.All.getChild("WingLeftBottom");
        this.bone3 = this.WingLeftBottom.getChild("bone3");
        this.WingRightTop = this.All.getChild("WingRightTop");
        this.bone2 = this.WingRightTop.getChild("bone2");
        this.WingRightBottom = this.All.getChild("WingRightBottom");
        this.bone = this.WingRightBottom.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Body = All.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Body_r1 = Body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(56, 13).addBox(-0.5F, -0.5F, 0.0F, 5.0F, 15.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, -2.0F, -0.0436F, 0.0873F, -0.1745F));

        PartDefinition Body_r2 = Body.addOrReplaceChild("Body_r2", CubeListBuilder.create().texOffs(59, 33).addBox(-0.5F, -1.5F, -1.0F, 5.0F, 17.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.0F, -0.5F, 0.2182F, -0.0873F, -0.1745F));

        PartDefinition Body_r3 = Body.addOrReplaceChild("Body_r3", CubeListBuilder.create().texOffs(59, 33).mirror().addBox(-4.5F, -1.5F, -1.0F, 5.0F, 17.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -15.0F, -0.5F, 0.2182F, 0.0873F, 0.1745F));

        PartDefinition Body_r4 = Body.addOrReplaceChild("Body_r4", CubeListBuilder.create().texOffs(56, 13).mirror().addBox(-4.5F, -0.5F, -0.5F, 5.0F, 15.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.0F, -1.5F, -0.0436F, -0.0873F, 0.1745F));

        PartDefinition Body_r5 = Body.addOrReplaceChild("Body_r5", CubeListBuilder.create().texOffs(98, 0).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.2F))
                .texOffs(0, 0).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(98, 11).addBox(-3.5F, -7.0F, -1.5F, 7.0F, 7.0F, 3.0F, new CubeDeformation(0.2F))
                .texOffs(52, 0).addBox(-3.5F, -7.0F, -1.5F, 7.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -0.0349F, 0.0F, 0.0F));

        PartDefinition Body_r6 = Body.addOrReplaceChild("Body_r6", CubeListBuilder.create().texOffs(76, 0).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 19.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition ArmRight = All.addOrReplaceChild("ArmRight", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-7.0F, -1.0F, -1.0F, 7.0F, 4.0F, 3.0F, new CubeDeformation(-0.0009F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -29.0F, 0.0F, 0.1745F, -0.1309F, -0.9599F));

        PartDefinition ArmRight2 = ArmRight.addOrReplaceChild("ArmRight2", CubeListBuilder.create().texOffs(30, 7).mirror().addBox(-7.0F, -4.0F, -3.0F, 7.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(30, 14).mirror().addBox(-7.0F, 0.0F, -3.0F, 7.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.0F, 3.0F, 2.0F, -0.0262F, -0.0873F, 0.5672F));

        PartDefinition ArmLeft = All.addOrReplaceChild("ArmLeft", CubeListBuilder.create().texOffs(30, 0).addBox(0.0F, -1.0F, -1.0F, 7.0F, 4.0F, 3.0F, new CubeDeformation(-0.009F)), PartPose.offsetAndRotation(4.0F, -29.0F, 0.0F, 0.1745F, 0.1309F, 0.9599F));

        PartDefinition ArmLeft2 = ArmLeft.addOrReplaceChild("ArmLeft2", CubeListBuilder.create().texOffs(30, 7).addBox(0.0F, -4.0F, -3.0F, 7.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(30, 14).addBox(0.0F, 0.0F, -3.0F, 7.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 3.0F, 2.0F, -0.0262F, 0.0873F, -0.5672F));

        PartDefinition Head = All.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 14).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(96, 86).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2F))
                .texOffs(0, 30).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.4F))
                .texOffs(112, 81).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -29.0F, 0.0F));

        PartDefinition Head_r1 = Head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(75, 116).addBox(-1.0F, -4.0F, 0.0F, 3.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -3.0F, -2.0F, -0.1745F, 0.1309F, -0.0436F));

        PartDefinition Head_r2 = Head.addOrReplaceChild("Head_r2", CubeListBuilder.create().texOffs(69, 116).addBox(-2.0F, -4.0F, 0.0F, 3.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -3.0F, -2.0F, -0.1745F, -0.1309F, 0.0436F));

        PartDefinition Head_r3 = Head.addOrReplaceChild("Head_r3", CubeListBuilder.create().texOffs(24, 116).addBox(0.0F, 0.0F, 0.0F, 3.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -6.0F, -2.0F, -0.0873F, -0.2182F, -0.1309F));

        PartDefinition Head_r4 = Head.addOrReplaceChild("Head_r4", CubeListBuilder.create().texOffs(6, 116).addBox(0.0F, 0.0F, 0.0F, 3.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -6.0F, -3.0F, -0.1309F, -0.2182F, 0.0436F));

        PartDefinition Head_r5 = Head.addOrReplaceChild("Head_r5", CubeListBuilder.create().texOffs(18, 116).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -6.0F, -2.0F, -0.0873F, 0.2182F, 0.1309F));

        PartDefinition Head_r6 = Head.addOrReplaceChild("Head_r6", CubeListBuilder.create().texOffs(0, 116).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -6.0F, -3.0F, -0.1309F, 0.2182F, -0.0436F));

        PartDefinition Head_r7 = Head.addOrReplaceChild("Head_r7", CubeListBuilder.create().texOffs(120, 116).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 4.0F, 0.1309F, 0.0F, 0.0873F));

        PartDefinition Head_r8 = Head.addOrReplaceChild("Head_r8", CubeListBuilder.create().texOffs(112, 116).addBox(0.0F, 0.0F, 0.0F, 4.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 4.0F, 0.1309F, 0.0F, -0.0873F));

        PartDefinition Head_r9 = Head.addOrReplaceChild("Head_r9", CubeListBuilder.create().texOffs(51, 116).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 4.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition Head_r10 = Head.addOrReplaceChild("Head_r10", CubeListBuilder.create().texOffs(113, 72).addBox(1.0F, -1.0F, -4.8F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(116, 74).addBox(0.0F, -2.0F, -4.6F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -5.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition Head_r11 = Head.addOrReplaceChild("Head_r11", CubeListBuilder.create().texOffs(88, -6).addBox(0.0F, -3.0F, 0.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -2.0F, -1.0F, 0.3491F, 0.7854F, 0.0F));

        PartDefinition Head_r12 = Head.addOrReplaceChild("Head_r12", CubeListBuilder.create().texOffs(88, -6).addBox(0.0F, -3.0F, 0.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.0F, -1.0F, 0.3491F, -0.7854F, 0.0F));

        PartDefinition bone5 = Head.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(78, 54).addBox(-0.2F, 0.0F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.3054F));

        PartDefinition Head3 = Head.addOrReplaceChild("Head3", CubeListBuilder.create().texOffs(110, 35).addBox(-0.2F, 0.0F, -3.0F, 3.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.2182F));

        PartDefinition Head_r13 = Head3.addOrReplaceChild("Head_r13", CubeListBuilder.create().texOffs(113, 72).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(113, 71).addBox(-0.3F, -1.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -2.5F, 0.1745F, -0.1745F, 0.0F));

        PartDefinition Head_r14 = Head3.addOrReplaceChild("Head_r14", CubeListBuilder.create().texOffs(96, 122).addBox(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 1.0F, -0.2182F, -0.6109F, 0.0F));

        PartDefinition Head_r15 = Head3.addOrReplaceChild("Head_r15", CubeListBuilder.create().texOffs(96, 120).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.4363F, -0.7854F, 0.0F));

        PartDefinition Head_r16 = Head3.addOrReplaceChild("Head_r16", CubeListBuilder.create().texOffs(96, 116).addBox(0.0F, -3.0F, 0.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, -2.0F, 0.2618F, -0.5236F, 0.0F));

        PartDefinition Head2 = Head.addOrReplaceChild("Head2", CubeListBuilder.create().texOffs(110, 49).addBox(-2.8F, 0.0F, -3.0F, 3.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -8.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

        PartDefinition Head_r17 = Head2.addOrReplaceChild("Head_r17", CubeListBuilder.create().texOffs(118, 27).addBox(0.2F, 0.0F, -1.0F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 0.6981F, 0.1309F, 0.0F));

        PartDefinition Head_r18 = Head2.addOrReplaceChild("Head_r18", CubeListBuilder.create().texOffs(118, 24).addBox(0.2F, -2.0F, 0.0F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, -1.1781F, 0.2182F, 0.0F));

        PartDefinition Head_r19 = Head2.addOrReplaceChild("Head_r19", CubeListBuilder.create().texOffs(99, 120).addBox(0.0F, -2.0F, 0.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.3054F, 0.7854F, -0.0873F));

        PartDefinition Head_r20 = Head2.addOrReplaceChild("Head_r20", CubeListBuilder.create().texOffs(99, 122).addBox(0.0F, -3.0F, 0.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 0.3491F, 0.6109F, 0.0F));

        PartDefinition Head_r21 = Head2.addOrReplaceChild("Head_r21", CubeListBuilder.create().texOffs(99, 116).addBox(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -3.0F, 0.1309F, 0.5236F, 0.2182F));

        PartDefinition bone6 = Head.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(78, 74).addBox(-2.8F, 0.0F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -8.0F, 0.0F, 0.0F, 0.0F, -0.3054F));

        PartDefinition WingLeftTop = All.addOrReplaceChild("WingLeftTop", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -22.0F, 2.5F, -0.0873F, 0.1309F, 0.0F));

        PartDefinition bone4 = WingLeftTop.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

        PartDefinition WingLeftTop_r1 = bone4.addOrReplaceChild("WingLeftTop_r1", CubeListBuilder.create().texOffs(0, 85).addBox(-35.0F, -20.0F, 0.5F, 34.0F, 25.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.0F, 0.0F, 0.3927F));

        PartDefinition WingLeftBottom = All.addOrReplaceChild("WingLeftBottom", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -22.0F, 2.5F, 0.0873F, 0.2618F, 0.0F));

        PartDefinition bone3 = WingLeftBottom.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

        PartDefinition WingLeftBottom_r1 = bone3.addOrReplaceChild("WingLeftBottom_r1", CubeListBuilder.create().texOffs(0, 51).addBox(-20.0F, -1.0F, 0.5F, 21.0F, 34.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -0.5F, 0.0F, 0.0F, 0.3491F));

        PartDefinition WingRightTop = All.addOrReplaceChild("WingRightTop", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -22.0F, 2.5F, -0.0873F, -0.0873F, 0.0F));

        PartDefinition bone2 = WingRightTop.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition WingRightTop_r1 = bone2.addOrReplaceChild("WingRightTop_r1", CubeListBuilder.create().texOffs(0, 85).mirror().addBox(1.0F, -20.0F, 0.5F, 34.0F, 25.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.0F, 0.0F, -0.3927F));

        PartDefinition WingRightBottom = All.addOrReplaceChild("WingRightBottom", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -22.0F, 2.5F, 0.0873F, -0.2618F, 0.0F));

        PartDefinition bone = WingRightBottom.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition WingRightBottom_r1 = bone.addOrReplaceChild("WingRightBottom_r1", CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-1.0F, -1.0F, 0.5F, 21.0F, 34.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.0F, -0.5F, 0.0F, 0.0F, -0.3491F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float loop = ageInTicks;

        if (WingLeftBottom != null)
            WingLeftBottom.yRot = (float) Math.toRadians(10 + Math.sin(loop * 0.2F) * Math.toDegrees(0.4F));
        if (WingLeftTop != null)
            WingLeftTop.yRot = (float) Math.toRadians(10 + Math.sin(loop * 0.2F) * Math.toDegrees(0.4F) - 10);
        if (WingRightBottom != null)
            WingRightBottom.yRot = (float) Math.toRadians(-10 + Math.sin(loop * 0.2F + Math.PI) * Math.toDegrees(0.4F));
        if (WingRightTop != null)
            WingRightTop.yRot = (float) Math.toRadians(-10 + Math.sin(loop * 0.2F + Math.PI) * Math.toDegrees(0.4F) + 10);

        if (bone5 != null) {
            bone5.xRot = (float) -Math.toRadians(Math.sin(loop * 0.067F) * Math.toDegrees(0.05F));
            bone5.zRot = (float) -Math.toRadians(Math.cos(loop * 0.09F) * Math.toDegrees(0.1F));
        }

        if (bone6 != null) {
            bone6.xRot = (float) Math.toRadians(Math.sin(loop * 0.067F) * Math.toDegrees(0.05F));
            bone6.zRot = (float) Math.toRadians(Math.cos(loop * 0.09F) * Math.toDegrees(0.1F));
        }
        float headYawRad = netHeadYaw * ((float) Math.PI / 180F);
        float headPitchRad = headPitch * ((float) Math.PI / 180F);

        Head.yRot = headYawRad;
        Head.xRot = headPitchRad;


        float armSwing = (float) Math.toRadians(Math.sin(loop * 0.15F) * Math.toDegrees(0.05F));

        ArmRight.xRot = armSwing;
        ArmLeft.xRot = armSwing;

        ArmRight2.xRot = -armSwing * 0.5F;
        ArmLeft2.xRot = -armSwing * 0.5F;

        float elbowZRot = (float) Math.toRadians(-(Math.sin(loop * 0.05F + Math.PI) * Math.toDegrees(0.4F)));

        float baseZRotOffset = (float) Math.toRadians(-50);

        ArmRight.zRot = elbowZRot * 0.4F + baseZRotOffset;
        ArmLeft.zRot = -elbowZRot * 0.4F - baseZRotOffset;

        float hoverScale = 8.0F;
        float hoverSpeed = 2F;
        float baseY = 18.0F;

        float hoverY = (float) Math.sin(loop * 0.05F * hoverSpeed) * (0.5F * hoverScale);

        All.y = baseY + hoverY;


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