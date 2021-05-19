package com.thanksmister.iot.mqtt.alarmpanel.ui.controls

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.view_countdown.view.*
import java.util.concurrent.TimeUnit

class CircularCountDown : ConstraintLayout {

    private var countDownTimer: CountDownTimer? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    init {
        this.visibility = View.INVISIBLE
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopCountdown()
    }

    fun startCountdown(delay: Int) {
        countDownTimer?.cancel()
        this.visibility = View.VISIBLE
        countDownTimer = object : CountDownTimer(TimeUnit.SECONDS.toMillis(delay.toLong()), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownText.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                countDownText.text = ""
                stopCountdown()
            }
        }.start()
    }

    fun stopCountdown() {
        countDownTimer?.cancel()
        this.visibility = View.INVISIBLE
    }
}