/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.content.Context
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.RotateAnimation
import kotlinx.android.synthetic.main.dialog_alarm_disable.view.*

class AlarmDisableView : BaseAlarmView {

    internal var alarmListener: ViewListener? = null
    private var displaySeconds: Int = 0
    private var countDownTimer: CountDownTimer? = null

    private val delayRunnable = object : Runnable {
        override fun run() {
            handler.removeCallbacks(this)
            validateCode(enteredCode)
        }
    }

    interface ViewListener {
        fun onComplete(code: Int)
        fun onError()
        fun onCancel()
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun setListener(listener: ViewListener) {
        this.alarmListener = listener
    }

    fun setUseFingerPrint(value: Boolean) {
        useFingerprint = value
        if(value) {
            disable_fingerprint_layout.visibility = View.VISIBLE
        } else {
            disable_fingerprint_layout.visibility = View.GONE
        }
    }

    override fun onCancel() {
        codeComplete = false
        enteredCode = ""
        showFilledPins(0)
        alarmListener?.onCancel()
    }

    fun startCountDown(pendingTime: Int) {
        if (pendingTime > 0) {
            val divideBy = 360 / pendingTime
            countDownTimer = object : CountDownTimer((pendingTime * 1000).toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    displaySeconds = (millisUntilFinished / 1000).toInt()
                    val an = RotateAnimation(0.0f, 90.0f, 250f, 273f)
                    an.fillAfter = true
                    countDownProgressWheel.setText(displaySeconds.toString())
                    countDownProgressWheel.setProgress(displaySeconds * divideBy)
                }

                override fun onFinish() {
                    alarmListener?.onCancel()
                }
            }.start()
        } else {
            if (alarmListener != null) {
                alarmListener?.onCancel()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(delayRunnable)
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun reset() {}

    override fun fingerNoMatch() {
        alarmListener?.onError()
    }

    override fun addPinCode(code: String) {
        if (codeComplete)
            return

        enteredCode += code
        showFilledPins(enteredCode.length)
        if (enteredCode.length == BaseAlarmView.Companion.MAX_CODE_LENGTH) {
            codeComplete = true
            handler.postDelayed(delayRunnable, 500)
        }
    }

    override fun removePinCode() {
        if (codeComplete) {
            return
        }

        if (!TextUtils.isEmpty(enteredCode)) {
            enteredCode = enteredCode.substring(0, enteredCode.length - 1)
        }

        showFilledPins(enteredCode.length)
    }

    private fun validateCode(validateCode: String) {
        val codeInt = Integer.parseInt(validateCode)
        if (codeInt == currentCode) {
            countDownTimer?.cancel()
            countDownTimer = null
            alarmListener?.onComplete(currentCode)
        } else {
            codeComplete = false
            enteredCode = ""
            showFilledPins(0)
            alarmListener?.onError()
        }
    }
}