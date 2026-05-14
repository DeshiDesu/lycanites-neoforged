package com.lycanitesmobs.client.model.item;

import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector4f;

public class ModelEquipmentPart extends ItemObjModel {
    /**
     * Constructor
     *
     * @param equipmentPart The equipment part item to render.
     */
    public ModelEquipmentPart(ItemEquipmentPart equipmentPart) {
        this.initModel(equipmentPart.itemName, "equipment/" + equipmentPart.itemName.replace("equipmentpart_", ""));
    }

    /**
     * Gets the texture for the provided item and layer.
     *
     * @param itemStack The item to get the texture for.
     * @param layer     The later to get the texture for, null for the base layer.
     * @return The texture to render.
     */
    @Override
    public ResourceLocation getTexture(ItemStack itemStack, LayerItem layer) {
        if (!(itemStack.getItem() instanceof ItemEquipmentPart)) {
            return null;
        }
        ItemEquipmentPart itemEquipmentPart = (ItemEquipmentPart) itemStack.getItem();

        String suffix = "";
        if (layer != null && layer.textureSuffix != null && !layer.textureSuffix.isEmpty()) {
            suffix = "_" + layer.textureSuffix;
        }

        String textureName = itemEquipmentPart.itemName.toLowerCase().replace("equipmentpart_", "") + suffix;
        if (TextureManager.getTexture(textureName) == null)
            TextureManager.addTexture(textureName, "textures/equipment/" + textureName + ".png");
        return TextureManager.getTexture(textureName);
    }

    /**
     * Gets the render color for the provided item and layer.
     *
     * @param partName  The name of the model part to render.
     * @param itemStack The item to get the texture for.
     * @param layer     The later to get the texture for, null for the base layer.
     * @param loop      The render animation tick for looping effects.
     * @return The part color to render.
     */
    @Override
    public Vector4f getPartColor(String partName, ItemStack itemStack, LayerItem layer, float loop) {
        if (!(itemStack.getItem() instanceof ItemEquipmentPart)) {
            return super.getPartColor(partName, itemStack, layer, loop);
        }
        ItemEquipmentPart itemEquipmentPart = (ItemEquipmentPart) itemStack.getItem();

        if (layer != null) {
            return layer.getPartColor(partName, itemStack, loop);
        }

        return new Vector4f(1, 1, 1, 1);
    }
}
