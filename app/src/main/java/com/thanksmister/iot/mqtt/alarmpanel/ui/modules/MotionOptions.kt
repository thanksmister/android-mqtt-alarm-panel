/*
 * Copyright (c) 2018 LocalBuzz
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

import dpreference.DPreference
import javax.inject.Inject

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class MotionOptions @Inject
constructor(private val sharedPreferences: DPreference) {

    fun setMotionEnabled(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MOTION_ENABLED, value)
        setOptionsUpdated(true)
    }

    fun setOptionsUpdated(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MOTION_ENABLED, value)
    }

    fun hasUpdates(): Boolean {
        return sharedPreferences.getPrefBoolean(MOTION_OPTIONS_UPDATED, false)
    }

    companion object {
        const val PREF_MOTION_ENABLED = "pref_motion_enabled"
        const val MOTION_OPTIONS_UPDATED = "motion_options_updated"
    }
}