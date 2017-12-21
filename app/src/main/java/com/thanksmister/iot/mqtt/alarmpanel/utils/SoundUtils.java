package com.thanksmister.iot.mqtt.alarmpanel.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.thanksmister.iot.mqtt.alarmpanel.R;

public class SoundUtils extends ContextWrapper {
    
    private MediaPlayer speaker;
    private boolean playing; 
    private boolean repeating;

    public SoundUtils(Context base) {
        super(base);
    }
    
    public void destroyBuzzer() {
        try {
            if (speaker != null) {
                if(playing || repeating) {
                    speaker.stop();
                    speaker.reset();
                    speaker.release();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        speaker = null;
    }
    
    public void playBuzzerOnButtonPress() {

        // stop the buzzer if repeating
        if(repeating) {
            stopBuzzerRepeat(); 
            repeating = false;
        }
        try {
            if(!playing) {
                speaker = MediaPlayer.create(getApplicationContext(), R.raw.beep);
                speaker.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mp.stop();
                        mp.release();
                        playing = false;
                    }
                });
                playing = true;
                speaker.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopBuzzerRepeat() {
        if (speaker != null) {
            if(speaker.isPlaying()) {
                speaker.stop();
            }
            speaker.release();
        }
    }
    
    public void playBuzzerRepeat() {
        if(speaker == null) {
            speaker = MediaPlayer.create(getApplicationContext(), R.raw.beep_loop);
        }
        repeating = true;
        speaker.setLooping(true);
        speaker.start();
    }
}