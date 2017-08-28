package com.thanksmister.iot.mqtt.alarmpanel.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.thanksmister.iot.mqtt.alarmpanel.AlarmSounds;
import com.thanksmister.iot.mqtt.alarmpanel.BoardDefaults;

import java.io.IOException;

import timber.log.Timber;

import static android.content.ContentValues.TAG;

/**
 * Created by michaelritchie on 8/25/17.
 */

public class SoundUtils {

    private static final long PLAYBACK_NOTE_DELAY = 800;
    private Handler mHandler;
    private Speaker speaker;

    public SoundUtils() {
        // init
    }

    public void destroyBuzzer() {
        if (speaker != null) {
            try {
                speaker.stop();
                speaker.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing speaker", e);
            } finally {
                speaker = null;
            }
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(mPlaybackRunnable);
        }
    }

    private void initSpeaker() {
        if(speaker == null) {
            try {
                speaker = new Speaker(BoardDefaults.getPwmPin());
                speaker.stop(); // in case the PWM pin was enabled already
            } catch (IOException e) {
                Log.e(TAG, "Error initializing speaker");
            }
        }
    }

    public void playBuzzerOnButtonPress() {
        initSpeaker();
        if(speaker != null) {
            double note = AlarmSounds.A4;
            try {
                speaker.play(note);
                Thread.sleep(100);
                speaker.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playBuzzerRepeat() {
        mHandler = new Handler();
        mHandler.post(mPlaybackRunnable);
    }

    private Runnable mPlaybackRunnable = new Runnable() {
        @Override
        public void run() {
            initSpeaker();
            if(speaker != null) {
                try {
                    double note = AlarmSounds.A4;
                    speaker.play(note);
                    Thread.sleep(200);
                    speaker.stop();
                    mHandler.postDelayed(mPlaybackRunnable, PLAYBACK_NOTE_DELAY);
                } catch (Exception e) {
                    Log.e(TAG, "Error playing speaker", e);
                }
            }
        }
    };
}
