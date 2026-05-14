package com.lycanitesmobs.client.renderer.entity.effect;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.util.Mth;

public class FearMesh {

    private final FearMeshProfile profile;
    /** Inner wisp chains (Rings 1-2). */
    private final float[][] chains;
    private final float[] lengthScales;
    private final float[] widthScales;

    /** Cape strip chains whose roots attach to the hood rim. */
    private final float[][] capeChains;
    private final float[] capeLengthScales;
    private final float[] capeWidthScales;

    /** Hood vertex grid: (rows+1)*(cols+1) vertices, 3 floats each (x,y,z). */
    private final float[] hoodGrid;
    /** Per-vertex alpha for the hood grid. */
    private final float[] hoodAlphas;

    private VertexBuffer vbo;
    private int vertexCount;
    private boolean needsUpload = true;
    private float time;

    private float smoothYaw;
    private float smoothPitch;
    private float prevYaw;
    private float prevPitch;
    private boolean hasOrientation;
    private float smoothSpeed;

    public FearMesh(FearMeshProfile profile) {
        this.profile = profile;

        // Inner wisp strips
        int count = profile.strips.length;
        this.chains = new float[count][];
        this.lengthScales = new float[count];
        this.widthScales = new float[count];

        for (int t = 0; t < count; t++) {
            FearMeshProfile.Strip strip = profile.strips[t];
            lengthScales[t] = 0.8f + hash(t * 137) * 0.4f;
            widthScales[t] = 0.7f + hash(t * 211) * 0.6f;

            int points = strip.segments + 1;
            float[] chain = new float[points * 3];
            for (int s = 0; s < points; s++) {
                int b = s * 3;
                chain[b] = strip.anchorX;
                chain[b + 1] = strip.anchorY - s * strip.segmentLength * lengthScales[t];
                chain[b + 2] = strip.anchorZ;
            }
            this.chains[t] = chain;
        }

        // Cape strips (attached to hood rim)
        if (profile.capeStrips != null && profile.capeStrips.length > 0 && profile.hood != null) {
            int capeCount = profile.capeStrips.length;
            this.capeChains = new float[capeCount][];
            this.capeLengthScales = new float[capeCount];
            this.capeWidthScales = new float[capeCount];

            for (int t = 0; t < capeCount; t++) {
                FearMeshProfile.CapeStrip cs = profile.capeStrips[t];
                capeLengthScales[t] = 0.85f + hash((t + 100) * 137) * 0.3f;
                capeWidthScales[t] = 0.8f + hash((t + 100) * 211) * 0.4f;

                int points = cs.segments + 1;
                float[] chain = new float[points * 3];
                // Initialize hanging straight down; corrected on first tick.
                for (int s = 0; s < points; s++) {
                    chain[s * 3]     = 0;
                    chain[s * 3 + 1] = profile.hood.bottomY - s * cs.segmentLength;
                    chain[s * 3 + 2] = 0;
                }
                this.capeChains[t] = chain;
            }
        } else {
            this.capeChains = null;
            this.capeLengthScales = null;
            this.capeWidthScales = null;
        }

        // Hood grid
        if (profile.hood != null) {
            int verts = (profile.hood.rows + 1) * (profile.hood.cols + 1);
            this.hoodGrid = new float[verts * 3];
            this.hoodAlphas = new float[verts];
            initHoodAlphas();
        } else {
            this.hoodGrid = null;
            this.hoodAlphas = null;
        }
    }

    public void tick(float rawYaw, float rawPitch, float speed, float partialTick) {
        time += 0.05f + partialTick * 0.005f;
        smoothSpeed += (speed - smoothSpeed) * profile.speedSmoothing;

        if (!hasOrientation) {
            smoothYaw = rawYaw;
            smoothPitch = rawPitch;
            prevYaw = rawYaw;
            prevPitch = rawPitch;
            hasOrientation = true;
        } else {
            float yawDelta = rawYaw - smoothYaw;
            while (yawDelta > 180.0f) yawDelta -= 360.0f;
            while (yawDelta < -180.0f) yawDelta += 360.0f;
            smoothYaw += yawDelta * profile.yawSmoothing;
            smoothPitch += (rawPitch - smoothPitch) * profile.pitchSmoothing;
        }

        float dYaw = smoothYaw - prevYaw;
        float dPitch = smoothPitch - prevPitch;

        if (Math.abs(dYaw) > 0.001f || Math.abs(dPitch) > 0.001f) {
            float yawRad = (float) Math.toRadians(-dYaw);
            float pitchRad = (float) Math.toRadians(-dPitch);
            float cosY = (float) Math.cos(yawRad);
            float sinY = (float) Math.sin(yawRad);
            float cosP = (float) Math.cos(pitchRad);
            float sinP = (float) Math.sin(pitchRad);

            // Inner wisp chains
            for (int t = 0; t < chains.length; t++) {
                applyIKRotation(chains[t], profile.strips[t].segments + 1, cosY, sinY, cosP, sinP);
            }
            // Cape strip chains
            if (capeChains != null) {
                for (int t = 0; t < capeChains.length; t++) {
                    applyIKRotation(capeChains[t], profile.capeStrips[t].segments + 1, cosY, sinY, cosP, sinP);
                }
            }
        }

        prevYaw = smoothYaw;
        prevPitch = smoothPitch;

        float idleBlend = Mth.clamp(1.0f - smoothSpeed * 40.0f, 0.0f, 1.0f);

        for (int t = 0; t < chains.length; t++) {
            FearMeshProfile.Strip strip = profile.strips[t];
            float[] chain = chains[t];
            float lenScale = lengthScales[t];
            int points = strip.segments + 1;

            float rDist = Mth.sqrt(strip.anchorX * strip.anchorX + strip.anchorZ * strip.anchorZ);
            float rAngle = (float) Math.atan2(strip.anchorZ, strip.anchorX);
            float rootWobble = (float) Math.sin(time * 1.8f + t * 2.1f) * 0.04f;
            float ringR = rDist + rootWobble;
            chain[0] = (float) Math.cos(rAngle) * ringR;
            chain[1] = strip.anchorY + (float) Math.sin(time * 1.2f + t * 1.7f) * 0.03f;
            chain[2] = (float) Math.sin(rAngle) * ringR;

            for (int s = 1; s < points; s++) {
                int cur = s * 3;
                int par = (s - 1) * 3;
                float segLen = strip.segmentLength * lenScale;
                float depthT = (float) s / strip.segments;

                float cx = chain[cur], cy = chain[cur + 1], cz = chain[cur + 2];
                float px = chain[par], py = chain[par + 1], pz = chain[par + 2];

                float idleInX = -chain[cur] * profile.idleInwardStrength;
                float idleInZ = -chain[cur + 2] * profile.idleInwardStrength;

                float wavePhase = time * profile.waveFrequency + t * 0.55f + s * 0.4f;
                float ws = profile.waveAmplitude * strip.waveScale * depthT;
                float waveX = (float) Math.sin(wavePhase) * ws;
                float waveZ = (float) Math.cos(wavePhase * 0.7f + 1.3f) * ws;
                float waveY = (float) Math.sin(wavePhase * 0.5f + 2.7f) * ws * 0.3f;

                float turbPhase = time * 4.5f + t * 3.7f + s * 1.9f;
                float ts = profile.idleTurbulence * depthT;
                float turbX = (float) Math.sin(turbPhase) * ts;
                float turbZ = (float) Math.cos(turbPhase * 1.3f) * ts;
                float turbY = (float) Math.sin(turbPhase * 0.8f + 1.0f) * ts * 0.5f;

                float dirX = (idleInX + turbX) * idleBlend + waveX;
                float dirY = -0.08f + turbY * idleBlend + waveY;
                float dirZ = (idleInZ + turbZ) * idleBlend + waveZ;

                float dirLen = Mth.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
                if (dirLen < 0.001f) {
                    dirX = 0;
                    dirY = -1;
                    dirZ = 0;
                    dirLen = 1;
                }

                float targetX = px + (dirX / dirLen) * segLen;
                float targetY = py + (dirY / dirLen) * segLen;
                float targetZ = pz + (dirZ / dirLen) * segLen;

                float stiff = Math.max(strip.stiffness - depthT * strip.depthLag, 0.03f);
                chain[cur] = cx + (targetX - cx) * stiff;
                chain[cur + 1] = cy + (targetY - cy) * stiff;
                chain[cur + 2] = cz + (targetZ - cz) * stiff;

                float dx = chain[cur] - chain[par];
                float dy = chain[cur + 1] - chain[par + 1];
                float dz = chain[cur + 2] - chain[par + 2];
                float dist = Mth.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > segLen * 1.5f && dist > 0.001f) {
                    float scale = segLen * 1.5f / dist;
                    chain[cur] = chain[par] + dx * scale;
                    chain[cur + 1] = chain[par + 1] + dy * scale;
                    chain[cur + 2] = chain[par + 2] + dz * scale;
                }
            }
        }

        if (profile.hood != null) {
            updateHood();
        }

        // Cape strips: anchor roots to hood rim, then solve chains.
        if (capeChains != null && profile.hood != null) {
            anchorCapesToHoodRim();
            for (int t = 0; t < capeChains.length; t++) {
                FearMeshProfile.CapeStrip cs = profile.capeStrips[t];
                float[] chain = capeChains[t];
                float lenScale = capeLengthScales[t];
                int points = cs.segments + 1;

                for (int s = 1; s < points; s++) {
                    int cur = s * 3;
                    int par = (s - 1) * 3;
                    float segLen = cs.segmentLength * lenScale;
                    float depthT = (float) s / cs.segments;

                    float cx = chain[cur], cy = chain[cur + 1], cz = chain[cur + 2];
                    float px = chain[par], py = chain[par + 1], pz = chain[par + 2];

                    float idleInX = -chain[cur] * profile.idleInwardStrength;
                    float idleInZ = -chain[cur + 2] * profile.idleInwardStrength;

                    // Offset by strip count so cape waves don't sync with wisps.
                    int waveIdx = t + chains.length;
                    float wavePhase = time * profile.waveFrequency + waveIdx * 0.55f + s * 0.4f;
                    float ws = profile.waveAmplitude * cs.waveScale * depthT;
                    float waveX = (float) Math.sin(wavePhase) * ws;
                    float waveZ = (float) Math.cos(wavePhase * 0.7f + 1.3f) * ws;
                    float waveY = (float) Math.sin(wavePhase * 0.5f + 2.7f) * ws * 0.3f;

                    float turbPhase = time * 4.5f + waveIdx * 3.7f + s * 1.9f;
                    float ts = profile.idleTurbulence * depthT;
                    float turbX = (float) Math.sin(turbPhase) * ts;
                    float turbZ = (float) Math.cos(turbPhase * 1.3f) * ts;
                    float turbY = (float) Math.sin(turbPhase * 0.8f + 1.0f) * ts * 0.5f;

                    float dirX = (idleInX + turbX) * idleBlend + waveX;
                    float dirY = -0.08f + turbY * idleBlend + waveY;
                    float dirZ = (idleInZ + turbZ) * idleBlend + waveZ;

                    float dirLen = Mth.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
                    if (dirLen < 0.001f) { dirX = 0; dirY = -1; dirZ = 0; dirLen = 1; }

                    float targetX = px + (dirX / dirLen) * segLen;
                    float targetY = py + (dirY / dirLen) * segLen;
                    float targetZ = pz + (dirZ / dirLen) * segLen;

                    float stiff = Math.max(cs.stiffness - depthT * cs.depthLag, 0.03f);
                    chain[cur]     = cx + (targetX - cx) * stiff;
                    chain[cur + 1] = cy + (targetY - cy) * stiff;
                    chain[cur + 2] = cz + (targetZ - cz) * stiff;

                    float dx = chain[cur] - chain[par];
                    float dy = chain[cur + 1] - chain[par + 1];
                    float dz = chain[cur + 2] - chain[par + 2];
                    float dist = Mth.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > segLen * 1.5f && dist > 0.001f) {
                        float scale = segLen * 1.5f / dist;
                        chain[cur]     = chain[par] + dx * scale;
                        chain[cur + 1] = chain[par + 1] + dy * scale;
                        chain[cur + 2] = chain[par + 2] + dz * scale;
                    }
                }
            }
        }

        needsUpload = true;
    }

    public float getSmoothedYaw() {
        return smoothYaw;
    }

    public float getSmoothedPitch() {
        return smoothPitch;
    }

    public VertexFormat getVertexFormat() {
        return DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;
    }

    public VertexBuffer getVbo() {
        if (!needsUpload && vbo != null) return vbo;

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        vertexCount = 0;

        for (int t = 0; t < chains.length; t++) {
            FearMeshProfile.Strip strip = profile.strips[t];
            float[] chain = chains[t];
            float wScale = widthScales[t];

            for (int s = 0; s < strip.segments; s++) {
                int a = s * 3;
                int b = (s + 1) * 3;

                float ax = chain[a], ay = chain[a + 1], az = chain[a + 2];
                float bx = chain[b], by = chain[b + 1], bz = chain[b + 2];

                float dx = bx - ax, dy = by - ay, dz = bz - az;
                float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
                if (len < 0.0001f) continue;
                dx /= len;
                dy /= len;
                dz /= len;

                float refX, refY, refZ;
                if (Math.abs(dy) > 0.95f) {
                    refX = 0;
                    refY = 0;
                    refZ = 1;
                } else {
                    refX = 0;
                    refY = 1;
                    refZ = 0;
                }

                float perpX = dy * refZ - dz * refY;
                float perpY = dz * refX - dx * refZ;
                float perpZ = dx * refY - dy * refX;
                float perpLen = Mth.sqrt(perpX * perpX + perpY * perpY + perpZ * perpZ);
                if (perpLen < 0.0001f) continue;
                perpX /= perpLen;
                perpY /= perpLen;
                perpZ /= perpLen;

                float perp2X = dy * perpZ - dz * perpY;
                float perp2Y = dz * perpX - dx * perpZ;
                float perp2Z = dx * perpY - dy * perpX;

                float tA = (float) s / strip.segments;
                float tB = (float) (s + 1) / strip.segments;
                float wA = Mth.lerp(tA, strip.widthStart, strip.widthEnd) * wScale;
                float wB = Mth.lerp(tB, strip.widthStart, strip.widthEnd) * wScale;

                if (strip.taperIn > 0.0f) {
                    float rampA = Mth.clamp(tA / strip.taperIn, 0.0f, 1.0f);
                    float rampB = Mth.clamp(tB / strip.taperIn, 0.0f, 1.0f);
                    rampA = rampA * rampA * (3.0f - 2.0f * rampA);
                    rampB = rampB * rampB * (3.0f - 2.0f * rampB);
                    wA *= rampA;
                    wB *= rampB;
                }

                // Per-segment alpha: lerp from strip's alphaStart (at base) to alphaEnd (at tip)
                float alphaA = Mth.lerp(tA, strip.alphaStart, strip.alphaEnd);
                float alphaB = Mth.lerp(tB, strip.alphaStart, strip.alphaEnd);

                emitQuadStrip(buf, ax, ay, az, bx, by, bz, perpX, perpY, perpZ, wA, wB, tA, tB, alphaA, alphaB);
                emitQuadStrip(buf, ax, ay, az, bx, by, bz, perp2X, perp2Y, perp2Z, wA, wB, tA, tB, alphaA, alphaB);
                vertexCount += 24;
            }
        }

        // Cape strips (individual cross-section quads for depth/thickness)
        if (capeChains != null) {
            for (int t = 0; t < capeChains.length; t++) {
                FearMeshProfile.CapeStrip cs = profile.capeStrips[t];
                float[] chain = capeChains[t];
                float wScale = capeWidthScales[t];

                for (int s = 0; s < cs.segments; s++) {
                    int a = s * 3;
                    int b = (s + 1) * 3;

                    float ax = chain[a], ay = chain[a + 1], az = chain[a + 2];
                    float bx = chain[b], by = chain[b + 1], bz = chain[b + 2];

                    float dx = bx - ax, dy = by - ay, dz = bz - az;
                    float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
                    if (len < 0.0001f) continue;
                    dx /= len; dy /= len; dz /= len;

                    float refX, refY, refZ;
                    if (Math.abs(dy) > 0.95f) { refX = 0; refY = 0; refZ = 1; }
                    else { refX = 0; refY = 1; refZ = 0; }

                    float perpX = dy * refZ - dz * refY;
                    float perpY = dz * refX - dx * refZ;
                    float perpZ = dx * refY - dy * refX;
                    float perpLen = Mth.sqrt(perpX * perpX + perpY * perpY + perpZ * perpZ);
                    if (perpLen < 0.0001f) continue;
                    perpX /= perpLen; perpY /= perpLen; perpZ /= perpLen;

                    float perp2X = dy * perpZ - dz * perpY;
                    float perp2Y = dz * perpX - dx * perpZ;
                    float perp2Z = dx * perpY - dy * perpX;

                    float tA = (float) s / cs.segments;
                    float tB = (float) (s + 1) / cs.segments;
                    float wA = Mth.lerp(tA, cs.widthStart, cs.widthEnd) * wScale;
                    float wB = Mth.lerp(tB, cs.widthStart, cs.widthEnd) * wScale;

                    float alphaA = Mth.lerp(tA, cs.alphaStart, cs.alphaEnd);
                    float alphaB = Mth.lerp(tB, cs.alphaStart, cs.alphaEnd);

                    emitQuadStrip(buf, ax, ay, az, bx, by, bz, perpX, perpY, perpZ, wA, wB, tA, tB, alphaA, alphaB);
                    emitQuadStrip(buf, ax, ay, az, bx, by, bz, perp2X, perp2Y, perp2Z, wA, wB, tA, tB, alphaA, alphaB);
                    vertexCount += 24;
                }
            }
            // Webbing: flat panels between adjacent cape strip centers
            emitCapeWebbing(buf);
        }

        if (profile.hood != null) {
            emitHood(buf);
        }

        if (vbo == null) {
            vbo = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }
        vbo.bind();
        vbo.upload(buf.end());
        VertexBuffer.unbind();

        needsUpload = false;
        return vbo;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void dispose() {
        if (vbo != null) {
            vbo.close();
            vbo = null;
        }
    }

    private void emitQuadStrip(BufferBuilder buf,
                               float ax, float ay, float az,
                               float bx, float by, float bz,
                               float px, float py, float pz,
                               float wA, float wB, float uvA, float uvB,
                               float alphaA, float alphaB) {
        float a0x = ax - px * wA, a0y = ay - py * wA, a0z = az - pz * wA;
        float a1x = ax + px * wA, a1y = ay + py * wA, a1z = az + pz * wA;
        float b0x = bx - px * wB, b0y = by - py * wB, b0z = bz - pz * wB;
        float b1x = bx + px * wB, b1y = by + py * wB, b1z = bz + pz * wB;

        emitVertex(buf, a0x, a0y, a0z, 0, uvA, alphaA, px, py, pz);
        emitVertex(buf, b0x, b0y, b0z, 0, uvB, alphaB, px, py, pz);
        emitVertex(buf, b1x, b1y, b1z, 1, uvB, alphaB, px, py, pz);
        emitVertex(buf, a0x, a0y, a0z, 0, uvA, alphaA, px, py, pz);
        emitVertex(buf, b1x, b1y, b1z, 1, uvB, alphaB, px, py, pz);
        emitVertex(buf, a1x, a1y, a1z, 1, uvA, alphaA, px, py, pz);

        emitVertex(buf, a0x, a0y, a0z, 0, uvA, alphaA, -px, -py, -pz);
        emitVertex(buf, b1x, b1y, b1z, 1, uvB, alphaB, -px, -py, -pz);
        emitVertex(buf, b0x, b0y, b0z, 0, uvB, alphaB, -px, -py, -pz);
        emitVertex(buf, a0x, a0y, a0z, 0, uvA, alphaA, -px, -py, -pz);
        emitVertex(buf, a1x, a1y, a1z, 1, uvA, alphaA, -px, -py, -pz);
        emitVertex(buf, b1x, b1y, b1z, 1, uvB, alphaB, -px, -py, -pz);
    }

    private static void emitVertex(BufferBuilder buf, float x, float y, float z,
                                   float u, float v, float alpha,
                                   float nx, float ny, float nz) {
        buf.vertex(x, y, z).uv(u, v).color(255, 255, 255, (int) (alpha * 255.0f)).normal(nx, ny, nz).endVertex();
    }

    private static float hash(long seed) {
        seed = seed * 6364136223846793005L + 1442695040888963407L;
        return ((seed >> 16) & 0x7FFF) / 32767.0f;
    }

    // ================================================================
    //  IK rotation + cape anchoring helpers
    // ================================================================

    /** Depth-based IK rotation: root segments follow the body, tips trail. */
    private static void applyIKRotation(float[] chain, int points,
                                        float cosY, float sinY, float cosP, float sinP) {
        for (int s = 1; s < points; s++) {
            int i = s * 3;
            float depthT = (float) s / (points - 1);
            float rotBlend = 1.0f - depthT * 0.85f;

            float x = chain[i], y = chain[i + 1], z = chain[i + 2];
            float rx = x * cosY + z * sinY;
            float rz = -x * sinY + z * cosY;
            float ry = y * cosP - rz * sinP;
            rz = y * sinP + rz * cosP;

            chain[i]     = x + (rx - x) * rotBlend;
            chain[i + 1] = y + (ry - y) * rotBlend;
            chain[i + 2] = z + (rz - z) * rotBlend;
        }
    }

    /** Snap each cape strip's root to its interpolated position on the hood rim. */
    private void anchorCapesToHoodRim() {
        FearMeshProfile.Hood h = profile.hood;
        int stride = h.cols + 1;
        int rimRow = h.rows;

        for (int t = 0; t < capeChains.length; t++) {
            float s = profile.capeStrips[t].arcPosition;

            float colF = s * h.cols;
            int c0 = Math.min((int) colF, h.cols - 1);
            int c1 = c0 + 1;
            float frac = colF - c0;

            int idx0 = (rimRow * stride + c0) * 3;
            int idx1 = (rimRow * stride + c1) * 3;

            capeChains[t][0] = Mth.lerp(frac, hoodGrid[idx0],     hoodGrid[idx1]);
            capeChains[t][1] = Mth.lerp(frac, hoodGrid[idx0 + 1], hoodGrid[idx1 + 1]);
            capeChains[t][2] = Mth.lerp(frac, hoodGrid[idx0 + 2], hoodGrid[idx1 + 2]);
        }
    }

    // ================================================================
    //  Cape webbing — flat panels between adjacent strip centers
    // ================================================================

    /**
     * Fills the gaps between adjacent cape strip chains with flat quad panels,
     * forming a continuous cape surface. Webbing only covers segments shared
     * by both strips — shorter edge strips end first, creating a natural
     * tattered/ragged bottom edge.
     */
    private void emitCapeWebbing(BufferBuilder buf) {
        if (capeChains == null || capeChains.length < 2) return;

        for (int t = 0; t < capeChains.length - 1; t++) {
            FearMeshProfile.CapeStrip csA = profile.capeStrips[t];
            FearMeshProfile.CapeStrip csB = profile.capeStrips[t + 1];
            float[] chainA = capeChains[t];
            float[] chainB = capeChains[t + 1];
            int minSegs = Math.min(csA.segments, csB.segments);

            for (int s = 0; s < minSegs; s++) {
                int cur = s * 3, nxt = (s + 1) * 3;

                float a0x = chainA[cur], a0y = chainA[cur+1], a0z = chainA[cur+2];
                float a1x = chainA[nxt], a1y = chainA[nxt+1], a1z = chainA[nxt+2];
                float b0x = chainB[cur], b0y = chainB[cur+1], b0z = chainB[cur+2];
                float b1x = chainB[nxt], b1y = chainB[nxt+1], b1z = chainB[nxt+2];

                // Depth-based alpha: average of both strips
                float tS = (float) s / minSegs;
                float tS1 = (float) (s + 1) / minSegs;
                float alphaS = (Mth.lerp(tS, csA.alphaStart, csA.alphaEnd)
                              + Mth.lerp(tS, csB.alphaStart, csB.alphaEnd)) * 0.5f;
                float alphaS1 = (Mth.lerp(tS1, csA.alphaStart, csA.alphaEnd)
                               + Mth.lerp(tS1, csB.alphaStart, csB.alphaEnd)) * 0.5f;

                // Normal from cross product of diagonals
                float e1x = b1x - a0x, e1y = b1y - a0y, e1z = b1z - a0z;
                float e2x = b0x - a1x, e2y = b0y - a1y, e2z = b0z - a1z;
                float nx = e1y * e2z - e1z * e2y;
                float ny = e1z * e2x - e1x * e2z;
                float nz = e1x * e2y - e1y * e2x;
                float nLen = Mth.sqrt(nx * nx + ny * ny + nz * nz);
                if (nLen < 0.0001f) continue;
                nx /= nLen; ny /= nLen; nz /= nLen;

                // Front face
                emitVertex(buf, a0x, a0y, a0z, 0, tS, alphaS, nx, ny, nz);
                emitVertex(buf, a1x, a1y, a1z, 0, tS1, alphaS1, nx, ny, nz);
                emitVertex(buf, b1x, b1y, b1z, 1, tS1, alphaS1, nx, ny, nz);
                emitVertex(buf, a0x, a0y, a0z, 0, tS, alphaS, nx, ny, nz);
                emitVertex(buf, b1x, b1y, b1z, 1, tS1, alphaS1, nx, ny, nz);
                emitVertex(buf, b0x, b0y, b0z, 1, tS, alphaS, nx, ny, nz);

                // Back face
                emitVertex(buf, a0x, a0y, a0z, 0, tS, alphaS, -nx, -ny, -nz);
                emitVertex(buf, b1x, b1y, b1z, 1, tS1, alphaS1, -nx, -ny, -nz);
                emitVertex(buf, a1x, a1y, a1z, 0, tS1, alphaS1, -nx, -ny, -nz);
                emitVertex(buf, a0x, a0y, a0z, 0, tS, alphaS, -nx, -ny, -nz);
                emitVertex(buf, b0x, b0y, b0z, 1, tS, alphaS, -nx, -ny, -nz);
                emitVertex(buf, b1x, b1y, b1z, 1, tS1, alphaS1, -nx, -ny, -nz);

                vertexCount += 12;
            }
        }
    }

    // ================================================================
    //  Hood geometry
    // ================================================================

    /**
     * Pre-compute per-vertex alpha. Edge fade near the face opening
     * transitions to transparent. Bottom rim stays opaque where cape
     * strips attach — no rim fade needed since cape webbing continues
     * the surface seamlessly.
     */
    private void initHoodAlphas() {
        FearMeshProfile.Hood h = profile.hood;
        int stride = h.cols + 1;

        for (int r = 0; r <= h.rows; r++) {
            for (int c = 0; c <= h.cols; c++) {
                float s = (float) c / h.cols;
                // Edge fade near opening edges (s≈0 and s≈1).
                float edgeDist = Math.min(s, 1.0f - s);
                float edgeFade = Mth.clamp(edgeDist / Math.max(h.edgeFadeWidth, 0.001f), 0.0f, 1.0f);
                edgeFade = edgeFade * edgeFade * (3.0f - 2.0f * edgeFade); // smoothstep

                hoodAlphas[r * stride + c] = h.alpha * edgeFade;
            }
        }
    }

    /**
     * Recompute hood vertex positions each tick with subtle breathing and ripple.
     * Positions are in mesh-local space; the poseStack in FearRenderer handles facing.
     */
    private void updateHood() {
        FearMeshProfile.Hood h = profile.hood;
        int stride = h.cols + 1;

        float openMinRad = (float) Math.toRadians(h.openAngleMinDeg);
        float openMaxRad = (float) Math.toRadians(h.openAngleMaxDeg);
        float PI = (float) Math.PI;

        for (int r = 0; r <= h.rows; r++) {
            float t = (float) r / h.rows; // 0 = peak, 1 = bottom rim

            float y = Mth.lerp(t, h.topY, h.bottomY);
            float baseRadius = (float) Math.pow(t, h.peakPower);
            float openHalf = Mth.lerp((float) Math.pow(t, h.archPower), openMinRad, openMaxRad);
            float lean = h.forwardLean * (1.0f - t);

            // Subtle breathing — radius pulse and Y sway.
            float breath = (float) Math.sin(time * 1.5f + t * 2.0f) * 0.008f * t;
            float ySway = (float) Math.sin(time * 1.1f + t * 1.3f) * 0.005f * t;

            // Arc spans from (π + openHalf) going around to (π - openHalf + 2π),
            // i.e. the full circle minus the front opening.
            float arcStart = PI + openHalf;
            float arcLength = 2.0f * PI - 2.0f * openHalf;

            for (int c = 0; c <= h.cols; c++) {
                float s = (float) c / h.cols;
                float theta = arcStart + s * arcLength;

                // Fabric ripple — small per-vertex perturbation.
                float ripple = (float) Math.sin(time * 2.5f + r * 1.3f + c * 0.7f) * 0.003f * t;

                float rX = (baseRadius + breath + ripple) * h.radiusX;
                float rZ = (baseRadius + breath + ripple) * h.radiusZ;

                float x = rX * (float) Math.sin(theta);
                float z = rZ * (float) Math.cos(theta) - lean;

                int idx = (r * stride + c) * 3;
                hoodGrid[idx]     = x;
                hoodGrid[idx + 1] = y + ySway;
                hoodGrid[idx + 2] = z;
            }
        }
    }

    /** Emit dual-sided triangles for the hood grid into the shared VBO. */
    private void emitHood(BufferBuilder buf) {
        FearMeshProfile.Hood h = profile.hood;
        int stride = h.cols + 1;

        for (int r = 0; r < h.rows; r++) {
            for (int c = 0; c < h.cols; c++) {
                int i00 = r * stride + c;
                int i01 = r * stride + c + 1;
                int i10 = (r + 1) * stride + c;
                int i11 = (r + 1) * stride + c + 1;

                int b00 = i00 * 3, b01 = i01 * 3, b10 = i10 * 3, b11 = i11 * 3;
                float x00 = hoodGrid[b00], y00 = hoodGrid[b00+1], z00 = hoodGrid[b00+2];
                float x01 = hoodGrid[b01], y01 = hoodGrid[b01+1], z01 = hoodGrid[b01+2];
                float x10 = hoodGrid[b10], y10 = hoodGrid[b10+1], z10 = hoodGrid[b10+2];
                float x11 = hoodGrid[b11], y11 = hoodGrid[b11+1], z11 = hoodGrid[b11+2];

                float a00 = hoodAlphas[i00], a01 = hoodAlphas[i01];
                float a10 = hoodAlphas[i10], a11 = hoodAlphas[i11];

                // Face normal via cross product of quad diagonals.
                float e1x = x11 - x00, e1y = y11 - y00, e1z = z11 - z00;
                float e2x = x01 - x10, e2y = y01 - y10, e2z = z01 - z10;
                float nx = e1y * e2z - e1z * e2y;
                float ny = e1z * e2x - e1x * e2z;
                float nz = e1x * e2y - e1y * e2x;
                float nLen = Mth.sqrt(nx * nx + ny * ny + nz * nz);
                if (nLen < 0.0001f) continue;
                nx /= nLen; ny /= nLen; nz /= nLen;

                float u0 = (float) c / h.cols, u1 = (float) (c + 1) / h.cols;
                float v0 = (float) r / h.rows, v1 = (float) (r + 1) / h.rows;

                // Front face
                emitVertex(buf, x00, y00, z00, u0, v0, a00, nx, ny, nz);
                emitVertex(buf, x10, y10, z10, u0, v1, a10, nx, ny, nz);
                emitVertex(buf, x11, y11, z11, u1, v1, a11, nx, ny, nz);
                emitVertex(buf, x00, y00, z00, u0, v0, a00, nx, ny, nz);
                emitVertex(buf, x11, y11, z11, u1, v1, a11, nx, ny, nz);
                emitVertex(buf, x01, y01, z01, u1, v0, a01, nx, ny, nz);

                // Back face
                emitVertex(buf, x00, y00, z00, u0, v0, a00, -nx, -ny, -nz);
                emitVertex(buf, x11, y11, z11, u1, v1, a11, -nx, -ny, -nz);
                emitVertex(buf, x10, y10, z10, u0, v1, a10, -nx, -ny, -nz);
                emitVertex(buf, x00, y00, z00, u0, v0, a00, -nx, -ny, -nz);
                emitVertex(buf, x01, y01, z01, u1, v0, a01, -nx, -ny, -nz);
                emitVertex(buf, x11, y11, z11, u1, v1, a11, -nx, -ny, -nz);

                vertexCount += 12;
            }
        }
    }
}
