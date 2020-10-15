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
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.CodeBottomSheetFragment
import kotlinx.android.synthetic.main.dialog_alarm_disable.view.*

class AlarmTriggeredView : BaseAlarmView {

    var listener: ViewListener? = null

    private val delayRunnable = object : Runnable {
        override fun run() {
            handler.removeCallbacks(this)
            if(codeComplete) {
                listener?.onComplete(enteredCode)
            } else {
                listener?.onError()
            }
            reset()
        }
    }

    interface ViewListener {
        fun onComplete(code: String)
        fun onError()
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun reset() {
        codeComplete = false
        enteredCode = ""
    }

    override fun onCancel() {
        // user cannot cancel
    }

    override fun addPinCode(code: String) {
        if (codeComplete) return
        enteredCode += code
        showFilledPins(enteredCode.length)
        if(codeType == CodeTypes.DISARM_REMOTE) {
            if(enteredCode.length == MAX_CODE_LENGTH) {
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

    override fun removePinCode() {
        if (codeComplete) {
            return
        }
        if (!TextUtils.isEmpty(enteredCode)) {
            enteredCode = enteredCode.substring(0, enteredCode.length - 1)
        }
    }
}