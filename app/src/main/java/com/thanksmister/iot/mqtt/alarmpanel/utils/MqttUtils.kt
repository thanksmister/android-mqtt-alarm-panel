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

package com.thanksmister.iot.mqtt.alarmpanel.utils

import android.content.Context
import android.text.TextUtils

import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTService

import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

import timber.log.Timber

class MqttUtils {

    companion object {

        const val TYPE_ALARM = "alarm"
        const val TYPE_CONTROL = "control"
        const val TYPE_COMMAND = "command"

        const val PORT = 1883

        const val BASE_TOPIC = "alarmpanel"

        const val TOPIC_COMMAND = "command"
        const val VALUE = "value"

        const val COMMAND_AUDIO = "audio"
        const val COMMAND_SPEAK = "speak"
        const val COMMAND_NOTIFICATION = "notification"
        const val COMMAND_ALERT = "alert"

        // commands
        const val COMMAND_ARM_HOME = "ARM_HOME"
        const val COMMAND_ARM_NIGHT = "ARM_NIGHT"
        const val COMMAND_ARM_AWAY = "ARM_AWAY"
        const val COMMAND_DISARM = "DISARM"
        const val COMMAND_ARM_CUSTOM_BYPASS = "ARM_CUSTOM_BYPASS"
        const val COMMAND_ON = "ON"
        const val COMMAND_OFF = "OFF"
        const val COMMAND_OPEN = "OPEN"
        const val COMMAND_CLOSE = "CLOSE"

        const val ALARM_COMMAND_TOPIC = "home/alarm/set"
        const val ALARM_STATE_TOPIC = "home/alarm"

        const val GARAGE_COMMAND_TOPIC = "home/garage/set"
        const val GARAGE_STATE_TOPIC = "home/garage"

        const val DOOR_COMMAND_TOPIC = "home/door/set"
        const val DOOR_STATE_TOPIC = "home/door"

        const val ALERT_COMMAND_TOPIC = "home/alert/set"
        const val ALERT_STATE_TOPIC = "home/alert"

        // mqtt states
        const val STATE_DISARMED = "disarmed"
        const val STATE_ARMED_AWAY = "armed_away"
        const val STATE_ARMED_HOME = "armed_home"
        const val STATE_ARMED_CUSTOM_BYPASS = "armed_custom_bypass"
        const val STATE_ARMED_NIGHT = "armed_night"
        const val STATE_PENDING = "pending"
        const val STATE_ARMING = "arming"
        const val STATE_DISARMING = "disarming"
        const val STATE_TRIGGERED = "triggered"
        const val STATE_DISABLED = "disabled"
        const val STATE_ON = "on"
        const val STATE_OFF = "off"
        const val STATE_OPEN = "open"
        const val STATE_CLOSE = "close"

        private val supportedCommands = ArrayList<String>()
        private val supportedStates = ArrayList<String>()

        init {
            supportedCommands.add(COMMAND_ARM_HOME)
            supportedCommands.add(COMMAND_ARM_AWAY)
            supportedCommands.add(COMMAND_ARM_NIGHT)
            supportedCommands.add(COMMAND_DISARM)
            supportedCommands.add(COMMAND_ARM_CUSTOM_BYPASS)
            supportedCommands.add(COMMAND_ON)
            supportedCommands.add(COMMAND_OFF)
            supportedCommands.add(COMMAND_CLOSE)
        }

        init {
            supportedStates.add(STATE_DISARMED)
            supportedStates.add(STATE_ARMED_AWAY)
            supportedStates.add(STATE_ARMED_HOME)
            supportedStates.add(STATE_ARMED_CUSTOM_BYPASS)
            supportedStates.add(STATE_PENDING)
            supportedStates.add(STATE_ARMING)
            supportedStates.add(STATE_DISARMING)
            supportedStates.add(STATE_TRIGGERED)
            supportedStates.add(STATE_ON)
            supportedStates.add(STATE_OFF)
            supportedStates.add(STATE_OPEN)
            supportedStates.add(STATE_CLOSE)
        }

        @Deprecated ("We don't need a callback for the client.")
        fun getMqttAndroidClient(context: Context, serverUri: String, clientId: String,
                                 mqttCallbackExtended: MqttCallbackExtended): MqttAndroidClient {
            val mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
            mqttAndroidClient.setCallback(mqttCallbackExtended)
            return mqttAndroidClient
        }

        /**
         * We need to make an array of listeners to pass to the subscribe topics.
         * @param length
         * @return
         */
        fun getMqttMessageListeners(length: Int, listener: MQTTService.MqttManagerListener?): Array<IMqttMessageListener?> {
            val mqttMessageListeners = arrayOfNulls<IMqttMessageListener>(length)
            for (i in 0 until length) {
                val mqttMessageListener = IMqttMessageListener { topic, message ->
                    Timber.i("Subscribe Topic: " + topic + "  Payload: " + String(message.payload))
                    Timber.i("Subscribe Topic Listener: " + listener!!)
                    listener.subscriptionMessage(message.id.toString(), topic, String(message.payload))
                }
                mqttMessageListeners[i] = mqttMessageListener
            }
            return mqttMessageListeners
        }

        /**
         * Generate an array of QOS values for subscribing to multiple topics.
         * @param length
         * @return
         */
        fun getQos(length: Int): IntArray {
            val qos = IntArray(length)
            for (i in 0 until length) {
                qos[i] = 0
            }
            return qos
        }
    }
}
