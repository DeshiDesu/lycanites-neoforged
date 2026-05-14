package com.lycanitesmobs.client.effect;

import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Looping, tickable sound instance for the fear heartbeat.
 * <p>
 * Plays at the player's ear (relative positioning) with pitch set by the
 * combined {@code fearHeartbeatSpeed} config and fear amplifier level.
 * OpenAL handles seamless looping at the audio level — no retrigger
 * scheduling is needed. Pitch controls both playback speed and loop rate.
 * <p>
 * Marks itself stopped when the fear effect is removed. Exempt from global
 * muffling. Pitch can be updated live when the fear amplifier changes.
 */
@OnlyIn(Dist.CLIENT)
public class FearHeartbeatSound extends AbstractSoundInstance implements TickableSoundInstance {

    private static final float BASE_VOLUME = 2F;

    private boolean stopped = false;
    private float speedPitch;

    public FearHeartbeatSound(SoundEvent sound, float speedPitch) {
        super(sound, SoundSource.AMBIENT, Minecraft.getInstance().level.getRandom());
        this.looping = true;
        this.relative = true;
        this.delay = 0;
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
        this.volume = BASE_VOLUME;
        this.speedPitch = Mth.clamp(speedPitch, 0.1F, 4.0F);
        this.pitch = this.speedPitch;
    }

    public float getSpeedPitch() {
        return speedPitch;
    }

    /** Update pitch live (e.g. when fear amplifier changes). */
    public void updatePitch(float newPitch) {
        this.speedPitch = Mth.clamp(newPitch, 0.1F, 4.0F);
        this.pitch = this.speedPitch;
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            stopped = true;
            return;
        }

        MobEffect fear = ObjectManager.getEffect("fear");
        if (fear == null || !player.hasEffect(fear)) {
            stopped = true;
        }
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    public void markStopped() {
        this.stopped = true;
    }
}
