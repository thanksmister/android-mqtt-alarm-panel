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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.SensorControlViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sensor_control.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SensorControlFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: SensorControlViewModel

    @Inject
    lateinit var dialogUtils: DialogUtils

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var mqttOptions: MQTTOptions

    private var topic: String = ""

    private var state: String = ""

    fun setSensorTitle(name: String) {
        titleText.text = name
    }

    fun setSensorState(state: String) {
        this.state = state
    }

    fun setSensorTopic(topic: String) {
        this.topic = topic
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SensorControlViewModel::class.java)
        observeViewModel(viewModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sensor_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun observeViewModel(viewModel: SensorControlViewModel) {
        disposable.add(viewModel.getSensorStates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ messages ->
                    activity?.runOnUiThread {
                        messages.forEach { message ->
                            when (message.topic?.toLowerCase(Locale.getDefault())) {
                                /*mqttOptions.sensorOneTopic -> {
                                    if(topic.equals(message.topic, ignoreCase = true)) {
                                        stateText.text = message.payload?.toUpperCase(Locale.getDefault())
                                        if(message.payload.equals(mqttOptions.sensorOneState, ignoreCase = true)) {
                                            iconImageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_green))
                                        } else {
                                            iconImageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_red))
                                        }
                                    }
                                }
                                mqttOptions.sensorTwoTopic -> {
                                    if(topic.equals(message.topic, ignoreCase = true)) {
                                        stateText.text = message.payload?.toUpperCase(Locale.getDefault())
                                        if (message.payload.equals(mqttOptions.sensorTwoState, ignoreCase = true)) {
                                            iconImageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_green))
                                        } else {
                                            iconImageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_red))
                                        }
                                    }
                                }
                                mqttOptions.sensorThreeTopic -> {
                                    if(topic.equals(message.topic, ignoreCase = true)) {
                                        stateText.text = message.payload?.toUpperCase(Locale.getDefault())
                                        if(message.payload.equals(mqttOptions.sensorThreeState, ignoreCase = true)) {
                                            iconImageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_green))
                                        } else {
                                            iconImageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_red))
                                        }
                                    }
                                }
                                mqttOptions.sensorFourTopic -> {
                                    if(topic.equals(message.topic, ignoreCase = true)) {
                                        stateText.text = message.payload?.toUpperCase(Locale.getDefault())
                                        if(message.payload.equals(mqttOptions.sensorFourState, ignoreCase = true)) {
                                            iconImageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_green))
                                        } else {
                                            iconImageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_red))
                                        }
                                    }
                                }*/
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get sensor message: $error") }))
    }

    companion object {
        fun newInstance(): SensorControlFragment {
            return SensorControlFragment()
        }
    }
}