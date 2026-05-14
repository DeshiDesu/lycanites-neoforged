package com.lycanitesmobs.client.effect;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A delegating wrapper around a {@link SoundInstance} that applies volume and pitch
 * scaling for fear audio muffling. All other properties are forwarded unchanged.
 * <p>
 * Use the static {@link #wrap(SoundInstance, float, float)} factory to automatically
 * produce the correct subtype (regular or {@link TickableSoundInstance}).
 */
@OnlyIn(Dist.CLIENT)
public class MuffledSoundInstance implements SoundInstance {

    protected final SoundInstance wrapped;
    protected final float volumeScale;
    protected final float pitchScale;

    protected MuffledSoundInstance(SoundInstance wrapped, float volumeScale, float pitchScale) {
        this.wrapped = wrapped;
        this.volumeScale = volumeScale;
        this.pitchScale = pitchScale;
    }

    /**
     * Factory method that wraps a sound instance with muffled volume and pitch.
     * If the original sound is a {@link TickableSoundInstance}, the returned wrapper
     * also implements that interface so the sound engine can tick it normally.
     *
     * @param sound       The original sound instance to wrap.
     * @param volumeScale Volume multiplier (0 = silent, 1 = original volume).
     * @param pitchScale  Pitch multiplier (< 1 = lower pitch).
     * @return A muffled wrapper around the original sound.
     */
    public static SoundInstance wrap(SoundInstance sound, float volumeScale, float pitchScale) {
        if (sound instanceof TickableSoundInstance tickable) {
            return new MuffledTickable(tickable, volumeScale, pitchScale);
        }
        return new MuffledSoundInstance(sound, volumeScale, pitchScale);
    }

    @Override
    public float getVolume() {
        return wrapped.getVolume() * volumeScale;
    }

    @Override
    public float getPitch() {
        return wrapped.getPitch() * pitchScale;
    }

    @Override
    public ResourceLocation getLocation() {
        return wrapped.getLocation();
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {
        return wrapped.resolve(manager);
    }

    @Override
    public Attenuation getAttenuation() {
        return wrapped.getAttenuation();
    }

    @Override
    public boolean isLooping() {
        return wrapped.isLooping();
    }

    @Override
    public boolean isRelative() {
        return wrapped.isRelative();
    }

    @Override
    public int getDelay() {
        return wrapped.getDelay();
    }

    @Override
    public SoundSource getSource() {
        return wrapped.getSource();
    }

    @Override
    public double getX() {
        return wrapped.getX();
    }

    @Override
    public double getY() {
        return wrapped.getY();
    }

    @Override
    public double getZ() {
        return wrapped.getZ();
    }

    @Override
    public Sound getSound() {
        return wrapped.getSound();
    }

    /**
     * Tickable variant that delegates {@code tick()} and {@code isStopped()} to the
     * wrapped {@link TickableSoundInstance}, ensuring looping and positional sounds
     * continue to update correctly while muffled.
     */
    private static class MuffledTickable extends MuffledSoundInstance implements TickableSoundInstance {

        private final TickableSoundInstance tickableWrapped;

        MuffledTickable(TickableSoundInstance wrapped, float volumeScale, float pitchScale) {
            super(wrapped, volumeScale, pitchScale);
            this.tickableWrapped = wrapped;
        }

        @Override
        public boolean isStopped() {
            return tickableWrapped.isStopped();
        }

        @Override
        public void tick() {
            tickableWrapped.tick();
        }
    }
}
