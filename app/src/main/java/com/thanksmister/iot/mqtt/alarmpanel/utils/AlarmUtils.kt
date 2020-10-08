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

import androidx.annotation.StringDef

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

        const val MODE_ARM_HOME = "mode_arm_home"
        const val MODE_ARM_HOME_PENDING = "mode_arm_home_pending"
        const val MODE_ARM_PENDING = "mode_arm_pending"
        const val MODE_ARM_AWAY = "mode_arm_away"
        const val MODE_ARM_AWAY_PENDING = "mode_arm_away_pending"
        const val MODE_DISARM = "mode_disarm"
        const val MODE_TRIGGERED = "mode_triggered"
        const val MODE_TRIGGERED_PENDING = "mode_triggered_pending"
        const val MODE_AWAY_TRIGGERED_PENDING = "mode_triggered_away_pending"
        const val MODE_HOME_TRIGGERED_PENDING = "mode_triggered_home_pending"
    }
}