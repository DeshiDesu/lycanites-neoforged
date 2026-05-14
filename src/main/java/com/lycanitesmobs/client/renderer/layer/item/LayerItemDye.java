package com.lycanitesmobs.client.renderer.layer.item;

import com.lycanitesmobs.client.renderer.item.IItemModelRenderer;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class LayerItemDye extends LayerItem {

    public LayerItemDye(IItemModelRenderer renderer, String name) {
        super(renderer, name);
    }

    @Override
    public Vector4f getPartColor(String partName, ItemStack itemStack, float loop) {
        if (!(itemStack.getItem() instanceof ItemEquipmentPart)) {
            return super.getPartColor(partName, itemStack, loop);
        }
        ItemEquipmentPart itemEquipmentPart = (ItemEquipmentPart) itemStack.getItem();
        Vector3d partColor = itemEquipmentPart.getColor(itemStack);
        return new Vector4f((float) partColor.x(), (float) partColor.y(), (float) partColor.z(), 1);
    }
}
