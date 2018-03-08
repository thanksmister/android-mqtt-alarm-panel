package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.media.AudioManager
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.utils.SoundUtils
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import kotlinx.android.synthetic.main.dialog_alarm_code_set.view.*
import kotlinx.android.synthetic.main.view_keypad.view.*
import timber.log.Timber


abstract class BaseAlarmView : LinearLayout {
    var currentCode: Int = 0
    var codeComplete = false
    var enteredCode = ""
    var useSystemSound: Boolean = true

    private var soundUtils: SoundUtils? = null

    private val fingerPrintIdentity = FingerprintIdentify(context)

    constructor(context: Context) : super(context) {
        // let's play the sound as loud as we can
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onFinishInflate() {

        super.onFinishInflate()

        button0.setOnClickListener {
            playButtonPress()
            addPinCode("0")
        }

        button1.setOnClickListener {
            playButtonPress()
            addPinCode("1")
        }

        button2.setOnClickListener {
            playButtonPress()
            addPinCode("2")
        }

        button3.setOnClickListener {
            playButtonPress()
            addPinCode("3")
        }

        button4.setOnClickListener {
            playButtonPress()
            addPinCode("4")
        }

        button5.setOnClickListener {
            playButtonPress()
            addPinCode("5")
        }

        button6.setOnClickListener {
            playButtonPress()
            addPinCode("6")
        }

        button7.setOnClickListener {
            playButtonPress()
            addPinCode("7")
        }

        button8.setOnClickListener {
            playButtonPress()
            addPinCode("8")
        }

        button9.setOnClickListener {
            playButtonPress()
            addPinCode("9")
        }

        buttonDel.setOnClickListener {
            playButtonPress()
            removePinCode()
        }

        buttonDel.setOnClickListener {
            playButtonPress()
            removePinCode()
        }

        if (buttonCancel != null) {
            buttonCancel.setOnClickListener {
                playButtonPress()
                onCancel()
            }
        }
    }

    @SuppressLint("InlinedApi")
    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        // Fingerprint API only available on from Android 6.0 (M) and we only use it if user has available hardware and fingerprint
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fingerprintManager = context.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            if (fingerprintManager.hasEnrolledFingerprints() && fingerprintManager.isHardwareDetected) {
                if (!this.isShown){
                    stopFingerprintIdentity()
                    return
                } else {
                    startFingerprintIdentity()
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
        fingerPrintIdentity.startIdentify(MAX_FINGERPRINT_RETRIES, object : BaseFingerprint.FingerprintIdentifyListener{
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
            }

            override fun onStartFailedByDeviceLocked() {
                Timber.d("Fingerprint identity failed by device locked");
            }
        })
    }

    private fun stopFingerprintIdentity(){
        Timber.d("Fingerprint identity stop");
        fingerPrintIdentity.cancelIdentify()
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
        if(!useSystemSound)
            return

        if (soundUtils != null) {
            soundUtils?.destroyBuzzer()
        }
    }

    private fun playButtonPress() {
        if(!useSystemSound)
            return

        if (soundUtils == null) {
            soundUtils = SoundUtils(context)
            soundUtils?.init()
        }
        soundUtils?.playBuzzerOnButtonPress()
    }

    fun playContinuousBeep() {
        if(!useSystemSound)
            return

        if (soundUtils == null) {
            soundUtils = SoundUtils(context)
            soundUtils?.init()
            soundUtils?.playBuzzerRepeat()
        }
    }

    protected fun showFilledPins(pinsShown: Int) {
        if (pinCode1 != null && pinCode2 != null && pinCode3 != null && pinCode4 != null) {
            when (pinsShown) {
                1 -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                }
                2 -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                }
                3 -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                }
                4 -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_checked_black_32dp)
                }
                else -> {
                    pinCode1.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                    pinCode2.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                    pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp)
                }
            }
        }
    }

    companion object {
        val MAX_CODE_LENGTH = 4
        val MAX_FINGERPRINT_RETRIES = 5
    }
}