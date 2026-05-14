package com.lycanitesmobs.client.obj.geometry;


import com.lycanitesmobs.core.util.math.Vector3o;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex {
    private Vector3o pos;
    private Vector2f texCoords;
    private Vector3o normal;
    private Vector3f tangent;

    public Vertex(Vector3o pos, Vector2f texCoords, Vector3o normal, Vector3f tangent, float r, float g, float b, float a) {
        this.pos = pos;
        this.texCoords = texCoords;
        this.normal = normal;
        this.tangent = tangent;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Vertex(Vector3o pos, Vector2f texCoords, Vector3o normal, Vector3f tangent) {
        this.pos = pos;
        this.texCoords = texCoords;
        this.normal = normal;
        this.tangent = tangent;
    }

    public float getU() {
        return texCoords.x;
    }

    public float getV() {
        return texCoords.y;
    }

    public Vector3o getPos() {
        return this.pos;
    }

    public Vector2f getTexCoords() {
        return this.texCoords;
    }

    /**
     * Returns per vertex normal for smoother shading.
     **/
    public Vector3o getNormal() {
        return this.normal;
    }

    public Vector3f getTangent() {
        return tangent;
    }

    private float x;

    private float y;

    private float z;

    private float r;

    private float g;

    private float b;

    private float a;

    public float getA() {
        return a;
    }

    public float getB() {
        return b;
    }

    public float getG() {
        return g;
    }

    public float getR() {
        return r;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public Vertex(float x, float y, float z, float r, float g, float b, float a) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}
