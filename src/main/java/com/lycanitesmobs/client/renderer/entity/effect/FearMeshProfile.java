package com.lycanitesmobs.client.renderer.entity.effect;

import java.util.ArrayList;
import java.util.List;

public class FearMeshProfile {

    public static class Strip {
        public final float anchorX, anchorY, anchorZ;
        public final int segments;
        public final float segmentLength;
        public final float widthStart;
        public final float widthEnd;
        public final float stiffness;
        public final float depthLag;
        public final float waveScale;
        public final float taperIn;
        public final float alphaStart;
        public final float alphaEnd;

        public Strip(float anchorX, float anchorY, float anchorZ,
                     int segments, float segmentLength,
                     float widthStart, float widthEnd,
                     float stiffness, float depthLag, float waveScale,
                     float taperIn, float alphaStart, float alphaEnd) {
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.anchorZ = anchorZ;
            this.segments = segments;
            this.segmentLength = segmentLength;
            this.widthStart = widthStart;
            this.widthEnd = widthEnd;
            this.stiffness = stiffness;
            this.depthLag = depthLag;
            this.waveScale = waveScale;
            this.taperIn = taperIn;
            this.alphaStart = alphaStart;
            this.alphaEnd = alphaEnd;
        }
    }

    public static class CapeStrip {
        public final float arcPosition;
        public final int segments;
        public final float segmentLength;
        public final float widthStart;
        public final float widthEnd;
        public final float stiffness;
        public final float depthLag;
        public final float waveScale;
        public final float alphaStart;
        public final float alphaEnd;

        public CapeStrip(float arcPosition, int segments, float segmentLength,
                         float widthStart, float widthEnd,
                         float stiffness, float depthLag, float waveScale,
                         float alphaStart, float alphaEnd) {
            this.arcPosition = arcPosition;
            this.segments = segments;
            this.segmentLength = segmentLength;
            this.widthStart = widthStart;
            this.widthEnd = widthEnd;
            this.stiffness = stiffness;
            this.depthLag = depthLag;
            this.waveScale = waveScale;
            this.alphaStart = alphaStart;
            this.alphaEnd = alphaEnd;
        }
    }

    public static class Hood {
        public final float topY;
        public final float bottomY;
        public final float radiusX;
        public final float radiusZ;
        public final float openAngleMinDeg;
        public final float openAngleMaxDeg;
        public final float archPower;
        public final float peakPower;
        public final int rows;
        public final int cols;
        public final float alpha;
        public final float edgeFadeWidth;
        public final float forwardLean;

        public Hood(float topY, float bottomY, float radiusX, float radiusZ,
                    float openAngleMinDeg, float openAngleMaxDeg,
                    float archPower, float peakPower,
                    int rows, int cols, float alpha, float edgeFadeWidth,
                    float forwardLean) {
            this.topY = topY;
            this.bottomY = bottomY;
            this.radiusX = radiusX;
            this.radiusZ = radiusZ;
            this.openAngleMinDeg = openAngleMinDeg;
            this.openAngleMaxDeg = openAngleMaxDeg;
            this.archPower = archPower;
            this.peakPower = peakPower;
            this.rows = rows;
            this.cols = cols;
            this.alpha = alpha;
            this.edgeFadeWidth = edgeFadeWidth;
            this.forwardLean = forwardLean;
        }
    }

    public final Strip[] strips;
    public final CapeStrip[] capeStrips;
    public final Hood hood;
    public final float yawSmoothing;
    public final float pitchSmoothing;
    public final float speedSmoothing;
    public final float idleInwardStrength;
    public final float idleTurbulence;
    public final float waveAmplitude;
    public final float waveFrequency;

    public FearMeshProfile(Strip[] strips, CapeStrip[] capeStrips, Hood hood,
                           float yawSmoothing, float pitchSmoothing,
                           float speedSmoothing, float idleInwardStrength, float idleTurbulence,
                           float waveAmplitude, float waveFrequency) {
        this.strips = strips;
        this.capeStrips = capeStrips;
        this.hood = hood;
        this.yawSmoothing = yawSmoothing;
        this.pitchSmoothing = pitchSmoothing;
        this.speedSmoothing = speedSmoothing;
        this.idleInwardStrength = idleInwardStrength;
        this.idleTurbulence = idleTurbulence;
        this.waveAmplitude = waveAmplitude;
        this.waveFrequency = waveFrequency;
    }

    public static float T_HOOD_TOP_Y = 0.5f;
    public static float T_HOOD_BOTTOM_Y = -0.45f;
    public static float T_HOOD_RADIUS_X = 0.55f;
    public static float T_HOOD_RADIUS_Z = 0.60f;
    public static float T_HOOD_OPEN_MIN = 6.0f;
    public static float T_HOOD_OPEN_MAX = 75.0f;
    public static float T_HOOD_ARCH_POWER = 0.50f;
    public static float T_HOOD_PEAK_POWER = 0.55f;
    public static int T_HOOD_ROWS = 8;
    public static int T_HOOD_COLS = 14;
    public static float T_HOOD_ALPHA = 0.01f;
    public static float T_HOOD_EDGE_FADE = 1f;
    public static float T_HOOD_FORWARD_LEAN = 0.12f;

    public static int T_CAPE_COUNT = 9;
    public static int T_CAPE_BASE_SEGS = 20;
    public static float T_CAPE_SEG_LEN = 0.28f;
    public static float T_CAPE_W_START = 0.35f;
    public static float T_CAPE_W_END = 0.05f;
    public static float T_CAPE_STIFFNESS = 0.09f;
    public static float T_CAPE_DEPTH_LAG = 0.012f;
    public static float T_CAPE_WAVE_SCALE = 0.85f;
    public static float T_CAPE_ALPHA_START = 0.05f;
    public static float T_CAPE_ALPHA_END = 0.0f;
    public static float T_CAPE_MARGIN = 0.08f;
    public static float T_CAPE_EDGE_SEG_RED = 6.0f;
    public static float T_CAPE_EDGE_W_RED = 0.25f;

    public static int T_R1_COUNT = 8;
    public static float T_R1_RADIUS = 0.12f;
    public static float T_R1_Y_OFF = -0.15f;
    public static int T_R1_SEGS = 6;
    public static float T_R1_SEG_LEN = 0.18f;
    public static float T_R1_W_START = 0.14f;
    public static float T_R1_W_END = 0.03f;
    public static float T_R1_STIFFNESS = 0.35f;
    public static float T_R1_DEPTH_LAG = 0.005f;
    public static float T_R1_WAVE_SCALE = 0.3f;
    public static float T_R1_TAPER_IN = 0.35f;
    public static float T_R1_ALPHA_START = 0.03f;
    public static float T_R1_ALPHA_END = 0.0f;

    public static int T_R2_COUNT = 8;
    public static float T_R2_RADIUS = 0.22f;
    public static float T_R2_Y_OFF = -0.30f;
    public static int T_R2_SEGS = 10;
    public static float T_R2_SEG_LEN = 0.24f;
    public static float T_R2_W_START = 0.18f;
    public static float T_R2_W_END = 0.04f;
    public static float T_R2_STIFFNESS = 0.2f;
    public static float T_R2_DEPTH_LAG = 0.01f;
    public static float T_R2_WAVE_SCALE = 0.5f;
    public static float T_R2_TAPER_IN = 0.25f;
    public static float T_R2_ALPHA_START = 0.03f;
    public static float T_R2_ALPHA_END = 0.0f;

    public static float T_YAW_SMOOTH = 0.45f;
    public static float T_PITCH_SMOOTH = 0.40f;
    public static float T_SPEED_SMOOTH = 0.35f;
    public static float T_IDLE_INWARD = 0.015f;
    public static float T_IDLE_TURB = 0.06f;
    public static float T_WAVE_AMP = 0.08f;
    public static float T_WAVE_FREQ = 2.5f;

    // ================================================================
    //  FACTORY — reads from tuning statics
    // ================================================================

    public static FearMeshProfile dementor() {
        List<Strip> strips = new ArrayList<>();
        addRing(strips, T_R1_COUNT, T_R1_RADIUS, T_R1_Y_OFF, T_R1_SEGS, T_R1_SEG_LEN,
                T_R1_W_START, T_R1_W_END, T_R1_STIFFNESS, T_R1_DEPTH_LAG,
                T_R1_WAVE_SCALE, T_R1_TAPER_IN, 73, T_R1_ALPHA_START, T_R1_ALPHA_END);
        addRing(strips, T_R2_COUNT, T_R2_RADIUS, T_R2_Y_OFF, T_R2_SEGS, T_R2_SEG_LEN,
                T_R2_W_START, T_R2_W_END, T_R2_STIFFNESS, T_R2_DEPTH_LAG,
                T_R2_WAVE_SCALE, T_R2_TAPER_IN, 137, T_R2_ALPHA_START, T_R2_ALPHA_END);

        Hood hood = new Hood(
                T_HOOD_TOP_Y, T_HOOD_BOTTOM_Y,
                T_HOOD_RADIUS_X, T_HOOD_RADIUS_Z,
                T_HOOD_OPEN_MIN, T_HOOD_OPEN_MAX,
                T_HOOD_ARCH_POWER, T_HOOD_PEAK_POWER,
                T_HOOD_ROWS, T_HOOD_COLS,
                T_HOOD_ALPHA, T_HOOD_EDGE_FADE,
                T_HOOD_FORWARD_LEAN
        );

        List<CapeStrip> capeStrips = new ArrayList<>();
        addCapeStrips(capeStrips, T_CAPE_COUNT, T_CAPE_BASE_SEGS, T_CAPE_SEG_LEN,
                T_CAPE_W_START, T_CAPE_W_END,
                T_CAPE_STIFFNESS, T_CAPE_DEPTH_LAG, T_CAPE_WAVE_SCALE,
                T_CAPE_ALPHA_START, T_CAPE_ALPHA_END, 397,
                T_CAPE_MARGIN, T_CAPE_EDGE_SEG_RED, T_CAPE_EDGE_W_RED);

        return new FearMeshProfile(
                strips.toArray(new Strip[0]),
                capeStrips.toArray(new CapeStrip[0]),
                hood,
                T_YAW_SMOOTH, T_PITCH_SMOOTH, T_SPEED_SMOOTH,
                T_IDLE_INWARD, T_IDLE_TURB,
                T_WAVE_AMP, T_WAVE_FREQ
        );
    }

    private static void addRing(List<Strip> strips, int count, float radius, float yOffset,
                                int segments, float segLen, float wStart, float wEnd,
                                float stiffness, float depthLag, float waveScale,
                                float taperIn, int seed, float alphaStart, float alphaEnd) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (i * Math.PI * 2.0 / count) + hash(i * seed) * 0.3f;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            strips.add(new Strip(x, yOffset, z, segments, segLen, wStart, wEnd,
                    stiffness, depthLag, waveScale, taperIn, alphaStart, alphaEnd));
        }
    }

    private static void addCapeStrips(List<CapeStrip> strips, int count,
                                      int baseSegments, float segLen,
                                      float wStart, float wEnd,
                                      float stiffness, float depthLag, float waveScale,
                                      float alphaStart, float alphaEnd, int seed,
                                      float margin, float edgeSegRed, float edgeWRed) {
        float span = 1.0f - 2.0f * margin;

        for (int i = 0; i < count; i++) {
            float s = margin + span * i / Math.max(count - 1, 1);
            s += (hash(i * seed) - 0.5f) * 0.03f;
            s = Math.max(0.02f, Math.min(0.98f, s));

            float centerDist = Math.abs(s - 0.5f) * 2.0f;

            int segs = baseSegments - (int) (centerDist * edgeSegRed);
            float w = wStart * (1.0f - centerDist * edgeWRed);

            strips.add(new CapeStrip(s, segs, segLen, w, wEnd,
                    stiffness, depthLag, waveScale, alphaStart, alphaEnd));
        }
    }

    private static float hash(long seed) {
        seed = seed * 6364136223846793005L + 1442695040888963407L;
        return ((seed >> 16) & 0x7FFF) / 32767.0f;
    }
}
