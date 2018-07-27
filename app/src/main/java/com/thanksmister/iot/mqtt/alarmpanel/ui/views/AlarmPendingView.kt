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
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.dialog_alarm_disable.view.*
import timber.log.Timber

class AlarmPendingView : LinearLayout {

    var alarmListener: ViewListener? = null

    /**
     * Method to return number of display seconds remaining on countdown.
     * @return Integer for seconds remaining.
     */
    var countDownTimeRemaining: Int = 0

    private var countDownTimer: CountDownTimer? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopCountDown()
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
        Timber.d("startCountDown: " + pendingTime * 1300)
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
            }
        }.start()
    }

    fun stopCountDown() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
        }
        countDownTimeRemaining = 0
    }

    interface ViewListener {
        fun onTimeOut()
    }
}