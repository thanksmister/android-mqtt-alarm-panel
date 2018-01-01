package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.widget.LinearLayout

import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.utils.SoundUtils

import kotlinx.android.synthetic.main.dialog_alarm_code_set.view.*
import kotlinx.android.synthetic.main.view_keypad.view.*

abstract class BaseAlarmView : LinearLayout {

    var currentCode: Int = 0
    var codeComplete = false
    var enteredCode = ""

    private var soundUtils: SoundUtils? = null

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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroySoundUtils()
    }

    fun setCode(code: Int) {
        currentCode = code
    }

    abstract fun onCancel()
    abstract fun removePinCode()
    abstract fun addPinCode(code: String)
    abstract fun reset()

    fun destroySoundUtils() {
        if (soundUtils != null) {
            soundUtils!!.destroyBuzzer()
        }
    }

    fun playButtonPress() {
        if (soundUtils == null) {
            soundUtils = SoundUtils(context)
        }
        soundUtils!!.playBuzzerOnButtonPress()
    }

    fun playContinuousBeep() {
        if (soundUtils == null) {
            soundUtils = SoundUtils(context)
            soundUtils!!.playBuzzerRepeat()
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
    }
}