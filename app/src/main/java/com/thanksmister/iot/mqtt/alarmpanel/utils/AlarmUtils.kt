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

import android.support.annotation.StringDef

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.ArrayList

/**
 * Just a utility class to work with the specific settings of the Home Assistant
 * MQTT Manual Alarm Control Panel https://home-assistant.io/components/alarm_control_panel.manual_mqtt/.
 */
class AlarmUtils {

    annotation class AlarmStates
    annotation class AlarmCommands

    companion object {

        @JvmField val MODE_ARM_HOME = "mode_arm_home"
        @JvmField val MODE_ARM_HOME_PENDING = "mode_arm_home_pending"
        @JvmField val MODE_ARM_PENDING = "mode_arm_pending"
        @JvmField val MODE_ARM_AWAY = "mode_arm_away"
        @JvmField val MODE_ARM_AWAY_PENDING = "mode_arm_away_pending"
        @JvmField val MODE_DISARM = "mode_disarm"
        @JvmField val MODE_TRIGGERED = "mode_triggered"
        @JvmField val MODE_TRIGGERED_PENDING = "mode_triggered_pending"
        @JvmField val MODE_AWAY_TRIGGERED_PENDING = "mode_triggered_away_pending"
        @JvmField val MODE_HOME_TRIGGERED_PENDING = "mode_triggered_home_pending"

        @JvmField val PORT = 1883

        @JvmField val ALARM_TYPE = "ALARM"

        @JvmField val COMMAND_ARM_HOME = "ARM_HOME"
        @JvmField val COMMAND_ARM_AWAY = "ARM_AWAY"
        @JvmField val COMMAND_DISARM = "DISARM"

        @JvmField val ALARM_COMMAND_TOPIC = "home/alarm/set"
        @JvmField val ALARM_STATE_TOPIC = "home/alarm"

        @JvmField val STATE_DISARM = "disarmed"
        @JvmField val STATE_ARM_AWAY = "armed_away"
        @JvmField val STATE_ARM_HOME = "armed_home"
        @JvmField val STATE_PENDING = "pending"
        @JvmField val STATE_TRIGGERED = "triggered"
        @JvmField val STATE_ERROR = "error"

        @JvmField val PENDING_TIME = 60
        @JvmField val PENDING_HOME_TIME = 60
        @JvmField val PENDING_AWAY_TIME = 60
        @JvmField val DELAY_TIME = 30
        @JvmField val DELAY_HOME_TIME = 30
        @JvmField val DELAY_AWAY_TIME = 30
        @JvmField val DISABLE_TIME = 30

        private val supportedCommands = ArrayList<String>()
        private val supportedStates = ArrayList<String>()

        init {
            supportedCommands.add(COMMAND_ARM_HOME)
            supportedCommands.add(COMMAND_ARM_AWAY)
            supportedCommands.add(COMMAND_DISARM)
        }

        init {
            supportedStates.add(STATE_DISARM)
            supportedStates.add(STATE_ARM_AWAY)
            supportedStates.add(STATE_ARM_HOME)
            supportedStates.add(STATE_PENDING)
            supportedStates.add(STATE_TRIGGERED)
        }

        /**
         * Topic is of type command topic
         * @param command
         * @return
         */
        @AlarmCommands
        fun hasSupportedCommands(command: String): Boolean {
            return supportedCommands.contains(command)
        }

        /**
         * Topic is of type state topic
         * @param state
         * @return
         */
        @AlarmStates
        fun hasSupportedStates(state: String): Boolean {
            return supportedStates.contains(state)
        }
    }
}