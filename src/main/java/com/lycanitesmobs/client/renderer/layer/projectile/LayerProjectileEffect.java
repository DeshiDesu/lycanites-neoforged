package com.lycanitesmobs.client.renderer.layer.projectile;

import com.lycanitesmobs.client.renderer.entity.projectile.ProjectileModelRenderer;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class LayerProjectileEffect extends LayerProjectileBase {

    public String textureSuffix;
    public boolean subspecies = true;
    public Vector2f scrollSpeed;


    public LayerProjectileEffect(ProjectileModelRenderer renderer, String textureSuffix) {
        super(renderer);
        this.name = textureSuffix;
        this.textureSuffix = textureSuffix;
    }

    public LayerProjectileEffect(ProjectileModelRenderer renderer, String textureSuffix, boolean glow, int blending, boolean subspecies) {
        super(renderer);
        this.name = textureSuffix;
        this.textureSuffix = textureSuffix;
        this.glow = glow;
        this.blending = blending;
        this.subspecies = subspecies;
    }

    @Override
    public Vector4f getPartColor(String partName, BaseProjectileEntity entity) {
        return new Vector4f(1, 1, 1, 1);
    }

    @Override
    public ResourceLocation getLayerTexture(BaseProjectileEntity entity) {
        return super.getLayerTexture(entity);
    }

    @Override
    public Vector2f getTextureOffset(String partName, BaseProjectileEntity entity, float loop) {
        if (this.scrollSpeed == null) {
            this.scrollSpeed = new Vector2f(0, 0);
        }
        return new Vector2f(loop * this.scrollSpeed.x, loop * this.scrollSpeed.y);
    }

    @Override
    public int getBrightness(String partName, BaseProjectileEntity entity, int brightness) {
        if (this.glow) {
            return 240;
        }
        return brightness;
    }
}
