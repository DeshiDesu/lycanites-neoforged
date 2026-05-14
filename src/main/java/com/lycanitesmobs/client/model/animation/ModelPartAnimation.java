package com.lycanitesmobs.client.model.animation;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class ModelPartAnimation {

    /**
     * The model parts that this layer should animation.
     **/
    public List<String> targetParts = new ArrayList<>();

    /**
     * How this animation determines the animation time, can be 'static' (a constant static loop), 'distance' (based on entity movement, entity only), or 'time' (a loop that resets to 0 when the model is offscreen, entity only).
     **/
    public String timeSource = "static";

    /**
     * How this animation loops, ignore if duration is 0, can be 'reset', or 'reverse'.
     **/
    public String loopType = "reset";

    /**
     * How long in seconds this animation lasts before looping, if 0, infinite.
     **/
    public float duration = 0;

    /**
     * Offsets the amount of animation seconds, used to put different part animations into a pattern.
     **/
    public float offset = 0;

    /**
     * The xyz translation to animate by.
     **/
    public Vector3f translate = new Vector3f(0, 0, 0);

    /**
     * The angle amount to animate by, (rotation, x, y, z).
     **/
    public Vector4f angle = new Vector4f(0, 0, 0, 0);

    /**
     * The xyz rotations to animate by.
     **/
    public Vector3f rotate = new Vector3f(0, 0, 0);

    /**
     * The xyz scaling to animate by.
     **/
    public Vector3f scale = new Vector3f(0, 0, 0);


    /**
     * Reads JSON data into this Animation Layer.
     *
     * @param json The JSON data to read from.
     */
    public void loadFromJson(JsonObject json) {
        if (json.has("targetParts"))
            this.targetParts = JSONHelper.getJsonStrings(json.getAsJsonArray("targetParts"));

        if (json.has("timeSource"))
            this.timeSource = json.get("timeSource").getAsString();

        if (json.has("loopType"))
            this.loopType = json.get("loopType").getAsString();

        if (json.has("duration"))
            this.duration = json.get("duration").getAsFloat();

        if (json.has("offset"))
            this.offset = json.get("offset").getAsFloat();

        float translateX = 0;
        if (json.has("translateX"))
            translateX = json.get("translateX").getAsFloat();
        float translateY = 0;
        if (json.has("translateY"))
            translateY = json.get("translateY").getAsFloat();
        float translateZ = 0;
        if (json.has("translateZ"))
            translateZ = json.get("translateZ").getAsFloat();
        this.translate = new Vector3f(translateX, translateY, translateZ);

        float angleAmount = 0;
        if (json.has("angleAmount"))
            angleAmount = json.get("angleAmount").getAsFloat();
        float angleX = 0;
        if (json.has("angleX"))
            angleX = json.get("angleX").getAsFloat();
        float angleY = 0;
        if (json.has("angleY"))
            angleY = json.get("angleY").getAsFloat();
        float angleZ = 0;
        if (json.has("angleZ"))
            angleZ = json.get("angleZ").getAsFloat();
        this.angle = new Vector4f(angleX, angleY, angleZ, angleAmount);

        float rotateX = 0;
        if (json.has("rotateX"))
            rotateX = json.get("rotateX").getAsFloat();
        float rotateY = 0;
        if (json.has("rotateY"))
            rotateY = json.get("rotateY").getAsFloat();
        float rotateZ = 0;
        if (json.has("rotateZ"))
            rotateZ = json.get("rotateZ").getAsFloat();
        this.rotate = new Vector3f(rotateX, rotateY, rotateZ);

        float scaleX = 0;
        if (json.has("scaleX"))
            scaleX = json.get("scaleX").getAsFloat();
        float scaleY = 0;
        if (json.has("scaleY"))
            scaleY = json.get("scaleY").getAsFloat();
        float scaleZ = 0;
        if (json.has("scaleZ"))
            scaleZ = json.get("scaleZ").getAsFloat();
        this.scale = new Vector3f(scaleX, scaleY, scaleZ);
    }


    /**
     * Applies this animation onto the provided part for the provided model.
     *
     * @param model    The model to animate.
     * @param partName The name of the part (should be made all lowercase).
     * @param entity   Can't be null but can be any entity. If the mob's exact entity or an EntityCreatureBase is used more animations will be used.
     * @param time     How long the model has been displayed for? This is currently unused.
     * @param distance Used for movement animations, this should just count up from 0 every tick and stop back at 0 when not moving.
     * @param loop     A continuous loop counting every tick, used for constant idle animations, etc.
     * @param lookY    A y looking rotation used by the head, etc.
     * @param lookX    An x looking rotation used by the head, etc.
     * @param scale    Used for scale based changes during animation but not to actually apply the scale as it is applied in the renderer method.
     */
    public void animatePart(IAnimationModel model, String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        // Check Part Name:
        boolean matchingPart = false;
        for (String targetPart : this.targetParts) {
            if (targetPart.equalsIgnoreCase(partName)) {
                matchingPart = true;
                break;
            }
        }
        if (!matchingPart) {
            return;
        }

        float progress = loop + (this.offset * 20);

        // Time Source:
        if ("distance".equalsIgnoreCase(this.timeSource))
            progress = distance + this.offset;
        else if ("time".equalsIgnoreCase(this.timeSource))
            progress = time + this.offset;

        // Looping Type:
        float duration = this.duration * 20;
        if (duration != 0) {
            if ("reset".equalsIgnoreCase(this.loopType)) {
                progress = progress % duration;
            } else if ("reverse".equalsIgnoreCase(this.loopType)) {
                float cycleProgress = progress % duration;
                int cycleCount = Math.round((progress - cycleProgress) / duration);
                if (cycleCount % 2 != 0) {
                    cycleProgress = duration - cycleProgress;
                }
                progress = cycleProgress;
            }
        }

        // Translate:
        model.translate(this.translate.x() * progress, this.translate.y() * progress, this.translate.z() * progress);

        // Angle:
        model.angle(this.angle.w() * progress, this.angle.x(), this.angle.y(), this.angle.z());

        // Rotate:
        model.rotate(this.rotate.x() * progress, this.rotate.y() * progress, this.rotate.z() * progress);

        // Scale:
        model.scale(1 + this.scale.x() * progress, 1 + this.scale.y() * progress, 1 + this.scale.z() * progress);
    }


    /**
     * Applies this animation onto the provided part for the provided model, this is a reduced method used by items and blocks.
     *
     * @param model    The model to animate.
     * @param partName The name of the part (should be made all lowercase).
     * @param loop     How long the model has been displayed for? This is currently unused.
     */
    public void animatePart(IAnimationModel model, String partName, float loop) {
        this.animatePart(model, partName, null, loop, 0, loop, 0, 0, 1);
    }
}
