package com.lycanitesmobs.client.model.item;

import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.model.animation.AnimationPart;
import com.lycanitesmobs.client.obj.model.VBOObjModel;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.item.IItemModelRenderer;
import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EquipmentModel implements IItemModelRenderer {
    protected List<LayerItem> renderLayers = new ArrayList<>();
    protected List<ItemObjModel> renderedModels = new ArrayList<>();

    /**
     * Constructor
     */
    public EquipmentModel() {
    }


    /**
     * Renders an Equipment Item Stack.
     *
     * @param itemStack        The itemstack to render.
     * @param hand             The hand that is holding the item or null if in the inventory instead.
     * @param matrixStack      The matrix stack for animation.
     * @param renderTypeBuffer The render buffer to render with.
     * @param renderer         The renderer that is rendering this model, needed for texture binding.
     * @param loop             The animation tick for looping animations, etc.
     * @param brightness       The base brightness to render at.
     */
    public void render(ItemStack itemStack, InteractionHand hand, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, IItemModelRenderer renderer, float loop, int brightness) {
        if (!(itemStack.getItem() instanceof ItemEquipment)) {
            return;
        }
        ItemEquipment itemEquipment = (ItemEquipment) itemStack.getItem();
        NonNullList<ItemStack> equipmentPartStacks = itemEquipment.getEquipmentPartStacks(itemStack);

        int slotId = -1;
        ItemObjModel modelPartBase = null;
        ItemObjModel modelPartHead = null;
        for (ItemStack partStack : equipmentPartStacks) {
            slotId++;

            AnimationPart targetPart = null;

            if (slotId == 0) {
                modelPartBase = this.renderPart(partStack, hand, matrixStack, renderTypeBuffer, renderer, null, loop, brightness);
            } else if (slotId == 1) {
                if (modelPartBase == null || !modelPartBase.animationParts.containsKey("head")) continue;
                targetPart = modelPartBase.animationParts.get("head");
                modelPartHead = this.renderPart(partStack, hand, matrixStack, renderTypeBuffer, renderer, targetPart, loop, brightness);
            } else if (slotId == 2) {
                if (modelPartHead == null || !modelPartHead.animationParts.containsKey("tipa")) continue;
                targetPart = modelPartHead.animationParts.get("tipa");
                this.renderPart(partStack, hand, matrixStack, renderTypeBuffer, renderer, targetPart, loop, brightness);
            } else if (slotId == 3) {
                if (modelPartHead == null || !modelPartHead.animationParts.containsKey("tipb")) continue;
                targetPart = modelPartHead.animationParts.get("tipb");
                this.renderPart(partStack, hand, matrixStack, renderTypeBuffer, renderer, targetPart, loop, brightness);
            } else if (slotId == 4) {
                if (modelPartHead == null || !modelPartHead.animationParts.containsKey("tipc")) continue;
                targetPart = modelPartHead.animationParts.get("tipc");
                if ((LMHelperClass.convertToResourceLocation(partStack.getItem()).getPath()).equals("equipmentpart_sutiramustinger") ||
                        (LMHelperClass.convertToResourceLocation(partStack.getItem()).getPath()).equals("equipmentpart_lacedonhead")) {
                    matrixStack.translate(0, 0.3, -0.1);
                }
                this.renderPart(partStack, hand, matrixStack, renderTypeBuffer, renderer, targetPart, loop, brightness);
            } else if (slotId == 5) {
                if (modelPartBase == null || !modelPartBase.animationParts.containsKey("pommel")) continue;
                targetPart = modelPartBase.animationParts.get("pommel");
                this.renderPart(partStack, hand, matrixStack, renderTypeBuffer, renderer, targetPart, loop, brightness);
            }
        }

        for (ItemObjModel itemObjModel : this.renderedModels) {
            itemObjModel.clearAnimationFrames();
            for (AnimationPart animationPart : itemObjModel.animationParts.values()) {
                animationPart.setOffset(null);
            }
        }
        this.renderedModels.clear();
    }


    /**
     * Renders an Equipment Part.
     *
     * @param partStack        The equipment part item stack to render.
     * @param hand             The hand that is holding the item or null if in the inventory instead.
     * @param matrixStack      The matrix stack for animation.
     * @param renderTypeBuffer The render buffer to render with.
     * @param renderer         The renderer that is rendering this model, needed for texture binding.
     * @param offsetPart       A ModelObjPart, if not null this model is offset by it, used by assembled equipment pieces to create a full model.
     * @param loop             The animation tick for looping animations, etc.
     * @param brightness       The base brightness to render at.
     */
    public ItemObjModel renderPart(ItemStack partStack, InteractionHand hand, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, IItemModelRenderer renderer, AnimationPart offsetPart, float loop, int brightness) {
        if (partStack.isEmpty() || !(partStack.getItem() instanceof ItemEquipmentPart)) {
            return null;
        }

        ItemEquipmentPart itemEquipmentPart = (ItemEquipmentPart) partStack.getItem();
        ItemObjModel itemObjModel = ModelManager.getInstance().getEquipmentPartModel(itemEquipmentPart);
        if (itemObjModel == null) {
            return null;
        }

        if (itemObjModel.animationParts.containsKey("base")) {
            itemObjModel.animationParts.get("base").setOffset(offsetPart);
        }

        this.renderLayers.clear();
        itemObjModel.addCustomLayers(this);
        itemObjModel.generateAnimationFrames(partStack, null, loop, offsetPart);

        ResourceLocation texture = itemObjModel.getTexture(partStack, null);
        RenderType renderType = CustomRenderStates.getObjVBORenderType(
                itemObjModel.getBlending(partStack, null),
                itemObjModel.getGlow(partStack, null)
        );
        VBOObjModel.renderType = renderType;
        VBOObjModel.renderNormal = true;
        VBOObjModel.tex = texture;
        itemObjModel.render(partStack, hand, matrixStack, null, renderer, offsetPart, null, loop, brightness);
        VBOObjModel.tex = null;
        VBOObjModel.renderNormal = false;
        VBOObjModel.renderType = null;

        for (LayerItem layer : this.renderLayers) {
            texture = itemObjModel.getTexture(partStack, layer);
            renderType = CustomRenderStates.getObjVBORenderType(
                    itemObjModel.getBlending(partStack, layer),
                    itemObjModel.getGlow(partStack, layer)
            );
            VBOObjModel.renderType = renderType;
            VBOObjModel.renderNormal = true;
            VBOObjModel.tex = texture;
            itemObjModel.render(partStack, hand, matrixStack, null, renderer, offsetPart, layer, loop, brightness);
            VBOObjModel.tex = null;
            VBOObjModel.renderNormal = false;
            VBOObjModel.renderType = null;
        }
        this.renderedModels.add(itemObjModel);

        return itemObjModel;
    }


    @Override
    public void bindItemTexture(ResourceLocation location) {
    }


    @Override
    public List<LayerItem> addLayer(LayerItem renderLayer) {
        if (!this.renderLayers.contains(renderLayer)) {
            this.renderLayers.add(renderLayer);
        }
        return this.renderLayers;
    }
}
