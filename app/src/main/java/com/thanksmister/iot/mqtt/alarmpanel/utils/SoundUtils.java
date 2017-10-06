package com.thanksmister.iot.mqtt.alarmpanel.utils;

import android.content.Context;
import android.media.MediaPlayer;

import com.thanksmister.iot.mqtt.alarmpanel.R;

public class SoundUtils {
    
    private MediaPlayer speaker;
    private Context context;
    private boolean playing; 
    private boolean repeating; 

    public SoundUtils(Context context) {
        this.context = context;
    }

    /**
     * We want to fully destroy the media player.
     */
    public void destroyBuzzer() {
        if (speaker != null) {
            try {
                speaker.stop();
                speaker.release();
            } catch (Exception e){
                e.printStackTrace();
            } 
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
                speaker = MediaPlayer.create(context, R.raw.beep);
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
            speaker = MediaPlayer.create(context, R.raw.beep_loop);
        }
        repeating = true;
        speaker.setLooping(true);
        speaker.start();
    }
}