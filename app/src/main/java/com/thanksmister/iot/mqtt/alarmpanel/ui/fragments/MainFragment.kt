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
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.adapters.SensorDisplayAdapter
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
        fun showCodeDialog(type: CodeTypes, delay: Int?)
        fun showAlarmTriggered()
        fun hideTriggeredView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMainFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnMainFragmentListener")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        observeViewModel(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorsDisplayList.layoutManager = LinearLayoutManager(context)
        sensorsDisplayList.adapter = sensorAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    private val sensorAdapter: SensorDisplayAdapter by lazy {
        SensorDisplayAdapter()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun observeViewModel(viewModel: MainViewModel) {
        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    Timber.d("Alarm state: $state")
                    Timber.d("Alarm mode: ${viewModel.getAlarmMode()}")
                    val payload = state.payload
                    activity?.runOnUiThread {
                        when (payload) {
                            MqttUtils.STATE_ARMED_AWAY,
                            MqttUtils.STATE_ARMED_NIGHT,
                            MqttUtils.STATE_ARMED_HOME -> {
                                dialogUtils.clearDialogs()
                            }
                            MqttUtils.STATE_ARMING -> {
                                dialogUtils.clearDialogs()
                            }
                            MqttUtils.STATE_DISARMED -> {
                                dialogUtils.clearDialogs()
                                listener?.hideTriggeredView()
                            }
                            MqttUtils.STATE_PENDING -> {
                                dialogUtils.clearDialogs()
                            }
                            MqttUtils.STATE_TRIGGERED -> {
                                dialogUtils.clearDialogs()
                                listener?.showAlarmTriggered()
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get message: $error") }))

        disposable.add(viewModel.getSensors()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    if (items.isNullOrEmpty()) {
                        handleDisplayAlignment(false)
                        sensorsDisplayList.visibility = View.GONE
                    } else {
                        handleDisplayAlignment(true)
                        sensorsDisplayList.visibility = View.VISIBLE
                        sensorAdapter.setItems(items)
                    }
                }, { error ->
                    Timber.e("Unable to get sensors: $error")
                }))
    }

    private fun handleDisplayAlignment(hasSensors: Boolean) {
        val metrics = resources.displayMetrics
        val scaleFactor = metrics.density
        val widthDp = metrics.widthPixels / scaleFactor
        val heightDp = metrics.heightPixels / scaleFactor
        val orientation = resources.configuration.orientation
        val smallestWidth = widthDp.coerceAtMost(heightDp)
        if (hasSensors.not()) {
            if (orientation == ORIENTATION_LANDSCAPE) {
                when {
                    smallestWidth >= 720 -> {
                        guideControlMiddle?.setGuidelinePercent(0.80f)
                        guidelineStart?.setGuidelinePercent(0.2f)
                    }
                    smallestWidth >= 600 -> {
                        guideControlMiddle?.setGuidelinePercent(0.78f)
                        guidelineStart?.setGuidelinePercent(0.22f)
                    }
                    smallestWidth >= 500 -> {
                        guideControlMiddle?.setGuidelinePercent(0.78f)
                        guidelineStart?.setGuidelinePercent(0.22f)
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
                    smallestWidth >= 600 -> {
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
    }

    companion object {
        @JvmStatic
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}