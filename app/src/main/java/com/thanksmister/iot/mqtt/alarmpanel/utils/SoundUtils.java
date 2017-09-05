package com.thanksmister.iot.mqtt.alarmpanel.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import com.thanksmister.iot.mqtt.alarmpanel.R;

/**
 * Created by michaelritchie on 8/25/17.
 */

public class SoundUtils {

    private static final long PLAYBACK_BEEP_DELAY = 800;
    private Handler mHandler;
    private MediaPlayer speaker;
    private Context context;

    public SoundUtils(Context context) {
        this.context = context;
    }

    public void destroyBuzzer() {
        if (speaker != null) {
            if(speaker.isPlaying()) {
                speaker.stop();
            }
            speaker.release();
        }
        stopBuzzerRepeat();
    }

    private void initSpeaker() {
        speaker = MediaPlayer.create(context, R.raw.beep);
    }

    public void playBuzzerOnButtonPress() {
        stopBuzzerRepeat(); // stop the buzzer if 
        initSpeaker();
        if(speaker != null) {
            try {
                speaker.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopBuzzerRepeat() {
        if(mHandler != null) {
            mHandler.removeCallbacks(mPlaybackRunnable);
            mHandler = null;
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
                    speaker.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.postDelayed(mPlaybackRunnable, PLAYBACK_BEEP_DELAY);
            }
        }
    };
}