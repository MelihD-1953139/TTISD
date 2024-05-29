package com.example.tttest;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class SoundPlayer {
    private final SoundPool soundPool;
    private final int[] soundIds;
    private final int[] activeSounds = new int[24];

    // List of sound resource IDs
    private static final int[] SOUND_RES_IDS = {
            R.raw.c3, R.raw.db3, R.raw.d3, R.raw.eb3, R.raw.e3, R.raw.f3, R.raw.gb3, R.raw.g3, R.raw.ab3, R.raw.a3, R.raw.bb3, R.raw.b3,
            R.raw.c4, R.raw.db4, R.raw.d4, R.raw.eb4, R.raw.e4, R.raw.f4, R.raw.gb4, R.raw.g4, R.raw.ab4, R.raw.a4, R.raw.bb4, R.raw.b4,
            // Add all your other sound resource IDs here
    };
    Uri C4Uri;
    SoundPlayer(Context context) {
        // Initialize the SoundPool with suitable parameters
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(24)
                .setAudioAttributes(audioAttributes)
                .build();

        // Load the sounds into the SoundPool
        soundIds = new int[SOUND_RES_IDS.length];
        for (int i = 0; i < SOUND_RES_IDS.length; i++) {
            soundIds[i] = soundPool.load(context, SOUND_RES_IDS[i], 1);
        }
    }

    public void playSound(int[] keys) {
        // keys vergeten te reversen :)
        keys = reverse(keys, keys.length);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == 1) {
                if (activeSounds[i] == 0)
                {
                    int streamId = soundPool.play(soundIds[i], 1, 1, 1, 0, 1);
                    activeSounds[i] = streamId;
                }
            } else if (keys[i] == 0) {
                stopSound(i);
            }
        }
    }

    private int[] reverse(int a[], int n)
    {
        int[] b = new int[n];
        int j = n;
        for (int i = 0; i < n; i++) {
            b[j - 1] = a[i];
            j = j - 1;
        }
        return b;
    }

    private void stopSound(int soundIndex) {
        if (activeSounds[soundIndex] == 0) {
            return;
        }
        int streamId = activeSounds[soundIndex];
        soundPool.stop(streamId);
        activeSounds[soundIndex] = 0;
    }

    public void release() {
        soundPool.release();
    }
}
