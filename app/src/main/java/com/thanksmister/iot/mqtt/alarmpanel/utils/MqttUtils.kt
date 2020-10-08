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

        // TODO use the rest of these in the app
        const val TYPE_ALARM = "alarm"
        const val TYPE_CONTROL = "control"
        const val TYPE_COMMAND = "command"

        const val PORT = 1883
        const val TOPIC_COMMAND = "command"
        const val COMMAND_STATE = "state"
        const val PANIC_STATE = "panic"

        const val VALUE = "value"
        const val ACTION = "value"
        const val CODE = "code"

        const val NOTIFICATION_STATE_TOPIC = "home/notification"
        const val NOTIFICATION_TYPE = "NOTIFICATION"
        
        const val COMMAND_SENSOR_FACE = "sensor/face"
        const val COMMAND_SENSOR_QR_CODE = "sensor/qrcode"
        const val COMMAND_SENSOR_MOTION = "sensor/motion"

        const val STATE_CURRENT_URL = "currentUrl"

        const val STATE_SCREEN_ON = "screenOn"
        const val STATE_BRIGHTNESS = "brightness"
        const val COMMAND_SENSOR_PREFIX = "sensor/"

        const val COMMAND_WAKE = "wake"
        const val COMMAND_AUDIO = "audio"
        const val COMMAND_SPEAK = "speak"
        const val COMMAND_NOTIFICATION = "notification"
        const val COMMAND_ALERT = "alert"
        const val COMMAND_DEVICE_SENSOR = "sensor"
        const val COMMAND_CAPTURE = "capture"
        const val COMMAND_WEATHER = "weather"
        const val COMMAND_SUN = "sun"

        const val SENSOR_TYPE = "SENSOR"
        const val SENSOR_GENERIC_TYPE = "GENERIC"
        const val SENSOR_DOOR_TYPE = "DOOR"
        const val SENSOR_WINDOW_TYPE = "WINDOW"
        const val SENSOR_SOUND_TYPE = "SOUND"
        const val SENSOR_MOTION_TYPE = "MOTION"
        const val SENSOR_CAMERA_TYPE = "CAMERA"

        // commands
        const val COMMAND_ARM_HOME = "ARM_HOME"
        const val COMMAND_ARM_NIGHT = "ARM_NIGHT"
        const val COMMAND_ARM_AWAY = "ARM_AWAY"
        const val COMMAND_DISARM = "DISARM"
        const val COMMAND_ON = "ON"

        // mqtt states
        const val STATE_DISARMED = "disarmed"
        const val STATE_ARMED_AWAY = "armed_away"
        const val STATE_ARMED_HOME = "armed_home"
        const val STATE_ARMED_NIGHT = "armed_night"
        const val STATE_PENDING = "pending"
        const val STATE_ARMING = "arming"
        const val STATE_ARMING_AWAY = "arming_away"
        const val STATE_ARMING_HOME = "arming_home"
        const val STATE_ARMING_NIGHT = "arming_night"

        //const val DEFAULT_ALERT_TOPIC = "home/alert/set"
        const val DEFAULT_COMMAND_TOPIC = "home/alarm/set"
        const val DEFAULT_CONFIG_TOPIC = "home/alarm/config"
        const val DEFAULT_STATUS_TOPIC = "home/alarm/status"
        const val DEFAULT_STATE_TOPIC = "home/alarm"
        const val DEFAULT_PANEL_COMMAND_TOPIC = "alarmpanel"
        const val DEFAULT_INVALID = "INVALID"

        const val STATE_TRIGGERED = "triggered"
        const val STATE_DISABLED = "disabled"

        private val supportedCommands = ArrayList<String>()
        private val supportedStates = ArrayList<String>()
        val sensorTypes = java.util.ArrayList<String>()
        
        init {
            supportedCommands.add(COMMAND_ARM_HOME)
            supportedCommands.add(COMMAND_ARM_AWAY)
            supportedCommands.add(COMMAND_ARM_NIGHT)
            supportedCommands.add(COMMAND_DISARM)
            supportedStates.add(STATE_DISARMED)
            supportedStates.add(STATE_ARMED_AWAY)
            supportedStates.add(STATE_ARMED_HOME)
            supportedStates.add(STATE_PENDING)
            supportedStates.add(STATE_ARMING)
            supportedStates.add(STATE_ARMING_AWAY)
            supportedStates.add(STATE_ARMING_HOME)
            supportedStates.add(STATE_ARMING_NIGHT)
            supportedStates.add(STATE_TRIGGERED)
            sensorTypes.add(SENSOR_GENERIC_TYPE)
            sensorTypes.add(SENSOR_DOOR_TYPE)
            sensorTypes.add(SENSOR_WINDOW_TYPE)
            sensorTypes.add(SENSOR_SOUND_TYPE)
            sensorTypes.add(SENSOR_MOTION_TYPE)
            sensorTypes.add(SENSOR_CAMERA_TYPE)
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
