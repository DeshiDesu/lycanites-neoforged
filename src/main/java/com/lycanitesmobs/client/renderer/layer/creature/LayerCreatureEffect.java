package com.lycanitesmobs.client.renderer.layer.creature;

import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class LayerCreatureEffect extends LayerCreatureBase {
	public String textureSuffix;
	public boolean subspecies = true;
	public Vector2f scrollSpeed;

    public LayerCreatureEffect(CreatureRenderer renderer, String textureSuffix) {
        super(renderer);
        this.name = textureSuffix;
        this.textureSuffix = textureSuffix;
    }

	public LayerCreatureEffect(CreatureRenderer renderer, String textureSuffix, boolean glow, int blending, boolean subspecies) {
		super(renderer);
		this.name = textureSuffix;
		this.textureSuffix = textureSuffix;
		this.glow = glow;
		this.blending = blending;
		this.subspecies = subspecies;
	}

	public LayerCreatureEffect(CreatureRenderer renderer, String name, String textureSuffix, boolean glow, int blending, boolean subspecies) {
		super(renderer);
		this.name = name;
		this.textureSuffix = textureSuffix;
		this.glow = glow;
		this.blending = blending;
		this.subspecies = subspecies;
	}

    @Override
    public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
        return new Vector4f(1, 1, 1, 1);
    }

    @Override
    public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
    	if (this.subspecies) {
			return entity.getTexture(this.textureSuffix);
		}
    	return entity.getSubTexture(this.textureSuffix);
    }

	@Override
	public Vector2f getTextureOffset(String partName, BaseCreatureEntity entity, boolean trophy, float loop) {
    	if(this.scrollSpeed == null) {
			this.scrollSpeed = new Vector2f(0, 0);
		}
		return new Vector2f(loop * this.scrollSpeed.x, loop * this.scrollSpeed.y);
	}

	@Override
	public int getBrightness(String partName, BaseCreatureEntity entity, int brightness) {
    	if(this.glow) {
    		return 240;
		}
		return brightness;
	}
}
