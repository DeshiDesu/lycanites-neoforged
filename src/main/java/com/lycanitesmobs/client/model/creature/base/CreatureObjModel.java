package com.lycanitesmobs.client.model.creature.base;

import com.google.gson.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.animation.AnimationPart;
import com.lycanitesmobs.client.model.animation.Animator;
import com.lycanitesmobs.client.model.animation.ModelAnimation;
import com.lycanitesmobs.client.model.animation.ModelObjAnimationFrame;
import com.lycanitesmobs.client.obj.model.ObjModel;
import com.lycanitesmobs.client.obj.geometry.ObjPart;
import com.lycanitesmobs.client.obj.model.VBOObjModel;
import com.lycanitesmobs.client.gui.screen.creature.RecolorDebug;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
public class CreatureObjModel extends CreatureModel {
    // Global:
    /**
     * An initial x rotation applied to make Blender models match Minecraft.
     **/
    public static float MODEL_OFFSET_ROT_X = 180F;
    /**
     * An initial y offset applied to make Blender models match Minecraft.
     **/
    public static float MODEL_OFFSET_POS_Y = -1.5F;

    // Model:
    /**
     * An INSTANCE of the model, the model should only be set once and not during every tick or things will get very laggy!
     **/
    public ObjModel objModel;

    /**
     * A list of all parts that belong to this model's wavefront obj.
     **/
    public List<ObjPart> objParts;

    /**
     * A list of all part definitions that this model will use when animating.
     **/
    public Map<String, AnimationPart> animationParts = new HashMap<>();
    public Map<ObjPart, float[][]> colorCache = new HashMap<>();

    // Looking and Head:
    /**
     * Used to scale how far the head part will turn based on the looking X angle.
     **/
    public float lookHeadScaleX = 1;
    /**
     * Used to scale how far the head part will turn based on the looking Y angle.
     **/
    public float lookHeadScaleY = 1;
    /**
     * Used to scale how far the neck part will turn based on the looking X angle.
     **/
    public float lookNeckScaleX = 0;
    /**
     * Used to scale how far the neck part will turn based on the looking Y angle.
     **/
    public float lookNeckScaleY = 0;
    /**
     * Used to scale how far the head part will turn based on the looking X angle.
     **/
    public float lookBodyScaleX = 0;
    /**
     * Used to scale how far the head part will turn based on the looking Y angle.
     **/
    public float lookBodyScaleY = 0;
    /**
     * If true, the head and mouth of this model wont be scaled down when the mob is a child for a bigger head.
     **/
    public boolean bigChildHead = false;

    // Head Model:
    /**
     * For trophies. Used for displaying a body in place of a head/mouth if the model has the head attached to the body part. Set to false if a head/mouth part is added.
     **/
    public boolean bodyIsTrophy = true;
    /**
     * Used for scaling this model when displaying as a trophy.
     **/
    public float trophyScale = 1;
    /**
     * Used for positioning this model when displaying as a trophy. If an empty array, no offset is applied, otherwise it must have at least 3 entries (x, y, z).
     **/
    public float[] trophyOffset = new float[0];
    /**
     * Used for positioning this model's mouth parts when displaying as a trophy. If an empty array, no offset is applied, otherwise it must have at least 3 entries (x, y, z).
     **/
    public float[] trophyMouthOffset = new float[0];

    // Coloring:
    /**
     * If true, no color effects will be applied, this is usually used for when the model is rendered as a red damage overlay, etc.
     **/
    public boolean dontColor = false;

    // Animating:
    /**
     * The animator INSTANCE, this is a helper class that performs actual GL11 functions, etc.
     **/
    public Animator animator;
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
    protected ModInfo modelModInfo;
    protected String modelName;
    protected String modelPath;


    public CreatureObjModel() {
        this(1.0F);
    }

    public CreatureObjModel(float shadowSize) {
        // Here a model should get its model, collect its parts into a list and then create ModelObjPart objects for each part.
    }

    private boolean colorsPrecomputed = false;

    public boolean areColorsPrecomputed() {
        return colorsPrecomputed;
    }

    public void setColorsPrecomputed(boolean precomputed) {
        this.colorsPrecomputed = precomputed;
    }

    public void reloadModel(ResourceManager resourceManager) {
        LMHelperClass.logDebug(
                "Resources",
                "CreatureObjModel.reloadModel: disposing name=" + this.modelName + " path=" + this.modelPath
        );

        if (this.objModel != null) {
            this.objModel.dispose();
            this.objModel = null;
        }

        this.objParts = null;
        this.animationParts.clear();
        this.animation = null;
        this.colorCache.clear();
        this.modelStates.clear();
        this.currentModelState = null;
        this.currentAnimationPart = null;
        this.colorsPrecomputed = false;

        if (this.modelName != null && this.modelModInfo != null && this.modelPath != null) {
            LMHelperClass.logDebug(
                    "Resources",
                    "CreatureObjModel.reloadModel: initModel name=" + this.modelName + " path=" + this.modelPath
            );
            this.initModel(this.modelName, this.modelModInfo, this.modelPath, resourceManager);
        } else {
            LMHelperClass.logDebug(
                    "Resources",
                    "CreatureObjModel.reloadModel: missing metadata for " + this.getClass().getName()
            );
        }
    }


    public CreatureObjModel initModel(String name, ModInfo modInfo, String path) {
        return initModel(name, modInfo, path, Minecraft.getInstance().getResourceManager());
    }

    public CreatureObjModel initModel(String name, ModInfo modInfo, String path, ResourceManager resourceManager) {
        this.modelName = name;
        this.modelModInfo = modInfo;
        this.modelPath = path;

        this.objModel = new VBOObjModel(
                new ResourceLocation(modInfo.modid, "modelparts/" + path + ".obj"),
                resourceManager
        );
        this.objParts = this.objModel.objParts;
        if (this.objParts.isEmpty())
            LMHelperClass.logWarning("", "Unable to load model obj for: " + name + "");

        this.animator = new Animator(this);

        ResourceLocation modelPartsLocation = new ResourceLocation(modInfo.modid, "modelparts/" + path + "_parts.json");
        try {
            Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
            InputStream in = resourceManager.getResource(modelPartsLocation).get().open();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            try {
                JsonArray jsonArray = GsonHelper.fromJson(gson, reader, JsonArray.class, false);
                Iterator<JsonElement> jsonIterator = jsonArray.iterator();
                while (jsonIterator.hasNext()) {
                    JsonObject partJson = jsonIterator.next().getAsJsonObject();
                    AnimationPart animationPart = new AnimationPart();
                    animationPart.loadFromJson(partJson);
                    this.addAnimationPart(animationPart);
                }
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } catch (Exception e) {
            LMHelperClass.logWarning("", "Unable to load model parts json for: " + name + "");
        }

        for (AnimationPart part : this.animationParts.values()) {
            part.addChildren(this.animationParts.values().toArray(new AnimationPart[this.animationParts.size()]));
        }

        ResourceLocation animationLocation = new ResourceLocation(modInfo.modid, "modelparts/" + path + "_animation.json");
        try {
            Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
            InputStream in = resourceManager.getResource(animationLocation).get().open();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            try {
                JsonObject json = GsonHelper.fromJson(gson, reader, JsonObject.class, false);
                this.animation = new ModelAnimation();
                this.animation.loadFromJson(json);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } catch (Exception e) {
            LMHelperClass.logWarning("Models", "Unable to load animation json for: " + name + ".");
        }

        return this;
    }


    /**
     * Adds an animation part, these are used to animate each model part.
     *
     * @param animationPart The animation part to add.
     */
    public void addAnimationPart(AnimationPart animationPart) {
        if (this.animationParts.containsKey(animationPart.name)) {
            //LycanitesMobs.logWarning("", "Tried to add an animation part that already exists: " + animationPart.name + ".");
            return;
        }
        if (animationPart.parentName != null) {
            if (animationPart.parentName.equals(animationPart.name))
                animationPart.parentName = null;
        }
        this.animationParts.put(animationPart.name, animationPart);
    }

    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        if (this.animation != null) {
            this.animation.addCreatureLayers(renderer);
        }
    }

    @Override
    public void generateAnimationFrames(BaseCreatureEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness) {
        boolean renderAsTrophy = false;
        this.young = false;
        if (entity != null) {
            this.young = entity.isBaby();
        }
        if (scale < 0) {
            renderAsTrophy = true;
            scale = -scale;
        } else {
            if (entity != null) {
                scale *= entity.getScale();
            }
        }

        if (entity != null) {
            if (entity.onlyRenderTicks >= 0) {
                loop = entity.onlyRenderTicks;
            }
        }

        this.currentModelState = this.getModelState(entity);
        this.updateAttackProgress(entity);


        if (this.currentModelState != null) {
            RecolorDebug.applyToState(this.currentModelState, entity);

            if (!this.currentModelState.variantInit) {
        /*
        this.currentModelState.baseR = 1.0F;
        this.currentModelState.baseG = 0.0F;
        this.currentModelState.baseB = 0.0F;
        this.currentModelState.baseRange = 0.35F;

        this.currentModelState.variantR = 0.0F;
        this.currentModelState.variantG = 0.0F;
        this.currentModelState.variantB = 1.0F;
        this.currentModelState.variantAmount = 1.0F;

        this.currentModelState.variantInit = true;
        */
            }
        }


        if (entity != null && entity.hasPerchTarget()) {
            distance = 0;
        }

        for (ObjPart part : this.objParts) {
            String partName = part.getName().toLowerCase();
            this.currentAnimationPart = this.animationParts.get(partName);
            if (this.currentAnimationPart == null)
                continue;

            this.animatePart(partName, entity, time, distance, loop, -lookY, lookX, scale);

            if (renderAsTrophy) {
                if (partName.contains("head")) {
                    if (!partName.contains("left")) {
                        this.translate(-0.3F, 0, 0);
                        this.angle(5F, 0, 1, 0);
                    }
                    if (!partName.contains("right")) {
                        this.translate(0.3F, 0, 0);
                        this.angle(-5F, 0, 1, 0);
                    }
                }
                if (this.trophyOffset.length >= 3)
                    this.translate(this.trophyOffset[0], this.trophyOffset[1], this.trophyOffset[2]);
            }
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
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        float rotX = 0F;
        float rotY = 0F;
        float rotZ = 0F;

        // Looking:
        if (partName.equals("head")) {
            rotX += (Math.toDegrees(lookX / (180F / (float) Math.PI)) * this.lookHeadScaleX);
            rotY += (Math.toDegrees(lookY / (180F / (float) Math.PI))) * this.lookHeadScaleY;
        }
        if (partName.contains("neck")) {
            rotX += (Math.toDegrees(lookX / (180F / (float) Math.PI)) * this.lookNeckScaleX);
            rotY += (Math.toDegrees(lookY / (180F / (float) Math.PI))) * this.lookNeckScaleY;
        }

        // Create Animation Frames:
        this.rotate(rotX, rotY, rotZ);
    }

    @Override
    public void clearAnimationFrames() {
        for (AnimationPart animationPart : this.animationParts.values()) {
            animationPart.animationFrames.clear();
        }
    }

    @Override
    public void render(BaseCreatureEntity entity, PoseStack matrixStack, VertexConsumer vertexBuilder, LayerCreatureBase layer, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness, int fade) {
        this.matrixStack = matrixStack;

        boolean renderAsTrophy = false;
        this.young = false;
        if (entity != null) {
            this.young = entity.isBaby();
        }
        if (scale < 0) {
            renderAsTrophy = true;
            scale = -scale;
        } else {
            if (entity != null) {
                scale *= entity.getScale();
            }
        }

        if (this.objModel instanceof VBOObjModel vbo && this.currentModelState != null) {
            vbo.applyVariantFromState(this.currentModelState);
        }

        for (ObjPart part : this.objParts) {
            String partName = part.getName().toLowerCase();
            if (!this.canRenderPart(partName, entity, layer, renderAsTrophy))
                continue;

            this.currentAnimationPart = this.animationParts.get(partName);
            if (this.currentAnimationPart == null) {
                continue;
            }

            matrixStack.pushPose();
            this.doAngle(MODEL_OFFSET_ROT_X, 1F, 0F, 0F);
            this.doTranslate(0F, MODEL_OFFSET_POS_Y, 0F);

            if (this.young && !renderAsTrophy) {
                this.childScale(partName);
                if (this.bigChildHead && (partName.contains("head") || partName.contains("mouth")))
                    this.doTranslate(-(this.currentAnimationPart.centerX / 2), -(this.currentAnimationPart.centerY / 2), -(this.currentAnimationPart.centerZ / 2));
            }

            if (renderAsTrophy)
                this.doScale(this.trophyScale, this.trophyScale, this.trophyScale);

            this.doScale(scale, scale, scale);

            this.currentAnimationPart.applyAnimationFrames(this.animator);

            if (this.objModel.getCreature() == null) {
                this.objModel.setCreature(entity);
            }

            this.objModel.renderPart(
                    vertexBuilder,
                    matrixStack.last().normal(),
                    matrixStack.last().pose(),
                    this.getBrightness(partName, layer, entity, brightness),
                    fade,
                    part,
                    this.getPartColor(partName, entity, layer, renderAsTrophy, loop),
                    this.getPartTextureOffset(partName, entity, layer, renderAsTrophy, loop)
            );
            matrixStack.popPose();
        }
    }


    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if (partName == null)
            return false;
        partName = partName.toLowerCase();

        // Check Animation Part:
        if (!this.animationParts.containsKey(partName))
            return false;

        // Check Trophy:
        if (trophy && !this.isTrophyPart(partName))
            return false;

        return super.canRenderPart(partName, entity, layer, trophy);
    }

    /**
     * Returns true if the provided part name should be shown for the trophy model.
     **/
    public boolean isTrophyPart(String partName) {
        if (partName == null)
            return false;
        partName = partName.toLowerCase();
        if (partName.contains("head") || partName.contains("mouth") || partName.contains("eye"))
            return true;
        if (this.bodyIsTrophy && partName.contains("body"))
            return true;
        return false;
    }

    /**
     * Returns an existing or new model state for the given entity.
     **/
    public ModelObjState getModelState(Entity entity) {
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

    public void childScale(String partName) {
        if (this.bigChildHead && (partName.contains("head") || partName.contains("mouth")))
            return;
        this.animator.doScale(0.5F, 0.5F, 0.5F);
    }

    public void updateAttackProgress(Entity entity) {
        if (this.currentModelState == null || !(entity instanceof BaseCreatureEntity))
            return;
        BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;

        if (this.currentModelState.attackAnimationPlaying) {
            if (this.currentModelState.attackAnimationIncreasing) {
                this.currentModelState.attackAnimationProgress = Math.min(this.currentModelState.attackAnimationProgress + this.currentModelState.attackAnimationSpeed, 1F);
                if (this.currentModelState.attackAnimationProgress >= 1)
                    this.currentModelState.attackAnimationIncreasing = false;
            } else {
                this.currentModelState.attackAnimationProgress = Math.max(this.currentModelState.attackAnimationProgress - this.currentModelState.attackAnimationSpeed, 0F);
                if (this.currentModelState.attackAnimationProgress <= 0) {
                    this.currentModelState.attackAnimationPlaying = false;
                }
            }
        } else if (entityCreature.isAttackOnCooldown()) {
            this.currentModelState.attackAnimationPlaying = true;
            this.currentModelState.attackAnimationIncreasing = true;
            this.currentModelState.attackAnimationProgress = 0;
        }
    }

    public float getAttackProgress() {
        if (this.currentModelState == null)
            return 0;
        return this.currentModelState.attackAnimationProgress;
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
}
