package com.lycanitesmobs.client.renderer.layer.creature;

import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.creature.aberration.EntityYale;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class LayerCreatureDye extends LayerCreatureBase {
    public String textureSuffix;
    public boolean subspecies = true;

    public LayerCreatureDye(CreatureRenderer renderer, String textureSuffix, boolean subspecies) {
        super(renderer);
        this.name = textureSuffix;
        this.textureSuffix = textureSuffix;
        this.subspecies = subspecies;
    }

    public LayerCreatureDye(CreatureRenderer renderer, String name, String textureSuffix, boolean subspecies) {
        super(renderer);
        this.name = name;
        this.textureSuffix = textureSuffix;
        this.subspecies = subspecies;
    }

    @Override
    public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
        if(!super.canRenderLayer(entity, scale))
            return false;
        if(!(entity instanceof EntityYale))
            return true;
        return ((EntityYale)entity).hasFur();
    }

    @Override
    public boolean canRenderPart(String partName, BaseCreatureEntity entity, boolean trophy) {
        return this.name.equals(partName);
    }

    @Override
    public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
        DyeColor color = entity.getColor();
        return new Vector4f(color.getTextureDiffuseColors()[0], color.getTextureDiffuseColors()[1], color.getTextureDiffuseColors()[2], 1.0F);
    }

    @Override
    public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
        if (this.subspecies) {
            return entity.getTexture(this.textureSuffix);
        }
        return entity.getSubTexture(this.textureSuffix);
    }
}
