package com.lycanitesmobs.client.model.item;

import com.google.gson.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.ModelObjState;
import com.lycanitesmobs.client.model.animation.*;
import com.lycanitesmobs.client.obj.model.ObjModel;
import com.lycanitesmobs.client.obj.geometry.ObjPart;
import com.lycanitesmobs.client.obj.model.VBOObjModel;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.item.IItemModelRenderer;
import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.io.IOUtils;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class ItemObjModel implements IAnimationModel {

    // Global:
    /**
     * An initial x rotation applied to make Blender models match Minecraft.
     * Value is ~233.24° because all equipment part display transforms (GUI, first-person, third-person)
     * were fine-tuned when doAngle was broken (passing 180 raw radians to AxisAngle4f instead of
     * converting degrees to radians). The old effective rotation was 180 rad mod 2π ≈ 4.071 rad ≈ 233.24°.
     * Keeping this value preserves all fine-tuned EquipmentPartRenderer/EquipmentRenderer offsets.
     **/
    public static float ROT_OFFSET_X = 233.24F;
    /**
     * An initial y offset applied to make Blender models match Minecraft.
     **/
    public static float POS_OFFSET_Y = -1.5F;

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

    // Animating:
    public PoseStack matrixStack;
    /**
     * The animator INSTANCE, this is a helper class that performs actual GL11 functions, etc.
     **/
    protected Animator animator;
    /**
     * The animation data for this model.
     **/
    protected ModelAnimation animation;
    /**
     * The current animation part that is having an animation frame generated for.
     **/
    protected AnimationPart currentAnimationPart;
    /**
     * A list of models states that hold unique render/animation data for a specific itemstack INSTANCE.
     **/
    protected Map<ItemStack, ModelObjState> modelStates = new HashMap<>();
    /**
     * The current model state for the entity that is being animated and rendered.
     **/
    protected ModelObjState currentModelState;

    /**
     * Constructor
     *
     * @param name The unique model name.
     * @param path The path to find the model obj in such as equipment/darklingskull (obj is already appended).
     * @return
     */
    public ItemObjModel initModel(String name, String path) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        return initModel(name, path, resourceManager);
    }

    protected String modelName;
    protected String modelPath;

    public ItemObjModel initModel(String name, String path, ResourceManager resourceManager) {
        this.modelName = name;
        this.modelPath = path;

        this.objModel = new VBOObjModel(new ResourceLocation(LycanitesMobs.MODID, "modelparts/" + path + ".obj"), resourceManager);
        this.objParts = this.objModel.objParts;
        if (this.objParts.isEmpty())
            LMHelperClass.logWarning("", "Unable to load any parts for the " + name + " model!");

        this.animator = new Animator(this);

        this.animationParts.clear();
        ResourceLocation animPartsLoc = new ResourceLocation(LycanitesMobs.MODID, "modelparts/" + path + "_parts.json");
        try {
            Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
            InputStream in = resourceManager.getResource(animPartsLoc).get().open();
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
            LMHelperClass.logWarning("", "There was a problem loading animation parts for " + name + ":");
            e.toString();
        }

        for (AnimationPart part : this.animationParts.values()) {
            part.addChildren(this.animationParts.values().toArray(new AnimationPart[this.animationParts.size()]));
        }

        ResourceLocation animationLocation = new ResourceLocation(LycanitesMobs.MODID, "modelparts/" + path + "_animation.json");
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
            LMHelperClass.logWarning("Model", "Unable to load animation json for " + name + ".");
        }

        return this;
    }

    public void reloadModel(ResourceManager resourceManager) {
        LMHelperClass.logDebug("Resources", "ItemObjModel.reloadModel: disposing name=" + this.modelName + " path=" + this.modelPath);

        if (this.objModel != null) {
            this.objModel.dispose();
            this.objModel = null;
        }

        this.objParts = null;
        this.animationParts.clear();
        this.animation = null;
        this.animator = null;
        this.currentAnimationPart = null;

        if (this.modelName != null && this.modelPath != null) {
            LMHelperClass.logDebug("Resources", "ItemObjModel.reloadModel: initModel name=" + this.modelName + " path=" + this.modelPath);
            this.initModel(this.modelName, this.modelPath, resourceManager);
        } else {
            LMHelperClass.logDebug("Resources", "ItemObjModel.reloadModel: missing metadata");
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

    /**
     * Adds extra texture layers to the renderer.
     *
     * @param renderer
     */
    public void addCustomLayers(IItemModelRenderer renderer) {
        if (this.animation != null) {
            this.animation.addItemLayers(renderer);
        }
    }

    /**
     * Renders this model based on an itemstack.
     *
     * @param itemStack     The itemstack to render.
     * @param hand          The hand that is holding the item or null if in the inventory instead.
     * @param matrixStack   The matrix stack for animation.
     * @param vertexBuilder The vertex builder to render with.
     * @param renderer      The renderer that is rendering this model, needed for texture binding.
     * @param offsetObjPart A ModelObjPart, if not null this model is offset by it, used by assembled equipment pieces to create a full model.
     * @param loop          The animation tick for looping animations, etc.
     * @param brightness    The base brightness to render at.
     */
    public void render(ItemStack itemStack, InteractionHand hand, PoseStack matrixStack, VertexConsumer vertexBuilder, IItemModelRenderer renderer, AnimationPart offsetObjPart, LayerItem layer, float loop, int brightness) {
        if (itemStack == null) {
            return;
        }
        this.matrixStack = matrixStack;

        if (layer == null && this.animation != null) {
            layer = this.animation.getBaseItemLayer();
        }

        // Render Parts:
        for (ObjPart part : this.objParts) {
            String partName = part.getName().toLowerCase();
            if (!this.canRenderPart(partName, itemStack, layer))
                continue;
            this.currentAnimationPart = this.animationParts.get(partName);

            // Begin Rendering Part:
            matrixStack.pushPose();

            // Apply Initial Offsets: (To Match Blender OBJ Export)
            this.doAngle(ROT_OFFSET_X, 1F, 0F, 0F);
            this.doTranslate(0F, POS_OFFSET_Y, 0F);

            // Apply Animation Frames:
            this.currentAnimationPart.applyAnimationFrames(this.animator);

            // Render Part:
            this.objModel.renderPart(vertexBuilder, matrixStack.last().normal(), matrixStack.last().pose(), this.getBrightness(partName, layer, itemStack, brightness), 0, part, this.getPartColor(partName, itemStack, layer, loop), this.getPartTextureOffset(partName, itemStack, layer, loop));
            matrixStack.popPose();
        }
    }

    /**
     * Gets the brightness to render the given part at.
     *
     * @param partName   The name of the part to render.
     * @param layer      The layer to render, null for base layer.
     * @param itemStack  The item stack to render.
     * @param brightness The base brightness.
     * @return The brightness to render at.
     */
    public int getBrightness(String partName, LayerItem layer, ItemStack itemStack, int brightness) {
        if (layer != null) {
            return layer.getBrightness(partName, itemStack, brightness);
        }
        return brightness;
    }

    /**
     * Gets the brightness to render the given part at.
     *
     * @param itemStack The item stack to render.
     * @param layer     The layer to render, null for base layer.
     * @return The brightness to render at.
     */
    public int getBlending(ItemStack itemStack, LayerItem layer) {
        if (layer == null && this.animation != null) {
            layer = this.animation.getBaseItemLayer();
        }
        if (layer != null) {
            return layer.getBlending(itemStack);
        }
        return CustomRenderStates.BLEND.NORMAL.getValue();
    }

    /**
     * Gets the brightness to render the given part at.
     *
     * @param itemStack The item stack to render.
     * @param layer     The layer to render, null for base layer.
     * @return The brightness to render at.
     */
    public boolean getGlow(ItemStack itemStack, LayerItem layer) {
        if (layer == null && this.animation != null) {
            layer = this.animation.getBaseItemLayer();
        }
        if (layer != null) {
            return layer.getGlow(itemStack);
        }
        return false;
    }

    /**
     * Generates all animation frames for a render tick.
     **/
    public void generateAnimationFrames(ItemStack itemStack, LayerItem layer, float loop, AnimationPart offsetObjPart) {
        for (ObjPart part : this.objParts) {
            String partName = part.getName().toLowerCase();
            if (!this.canRenderPart(partName, itemStack, layer))
                continue;
            this.currentAnimationPart = this.animationParts.get(partName);

            // Animate:
            this.animatePart(partName, itemStack, loop);
        }
    }

    /**
     * Clears all animation frames that were generated for a render tick.
     **/
    public void clearAnimationFrames() {
        for (AnimationPart animationPart : this.animationParts.values()) {
            animationPart.animationFrames.clear();
        }
    }

    /**
     * Returns true if the part can be rendered for the given stack.
     **/
    public boolean canRenderPart(String partName, ItemStack itemStack, LayerItem layer) {
        if (partName == null)
            return false;
        partName = partName.toLowerCase();

        // Check Animation Part:
        if (!this.animationParts.containsKey(partName))
            return false;

        if (layer != null && !layer.targetParts.isEmpty()) {
            if (!layer.targetParts.contains(partName))
                return false;
        }

        return true;
    }

    /**
     * Animates the individual part.
     *
     * @param partName  The name of the part (should be made all lowercase).
     * @param itemStack The itemstack to render.
     * @param loop      A continuous loop counting every tick, used for constant idle animations, etc.
     */
    public void animatePart(String partName, ItemStack itemStack, float loop) {
        if (this.animation != null) {
            for (ModelPartAnimation partAnimation : this.animation.partAnimations) {
                partAnimation.animatePart(this, partName, loop);
            }
        }
    }

    /**
     * Returns a texture ResourceLocation for the provided itemstack.
     **/
    public ResourceLocation getTexture(ItemStack itemStack, LayerItem layer) {
        return null;
    }

    /**
     * Returns the coloring to be used for this part for the given itemstack.
     **/
    public Vector4f getPartColor(String partName, ItemStack itemStack, LayerItem layer, float loop) {
        if (layer != null) {
            return layer.getPartColor(partName, itemStack, loop);
        }
        return CustomRenderStates.WHITE;
    }

    /**
     * Returns the texture offset to be used for this part and layer.
     **/
    public Vector2f getPartTextureOffset(String partName, ItemStack itemStack, LayerItem layer, float loop) {
        if (layer != null) {
            return layer.getTextureOffset(partName, itemStack, loop);
        }

        return new Vector2f(0, 0);
    }

    @Override
    public void doRotate(float rotX, float rotY, float rotZ) {
        if (rotX != 0.0F) {
            this.matrixStack.mulPose(Axis.XP.rotationDegrees(rotX));
        }
        if (rotY != 0.0F) {
            this.matrixStack.mulPose(Axis.YP.rotationDegrees(rotY));
        }
        if (rotZ != 0.0F) {
            this.matrixStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
        }
    }

    @Override
    public void doAngle(float rotation, float angleX, float angleY, float angleZ) {
        if (rotation != 0.0F && (angleX != 0.0F || angleY != 0.0F || angleZ != 0.0F)) {
            this.matrixStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(rotation), angleX, angleY, angleZ)));
        }
    }

    @Override
    public void doTranslate(float posX, float posY, float posZ) {
        if (posX != 0.0F || posY != 0.0F || posZ != 0.0F) {
            this.matrixStack.translate(posX, posY, posZ); // TODO Translation?
        }
    }

    @Override
    public void doScale(float scaleX, float scaleY, float scaleZ) {
        if (scaleX != 1.0F || scaleY != 1.0F || scaleZ != 1.0F) {
            this.matrixStack.scale(scaleX, scaleY, scaleZ); // TODO Scaling?
        }
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

    @Override
    public double rotateToPoint(double aTarget, double bTarget) {
        return rotateToPoint(0, 0, aTarget, bTarget);
    }

    @Override
    public double rotateToPoint(double aCenter, double bCenter, double aTarget, double bTarget) {
        if (aTarget - aCenter == 0)
            if (aTarget > aCenter) return 0;
            else if (aTarget < aCenter) return 180;
        if (bTarget - bCenter == 0)
            if (bTarget > bCenter) return 90;
            else if (bTarget < bCenter) return -90;
        if (aTarget - aCenter == 0 && bTarget - bCenter == 0)
            return 0;
        return Math.toDegrees(Math.atan2(aCenter - aTarget, bCenter - bTarget) - Math.PI / 2);
    }

    @Override
    public double[] rotateToPoint(double xCenter, double yCenter, double zCenter, double xTarget, double yTarget, double zTarget) {
        double[] rotations = new double[3];
        rotations[0] = this.rotateToPoint(yCenter, -zCenter, yTarget, -zTarget);
        rotations[1] = this.rotateToPoint(-zCenter, xCenter, -zTarget, xTarget);
        rotations[2] = this.rotateToPoint(yCenter, xCenter, yTarget, xTarget);
        return rotations;
    }
}
