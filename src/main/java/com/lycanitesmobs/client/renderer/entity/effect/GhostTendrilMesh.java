package com.lycanitesmobs.client.renderer.entity.effect;

import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.util.Mth;

/**
 * Procedural ghost mesh: tentacle-like tendrils forming a tattered wraith silhouette.
 * <p>
 * Orientation is handled externally by the renderer via {@code poseStack.mulPose()}
 * using atan2 on delta movement (same technique as arrows/tridents). This class
 * operates entirely in local space where -Y is "behind" the entity after rotation.
 * <p>
 * When the entity changes direction, existing chain segment positions are
 * counter-rotated by the facing delta so they appear to lag behind in the new
 * frame — creating a snake-like trailing effect without physics simulation.
 * <p>
 * <b>Moving:</b> tendrils stream behind (local -Y = world trailing direction).
 * <b>Idle:</b> tendrils droop with subtle turbulence.
 */
public class GhostTendrilMesh {

    // --- Tendril layout ---
    private static final int TENDRIL_COUNT = 12;
    private static final int SEGMENTS = 10;
    private static final float SEGMENT_LENGTH = 0.27F;
    private static final float SPAWN_RING_RADIUS = 0.25F;

    // --- Width taper ---
    private static final float BASE_WIDTH = 0.15F;
    private static final float TIP_WIDTH = 0.01F;

    // --- Follow-the-leader ---
    private static final float FOLLOW_STIFFNESS = 0.12F;
    private static final float DEPTH_LAG = 0.015F;

    // --- Sine perturbation for organic waviness ---
    private static final float WAVE_AMPLITUDE = 0.08F;
    private static final float WAVE_FREQUENCY = 2.5F;
    private static final float WAVE_PHASE_PER_TENDRIL = 0.55F;
    private static final float WAVE_PHASE_PER_SEGMENT = 0.4F;

    // --- Idle fire behavior ---
    private static final float IDLE_INWARD_STRENGTH = 0.015F;
    private static final float IDLE_TURBULENCE = 0.06F;

    // --- Orientation smoothing ---
    private static final float YAW_SMOOTHING = 0.25F;
    private static final float PITCH_SMOOTHING = 0.25F;

    // Chain positions: [tendril][segment * 3 + {x,y,z}]
    private final float[][] chains;
    private final float[] tendrilAngles;
    private final float[] tendrilLengthScale;
    private final float[] tendrilWidthScale;

    private VertexBuffer vbo;
    private int vertexCount;
    private boolean needsUpload = true;
    private float time;

    // Smoothed yaw/pitch (degrees) — exposed for the renderer's poseStack
    private float smoothYaw;
    private float smoothPitch;
    // Previous frame's smoothed values for computing deltas
    private float prevYaw;
    private float prevPitch;
    private boolean hasOrientation = false;

    private float smoothSpeed;

    public GhostTendrilMesh() {
        this.chains = new float[TENDRIL_COUNT][];
        this.tendrilAngles = new float[TENDRIL_COUNT];
        this.tendrilLengthScale = new float[TENDRIL_COUNT];
        this.tendrilWidthScale = new float[TENDRIL_COUNT];

        for (int t = 0; t < TENDRIL_COUNT; t++) {
            float angle = (float) (t * Math.PI * 2.0 / TENDRIL_COUNT);
            angle += hash(t * 73) * 0.3F;
            this.tendrilAngles[t] = angle;
            this.tendrilLengthScale[t] = 0.7F + hash(t * 137) * 0.6F;
            this.tendrilWidthScale[t] = 0.6F + hash(t * 211) * 0.8F;

            int points = SEGMENTS + 1;
            float[] chain = new float[points * 3];
            float rx = (float) Math.cos(angle) * SPAWN_RING_RADIUS;
            float rz = (float) Math.sin(angle) * SPAWN_RING_RADIUS;
            for (int s = 0; s < points; s++) {
                int b = s * 3;
                chain[b] = rx;
                chain[b + 1] = -s * SEGMENT_LENGTH * this.tendrilLengthScale[t];
                chain[b + 2] = rz;
            }
            this.chains[t] = chain;
        }
    }

    /**
     * Advance the animation by one frame.
     *
     * @param rawYaw   raw yaw in degrees from atan2 on delta movement
     * @param rawPitch raw pitch in degrees from atan2 on delta movement
     * @param speed    horizontal speed magnitude
     * @param partialTick render partial tick
     */
    public void tick(float rawYaw, float rawPitch, float speed, float partialTick) {
        this.time += 0.05F + partialTick * 0.005F;

        // Smooth speed
        this.smoothSpeed += (speed - this.smoothSpeed) * 0.35F;

        // --- Smooth yaw/pitch with angle wrapping ---
        if (!this.hasOrientation) {
            this.smoothYaw = rawYaw;
            this.smoothPitch = rawPitch;
            this.prevYaw = rawYaw;
            this.prevPitch = rawPitch;
            this.hasOrientation = true;
        } else {
            // Wrap yaw delta to [-180, 180]
            float yawDelta = rawYaw - this.smoothYaw;
            while (yawDelta > 180.0F) yawDelta -= 360.0F;
            while (yawDelta < -180.0F) yawDelta += 360.0F;
            this.smoothYaw += yawDelta * YAW_SMOOTHING;

            float pitchDelta = rawPitch - this.smoothPitch;
            this.smoothPitch += pitchDelta * PITCH_SMOOTHING;
        }

        // --- Counter-rotate chain segments by facing delta (snake trailing) ---
        float dYaw = this.smoothYaw - this.prevYaw;
        float dPitch = this.smoothPitch - this.prevPitch;

        if (Math.abs(dYaw) > 0.001F || Math.abs(dPitch) > 0.001F) {
            float yawRad = (float) Math.toRadians(-dYaw);
            float pitchRad = (float) Math.toRadians(-dPitch);
            float cosY = (float) Math.cos(yawRad);
            float sinY = (float) Math.sin(yawRad);
            float cosP = (float) Math.cos(pitchRad);
            float sinP = (float) Math.sin(pitchRad);

            for (int t = 0; t < TENDRIL_COUNT; t++) {
                float[] chain = this.chains[t];
                int points = SEGMENTS + 1;
                for (int s = 1; s < points; s++) {
                    int i = s * 3;
                    float x = chain[i], y = chain[i + 1], z = chain[i + 2];

                    // Rotate around Y axis (yaw)
                    float rx = x * cosY + z * sinY;
                    float rz = -x * sinY + z * cosY;

                    // Rotate around X axis (pitch)
                    float ry = y * cosP - rz * sinP;
                    rz = y * sinP + rz * cosP;

                    chain[i] = rx;
                    chain[i + 1] = ry;
                    chain[i + 2] = rz;
                }
            }
        }

        this.prevYaw = this.smoothYaw;
        this.prevPitch = this.smoothPitch;

        // --- Idle/moving blend ---
        float idleBlend = Mth.clamp(1.0F - this.smoothSpeed * 40.0F, 0.0F, 1.0F);

        // --- Update chains ---
        for (int t = 0; t < TENDRIL_COUNT; t++) {
            float[] chain = this.chains[t];
            float angle = this.tendrilAngles[t];
            float lenScale = this.tendrilLengthScale[t];
            int points = SEGMENTS + 1;

            // Root position: ring in XZ plane (local space, -Y is trailing)
            float rootWobble = (float) Math.sin(this.time * 1.8F + t * 2.1F) * 0.04F;
            float ringR = SPAWN_RING_RADIUS + rootWobble;
            chain[0] = (float) Math.cos(angle) * ringR;
            chain[1] = (float) Math.sin(this.time * 1.2F + t * 1.7F) * 0.03F;
            chain[2] = (float) Math.sin(angle) * ringR;

            // Follow-the-leader for each segment
            for (int s = 1; s < points; s++) {
                int cur = s * 3;
                int par = (s - 1) * 3;
                float segLen = SEGMENT_LENGTH * lenScale;
                float depthT = (float) s / SEGMENTS;

                float cx = chain[cur], cy = chain[cur + 1], cz = chain[cur + 2];
                float px = chain[par], py = chain[par + 1], pz = chain[par + 2];

                // Idle: slight inward curl with turbulence
                float idleInX = -chain[cur] * IDLE_INWARD_STRENGTH;
                float idleInZ = -chain[cur + 2] * IDLE_INWARD_STRENGTH;

                // Sine wave for organic motion
                float wavePhase = this.time * WAVE_FREQUENCY
                        + t * WAVE_PHASE_PER_TENDRIL
                        + s * WAVE_PHASE_PER_SEGMENT;
                float waveX = (float) Math.sin(wavePhase) * WAVE_AMPLITUDE * depthT;
                float waveZ = (float) Math.cos(wavePhase * 0.7F + 1.3F) * WAVE_AMPLITUDE * depthT;
                float waveY = (float) Math.sin(wavePhase * 0.5F + 2.7F) * WAVE_AMPLITUDE * 0.3F * depthT;

                // Idle turbulence
                float turbPhase = this.time * 4.5F + t * 3.7F + s * 1.9F;
                float turbX = (float) Math.sin(turbPhase) * IDLE_TURBULENCE * depthT;
                float turbZ = (float) Math.cos(turbPhase * 1.3F) * IDLE_TURBULENCE * depthT;
                float turbY = (float) Math.sin(turbPhase * 0.8F + 1.0F) * IDLE_TURBULENCE * 0.5F * depthT;

                // Direction: gravity (-Y in local space = trailing behind after poseStack rotation)
                // plus idle turbulence and sine waves for organic feel
                float dirX = (idleInX + turbX) * idleBlend + waveX;
                float dirY = -0.08F + turbY * idleBlend + waveY;
                float dirZ = (idleInZ + turbZ) * idleBlend + waveZ;

                float dirLen = Mth.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
                if (dirLen < 0.001F) {
                    dirX = 0;
                    dirY = -1;
                    dirZ = 0;
                    dirLen = 1;
                }
                float targetX = px + (dirX / dirLen) * segLen;
                float targetY = py + (dirY / dirLen) * segLen;
                float targetZ = pz + (dirZ / dirLen) * segLen;

                float stiffness = FOLLOW_STIFFNESS - depthT * DEPTH_LAG;
                stiffness = Math.max(stiffness, 0.03F);
                chain[cur] = cx + (targetX - cx) * stiffness;
                chain[cur + 1] = cy + (targetY - cy) * stiffness;
                chain[cur + 2] = cz + (targetZ - cz) * stiffness;

                // Hard distance constraint
                float dx = chain[cur] - chain[par];
                float dy = chain[cur + 1] - chain[par + 1];
                float dz = chain[cur + 2] - chain[par + 2];
                float dist = Mth.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > segLen * 1.5F && dist > 0.001F) {
                    float scale = segLen * 1.5F / dist;
                    chain[cur] = chain[par] + dx * scale;
                    chain[cur + 1] = chain[par + 1] + dy * scale;
                    chain[cur + 2] = chain[par + 2] + dz * scale;
                }
            }
        }

        this.needsUpload = true;
    }

    /** Smoothed yaw for the renderer's poseStack rotation. */
    public float getSmoothedYaw() {
        return this.smoothYaw;
    }

    /** Smoothed pitch for the renderer's poseStack rotation. */
    public float getSmoothedPitch() {
        return this.smoothPitch;
    }

    /**
     * Builds vertex data from chain positions and uploads to VBO.
     */
    public VertexBuffer getVbo() {
        if (!this.needsUpload && this.vbo != null) return this.vbo;

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, CustomRenderStates.POS_TEX_NORMAL);

        this.vertexCount = 0;

        for (int t = 0; t < TENDRIL_COUNT; t++) {
            float[] chain = this.chains[t];
            float widthScale = this.tendrilWidthScale[t];

            for (int s = 0; s < SEGMENTS; s++) {
                int a = s * 3;
                int b = (s + 1) * 3;

                float ax = chain[a], ay = chain[a + 1], az = chain[a + 2];
                float bx = chain[b], by = chain[b + 1], bz = chain[b + 2];

                float dx = bx - ax, dy = by - ay, dz = bz - az;
                float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
                if (len < 0.0001F) continue;
                dx /= len; dy /= len; dz /= len;

                float refX, refY, refZ;
                if (Math.abs(dy) > 0.95F) {
                    refX = 0; refY = 0; refZ = 1;
                } else {
                    refX = 0; refY = 1; refZ = 0;
                }
                float perpX = dy * refZ - dz * refY;
                float perpY = dz * refX - dx * refZ;
                float perpZ = dx * refY - dy * refX;
                float perpLen = Mth.sqrt(perpX * perpX + perpY * perpY + perpZ * perpZ);
                if (perpLen < 0.0001F) continue;
                perpX /= perpLen; perpY /= perpLen; perpZ /= perpLen;

                float perp2X = dy * perpZ - dz * perpY;
                float perp2Y = dz * perpX - dx * perpZ;
                float perp2Z = dx * perpY - dy * perpX;

                float tA = (float) s / SEGMENTS;
                float tB = (float) (s + 1) / SEGMENTS;
                float wA = Mth.lerp(tA, BASE_WIDTH, TIP_WIDTH) * widthScale;
                float wB = Mth.lerp(tB, BASE_WIDTH, TIP_WIDTH) * widthScale;

                emitQuadStrip(buf, ax, ay, az, bx, by, bz, perpX, perpY, perpZ, wA, wB, tA, tB);
                emitQuadStrip(buf, ax, ay, az, bx, by, bz, perp2X, perp2Y, perp2Z, wA, wB, tA, tB);
                this.vertexCount += 24;
            }
        }

        if (this.vbo == null) {
            this.vbo = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }
        this.vbo.bind();
        this.vbo.upload(buf.end());
        VertexBuffer.unbind();

        this.needsUpload = false;
        return this.vbo;
    }

    private void emitQuadStrip(BufferBuilder buf,
                               float ax, float ay, float az,
                               float bx, float by, float bz,
                               float px, float py, float pz,
                               float wA, float wB, float uvA, float uvB) {
        float a0x = ax - px * wA, a0y = ay - py * wA, a0z = az - pz * wA;
        float a1x = ax + px * wA, a1y = ay + py * wA, a1z = az + pz * wA;
        float b0x = bx - px * wB, b0y = by - py * wB, b0z = bz - pz * wB;
        float b1x = bx + px * wB, b1y = by + py * wB, b1z = bz + pz * wB;

        emitVertex(buf, a0x, a0y, a0z, 0, uvA, px, py, pz);
        emitVertex(buf, b0x, b0y, b0z, 0, uvB, px, py, pz);
        emitVertex(buf, b1x, b1y, b1z, 1, uvB, px, py, pz);
        emitVertex(buf, a0x, a0y, a0z, 0, uvA, px, py, pz);
        emitVertex(buf, b1x, b1y, b1z, 1, uvB, px, py, pz);
        emitVertex(buf, a1x, a1y, a1z, 1, uvA, px, py, pz);

        emitVertex(buf, a0x, a0y, a0z, 0, uvA, -px, -py, -pz);
        emitVertex(buf, b1x, b1y, b1z, 1, uvB, -px, -py, -pz);
        emitVertex(buf, b0x, b0y, b0z, 0, uvB, -px, -py, -pz);
        emitVertex(buf, a0x, a0y, a0z, 0, uvA, -px, -py, -pz);
        emitVertex(buf, a1x, a1y, a1z, 1, uvA, -px, -py, -pz);
        emitVertex(buf, b1x, b1y, b1z, 1, uvB, -px, -py, -pz);
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public void dispose() {
        if (this.vbo != null) {
            this.vbo.close();
            this.vbo = null;
        }
    }

    private static void emitVertex(BufferBuilder buf, float x, float y, float z,
                                   float u, float v, float nx, float ny, float nz) {
        buf.vertex(x, y, z).uv(u, v).normal(nx, ny, nz).endVertex();
    }

    private static float hash(long seed) {
        seed = seed * 6364136223846793005L + 1442695040888963407L;
        return ((seed >> 16) & 0x7FFF) / 32767.0F;
    }
}
