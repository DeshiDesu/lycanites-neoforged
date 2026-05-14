package com.lycanitesmobs.client.effect;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Optional;

/**
 * Pre-computed amplitude envelope of the heartbeat OGG file.
 * <p>
 * On first access, decodes {@code sounds/effect/heartbeat.ogg} to PCM via STBVorbis,
 * computes RMS amplitude in 20ms windows, and normalizes to a 0–1 float array.
 * This allows the flicker system to query the exact loudness at any point in the
 * heartbeat loop, producing light pulses that are perfectly synchronized with beats.
 * <p>
 * Playback-time lookups account for pitch scaling (faster pitch = faster traversal)
 * and wrap around the duration for seamless looping.
 */
@OnlyIn(Dist.CLIENT)
public class HeartbeatAmplitudeMap {

    private static final float WINDOW_SECONDS = 0.02F;

    private static float[] envelope = null;
    private static float duration = 0.0F;
    private static float envelopeRate = 0.0F;
    private static boolean loaded = false;
    private static boolean loadFailed = false;

    public static void ensureLoaded() {
        if (loaded || loadFailed) return;
        try {
            decode();
            loaded = true;
            LycanitesMobs.LOGGER.info("HeartbeatAmplitudeMap: decoded envelope — "
                    + envelope.length + " samples, " + String.format("%.2f", duration) + "s duration");
        } catch (Exception e) {
            loadFailed = true;
            LycanitesMobs.LOGGER.warn("HeartbeatAmplitudeMap: failed to decode heartbeat.ogg", e);
        }
    }

    private static void decode() throws Exception {
        ResourceLocation loc = new ResourceLocation(LycanitesMobs.MODID, "sounds/effect/heartbeat.ogg");
        Optional<Resource> optResource = Minecraft.getInstance().getResourceManager().getResource(loc);
        if (optResource.isEmpty()) {
            throw new RuntimeException("heartbeat.ogg not found at " + loc);
        }

        byte[] oggBytes;
        try (InputStream is = optResource.get().open()) {
            oggBytes = is.readAllBytes();
        }

        ByteBuffer oggBuffer = MemoryUtil.memAlloc(oggBytes.length);
        try {
            oggBuffer.put(oggBytes);
            oggBuffer.flip();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer error = stack.mallocInt(1);

                long handle = STBVorbis.stb_vorbis_open_memory(oggBuffer, error, null);
                if (handle == 0) {
                    throw new RuntimeException("STBVorbis failed to open heartbeat.ogg, error code: " + error.get(0));
                }

                try {
                    STBVorbisInfo info = STBVorbisInfo.malloc(stack);
                    STBVorbis.stb_vorbis_get_info(handle, info);

                    int channels = info.channels();
                    int sampleRate = info.sample_rate();
                    int totalSamples = STBVorbis.stb_vorbis_stream_length_in_samples(handle);

                    ShortBuffer pcm = MemoryUtil.memAllocShort(totalSamples * channels);
                    try {
                        int framesRead = STBVorbis.stb_vorbis_get_samples_short_interleaved(handle, channels, pcm);
                        int totalFrames = framesRead;
                        int windowFrames = (int) (WINDOW_SECONDS * sampleRate);
                        int windowCount = totalFrames / windowFrames;

                        envelope = new float[windowCount];
                        envelopeRate = 1.0F / WINDOW_SECONDS;
                        duration = (float) totalFrames / sampleRate;

                        float maxRms = 0.0F;
                        for (int w = 0; w < windowCount; w++) {
                            double sumSq = 0.0;
                            int baseIndex = w * windowFrames * channels;
                            for (int f = 0; f < windowFrames; f++) {
                                for (int c = 0; c < channels; c++) {
                                    float sample = pcm.get(baseIndex + f * channels + c) / 32768.0F;
                                    sumSq += sample * sample;
                                }
                            }
                            float rms = (float) Math.sqrt(sumSq / (windowFrames * channels));
                            envelope[w] = rms;
                            if (rms > maxRms) maxRms = rms;
                        }

                        if (maxRms > 0.0F) {
                            for (int i = 0; i < envelope.length; i++) {
                                envelope[i] /= maxRms;
                            }
                        }
                    } finally {
                        MemoryUtil.memFree(pcm);
                    }
                } finally {
                    STBVorbis.stb_vorbis_close(handle);
                }
            }
        } finally {
            MemoryUtil.memFree(oggBuffer);
        }
    }

    public static float getAmplitude(float audioTimeSeconds) {
        if (envelope == null || envelope.length == 0) return 0.0F;

        float wrapped = audioTimeSeconds % duration;
        if (wrapped < 0.0F) wrapped += duration;

        float indexF = wrapped * envelopeRate;
        int i0 = (int) indexF;
        float frac = indexF - i0;

        i0 = Math.min(i0, envelope.length - 1);
        int i1 = Math.min(i0 + 1, envelope.length - 1);

        return envelope[i0] + (envelope[i1] - envelope[i0]) * frac;
    }

    public static float getDuration() {
        return duration;
    }

    public static boolean isReady() {
        return loaded && envelope != null;
    }
}
