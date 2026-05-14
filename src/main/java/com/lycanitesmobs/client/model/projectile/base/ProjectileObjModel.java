package com.lycanitesmobs.client.model.projectile.base;

import com.google.gson.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.ModelObjState;
import com.lycanitesmobs.client.model.animation.AnimationPart;
import com.lycanitesmobs.client.model.animation.Animator;
import com.lycanitesmobs.client.model.animation.ModelAnimation;
import com.lycanitesmobs.client.model.animation.ModelObjAnimationFrame;
import com.lycanitesmobs.client.obj.model.ObjModel;
import com.lycanitesmobs.client.obj.geometry.ObjPart;
import com.lycanitesmobs.client.obj.model.VBOObjModel;
import com.lycanitesmobs.client.renderer.entity.projectile.ProjectileModelRenderer;
import com.lycanitesmobs.client.renderer.layer.projectile.LayerProjectileBase;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ProjectileObjModel extends ProjectileModel {
    // Global:
    /**
     * An initial x rotation applied to make Blender models match Minecraft.
     **/
    public static float modelXRotOffset = 180F;
    /**
     * An initial y offset applied to make Blender models match Minecraft.
     **/
    public static float modelYPosOffset = -1.5F;

    // Model:
    /**
     * An INSTANCE of the model, the model should only be set once and not during every tick or things will get very laggy!
     **/
    public ObjModel wavefrontObject;

    /**
     * A list of all parts that belong to this model's wavefront obj.
     **/
    public List<ObjPart> objParts;

    /**
     * A list of all part definitions that this model will use when animating.
     **/
    public Map<String, AnimationPart> animationParts = new HashMap<>();

    // Animating:
    /**
     * The animator INSTANCE, this is a helper class that performs actual GL11 functions, etc.
     **/
    protected Animator animator;
    /**
     * The current animation part that is having an animation frame generated for.
     **/
    protected AnimationPart currentAnimationPart;
    /**
     * The animation data for this model.
     **/
    protected ModelAnimation animation;
    /**
     * A list of models states that hold unique render/animation data for a specific entity INSTANCE.
     **/
    protected Map<Entity, ModelObjState> modelStates = new HashMap<>();
    /**
     * The current model state for the entity that is being animated and rendered.
     **/
    protected ModelObjState currentModelState;
    protected String modelName;
    protected ModInfo modelModInfo;
    protected String modelPath;


    public ProjectileObjModel() {
        this(1.0F);
    }

    public ProjectileObjModel(float shadowSize) {
        // Here a model should get its model, collect its parts into a list and then create ModelObjPart objects for each part.
    }

    /**
     * Initializes this model, loading model data, etc.
     *
     * @param name    The unique name this model should have.
     * @param modInfo The mod this model belongs to.
     * @param path    The path to load the model data from (no extension).
     * @return This model instance.
     */
    public ProjectileObjModel initModel(String name, ModInfo modInfo, String path) {
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(name);
        if (projectileInfo != null && !projectileInfo.enabled) {
            return this;
        }

        this.modelName = name;
        this.modelModInfo = modInfo;
        this.modelPath = path;

        this.reloadModel(Minecraft.getInstance().getResourceManager());

        return this;
    }

    public void reloadModel(ResourceManager resourceManager) {
        if (this.modelModInfo == null || this.modelPath == null) {
            return;
        }

        this.wavefrontObject = new VBOObjModel(new ResourceLocation(this.modelModInfo.modid, "modelparts/" + this.modelPath + ".obj"));
        this.objParts = this.wavefrontObject.objParts;
        if (this.objParts.isEmpty()) {
            LMHelperClass.logWarning("Resources", "Unable to load any parts for the " + this.modelName + " model!");
        }

        this.animationParts.clear();
        this.animator = new Animator(this);

        ResourceLocation modelPartsLocation = new ResourceLocation(this.modelModInfo.modid, "modelparts/" + this.modelPath + "_parts.json");
        try {
            Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
            InputStream in = resourceManager.getResource(modelPartsLocation).get().open();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            try {
                JsonArray jsonArray = GsonHelper.fromJson(gson, reader, JsonArray.class, false);
                if (jsonArray != null) {
                    Iterator<JsonElement> jsonIterator = jsonArray.iterator();
                    while (jsonIterator.hasNext()) {
                        JsonObject partJson = jsonIterator.next().getAsJsonObject();
                        AnimationPart animationPart = new AnimationPart();
                        animationPart.loadFromJson(partJson);
                        this.addAnimationPart(animationPart);
                    }
                }
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } catch (Exception e) {
            LMHelperClass.logWarning("Resources", "There was a problem loading animation parts for " + this.modelName + ": " + e);
        }

        for (AnimationPart part : this.animationParts.values()) {
            part.addChildren(this.animationParts.values().toArray(new AnimationPart[this.animationParts.size()]));
        }

        ResourceLocation animationLocation = new ResourceLocation(this.modelModInfo.modid, "modelparts/" + this.modelPath + "_animation.json");
        try {
            Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
            InputStream in = resourceManager.getResource(animationLocation).get().open();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            try {
                JsonObject json = GsonHelper.fromJson(gson, reader, JsonObject.class, false);
                if (json != null) {
                    this.animation = new ModelAnimation();
                    this.animation.loadFromJson(json);
                }
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } catch (Exception e) {
            LMHelperClass.logWarning("Models", "Unable to load animation json for " + this.modelName + ".");
        }
    }

    /**
     * Adds an animation part, these are used to animate each model part.
     *
     * @param animationPart The animation part to add.
     */
    public void addAnimationPart(AnimationPart animationPart) {
        if (this.animationParts.containsKey(animationPart.name)) {
            LMHelperClass.logWarning("", "Tried to add an animation part that already exists: " + animationPart.name + ".");
            return;
        }
        if (animationPart.parentName != null) {
            if (animationPart.parentName.equals(animationPart.name))
                animationPart.parentName = null;
        }
        this.animationParts.put(animationPart.name, animationPart);
    }

    @Override
    public void addCustomLayers(ProjectileModelRenderer renderer) {
        super.addCustomLayers(renderer);
        if (this.animation != null) {
            this.animation.addProjectileLayers(renderer);
        }
    }

    /**
     * Generates all animation frames for a render tick.
     **/
    @Override
    public void generateAnimationFrames(BaseProjectileEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness) {
        for (ObjPart part : this.objParts) {
            String partName = part.getName().toLowerCase();
            //if(!this.canRenderPart(partName, entity, layer, renderAsTrophy))
            //continue;
            this.currentAnimationPart = this.animationParts.get(partName);
            if (this.currentAnimationPart == null)
                continue;

            // Animate:
            this.animatePart(partName, entity, time, distance, loop, -lookY, lookX, scale);
        }
    }

    /**
     * Animates the individual part.
     *
     * @param partName The name of the part (should be made all lowercase).
     * @param entity   Can't be null but can be any entity. If the mob's exact entity or an EntityCreatureBase is used more animations will be used.
     * @param time     How long the model has been displayed for? This is currently unused.
     * @param distance Used for movement animations, this should just count up from 0 every tick and stop back at 0 when not moving.
     * @param loop     A continuous loop counting every tick, used for constant idle animations, etc.
     * @param lookY    A y looking rotation used by the head, etc.
     * @param lookX    An x looking rotation used by the head, etc.
     * @param scale    Used for scale based changes during animation but not to actually apply the scale as it is applied in the renderer method.
     */
    public void animatePart(String partName, BaseProjectileEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        float rotX = 0F;
        float rotY = 0F;
        float rotZ = 0F;

        // Create Animation Frames:
        this.rotate(rotX, rotY, rotZ);
    }

    /**
     * Clears all animation frames that were generated for a render tick.
     **/
    @Override
    public void clearAnimationFrames() {
        for (AnimationPart animationPart : this.animationParts.values()) {
            animationPart.animationFrames.clear();
        }
    }

    @Override
    public void render(BaseProjectileEntity entity, PoseStack matrixStack, VertexConsumer vertexBuilder, LayerProjectileBase layer, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness) {
        this.matrixStack = matrixStack;

        // Assess Scale:
        if (scale < 0) {
            scale = -scale;
        } else {
            if (entity != null) {
                scale *= 4;
                scale *= entity.getProjectileScale();
            }
        }

        // Animation States:
        this.currentModelState = this.getModelState(entity);

        // Render Parts:
        for (ObjPart part : this.objParts) {
            String partName = part.getName().toLowerCase();
            if (!this.canRenderPart(partName, entity, layer))
                continue;
            this.currentAnimationPart = this.animationParts.get(partName);
            if (this.currentAnimationPart == null) {
                continue;
            }

            // Begin Rendering Part:
            matrixStack.pushPose();

            // Apply Initial Offsets: (To Match Blender OBJ Export)
            this.doAngle(modelXRotOffset, 1F, 0F, 0F);
            this.doTranslate(0F, modelYPosOffset, 0F);

            // Apply Entity Scaling:
            this.doScale(scale, scale, scale);

            // Apply Animation Frames:
            this.currentAnimationPart.applyAnimationFrames(this.animator);

            // Render Part:
            this.wavefrontObject.renderPart(vertexBuilder, matrixStack.last().normal(), matrixStack.last().pose(), this.getBrightness(partName, layer, entity, brightness), 0, part, this.getPartColor(partName, entity, layer, loop), this.getPartTextureOffset(partName, entity, layer, loop));
            matrixStack.popPose();
        }
    }

    /**
     * Returns true if the part can be rendered, this can do various checks such as Yale wool only rendering in the YaleWoolLayer or hiding body parts in place of armor parts, etc.
     **/
    @Override
    public boolean canRenderPart(String partName, BaseProjectileEntity entity, LayerProjectileBase layer) {
        if (partName == null)
            return false;
        partName = partName.toLowerCase();

        // Check Animation Part:
        if (!this.animationParts.containsKey(partName))
            return false;

        return super.canRenderPart(partName, entity, layer);
    }

    /**
     * Moves the animation origin to a different part origin.
     *
     * @param fromPartName The part name to move the origin from.
     * @param toPartName   The part name to move the origin to.
     */
    public void shiftOrigin(String fromPartName, String toPartName) {
        AnimationPart fromPart = this.animationParts.get(fromPartName);
        AnimationPart toPart = this.animationParts.get(toPartName);
        float offsetX = toPart.centerX - fromPart.centerX;
        float offsetY = toPart.centerY - fromPart.centerY;
        float offsetZ = toPart.centerZ - fromPart.centerZ;
        this.translate(offsetX, offsetY, offsetZ);
    }

    /**
     * Moves the animation origin back from a different part origin.
     *
     * @param fromPartName The part name that the origin moved from.
     * @param toPartName   The part name that the origin was moved to.
     */
    public void shiftOriginBack(String fromPartName, String toPartName) {
        AnimationPart fromPart = this.animationParts.get(fromPartName);
        AnimationPart toPart = this.animationParts.get(toPartName);
        float offsetX = toPart.centerX - fromPart.centerX;
        float offsetY = toPart.centerY - fromPart.centerY;
        float offsetZ = toPart.centerZ - fromPart.centerZ;
        this.translate(-offsetX, -offsetY, -offsetZ);
    }

    /**
     * Returns an existing or new model state for the given entity.
     **/
    public ModelObjState getModelState(BaseProjectileEntity entity) {
        if (entity == null)
            return null;
        if (this.modelStates.containsKey(entity)) {
            if (!entity.isAlive()) {
                this.modelStates.remove(entity);
                return null;
            }
            return this.modelStates.get(entity);
        }
        ModelObjState modelState = new ModelObjState(entity);
        this.modelStates.put(entity, modelState);
        return modelState;
    }


    @Override
    public void angle(float rotation, float angleX, float angleY, float angleZ) {
        this.currentAnimationPart.addAnimationFrame(new ModelObjAnimationFrame("angle", rotation, angleX, angleY, angleZ));
    }

    @Override
    public void rotate(float rotX, float rotY, float rotZ) {
        this.currentAnimationPart.addAnimationFrame(new ModelObjAnimationFrame("rotate", 1, rotX, rotY, rotZ));
    }

    @Override
    public void translate(float posX, float posY, float posZ) {
        this.currentAnimationPart.addAnimationFrame(new ModelObjAnimationFrame("translate", 1, posX, posY, posZ));
    }

    @Override
    public void scale(float scaleX, float scaleY, float scaleZ) {
        this.currentAnimationPart.addAnimationFrame(new ModelObjAnimationFrame("scale", 1, scaleX, scaleY, scaleZ));
    }
}
