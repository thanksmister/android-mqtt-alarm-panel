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
import android.util.AttributeSet
import android.view.View

import com.thanksmister.iot.mqtt.alarmpanel.R
import kotlinx.android.synthetic.main.dialog_alarm_triggered_code.view.*
import kotlinx.android.synthetic.main.dialog_settings_code.view.*

class SettingsCodeView : BaseAlarmView {

    var settingsListener: ViewListener? = null

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
        settings_code_title.setText(R.string.text_settings_code_title)
        buttonDel.isEnabled = false
        buttonDel.visibility = View.INVISIBLE
    }

    fun setListener(listener: ViewListener) {
        this.settingsListener = listener
    }

    override fun onCancel() {
        if (settingsListener != null) {
            settingsListener!!.onCancel()
        }
        codeComplete = false
        enteredCode = ""
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (handler != null) {
            handler.removeCallbacks(delayRunnable)
        }
    }

    override fun reset() {}

    override fun addPinCode(code: String) {

        if (codeComplete)
            return

        enteredCode = enteredCode + code

        if (enteredCode.length == BaseAlarmView.Companion.MAX_CODE_LENGTH) {
            codeComplete = true
            handler.postDelayed(delayRunnable, 500)
        }
    }

    override fun removePinCode() {}

    private fun validateCode(validateCode: String) {
        val codeInt = Integer.parseInt(validateCode)
        if (codeInt == code) {
            if (settingsListener != null) {
                settingsListener!!.onComplete(code)
            }
        } else {
            codeComplete = false
            enteredCode = ""
            if (settingsListener != null) {
                settingsListener!!.onError()
            }
        }
    }
}