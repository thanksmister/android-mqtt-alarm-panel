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
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import javax.inject.Inject


class TriggeredFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var dialogUtils: DialogUtils

    @Inject
    lateinit var mqttOptions: MQTTOptions

    private var listener: OnTriggeredFragmentListener? = null

    interface OnTriggeredFragmentListener {
        fun publishAlertCall()
        fun showCodeDialog(type: CodeTypes)
        fun hideTriggeredView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("onAttach")
        if (context is OnTriggeredFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTriggeredFragmentListener")
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

        alertButton?.setOnClickListener {
            listener?.publishAlertCall()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_alarm_triggered_code, container, false)
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
        if (configuration.hasScreenSaver()) {
            buttonSleepLayout?.visibility = View.VISIBLE
        } else {
            buttonSleepLayout?.visibility = View.GONE
        }
        if (configuration.panicButton.not()) {
            alertButton?.visibility = View.GONE
        } else {
            alertButton?.visibility = View.VISIBLE
        }

        if (mqttOptions.sensorOneActive.not()) {
            view?.findViewById<View>(R.id.oneSensorContainer)?.visibility = View.GONE
        } else {
            view?.findViewById<View>(R.id.oneSensorContainer)?.visibility = View.VISIBLE
        }

        if (mqttOptions.sensorTwoActive.not()) {
            view?.findViewById<View>(R.id.twoSensorContainer)?.visibility = View.GONE
        } else {
            view?.findViewById<View>(R.id.twoSensorContainer)?.visibility = View.VISIBLE
        }

        if (mqttOptions.sensorThreeActive.not()) {
            view?.findViewById<View>(R.id.threeSensorContainer)?.visibility = View.GONE
        } else {
            view?.findViewById<View>(R.id.threeSensorContainer)?.visibility = View.VISIBLE
        }

        if (mqttOptions.sensorFourActive.not()) {
            view?.findViewById<View>(R.id.fourSensorContainer)?.visibility = View.GONE
        } else {
            view?.findViewById<View>(R.id.fourSensorContainer)?.visibility = View.VISIBLE
        }

        // TODO we have to move the guides based on screen orientation and dimensions
        if (mqttOptions.sensorOneActive.not()
                && mqttOptions.sensorTwoActive.not()
                && mqttOptions.sensorThreeActive.not()
                && mqttOptions.sensorFourActive.not()) {
            guideControlBottom?.setGuidelinePercent(0.60f)
        }

        // TODO we have to move the guides based on screen orientation and dimensions
        if (mqttOptions.sensorOneActive.not()
                && mqttOptions.sensorTwoActive.not()
                && mqttOptions.sensorThreeActive.not()
                && mqttOptions.sensorFourActive.not()) {
            guideControlMiddle?.setGuidelinePercent(0.60f)
        }

        childFragmentManager.findFragmentById(R.id.oneSensorContainer)?.apply {
            val sensor = this as SensorControlFragment
            sensor.setSensorTitle(mqttOptions.sensorOneName)
            sensor.setSensorState(mqttOptions.sensorOneState)
            sensor.setSensorTopic(mqttOptions.sensorOneTopic)
        }

        childFragmentManager.findFragmentById(R.id.twoSensorContainer)?.apply {
            val sensor = this as SensorControlFragment
            sensor.setSensorTitle(mqttOptions.sensorTwoName)
            sensor.setSensorState(mqttOptions.sensorTwoState)
            sensor.setSensorTopic(mqttOptions.sensorTwoTopic)
        }

        childFragmentManager.findFragmentById(R.id.threeSensorContainer)?.apply {
            val sensor = this as SensorControlFragment
            sensor.setSensorTitle(mqttOptions.sensorThreeName)
            sensor.setSensorState(mqttOptions.sensorThreeState)
            sensor.setSensorTopic(mqttOptions.sensorThreeTopic)
        }

        childFragmentManager.findFragmentById(R.id.fourSensorContainer)?.apply {
            val sensor = this as SensorControlFragment
            sensor.setSensorTitle(mqttOptions.sensorFourName)
            sensor.setSensorState(mqttOptions.sensorFourState)
            sensor.setSensorTopic(mqttOptions.sensorFourTopic)
        }
    }

    override fun onDetach() {
        super.onDetach()
        buttonSleep?.apply {
            setOnTouchListener(null)
        }
        listener = null
    }

    private fun observeViewModel(viewModel: MainViewModel) {
        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    Timber.d("Alarm state: $state")
                    Timber.d("Alarm mode: ${viewModel.getAlarmMode()}")
                }, { error -> Timber.e("Unable to get message: $error") }))
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
        @JvmStatic
        fun newInstance(): TriggeredFragment {
            return TriggeredFragment()
        }
    }
}