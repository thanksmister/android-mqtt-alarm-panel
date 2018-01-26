package com.thanksmister.iot.mqtt.alarmpanel.utils

import android.content.Context
import android.content.ContextWrapper
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import com.thanksmister.iot.mqtt.alarmpanel.R
import timber.log.Timber

class SoundUtils(base: Context) : ContextWrapper(base) {

    private var speaker: MediaPlayer? = null
    private var playing: Boolean = false
    private var repeating: Boolean = false

    private val soundThread: HandlerThread = HandlerThread("buttonSound");
    private var soundHandler: Handler? = null

    fun init(){
        Timber.d("init")
        soundThread.start();
        soundHandler = Handler(soundThread.looper);
    }

    fun destroyBuzzer() {
        Timber.d("destroyBuzzer")
        soundHandler?.post(Runnable { soundHandler?.removeCallbacks(repeatAudioRunnable) })
        soundHandler?.post(Runnable { soundHandler?.removeCallbacks(streamAudioRunnable) })
    }

    fun playBuzzerOnButtonPress() {
        Timber.d("playBuzzerOnButtonPress")
        if (repeating) {
            stopBuzzerRepeat()
            repeating = false
        }
        soundHandler?.post(streamAudioRunnable);
    }

    private val streamAudioRunnable = Runnable {
        val speaker = MediaPlayer.create(applicationContext, R.raw.beep)
        speaker.setOnCompletionListener { mp ->
            mp.stop()
            mp.release()
            playing = false
        }
        speaker.start()
        playing = true
    }

    private val repeatAudioRunnable = Runnable {
        Timber.d("repeatAudioRunnable")
        if(repeating) {
            speaker = MediaPlayer.create(applicationContext, R.raw.beep_loop)
            speaker?.setOnCompletionListener { mp ->
                mp.stop()
                mp.release()
            }
            speaker?.isLooping = true
            speaker?.start()
        }
    }

    private fun stopBuzzerRepeat() {
        Timber.d("stopBuzzerRepeat")
        if (speaker != null) {
            if (speaker!!.isPlaying) {
                speaker!!.stop()
            }
            speaker!!.release()
        }
        repeating = false
        soundHandler?.removeCallbacks(repeatAudioRunnable);
    }

    private fun repeatBuzzer() {
        soundHandler?.postDelayed(repeatAudioRunnable, 2000);
    }

    fun playBuzzerRepeat() {
        Timber.d("playBuzzerRepeat")
        repeating = true
        soundHandler?.post(repeatAudioRunnable);
    }
}