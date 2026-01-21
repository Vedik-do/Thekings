package com.pavithra.cataclysmomcompat;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class BossThemeSoundInstance extends AbstractTickableSoundInstance {
    private static final float EPS = 0.001f;

    private float volumeTarget = EPS;
    private float volumeVel = 0f;
    private int velTicksLeft = 0;

    private boolean hardStopWhenSilent = false;
    private boolean done = false;

    public BossThemeSoundInstance(SoundEvent soundEvent) {
        super(soundEvent, SoundSource.MUSIC, net.minecraft.util.RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.attenuation = Attenuation.NONE;

        // Start almost silent, then fade in.
        this.volume = EPS;
        fadeTo(1.0f, 40);
    }

    public void fadeTo(float target, int ticks) {
        this.volumeTarget = Mth.clamp(target, 0f, 1f);
        this.velTicksLeft = Math.max(1, ticks);
        this.volumeVel = (this.volumeTarget - this.volume) / this.velTicksLeft;
    }

    public void beginFadeOut(int ticks, boolean hardStopWhenSilent) {
        this.hardStopWhenSilent = hardStopWhenSilent;
        fadeTo(0.0f, ticks);
    }

    public void hardStopAfterFade() {
        this.hardStopWhenSilent = true;
        if (this.volume <= EPS) {
            stopNow();
        }
    }

    public boolean isDone() {
        return done;
    }

    private void stopNow() {
        this.stop();
        this.done = true;
    }

    @Override
    public void tick() {
        if (done) return;

        if (velTicksLeft > 0) {
            this.volume += volumeVel;
            velTicksLeft--;
            if (velTicksLeft == 0) {
                this.volume = volumeTarget;
            }
        }

        if (hardStopWhenSilent && this.volume <= EPS) {
            stopNow();
        }
    }
}
