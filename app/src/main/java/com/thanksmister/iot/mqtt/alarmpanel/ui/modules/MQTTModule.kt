/*
 * Copyright (c) 2017. ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.ui.modules

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.ContextWrapper
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTService
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.NOTIFICATION_STATE_TOPIC
import org.eclipse.paho.client.mqttv3.MqttException
import timber.log.Timber

class MQTTModule (base: Context?, var mqttOptions: MQTTOptions, private val listener: MQTTListener) : ContextWrapper(base),
        LifecycleObserver,
        MQTTService.MqttManagerListener {

    private var mqttService: MQTTService? = null

    init {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun start() {
        Timber.d("start")
        if (mqttService == null) {
            try {
                mqttService = MQTTService(applicationContext, mqttOptions, this)
            } catch (t: Throwable) {
                // TODO should we loop back and try again?
                Timber.e("Could not create MQTTPublisher: " + t.message)
            }
        } else if (mqttOptions.hasUpdates()) {
            Timber.d("readMqttOptions().hasUpdates(): " + mqttOptions.hasUpdates())
            try {
                mqttService!!.reconfigure(applicationContext, mqttOptions, this)
                mqttOptions.setOptionsUpdated(false)
            } catch (t: Throwable) {
                // TODO should we loop back and try again?
                Timber.e("Could not create MQTTPublisher: " + t.message)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun stop() {
        Timber.d("stop")
        if (mqttService != null) {
            try {
                mqttService!!.close()
            } catch (e: MqttException) {
                e.printStackTrace()
            }
            mqttService = null
        }
    }

    fun restart() {
        Timber.d("restart")
        stop()
        start()
    }

    fun pause() {
        Timber.d("pause")
        stop()
    }

    fun publish(command : String) {
        Timber.d("command: " + command)
        if(mqttService != null) {
            mqttService!!.publish(command)
        }
    }

    fun resetMQttOptions(mqttOptions: MQTTOptions) {
        this.mqttOptions = mqttOptions
        if (mqttService != null && mqttOptions.hasUpdates()) {
            Timber.d("readMqttOptions().hasUpdates(): " + mqttOptions.hasUpdates())
            try {
                mqttService!!.reconfigure(applicationContext, mqttOptions, this)
                mqttOptions.setOptionsUpdated(false)
            } catch (t: Throwable) {
                // TODO should we loop back and try again?
                Timber.e("Could not create MQTTPublisher: " + t.message)
            }
        }
    }

    override fun subscriptionMessage(id: String, topic: String, payload: String) {
        Timber.d("topic: " + topic)
        if (mqttOptions.getNotificationTopic() == topic || (ALARM_STATE_TOPIC == topic && AlarmUtils.hasSupportedStates(payload))) {
            listener.onMQTTMessage(id, topic, payload)
        } else {
            Timber.e("We received some bad info: topic: $topic payload: $payload");
        }
    }

    override fun handleMqttException(errorMessage: String) {
        listener.onMQTTException(errorMessage)
    }

    override fun handleMqttDisconnected() {
        listener.onMQTTDisconnect()
    }

    interface MQTTListener {
        fun onMQTTDisconnect()
        fun onMQTTException(message : String)
        fun onMQTTMessage(id: String, topic: String, payload: String)
    }
}