package com.lycanitesmobs.client.model.blocky;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.creature.elemental.EntityGrue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;


public class GrueBlockyModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(LycanitesMobs.MODID, "grue"), "main");
    private final ModelPart bone7;
    private final ModelPart All;
    private final ModelPart Head;
    private final ModelPart bone2;
    private final ModelPart bone3;
    private final ModelPart bone8;
    private final ModelPart Head2;
    private final ModelPart Head3;
    private final ModelPart Mouth;
    private final ModelPart Body;
    private final ModelPart bone;
    private final ModelPart Body2;
    private final ModelPart Body3;
    private final ModelPart Body4;
    private final ModelPart Body5;
    private final ModelPart bone4;
    private final ModelPart bone5;
    private final ModelPart bone6;
    private final ModelPart bone9;
    private final ModelPart bone10;
    private final ModelPart ArmRight;
    private final ModelPart ArmRight2;
    private final ModelPart FingerRight01;
    private final ModelPart FingerRight012;
    private final ModelPart FingerRight02;
    private final ModelPart FingerRight023;
    private final ModelPart FingerRight03;
    private final ModelPart FingerRight032;
    private final ModelPart FingerRight04;
    private final ModelPart FingerRight042;
    private final ModelPart FingerRight2;
    private final ModelPart FingerRight3;
    private final ModelPart FingerRight4;
    private final ModelPart FingerRight5;
    private final ModelPart FingerRight6;
    private final ModelPart FingerRight8;
    private final ModelPart bone11;
    private final ModelPart ArmLeft;
    private final ModelPart ArmLeft2;
    private final ModelPart FingerLeft01;
    private final ModelPart FingerLeft3;
    private final ModelPart FingerLeft02;
    private final ModelPart FingerLeft4;
    private final ModelPart FingerLeft03;
    private final ModelPart FingerRight7;
    private final ModelPart FingerLeft04;
    private final ModelPart FingerLeft2;
    private final ModelPart FingerLeft5;
    private final ModelPart FingerLeft10;
    private final ModelPart FingerLeft11;
    private final ModelPart FingerLeft6;
    private final ModelPart FingerLeft9;
    private final ModelPart FingerLeft7;
    private final ModelPart FingerLeft8;

    public GrueBlockyModel(ModelPart root) {
        this.bone7 = root.getChild("bone7");
        this.All = root.getChild("All");
        this.Head = this.All.getChild("Head");
        this.bone2 = this.Head.getChild("bone2");
        this.bone3 = this.bone2.getChild("bone3");
        this.bone8 = this.bone2.getChild("bone8");
        this.Head2 = this.bone2.getChild("Head2");
        this.Head3 = this.Head.getChild("Head3");
        this.Mouth = this.Head.getChild("Mouth");
        this.Body = this.All.getChild("Body");
        this.bone = this.Body.getChild("bone");
        this.Body2 = this.Body.getChild("Body2");
        this.Body3 = this.Body2.getChild("Body3");
        this.Body4 = this.Body3.getChild("Body4");
        this.Body5 = this.Body4.getChild("Body5");
        this.bone4 = this.Body.getChild("bone4");
        this.bone5 = this.bone4.getChild("bone5");
        this.bone6 = this.bone5.getChild("bone6");
        this.bone9 = this.bone5.getChild("bone9");
        this.bone10 = this.All.getChild("bone10");
        this.ArmRight = this.bone10.getChild("ArmRight");
        this.ArmRight2 = this.ArmRight.getChild("ArmRight2");
        this.FingerRight01 = this.bone10.getChild("FingerRight01");
        this.FingerRight012 = this.FingerRight01.getChild("FingerRight012");
        this.FingerRight02 = this.bone10.getChild("FingerRight02");
        this.FingerRight023 = this.FingerRight02.getChild("FingerRight023");
        this.FingerRight03 = this.bone10.getChild("FingerRight03");
        this.FingerRight032 = this.FingerRight03.getChild("FingerRight032");
        this.FingerRight04 = this.bone10.getChild("FingerRight04");
        this.FingerRight042 = this.FingerRight04.getChild("FingerRight042");
        this.FingerRight2 = this.bone10.getChild("FingerRight2");
        this.FingerRight3 = this.FingerRight2.getChild("FingerRight3");
        this.FingerRight4 = this.bone10.getChild("FingerRight4");
        this.FingerRight5 = this.FingerRight4.getChild("FingerRight5");
        this.FingerRight6 = this.bone10.getChild("FingerRight6");
        this.FingerRight8 = this.FingerRight6.getChild("FingerRight8");
        this.bone11 = this.All.getChild("bone11");
        this.ArmLeft = this.bone11.getChild("ArmLeft");
        this.ArmLeft2 = this.ArmLeft.getChild("ArmLeft2");
        this.FingerLeft01 = this.bone11.getChild("FingerLeft01");
        this.FingerLeft3 = this.FingerLeft01.getChild("FingerLeft3");
        this.FingerLeft02 = this.bone11.getChild("FingerLeft02");
        this.FingerLeft4 = this.FingerLeft02.getChild("FingerLeft4");
        this.FingerLeft03 = this.bone11.getChild("FingerLeft03");
        this.FingerRight7 = this.FingerLeft03.getChild("FingerRight7");
        this.FingerLeft04 = this.bone11.getChild("FingerLeft04");
        this.FingerLeft2 = this.FingerLeft04.getChild("FingerLeft2");
        this.FingerLeft5 = this.bone11.getChild("FingerLeft5");
        this.FingerLeft10 = this.FingerLeft5.getChild("FingerLeft10");
        this.FingerLeft11 = this.FingerLeft5.getChild("FingerLeft11");
        this.FingerLeft6 = this.bone11.getChild("FingerLeft6");
        this.FingerLeft9 = this.FingerLeft6.getChild("FingerLeft9");
        this.FingerLeft7 = this.bone11.getChild("FingerLeft7");
        this.FingerLeft8 = this.FingerLeft7.getChild("FingerLeft8");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone7 = partdefinition.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -2.0F, -11.0F, 0.5236F, 0.48F, 0.0F));

        PartDefinition Head_r1 = bone7.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(32, 13).addBox(0.0F, -5.0F, 0.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -1.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Head = All.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, -20.0F, -9.1F));

        PartDefinition Head_r2 = Head.addOrReplaceChild("Head_r2", CubeListBuilder.create().texOffs(56, 108).addBox(-10.0F, -1.0F, -10.0F, 20.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.0F, 4.1F, -0.3927F, 0.0F, 0.0F));

        PartDefinition bone2 = Head.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -7.0F, 0.0F, 16.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -3.9F, 0.2793F, 0.0F, 0.0F));

        PartDefinition Head_r3 = bone2.addOrReplaceChild("Head_r3", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 2.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition Head_r4 = bone2.addOrReplaceChild("Head_r4", CubeListBuilder.create().texOffs(44, 48).addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -0.5F, 6.0F, 0.2618F, -0.3491F, 0.0F));

        PartDefinition Head_r5 = bone2.addOrReplaceChild("Head_r5", CubeListBuilder.create().texOffs(44, 42).addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -0.5F, 6.0F, 0.2618F, 0.3491F, 0.0F));

        PartDefinition bone3 = bone2.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -7.0F, 2.0F, 0.5236F, 0.48F, 0.0F));

        PartDefinition Head_r6 = bone3.addOrReplaceChild("Head_r6", CubeListBuilder.create().texOffs(32, 13).addBox(0.0F, -5.0F, 0.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -1.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bone8 = bone2.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -7.0F, 2.0F, 0.5236F, -0.48F, 0.0F));

        PartDefinition Head_r7 = bone8.addOrReplaceChild("Head_r7", CubeListBuilder.create().texOffs(32, 13).addBox(0.0F, -5.0F, 0.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -1.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition Head2 = bone2.addOrReplaceChild("Head2", CubeListBuilder.create().texOffs(0, 48).addBox(-7.5F, 0.0F, 0.0F, 15.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 6.0F, -0.48F, 0.0F, 0.0F));

        PartDefinition Head_r8 = Head2.addOrReplaceChild("Head_r8", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -0.7418F, 0.0F, 0.0F));

        PartDefinition Head_r9 = Head2.addOrReplaceChild("Head_r9", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition Head_r10 = Head2.addOrReplaceChild("Head_r10", CubeListBuilder.create().texOffs(44, 42).addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, 6.0F, 7.0F, 0.2182F, 0.2618F, 0.0F));

        PartDefinition Head_r11 = Head2.addOrReplaceChild("Head_r11", CubeListBuilder.create().texOffs(44, 48).addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, 6.0F, 7.0F, 0.1745F, -0.1745F, 0.0F));

        PartDefinition Head_r12 = Head2.addOrReplaceChild("Head_r12", CubeListBuilder.create().texOffs(30, 35).addBox(-7.5F, 0.0F, 0.0F, 15.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 7.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition Head_r13 = Head2.addOrReplaceChild("Head_r13", CubeListBuilder.create().texOffs(30, 42).addBox(-7.5F, 0.0F, 0.1F, 15.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 6.9F, 0.48F, 0.0F, 0.0F));

        PartDefinition Head3 = Head.addOrReplaceChild("Head3", CubeListBuilder.create().texOffs(0, 24).addBox(-4.5F, -7.0F, 0.0F, 7.0F, 7.0F, 6.0F, new CubeDeformation(0.002F)), PartPose.offset(1.0F, 2.0F, -9.9F));

        PartDefinition Head_r14 = Head3.addOrReplaceChild("Head_r14", CubeListBuilder.create().texOffs(0, 63).addBox(-7.0F, 0.0F, 0.0F, 7.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 0.0F, 0.0F, 0.1745F, 0.8727F, 0.0F));

        PartDefinition Head_r15 = Head3.addOrReplaceChild("Head_r15", CubeListBuilder.create().texOffs(0, 13).addBox(-7.0F, -4.0F, 0.0F, 7.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -3.0F, 0.0F, 0.0F, 0.8727F, 0.0F));

        PartDefinition Head_r16 = Head3.addOrReplaceChild("Head_r16", CubeListBuilder.create().texOffs(0, 65).addBox(0.0F, 0.0F, 0.0F, 7.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 0.0F, 0.0F, 0.1745F, -0.8727F, 0.0F));

        PartDefinition Head_r17 = Head3.addOrReplaceChild("Head_r17", CubeListBuilder.create().texOffs(0, 37).addBox(0.0F, -4.0F, 0.0F, 7.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -3.0F, 0.0F, 0.0F, -0.8727F, 0.0F));

        PartDefinition Head_r18 = Head3.addOrReplaceChild("Head_r18", CubeListBuilder.create().texOffs(0, 61).addBox(-4.5F, 0.0F, 0.0F, 7.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition Head_r19 = Head3.addOrReplaceChild("Head_r19", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -7.0F, 2.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition Head_r20 = Head3.addOrReplaceChild("Head_r20", CubeListBuilder.create().texOffs(18, 37).addBox(-2.0F, 0.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.48F, 0.0F, 0.0F));

        PartDefinition Mouth = Head.addOrReplaceChild("Mouth", CubeListBuilder.create().texOffs(0, 111).addBox(-7.5F, 1.0F, -6.0F, 13.0F, 2.0F, 4.0F, new CubeDeformation(0.001F))
                .texOffs(0, 102).addBox(-7.0F, -1.0F, -2.5F, 12.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(17, 121).addBox(-4.0F, 1.0F, -10.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.002F))
                .texOffs(0, 124).addBox(-4.0F, -1.0F, -10.0F, 6.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 1.0F, 0.1F, 0.5236F, 0.0F, 0.0F));

        PartDefinition Mouth_r1 = Mouth.addOrReplaceChild("Mouth_r1", CubeListBuilder.create().texOffs(0, 122).addBox(-6.0F, -2.0F, 0.0F, 6.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 117).addBox(-6.0F, 0.0F, 0.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 1.0F, -10.0F, 0.0F, 0.829F, 0.0F));

        PartDefinition Mouth_r2 = Mouth.addOrReplaceChild("Mouth_r2", CubeListBuilder.create().texOffs(0, 126).addBox(0.0F, -2.0F, 0.0F, 6.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(36, 117).addBox(0.0F, 0.0F, 0.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 1.0F, -10.0F, 0.0F, -0.829F, 0.0F));

        PartDefinition Body = All.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(65, 11).addBox(-5.0F, 0.0F, -4.0F, 10.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(91, 23).addBox(-1.0F, 2.0F, -4.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.0F, -2.0F, 0.6109F, 0.0F, 0.0F));

        PartDefinition Body_r1 = Body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(97, 4).addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 6.0F, 2.0F, 0.2182F, 0.4363F, 0.0F));

        PartDefinition Body_r2 = Body.addOrReplaceChild("Body_r2", CubeListBuilder.create().texOffs(97, 10).addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 6.0F, 2.0F, 0.2182F, -0.4363F, 0.0F));

        PartDefinition bone = Body.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(67, 0).addBox(-4.0F, -5.0F, -2.0F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, 0.3054F, 0.0F, 0.0F));

        PartDefinition Body2 = Body.addOrReplaceChild("Body2", CubeListBuilder.create().texOffs(70, 23).addBox(-4.0F, -1.0F, -2.5F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -1.0F, -0.3491F, 0.0F, 0.0F));

        PartDefinition Body_r3 = Body2.addOrReplaceChild("Body_r3", CubeListBuilder.create().texOffs(114, 23).addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 4.0F, 2.5F, 0.2182F, 0.2618F, 0.0F));

        PartDefinition Body_r4 = Body2.addOrReplaceChild("Body_r4", CubeListBuilder.create().texOffs(114, 17).addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 4.0F, 2.5F, 0.2182F, -0.2618F, 0.0F));

        PartDefinition Body3 = Body2.addOrReplaceChild("Body3", CubeListBuilder.create().texOffs(73, 34).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 1.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition Body_r5 = Body3.addOrReplaceChild("Body_r5", CubeListBuilder.create().texOffs(93, 37).addBox(0.0F, 0.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -1.0F, 1.0F, -0.2182F, 0.1309F, 0.0F));

        PartDefinition Body_r6 = Body3.addOrReplaceChild("Body_r6", CubeListBuilder.create().texOffs(93, 31).addBox(0.0F, 0.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, 1.0F, -0.2182F, -0.1309F, 0.0F));

        PartDefinition Body4 = Body3.addOrReplaceChild("Body4", CubeListBuilder.create().texOffs(76, 44).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, -0.5F, 0.4363F, 0.0F, 0.0F));

        PartDefinition Body5 = Body4.addOrReplaceChild("Body5", CubeListBuilder.create().texOffs(79, 52).addBox(0.0F, -1.0F, -2.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 4.0F, 0.5F, 0.4363F, 0.0F, 0.0F));

        PartDefinition Body_r7 = Body5.addOrReplaceChild("Body_r7", CubeListBuilder.create().texOffs(87, 54).mirror().addBox(-3.0F, 0.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.2618F, -0.5236F));

        PartDefinition Body_r8 = Body5.addOrReplaceChild("Body_r8", CubeListBuilder.create().texOffs(87, 52).mirror().addBox(-1.0F, 0.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, 2.0F, -1.0F, 0.0F, 0.2618F, 0.5236F));

        PartDefinition Body_r9 = Body5.addOrReplaceChild("Body_r9", CubeListBuilder.create().texOffs(87, 52).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -1.0F, -1.0F, 0.0F, -0.2618F, 0.48F));

        PartDefinition Body_r10 = Body5.addOrReplaceChild("Body_r10", CubeListBuilder.create().texOffs(87, 54).mirror().addBox(-4.0F, 0.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.0F, -1.0F, 0.0F, 0.2618F, -0.48F));

        PartDefinition bone4 = Body.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(100, 52).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone5 = bone4.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(100, 59).addBox(-7.0F, 0.0F, 0.0F, 14.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition bone6 = bone5.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(116, 66).addBox(0.0F, -1.0F, -6.0F, 0.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 4.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition bone9 = bone5.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(116, 76).addBox(0.0F, 0.0F, -6.0F, 0.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 3.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

        PartDefinition bone10 = All.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offset(-5.0F, -16.0F, -1.0F));

        PartDefinition ArmRight = bone10.addOrReplaceChild("ArmRight", CubeListBuilder.create().texOffs(6, 68).addBox(-7.0F, -2.5F, -4.5F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(6, 68).addBox(-3.0F, -2.5F, -4.5F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, -0.5236F, -0.2182F));

        PartDefinition ArmRight_r1 = ArmRight.addOrReplaceChild("ArmRight_r1", CubeListBuilder.create().texOffs(2, 80).addBox(-10.0F, -1.5F, -2.0F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -0.0873F));

        PartDefinition ArmRight2 = ArmRight.addOrReplaceChild("ArmRight2", CubeListBuilder.create().texOffs(0, 87).addBox(-11.0F, -2.0F, -3.0F, 11.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(6, 68).addBox(-8.0F, -3.5F, -4.5F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(6, 68).addBox(-3.0F, -3.5F, -4.5F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, 0.5F, 0.0F, 0.0436F, -0.7854F, 0.3491F));

        PartDefinition ArmRight_r2 = ArmRight2.addOrReplaceChild("ArmRight_r2", CubeListBuilder.create().texOffs(30, 79).addBox(-3.0F, 0.0F, -3.5F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -2.0F, 0.0F, 0.0F, -0.3491F, -0.3927F));

        PartDefinition FingerRight01 = bone10.addOrReplaceChild("FingerRight01", CubeListBuilder.create().texOffs(29, 95).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -1.5F, -14.0F, -0.2618F, -0.3054F, -0.1745F));

        PartDefinition FingerRight012 = FingerRight01.addOrReplaceChild("FingerRight012", CubeListBuilder.create().texOffs(39, 89).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerRight01_r1 = FingerRight012.addOrReplaceChild("FingerRight01_r1", CubeListBuilder.create().texOffs(37, 94).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerRight02 = bone10.addOrReplaceChild("FingerRight02", CubeListBuilder.create().texOffs(28, 90).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, -1.5F, -14.0F, -0.3491F, -0.1309F, -0.1745F));

        PartDefinition FingerRight023 = FingerRight02.addOrReplaceChild("FingerRight023", CubeListBuilder.create().texOffs(38, 85).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerRight02_r1 = FingerRight023.addOrReplaceChild("FingerRight02_r1", CubeListBuilder.create().texOffs(37, 94).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerRight03 = bone10.addOrReplaceChild("FingerRight03", CubeListBuilder.create().texOffs(28, 85).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -1.5F, -14.0F, -0.2182F, -0.0436F, -0.1745F));

        PartDefinition FingerRight032 = FingerRight03.addOrReplaceChild("FingerRight032", CubeListBuilder.create().texOffs(49, 89).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerRight03_r1 = FingerRight032.addOrReplaceChild("FingerRight03_r1", CubeListBuilder.create().texOffs(37, 94).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerRight04 = bone10.addOrReplaceChild("FingerRight04", CubeListBuilder.create().texOffs(48, 85).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -1.5F, -14.0F, -0.1309F, 0.0873F, -0.1745F));

        PartDefinition FingerRight042 = FingerRight04.addOrReplaceChild("FingerRight042", CubeListBuilder.create().texOffs(49, 89).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerRight04_r1 = FingerRight042.addOrReplaceChild("FingerRight04_r1", CubeListBuilder.create().texOffs(37, 94).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerRight2 = bone10.addOrReplaceChild("FingerRight2", CubeListBuilder.create().texOffs(58, 85).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -1.5F, -14.0F, -0.5672F, 0.0F, 0.0F));

        PartDefinition FingerRight3 = FingerRight2.addOrReplaceChild("FingerRight3", CubeListBuilder.create().texOffs(59, 90).addBox(0.0F, 0.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerRight03_r2 = FingerRight3.addOrReplaceChild("FingerRight03_r2", CubeListBuilder.create().texOffs(68, 83).addBox(0.5F, -0.5F, -5.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerRight4 = bone10.addOrReplaceChild("FingerRight4", CubeListBuilder.create().texOffs(58, 85).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -1.5F, -14.0F, -0.2182F, 0.1745F, -0.1745F));

        PartDefinition FingerRight5 = FingerRight4.addOrReplaceChild("FingerRight5", CubeListBuilder.create().texOffs(59, 90).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerRight04_r2 = FingerRight5.addOrReplaceChild("FingerRight04_r2", CubeListBuilder.create().texOffs(68, 83).addBox(0.0F, -0.5F, -4.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerRight6 = bone10.addOrReplaceChild("FingerRight6", CubeListBuilder.create().texOffs(58, 85).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -1.5F, -14.0F, -0.2182F, -0.1745F, 0.1745F));

        PartDefinition FingerRight8 = FingerRight6.addOrReplaceChild("FingerRight8", CubeListBuilder.create().texOffs(59, 90).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerRight01_r2 = FingerRight8.addOrReplaceChild("FingerRight01_r2", CubeListBuilder.create().texOffs(68, 83).addBox(0.0F, -0.5F, -4.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone11 = All.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offset(5.0F, -16.0F, -1.0F));

        PartDefinition ArmLeft = bone11.addOrReplaceChild("ArmLeft", CubeListBuilder.create().texOffs(0, 62).mirror().addBox(7.0F, -2.5F, -4.5F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 62).mirror().addBox(3.0F, -2.5F, -4.5F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, 0.5236F, 0.2182F));

        PartDefinition ArmLeft_r1 = ArmLeft.addOrReplaceChild("ArmLeft_r1", CubeListBuilder.create().texOffs(2, 80).mirror().addBox(0.0F, -1.5F, -2.0F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition ArmLeft2 = ArmLeft.addOrReplaceChild("ArmLeft2", CubeListBuilder.create().texOffs(0, 87).mirror().addBox(0.0F, -2.0F, -3.0F, 11.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 62).mirror().addBox(8.0F, -3.5F, -4.5F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 62).mirror().addBox(3.0F, -3.5F, -4.5F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(10.0F, 0.5F, 0.0F, 0.0436F, 0.7854F, -0.3491F));

        PartDefinition ArmLeft_r2 = ArmLeft2.addOrReplaceChild("ArmLeft_r2", CubeListBuilder.create().texOffs(30, 79).mirror().addBox(0.0F, 0.0F, -3.5F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(11.0F, -2.0F, 0.0F, 0.0F, 0.3491F, 0.3927F));

        PartDefinition FingerLeft01 = bone11.addOrReplaceChild("FingerLeft01", CubeListBuilder.create().texOffs(29, 95).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(8.0F, -1.5F, -15.0F, -0.2618F, 0.3054F, 0.1745F));

        PartDefinition FingerLeft3 = FingerLeft01.addOrReplaceChild("FingerLeft3", CubeListBuilder.create().texOffs(39, 89).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerLeft01_r1 = FingerLeft3.addOrReplaceChild("FingerLeft01_r1", CubeListBuilder.create().texOffs(37, 94).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerLeft02 = bone11.addOrReplaceChild("FingerLeft02", CubeListBuilder.create().texOffs(28, 90).mirror().addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(9.0F, -1.5F, -15.0F, -0.3491F, 0.1309F, 0.1745F));

        PartDefinition FingerLeft4 = FingerLeft02.addOrReplaceChild("FingerLeft4", CubeListBuilder.create().texOffs(38, 85).mirror().addBox(-1.5F, 0.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 0.0F, -3.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerLeft02_r1 = FingerLeft4.addOrReplaceChild("FingerLeft02_r1", CubeListBuilder.create().texOffs(37, 94).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 0.0F, -3.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerLeft03 = bone11.addOrReplaceChild("FingerLeft03", CubeListBuilder.create().texOffs(28, 85).mirror().addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(10.0F, -1.5F, -15.0F, -0.2182F, 0.0436F, 0.1745F));

        PartDefinition FingerRight7 = FingerLeft03.addOrReplaceChild("FingerRight7", CubeListBuilder.create().texOffs(49, 89).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerLeft03_r1 = FingerRight7.addOrReplaceChild("FingerLeft03_r1", CubeListBuilder.create().texOffs(37, 94).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerLeft04 = bone11.addOrReplaceChild("FingerLeft04", CubeListBuilder.create().texOffs(48, 85).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(11.0F, -1.5F, -15.0F, -0.1309F, -0.0873F, 0.1745F));

        PartDefinition FingerLeft2 = FingerLeft04.addOrReplaceChild("FingerLeft2", CubeListBuilder.create().texOffs(49, 89).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerLeft04_r1 = FingerLeft2.addOrReplaceChild("FingerLeft04_r1", CubeListBuilder.create().texOffs(37, 94).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerLeft5 = bone11.addOrReplaceChild("FingerLeft5", CubeListBuilder.create().texOffs(58, 85).mirror().addBox(-1.0F, 0.0F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(10.0F, -1.5F, -14.0F, -0.5672F, 0.0F, 0.0F));

        PartDefinition FingerLeft10 = FingerLeft5.addOrReplaceChild("FingerLeft10", CubeListBuilder.create().texOffs(59, 90).mirror().addBox(-1.0F, 0.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerLeft03_r2 = FingerLeft10.addOrReplaceChild("FingerLeft03_r2", CubeListBuilder.create().texOffs(68, 83).mirror().addBox(-0.5F, -0.5F, -4.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerLeft11 = FingerLeft5.addOrReplaceChild("FingerLeft11", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerLeft6 = bone11.addOrReplaceChild("FingerLeft6", CubeListBuilder.create().texOffs(58, 85).mirror().addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(8.0F, -1.5F, -14.0F, -0.2182F, 0.1745F, -0.1745F));

        PartDefinition FingerLeft9 = FingerLeft6.addOrReplaceChild("FingerLeft9", CubeListBuilder.create().texOffs(59, 90).mirror().addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerLeft01_r2 = FingerLeft9.addOrReplaceChild("FingerLeft01_r2", CubeListBuilder.create().texOffs(68, 83).mirror().addBox(0.0F, -0.5F, -3.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition FingerLeft7 = bone11.addOrReplaceChild("FingerLeft7", CubeListBuilder.create().texOffs(58, 85).mirror().addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(11.0F, -1.5F, -14.0F, -0.2182F, -0.1745F, 0.1745F));

        PartDefinition FingerLeft8 = FingerLeft7.addOrReplaceChild("FingerLeft8", CubeListBuilder.create().texOffs(59, 90).mirror().addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition FingerLeft04_r2 = FingerLeft8.addOrReplaceChild("FingerLeft04_r2", CubeListBuilder.create().texOffs(68, 83).mirror().addBox(0.0F, -0.5F, -3.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 0.3491F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.All.getAllParts().forEach(ModelPart::resetPose);

        float jawBase = 10F * Mth.DEG_TO_RAD;
        float jawAmp = 7F * Mth.DEG_TO_RAD;


        float a1 = Mth.cos((ageInTicks + 0F) * 0.2F) * 0.2F - 0.2F;
        float a2 = Mth.cos((ageInTicks + 20F) * 0.2F) * 0.2F - 0.2F;
        float a3 = Mth.cos((ageInTicks + 40F) * 0.2F) * 0.2F - 0.2F;
        float a4 = Mth.cos((ageInTicks + 60F) * 0.2F) * 0.2F - 0.2F;

        this.FingerLeft01.xRot += a1;
        this.FingerRight01.xRot += a1;
        this.FingerLeft02.xRot += a2;
        this.FingerRight02.xRot += a2;
        this.FingerLeft03.xRot += a3;
        this.FingerRight03.xRot += a3;
        this.FingerLeft04.xRot += a4;
        this.FingerRight04.xRot += a4;

        float tailAmp = 8F * Mth.DEG_TO_RAD;
        float tailFreq = 0.025F;
        this.Body3.xRot += Mth.sin(ageInTicks * tailFreq + 0.0F) * tailAmp;
        this.Body4.xRot += Mth.sin(ageInTicks * tailFreq + 0.6F) * tailAmp;
        this.Body5.xRot += Mth.sin(ageInTicks * tailFreq + 1.2F) * tailAmp;

        int LEFT_OFFSET_TICKS = 2;
        float pt = Minecraft.getInstance().getFrameTime();
        float atk = 0F;
        byte atkType = 0;

        if (entity instanceof EntityGrue g) {
            atk = g.getMeleeAttackAnim(pt);
            atkType = g.getAttackAnimType();
        }

        var DEBUG_FORCE_BITE = false;
        if (DEBUG_FORCE_BITE) {
            atkType = EntityGrue.ATTACK_BITE; atk = (entity.tickCount % 8) / 8f;
        }

        if (!(atkType == EntityGrue.ATTACK_BITE && atk > 0F)) {
            this.Mouth.xRot += jawBase + Mth.sin(ageInTicks * 0.1F) * jawAmp;
        }
        if (atkType == EntityGrue.ATTACK_BITE && atk > 0F) {
            float lengthFactor = 2F;
            float u = Mth.clamp(atk * (1F / lengthFactor), 0F, 1F);
            if (u >= 0.9999F) u = 1F;

            float baseLen = 0.416F;
            float t = u * (baseLen * lengthFactor);


            float[] tHeadR = {
                    0.0F,
                    0.0417F * lengthFactor,
                    0.0833F * lengthFactor,
                    0.1667F * lengthFactor,
                    0.25F * lengthFactor,
                    baseLen
            };
            float[] hx = {0, 0, 0, 0, 0, 0};
            float[] hy = {0, 0, 0, 0, 0, 0};
            float[] hz = {0, 7.5F, 30F, 15F, 0, 0};

            float[] tHeadP = {
                    0.0F,
                    0.125F * lengthFactor,
                    0.1667F * lengthFactor,
                    0.2083F * lengthFactor,
                    0.2917F * lengthFactor,
                    baseLen
            };
            float[] px = {0, 0, 0, 0, 0, 0};
            float[] py = {0, 0, 0, 0, 0, 0};
            float[] pz = {0, 0, -4F, -2F, 0, 0};

            float[] tMouthR = {
                    0.0F,
                    0.0417F * lengthFactor,
                    0.0833F * lengthFactor,
                    0.1667F * lengthFactor,
                    0.2083F * lengthFactor,
                    0.25F * lengthFactor,
                    0.2917F * lengthFactor,
                    baseLen
            };
            float[] mx = {0F, 27.5F, 65F, 30.5897F, -4.7052F, -20F, -10F, 0F};
            float[] my = {0F, 0F, 0F, 2.4828F, 1.2414F, 0F, 0F, 0F};
            float[] mz = {0F, 0F, 0F, -4.3255F, -2.1627F, 0F, 0F, 0F};

            this.Head.xRot += klerp(t, tHeadR, hx) * Mth.DEG_TO_RAD;
            this.Head.yRot += klerp(t, tHeadR, hy) * Mth.DEG_TO_RAD;
            this.Head.zRot += klerp(t, tHeadR, hz) * Mth.DEG_TO_RAD;

            this.Head.x += klerp(t, tHeadP, px);
            this.Head.y += klerp(t, tHeadP, py);
            this.Head.z += klerp(t, tHeadP, pz);

            this.Mouth.xRot += klerp(t, tMouthR, mx) * Mth.DEG_TO_RAD;
            this.Mouth.yRot += klerp(t, tMouthR, my) * Mth.DEG_TO_RAD;
            this.Mouth.zRot += klerp(t, tMouthR, mz) * Mth.DEG_TO_RAD;
        } else if (atkType == EntityGrue.ATTACK_SWIPE) {
            float baseLen = 0.5F;

            float tR = Mth.clamp(atk * baseLen, 0F, baseLen);
            float tL = Mth.clamp(tR - (LEFT_OFFSET_TICKS / 20F), 0F, baseLen);

            float[] tRight = new float[]{0.0F, 0F, 0F, 0.5F};
            float[] xRight = new float[]{0.0F, 0.0F, 43.2578F, 0.0F};
            float[] yRight = new float[]{0.0F, -22.5F, -23.7844F, 0.0F};
            float[] zRight = new float[]{0.0F, -37.5F, -31.8847F, 0.0F};

            float[] tLeft = new float[]{0.0F, 0F, 0F, 0.5F};
            float[] xLeft = new float[]{0.0F, 0.0F, 43.2578F, 0.0F};
            float[] yLeft = new float[]{0.0F, 22.5F, 23.7844F, 0.0F};
            float[] zLeft = new float[]{0.0F, 37.5F, 31.8847F, 0.0F};

            this.bone10.xRot += klerp(tR, tRight, xRight) * Mth.DEG_TO_RAD;
            this.bone10.yRot += klerp(tR, tRight, yRight) * Mth.DEG_TO_RAD;
            this.bone10.zRot += klerp(tR, tRight, zRight) * Mth.DEG_TO_RAD;

            this.bone11.xRot += klerp(tL, tLeft, xLeft) * Mth.DEG_TO_RAD;
            this.bone11.yRot += klerp(tL, tLeft, yLeft) * Mth.DEG_TO_RAD;
            this.bone11.zRot += klerp(tL, tLeft, zLeft) * Mth.DEG_TO_RAD;
        }


    }

    private static float klerp(float t, float[] ts, float[] vs) {
        if (t <= ts[0]) return vs[0];
        for (int i = 1; i < ts.length; i++) {
            if (t <= ts[i]) {
                float a = (t - ts[i - 1]) / (ts[i] - ts[i - 1]);
                return vs[i - 1] + a * (vs[i] - vs[i - 1]);
            }
        }
        return vs[ts.length - 1];
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bone7.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        All.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return All;
    }
}