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
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
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


class MainFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var dialogUtils: DialogUtils

    @Inject
    lateinit var mqttOptions: MQTTOptions

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

        // TODO handle 600 dp vs others?
        val metrics = resources.displayMetrics
        val scaleFactor = metrics.density
        val widthDp = metrics.widthPixels / scaleFactor
        val heightDp = metrics.heightPixels / scaleFactor
        val orientation = resources.configuration.orientation
        val smallestWidth = widthDp.coerceAtMost(heightDp)
        if (mqttOptions.hasNoSensors()) {
            if (orientation == ORIENTATION_LANDSCAPE) {
                when {
                    smallestWidth > 720 -> {
                        guideControlMiddle?.setGuidelinePercent(0.80f)
                        guidelineStart?.setGuidelinePercent(0.2f)
                    }
                    smallestWidth > 500 -> {
                        guideControlMiddle?.setGuidelinePercent(0.82f)
                        guidelineStart?.setGuidelinePercent(0.18f)
                    }
                    else -> {
                        guideControlMiddle?.setGuidelinePercent(0.78f)
                        guidelineStart?.setGuidelinePercent(0.22f)
                    }
                }
            } else {
                when {
                    smallestWidth > 720 -> {
                        guideControlTop?.setGuidelinePercent(0.32f)
                        guideControlBottom?.setGuidelinePercent(0.64f)
                    }
                    smallestWidth > 500 -> {
                        guideControlTop?.setGuidelinePercent(0.32f)
                        guideControlBottom?.setGuidelinePercent(0.64f)
                    }
                    else -> {
                        guideControlTop?.setGuidelinePercent(0.34f)
                        guideControlBottom?.setGuidelinePercent(0.62f)
                    }
                }
            }
        } else {
            if (orientation == ORIENTATION_LANDSCAPE) {
                when {
                    smallestWidth > 720 -> {
                        guideControlMiddle?.setGuidelinePercent(0.62f)
                        guidelineStart?.setGuidelinePercent(0.08f)
                    }
                    smallestWidth > 500 -> {
                        guideControlMiddle?.setGuidelinePercent(0.62f)
                        guidelineStart?.setGuidelinePercent(0.08f)
                    }
                    else -> {
                        guideControlMiddle?.setGuidelinePercent(0.60f)
                        guidelineStart?.setGuidelinePercent(0.08f)
                    }
                }

            } else {

                when {
                    smallestWidth > 720 -> {
                        guideControlTop?.setGuidelinePercent(0.26f)
                        guideControlBottom?.setGuidelinePercent(0.58f)
                    }
                    smallestWidth > 500 -> {
                        guideControlTop?.setGuidelinePercent(0.26f)
                        guideControlBottom?.setGuidelinePercent(0.58f)
                    }
                    else -> {
                        guideControlTop?.setGuidelinePercent(0.2f)
                        guideControlBottom?.setGuidelinePercent(0.5f)
                    }
                }

            }
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
                                    //showAlarmDisableDialog()
                                }
                            }
                            MqttUtils.STATE_TRIGGERED -> {
                                dialogUtils.clearDialogs()
                                listener?.showAlarmTriggered()
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get message: $error") }))
    }

    /**
     * We show the disarm dialog if we have delay time and a sensor was triggered in home or away mode.
     * This will be the time before the alarm is triggered, otherwise the expected behavior is the
     * alarm will trigger immediately.
     */
    /*private fun showAlarmDisableDialog(delayTime: Int) {
        activity.takeIf { isAdded }?.let {
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
        }
    }*/

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
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}