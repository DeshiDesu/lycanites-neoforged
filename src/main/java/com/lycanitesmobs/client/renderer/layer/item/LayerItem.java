package com.lycanitesmobs.client.renderer.layer.item;

import com.lycanitesmobs.client.renderer.item.IItemModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class LayerItem {
    public IItemModelRenderer renderer;
    public String name;

    public String textureSuffix;

    public boolean glow = false;

    public enum BLEND {
        NORMAL(0), ADD(1), SUB(2);
        public final int id;

        BLEND(int value) {
            this.id = value;
        }

        public int getValue() {
            return id;
        }
    }

    public int blending = 0;

    public Vector2f scrollSpeed;

    public Vector4f colorFadeSpeed;

    public List<String> targetParts = new ArrayList<>();


    /**
     * Constructor
     *
     * @param renderer The renderer that is rendering this layer.
     */
    public LayerItem(IItemModelRenderer renderer, String name) {
        this.renderer = renderer;
        this.name = name;
    }

    /**
     * Returns the color that this layer should render the provided part at.
     *
     * @param partName  The name of the model part.
     * @param itemStack The item stack to render.
     * @param loop      The animation loop to use.
     * @return The part color.
     */
    public Vector4f getPartColor(String partName, ItemStack itemStack, float loop) {
        if (this.colorFadeSpeed != null) {
            float red = 1;
            if (this.colorFadeSpeed.x() != 0)
                red = (loop % this.colorFadeSpeed.x() / this.colorFadeSpeed.x()) * 2;
            if (red > 1)
                red = -(red - 1);

            float green = 1;
            if (this.colorFadeSpeed.y() != 0)
                green = (loop % this.colorFadeSpeed.y() / this.colorFadeSpeed.y()) * 2;
            if (green > 1)
                green = -(green - 1);

            float blue = 1;
            if (this.colorFadeSpeed.z() != 0)
                blue = (loop % this.colorFadeSpeed.z() / this.colorFadeSpeed.z()) * 2;
            if (blue > 1)
                blue = -(blue - 1);

            float alpha = 1;
            if (this.colorFadeSpeed.w() != 0)
                alpha = (loop % this.colorFadeSpeed.w() / this.colorFadeSpeed.w()) * 2;
            if (alpha > 1)
                alpha = -(alpha - 1);

            return new Vector4f(red, green, blue, alpha);
        }

        return new Vector4f(1, 1, 1, 1);
    }

    public ResourceLocation getLayerTexture(ItemStack itemStack) {
        return null;
    }

    public Vector2f getTextureOffset(String partName, ItemStack itemStack, float loop) {
        if (this.scrollSpeed == null) {
            this.scrollSpeed = new Vector2f(0, 0);
        }
        return new Vector2f(loop * this.scrollSpeed.x, loop * this.scrollSpeed.y);
    }

    /**
     * Returns the brightness that this layer should use.
     *
     * @param partName   The name of the model part.
     * @param itemStack  The item stack to render.
     * @param brightness The base brightness.
     * @return The brightness to render at.
     */
    public int getBrightness(String partName, ItemStack itemStack, int brightness) {
        if (this.glow) {
            return 240;
        }
        return brightness;
    }

    /**
     * Returns the blending type that this layer should use, see CustomRenderStates.BLEND.
     *
     * @param itemStack The item stack to render.
     * @return The part blending type.
     */
    public int getBlending(ItemStack itemStack) {
        return this.blending;
    }

    /**
     * Returns if this layer should glow where it ignores shading.
     *
     * @param itemStack The item stack to render.
     * @return True for glowing (shadeless rendering).
     */
    public boolean getGlow(ItemStack itemStack) {
        return this.glow;
    }
}
