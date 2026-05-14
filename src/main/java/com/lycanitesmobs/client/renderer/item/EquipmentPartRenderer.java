package com.lycanitesmobs.client.renderer.item;

import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.model.animation.AnimationPart;
import com.lycanitesmobs.client.model.item.ItemObjModel;
import com.lycanitesmobs.client.obj.model.VBOObjModel;
import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EquipmentPartRenderer extends BlockEntityWithoutLevelRenderer implements IItemModelRenderer {

    protected final List<LayerItem> renderLayers = new ArrayList<>();

    public EquipmentPartRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    public EquipmentPartRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack itemStack,
                             ItemDisplayContext displayContext,
                             PoseStack matrixStack,
                             MultiBufferSource renderTypeBuffer,
                             int brightness,
                             int packedOverlay) {
        if (!(itemStack.getItem() instanceof ItemEquipmentPart itemEquipmentPart)) {
            return;
        }

        InteractionHand hand = null;

        ItemObjModel itemObjModel = ModelManager.getInstance().getEquipmentPartModel(itemEquipmentPart);
        if (itemObjModel == null) {
            return;
        }

        for (AnimationPart animPart : itemObjModel.animationParts.values()) {
            animPart.setOffset(null);
        }

        this.renderLayers.clear();
        itemObjModel.addCustomLayers(this);

        float loop = 0;
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().isPaused()) {
            loop = Minecraft.getInstance().player.tickCount;
        }

        if (displayContext == ItemDisplayContext.GUI) {
            matrixStack.mulPose(Axis.YN.rotationDegrees(45F));
            matrixStack.mulPose(Axis.YP.rotationDegrees(-90F));
            matrixStack.mulPose(Axis.XN.rotationDegrees(-45F));
            matrixStack.mulPose(Axis.XP.rotationDegrees(0F));
            matrixStack.mulPose(Axis.ZN.rotationDegrees(0F));
            matrixStack.mulPose(Axis.ZP.rotationDegrees(-180F));
        }

        matrixStack.pushPose();
        String itemName = Arrays.stream(itemEquipmentPart.itemName.split("_")).toList().get(1);

        if (displayContext == ItemDisplayContext.GUI) {
            applyGuiTransform(itemName, matrixStack);
            matrixStack.translate(0F, -1.5F, -1.7F);
        } else if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            applyFirstPersonTransform(itemName, matrixStack);
        } else {
            applyOtherTransform(displayContext, itemName, matrixStack);
        }

        itemObjModel.generateAnimationFrames(itemStack, null, loop, null);
        this.renderModel(itemObjModel, itemStack, hand, matrixStack, renderTypeBuffer, null, null, loop, brightness);
        for (LayerItem renderLayer : this.renderLayers) {
            this.renderModel(itemObjModel, itemStack, hand, matrixStack, renderTypeBuffer, renderLayer, null, loop, brightness);
        }
        itemObjModel.clearAnimationFrames();

        matrixStack.popPose();
        VBOBatcher.getInstance().endBatches();
    }

    private void applyGuiTransform(String itemName, PoseStack matrixStack) {
        switch (itemName) {
            case "eechetikarm" -> {
                matrixStack.scale(0.9f, 0.9f, 0.9f);
                matrixStack.translate(1F, 0F, 0.18F);
            }
            case "argustail" -> {
                matrixStack.translate(1F, 0.1F, 0.3F);
                matrixStack.mulPose(Axis.ZP.rotationDegrees(30F));
                matrixStack.mulPose(Axis.ZN.rotationDegrees(40F));
            }
            case "belphegorarm" -> matrixStack.translate(1.3F, 0F, 0.4F);
            case "cinderblade" -> matrixStack.translate(0.4F, 0F, 0.3F);
            case "entarm" -> matrixStack.translate(1F, 0F, 0.3F);
            case "epionwing" -> {
                matrixStack.scale(0.75f, 0.75f, 0.75f);
                matrixStack.translate(0.3F, -0.4F, 0.1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(-25F));
            }
            case "woodenpaxel", "wildkinarm" -> {
                matrixStack.scale(0.75f, 0.75f, 0.75f);
                matrixStack.translate(2F, -0.5F, 0.1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            }
            case "wendigoantler" -> matrixStack.translate(1.5F, -0.7F, 0.8F);
            case "sutiramustinger" -> {
                matrixStack.scale(0.75f, 0.75f, 0.75f);
                matrixStack.translate(3F, -2F, 0F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            }
            case "stryderheart" -> {
                matrixStack.scale(0.75f, 0.75f, 0.75f);
                matrixStack.translate(1.5F, .2F, 0F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            }
            case "spectretendril" -> {
                matrixStack.scale(0.75f, 0.75f, 0.75f);
                matrixStack.translate(1.1F, .2F, 0F);
            }
            case "remobrawing" -> {
                matrixStack.translate(1F, .1F, .3F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(45F));
            }
            case "reiverhorns" -> {
                matrixStack.translate(.4F, 0.5F, -1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            }
            case "reaperclaw" -> {
                matrixStack.translate(.4F, 0.7F, -1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            }
            case "raidrablade" -> {
                matrixStack.scale(0.8f, 0.8f, 0.8f);
                matrixStack.translate(.8F, .5F, -.5F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
                matrixStack.mulPose(Axis.ZP.rotationDegrees(90F));
            }
            case "lacedonhead" -> {
                matrixStack.translate(0.2F, -.7F, .2F);
                matrixStack.mulPose(Axis.ZP.rotationDegrees(90F));
            }
            case "ironpaxel" -> {
                matrixStack.translate(0.2F, .8F, -1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            }
            case "ioraystinger" -> matrixStack.translate(-.2F, 1.5F, -.9F);
            case "frostweaverleg" -> matrixStack.translate(-.3F, 0.2F, 0.1F);
            case "ettinclub", "bruchaquill" -> {
                matrixStack.scale(0.9f, 0.9f, 0.9f);
                matrixStack.translate(-.2F, 0F, 0F);
            }
            case "erepededrill" -> {
                matrixStack.scale(0.9f, 0.9f, 0.9f);
                matrixStack.translate(0F, 0.1F, .2F);
            }
            case "cherufecore" -> {
                matrixStack.scale(0.9f, 0.9f, 0.9f);
                matrixStack.translate(0.1F, 0.1F, 0.05F);
            }
            case "behemophethand" -> {
                matrixStack.scale(0.9f, 0.9f, 0.9f);
                matrixStack.translate(0F, 0.4F, -0.1F);
            }
            case "apollyonclaw" -> {
                matrixStack.scale(1.2F, 1.2F, 1.2F);
                matrixStack.translate(-.2F, 0.35F, -0.8F);
                matrixStack.mulPose(Axis.XP.rotationDegrees(45F));
                matrixStack.mulPose(Axis.YP.rotationDegrees(45F));
            }
            case "sylphwing" -> {
                matrixStack.scale(0.9f, 0.9f, 0.9f);
                matrixStack.translate(0.5F, 0F, -.1F);
            }
            case "astarothclaw" -> matrixStack.translate(0F, 0.45F, 0F);
            case "vespidstinger" -> matrixStack.translate(1.2F, 0F, 0.5F);
            case "wraithskull" -> {
                matrixStack.mulPose(Axis.ZP.rotationDegrees(-100F));
                matrixStack.mulPose(Axis.YN.rotationDegrees(-9F));
                matrixStack.mulPose(Axis.YP.rotationDegrees(-140F));
                matrixStack.mulPose(Axis.XP.rotationDegrees(-180F));
                matrixStack.mulPose(Axis.XN.rotationDegrees(20F));
                matrixStack.translate(-.35F, 0F, 0.15F);
            }
            case "sprigganheart" -> matrixStack.translate(0.3F, 0.2F, 0.1F);
            case "afritlung" -> matrixStack.translate(0F, .3F, 0F);
            case "conbabutt", "eyewigeye", "naxiriseye", "malwratheye", "bansheeeye",
                 "woodenguard", "gammasphere", "ironrod", "arixbrain", "geonachfist" -> {
                if (itemName.equals("bansheeeye")
                        || itemName.equals("woodenguard")
                        || itemName.equals("gammasphere")) {
                    matrixStack.translate(0.1F, .8F, -.1F);
                } else {
                    matrixStack.translate(0.1F, .5F, .1F);
                }
                if (itemName.equals("ironrod")) {
                    matrixStack.translate(0.55F, -.1F, 0F);
                } else {
                    matrixStack.scale(1.3f, 1.3f, 1.3f);
                }
            }
            default -> matrixStack.translate(.7F, .2F, 0.3F);
        }
    }

    private void applyFirstPersonTransform(String itemName, PoseStack matrixStack) {
        matrixStack.scale(0.7f, 0.7f, 0.7f);

        switch (itemName) {
            case "bansheeeye",
                 "belphegorarm",
                 "ironpikejoint",
                 "reiverhorns",
                 "wargskull" -> {
                matrixStack.translate(0F, 0.3F, -0.3F);
            }

            case "bruchaquill",
                 "ettinclub" -> {
                matrixStack.translate(0F, -0.3F, 0.7F);
                matrixStack.scale(0.7f, 0.7f, 0.7f);
            }

            case "cherufecore",
                 "cinderblade",
                 "erepededrill",
                 "frostweaverleg",
                 "sprigganheart",
                 "stryderheart" -> {
                matrixStack.scale(0.7f, 0.7f, 0.7f);
                matrixStack.translate(0.4F, 0F, 0.3F);
            }

            case "darklingskull",
                 "ironaxehead" -> {
                matrixStack.translate(-.1F, 2.5F, 1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(-130F));
            }

            case "epionwing" -> {
                matrixStack.scale(0.65f, 0.65f, 0.65f);
                matrixStack.translate(0.8F, 0F, 0.7F);
            }

            case "ioraystinger",
                 "lacedonhead",
                 "sutiramustinger",
                 "vespidstinger" -> {
                matrixStack.translate(.4F, 2.2F, 1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(-130F));
            }

            case "ironguard",
                 "woodenguard",
                 "wraithskull" -> {
                matrixStack.translate(-.1F, 2.5F, 1.15F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(-130F));
            }

            case "apollyonclaw" -> {
                matrixStack.translate(0F, -.3F, -0.3F);
            }

            default -> {
            }
        }

        matrixStack.translate(0.65F, -.1F, 0F);
    }

    private void applyOtherTransform(ItemDisplayContext displayContext, String itemName, PoseStack matrixStack) {
        float offSetZ = -.32F;
        float offSetY = -0.7F;
        float offSetX = .55F;

        switch (itemName) {
            case "bruchaquill",
                 "cinderblade",
                 "erepededrill",
                 "ettinclub",
                 "frostweaverleg",
                 "goldscepterhead",
                 "raidrablade",
                 "sprigganheart",
                 "stryderheart" -> {
                offSetY += -.8f;
                offSetZ += .1f;
            }
            default -> {
            }
        }

        switch (itemName) {
            case "ioraystinger",
                 "lacedonhead",
                 "sutiramustinger",
                 "vespidstinger" -> {
                offSetX += -.1f;
                offSetY += 0.5f;
                offSetZ += -.6f;
            }

            case "wraithskull" -> {
                matrixStack.mulPose(Axis.ZP.rotationDegrees(-90F));
                matrixStack.mulPose(Axis.YN.rotationDegrees(0F));
                matrixStack.mulPose(Axis.YP.rotationDegrees(0F));
                matrixStack.mulPose(Axis.XP.rotationDegrees(0F));
                offSetX += -1f;
                offSetY += 0f;
                offSetZ += 0f;
            }

            default -> {
            }
        }

        offSetX += 0f;
        offSetY += 0.5f;
        offSetZ += -1.2f;

        if (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {

            switch (itemName) {
                case "bansheeeye",
                     "darklingskull",
                     "entarm",
                     "gammasphere",
                     "geonachfist",
                     "ironaxehead",
                     "ironguard",
                     "woodenguard",
                     "wraithskull" -> {
                    matrixStack.translate(0f, 0f, .5f);
                }

                case "behemophethand",
                     "clinkscythe",
                     "conbabutt" -> {
                    matrixStack.translate(0f, 0f, .4f);
                }

                case "belphegorarm" -> {
                    matrixStack.translate(0f, -0.1f, .9f);
                }

                case "bruchaquill",
                     "cinderblade",
                     "ettinclub",
                     "frostweaverleg" -> {
                    matrixStack.translate(0f, 1f, -.3f);
                    matrixStack.mulPose(Axis.XP.rotationDegrees(90F));
                }

                case "epionwing" -> {
                    matrixStack.scale(0.5f, 0.5f, 0.5f);
                    matrixStack.translate(.6f, 1.7f, 0.1f);
                    matrixStack.mulPose(Axis.XP.rotationDegrees(70F));
                }

                case "erepededrill",
                     "lacedonhead" -> {
                    matrixStack.translate(0f, .6f, -.4f);
                    matrixStack.mulPose(Axis.XP.rotationDegrees(90F));
                }

                case "eyewigeye",
                     "geonachspear" -> {
                    matrixStack.translate(0f, 0f, .3f);
                }

                case "goldscepterhead" -> {
                    matrixStack.translate(0f, -0.4f, .9f);
                }

                case "grueclaw",
                     "ironrod" -> {
                    matrixStack.translate(0f, 0.5f, 0f);
                    matrixStack.mulPose(Axis.XP.rotationDegrees(90F));
                }

                case "ironpikejoint",
                     "reiverhorns" -> {
                    matrixStack.translate(0f, 0f, .7f);
                }

                case "raidrablade",
                     "stryderheart" -> {
                    matrixStack.translate(-0.15f, .8f, 1.55f);
                    matrixStack.mulPose(Axis.XP.rotationDegrees(270F));
                }

                case "sprigganheart" -> {
                    matrixStack.translate(-0.05f, .8f, 1.55f);
                    matrixStack.mulPose(Axis.XP.rotationDegrees(270F));
                }

                default -> {
                    matrixStack.translate(0f, .3f, 1f);
                    matrixStack.mulPose(Axis.XP.rotationDegrees(-90F));
                }
            }

            matrixStack.translate(0f, 0f, -.5f);
        }

        float pivotX = 0.0F;
        float pivotY = 0.5F;
        float pivotZ = 0.0F;

        matrixStack.translate(pivotX, pivotY, pivotZ);
        matrixStack.mulPose(Axis.XN.rotationDegrees(220F));
        matrixStack.translate(-pivotX, -pivotY, -pivotZ);
        matrixStack.translate(offSetX, offSetY, offSetZ);
    }

    protected void renderModel(ItemObjModel model,
                               ItemStack itemStack,
                               InteractionHand hand,
                               PoseStack matrixStack,
                               MultiBufferSource renderTypeBuffer,
                               LayerItem layer,
                               AnimationPart offsetObjPart,
                               float loop,
                               int brightness) {
        ResourceLocation texture = model.getTexture(itemStack, layer);
        RenderType renderType = CustomRenderStates.getObjVBORenderType(
                model.getBlending(itemStack, layer),
                model.getGlow(itemStack, layer)
        );

        VBOObjModel.renderType = renderType;
        VBOObjModel.renderNormal = true;
        VBOObjModel.tex = texture;
        model.render(itemStack, hand, matrixStack, null, this, offsetObjPart, layer, loop, brightness);
        VBOObjModel.tex = null;
        VBOObjModel.renderNormal = false;
        VBOObjModel.renderType = null;
    }

    @Override
    public void bindItemTexture(ResourceLocation location) {
        if (location == null) {
            return;
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(location);
    }

    @Override
    public List<LayerItem> addLayer(LayerItem renderLayer) {
        if (!this.renderLayers.contains(renderLayer)) {
            this.renderLayers.add(renderLayer);
        }
        return this.renderLayers;
    }
}
