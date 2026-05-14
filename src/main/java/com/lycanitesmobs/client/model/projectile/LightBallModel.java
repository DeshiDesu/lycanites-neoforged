package com.lycanitesmobs.client.model.projectile;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.projectile.base.ProjectileObjModel;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.entity.projectile.ProjectileModelRenderer;
import com.lycanitesmobs.client.renderer.layer.projectile.LayerProjectileBase;
import com.lycanitesmobs.client.renderer.layer.projectile.LayerProjectileEffect;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class LightBallModel extends ProjectileObjModel {
    LayerProjectileBase ballGlowLayer;

    public LightBallModel() {
        this(1.0F);
    }

    public LightBallModel(float shadowSize) {

        // Load Model:
        this.initModel("lightball", LycanitesMobs.modInfo, "projectile/lightball");
    }

    @Override
    public void addCustomLayers(ProjectileModelRenderer renderer) {
        super.addCustomLayers(renderer);
        this.ballGlowLayer = new LayerProjectileEffect(renderer, "", true, CustomRenderStates.BLEND.ADD.id, true);
        renderer.addLayer(this.ballGlowLayer);
    }

    @Override
    public void animatePart(String partName, BaseProjectileEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
        this.rotate(loop * 8, 0, 0);
    }

    @Override
    public boolean canRenderPart(String partName, BaseProjectileEntity entity, LayerProjectileBase layer) {
        if (partName.equals("ball02") || partName.equals("ball03")) {
            return layer == this.ballGlowLayer;
        }
        return layer == null;
    }

    /**
     * Returns the coloring to be used for this part and layer.
     **/
    @Override
    public Vector4f getPartColor(String partName, BaseProjectileEntity entity, LayerProjectileBase layer, float loop) {
        float glowSpeed = 40;
        float glow = loop * glowSpeed % 360;
        float color = ((float) Math.cos(Math.toRadians(glow)) * 0.1f) + 0.9f;
        return new Vector4f(color, color, color, 1);
    }

    @Override
    public int getBrightness(String partName, LayerProjectileBase layer, BaseProjectileEntity entity, int brightness) {
        return ClientManager.FULL_BRIGHT;
    }
}
