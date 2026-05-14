package com.lycanitesmobs.client.obj.geometry;

import com.lycanitesmobs.core.util.math.Vector3o;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import com.lycanitesmobs.client.renderer.util.CustomRenderStates;

import java.util.ArrayList;
import java.util.List;

public class Mesh {

    public int[] indices;
    public Vertex[] vertices;
    public Vector3o[] normals;

    private VertexBuffer vbo = null;
    public static List<String> logs = new ArrayList<>();

    /**
     * Returns (and lazily builds) the VBO in our custom POS_TEX_NORMAL format.
     */
    public VertexBuffer getVbo() {
        try {
            if (this.vbo == null) {
                LMHelperClass.logDebug("Resources", "Mesh.getVbo: building custom-format VBO");
                computeVertexNormalsIfNeeded();

                Tesselator tess = Tesselator.getInstance();
                BufferBuilder buf = tess.getBuilder();
                buf.begin(VertexFormat.Mode.TRIANGLES, CustomRenderStates.POS_TEX_NORMAL);

                for (int i = 0; i < this.indices.length; i++) {
                    int idx = this.indices[i];
                    Vertex vertex = this.vertices[idx];
                    Vector3o normal = this.normals != null && idx < this.normals.length ? this.normals[idx] : null;

                    float px = vertex.getPos().x();
                    float py = vertex.getPos().y();
                    float pz = vertex.getPos().z();

                    float u = vertex.getTexCoords().x;
                    float v = 1.0F - vertex.getTexCoords().y;

                    if (normal == null) {
                        String message = "[Lycanites Mobs Debug]: Normal vector is null";
                        if (!logs.contains(message)) {
                            LMHelperClass.logWarning("", message);
                            logs.add(message);
                        }
                        normal = new Vector3o(0, 0, 0);
                    }

                    buf.vertex(px, py, pz)
                            .uv(u, v)
                            .normal(normal.x(), normal.y(), normal.z())
                            .endVertex();
                }

                VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
                vb.bind();
                vb.upload(buf.end());
                VertexBuffer.unbind();

                this.vbo = vb;
            }
        } catch (Exception e) {
            LMHelperClass.logErrorMessageOnceCatchable("Error in Mesh class (custom VBO). ", e);
        }
        return this.vbo;
    }

    public void delete() {
        if (this.vbo != null) {
            this.vbo.close();
            this.vbo = null;
        }
    }

    private static Vector3o calcNormal(Vector3o v1, Vector3o v2, Vector3o v3) {
        Vector3o u = new Vector3o(v2).sub(v1);
        Vector3o v = new Vector3o(v3).sub(v1);
        Vector3o out = new Vector3o(
                u.y() * v.z() - u.z() * v.y(),
                u.z() * v.x() - u.x() * v.z(),
                u.x() * v.y() - u.y() * v.x()
        );
        out.normalize();
        return out;
    }

    public void computeVertexNormalsIfNeeded() {
        if (this.normals != null && this.vertices != null && this.normals.length == this.vertices.length) {
            return;
        }
        if (this.vertices == null || this.indices == null) {
            this.normals = null;
            return;
        }

        Vector3o[] vertexNormals = new Vector3o[this.vertices.length];
        for (int i = 0; i < vertexNormals.length; i++) {
            vertexNormals[i] = new Vector3o(0, 0, 0);
        }

        for (int i = 0; i < this.indices.length; i += 3) {
            int i0 = this.indices[i];
            int i1 = this.indices[i + 1];
            int i2 = this.indices[i + 2];

            Vector3o v1 = this.vertices[i0].getPos();
            Vector3o v2 = this.vertices[i1].getPos();
            Vector3o v3 = this.vertices[i2].getPos();

            Vector3o faceNormal = calcNormal(v1, v2, v3);

            Vector3o n0 = vertexNormals[i0];
            Vector3o n1 = vertexNormals[i1];
            Vector3o n2 = vertexNormals[i2];

            n0.set(n0.x() + faceNormal.x(), n0.y() + faceNormal.y(), n0.z() + faceNormal.z());
            n1.set(n1.x() + faceNormal.x(), n1.y() + faceNormal.y(), n1.z() + faceNormal.z());
            n2.set(n2.x() + faceNormal.x(), n2.y() + faceNormal.y(), n2.z() + faceNormal.z());
        }

        for (int i = 0; i < vertexNormals.length; i++) {
            vertexNormals[i].normalize();
        }

        this.normals = vertexNormals;
    }
}
