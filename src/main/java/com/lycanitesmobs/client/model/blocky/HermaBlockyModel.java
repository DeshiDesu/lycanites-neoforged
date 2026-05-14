package com.lycanitesmobs.client.model.blocky;// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.lycanitesmobs.core.entity.creature.aquatic.EntityHerma;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class HermaBlockyModel<T extends EntityHerma> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "herma"), "main");
    private final ModelPart All;
    private final ModelPart Head;
    private final ModelPart Head2;
    private final ModelPart Head3;
    private final ModelPart ArmRight;
    private final ModelPart ArmRight2;
    private final ModelPart ArmRight3;
    private final ModelPart ArmLeft;
    private final ModelPart ArmLeft2;
    private final ModelPart ArmLeft3;
    private final ModelPart LegRightFront;
    private final ModelPart LegRightFront2;
    private final ModelPart LegRightFront3;
    private final ModelPart LegRightMiddleFront;
    private final ModelPart LegRightMiddleFront2;
    private final ModelPart LegRightMiddleFront3;
    private final ModelPart LegRightMiddleBack;
    private final ModelPart LegRightMiddleBack2;
    private final ModelPart LegRightMiddleBack3;
    private final ModelPart LegRightBack;
    private final ModelPart LegRightBack2;
    private final ModelPart LegRightBack3;
    private final ModelPart LegLeftFront;
    private final ModelPart LegLeftFront2;
    private final ModelPart LegLeftFront3;
    private final ModelPart LegLeftMiddleFront;
    private final ModelPart LegLeftMiddleFront2;
    private final ModelPart LegLeftMiddleFront3;
    private final ModelPart LegLeftMiddleBack;
    private final ModelPart LegLeftMiddleBack2;
    private final ModelPart LegLeftMiddleBack3;
    private final ModelPart LegLeftBack;
    private final ModelPart LegLeftBack2;
    private final ModelPart LegLeftBack3;

    public HermaBlockyModel(ModelPart root) {
        this.All = root.getChild("All");
        this.Head = this.All.getChild("Head");
        this.Head2 = this.Head.getChild("Head2");
        this.Head3 = this.Head2.getChild("Head3");
        this.ArmRight = this.All.getChild("ArmRight");
        this.ArmRight2 = this.ArmRight.getChild("ArmRight2");
        this.ArmRight3 = this.ArmRight2.getChild("ArmRight3");
        this.ArmLeft = this.All.getChild("ArmLeft");
        this.ArmLeft2 = this.ArmLeft.getChild("ArmLeft2");
        this.ArmLeft3 = this.ArmLeft2.getChild("ArmLeft3");
        this.LegRightFront = this.All.getChild("LegRightFront");
        this.LegRightFront2 = this.LegRightFront.getChild("LegRightFront2");
        this.LegRightFront3 = this.LegRightFront2.getChild("LegRightFront3");
        this.LegRightMiddleFront = this.All.getChild("LegRightMiddleFront");
        this.LegRightMiddleFront2 = this.LegRightMiddleFront.getChild("LegRightMiddleFront2");
        this.LegRightMiddleFront3 = this.LegRightMiddleFront2.getChild("LegRightMiddleFront3");
        this.LegRightMiddleBack = this.All.getChild("LegRightMiddleBack");
        this.LegRightMiddleBack2 = this.LegRightMiddleBack.getChild("LegRightMiddleBack2");
        this.LegRightMiddleBack3 = this.LegRightMiddleBack2.getChild("LegRightMiddleBack3");
        this.LegRightBack = this.All.getChild("LegRightBack");
        this.LegRightBack2 = this.LegRightBack.getChild("LegRightBack2");
        this.LegRightBack3 = this.LegRightBack2.getChild("LegRightBack3");
        this.LegLeftFront = this.All.getChild("LegLeftFront");
        this.LegLeftFront2 = this.LegLeftFront.getChild("LegLeftFront2");
        this.LegLeftFront3 = this.LegLeftFront2.getChild("LegLeftFront3");
        this.LegLeftMiddleFront = this.All.getChild("LegLeftMiddleFront");
        this.LegLeftMiddleFront2 = this.LegLeftMiddleFront.getChild("LegLeftMiddleFront2");
        this.LegLeftMiddleFront3 = this.LegLeftMiddleFront2.getChild("LegLeftMiddleFront3");
        this.LegLeftMiddleBack = this.All.getChild("LegLeftMiddleBack");
        this.LegLeftMiddleBack2 = this.LegLeftMiddleBack.getChild("LegLeftMiddleBack2");
        this.LegLeftMiddleBack3 = this.LegLeftMiddleBack2.getChild("LegLeftMiddleBack3");
        this.LegLeftBack = this.All.getChild("LegLeftBack");
        this.LegLeftBack2 = this.LegLeftBack.getChild("LegLeftBack2");
        this.LegLeftBack3 = this.LegLeftBack2.getChild("LegLeftBack3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 15.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition ArmLeft_r1 = All.addOrReplaceChild("ArmLeft_r1", CubeListBuilder.create().texOffs(41, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 18.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, -4.0F, -6.0F, 0.1745F, 0.2618F, 0.0F));

        PartDefinition ArmRight_r1 = All.addOrReplaceChild("ArmRight_r1", CubeListBuilder.create().texOffs(41, 0).addBox(-17.5F, 0.0F, -0.5F, 18.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -4.0F, -6.0F, 0.1745F, -0.2618F, 0.0F));

        PartDefinition Head = All.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -9.0F, -5.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(-4.0F, -9.0F, -5.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.1F))
                .texOffs(1, 15).addBox(-1.5F, -8.0F, -5.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 15).addBox(0.5F, -8.0F, -5.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -1.0F));

        PartDefinition Head_r1 = Head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -8.0F, -5.0F, 0.7418F, 0.0F, 0.1745F));

        PartDefinition Head_r2 = Head.addOrReplaceChild("Head_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -8.0F, -5.0F, 0.7418F, 0.0F, -0.1745F));

        PartDefinition Head_r3 = Head.addOrReplaceChild("Head_r3", CubeListBuilder.create().texOffs(12, 76).mirror().addBox(-8.0F, -2.0F, 0.0F, 2.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(12, 12).mirror().addBox(-8.0F, -2.0F, 0.0F, 2.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 76).mirror().addBox(-6.0F, -2.0F, 0.0F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(0, 12).mirror().addBox(-6.0F, -2.0F, 0.0F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -7.0F, -5.0F, 0.0F, 0.4363F, 0.0F));

        PartDefinition Head_r4 = Head.addOrReplaceChild("Head_r4", CubeListBuilder.create().texOffs(12, 76).addBox(6.0F, -2.0F, 0.0F, 2.0F, 0.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(12, 12).addBox(6.0F, -2.0F, 0.0F, 2.0F, 0.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 76).addBox(0.0F, -2.0F, 0.0F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.1F))
                .texOffs(0, 12).addBox(0.0F, -2.0F, 0.0F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -7.0F, -5.0F, 0.0F, -0.4363F, 0.0F));

        PartDefinition Head_r5 = Head.addOrReplaceChild("Head_r5", CubeListBuilder.create().texOffs(34, 12).addBox(-3.0F, -1.1F, 0.9F, 6.0F, 3.0F, 10.0F, new CubeDeformation(0.009F)), PartPose.offsetAndRotation(0.0F, -5.0F, -4.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition Head2 = Head.addOrReplaceChild("Head2", CubeListBuilder.create().texOffs(25, 50).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(25, 114).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 2.0F, 12.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -9.0F, -5.0F, -0.1623F, 0.0F, 0.0F));

        PartDefinition Head_r6 = Head2.addOrReplaceChild("Head_r6", CubeListBuilder.create().texOffs(53, 50).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 12.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition Head3 = Head2.addOrReplaceChild("Head3", CubeListBuilder.create().texOffs(65, 54).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(65, 118).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -1.0F, 12.0F, -0.2182F, 0.0F, 0.0F));

        PartDefinition Head_r7 = Head3.addOrReplaceChild("Head_r7", CubeListBuilder.create().texOffs(-6, 56).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 5.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition ArmRight = All.addOrReplaceChild("ArmRight", CubeListBuilder.create().texOffs(2, 30).addBox(-1.0F, 0.0F, -6.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(2, 94).addBox(-1.0F, 0.0F, -6.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-4.0F, -3.0F, -5.0F, 0.1745F, 1.0036F, -0.1745F));

        PartDefinition ArmRight2 = ArmRight.addOrReplaceChild("ArmRight2", CubeListBuilder.create().texOffs(19, 32).addBox(-1.5F, -1.5F, -7.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(19, 96).addBox(-1.5F, -1.5F, -7.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 1.0F, -5.0F, -0.1309F, -0.6981F, 0.0F));

        PartDefinition ArmRight3 = ArmRight2.addOrReplaceChild("ArmRight3", CubeListBuilder.create().texOffs(0, 39).addBox(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 103).addBox(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, -6.0F, 0.0F, -0.48F, 0.1309F));

        PartDefinition ArmRight_r2 = ArmRight3.addOrReplaceChild("ArmRight_r2", CubeListBuilder.create().texOffs(15, 45).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -5.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition ArmRight_r3 = ArmRight3.addOrReplaceChild("ArmRight_r3", CubeListBuilder.create().texOffs(32, 33).addBox(-0.5F, -1.0F, -5.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 33).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition ArmLeft = All.addOrReplaceChild("ArmLeft", CubeListBuilder.create().texOffs(2, 30).mirror().addBox(-1.0F, 0.0F, -6.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(2, 94).mirror().addBox(-1.0F, 0.0F, -6.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(4.0F, -3.0F, -5.0F, 0.1745F, -1.0036F, 0.1745F));

        PartDefinition ArmLeft2 = ArmLeft.addOrReplaceChild("ArmLeft2", CubeListBuilder.create().texOffs(19, 32).mirror().addBox(-1.5F, -1.5F, -7.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(19, 96).mirror().addBox(-1.5F, -1.5F, -7.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -5.0F, -0.1309F, 0.6981F, 0.0F));

        PartDefinition ArmLeft3 = ArmLeft2.addOrReplaceChild("ArmLeft3", CubeListBuilder.create().texOffs(0, 39).mirror().addBox(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 103).mirror().addBox(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -6.0F, 0.0F, 0.48F, -0.1309F));

        PartDefinition ArmLeft_r2 = ArmLeft3.addOrReplaceChild("ArmLeft_r2", CubeListBuilder.create().texOffs(15, 45).mirror().addBox(-1.0F, 0.0F, -3.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -5.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition ArmLeft_r3 = ArmLeft3.addOrReplaceChild("ArmLeft_r3", CubeListBuilder.create().texOffs(32, 33).mirror().addBox(-0.5F, -1.0F, -5.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(34, 33).mirror().addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition LegRightFront = All.addOrReplaceChild("LegRightFront", CubeListBuilder.create().texOffs(15, 26).addBox(-5.0F, -1.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 1.0F, -1.5F, 0.4363F, -0.5236F, 0.0175F));

        PartDefinition LegRightFront2 = LegRightFront.addOrReplaceChild("LegRightFront2", CubeListBuilder.create().texOffs(27, 26).addBox(-5.0F, 0.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.829F));

        PartDefinition LegRightFront3 = LegRightFront2.addOrReplaceChild("LegRightFront3", CubeListBuilder.create().texOffs(39, 25).addBox(-6.0F, -0.5F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6981F));

        PartDefinition LegRightMiddleFront = All.addOrReplaceChild("LegRightMiddleFront", CubeListBuilder.create().texOffs(15, 26).addBox(-5.0F, -1.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 1.0F, 0.5F, 0.5672F, -0.2618F, 0.0F));

        PartDefinition LegRightMiddleFront2 = LegRightMiddleFront.addOrReplaceChild("LegRightMiddleFront2", CubeListBuilder.create().texOffs(27, 26).addBox(-5.0F, 0.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.9163F));

        PartDefinition LegRightMiddleFront3 = LegRightMiddleFront2.addOrReplaceChild("LegRightMiddleFront3", CubeListBuilder.create().texOffs(39, 25).addBox(-6.0F, -0.5F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

        PartDefinition LegRightMiddleBack = All.addOrReplaceChild("LegRightMiddleBack", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 1.0F, 2.5F, 0.5236F, 0.1745F, -0.0873F));

        PartDefinition LegRightMiddleBack_r1 = LegRightMiddleBack.addOrReplaceChild("LegRightMiddleBack_r1", CubeListBuilder.create().texOffs(15, 26).addBox(0.0F, 0.0F, -1.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.5F, 0.0F, 0.0F, -0.0873F));

        PartDefinition LegRightMiddleBack2 = LegRightMiddleBack.addOrReplaceChild("LegRightMiddleBack2", CubeListBuilder.create().texOffs(27, 26).addBox(-5.0F, 0.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.6981F));

        PartDefinition LegRightMiddleBack3 = LegRightMiddleBack2.addOrReplaceChild("LegRightMiddleBack3", CubeListBuilder.create().texOffs(39, 25).addBox(-6.0F, -0.5F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition LegRightBack = All.addOrReplaceChild("LegRightBack", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 1.0F, 5.5F, 0.5672F, 0.6109F, -0.1309F));

        PartDefinition LegRightBack_r1 = LegRightBack.addOrReplaceChild("LegRightBack_r1", CubeListBuilder.create().texOffs(32, 28).addBox(0.0F, 0.0F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, 0.5F, 0.0F, 0.0F, -0.0873F));

        PartDefinition LegRightBack2 = LegRightBack.addOrReplaceChild("LegRightBack2", CubeListBuilder.create().texOffs(42, 28).addBox(-4.0F, 0.0F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.6109F));

        PartDefinition LegRightBack3 = LegRightBack2.addOrReplaceChild("LegRightBack3", CubeListBuilder.create().texOffs(52, 27).addBox(-5.0F, -0.5F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

        PartDefinition LegLeftFront = All.addOrReplaceChild("LegLeftFront", CubeListBuilder.create().texOffs(15, 26).mirror().addBox(0.0F, -1.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, 1.0F, -1.5F, 0.4363F, 0.5236F, -0.0175F));

        PartDefinition LegLeftFront2 = LegLeftFront.addOrReplaceChild("LegLeftFront2", CubeListBuilder.create().texOffs(27, 26).mirror().addBox(0.0F, 0.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.829F));

        PartDefinition LegLeftFront3 = LegLeftFront2.addOrReplaceChild("LegLeftFront3", CubeListBuilder.create().texOffs(39, 25).mirror().addBox(0.0F, -0.5F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6981F));

        PartDefinition LegLeftMiddleFront = All.addOrReplaceChild("LegLeftMiddleFront", CubeListBuilder.create().texOffs(15, 26).mirror().addBox(0.0F, -1.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, 1.0F, 0.5F, 0.5672F, 0.2618F, 0.0F));

        PartDefinition LegLeftMiddleFront2 = LegLeftMiddleFront.addOrReplaceChild("LegLeftMiddleFront2", CubeListBuilder.create().texOffs(27, 26).mirror().addBox(0.0F, 0.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.9163F));

        PartDefinition LegLeftMiddleFront3 = LegLeftMiddleFront2.addOrReplaceChild("LegLeftMiddleFront3", CubeListBuilder.create().texOffs(39, 25).mirror().addBox(0.0F, -0.5F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

        PartDefinition LegLeftMiddleBack = All.addOrReplaceChild("LegLeftMiddleBack", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, 1.0F, 2.5F, 0.5236F, -0.1745F, 0.0873F));

        PartDefinition LegLeftMiddleBack_r1 = LegLeftMiddleBack.addOrReplaceChild("LegLeftMiddleBack_r1", CubeListBuilder.create().texOffs(15, 26).mirror().addBox(-5.0F, 0.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, -1.0F, -0.5F, 0.0F, 0.0F, 0.0873F));

        PartDefinition LegLeftMiddleBack2 = LegLeftMiddleBack.addOrReplaceChild("LegLeftMiddleBack2", CubeListBuilder.create().texOffs(27, 26).mirror().addBox(0.0F, 0.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.6981F));

        PartDefinition LegLeftMiddleBack3 = LegLeftMiddleBack2.addOrReplaceChild("LegLeftMiddleBack3", CubeListBuilder.create().texOffs(39, 25).mirror().addBox(0.0F, -0.5F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7418F));

        PartDefinition LegLeftBack = All.addOrReplaceChild("LegLeftBack", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0F, 1.0F, 5.5F, 0.5672F, -0.6109F, 0.1309F));

        PartDefinition LegLeftBack_r1 = LegLeftBack.addOrReplaceChild("LegLeftBack_r1", CubeListBuilder.create().texOffs(32, 28).mirror().addBox(-4.0F, 0.0F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, -1.0F, 0.5F, 0.0F, 0.0F, 0.0873F));

        PartDefinition LegLeftBack2 = LegLeftBack.addOrReplaceChild("LegLeftBack2", CubeListBuilder.create().texOffs(42, 28).mirror().addBox(0.0F, 0.0F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.6109F));

        PartDefinition LegLeftBack3 = LegLeftBack2.addOrReplaceChild("LegLeftBack3", CubeListBuilder.create().texOffs(52, 27).mirror().addBox(0.0F, -0.5F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        return LayerDefinition.create(meshdefinition, 1000, 750);
    }

    @Override
    public void setupAnim(EntityHerma entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        All.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.All;
    }
}
