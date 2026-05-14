package com.lycanitesmobs.client.model.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.item.IItemModelRenderer;
import com.lycanitesmobs.client.renderer.entity.projectile.ProjectileModelRenderer;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import com.lycanitesmobs.client.renderer.layer.projectile.LayerProjectileBase;

import java.util.*;

public class ModelAnimation {

    /**
     * A list of model texture layer definitions.
     **/
    public Map<String, TextureLayerAnimation> textureLayers = new HashMap<>();

    /**
     * The base item layer to use.
     **/
    LayerCreatureBase baseCreatureLayer;

    /**
     * The base item layer to use.
     **/
    LayerProjectileBase baseProjectileLayer;

    /**
     * The base item layer to use.
     **/
    LayerItem baseItemLayer;

    /**
     * A list of part animations.
     **/
    public List<ModelPartAnimation> partAnimations = new ArrayList<>();


    /**
     * Reads JSON data into this ObjPart.
     *
     * @param json The JSON data to read from.
     */
    public void loadFromJson(JsonObject json) {
        // Texture Layers:
        if (json.has("textureLayers")) {
            Iterator<JsonElement> textureLayers = json.get("textureLayers").getAsJsonArray().iterator();
            while (textureLayers.hasNext()) {
                JsonObject jsonObject = textureLayers.next().getAsJsonObject();
                TextureLayerAnimation animationLayer = new TextureLayerAnimation();
                animationLayer.loadFromJson(jsonObject);
                this.textureLayers.put(animationLayer.name, animationLayer);
            }
        }

        // Part Animations:
        if (json.has("partAnimations")) {
            Iterator<JsonElement> partAnimations = json.get("partAnimations").getAsJsonArray().iterator();
            while (partAnimations.hasNext()) {
                JsonObject jsonObject = partAnimations.next().getAsJsonObject();
                ModelPartAnimation partAnimation = new ModelPartAnimation();
                partAnimation.loadFromJson(jsonObject);
                this.partAnimations.add(partAnimation);
            }
        }
    }


    /**
     * Adds creature layers from this Animation to the provided renderer.
     *
     * @param renderer The renderer to add the layers to.
     */
    public void addCreatureLayers(CreatureRenderer renderer) {
        for (TextureLayerAnimation textureLayer : this.textureLayers.values()) {
            if (textureLayer.name.equals("base")) {
                this.baseCreatureLayer = textureLayer.createCreatureLayer(renderer);
                continue;
            }
            renderer.addLayer(textureLayer.createCreatureLayer(renderer));
        }
    }


    /**
     * Adds projectile layers from this Animation to the provided renderer.
     *
     * @param renderer The renderer to add the layers to.
     */
    public void addProjectileLayers(ProjectileModelRenderer renderer) {
        for (TextureLayerAnimation textureLayer : this.textureLayers.values()) {
            if (textureLayer.name.equals("base")) {
                this.baseProjectileLayer = textureLayer.createProjectileLayer(renderer);
                continue;
            }
            renderer.addLayer(textureLayer.createProjectileLayer(renderer));
        }
    }


    /**
     * Adds item layers from this Animation to the provided renderer.
     *
     * @param renderer The renderer to add the layers to.
     */
    public void addItemLayers(IItemModelRenderer renderer) {
        for (TextureLayerAnimation textureLayer : this.textureLayers.values()) {
            if (textureLayer.name.equals("base")) {
                this.baseItemLayer = textureLayer.createItemLayer(renderer);
                continue;
            }
            renderer.addLayer(textureLayer.createItemLayer(renderer));
        }
    }


    /**
     * Returns a layer to use for the base texture or null if none is provided.
     *
     * @return Null or a base layer.
     */
    public LayerCreatureBase getBaseCreatureLayer() {
        return this.baseCreatureLayer;
    }


    /**
     * Returns a layer to use for the base texture or null if none is provided.
     *
     * @return Null or a base layer.
     */
    public LayerProjectileBase getBaseProjectileLayer() {
        return this.baseProjectileLayer;
    }


    /**
     * Returns a layer to use for the base texture or null if none is provided.
     *
     * @return Null or a base layer.
     */
    public LayerItem getBaseItemLayer() {
        return this.baseItemLayer;
    }
}
