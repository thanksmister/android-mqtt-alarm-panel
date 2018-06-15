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

import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration

import java.util.ArrayList

/**
 * Just a utility class to work with the multiple topics and commands for different components.
 */
class ComponentUtils {

    annotation class SensorTypes

    companion object {

        val NOTIFICATION_STATE_TOPIC = "home/notification"
        val NOTIFICATION_TYPE = "NOTIFICATION"

        val SENSOR_STATE_TOPIC = "home/sensor/"
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