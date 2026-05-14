package com.lycanitesmobs.client.model.animation;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationPart {
    /**
     * The name of this model part.
     **/
    public String name;
    /**
     * The parent part of this model part, null if this part has no parent. This will do all animations that the parent does.
     **/
    public AnimationPart parent;
    /**
     * The offet part of this model part, null if this part has no offset. Offsets are similar to parents but are owned by other models, used by Equipment pieces.
     **/
    public AnimationPart offset;
    /**
     * The parent name of this model part, used for initial setup and null if this part has no parent.
     **/
    public String parentName;
    /**
     * The child parts connected to this part, these will all do the animations that this does.
     **/
    public Map<String, AnimationPart> children = new HashMap<>();
    /**
     * The x center location of this part for rotating around.
     **/
    public float centerX;
    /**
     * The y center location of this part for rotating around.
     **/
    public float centerY;
    /**
     * The z center location of this part for rotating around.
     **/
    public float centerZ;
    /**
     * The x rotation of this part.
     **/
    public float rotationX;
    /**
     * The y rotation of this part.
     **/
    public float rotationY;
    /**
     * The z rotation of this part.
     **/
    public float rotationZ;

    /**
     * A list of animation frames to apply to this part on the next render frame.
     **/
    public List<ModelObjAnimationFrame> animationFrames = new ArrayList<>();


    /**
     * Reads JSON dat into this ObjPart.
     *
     * @param jsonObject
     */
    public void loadFromJson(JsonObject jsonObject) {
        this.name = jsonObject.get("name").getAsString().toLowerCase();
        this.parentName = jsonObject.get("parent").getAsString().toLowerCase();
        if (this.parentName.isEmpty())
            this.parentName = null;
        this.centerX = Float.parseFloat(jsonObject.get("centerX").getAsString());
        this.centerY = Float.parseFloat(jsonObject.get("centerY").getAsString());
        this.centerZ = Float.parseFloat(jsonObject.get("centerZ").getAsString());
        if (jsonObject.has("rotationX"))
            this.rotationX = Float.parseFloat(jsonObject.get("rotationX").getAsString());
        if (jsonObject.has("rotationY"))
            this.rotationY = Float.parseFloat(jsonObject.get("rotationY").getAsString());
        if (jsonObject.has("rotationZ"))
            this.rotationZ = Float.parseFloat(jsonObject.get("rotationZ").getAsString());
    }


    /**
     * Adds child parts to this part.
     *
     * @param parts An array of child parts to add.
     */
    public void addChildren(AnimationPart[] parts) {
        for (AnimationPart part : parts) {
            if (part == null || part == this || part.parentName == null)
                continue;
            if (this.children.containsKey(part.parentName))
                continue;
            if (this.name.equals(part.parentName)) {
                this.children.put(part.name, part);
                part.parent = this;
            }
        }
    }


    /**
     * Searches for the root parent (the first parent, of parent, etc that has no parent).
     *
     * @return The root parent part.
     */
    public AnimationPart getRootParent() {
        if (this.parent == null) {
            return this;
        }
        return this.parent.getRootParent();
    }


    /**
     * Adds a new animation frame to apply during the next render frame.
     *
     * @param frame The animation frame to add.
     */
    public void addAnimationFrame(ModelObjAnimationFrame frame) {
        this.animationFrames.add(frame);
    }


    /**
     * Applies all animation frames to this part and will then go through any parents and apply theirs also.
     *
     * @param animator The animator instance to use.
     */
    public void applyAnimationFrames(Animator animator) {
        // Apply Parent Frames:
        if (this.parent != null) {
            this.parent.applyAnimationFrames(animator);
        }

        // Apply Offset Frames:
        if (this.offset != null) {
            this.offset.applyAnimationFrames(animator);
            animator.doTranslate(this.centerX + this.offset.centerX, this.centerY + this.offset.centerY, this.centerZ + this.offset.centerZ);
            animator.doRotate(-this.offset.rotationX, -this.offset.rotationY, -this.offset.rotationZ);
        }

        // Center Part:
        animator.doTranslate(this.centerX, this.centerY, this.centerZ);

        // Apply Frames:
        for (ModelObjAnimationFrame animationFrame : this.animationFrames) {
            animationFrame.apply(animator);
        }

        // Uncenter Part:
        animator.doTranslate(-this.centerX, -this.centerY, -this.centerZ);
    }


    /**
     * Sets the offset of this part to the provided part.
     *
     * @param offsetPart The part to set as the offset.
     * @return This part instance for chaining.
     */
    public AnimationPart setOffset(AnimationPart offsetPart) {
        this.offset = offsetPart;
        return this;
    }
}
