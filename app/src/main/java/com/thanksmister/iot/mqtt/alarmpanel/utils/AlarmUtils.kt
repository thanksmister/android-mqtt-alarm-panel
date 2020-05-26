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
 * Just a utility class to work with the specific settings of the Home Assistant
 * MQTT Manual Alarm Control Panel https://home-assistant.io/components/alarm_control_panel.manual_mqtt/.
 */
class AlarmUtils {

    annotation class AlarmStates
    annotation class AlarmCommands

    companion object {

        // internal for setting state
        @Deprecated("Just use MQTT states")
        const val MODE_ARMED_HOME = "mode_arm_home"
        @Deprecated("Just use MQTT states")
        const val MODE_ARM_HOME_PENDING = "mode_arm_home_pending"
        @Deprecated("Just use MQTT states")
        const val MODE_ARM_HOME_ARMING = "mode_arm_home_arming"
        @Deprecated("Just use MQTT states")
        const val MODE_ARM_NIGHT_ARMING = "mode_arm_night_arming"
        @Deprecated("Just use MQTT states")
        const val MODE_ARM_PENDING = "mode_arm_pending"
        @Deprecated("Just use MQTT states")
        const val MODE_ARM_ARMING = "mode_arm_arming"
        @Deprecated("Just use MQTT states")
        const val MODE_ARMED_AWAY = "mode_arm_away"
        @Deprecated("Just use MQTT states")
        const val MODE_ARMED_NIGHT = "mode_arm_night"
        @Deprecated("Just use MQTT states")
        const val MODE_ARM_AWAY_PENDING = "mode_arm_away_pending"
        @Deprecated("Just use MQTT states")
        const val MODE_ARM_AWAY_ARMING = "mode_arm_away_arming"

        @Deprecated("Just use MQTT states")
        const val MODE_DISARM = "mode_disarm"
        @Deprecated("Just use MQTT states")
        const val MODE_DISARMING = "mode_disarming"
        @Deprecated("Just use MQTT states")
        const val MODE_ARMING = "mode_arming"
        @Deprecated("Just use MQTT states")
        const val MODE_TRIGGERED = "mode_triggered"
        @Deprecated("Just use MQTT states")
        const val MODE_TRIGGERED_PENDING = "mode_triggered_pending"
        @Deprecated("Just use MQTT states")
        const val MODE_AWAY_TRIGGERED_PENDING = "mode_triggered_away_pending"
        @Deprecated("Just use MQTT states")
        const val MODE_HOME_TRIGGERED_PENDING = "mode_triggered_home_pending"
        @Deprecated("Just use MQTT states")
        const val MODE_NIGHT_TRIGGERED_PENDING = "mode_triggered_night_pending"

        const val PORT = 1883

        const val ALARM_TYPE = "ALARM"

        // commands
        const val COMMAND_ARM_HOME = "ARM_HOME"
        const val COMMAND_ARM_NIGHT = "ARM_NIGHT"
        const val COMMAND_ARM_AWAY = "ARM_AWAY"
        const val COMMAND_DISARM = "DISARM"
        const val COMMAND_ARM_CUSTOM_BYPASS = "ARM_CUSTOM_BYPASS"

        const val ALARM_COMMAND_TOPIC = "home/alarm/set"
        const val ALARM_STATE_TOPIC = "home/alarm"

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

        const val PENDING_TIME = 60
        const val ARMING_TIME = 60
        const val DISARMING_TIME = 60
        const val PENDING_HOME_TIME = 60
        const val PENDING_AWAY_TIME = 60
        const val DELAY_TIME = 30
        const val DELAY_HOME_TIME = 30
        const val DELAY_AWAY_TIME = 30
        const val DELAY_NIGHT_TIME = 30
        const val DISABLE_TIME = 30

        private val supportedCommands = ArrayList<String>()
        private val supportedStates = ArrayList<String>()

        init {
            supportedCommands.add(COMMAND_ARM_HOME)
            supportedCommands.add(COMMAND_ARM_AWAY)
            supportedCommands.add(COMMAND_ARM_NIGHT)
            supportedCommands.add(COMMAND_DISARM)
            supportedCommands.add(COMMAND_ARM_CUSTOM_BYPASS)
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