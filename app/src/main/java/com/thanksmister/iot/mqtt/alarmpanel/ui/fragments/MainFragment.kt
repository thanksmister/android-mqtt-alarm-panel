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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_EVENT_ALARM_MODE
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmTriggeredView
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import javax.inject.Inject


class MainFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MainViewModel
    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var mqttOptions: MQTTOptions

    private var listener: OnMainFragmentListener? = null

    interface OnMainFragmentListener {
        fun manuallyLaunchScreenSaver()
        fun navigatePlatformPanel()
        fun publishDisarm(code: String)
        fun publishAlertCall()
        fun showCodeDialog(type: CodeTypes)
        fun showAlarmTriggered()
        fun hideTriggeredView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("onAttach")
        if (context is OnMainFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnMainFragmentListener")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.d("onActivityCreated")
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        observeViewModel(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        buttonSettings?.setOnClickListener {
            showSettingsCodeDialog()
        }
        platformButton?.setOnClickListener {
            listener?.navigatePlatformPanel()
        }
        buttonSleep?.setOnClickListener {
            listener?.manuallyLaunchScreenSaver()
        }
        alertButton?.setOnClickListener {
            listener?.publishAlertCall()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    /**
     * Here we setup the visibility of the bottom navigation bar buttons based on changes in the settings.
     */
    override fun onResume() {
        super.onResume()
        if (viewModel.hasPlatform()) {
            buttonPlatformLayout?.visibility = View.VISIBLE;
        } else {
            buttonPlatformLayout?.visibility = View.INVISIBLE;
        }
        if(configuration.hasScreenSaver()) {
            buttonSleepLayout?.visibility = View.VISIBLE
        } else {
            buttonSleepLayout?.visibility = View.GONE
        }
        if(configuration.panicButton.not()) {
            alertButton?.visibility = View.GONE
        } else {
            alertButton?.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
    }

    override fun onDetach() {
        super.onDetach()
        buttonSleep?.apply {
            setOnTouchListener(null)
        }
        listener = null
        Timber.d("onDetach")
    }

    private fun observeViewModel(viewModel: MainViewModel) {
        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    Timber.d("Alarm state: $state")
                    Timber.d("Alarm mode: ${viewModel.getAlarmMode()}" )
                    activity?.runOnUiThread {
                        when (state) {
                            MqttUtils.STATE_ARMED_AWAY,
                            MqttUtils.STATE_ARMED_NIGHT,
                            MqttUtils.STATE_ARMED_HOME -> {
                                dialogUtils.clearDialogs()
                            }
                            MqttUtils.STATE_ARMING_NIGHT,
                            MqttUtils.STATE_ARMING_AWAY,
                            MqttUtils.STATE_ARMING_HOME,
                            MqttUtils.STATE_ARMING -> {
                                dialogUtils.clearDialogs()
                            }
                            MqttUtils.STATE_DISARMED -> {
                                dialogUtils.clearDialogs()
                                listener?.hideTriggeredView()
                            }
                            MqttUtils.STATE_PENDING -> {
                                dialogUtils.clearDialogs()
                                if (configuration.isAlarmArmedMode()) {
                                    listener?.showAlarmTriggered()
                                }
                            }
                            MqttUtils.STATE_TRIGGERED -> {
                                dialogUtils.clearDialogs()
                                listener?.showAlarmTriggered()
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get message: " + error) }))
    }

    /**
     * We show the disarm dialog if we have delay time and a sensor was triggered in home or away mode.
     * This will be the time before the alarm is triggered, otherwise the expected behavior is the
     * alarm will trigger immediately.
     */
    private fun showAlarmDisableDialog(delayTime: Int) {
        /*activity.takeIf { isAdded }?.let {
            if(delayTime > 0) {
                dialogUtils.showAlarmDisableDialog(it as BaseActivity, object : AlarmDisableView.ViewListener {
                    override fun onComplete(code: Int) {
                        listener?.publishDisarmed()
                        dialogUtils.clearDialogs()
                    }
                    override fun onError() {
                        Toast.makeText(it, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show()
                    }
                    override fun onCancel() {
                        dialogUtils.clearDialogs()
                    }
                }, configuration.alarmCode, delayTime, configuration.systemSounds, configuration.fingerPrint)
            }
        }*/
    }

    private fun showSettingsCodeDialog() {
        if (configuration.isFirstTime) {
            activity?.let {
                val intent = SettingsActivity.createStartIntent(it.applicationContext)
                startActivity(intent)
            }
        } else {
            listener?.showCodeDialog(CodeTypes.SETTINGS)
        }
    }

    companion object {
        @JvmStatic fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}