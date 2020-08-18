/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.util.AttributeSet
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import com.thanksmister.iot.mqtt.alarmpanel.R
import kotlinx.android.synthetic.main.dialog_alarm_disable.view.*
import timber.log.Timber
import java.io.FileNotFoundException

class AlarmPendingView : LinearLayout {

    private var alarmListener: ViewListener? = null
    private var useSystemSounds: Boolean = true
    private var mediaPlayer: MediaPlayer? = null
    private var pendingSoundFlag = false
    private var countDownTimeRemaining: Int = 0
    private var countDownTimer: CountDownTimer? = null

    constructor(context: Context) : super(context) {
        // let's play the sound as loud as we can
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        am.setStreamVolume(AudioManager.STREAM_ALARM, amStreamMusicMaxVol, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroySoundUtils()
        stopCountDown()
        alarmListener = null
    }

    fun setUseSound(value: Boolean) {
        useSystemSounds = value
    }

    fun destroySoundUtils() {
        pendingSoundFlag = false
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
        }
        mediaPlayer = null
    }

    private fun playContinuousAlarm() {
        if (useSystemSounds && !pendingSoundFlag) {
            try {
                val alert: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (alert == null) {
                    playContinuousBeep()
                    return
                }
                pendingSoundFlag = true
                Handler().postDelayed(Runnable { // Do something after 5s = 5000ms
                    if(pendingSoundFlag) {
                        mediaPlayer = MediaPlayer.create(context!!, alert)
                        mediaPlayer?.isLooping = true
                        mediaPlayer?.start()
                    }
                }, 500)
            } catch (e: SecurityException) {
                playContinuousBeep()
            } catch (e: FileNotFoundException) {
                playContinuousBeep()
            }
        }
    }

    private fun playContinuousBeep() {
        pendingSoundFlag = false
        mediaPlayer = MediaPlayer.create(context!!, R.raw.beep_loop)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    fun setListener(listener: ViewListener) {
        this.alarmListener = listener
    }

    /**
     * Countdown timer time
     * @param pendingTime seconds
     */
    fun startCountDown(pendingTime: Int) {
        if (pendingTime <= 0) {
            stopCountDown()
            if (alarmListener != null) {
                alarmListener!!.onTimeOut()
                countDownTimeRemaining = 0
            }
            return
        }

        if (countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
            countDownTimeRemaining = 0
        }
        playContinuousAlarm()
        val divideBy = 360 / pendingTime
        countDownTimer = object : CountDownTimer((pendingTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTimeRemaining = (millisUntilFinished / 1000).toInt()
                val an = RotateAnimation(0.0f, 90.0f, 250f, 273f)
                an.fillAfter = true
                countDownProgressWheel.setText(countDownTimeRemaining.toString())
                countDownProgressWheel.setProgress(countDownTimeRemaining * divideBy)
            }

            override fun onFinish() {
                Timber.d("Timed up...")
                if (alarmListener != null) {
                    alarmListener!!.onTimeOut()
                    countDownTimeRemaining = 0
                }
                destroySoundUtils()
            }
        }.start()
    }

    fun stopCountDown() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
        }
        countDownTimeRemaining = 0
        destroySoundUtils()
    }

    interface ViewListener {
        fun onTimeOut()
    }
}