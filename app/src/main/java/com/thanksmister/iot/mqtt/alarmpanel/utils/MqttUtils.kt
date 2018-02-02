/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
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

        val mqttConnectOptions: MqttConnectOptions
            get() {
                val mqttConnectOptions = MqttConnectOptions()
                mqttConnectOptions.isAutomaticReconnect = true
                mqttConnectOptions.isCleanSession = false
                return mqttConnectOptions
            }

        fun getMqttConnectOptions(username: String, password: String): MqttConnectOptions {
            val mqttConnectOptions = MqttConnectOptions()
            mqttConnectOptions.isAutomaticReconnect = true
            mqttConnectOptions.isCleanSession = false

            if (!TextUtils.isEmpty(username)) {
                mqttConnectOptions.userName = username
            }

            if (!TextUtils.isEmpty(password)) {
                val passwordArray = password.toCharArray()
                mqttConnectOptions.password = passwordArray
            }

            return mqttConnectOptions
        }

        fun getMqttAndroidClient(context: Context, serverUri: String, clientId: String): MqttAndroidClient {
            val mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
            return mqttAndroidClient
        }

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
