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
package com.thanksmister.iot.mqtt.alarmpanel.network

import android.content.Context
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTService.MqttManagerListener
import org.eclipse.paho.client.mqttv3.MqttException

interface MQTTServiceInterface {
    val isReady: Boolean
    fun publishAlarm(action: String, code: Int)
    fun publishCommand(command: String, payload: String)
    fun reconfigure(context: Context, newOptions: MQTTOptions, listener: MqttManagerListener)
    @Throws(MqttException::class)
    fun close()
}