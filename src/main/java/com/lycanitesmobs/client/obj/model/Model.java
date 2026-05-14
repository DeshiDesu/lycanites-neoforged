package com.lycanitesmobs.client.obj.model;


import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public abstract class Model {


    public abstract void render(VertexConsumer vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f, int brightness);

    public abstract void renderGroups(VertexConsumer vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f, int brightness, String s);

}
