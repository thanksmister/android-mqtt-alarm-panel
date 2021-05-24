/*
 * Copyright (c) 2020 ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import kotlinx.android.synthetic.main.fragment_code_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_code_bottom_sheet.countDownProgressWheel
import kotlinx.android.synthetic.main.fragment_controls.*
import kotlinx.android.synthetic.main.view_keypad.*
import timber.log.Timber

class CodeBottomSheetFragment (private val alarmListener: OnAlarmCodeFragmentListener) : BottomSheetDialogFragment() {

    private var codeComplete = false
    private var enteredCode = ""
    private var countDownTimer: CountDownTimer? = null
    private var countDownTimeRemaining: Int = 0
    private val handler: Handler by lazy {
        Handler()
    }

    private val delayRunnable = object : Runnable {
        override fun run() {
            handler.removeCallbacks(this)
            if(codeComplete) {
                onComplete(enteredCode)
            } else {
                onError()
            }
        }
    }

    interface OnAlarmCodeFragmentListener {
        fun onComplete(code: String)
        fun onCodeError()
        fun onCodeEmpty()
        fun onCancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeCodeButton.setOnClickListener {
            onCancel()
        }
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

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog
                val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
                val behavior = BottomSheetBehavior.from(bottomSheet!!)
                behavior.isDraggable = false
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        })

        if(codeType == CodeTypes.ARM_REMOTE || codeType == CodeTypes.DISARM_REMOTE) {
            buttonKey.visibility = View.VISIBLE
            buttonKey.setOnClickListener {
                if(enteredCode.isNotEmpty()) {
                    codeComplete = true
                    handler.postDelayed(delayRunnable, 500)
                } else {
                    alarmListener.onCodeEmpty()
                }
            }
        }

        if(delayTime > 0) {
            countDownProgressWheel.visibility = View.VISIBLE
            val divideBy = 360 / delayTime
            countDownTimer = object : CountDownTimer((delayTime * 1000).toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    countDownTimeRemaining = (millisUntilFinished / 1000).toInt()
                    val an = RotateAnimation(0.0f, 90.0f, 250f, 273f)
                    an.fillAfter = true
                    countDownProgressWheel.setText(countDownTimeRemaining.toString())
                    countDownProgressWheel.setWheelProgress(countDownTimeRemaining * divideBy)
                }

                override fun onFinish() {
                    Timber.d("Timed up...")
                    countDownTimeRemaining = 0
                    countDownProgressWheel.visibility = View.GONE
                    onCancel()
                }
            }.start()
        }
    }

    @Nullable
    override fun onCreateView(@NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_code_bottom_sheet, container, false)
    }

    private fun onCancel() {
        enteredCode = ""
        showFilledPins(0)
        alarmListener.onCancel()
        dismiss()
    }

    private fun onComplete(code: String) {
        alarmListener.onComplete(code)
        dismiss()
    }

    private fun onError() {
        codeComplete = false
        alarmListener.onCodeError()
        enteredCode = ""
        showFilledPins(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(delayRunnable)
        countDownTimer?.cancel()
        countDownTimer = null
        countDownTimeRemaining = 0
        countDownProgressWheel?.visibility = View.GONE
    }

    private fun addPinCode(code: String) {
        if (codeComplete) return
        enteredCode += code
        showFilledPins(enteredCode.length)
        if(codeType == CodeTypes.ARM_REMOTE || codeType == CodeTypes.DISARM_REMOTE) {
            if(enteredCode.length == MAX_REMOTE_CODE_LENGTH) {
                codeComplete = true
                handler.postDelayed(delayRunnable, 500)
            }
        } else if (enteredCode.length == MAX_CODE_LENGTH && enteredCode == currentCode) {
            codeComplete = true
            handler.postDelayed(delayRunnable, 500)
        } else if (enteredCode.length == MAX_CODE_LENGTH) {
            handler.postDelayed(delayRunnable, 500)
        }
    }

    private fun removePinCode() {
        if (codeComplete) return
        if (enteredCode.isNotEmpty()) {
            enteredCode = enteredCode.substring(0, enteredCode.length - 1)
            showFilledPins(enteredCode.length)
        } else {
            showFilledPins(0)
        }
    }

    private fun showFilledPins(pinsShown: Int) {
        if (pinCode1 != null && pinCode2 != null && pinCode3 != null && pinCode4 != null) {
            when (pinsShown) {
                0 -> {
                    pinCode1.visibility = View.INVISIBLE
                    pinCode2.visibility = View.INVISIBLE
                    pinCode3.visibility = View.INVISIBLE
                    pinCode4.visibility = View.INVISIBLE
                    pinCode5.visibility = View.GONE
                    pinCode6.visibility = View.GONE
                    pinCode7.visibility = View.GONE
                    pinCode8.visibility = View.GONE
                }
                1 -> {
                    pinCode1.visibility = View.VISIBLE
                    pinCode2.visibility = View.INVISIBLE
                    pinCode3.visibility = View.INVISIBLE
                    pinCode4.visibility = View.INVISIBLE
                    pinCode5.visibility = View.GONE
                    pinCode6.visibility = View.GONE
                    pinCode7.visibility = View.GONE
                    pinCode8.visibility = View.GONE
                }
                2 -> {
                    pinCode1.visibility = View.VISIBLE
                    pinCode2.visibility = View.VISIBLE
                    pinCode3.visibility = View.INVISIBLE
                    pinCode4.visibility = View.INVISIBLE
                    pinCode5.visibility = View.GONE
                    pinCode6.visibility = View.GONE
                    pinCode7.visibility = View.GONE
                    pinCode8.visibility = View.GONE
                }
                3 -> {
                    pinCode1.visibility = View.VISIBLE
                    pinCode2.visibility = View.VISIBLE
                    pinCode3.visibility = View.VISIBLE
                    pinCode4.visibility = View.INVISIBLE
                    pinCode5.visibility = View.GONE
                    pinCode6.visibility = View.GONE
                    pinCode7.visibility = View.GONE
                    pinCode8.visibility = View.GONE
                }
                4 -> {
                    pinCode1.visibility = View.VISIBLE
                    pinCode2.visibility = View.VISIBLE
                    pinCode3.visibility = View.VISIBLE
                    pinCode4.visibility = View.VISIBLE
                    pinCode5.visibility = View.GONE
                    pinCode6.visibility = View.GONE
                    pinCode7.visibility = View.GONE
                    pinCode8.visibility = View.GONE
                }
                5 -> {
                    pinCode1.visibility = View.VISIBLE
                    pinCode2.visibility = View.VISIBLE
                    pinCode3.visibility = View.VISIBLE
                    pinCode4.visibility = View.VISIBLE
                    pinCode5.visibility = View.VISIBLE
                    pinCode6.visibility = View.GONE
                    pinCode7.visibility = View.GONE
                    pinCode8.visibility = View.GONE
                }
                6 -> {
                    pinCode1.visibility = View.VISIBLE
                    pinCode2.visibility = View.VISIBLE
                    pinCode3.visibility = View.VISIBLE
                    pinCode4.visibility = View.VISIBLE
                    pinCode5.visibility = View.VISIBLE
                    pinCode6.visibility = View.VISIBLE
                    pinCode7.visibility = View.GONE
                    pinCode8.visibility = View.GONE
                }
                7 -> {
                    pinCode1.visibility = View.VISIBLE
                    pinCode2.visibility = View.VISIBLE
                    pinCode3.visibility = View.VISIBLE
                    pinCode4.visibility = View.VISIBLE
                    pinCode5.visibility = View.VISIBLE
                    pinCode6.visibility = View.VISIBLE
                    pinCode7.visibility = View.VISIBLE
                    pinCode8.visibility = View.GONE
                }
                8 -> {
                    pinCode1.visibility = View.VISIBLE
                    pinCode2.visibility = View.VISIBLE
                    pinCode3.visibility = View.VISIBLE
                    pinCode4.visibility = View.VISIBLE
                    pinCode5.visibility = View.VISIBLE
                    pinCode6.visibility = View.VISIBLE
                    pinCode7.visibility = View.VISIBLE
                    pinCode8.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        private  val MAX_CODE_LENGTH = 4
        private  val MAX_REMOTE_CODE_LENGTH = 8
        private var currentCode: String = ""
        private var codeType: CodeTypes = CodeTypes.SETTINGS
        private var delayTime = 0

        fun newInstance(code: String, delay: Int?, type: CodeTypes, listener : OnAlarmCodeFragmentListener): CodeBottomSheetFragment {
            codeType = type
            currentCode = code
            delayTime = delay?:0
            return CodeBottomSheetFragment(listener)
        }
    }
}