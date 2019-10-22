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

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import kotlinx.android.synthetic.main.dialog_alarm_code_set.view.*
import kotlinx.android.synthetic.main.view_keypad.view.*
import timber.log.Timber
import java.io.FileNotFoundException

abstract class BaseAlarmView : LinearLayout {
    var currentCode: Int = 0
    var codeComplete = false
    var enteredCode = ""
    var useSystemSound: Boolean = true
    var useFingerprint: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private var fingerPrintIdentity:FingerprintIdentify? = null

    constructor(context: Context) : super(context) {
        // let's play the sound as loud as we can
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        am.setStreamVolume(AudioManager.STREAM_ALARM, amStreamMusicMaxVol, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onFinishInflate() {

        super.onFinishInflate()

        button0.setOnClickListener {
            addPinCode("0")
        }

        button1.setOnClickListener {
            addPinCode("1")
        }

        button2.setOnClickListener {
            addPinCode("2")
        }

        button3.setOnClickListener {
            addPinCode("3")
        }

        button4.setOnClickListener {
            addPinCode("4")
        }

        button5.setOnClickListener {
            addPinCode("5")
        }

        button6.setOnClickListener {
            addPinCode("6")
        }

        button7.setOnClickListener {
            addPinCode("7")
        }

        button8.setOnClickListener {
            addPinCode("8")
        }

        button9.setOnClickListener {
            addPinCode("9")
        }

        buttonDel.setOnClickListener {
            removePinCode()
        }

        buttonDel.setOnClickListener {
            removePinCode()
        }

        if (buttonCancel != null) {
            buttonCancel.setOnClickListener {
                onCancel()
            }
        }
    }

    @SuppressLint("InlinedApi")
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if(useFingerprint) {
            // TODO looks like this throws an error internally
            try {
                fingerPrintIdentity = FingerprintIdentify(context)
            } catch (e: ClassNotFoundException) {
                Timber.w("Fingerprint: " + e.message)
            }
            if (fingerPrintIdentity != null) {
                if (fingerPrintIdentity!!.isFingerprintEnable && fingerPrintIdentity!!.isHardwareEnable) {
                    if (!this.isShown) {
                        stopFingerprintIdentity()
                        return
                    } else {
                        startFingerprintIdentity()
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroySoundUtils()
        stopFingerprintIdentity()
    }

    private fun startFingerprintIdentity(){
        Timber.d("Fingerprint identity start");
        fingerPrintIdentity?.startIdentify(MAX_FINGERPRINT_RETRIES, object : BaseFingerprint.IdentifyListener{

            override fun onSucceed(){
                Timber.d("Fingerprint identity success");
                codeComplete = false;
                enteredCode = ""
                addPinCode(currentCode.toString().padStart(MAX_CODE_LENGTH, '0'))
            }

            override fun onNotMatch(availableTimes: Int) {
                Timber.d("Fingerprint identity no match");
                fingerNoMatch()
            }

            override fun onFailed(isDeviceLocked: Boolean) {
                Timber.d("Fingerprint identity failed");
                fingerNoMatch()
            }

            override fun onStartFailedByDeviceLocked() {
                Timber.d("Fingerprint identity failed by device locked");
                fingerNoMatch()
            }
        })
    }

    private fun stopFingerprintIdentity(){
        Timber.d("Fingerprint identity stop");
        fingerPrintIdentity?.cancelIdentify()
    }

    fun setUseSound(value: Boolean) {
        useSystemSound = value
    }

    fun setCode(code: Int) {
        currentCode = code
    }

    abstract fun onCancel()
    abstract fun removePinCode()
    abstract fun addPinCode(code: String)
    abstract fun reset()
    abstract fun fingerNoMatch()

    fun destroySoundUtils() {
        if(mediaPlayer != null) {
            mediaPlayer?.stop()
        }
    }

    fun playContinuousAlarm() {
        if(useSystemSound) {
            try {
                var alert: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                if (alert == null) {
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    if (alert == null) {
                        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    }
                }
                mediaPlayer = MediaPlayer.create(context!!, alert)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            } catch (e: SecurityException) {
                playContinuousBeep()
            } catch(e: FileNotFoundException) {
                playContinuousBeep()
            }
        } else {
            playContinuousBeep()
        }
    }

    private fun playContinuousBeep() {
        Timber.d("playContinuousBeep")
        mediaPlayer = MediaPlayer.create(context!!, R.raw.beep_loop)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    protected fun showFilledPins(pinsShown: Int) {
        if (pinCode1 != null && pinCode2 != null && pinCode3 != null && pinCode4 != null) {
            when (pinsShown) {
                1 -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                }
                2 -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                }
                3 -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_checked_black)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                }
                4 -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_checked_black)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_checked_black)
                }
                else -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                }
            }
        }
    }

    companion object {
        val MAX_CODE_LENGTH = 4
        val MAX_FINGERPRINT_RETRIES = 5
    }
}