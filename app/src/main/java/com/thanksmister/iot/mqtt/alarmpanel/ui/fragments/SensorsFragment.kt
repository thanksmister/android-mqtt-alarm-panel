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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import com.thanksmister.iot.mqtt.alarmpanel.ui.adapters.SensorAdapter
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.EditTextDialogView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SensorDialogView
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_DEVICE_SENSOR
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.SENSOR_GENERIC_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.SensorViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sensors.*
import kotlinx.android.synthetic.main.fragment_sensors.view.*
import timber.log.Timber
import javax.inject.Inject

class SensorsFragment : BaseFragment(), SensorAdapter.OnItemClickListener {

    @Inject lateinit var sensorViewModel: SensorViewModel
    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var mqttOptions: MQTTOptions

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(sensorViewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.getContext()
        view.sensorList.layoutManager = LinearLayoutManager(context)
        view.sensorList.adapter =  SensorAdapter(ArrayList<Sensor>(), COMMAND_DEVICE_SENSOR, this)
        addSensorButton.setOnClickListener {
            showAddSensorDialog(Sensor())
        }
        sensorTopic.setOnClickListener {
            showTopicDialog(COMMAND_DEVICE_SENSOR)
        }
        sensorTopicPrefixValue.text = COMMAND_DEVICE_SENSOR
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sensors, container, false)
    }

    private fun observeViewModel(viewModel: SensorViewModel) {
        disposable.add(viewModel.getItems()
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe({items ->
                   sensorList.adapter = SensorAdapter(items, COMMAND_DEVICE_SENSOR, this)
                   sensorList.invalidate()
               }, { error -> Timber.e("Unable to get sensors: " + error)}))
    }

    companion object {
        fun newInstance(): SensorsFragment {
            return SensorsFragment()
        }
    }

    override fun onItemClick(sensor: Sensor) {
        showAddSensorDialog(sensor)
    }

    private fun showTopicDialog(topic: String) {
        if (isAdded) {
            dialogUtils.showEditTextDialog(activity as BaseActivity, topic, object : EditTextDialogView.ViewListener {
                override fun onComplete(value: String) {
                    if(TextUtils.isEmpty(value)) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                    } else {
                        mqttOptions.setSensorTopic(value)
                    }
                }
                override fun onCancel() =
                        Toast.makeText(activity, getString(R.string.toast_changes_cancelled), Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun showAddSensorDialog(sensor: Sensor) {
        if (isAdded) {
            dialogUtils.showSensorDialog(activity as BaseActivity, sensor, COMMAND_DEVICE_SENSOR, object : SensorDialogView.ViewListener {
                override fun onComplete(sensor: Sensor) {
                    verifySensorAndCommit(sensor)
                }
                override fun onCancel() =
                        Toast.makeText(activity, getString(R.string.toast_changes_cancelled), Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun verifySensorAndCommit(sensor: Sensor) {
        Timber.d("verifySensorAndCommit")

        var valid = true
        if(TextUtils.isEmpty(sensor.topic)) {
            valid = false
        } else if(TextUtils.isEmpty(sensor.payloadActive)) {
            valid = false
        } else if(TextUtils.isEmpty(sensor.payloadInactive)) {
            valid = false
        }

        // if not valid show error and make no changes
        if(!valid) {
            dialogUtils.showAlertDialog(activity as AppCompatActivity, getString(R.string.error_sensor_empty_data))
            return;
        }

        // if not type set to generic
        if(TextUtils.isEmpty(sensor.type)) {
            sensor.type = SENSOR_GENERIC_TYPE
        }

        // just set the name if its empty
        if(TextUtils.isEmpty(sensor.name)) {
            sensor.name = context?.getString(R.string.text_sensor)
        }

        sensorViewModel.insertItem(sensor)
    }
}