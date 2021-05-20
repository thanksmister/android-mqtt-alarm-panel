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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.TriggeredViewModel
import kotlinx.android.synthetic.main.dialog_alarm_triggered_code.*
import kotlinx.android.synthetic.main.view_keypad.*
import kotlinx.android.synthetic.main.view_keypad.view.*
import javax.inject.Inject

class TriggeredFragment : BaseFragment() {

    var codeType: CodeTypes = CodeTypes.DISARM
    var currentCode: String = ""
    var codeComplete = false
    var enteredCode = ""
    val handler: Handler = Handler()

    private val delayRunnable = object : Runnable {
        override fun run() {
            handler.removeCallbacks(this)
            if (codeComplete) {
                listener?.publishDisarm(enteredCode)
            }
            codeComplete = false
            enteredCode = ""
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: TriggeredViewModel

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var dialogUtils: DialogUtils

    @Inject
    lateinit var mqttOptions: MQTTOptions

    private var listener: OnTriggeredFragmentListener? = null

    interface OnTriggeredFragmentListener {
        fun publishDisarm(code: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTriggeredFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTriggeredFragmentListener")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TriggeredViewModel::class.java)
        observeViewModel(viewModel)
        val useRemoteDisarm = mqttOptions.useRemoteCode
        val requireCodeForDisarming = mqttOptions.requireCodeForDisarming
        if (useRemoteDisarm && requireCodeForDisarming) {
            codeType = CodeTypes.DISARM_REMOTE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keypadLayout.button0.setOnClickListener {
            addPinCode("0")
        }
        keypadLayout.button1.setOnClickListener {
            addPinCode("1")
        }
        keypadLayout.button2.setOnClickListener {
            addPinCode("2")
        }
        keypadLayout.button3.setOnClickListener {
            addPinCode("3")
        }
        keypadLayout.button4.setOnClickListener {
            addPinCode("4")
        }
        keypadLayout.button5.setOnClickListener {
            addPinCode("5")
        }
        keypadLayout.button6.setOnClickListener {
            addPinCode("6")
        }
        keypadLayout.button7.setOnClickListener {
            addPinCode("7")
        }
        keypadLayout.button8.setOnClickListener {
            addPinCode("8")
        }
        keypadLayout.button9.setOnClickListener {
            addPinCode("9")
        }
        keypadLayout.buttonDel.setOnClickListener {
            removePinCode()
        }
        keypadLayout.buttonDel.setOnClickListener {
            removePinCode()
        }
        if (codeType == CodeTypes.DISARM || codeType == CodeTypes.DISARM_REMOTE) {
            buttonKey.visibility = View.VISIBLE
            buttonKey.setOnClickListener {
                codeComplete = true
                handler.postDelayed(delayRunnable, 500)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_alarm_triggered_code, container, false)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        handler.removeCallbacks(delayRunnable)
    }

    private fun observeViewModel(viewModel: TriggeredViewModel) {
        // nothing to observer
    }

    private fun addPinCode(code: String) {
        if (codeComplete) return
        enteredCode += code
        showFilledPins(enteredCode.length)
        if (codeType == CodeTypes.ARM_REMOTE || codeType == CodeTypes.DISARM_REMOTE) {
            if (enteredCode.length == MAX_REMOTE_CODE_LENGTH) {
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
        private val MAX_CODE_LENGTH = 4
        private val MAX_REMOTE_CODE_LENGTH = 8
        @JvmStatic
        fun newInstance(): TriggeredFragment {
            return TriggeredFragment()
        }
    }
}