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
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ScrollView
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import kotlinx.android.synthetic.main.dialog_sensor.view.*
import android.widget.ArrayAdapter
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_DEVICE_SENSOR
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.SENSOR_GENERIC_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.sensorTypes
import timber.log.Timber

class SensorDialogView : ScrollView {

    internal var sensorDialogListener: ViewListener? = null
    internal var sensor: Sensor = Sensor()
    internal var topicPrefix: String = ""

    interface ViewListener {
        fun onComplete(sensor: Sensor)
        fun onCancel()
    }

    fun setListener(listener: ViewListener) {
        this.sensorDialogListener = listener
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    fun setSensor(sensor: Sensor, topicPrefix: String) {
        this.sensor = sensor
        this.topicPrefix = topicPrefix

        if(TextUtils.isEmpty(sensor.name)) {
            sensorName.setText(context.getString(R.string.text_sensor))
        } else {
            sensorName.setText(sensor.name)
        }

        if(!TextUtils.isEmpty(sensor.topic)){
            sensorTopic.setText(sensor.topic)
        }

        if(!TextUtils.isEmpty(sensor.payloadActive)){
            sensorPayloadActive.setText(sensor.payloadActive)
        }

        if(!TextUtils.isEmpty(sensor.payloadInactive)){
            sensorPayloadInactive.setText(sensor.payloadInactive)
        }

        sensorAlarm.isChecked = sensor.alarmMode
        sensorNotification.isChecked = sensor.notify

        if(TextUtils.isEmpty(sensor.type)) {
            sensor.type = SENSOR_GENERIC_TYPE
        }
        val pos = sensorTypes.indexOf(sensor.type)
        sensorType.setSelection(pos)

        val topicDescription = context.getString(R.string.sensor_topic_description, topicPrefix)
        sensorTopicDescription.text = topicDescription
        sensorTopicPrefix.text = topicPrefix

        createSensorMQTT(sensor)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        sensorName.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sensor.name = s.toString()
                Timber.d("Sensor Name: " + sensor.name)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        sensorTopic.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sensor.topic = s.toString()
                createSensorMQTT(sensor)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        sensorPayloadActive.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sensor.payloadActive = s.toString()
                createSensorMQTT(sensor)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        sensorPayloadInactive.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sensor.payloadInactive = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        sensorNotification.setOnClickListener { sensor.notify = sensorNotification.isChecked }
        sensorAlarm.setOnClickListener { sensor.alarmMode = sensorAlarm.isChecked }

        val adapter = ArrayAdapter.createFromResource(context, R.array.sensor_types , android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sensorType.adapter = adapter;
        sensorType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sensor.type = sensorTypes[position]
                Timber.d("Sensor Type: " + sensor.type)
            }
        }
    }

    private fun createSensorMQTT(sensor: Sensor) {
        var payload = sensor.payloadActive
        var topic = COMMAND_DEVICE_SENSOR + sensor.topic
        if(TextUtils.isEmpty(sensor.topic)) {
            topic = COMMAND_DEVICE_SENSOR + "frontdoor"
        }
        if(TextUtils.isEmpty(sensor.payloadActive)) {
            payload = "open"
        }
        sensorMQTT.text = context.getString(R.string.text_sensor_mqtt_output, topic, payload)
    }
}