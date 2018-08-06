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

import java.util.ArrayList

/**
 * Just a utility class to work with the multiple topics and commands for different components.
 */
class ComponentUtils {

    annotation class SensorTypes

    companion object {

        val NOTIFICATION_STATE_TOPIC = "home/notification"
        val NOTIFICATION_TYPE = "NOTIFICATION"

        const val BASE_TOPIC = "alarmpanel"

        const val TOPIC_COMMAND = "command"
        const val COMMAND_STATE = "state"
        const val VALUE = "value"
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

        val SESOR_TYPE = "SENSOR"
        val SENSOR_GENERIC_TYPE = "GENERIC"
        val SENSOR_DOOR_TYPE = "DOOR"
        val SENSOR_WINDOW_TYPE = "WINDOW"
        val SENSOR_SOUND_TYPE = "SOUND"
        val SENSOR_MOTION_TYPE = "MOTION"
        val SENSOR_CAMERA_TYPE = "CAMERA"

        val sensorTypes = ArrayList<String>()

        init {
            sensorTypes.add(SENSOR_GENERIC_TYPE)
            sensorTypes.add(SENSOR_DOOR_TYPE)
            sensorTypes.add(SENSOR_WINDOW_TYPE)
            sensorTypes.add(SENSOR_SOUND_TYPE)
            sensorTypes.add(SENSOR_MOTION_TYPE)
            sensorTypes.add(SENSOR_CAMERA_TYPE)
        }

        /**
         * Sensor is of sensor type
         * @param type
         * @return
         */
        @SensorTypes
        fun hasSensorType(type: String): Boolean {
            return sensorTypes.contains(type)
        }
    }
}