package com.lycanitesmobs.client.model.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class ModelObjAnimationFrame {
    /**
     * The type of animation to do. Can be: angle, rotate, translate or scale.
     **/
    public String type;
    /**
     * The amount to animate by, usually 1, but can be used to scale an animation and also used for the angle type.
     **/
    public float amount;
    /**
     * The x amount this animation frame moves by.
     **/
    public float x;
    /**
     * The y amount this animation frame moves by.
     **/
    public float y;
    /**
     * The z amount this animation frame moves by.
     **/
    public float z;

    // ==================================================
    //                    Constructor
    // ==================================================
    public ModelObjAnimationFrame(String type, float amount, float x, float y, float z) {
        this.type = type;
        this.amount = amount;
        this.x = x;
        this.y = y;
        this.z = z;
    }


    // ==================================================
    //                      Apply
    // ==================================================

    /**
     * Performs this animation frame.
     **/
    public void apply(Animator animator) {
        if ("angle".equals(this.type))
            animator.doAngle(this.amount, this.x, this.y, this.z);
        if ("rotate".equals(this.type))
            animator.doRotate(this.x * this.amount, this.y * this.amount, this.z * this.amount);
        if ("translate".equals(this.type))
            animator.doTranslate(this.x * this.amount, this.y * this.amount, this.z * this.amount);
        if ("scale".equals(this.type))
            animator.doScale(this.x * this.amount, this.y * this.amount, this.z * this.amount);
    }

    public void apply(PoseStack poseStack) {
        if ("angle".equals(this.type)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(this.amount));
            poseStack.mulPose(Axis.XP.rotationDegrees(this.x));
            poseStack.mulPose(Axis.ZP.rotationDegrees(this.z));
        } else if ("rotate".equals(this.type)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(this.x * this.amount));
            poseStack.mulPose(Axis.YP.rotationDegrees(this.y * this.amount));
            poseStack.mulPose(Axis.ZP.rotationDegrees(this.z * this.amount));
        } else if ("translate".equals(this.type)) {
            poseStack.translate(this.x * this.amount, this.y * this.amount, this.z * this.amount);
        } else if ("scale".equals(this.type)) {
            poseStack.scale(this.x * this.amount, this.y * this.amount, this.z * this.amount);
        }
    }

}
