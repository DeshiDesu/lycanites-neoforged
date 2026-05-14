package com.lycanitesmobs.client.model.blocky;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BalayangBlockyModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("lycanites", "balayang_layer"), "main");
    private final ModelPart All;
    private final ModelPart Mouth;
    private final ModelPart Eye;
    private final ModelPart Body;
    private final ModelPart Body4;
    private final ModelPart Body5;
    private final ModelPart Body6;
    private final ModelPart Body7;
    private final ModelPart Body8;
    private final ModelPart Body9;
    private final ModelPart Body3;
    private final ModelPart Body2;
    private final ModelPart Tail01;
    private final ModelPart LegRight;
    private final ModelPart LegRight2;
    private final ModelPart LegLeft;
    private final ModelPart LegLeft2;
    private final ModelPart WingRight01;
    private final ModelPart WingRight02;
    private final ModelPart WingLeft01;
    private final ModelPart WingLeft02;

    public BalayangBlockyModel(ModelPart root) {
        this.All = root.getChild("All");
        this.Mouth = this.All.getChild("Mouth");
        this.Eye = this.All.getChild("Eye");
        this.Body = this.All.getChild("Body");
        this.Body4 = this.Body.getChild("Body4");
        this.Body5 = this.Body4.getChild("Body5");
        this.Body6 = this.Body5.getChild("Body6");
        this.Body7 = this.Body.getChild("Body7");
        this.Body8 = this.Body7.getChild("Body8");
        this.Body9 = this.Body8.getChild("Body9");
        this.Body3 = this.Body.getChild("Body3");
        this.Body2 = this.Body.getChild("Body2");
        this.Tail01 = this.Body2.getChild("Tail01");
        this.LegRight = this.All.getChild("LegRight");
        this.LegRight2 = this.LegRight.getChild("LegRight2");
        this.LegLeft = this.All.getChild("LegLeft");
        this.LegLeft2 = this.LegLeft.getChild("LegLeft2");
        this.WingRight01 = this.All.getChild("WingRight01");
        this.WingRight02 = this.WingRight01.getChild("WingRight02");
        this.WingLeft01 = this.All.getChild("WingLeft01");
        this.WingLeft02 = this.WingLeft01.getChild("WingLeft02");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Mouth = All.addOrReplaceChild("Mouth", CubeListBuilder.create().texOffs(47, 84).addBox(-5.0F, 0.0F, -8.5F, 10.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(47, 109).addBox(-5.0F, -1.0F, -8.5F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 1.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition Eye = All.addOrReplaceChild("Eye", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -2.5F, -1.0F, 7.0F, 5.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, -3.0F, -8.0F));

        PartDefinition Body = All.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(46, 96).addBox(-6.5F, -1.0F, -7.0F, 13.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(46, 67).addBox(-6.5F, -9.0F, -7.0F, 13.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(90, 81).addBox(-6.5F, -1.0F, 2.0F, 13.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -1.0F));

        PartDefinition Body_r1 = Body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(51, 34).mirror().addBox(-2.0F, -2.5F, 0.0F, 5.0F, 3.0F, 3.0F, new CubeDeformation(-0.1F)).mirror(false), PartPose.offsetAndRotation(-7.0F, -3.0F, -4.5F, 0.0F, -0.1309F, 0.5672F));

        PartDefinition Body_r2 = Body.addOrReplaceChild("Body_r2", CubeListBuilder.create().texOffs(51, 34).addBox(-3.0F, -2.5F, 0.0F, 5.0F, 3.0F, 3.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(7.0F, -3.0F, -4.5F, 0.0F, 0.1309F, -0.5672F));

        PartDefinition Body_r3 = Body.addOrReplaceChild("Body_r3", CubeListBuilder.create().texOffs(32, 110).mirror().addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.5F, -3.0F, -6.0F, 0.1745F, -0.5236F, 0.0F));

        PartDefinition Body_r4 = Body.addOrReplaceChild("Body_r4", CubeListBuilder.create().texOffs(24, 110).mirror().addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(6.5F, -3.0F, -6.0F, 0.1745F, 0.5236F, 0.0F));

        PartDefinition Body_r5 = Body.addOrReplaceChild("Body_r5", CubeListBuilder.create().texOffs(-4, 116).mirror().addBox(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -9.0F, -6.0F, 0.7418F, 0.0F, 0.0F));

        PartDefinition Body_r6 = Body.addOrReplaceChild("Body_r6", CubeListBuilder.create().texOffs(-4, 124).mirror().addBox(-6.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -9.0F, -6.0F, 0.6981F, 0.0F, -0.2182F));

        PartDefinition Body_r7 = Body.addOrReplaceChild("Body_r7", CubeListBuilder.create().texOffs(8, 124).mirror().addBox(0.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -9.0F, -6.0F, 0.6981F, 0.0F, 0.2182F));

        PartDefinition Body4 = Body.addOrReplaceChild("Body4", CubeListBuilder.create().texOffs(2, 86).addBox(-3.5F, 0.0F, -1.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -9.0F, -3.0F, 0.7854F, -0.0873F, -0.6545F));

        PartDefinition Body5 = Body4.addOrReplaceChild("Body5", CubeListBuilder.create().texOffs(3, 77).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, 6.0F, -0.6981F, 0.0F, 0.0F));

        PartDefinition Body6 = Body5.addOrReplaceChild("Body6", CubeListBuilder.create().texOffs(6, 71).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 6.0F, 0.829F, 0.0F, 0.0F));

        PartDefinition Body7 = Body.addOrReplaceChild("Body7", CubeListBuilder.create().texOffs(2, 86).mirror().addBox(-0.5F, 0.0F, -1.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.5F, -9.0F, -3.0F, 0.7854F, 0.0873F, 0.6545F));

        PartDefinition Body8 = Body7.addOrReplaceChild("Body8", CubeListBuilder.create().texOffs(3, 77).mirror().addBox(1.5F, 0.0F, 0.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.5F, 0.0F, 6.0F, -0.6981F, 0.0F, 0.0F));

        PartDefinition Body9 = Body8.addOrReplaceChild("Body9", CubeListBuilder.create().texOffs(6, 71).mirror().addBox(2.0F, -2.0F, 0.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 3.0F, 6.0F, 0.829F, 0.0F, 0.0F));

        PartDefinition Body3 = Body.addOrReplaceChild("Body3", CubeListBuilder.create().texOffs(82, 66).addBox(-4.5F, 0.0F, 0.0F, 11.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -9.0F, -5.0F, 0.1571F, 0.0F, 0.0F));

        PartDefinition Body_r8 = Body3.addOrReplaceChild("Body_r8", CubeListBuilder.create().texOffs(8, 116).mirror().addBox(-0.5F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.48F, -0.1309F, 0.0873F));

        PartDefinition Body_r9 = Body3.addOrReplaceChild("Body_r9", CubeListBuilder.create().texOffs(8, 116).mirror().addBox(0.5F, 0.0F, 1.5F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 1.0F, 2.0F, 0.6109F, -0.1745F, 0.0873F));

        PartDefinition Body_r10 = Body3.addOrReplaceChild("Body_r10", CubeListBuilder.create().texOffs(-4, 116).mirror().addBox(-5.5F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.48F, 0.1309F, -0.0873F));

        PartDefinition Body_r11 = Body3.addOrReplaceChild("Body_r11", CubeListBuilder.create().texOffs(-4, 116).mirror().addBox(-6.5F, 0.0F, 1.5F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 1.0F, 2.0F, 0.6109F, 0.1745F, -0.0873F));

        PartDefinition Body2 = Body.addOrReplaceChild("Body2", CubeListBuilder.create().texOffs(6, 12).addBox(-6.0F, -6.0F, -4.0F, 12.0F, 11.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(89, 55).addBox(-5.0F, 4.0F, -3.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 1.0F, 0.7418F, 0.0F, 0.0F));

        PartDefinition Body_r12 = Body2.addOrReplaceChild("Body_r12", CubeListBuilder.create().texOffs(8, 100).mirror().addBox(-5.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 6.0F, -1.1345F, -0.0873F, 0.0F));

        PartDefinition Body_r13 = Body2.addOrReplaceChild("Body_r13", CubeListBuilder.create().texOffs(8, 124).mirror().addBox(-1.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 6.0F, -1.1345F, 0.0873F, 0.0F));

        PartDefinition Body_r14 = Body2.addOrReplaceChild("Body_r14", CubeListBuilder.create().texOffs(8, 124).mirror().addBox(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 6.0F, -1.1345F, 0.0F, 0.0F));

        PartDefinition Body_r15 = Body2.addOrReplaceChild("Body_r15", CubeListBuilder.create().texOffs(8, 124).mirror().addBox(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -6.0F, 6.0F, -0.8727F, 0.0F, 0.0F));

        PartDefinition Body_r16 = Body2.addOrReplaceChild("Body_r16", CubeListBuilder.create().texOffs(-4, 100).mirror().addBox(-6.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -3.0F, 6.0F, -1.0385F, -0.1745F, 0.0F));

        PartDefinition Body_r17 = Body2.addOrReplaceChild("Body_r17", CubeListBuilder.create().texOffs(8, 116).mirror().addBox(-6.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -5.0F, 6.0F, -0.9076F, -0.0436F, 0.0F));

        PartDefinition Body_r18 = Body2.addOrReplaceChild("Body_r18", CubeListBuilder.create().texOffs(8, 108).mirror().addBox(0.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -3.0F, 6.0F, -1.0385F, 0.1745F, 0.0F));

        PartDefinition Body_r19 = Body2.addOrReplaceChild("Body_r19", CubeListBuilder.create().texOffs(-4, 116).mirror().addBox(0.0F, 0.0F, 0.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -5.0F, 6.0F, -0.9076F, 0.0436F, 0.0F));

        PartDefinition Body_r20 = Body2.addOrReplaceChild("Body_r20", CubeListBuilder.create().texOffs(40, 4).addBox(5.0F, -3.0F, 0.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(29, 4).addBox(-6.0F, -3.0F, 0.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -4.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition Tail01 = Body2.addOrReplaceChild("Tail01", CubeListBuilder.create().texOffs(73, 23).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(106, 22).addBox(-1.0F, 0.5F, 0.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 5.0F, -1.0472F, 0.0F, 0.0F));

        PartDefinition Tail02_r1 = Tail01.addOrReplaceChild("Tail02_r1", CubeListBuilder.create().texOffs(91, 23).addBox(-1.0F, 0.0F, -0.8F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 6.0F, -0.2618F, 0.0F, 0.0F));

        PartDefinition Tail02_r2 = Tail01.addOrReplaceChild("Tail02_r2", CubeListBuilder.create().texOffs(110, 8).addBox(-1.5F, 0.5F, 3.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(112, 13).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 6.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition LegRight = All.addOrReplaceChild("LegRight", CubeListBuilder.create().texOffs(1, 45).addBox(-2.0F, 8.0F, -2.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 4.0F, 5.0F, 0.2618F, 0.0F, 0.0873F));

        PartDefinition LegRight_r1 = LegRight.addOrReplaceChild("LegRight_r1", CubeListBuilder.create().texOffs(50, 17).addBox(-2.5F, 3.0F, 2.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(42, 16).addBox(-3.5F, 0.0F, 1.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(29, 35).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -4.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition LegRight_r2 = LegRight.addOrReplaceChild("LegRight_r2", CubeListBuilder.create().texOffs(53, 17).addBox(-3.0F, -2.0F, 2.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(42, 16).addBox(-4.0F, -4.0F, 1.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(42, 16).addBox(-4.0F, -6.0F, 3.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(-2.0F, -6.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -4.0F, -0.3927F, 0.0F, 0.0F));

        PartDefinition LegRight2 = LegRight.addOrReplaceChild("LegRight2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.0F));

        PartDefinition LegRight_r3 = LegRight2.addOrReplaceChild("LegRight_r3", CubeListBuilder.create().texOffs(18, 46).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, -0.0436F, 0.0F, 0.0F));

        PartDefinition LegRight_r4 = LegRight2.addOrReplaceChild("LegRight_r4", CubeListBuilder.create().texOffs(18, 42).addBox(-1.5F, 1.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 46).addBox(-2.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, -2.0F, 0.2618F, 0.2618F, 0.0F));

        PartDefinition LegRight_r5 = LegRight2.addOrReplaceChild("LegRight_r5", CubeListBuilder.create().texOffs(18, 42).addBox(0.5F, 1.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 46).addBox(0.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, -2.0F, 0.2182F, -0.2618F, 0.0F));

        PartDefinition LegLeft = All.addOrReplaceChild("LegLeft", CubeListBuilder.create().texOffs(1, 45).mirror().addBox(-2.0F, 8.0F, -2.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(6.0F, 4.0F, 5.0F, 0.2618F, 0.0F, -0.0873F));

        PartDefinition LegLeft_r1 = LegLeft.addOrReplaceChild("LegLeft_r1", CubeListBuilder.create().texOffs(53, 17).mirror().addBox(1.5F, 3.0F, 1.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(29, 35).mirror().addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 3.0F, -4.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition LegLeft_r2 = LegLeft.addOrReplaceChild("LegLeft_r2", CubeListBuilder.create().texOffs(50, 17).mirror().addBox(2.0F, -2.0F, 2.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(42, 16).mirror().addBox(2.0F, -4.0F, 1.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(42, 16).mirror().addBox(2.0F, -5.0F, 3.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 34).mirror().addBox(-2.0F, -6.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 3.0F, -4.0F, -0.3927F, 0.0F, 0.0F));

        PartDefinition LegLeft2 = LegLeft.addOrReplaceChild("LegLeft2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.0F));

        PartDefinition LegLeft_r3 = LegLeft2.addOrReplaceChild("LegLeft_r3", CubeListBuilder.create().texOffs(18, 46).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, -0.0436F, 0.0F, 0.0F));

        PartDefinition LegLeft_r4 = LegLeft2.addOrReplaceChild("LegLeft_r4", CubeListBuilder.create().texOffs(18, 42).mirror().addBox(-1.5F, 1.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(18, 46).mirror().addBox(-2.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 8.0F, -2.0F, 0.2182F, 0.2618F, 0.0F));

        PartDefinition LegLeft_r5 = LegLeft2.addOrReplaceChild("LegLeft_r5", CubeListBuilder.create().texOffs(18, 42).mirror().addBox(0.5F, 1.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(18, 46).mirror().addBox(0.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 8.0F, -2.0F, 0.2618F, -0.2618F, 0.0F));

        PartDefinition WingRight01 = All.addOrReplaceChild("WingRight01", CubeListBuilder.create().texOffs(51, 34).addBox(-22.0F, -1.5F, -1.0F, 22.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 53).addBox(-22.0F, 0.0F, 1.5F, 22.0F, 0.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(110, 37).mirror().addBox(-22.0F, -0.5F, 10.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(99, 35).mirror().addBox(-22.0F, -0.5F, 2.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.0F, -3.0F, -4.5F, 0.0F, -0.3054F, 0.2182F));

        PartDefinition WingRight01_r1 = WingRight01.addOrReplaceChild("WingRight01_r1", CubeListBuilder.create().texOffs(63, 30).addBox(-1.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 14).addBox(-0.5F, 0.0F, -5.5F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 16).addBox(-0.5F, -1.0F, -2.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 23).addBox(-1.0F, 0.0F, -3.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.5F, -1.0F, -1.0F, 0.2618F, 0.2618F, -0.2182F));

        PartDefinition WingRight01_r2 = WingRight01.addOrReplaceChild("WingRight01_r2", CubeListBuilder.create().texOffs(110, 37).mirror().addBox(-0.5F, -1.5F, 9.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(99, 35).mirror().addBox(-0.5F, -1.5F, 1.5F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-21.5F, 1.0F, 0.5F, 0.0F, 0.6981F, 0.0F));

        PartDefinition WingRight02 = WingRight01.addOrReplaceChild("WingRight02", CubeListBuilder.create(), PartPose.offsetAndRotation(-21.0F, 0.0F, -0.5F, 0.0F, 0.3054F, -0.3491F));

        PartDefinition WingRight02_r1 = WingRight02.addOrReplaceChild("WingRight02_r1", CubeListBuilder.create().texOffs(112, 39).mirror().addBox(-0.5F, -1.5F, 9.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(99, 35).mirror().addBox(-0.5F, -1.5F, 1.5F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 1.0F, 1.0F, 0.0436F, -0.6981F, 0.0F));

        PartDefinition WingRight02_r2 = WingRight02.addOrReplaceChild("WingRight02_r2", CubeListBuilder.create().texOffs(39, 51).addBox(-17.0F, 0.0F, 1.5F, 18.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.5F, 0.0436F, 0.1745F, 0.0F));

        PartDefinition WingRight02_r3 = WingRight02.addOrReplaceChild("WingRight02_r3", CubeListBuilder.create().texOffs(53, 40).addBox(-18.0F, -1.0F, -0.5F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.5F, 0.0F, 0.1745F, 0.0F));

        PartDefinition WingLeft01 = All.addOrReplaceChild("WingLeft01", CubeListBuilder.create().texOffs(-7, 53).mirror().addBox(0.0F, 0.0F, 0.5F, 22.0F, 0.0F, 13.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(51, 34).mirror().addBox(0.0F, -1.5F, -2.0F, 22.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(99, 35).mirror().addBox(21.0F, -0.5F, 1.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(110, 37).mirror().addBox(21.0F, -0.5F, 9.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.0F, -3.0F, -3.5F, 0.0F, 0.3054F, -0.2182F));

        PartDefinition WingLeft01_r1 = WingLeft01.addOrReplaceChild("WingLeft01_r1", CubeListBuilder.create().texOffs(56, 16).addBox(0.5F, -1.0F, -2.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 14).addBox(0.5F, 0.0F, -5.5F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 23).addBox(0.0F, 0.0F, -3.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(63, 30).addBox(0.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.5F, -1.0F, -2.0F, 0.2618F, -0.2618F, 0.2182F));

        PartDefinition WingLeft01_r2 = WingLeft01.addOrReplaceChild("WingLeft01_r2", CubeListBuilder.create().texOffs(110, 37).mirror().addBox(-0.5F, -1.5F, 9.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(99, 35).mirror().addBox(-0.5F, -1.5F, 1.5F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(21.5F, 1.0F, -0.5F, 0.0F, -0.6981F, 0.0F));

        PartDefinition WingLeft02 = WingLeft01.addOrReplaceChild("WingLeft02", CubeListBuilder.create(), PartPose.offsetAndRotation(21.0F, 0.0F, -1.5F, 0.0F, -0.3054F, 0.3491F));

        PartDefinition WingLeft02_r1 = WingLeft02.addOrReplaceChild("WingLeft02_r1", CubeListBuilder.create().texOffs(112, 39).mirror().addBox(-0.5F, -1.5F, 9.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(99, 35).mirror().addBox(-0.5F, -1.5F, 1.5F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, 1.0F, 1.0F, 0.0436F, 0.6981F, 0.0F));

        PartDefinition WingLeft02_r2 = WingLeft02.addOrReplaceChild("WingLeft02_r2", CubeListBuilder.create().texOffs(39, 51).mirror().addBox(-1.0F, 0.0F, 1.5F, 18.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 0.0F, 0.5F, 0.0436F, -0.1745F, 0.0F));

        PartDefinition WingLeft02_r3 = WingLeft02.addOrReplaceChild("WingLeft02_r3", CubeListBuilder.create().texOffs(53, 40).mirror().addBox(-2.0F, -1.0F, -0.5F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 0.0F, 0.5F, 0.0F, -0.1745F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float loop = ageInTicks;
        float lookX = headPitch * ((float) Math.PI / 180F);
        float lookY = netHeadYaw * ((float) Math.PI / 180F);
        float wingScale = 1.0F;

        boolean flying = !entity.onGround() && !entity.isInWater();
        if (entity instanceof BaseCreatureEntity baseCreature && baseCreature.hasPerchTarget()) {
            flying = false;
        }

        if (flying) {
            WingLeft01.xRot = (float) Math.toRadians(-40);
            WingLeft01.yRot = (float) Math.toRadians(20.5F);
            WingLeft01.zRot = (float) Math.sin(loop * 0.4F * wingScale) * 0.6F;

            WingRight01.xRot = (float) Math.toRadians(-40);
            WingRight01.yRot = (float) Math.toRadians(-20.5F);
            WingRight01.zRot = (float) Math.sin(loop * 0.4F * wingScale + Math.PI) * 0.6F;

            WingLeft02.zRot = (float) Math.sin(loop * 0.4F * wingScale) * 0.15F;
            WingRight02.zRot = (float) Math.sin(loop * 0.4F * wingScale + Math.PI) * 0.15F;
        } else {
            WingLeft01.xRot = WingLeft01.yRot = WingLeft01.zRot = 0;
            WingRight01.xRot = WingRight01.yRot = WingRight01.zRot = 0;
            WingLeft02.zRot = WingRight02.zRot = 0;
        }

        Body.xRot = lookX * 0.75F;
        Body.yRot = lookY * 0.75F;

        Eye.xRot = lookX * 0.25F;
        Eye.yRot = lookY * 0.25F;
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