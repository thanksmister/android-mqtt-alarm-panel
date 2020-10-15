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
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View

import com.thanksmister.iot.mqtt.alarmpanel.R
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
        fun onComplete(code: String)
        fun onError()
        fun onCancel()
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
        settings_code_title.setText(R.string.text_settings_code_title)
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
        showFilledPins(0)
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
        if (validateCode == currentCode) {
            if (settingsListener != null) {
                settingsListener!!.onComplete(currentCode)
            }
        } else {
            codeComplete = false
            enteredCode = ""
            showFilledPins(0)
            if (settingsListener != null) {
                settingsListener!!.onError()
            }
        }
    }
}