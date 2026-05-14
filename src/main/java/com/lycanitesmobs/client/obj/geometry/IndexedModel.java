package com.lycanitesmobs.client.obj.geometry;


import com.lycanitesmobs.client.loader.OBJLoader;
import com.lycanitesmobs.core.util.math.Vector3o;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class IndexedModel {

    private ArrayList<Vector3o> vertices;
    private ArrayList<Vector2f> texCoords;
    private ArrayList<Vector3o> normals;
    private ArrayList<Vector3o> tangents;
    private ArrayList<Integer> indices;
    private ArrayList<OBJLoader.OBJIndex> objindices;

    public IndexedModel() {
        vertices = new ArrayList<>();
        texCoords = new ArrayList<>();
        normals = new ArrayList<>();
        tangents = new ArrayList<>();
        indices = new ArrayList<>();
        objindices = new ArrayList<>();
    }

    public ArrayList<Vector3o> getPositions() {
        return vertices;
    }

    public ArrayList<Vector2f> getTexCoords() {
        return texCoords;
    }

    public ArrayList<Vector3o> getNormals() {
        return normals;
    }

    public ArrayList<Integer> getIndices() {
        return indices;
    }

    public ArrayList<Vector3o> getTangents() {
        return tangents;
    }

    public void toMesh(Mesh mesh) {
        ArrayList<Vertex> verticesList = new ArrayList<>();
        int n = Math.min(vertices.size(), Math.min(texCoords.size(), normals.size()));
        for (int i = 0; i < n; i++) {
            Vertex vertex = new Vertex(
                    vertices.get(i),
                    texCoords.get(i),
                    normals.get(i),
                    new Vector3f());
            verticesList.add(vertex);
        }
        Integer[] indicesArray = indices.toArray(new Integer[0]);
        Vertex[] verticesArray = verticesList.toArray(new Vertex[0]);
        int[] indicesArrayInt = new int[indicesArray.length];
        for (int i = 0; i < indicesArray.length; i++)
            indicesArrayInt[i] = indicesArray[i];
        mesh.vertices = verticesArray;
        mesh.indices = indicesArrayInt;
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3o copy(Vector3o vector3f) {
        return new Vector3o(vector3f.x, vector3f.y, vector3f.z);
    }

    public void computeNormals() throws CloneNotSupportedException {
        for (int i = 0; i < indices.size(); i += 3) {
            int x = indices.get(i);
            int y = indices.get(i + 1);
            int z = indices.get(i + 2);

            // Per Vertex Normal:
            Vector3o v = copy(vertices.get(y));
            v.sub(vertices.get(x));
            Vector3o l0 = v;
            v = copy(vertices.get(z));
            v.sub(vertices.get(x));
            Vector3o l1 = v;
            v = copy(l0);
            v.cross(l1); //cross(l0, l1)
            Vector3o normal = v;

            v = copy(normals.get(x));
            v.add(normal);
            normals.set(x, v);
            v = copy(normals.get(y));
            v.add(normal);
            normals.set(y, v);
            v = copy(normals.get(z));
            v.add(normal);
            normals.set(z, v);
        }

        for (int i = 0; i < normals.size(); i++) {
            normals.get(i).normalize();
        }
    }

    public Vector3f getFaceNormal(Vector3f p1, Vector3f p2, Vector3f p3) {
        Vector3f output = new Vector3f();

        // Calculate Edges:
        Vector3f calU = new Vector3f(p2.x() - p1.x(), p2.y() - p1.y(), p2.z() - p1.z());
        Vector3f calV = new Vector3f(p3.x() - p1.x(), p3.y() - p1.y(), p3.z() - p1.z());

        // Cross Edges:
        output.set(
                calU.y() * calV.z() - calU.z() * calV.y(),
                calU.z() * calV.x() - calU.x() * calV.z(),
                calU.x() * calV.y() - calU.y() * calV.x()
        );

        output.normalize(); // normalize()
        return output;
    }

    public void computeTangents() {
        tangents.clear();
        for (int i = 0; i < vertices.size(); i++)
            tangents.add(new Vector3o());

        for (int i = 0; i < indices.size(); i += 3) {
            int i0 = indices.get(i);
            int i1 = indices.get(i + 1);
            int i2 = indices.get(i + 2);

            Vector3o v = copy(vertices.get(i1)); // clone()
            v.sub(vertices.get(i0));
            Vector3o edge1 = v;
            v = copy(vertices.get(i2)); // clone()
            v.sub(vertices.get(i0));
            Vector3o edge2 = v;

            double deltaU1 = texCoords.get(i1).x - texCoords.get(i0).x;
            double deltaU2 = texCoords.get(i2).x - texCoords.get(i0).x;
            double deltaV1 = texCoords.get(i1).y - texCoords.get(i0).y;
            double deltaV2 = texCoords.get(i2).y - texCoords.get(i0).y;

            double dividend = (deltaU1 * deltaV2 - deltaU2 * deltaV1);
            double f = dividend == 0.0f ? 0.0f : 1.0f / dividend;

            Vector3o tangent = new Vector3o((float) (f * (deltaV2 * edge1.x() - deltaV1 * edge2.x())), (float) (f * (deltaV2 * edge1.y() - deltaV1 * edge2.y())), (float) (f * (deltaV2 * edge1.z() - deltaV1 * edge2.z())));

            v = copy(tangents.get(i0)); // clone()
            v.add(tangent); // add()
            tangents.set(i0, v);
            v = copy(tangents.get(i1)); // clone()
            v.add(tangent);
            tangents.set(i1, v);
            v = copy(tangents.get(i2)); // clone()
            v.add(tangent);
            tangents.set(i2, v);
        }

        for (int i = 0; i < tangents.size(); i++)
            tangents.get(i).normalize(); // normalize()
    }

    public ArrayList<OBJLoader.OBJIndex> getObjIndices() {
        return objindices;
    }

    public Vector3f computeCenter() {
        float x = 0;
        float y = 0;
        float z = 0;
        for (Vector3o position : vertices) {
            x += position.x();
            y += position.y();
            z += position.z();
        }
        x /= vertices.size();
        y /= vertices.size();
        z /= vertices.size();
        return new Vector3f(x, y, z);
    }
}
